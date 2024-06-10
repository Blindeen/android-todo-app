package com.project.todolist.database;

import static com.project.todolist.utils.Utils.displayToast;

import android.content.Context;

import com.project.todolist.database.dao.AttachmentDao;
import com.project.todolist.database.dao.CategoryDao;
import com.project.todolist.database.dao.TaskDao;
import com.project.todolist.database.entity.Attachment;
import com.project.todolist.database.entity.Category;
import com.project.todolist.database.entity.Task;
import com.project.todolist.database.entity.TaskWithAttachments;

import java.util.List;
import java.util.function.Consumer;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DatabaseManager {
    private final Context context;
    private final TaskDao taskDao;
    private final CategoryDao categoryDao;
    private final AttachmentDao attachmentDao;

    public DatabaseManager(Context context) {
        AppDatabase database = AppDatabase.getDatabase(context);
        this.context = context;
        this.taskDao = database.taskDao();
        this.categoryDao = database.categoryDao();
        this.attachmentDao = database.attachmentDao();
    }

    public Disposable addTask(Task task, Consumer<Long> onSuccess) {
        Single<Long> completable = taskDao.insertTask(
                task.getTitle(),
                task.getDescription(),
                task.getDoneAt(),
                task.getCategoryId(),
                task.isNotification()
        );
        return completable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        onSuccess::accept,
                        throwable -> displayToast(context, "Failed to add task")
                );
    }

    public Disposable fetchTasks(
            String titlePattern,
            boolean hideDoneTasks,
            Long chosenCategory,
            Consumer<List<TaskWithAttachments>> onSuccess
    ) {
        Single<List<TaskWithAttachments>> taskList = taskDao.getTasks(titlePattern, hideDoneTasks, chosenCategory);
        return taskList
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        onSuccess::accept,
                        throwable -> displayToast(context, "Unable to fetch tasks")
                );
    }

    public Disposable updateTask(Task task, Runnable onSuccess) {
        Completable completable = taskDao.updateTask(task);
        return completable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        onSuccess::run,
                        throwable -> displayToast(context, "Unable to update task")
                );
    }

    public Disposable deleteTask(Task task, Runnable onSuccess) {
        Completable completable = taskDao.deleteTask(task);
        return completable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        onSuccess::run,
                        throwable -> displayToast(context, "Failed to delete task")
                );
    }

    public Disposable fetchCategories(Consumer<List<Category>> onSuccess) {
        Single<List<Category>> categoryListSingle = categoryDao.getAll();
        return categoryListSingle
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        onSuccess::accept,
                        throwable -> displayToast(context, "Unable to fetch categories")
                );
    }

    public Disposable addAttachment(Attachment attachment, Consumer<Long> onSuccess) {
        Single<Long> completable = attachmentDao.insertAttachment(attachment);
        return completable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        onSuccess::accept,
                        throwable -> displayToast(context, "Failed to add attachment")
                );
    }

    public Disposable addAttachments(List<Attachment> attachments) {
        Completable completable = attachmentDao.insertAttachments(attachments);
        return completable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                        },
                        throwable -> displayToast(context, "Failed to add attachments")
                );
    }

    public Disposable deleteAttachment(Attachment attachment, Runnable onSuccess) {
        Completable completable = attachmentDao.deleteAttachment(attachment);
        return completable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        onSuccess::run,
                        throwable -> displayToast(context, "Failed to delete attachment")
                );
    }

    public Disposable deleteAttachments(List<Attachment> attachments) {
        Completable completable = attachmentDao.deleteAttachments(attachments);
        return completable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                        },
                        throwable -> displayToast(context, "Failed to delete attachments")
                );
    }
}
