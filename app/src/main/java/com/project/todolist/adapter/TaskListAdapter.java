package com.project.todolist.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.todolist.R;
import com.project.todolist.activity.AddEditTaskActivity;
import com.project.todolist.database.DatabaseManager;
import com.project.todolist.database.entity.Attachment;
import com.project.todolist.database.entity.Task;
import com.project.todolist.database.entity.TaskWithAttachments;

import java.util.List;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.ViewHolder> {
    private final List<TaskWithAttachments> data;
    private final DatabaseManager databaseManager;

    public TaskListAdapter(Context context, List<TaskWithAttachments> data) {
        this.data = data;
        this.databaseManager = new DatabaseManager(context);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox checkBox;
        private final ImageView attachmentIcon;

        public ViewHolder(View view) {
            super(view);
            checkBox = view.findViewById(R.id.checkBox);
            attachmentIcon = view.findViewById(R.id.image_attachment);
        }

        public CheckBox getCheckBox() {
            return checkBox;
        }
        public ImageView getAttachmentIcon() {
            return attachmentIcon;
        }
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
        TaskWithAttachments taskWithAttachments = data.get(position);
        CheckBox checkBox = viewHolder.getCheckBox();
        ImageView attachmentIcon = viewHolder.getAttachmentIcon();
        checkBox.setOnCheckedChangeListener(null);
        prepareView(checkBox, attachmentIcon, taskWithAttachments);
        checkBox.setOnCheckedChangeListener(
                (buttonView, isChecked) -> checkBoxOnCheckedChanged(
                        isChecked,
                        taskWithAttachments,
                        viewHolder
                )
        );

        LinearLayout linearLayout = viewHolder.itemView.findViewById(R.id.linear_layotu_row);
        linearLayout.setOnClickListener(v -> openAddEditActivity(v.getContext(), taskWithAttachments));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private void prepareView(CheckBox checkBox, ImageView attachmentIcon, TaskWithAttachments taskWithAttachments) {
        Task task = taskWithAttachments.getTask();
        List<Attachment> attachments = taskWithAttachments.getAttachments();
        checkBox.setText(task.toString());
        checkBox.setChecked(task.isDone());
        attachmentIcon.setVisibility(attachments.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void checkBoxOnCheckedChanged(
            boolean isChecked,
            TaskWithAttachments taskWithAttachments,
            ViewHolder viewHolder
    ) {
        Task task = taskWithAttachments.getTask();
        task.setDone(isChecked);

        int oldPosition = data.indexOf(taskWithAttachments);
        data.sort((task1, task2) -> Boolean.compare(task1.getTask().isDone(), task2.getTask().isDone()));
        int newPosition = data.indexOf(taskWithAttachments);
        if (oldPosition != newPosition) {
            viewHolder.itemView.post(() -> notifyItemMoved(oldPosition, newPosition));
        } else {
            viewHolder.itemView.post(() -> notifyItemChanged(oldPosition));
        }

        databaseManager.updateTask(task, () -> {});
    }

    private void openAddEditActivity(Context context, TaskWithAttachments task) {
        Intent intent = new Intent(context, AddEditTaskActivity.class);
        intent.putExtra("task", task);
        context.startActivity(intent);
    }

    public void setData(List<TaskWithAttachments> data) {
        this.data.clear();
        this.data.addAll(data);
        notifyDataSetChanged();
    }
}
