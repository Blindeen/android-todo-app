package com.project.todolist.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;

import com.project.todolist.database.entity.Attachment;

import io.reactivex.rxjava3.core.Completable;

@Dao
public interface AttachmentDao {
    @Insert
    Completable insertAttachment(Attachment attachment);

    @Delete
    Completable deleteAttachment(Attachment attachment);
}
