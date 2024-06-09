package com.project.todolist.spinner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.todolist.R;
import com.project.todolist.database.AppDatabase;
import com.project.todolist.database.entity.Attachment;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AttachmentListAdapter extends RecyclerView.Adapter<AttachmentListAdapter.ViewHolder> {
    private final List<Attachment> data;

    private final AppDatabase database;

    public AttachmentListAdapter(List<Attachment> data, AppDatabase database) {
        this.data = data;
        this.database = database;
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
        deleteAttachmentButton.setOnClickListener(v -> deleteAttachment(attachment));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private void deleteAttachment(Attachment attachment) {
        Completable completable = database.attachmentDao().deleteAttachment(attachment);
        Disposable disposable = completable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    int position = data.indexOf(attachment);
                    data.remove(attachment);
                    notifyItemRemoved(position);
                });
    }
}
