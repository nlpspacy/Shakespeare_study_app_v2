package com.shakespeare.new_app;

import java.util.ArrayList;
import java.util.List;

public interface BookmarkCallback {
    void onBookmarksFetched(ArrayList<List<String>> bookmarkEntriesList);
    void onError(Throwable e);
}
