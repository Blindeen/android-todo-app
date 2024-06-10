package com.project.todolist.interfaces;

@FunctionalInterface
public interface AddAttachmentResponseHandler {
    void onAttachmentAdded(Long attachmentId);
}
