package com.shakespeare.new_app;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public abstract class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "play_navigation.db";
//    private static final String TABLE_PLAY = "play_navigation";
    private static final String TABLE_PLAY = "play_nav_detailed";
    private static final String TABLE_PLAY_POSITION = "play_position";
    private static final String KEY_ID = "line_number";
    private static final String KEY_NAME = "line_text";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //3rd argument to be passed is CursorFactory instance

        Log.d("progress update", "DatabaseHandler constructor");

    }

    // Getting row Count
    public int getRowCount() {

        SQLiteDatabase db;
//        String tableNameQuery = "SELECT * FROM sqlite_master WHERE type='table' LIMIT 1;";
        String selectAllQuery = "SELECT * FROM " + TABLE_PLAY + " WHERE play_code='" + com.shakespeare.new_app.GlobalClass.selectedPlayCode + "';";
//        Log.d("database handler action","about to query for row count using: " + selectAllQuery);
        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectAllQuery, null);

        return cursor.getCount();

    }

    // Get act number
    public int getActNumber() {
        SQLiteDatabase db;
        String selectQuery = "SELECT * FROM " + TABLE_PLAY + " WHERE play_code='" + com.shakespeare.new_app.GlobalClass.selectedPlayCode + "';";
        if(com.shakespeare.new_app.GlobalClass.selectedActNumber!=0 && com.shakespeare.new_app.GlobalClass.selectedSceneNumber!=0){
            selectQuery = "SELECT act_nr FROM " + TABLE_PLAY + " WHERE play_code='" + com.shakespeare.new_app.GlobalClass.selectedPlayCode + "' AND act_nr=" + com.shakespeare.new_app.GlobalClass.selectedActNumber + " AND scene_nr=" + com.shakespeare.new_app.GlobalClass.selectedSceneNumber + ";";
        }
//        Log.d("database handler action","about to query for act number using: " + selectQuery);
        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();
        return cursor.getInt(0);

        //        cursor.close();
    }

    // Get scene number
    public int getSceneNumber() {
        SQLiteDatabase db;
        String selectQuery = "SELECT * FROM " + TABLE_PLAY + " WHERE play_code='" + com.shakespeare.new_app.GlobalClass.selectedPlayCode + "';";
        if(com.shakespeare.new_app.GlobalClass.selectedActNumber!=0 && com.shakespeare.new_app.GlobalClass.selectedSceneNumber!=0){
            selectQuery = "SELECT scene_nr FROM " + TABLE_PLAY + " WHERE play_code='" + com.shakespeare.new_app.GlobalClass.selectedPlayCode + "' AND act_nr=" + com.shakespeare.new_app.GlobalClass.selectedActNumber + " AND scene_nr=" + com.shakespeare.new_app.GlobalClass.selectedSceneNumber + ";";
        }
//        Log.d("database handler action","about to query for scene number using: " + selectQuery);
        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();
        return cursor.getInt(0);

        //        cursor.close();
    }

    // Get script
    public ArrayList getScript() {

        ArrayList<String> scriptLinesList = new ArrayList<>();

        SQLiteDatabase db;
        // this uses the play_navigation table
//        String selectQuery = "SELECT script_text FROM " + TABLE_PLAY + " WHERE play_code='" + com.shakespeare.new_app.GlobalClass.selectedPlayCode + "' AND act_nr=" + com.shakespeare.new_app.GlobalClass.selectedActNumber + " AND scene_nr=" + com.shakespeare.new_app.GlobalClass.selectedSceneNumber + ";";

        // this uses the play_nav_detailed table
        String selectQuery = "SELECT scene_line_number, script_text, character_short_name FROM " + TABLE_PLAY + " WHERE play_code='" + com.shakespeare.new_app.GlobalClass.selectedPlayCode + "' AND act_nr=" + com.shakespeare.new_app.GlobalClass.selectedActNumber + " AND scene_nr=" + com.shakespeare.new_app.GlobalClass.selectedSceneNumber + ";";
        Log.d("sql",selectQuery);

        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // up to here 24Jan2025 - need to update to show all the rows which satisfy not just the first one
        cursor.moveToFirst();
        String strCharacter = cursor.getString(2)+"+";
        String strPreviousCharacter = "";
        Integer intPreviousLineNumber = -9;
        String strScriptText = cursor.getString(1);
        Integer intLineNumber = cursor.getInt(0);
        String strShowLineOnScreen;

        if(intLineNumber!=0){
            strShowLineOnScreen = String.valueOf(intLineNumber) + ' ' + strScriptText + " no. of lines: " + String.valueOf(cursor.getCount());

        } else {
            strShowLineOnScreen = strScriptText + " no. of lines: " + String.valueOf(cursor.getCount());

        }
//        return cursor.getString(0);
        // we need to return a list to our recycler view - see this page for guidance:
        // https://stackoverflow.com/questions/55159923/how-to-display-data-from-sqlite-database-into-recyclerview

        // Need an individual scriptLinesList entry for the character name so that the character name can be in bold.
        // Also need to label each line with the relevant character so that text by particular characters can be in the specified
        // colour shading if the user wants to colour particular characters to highlight those characters' text.

        if (cursor.moveToFirst()) {

            do {

                strCharacter = cursor.getString(2)+"+";
                strScriptText = cursor.getString(1);
                intLineNumber = cursor.getInt(0);

//                Log.d("character update", "current: "+strCharacter + ", previous: " + strPreviousCharacter);

                if(com.shakespeare.new_app.GlobalClass.selectedActNumber==0 && com.shakespeare.new_app.GlobalClass.selectedSceneNumber==0){
                    scriptLinesList.add(strScriptText );

                }else {

                // Adding user record to list
                if(!strCharacter.equalsIgnoreCase("N.A.") && !strCharacter.equalsIgnoreCase(strPreviousCharacter)){

                    if(intLineNumber == intPreviousLineNumber){
//                        scriptLinesList.add(strCharacter + "\n" + strScriptText );
                        scriptLinesList.add(strCharacter);
                        scriptLinesList.add(strScriptText);

                    } else{
//                        scriptLinesList.add(strCharacter + "\n" + toString().valueOf(intLineNumber) + ' ' + strScriptText );
                        scriptLinesList.add(strCharacter);
                        if(intLineNumber==0){
                            scriptLinesList.add(strScriptText );
                        } else {
                            if(com.shakespeare.new_app.GlobalClass.intShowLineNumbers==1){
                                scriptLinesList.add(toString().valueOf(intLineNumber) + ' ' + strScriptText);
                            } else {
                                scriptLinesList.add(strScriptText);
                            }
                        }

                    }
//                    Log.d("character update", "current != N.A.");
                }else {

                    if(intLineNumber == intPreviousLineNumber){
                        scriptLinesList.add(strScriptText );

                    } else{
                        if(intLineNumber==0){
                            scriptLinesList.add(strScriptText );
                        } else {
                            if(com.shakespeare.new_app.GlobalClass.intShowLineNumbers==1){
                                scriptLinesList.add(toString().valueOf(intLineNumber) + ' ' + strScriptText);
                            } else {
                                scriptLinesList.add(strScriptText);
                            }
                        }

                    }

//                    Log.d("character update", "current is N.A.");
                }
                }

                strPreviousCharacter = strCharacter;
                intPreviousLineNumber = intLineNumber;
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

//        return strShowLineOnScreen;
        Log.d("list info", "list size: " + scriptLinesList.size());
        return scriptLinesList;

        //        cursor.close();
    }

    // Get number of acts in the selected play
    public int getNumberOfActsInPlay() {
        SQLiteDatabase db;
        String selectQuery = "SELECT number_of_acts_in_play FROM " + TABLE_PLAY + " WHERE play_code='" + com.shakespeare.new_app.GlobalClass.selectedPlayCode + "';";
        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();
        return cursor.getInt(0);

    }
    // Get number of scenes in the selected act
    public int getNumberOfScenesInAct() {
        SQLiteDatabase db;
        String selectQuery = "SELECT * FROM " + TABLE_PLAY + " WHERE play_code='" + com.shakespeare.new_app.GlobalClass.selectedPlayCode + "';";
        if(com.shakespeare.new_app.GlobalClass.selectedActNumber!=0){
            selectQuery = "SELECT number_of_scenes_in_act FROM " + TABLE_PLAY + " WHERE play_code='" + com.shakespeare.new_app.GlobalClass.selectedPlayCode + "' AND act_nr=" + com.shakespeare.new_app.GlobalClass.selectedActNumber + ";";
        }
        db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();
        return cursor.getInt(0);

    }

    // Get current act number in case the user is returning to the play, so navigation goes to where they left off last time.
    public int getCurrentActNumber() {

        SQLiteDatabase db;
        String selectQuery = "SELECT current_act_nr FROM " + TABLE_PLAY_POSITION + " WHERE play_code='" + com.shakespeare.new_app.GlobalClass.selectedPlayCode + "';";
        Log.d("check", selectQuery);

        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();
        Log.d("check", "current act number: " + String.valueOf(cursor.getInt(0)));
        return cursor.getInt(0);

    }

    // Get current scene number in case the user is returning to the play, so navigation goes to where they left off last time.
    public int getCurrentSceneNumber() {

        SQLiteDatabase db;
        String selectQuery = "SELECT current_scene_nr FROM " + TABLE_PLAY_POSITION + " WHERE play_code='" + com.shakespeare.new_app.GlobalClass.selectedPlayCode + "';";

        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();
        Log.d("check", "current scene number: " + String.valueOf(cursor.getInt(0)));
        return cursor.getInt(0);

    }


    // Get current act number in case the user is returning to the play, so navigation goes to where they left off last time.
    public int updateNavDbWithCurrentActSceneInPlay() {

        SQLiteDatabase db;
        String updateQuery = "UPDATE " + TABLE_PLAY_POSITION + " SET current_act_nr = " + GlobalClass.selectedActNumber + ", current_scene_nr = " + GlobalClass.selectedSceneNumber + " WHERE play_code='" + com.shakespeare.new_app.GlobalClass.selectedPlayCode + "';";

        Log.d("update query",updateQuery);

        db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(updateQuery, null);

        cursor.moveToFirst();
        return 1;

    }


    // Get current act number in case the user is returning to the play, so navigation goes to where they left off last time.
    public int addBookmark(Integer intRvPosition, String strScriptText) {

        SQLiteDatabase db;
        String insertQuery = "INSERT INTO bookmark (username, date_time_added, play_code, play_full_name, act_nr, scene_nr, play_line_nr, scene_line_nr, position_in_view, script_text, annotation, active_0_or_1) ";
        insertQuery += "VALUES ('blank', '1','" + com.shakespeare.new_app.GlobalClass.selectedPlayCode + "','" + com.shakespeare.new_app.GlobalClass.selectedPlay + "', " + GlobalClass.selectedActNumber + ", " + GlobalClass.selectedSceneNumber + ", -1, -1, " + intRvPosition + ", '" + strScriptText + "', 'blank note', 1);";

        Log.d("insert query",insertQuery);

        db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(insertQuery, null);

        cursor.moveToFirst();
        return 1;

    }
}
