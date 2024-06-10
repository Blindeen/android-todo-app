package com.project.todolist.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.project.todolist.database.entity.Attachment;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface AttachmentDao {
    @Query("SELECT * FROM attachment WHERE taskId = :taskId")
    Single<List<Attachment>> getAttachmentsByTaskId(long taskId);

    @Insert
    Single<Long> insertAttachment(Attachment attachment);

    @Insert
    Completable insertAttachments(List<Attachment> attachments);

    @Delete
    Completable deleteAttachment(Attachment attachment);

    @Delete
    Completable deleteAttachments(List<Attachment> attachments);
}
