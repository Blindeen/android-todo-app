package com.project.todolist.interfaces;

import com.project.todolist.database.entity.Attachment;

import java.util.List;

@FunctionalInterface
public interface AttachmentResponseHandler {
    void onAttachmentFetched(List<Attachment> attachments);
}
