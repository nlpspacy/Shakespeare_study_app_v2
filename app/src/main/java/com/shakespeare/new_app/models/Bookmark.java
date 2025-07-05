package com.shakespeare.new_app.models;

public class Bookmark {

    private int bookmark_row_id
            ;
    private String username;
    private String annotation;
    private String playCode;
    private int act_nr;
    private int scene_nr;
    private int play_line_nr;
    private int scene_line_nr;

    // Getters and Setters
    public int getId() {
        return bookmark_row_id;
    }

    public void setId(int id) {
        this.bookmark_row_id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public String getPlayCode() {
        return playCode;
    }

    public void setPlayCode(String playCode) {
        this.playCode = playCode;
    }

    public int getAct() {
        return act_nr;
    }

    public void setAct(int act) {
        this.act_nr = act;
    }

    public int getScene() {
        return scene_nr;
    }

    public void setScene(int scene) {
        this.scene_nr = scene;
    }

    public int getSceneLineNumber() {
        return scene_line_nr;
    }

    public void setSceneLineNumber(int sceneLineNumber) {
        this.scene_line_nr = sceneLineNumber;
    }

    public int getPlayLineNumber() {
        return play_line_nr;
    }

    public void setPlayLineNumber(int playLineNumber) {
        this.play_line_nr = playLineNumber;
    }
}
