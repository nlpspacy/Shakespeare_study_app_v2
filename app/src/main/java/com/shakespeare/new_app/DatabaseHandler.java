package com.shakespeare.new_app;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
//        if(com.shakespeare.new_app.GlobalClass.selectedActNumber!=0 && com.shakespeare.new_app.GlobalClass.selectedSceneNumber!=0){
        if(com.shakespeare.new_app.GlobalClass.selectedActNumber!=0){
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
    public ArrayList  getScript(Boolean boolAtPrologue, Boolean boolAtEpilogue) {

        ArrayList<String> scriptLinesList = new ArrayList<>();

        SQLiteDatabase db;
        // this uses the play_navigation table
//        String selectQuery = "SELECT script_text FROM " + TABLE_PLAY + " WHERE play_code='" + com.shakespeare.new_app.GlobalClass.selectedPlayCode + "' AND act_nr=" + com.shakespeare.new_app.GlobalClass.selectedActNumber + " AND scene_nr=" + com.shakespeare.new_app.GlobalClass.selectedSceneNumber + ";";

        // this uses the play_nav_detailed table
        //
        // ** improvement needed 18 Feb 2025 - when we get script, we want to left outer join to a SELECT DISTINCT
        // by act, scene and line on the bookmarks table so that where there is a bookmark we can show an asterisk
        // or other mark at the end of the line so the user knows there is a bookmark on that line.
        // This means that when we save a bookmark we need to save the universal line number, i.e. play_line_number,
        // in the bookmark as well.

        db = this.getReadableDatabase();
        String selectQuery;
        Cursor cursor;

//        selectQuery = "SELECT scene_line_number, script_text, character_short_name FROM " + TABLE_PLAY + " WHERE play_code='" + com.shakespeare.new_app.GlobalClass.selectedPlayCode + "' AND act_nr=" + com.shakespeare.new_app.GlobalClass.selectedActNumber + " AND scene_nr=" + com.shakespeare.new_app.GlobalClass.selectedSceneNumber + " ORDER BY play_line_number;";
        if (boolAtPrologue) {
            selectQuery = "SELECT p.scene_line_number, p.script_text, p.character_short_name, p.play_code, p.play_line_number, b.bookmark_count, p.line_text, p.indent_text FROM " + TABLE_PLAY + " p LEFT OUTER JOIN (SELECT play_code, play_line_nr, count(distinct bookmark_row_id) as bookmark_count from bookmark where active_0_or_1 = 1 group by play_code, play_line_nr) b on p.play_code = b.play_code and p.play_line_number = b.play_line_nr WHERE p.play_code='" + com.shakespeare.new_app.GlobalClass.selectedPlayCode + "' AND act_nr_roman='Prologue' ORDER BY p.play_line_number;";

        } else if (boolAtEpilogue) {
            selectQuery = "SELECT p.scene_line_number, p.script_text, p.character_short_name, p.play_code, p.play_line_number, b.bookmark_count, p.line_text, p.indent_text FROM " + TABLE_PLAY + " p LEFT OUTER JOIN (SELECT play_code, play_line_nr, count(distinct bookmark_row_id) as bookmark_count from bookmark where active_0_or_1 = 1 group by play_code, play_line_nr) b on p.play_code = b.play_code and p.play_line_number = b.play_line_nr WHERE p.play_code='" + com.shakespeare.new_app.GlobalClass.selectedPlayCode + "' AND act_nr_roman='Epilogue' ORDER BY p.play_line_number;";

        } else {
            selectQuery = "SELECT p.scene_line_number, p.script_text, p.character_short_name, p.play_code, p.play_line_number, b.bookmark_count, p.line_text, p.indent_text FROM " + TABLE_PLAY + " p LEFT OUTER JOIN (SELECT play_code, play_line_nr, count(distinct bookmark_row_id) as bookmark_count from bookmark where active_0_or_1 = 1 group by play_code, play_line_nr) b on p.play_code = b.play_code and p.play_line_number = b.play_line_nr WHERE p.play_code='" + com.shakespeare.new_app.GlobalClass.selectedPlayCode + "' AND act_nr=" + com.shakespeare.new_app.GlobalClass.selectedActNumber + " AND scene_nr=" + com.shakespeare.new_app.GlobalClass.selectedSceneNumber + " ORDER BY p.play_line_number;";

        }

        Log.d("sql",selectQuery);

        cursor = db.rawQuery(selectQuery, null);

        // up to here 24Jan2025 - need to update to show all the rows which satisfy not just the first one
        cursor.moveToFirst();
        String strCharacter = cursor.getString(2)+"+";
        String strPreviousCharacter = "";
        Integer intPreviousLineNumber = -9;
        String strScriptText = "";
        Integer intLineNumber = cursor.getInt(0);
        Integer intPlayLineNumber = cursor.getInt(4);
        Integer intBookmarkCount = cursor.getInt(5);
        String strShowLineOnScreen;
        Integer intIndentFlag = 0;

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
                intLineNumber = cursor.getInt(0);
                // add line reference which will be included as a hidden row for reference purposes
                intPlayLineNumber = cursor.getInt(4);
                intBookmarkCount = cursor.getInt(5);
                intIndentFlag = cursor.getInt(7);

                // If we are in the Characters in the play section, then
                // present the character name and, if any, extension to their name.
                // For the hading of this section, leave out the character name field which holds the value "N.A."
                if (cursor.getString(6).equals("Characters in the play")){
                    if (cursor.getString(2).equals("N.A.") && cursor.getString(1).equals("Characters in the Play")){
                        strScriptText = cursor.getString(1);

                    } else{
                        strScriptText = cursor.getString(2) + cursor.getString(1);
                        if (intIndentFlag==1){
                            strScriptText = "   " + strScriptText;
                        }

                    }

                }else {
                    strScriptText = cursor.getString(1);

                }

                if(intBookmarkCount>0){
                    strScriptText += " <" + String.valueOf(intBookmarkCount) + ">";
                    Log.d("indicate bookmark exists", "bookmark(s): " + String.valueOf(intBookmarkCount));
                }

                scriptLinesList.add("play_code: " + com.shakespeare.new_app.GlobalClass.selectedPlayCode + " Act " + com.shakespeare.new_app.GlobalClass.selectedActNumber + " Scene " + com.shakespeare.new_app.GlobalClass.selectedSceneNumber + " scene_line_nr " + intLineNumber + " play_line_nr " + String.valueOf(intPlayLineNumber));

                //Log.d("character update", "line nr "+intLineNumber +" previous line nr "+ intPreviousLineNumber + ", current: "+strCharacter + ", previous: " + strPreviousCharacter);

                if(com.shakespeare.new_app.GlobalClass.selectedActNumber==0 && com.shakespeare.new_app.GlobalClass.selectedSceneNumber==0){
                    scriptLinesList.add(strScriptText);

                }else {

                // Adding user record to list
                if(!strCharacter.equalsIgnoreCase("N.A.+") && !strCharacter.equalsIgnoreCase(strPreviousCharacter)){

                    //Log.d("flag", "check lines numbers to decide whether to add not NA new character " +strCharacter+" "+strScriptText);
                    if(intLineNumber == intPreviousLineNumber){
//                        scriptLinesList.add(strCharacter + "\n" + strScriptText );
                        scriptLinesList.add(strCharacter);
                        scriptLinesList.add(strScriptText);
                        //Log.d("flag", "not NA new character added");

                    } else{
//                        scriptLinesList.add(strCharacter + "\n" + toString().valueOf(intLineNumber) + ' ' + strScriptText );
                            scriptLinesList.add(strCharacter);
                        //Log.d("flag", "NA *or* non-new character added " +strCharacter);
                        if(intLineNumber==0){
                            scriptLinesList.add(strScriptText );
                        } else {
                            if(com.shakespeare.new_app.GlobalClass.intShowLineNumbers==1){
                                scriptLinesList.add(toString().valueOf(intLineNumber) + ' ' + strScriptText);
                                //Log.d("flag", "option 1: add script text without line number " +strCharacter + " line nr " + strScriptText);
                            } else {
                                scriptLinesList.add(strScriptText);
                            }
                        }

                    }
//                    Log.d("character update", "current != N.A.");
                }else {

                    //Log.d("flag", "line numbers: " + intLineNumber +", "+ intPreviousLineNumber+", difference: "+String.valueOf(intLineNumber-intPreviousLineNumber));
                    if((intLineNumber-intPreviousLineNumber)==0){
                        //Log.d("flag", "option 2: add script text without line number " +strCharacter + " line nr " + strScriptText);
                        scriptLinesList.add(strScriptText);

                    } else{
                        if(intLineNumber==0){
                            scriptLinesList.add(strScriptText );
                        } else {
                            if(com.shakespeare.new_app.GlobalClass.intShowLineNumbers==1){
                                scriptLinesList.add(toString().valueOf(intLineNumber) + ' ' + strScriptText);
                                //Log.d("flag", "option 3: add script text with line number " +strCharacter + " line nr " + strScriptText);
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

        Log.d("check","GlobalClass.selectedPlayCode: " + com.shakespeare.new_app.GlobalClass.selectedPlayCode);
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


    // Get minimum scene number of current act to find whether it is scene 0, usually with a chorus, or scene 1.
    public int getMinimumSceneNumber() {

        SQLiteDatabase db;
        String selectQuery = "SELECT MIN(scene_nr) FROM " + TABLE_PLAY + " WHERE play_code='" + com.shakespeare.new_app.GlobalClass.selectedPlayCode + "' AND act_nr = " + GlobalClass.selectedActNumber + ";";

        Log.d("check", selectQuery);

        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();
        Log.d("check", "minimum scene number: " + String.valueOf(cursor.getInt(0)));
        return cursor.getInt(0);

    }

    // Get minimum scene number of current act to find whether it is scene 0, usually with a chorus, or scene 1.
    public boolean checkForEpilogue() {

        SQLiteDatabase db;
        String selectQuery = "SELECT count(*) FROM " + TABLE_PLAY + " ";
        selectQuery += "WHERE play_code='" + com.shakespeare.new_app.GlobalClass.selectedPlayCode + "' ";
        selectQuery += "AND act_nr_roman = 'Epilogue';";

        Log.d("check", selectQuery);

        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();
        Integer intEpilogueCheck = Integer.valueOf(cursor.getInt(0));
        Log.d("check", "check whether there is an epilogue: " + String.valueOf(cursor.getInt(0)));
        if (intEpilogueCheck > 0) {
            return true;
        } else {
            return false;
        }

    }

    public boolean checkForPrologue() {

        SQLiteDatabase db;
        String selectQuery = "SELECT count(*) FROM " + TABLE_PLAY + " ";
        selectQuery += "WHERE play_code='" + com.shakespeare.new_app.GlobalClass.selectedPlayCode + "' ";
        selectQuery += "AND act_nr_roman = 'Prologue';";

        Log.d("check", selectQuery);

        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();
        Integer intPrologueCheck = Integer.valueOf(cursor.getInt(0));
        Log.d("check", "check whether there is an prologue: " + String.valueOf(cursor.getInt(0)));
        if (intPrologueCheck > 0) {
            return true;
        } else {
            return false;
        }

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
    public int addBookmark(Integer intRvPosition, String strScriptText, String strUserNote, Integer intSceneLineNr, Integer intPlayLineNr) {

        SQLiteDatabase db;
        String insertQuery = "INSERT INTO bookmark (username, date_time_added, play_code, play_full_name, act_nr, scene_nr, scene_line_nr, play_line_nr, position_in_view, script_text, annotation, active_0_or_1) ";
        insertQuery += "VALUES ('blank', strftime('%Y-%m-%d %H:%M:%S', datetime('now')), '" + com.shakespeare.new_app.GlobalClass.selectedPlayCode + "','" + com.shakespeare.new_app.GlobalClass.selectedPlay + "', " + GlobalClass.selectedActNumber + ", " + GlobalClass.selectedSceneNumber + ", " + intSceneLineNr + ", " + intPlayLineNr + ", " + intRvPosition + ", '" + strScriptText + "', '" + strUserNote + "', 1);";
        Log.d("insert query",insertQuery);

        db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(insertQuery, null);

        cursor.moveToFirst();
        return 1;

    }

    // Get script
    public ArrayList getBookmarks() {

        //ArrayList<String> bookmarksList = new ArrayList<>();
        // we need a 2-D array because each bookmark is itself a list, so a list of lists
        ArrayList<List<String>> bookmarkEntriesList = new ArrayList<List<String>>();
        // this is the 1-D array which is the list of items within each bookmark in the "outer" list
//        ArrayList<String> bookmarkEntries = new ArrayList<>();

        SQLiteDatabase db;

        // this uses the bookmark table
        String selectQuery = "SELECT DISTINCT bookmark_row_id, username, play_code, play_full_name, act_nr, scene_nr, ";
        selectQuery += "play_line_nr, scene_line_nr, position_in_view, script_text, annotation, active_0_or_1 ";
        selectQuery += "FROM bookmark WHERE active_0_or_1 = 1 ";
        selectQuery += "ORDER BY play_code, date_time_added DESC;";
        Log.d("sql",selectQuery);

        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();

        if (cursor.moveToFirst()) {

            do {

                ArrayList<String> bookmarkEntries = new ArrayList<>();

                bookmarkEntries.add(cursor.getString(0)); // bookmark_row_id
                bookmarkEntries.add(cursor.getString(2)); // play_code
                bookmarkEntries.add(cursor.getString(3)); // play_full_name
                bookmarkEntries.add(cursor.getString(4)); // act_nr
                bookmarkEntries.add(cursor.getString(5)); // scene_nr
                bookmarkEntries.add(cursor.getString(9)); // script_text
                bookmarkEntries.add(cursor.getString(10)); // annotation

                bookmarkEntriesList.add(bookmarkEntries);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

//        return bookmarksList
        return bookmarkEntriesList;

    }


    // Get current scene number in case the user is returning to the play, so navigation goes to where they left off last time.
    public void removeBookmarkLongClicked(Integer intBookmarkReference) {

        SQLiteDatabase db;
        String updateQuery = "UPDATE bookmark SET active_0_or_1 = 0 WHERE bookmark_row_id = "+String.valueOf(intBookmarkReference)+";";

        db = this.getWritableDatabase();
        db.execSQL(updateQuery);

        Log.d("response", "completed");

    }


}
