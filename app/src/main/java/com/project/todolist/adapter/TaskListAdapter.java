package com.project.todolist.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.todolist.R;
import com.project.todolist.activity.AddEditTaskActivity;
import com.project.todolist.db.AppDatabase;
import com.project.todolist.db.entity.Task;
import com.project.todolist.db.entity.TaskWithCategory;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.ViewHolder> {
    private final List<TaskWithCategory> data;
    private final AppDatabase database;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox checkBox;

        public ViewHolder(View view) {
            super(view);

            checkBox = view.findViewById(R.id.checkBox);
        }

        public CheckBox getCheckBox() {
            return checkBox;
        }
    }

    public TaskListAdapter(List<TaskWithCategory> data, AppDatabase database) {
        this.data = data;
        this.database = database;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recycler_view_row_item, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        TaskWithCategory taskWithCategory = data.get(position);
        Task task = taskWithCategory.getTask();
        CheckBox checkBox = viewHolder.getCheckBox();
        prepareView(checkBox, task);

        checkBox.setOnCheckedChangeListener(
                (buttonView, isChecked) -> checkBoxOnCheckedChanged(isChecked, taskWithCategory, viewHolder)
        );

        LinearLayout linearLayout = viewHolder.itemView.findViewById(R.id.linear_layotu_row);
        linearLayout.setOnClickListener(v -> openAddEditActivity(v.getContext(), task));
    }

    private void prepareView(CheckBox checkBox, Task task) {
        checkBox.setText(task.toString());
        checkBox.setChecked(task.isDone());
    }

    private void checkBoxOnCheckedChanged(
            boolean isChecked,
            TaskWithCategory taskWithCategory,
            ViewHolder viewHolder
    ) {
        Task task = taskWithCategory.getTask();
        task.setDone(isChecked);

        int oldPosition = data.indexOf(taskWithCategory);
        data.sort((task1, task2) -> Boolean.compare(task1.getTask().isDone(), task2.getTask().isDone()));
        int newPosition = data.indexOf(taskWithCategory);
        if (oldPosition != newPosition) {
            viewHolder.itemView.post(() -> notifyItemMoved(oldPosition, newPosition));
        } else {
            viewHolder.itemView.post(() -> notifyItemChanged(oldPosition));
        }

        updateTask(task);
    }

    private void updateTask(Task task) {
        Completable completable = database.taskDao().updateTask(task);
        Disposable updateTaskQuerySubscriber = completable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {}, throwable -> {});
    }

    private void openAddEditActivity(Context context, Task task) {
        Intent intent = new Intent(context, AddEditTaskActivity.class);
        intent.putExtra("task", task);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
