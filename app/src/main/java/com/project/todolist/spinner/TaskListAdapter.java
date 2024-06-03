package com.project.todolist.spinner;

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

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static com.project.todolist.Utils.*;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.ViewHolder> {
    private final List<Task> data;

    private final Context context;
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

    public TaskListAdapter(Context context, List<Task> data, AppDatabase database) {
        this.context = context;
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
        Task task = data.get(position);
        CheckBox checkBox = viewHolder.getCheckBox();

        checkBox.setOnCheckedChangeListener(
                (buttonView, isChecked) -> checkBoxOnCheckedChanged(
                        isChecked,
                        task,
                        viewHolder
                )
        );

        prepareView(checkBox, task);

        LinearLayout linearLayout = viewHolder.itemView.findViewById(R.id.linear_layotu_row);
        linearLayout.setOnClickListener(v -> openAddEditActivity(v.getContext(), task));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private void prepareView(CheckBox checkBox, Task task) {
        checkBox.setText(task.toString());
        checkBox.setChecked(task.isDone());
    }

    private void checkBoxOnCheckedChanged(
            boolean isChecked,
            Task task,
            ViewHolder viewHolder
    ) {
        task.setDone(isChecked);

        int oldPosition = data.indexOf(task);
        data.sort((task1, task2) -> Boolean.compare(task1.isDone(), task2.isDone()));
        int newPosition = data.indexOf(task);
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
                .subscribe(
                        () -> {},
                        throwable -> displayToast(context, "Cannot update task state")
                );
    }

    private void openAddEditActivity(Context context, Task task) {
        Intent intent = new Intent(context, AddEditTaskActivity.class);
        intent.putExtra("task", task);
        context.startActivity(intent);
    }

    public void setData(List<Task> data) {
        this.data.clear();
        this.data.addAll(data);
        notifyDataSetChanged();
    }
}
