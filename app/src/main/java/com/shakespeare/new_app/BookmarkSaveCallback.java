package com.shakespeare.new_app;

public interface BookmarkSaveCallback {
    void onBookmarkSaved();
    void onBookmarkSaveFailed(Throwable e);
}