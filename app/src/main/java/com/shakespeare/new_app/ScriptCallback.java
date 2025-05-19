package com.shakespeare.new_app;

import java.util.ArrayList;

public interface ScriptCallback {
    void onScriptFetched(ArrayList<String> scriptLinesList);
    void onError(Throwable e);
}
