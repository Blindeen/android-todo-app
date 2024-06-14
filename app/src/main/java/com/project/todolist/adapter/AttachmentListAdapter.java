package com.project.todolist.adapter;

import static com.project.todolist.utils.FileUtils.deleteFile;
import static com.project.todolist.utils.Utils.displayToast;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.project.todolist.R;
import com.project.todolist.database.DatabaseManager;
import com.project.todolist.database.entity.Attachment;

import java.io.File;
import java.util.List;

public class AttachmentListAdapter extends RecyclerView.Adapter<AttachmentListAdapter.ViewHolder> {
    private final Context context;
    private final List<Attachment> data;
    private final DatabaseManager databaseManager;

    public AttachmentListAdapter(Context context, List<Attachment> data) {
        this.context = context;
        this.data = data;
        this.databaseManager = new DatabaseManager(context);
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
        attachmentName.setOnClickListener(v -> openAttachment(position));
        deleteAttachmentButton.setOnClickListener(v -> databaseManager.deleteAttachment(
                attachment,
                () -> {
                    deleteFile(attachment.getPath());
                    data.remove(attachment);
                    notifyItemRemoved(position);
                }));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void addAttachment(Attachment attachment) {
        data.add(attachment);
        notifyItemInserted(data.size() - 1);
    }

    public List<Attachment> getData() {
        return data;
    }

    private void openAttachment(int position) {
        Attachment attachment = data.get(position);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        File file = new File(attachment.getPath());
        Uri fileUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
        intent.setDataAndType(fileUri, context.getContentResolver().getType(fileUri));
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            displayToast(context, "No application found to open this file.");
        }
    }
}
