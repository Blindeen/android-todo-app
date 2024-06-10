package com.project.todolist.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.todolist.R;
import com.project.todolist.database.DatabaseManager;
import com.project.todolist.database.entity.Attachment;

import java.util.List;

public class AttachmentListAdapter extends RecyclerView.Adapter<AttachmentListAdapter.ViewHolder> {
    private final List<Attachment> data;
    private final DatabaseManager databaseManager;

    public AttachmentListAdapter(List<Attachment> data, DatabaseManager databaseManager) {
        this.data = data;
        this.databaseManager = databaseManager;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView attachmentName;
        private final ImageButton deleteAttachmentButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            attachmentName = itemView.findViewById(R.id.text_attachment_name);
            deleteAttachmentButton = itemView.findViewById(R.id.button_attachment_delete);
        }

        public TextView getAttachmentName() {
            return attachmentName;
        }

        public ImageButton getDeleteAttachmentButton() {
            return deleteAttachmentButton;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recycler_view_attachment_row_item, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        Attachment attachment = data.get(position);
        TextView attachmentName = viewHolder.getAttachmentName();
        ImageButton deleteAttachmentButton = viewHolder.getDeleteAttachmentButton();

        attachmentName.setText(attachment.getName());
        deleteAttachmentButton.setOnClickListener(v -> databaseManager.deleteAttachment(
                attachment,
                () -> {
                    data.remove(attachment);
                    notifyItemRemoved(position);
                }));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
