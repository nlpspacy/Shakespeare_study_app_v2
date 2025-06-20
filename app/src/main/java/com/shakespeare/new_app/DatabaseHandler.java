package com.shakespeare.new_app;

import androidx.annotation.NonNull;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.database.QueryResult;
import com.example.database.QueryResultCallback;
import com.example.database.RemoteDatabaseHelperHttp;
//import com.shakespeare.new_app.RemoteDatabaseHelperHttp;
import com.example.database.InsertCallback;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class DatabaseHandler extends SQLiteOpenHelper {
    private RemoteDatabaseHelperHttp remoteHelper;
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "play_navigation.db";
//    private static final String TABLE_PLAY = "play_navigation";
    private static final String TABLE_PLAY = "play_nav_detailed";
    private static final String TABLE_PLAY_POSITION = "play_position";
    private static final String KEY_ID = "line_number";
    private static final String KEY_NAME = "line_text";
    private Context context;

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //3rd argument to be passed is CursorFactory instance

        this.context = context; // ✅ Save the context for later use
        this.remoteHelper = new RemoteDatabaseHelperHttp(context);

        Log.d("progress update", "DatabaseHandler constructor");

    }

    // Getting row Count
    public int getRowCount() {

        SQLiteDatabase db;
//        String tableNameQuery = "SELECT * FROM sqlite_master WHERE type='table' LIMIT 1;";
        String selectAllQuery = "SELECT * FROM " + TABLE_PLAY + " WHERE play_code='" + GlobalClass.selectedPlayCode + "';";
//        Log.d("database handler action","about to query for row count using: " + selectAllQuery);
        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectAllQuery, null);

        return cursor.getCount();

    }

    // Get act number
    public int getActNumber() {
        SQLiteDatabase db;
        String selectQuery = "SELECT * FROM " + TABLE_PLAY + " WHERE play_code='" + GlobalClass.selectedPlayCode + "';";
        if(GlobalClass.selectedActNumber!=0 && GlobalClass.selectedSceneNumber!=0){
            selectQuery = "SELECT act_nr FROM " + TABLE_PLAY + " WHERE play_code='" + GlobalClass.selectedPlayCode + "' AND act_nr=" + GlobalClass.selectedActNumber + " AND scene_nr=" + GlobalClass.selectedSceneNumber + ";";
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
        String selectQuery = "SELECT * FROM " + TABLE_PLAY + " WHERE play_code='" + GlobalClass.selectedPlayCode + "';";
//        if(com.shakespeare.new_app.GlobalClass.selectedActNumber!=0 && com.shakespeare.new_app.GlobalClass.selectedSceneNumber!=0){
        if(GlobalClass.selectedActNumber!=0){
            selectQuery = "SELECT scene_nr FROM " + TABLE_PLAY + " WHERE play_code='" + GlobalClass.selectedPlayCode + "' AND act_nr=" + GlobalClass.selectedActNumber + " AND scene_nr=" + GlobalClass.selectedSceneNumber + ";";
        }
//        Log.d("database handler action","about to query for scene number using: " + selectQuery);
        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();
        return cursor.getInt(0);

        //        cursor.close();
    }

    // Get script
    public ArrayList getScript(Boolean boolAtPrologue, Boolean boolAtEpilogue) {

        ArrayList<String> scriptLinesList = new ArrayList<>();
        // this will contain the character name and the script text for the particular script line
        ArrayList<String> strScriptIndividualRow = new ArrayList<>();

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

        String currentUser = UserManager.getUsername(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> visibleUsers = prefs.getStringSet("visible_bookmark_users", new HashSet<>());

        StringBuilder inClause = new StringBuilder();
        for (String user : visibleUsers) {
            if (!user.equals(currentUser)) {
                inClause.append("'").append(user).append("',");
            }
        }
        if (inClause.length() > 0) {
            inClause.setLength(inClause.length() - 1); // remove trailing comma
        } else {
            inClause.append("''"); // prevent SQL error if empty
        }


//        selectQuery = "SELECT scene_line_number, script_text, character_short_name FROM " + TABLE_PLAY + " WHERE play_code='" + com.shakespeare.new_app.GlobalClass.selectedPlayCode + "' AND act_nr=" + com.shakespeare.new_app.GlobalClass.selectedActNumber + " AND scene_nr=" + com.shakespeare.new_app.GlobalClass.selectedSceneNumber + " ORDER BY play_line_number;";
        if (boolAtPrologue) {
            selectQuery = "SELECT p.scene_line_number, p.script_text, p.character_short_name, p.play_code, p.play_line_number, b.bookmark_count, p.line_text, p.indent_text, shared_b.shared_bookmark_count FROM " + TABLE_PLAY + " p LEFT OUTER JOIN (SELECT play_code, play_line_nr, count(distinct bookmark_row_id) as bookmark_count from bookmark where username = '" + currentUser + "' AND active_0_or_1 = 1 group by play_code, play_line_nr) b on p.play_code = b.play_code and p.play_line_number = b.play_line_nr LEFT OUTER JOIN (SELECT play_code, play_line_nr, count(distinct bookmark_row_id) as shared_bookmark_count from bookmark where username IN (" + inClause + ") AND active_0_or_1 = 1 group by play_code, play_line_nr) shared_b on p.play_code = shared_b.play_code and p.play_line_number = shared_b.play_line_nr WHERE p.play_code='" + GlobalClass.selectedPlayCode + "' AND act_nr_roman='Prologue' ORDER BY p.play_line_number;";

        } else if (boolAtEpilogue) {
            selectQuery = "SELECT p.scene_line_number, p.script_text, p.character_short_name, p.play_code, p.play_line_number, b.bookmark_count, p.line_text, p.indent_text, shared_b.shared_bookmark_count FROM " + TABLE_PLAY + " p LEFT OUTER JOIN (SELECT play_code, play_line_nr, count(distinct bookmark_row_id) as bookmark_count from bookmark where username = '" + currentUser + "' AND active_0_or_1 = 1 group by play_code, play_line_nr) b on p.play_code = b.play_code and p.play_line_number = b.play_line_nr LEFT OUTER JOIN (SELECT play_code, play_line_nr, count(distinct bookmark_row_id) as shared_bookmark_count from bookmark where username IN (" + inClause + ") AND active_0_or_1 = 1 group by play_code, play_line_nr) shared_b on p.play_code = shared_b.play_code and p.play_line_number = shared_b.play_line_nr WHERE p.play_code='" + GlobalClass.selectedPlayCode + "' AND act_nr_roman='Epilogue' ORDER BY p.play_line_number;";

        } else {
            selectQuery = "SELECT p.scene_line_number, p.script_text, p.character_short_name, p.play_code, p.play_line_number, b.bookmark_count, p.line_text, p.indent_text, shared_b.shared_bookmark_count FROM " + TABLE_PLAY + " p LEFT OUTER JOIN (SELECT play_code, play_line_nr, count(distinct bookmark_row_id) as bookmark_count from bookmark where username = '" + currentUser + "' AND active_0_or_1 = 1 group by play_code, play_line_nr) b on p.play_code = b.play_code and p.play_line_number = b.play_line_nr LEFT OUTER JOIN (SELECT play_code, play_line_nr, count(distinct bookmark_row_id) as shared_bookmark_count from bookmark where username IN (" + inClause + ") AND active_0_or_1 = 1 group by play_code, play_line_nr) shared_b on p.play_code = shared_b.play_code and p.play_line_number = shared_b.play_line_nr WHERE p.play_code='" + GlobalClass.selectedPlayCode + "' AND act_nr=" + GlobalClass.selectedActNumber + " AND scene_nr=" + GlobalClass.selectedSceneNumber + " ORDER BY p.play_line_number;";

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
        Integer intSharedBookmarkCount = cursor.getInt(8);
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

//                strScriptIndividualRow = null;
                strCharacter = cursor.getString(2)+"+";
                intLineNumber = cursor.getInt(0);
                // add line reference which will be included as a hidden row for reference purposes
                intPlayLineNumber = cursor.getInt(4);
                intBookmarkCount = cursor.getInt(5);
                intSharedBookmarkCount = cursor.getInt(8);
                intIndentFlag = cursor.getInt(7);
                strScriptIndividualRow.clear();

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

//                Log.d("bookmark counter","own bookmarks <" + String.valueOf(intBookmarkCount) + ">, shared bookmarks <" + String.valueOf(intSharedBookmarkCount) + ">");

                if(intBookmarkCount>0){
                    strScriptText += "  <font color='#FFFFFF'>&lt;" + String.valueOf(intBookmarkCount) + "&gt;</font>";
//                    Log.d("indicate bookmark exists", "bookmark(s): " + String.valueOf(intBookmarkCount));
                }

                if(intSharedBookmarkCount>0){
                    strScriptText += " <font color='#30D5C8'><i>&lt;" + String.valueOf(intSharedBookmarkCount) + "&gt;</i></font>";
//                    Log.d("indicate bookmark exists", "shared bookmark(s): " + String.valueOf(intSharedBookmarkCount));
                }

                scriptLinesList.add("play_code: " + GlobalClass.selectedPlayCode + " Act " + GlobalClass.selectedActNumber + " Scene " + GlobalClass.selectedSceneNumber + " scene_line_nr " + intLineNumber + " play_line_nr " + String.valueOf(intPlayLineNumber));

                //Log.d("character update", "line nr "+intLineNumber +" previous line nr "+ intPreviousLineNumber + ", current: "+strCharacter + ", previous: " + strPreviousCharacter);

                if(GlobalClass.selectedActNumber==0 && GlobalClass.selectedSceneNumber==0){
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
                            scriptLinesList.add(strScriptText);
                        } else {
                            if(GlobalClass.intShowLineNumbers==1){
                                scriptLinesList.add(toString().valueOf(intLineNumber) + ' ' + strScriptText);
                                //Log.d("flag", "option 1: add script text without line number " +strCharacter + " line nr " + strScriptText);
                            } else {
                                scriptLinesList.add(strScriptText);

                            }
                        }

                    }
//                    Log.d("character update", "current != N.A.");
                }else {

//                    Log.d("flag", "line numbers: " + intLineNumber +", "+ intPreviousLineNumber+", difference: "+String.valueOf(intLineNumber-intPreviousLineNumber));
                    if((intLineNumber-intPreviousLineNumber)==0){
//                        Log.d("flag", "option 2: add script text without line number " +strCharacter + " line nr " + strScriptText);

                        if(GlobalClass.intShowLineNumbers==1){
                            scriptLinesList.add(toString().valueOf(intLineNumber) + ' ' + strScriptText);
                            //Log.d("flag", "option 3: add script text with line number " +strCharacter + " line nr " + strScriptText);
                        } else {
                            scriptLinesList.add(strScriptText);
                        }

                    } else{
                        if(intLineNumber==0){
                            scriptLinesList.add(strScriptText );
                        } else {

                            if(GlobalClass.intShowLineNumbers==1){
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

//        Log.d("list info", "list size: " + scriptLinesList.size());
        return scriptLinesList;

    }

    public void getScriptFromCloud(boolean isPrologue, boolean isEpilogue, ScriptCallback callback) {

//        Log.d("tracking","getScriptFromCloud");

        String currentUser = UserManager.getUsername(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> visibleUsers = prefs.getStringSet("visible_bookmark_users", new HashSet<>());

        StringBuilder inClause = new StringBuilder();
        for (String user : visibleUsers) {
            if (!user.equals(currentUser)) {
                inClause.append("'").append(user).append("',");
            }
        }
        if (inClause.length() > 0) {
            inClause.setLength(inClause.length() - 1); // remove trailing comma
        } else {
            inClause.append("''"); // prevent SQL error if empty
        }

//        Log.d("users whose shared bookmarks to view","users whose shared bookmarks to view: "+String.valueOf(inClause));

        String baseQuery =
                "SELECT p.scene_line_number, p.script_text, p.character_short_name, p.play_code, " +
                        "p.play_line_number, COALESCE(b.bookmark_count, 0) AS bookmark_count, " +
                        "p.line_text, p.indent_text, " +
                        "COALESCE(shared_b.bookmark_count, 0) AS shared_bookmark_count " +
                        "FROM play_nav_detailed p " +
                        "LEFT OUTER JOIN (" +
                        "    SELECT play_code, play_line_nr, COUNT(DISTINCT bookmark_row_id) AS bookmark_count " +
                        "    FROM bookmark " +
                        "    WHERE username = '" + currentUser + "' AND active_0_or_1 = 1 " +
                        "    GROUP BY play_code, play_line_nr" +
                        ") b ON p.play_code = b.play_code AND p.play_line_number = b.play_line_nr " +
                        "LEFT OUTER JOIN (" +
                        "    SELECT play_code, play_line_nr, COUNT(DISTINCT bookmark_row_id) AS bookmark_count " +
                        "    FROM bookmark " +
                        "    WHERE username <> '" + currentUser + "' AND username IN (" + inClause + ") " +
                        "     AND active_0_or_1 = 1 " +
                        "    GROUP BY play_code, play_line_nr" +
                        ") shared_b ON p.play_code = shared_b.play_code AND p.play_line_number = shared_b.play_line_nr " +
                        "WHERE p.play_code = '" + GlobalClass.selectedPlayCode + "' ";

        Log.d("sql check","sql:" + baseQuery);

        if (isPrologue) {
            baseQuery += "AND act_nr_roman = 'Prologue' ";
        } else if (isEpilogue) {
            baseQuery += "AND act_nr_roman = 'Epilogue' ";
        } else {
            baseQuery += "AND act_nr = " + GlobalClass.selectedActNumber +
                    " AND scene_nr = " + GlobalClass.selectedSceneNumber + " ";
        }

        baseQuery += "ORDER BY p.play_line_number;";

        RemoteDatabaseHelperHttp remoteDb = new RemoteDatabaseHelperHttp(context);

//        remoteDb.runQueryFromJava(baseQuery, new QueryResultCallback<List<Map<String, String>>>() {
////        remoteDb.runQueryFromJava(baseQuery, new kotlin.jvm.functions.Function1<QueryResult<List<Map<String, String>>>, kotlin.Unit>() {
//            @Override
//            public kotlin.Unit invoke(QueryResult<List<Map<String, String>>> result) {
//                if (result.isSuccess()) {
//                    ArrayList<String> scriptLinesList = new ArrayList<>();
//                    String strPreviousCharacter = "";
//                    int intPreviousLineNumber = -9;
                    remoteDb.runQueryFromJava(baseQuery, new QueryResultCallback<List<Map<String, String>>>() {
                        ArrayList<String> scriptLinesList = new ArrayList<>();
                        String strPreviousCharacter = "";
                        int intPreviousLineNumber = -9;

                                @Override
                                public void onResult(QueryResult<List<Map<String, String>>> result) {
                                    List<Map<String, String>> rows = result.getData();
                                    if (rows != null) {
                                        for (Map<String, String> row : rows) {

//                                        }
//                    for (Map<String, String> row : result.getData()) {
                        String character = row.get("character_short_name") + "+";
                        int lineNumber = Integer.parseInt(row.get("scene_line_number"));
                        int playLineNumber = Integer.parseInt(row.get("play_line_number"));
                        int bookmarkCount = Integer.parseInt(row.get("bookmark_count"));
                        int sharedBookmarkCount = Integer.parseInt(row.get("shared_bookmark_count"));
                        String lineText = row.get("script_text");
                        String indentFlag = row.get("indent_text");
                        String characterSectionTitle = row.get("line_text");

                        // Handle Characters in the play section
                        if ("Characters in the play".equals(characterSectionTitle)) {
                            if ("N.A.".equals(row.get("character_short_name")) &&
                                    "Characters in the Play".equals(lineText)) {
                                lineText = row.get("script_text");
                            } else {
                                lineText = row.get("character_short_name") + row.get("script_text");
                                if ("1".equals(indentFlag)) {
                                    lineText = "   " + lineText;
                                }
                            }
                        }

//                        Log.d("bookmark counter","own bookmarks <" + String.valueOf(bookmarkCount) + ">, shared bookmarks <" + String.valueOf(sharedBookmarkCount) + ">");

                        if (bookmarkCount > 0) {
                            lineText += " <font color='#FFFFFF'>&lt;" + String.valueOf(bookmarkCount) + "&gt;</font>";
                        }

                        if (sharedBookmarkCount > 0) {
                            lineText += " <font color='#30D5C8'><i>&lt;" + String.valueOf(sharedBookmarkCount) + "&gt;</i></font>";
                        }

                        scriptLinesList.add("play_code: " + GlobalClass.selectedPlayCode +
                                " Act " + GlobalClass.selectedActNumber +
                                " Scene " + GlobalClass.selectedSceneNumber +
                                " scene_line_nr " + lineNumber +
                                " play_line_nr " + playLineNumber);

                        // Output logic
                        if (GlobalClass.selectedActNumber == 0 && GlobalClass.selectedSceneNumber == 0) {
                            scriptLinesList.add(lineText);
                        } else if (!character.equalsIgnoreCase("N.A.+") &&
                                !character.equalsIgnoreCase(strPreviousCharacter)) {
                            scriptLinesList.add(character);

                            if (GlobalClass.intShowLineNumbers == 1) {
                                scriptLinesList.add(lineNumber + " " + lineText);
                            } else {
                                scriptLinesList.add(lineText);
                            }

                        } else {
                            if (lineNumber == intPreviousLineNumber) {
                                scriptLinesList.add(lineText);
                            } else {
                                if (lineNumber == 0) {
                                    scriptLinesList.add(lineText);
                                } else if (GlobalClass.intShowLineNumbers == 1) {
                                    scriptLinesList.add(lineNumber + " " + lineText);
                                } else {
                                    scriptLinesList.add(lineText);
                                }
                            }
                        }

                        strPreviousCharacter = character;
                        intPreviousLineNumber = lineNumber;
                    }

                    callback.onScriptFetched(scriptLinesList);
                } else {
                    callback.onError(result.getException());
                }

//                return kotlin.Unit.INSTANCE;
            }

//                        @Override
//                        public void onError(Throwable error) {
//                            // ✅ This is the required method!
//                            Log.e("DatabaseHandler", "Query failed", error);
//                        }
        });
    }

    // Get number of acts in the selected play
    public int getNumberOfActsInPlay() {
        SQLiteDatabase db;
        String selectQuery = "SELECT number_of_acts_in_play FROM " + TABLE_PLAY + " WHERE play_code='" + GlobalClass.selectedPlayCode + "';";
        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();
        return cursor.getInt(0);

    }
    // Get number of scenes in the selected act
    public int getNumberOfScenesInAct() {
        SQLiteDatabase db;
        String selectQuery = "SELECT * FROM " + TABLE_PLAY + " WHERE play_code='" + GlobalClass.selectedPlayCode + "';";
        if(GlobalClass.selectedActNumber!=0){
            selectQuery = "SELECT number_of_scenes_in_act FROM " + TABLE_PLAY + " WHERE play_code='" + GlobalClass.selectedPlayCode + "' AND act_nr=" + GlobalClass.selectedActNumber + ";";
        }
        db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();
        return cursor.getInt(0);

    }

    // Get current act number in case the user is returning to the play, so navigation goes to where they left off last time.
    public int getCurrentActNumber() {

        Log.d("check","GlobalClass.selectedPlayCode: " + GlobalClass.selectedPlayCode);
        SQLiteDatabase db;
        String selectQuery = "SELECT current_act_nr FROM " + TABLE_PLAY_POSITION + " WHERE play_code='" + GlobalClass.selectedPlayCode + "';";
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
        String selectQuery = "SELECT current_scene_nr FROM " + TABLE_PLAY_POSITION + " WHERE play_code='" + GlobalClass.selectedPlayCode + "';";

        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();
        Log.d("check", "current scene number: " + String.valueOf(cursor.getInt(0)));
        return cursor.getInt(0);

    }


    // Get minimum scene number of current act to find whether it is scene 0, usually with a chorus, or scene 1.
    public int getMinimumSceneNumber() {

        SQLiteDatabase db;
        String selectQuery = "SELECT MIN(scene_nr) FROM " + TABLE_PLAY + " WHERE play_code='" + GlobalClass.selectedPlayCode + "' AND act_nr = " + GlobalClass.selectedActNumber + ";";

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
        selectQuery += "WHERE play_code='" + GlobalClass.selectedPlayCode + "' ";
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
        selectQuery += "WHERE play_code='" + GlobalClass.selectedPlayCode + "' ";
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
        String updateQuery = "UPDATE " + TABLE_PLAY_POSITION + " SET current_act_nr = " + GlobalClass.selectedActNumber + ", current_scene_nr = " + GlobalClass.selectedSceneNumber + " WHERE play_code='" + GlobalClass.selectedPlayCode + "';";

        Log.d("update query",updateQuery);

        db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(updateQuery, null);

        cursor.moveToFirst();
        return 1;

    }


      // Get current act number in case the user is returning to the play, so navigation goes to where they left off last time.

    public int addBookmark(Integer intRvPosition, String strScriptText, String strUserNote, Integer intSceneLineNr, Integer intPlayLineNr, boolean share) {
        int shareFlag = share ? 1 : 0;
        String currentUsername = UserManager.getUsername(context);
        String sql = "INSERT INTO bookmark (" +
                "username, date_time_added, play_code, play_full_name, act_nr, scene_nr, scene_line_nr, " +
                "play_line_nr, position_in_view, script_text, annotation, active_0_or_1, share_with_others) " +
                "VALUES ('" + currentUsername + "', datetime('now'), '" + GlobalClass.selectedPlayCode + "', '" + GlobalClass.selectedPlay + "', " +
                GlobalClass.selectedActNumber + ", " + GlobalClass.selectedSceneNumber + ", " +
                intSceneLineNr + ", " + intPlayLineNr + ", " + intRvPosition + ", '" +
                strScriptText.replace("'", "''") + "', '" + strUserNote.replace("'", "''") + "', 1 , " + shareFlag + ");";

        RemoteDatabaseHelperHttp remoteDb = new RemoteDatabaseHelperHttp(context);
//        remoteDb.runInsert(sql);  // ✅ simpler, lambda-free

        remoteDb.runInsert(sql, new com.example.database.InsertCallback() {
//        remoteDb.runQueryFromJava(sql, new RemoteDatabaseHelperHttp.InsertCallback() {

            @Override
            public void onInsertFailure(Throwable e) {
                // Handle failure, e.g. log or show error message
                Log.e("DatabaseHandler", "Insert failed", e);
            }
            @Override
            public void onInsertSuccess() {
                Log.d("bookmark", "✅ Bookmark saved to SQLiteCloud.");
//                callback.onBookmarkSaved(); // ✅ notify AMSND

                // Set shared preference flag to trigger refresh in AMSND
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                prefs.edit().putBoolean("refresh_script_on_return", true).apply();

            }

            //@Override
//            public void onInsertFailure(Throwable e) {
//                Log.e("bookmark", "❌ Failed to save bookmark", e);
////                callback.onBookmarkSaveFailed(e);
//                }
        });

//        // Asynchronous cloud insert
//        remoteDb.runQueryFromJava(sql, result -> {
//            if (result.isSuccess()) {
//                Log.d("bookmark", "✅ Bookmark saved to SQLiteCloud.");
//            } else {
//                Log.e("bookmark", "❌ Failed to save bookmark", result.getException());
//            }
//        });

        // Return immediately (assume success for now)
        return 1;
    }

    public void getBookmarksFromCloud(BookmarkCallback callback) {

//        String currentUser = UserManager.getUsername(context);
//
//        String sql = "SELECT DISTINCT bookmark_row_id, username, play_code, play_full_name, act_nr, scene_nr, " +
//                "play_line_nr, scene_line_nr, position_in_view, script_text, annotation " +
//                "FROM bookmark WHERE active_0_or_1 = 1 " +
////                "(username = \"" + currentUser + "\" OR (share_with_others = 1 AND username IN (" + usersToShow + ")))" +
//                "ORDER BY play_code, date_time_added DESC;";

        String currentUser = UserManager.getUsername(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> visibleUsers = prefs.getStringSet("visible_bookmark_users", new HashSet<>());

        Log.d("show user list","user list for sql query for shared bookmarks (1): " + visibleUsers.toString());

        StringBuilder inClause = new StringBuilder();
        for (String user : visibleUsers) {
            if (!user.equals(currentUser)) {
                inClause.append("'").append(user).append("',");
            }
        }
        if (inClause.length() > 0) {
            inClause.setLength(inClause.length() - 1); // remove trailing comma
        } else {
            inClause.append("''"); // prevent SQL error if empty
        }

        Log.d("show user list","user list for sql query for shared bookmarks (2): " + inClause.toString());

        String sql = "SELECT DISTINCT bookmark_row_id, username, play_code, play_full_name, " +
                "act_nr, scene_nr, play_line_nr, scene_line_nr, " +
                "position_in_view, script_text, annotation, share_with_others " +
                "FROM bookmark " +
                "WHERE active_0_or_1 = 1 AND (" +
                "username = '" + currentUser + "' " +
                "OR (share_with_others = 1 AND username IN (" + inClause + "))" +
                ") " +
                "ORDER BY play_full_name, act_nr, scene_nr, play_line_nr;";

        Log.d("db tracker", "inside getBookmarksFromCloud sql: " + sql);

        RemoteDatabaseHelperHttp remoteDb = new RemoteDatabaseHelperHttp(context);

//        remoteDb.runQueryFromJava(sql, result -> {
//            if (result.isSuccess()) {
//                ArrayList<List<String>> bookmarkEntriesList = new ArrayList<>();
//
//                for (Map<String, String> row : result.getData()) {
//                    ArrayList<String> entry = new ArrayList<>();
//                    entry.add(row.get("bookmark_row_id"));
//                    entry.add(row.get("play_code"));
//                    entry.add(row.get("play_full_name"));
//                    entry.add(row.get("act_nr"));
//                    entry.add(row.get("scene_nr"));
//                    entry.add(row.get("script_text"));
//                    entry.add(row.get("annotation"));
//                    bookmarkEntriesList.add(entry);
//                }
//
//                callback.onBookmarksFetched(bookmarkEntriesList);
//
//            } else {
//                callback.onError(result.getException());
//            }
//        });

        remoteDb.runQueryFromJava(sql, new QueryResultCallback<List<Map<String, String>>>() {
//        remoteDb.runQueryFromJava(sql, new kotlin.jvm.functions.Function1<QueryResult<List<Map<String, String>>>, kotlin.Unit>() {
            @Override
//            public kotlin.Unit invoke(QueryResult<List<Map<String, String>>> result) {
            public void onResult(QueryResult<List<Map<String, String>>> result) {

                List<Map<String, String>> rows = result.getData();

                if (result.isSuccess()) {
                    ArrayList<List<String>> bookmarkEntriesList = new ArrayList<>();

                    for (Map<String, String> row : result.getData()) {
                        ArrayList<String> entry = new ArrayList<>();
                        entry.add(row.get("bookmark_row_id"));
                        entry.add(row.get("play_code"));
                        entry.add(row.get("play_full_name"));
                        entry.add(row.get("act_nr"));
                        entry.add(row.get("scene_nr"));
                        entry.add(row.get("script_text"));
                        entry.add(row.get("annotation"));
                        entry.add(row.get("username"));
                        entry.add(row.get("share_with_others"));
                        bookmarkEntriesList.add(entry);
                    }

                    callback.onBookmarksFetched(bookmarkEntriesList);

                } else {
                    callback.onError(result.getException());
                }

//                return kotlin.Unit.INSTANCE; // ✅ Return Unit to satisfy Kotlin function type
            }

//            @Override
//            public void onError(Throwable error) {
//                // ✅ This is the required method!
//                Log.e("DatabaseHandler", "Query failed", error);
//            }

        });

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
        selectQuery += "play_line_nr, scene_line_nr, position_in_view, script_text, annotation, active_0_or_1, share_with_others ";
        selectQuery += "FROM bookmark WHERE active_0_or_1 = 1 ";
        selectQuery += "ORDER BY play_code, date_time_added DESC;";
        //Log.d("sql",selectQuery);

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
                bookmarkEntries.add(cursor.getString(12)); // share_with_others

                // these are not needed but added to ensure we have the right number of columns in our arraylist
                bookmarkEntries.add(cursor.getString(1)); // username
                bookmarkEntries.add(cursor.getString(6)); // play_line_nr
                bookmarkEntries.add(cursor.getString(7)); // scene_line_nr
                bookmarkEntries.add(cursor.getString(8)); // position_in_view
                bookmarkEntries.add(cursor.getString(11)); // active_0_or_1

                bookmarkEntriesList.add(bookmarkEntries);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

//        return bookmarksList
        return bookmarkEntriesList;

    }


    // Get current scene number in case the user is returning to the play, so navigation goes to where they left off last time.
    // This is the version for the local sqlite db.
//    public void removeBookmarkLongClicked(Integer intBookmarkReference) {
//
//        SQLiteDatabase db;
//        String updateQuery = "UPDATE bookmark SET active_0_or_1 = 0 WHERE bookmark_row_id = "+String.valueOf(intBookmarkReference)+";";
//
//        db = this.getWritableDatabase();
//        db.execSQL(updateQuery);
//
//        Log.d("response", "completed");
//
//    }

    // This is the version for the sqlite cloud db.
    public void removeBookmarkLongClicked(Integer intBookmarkReference) {
        String sql = "UPDATE bookmark SET active_0_or_1 = 0 WHERE bookmark_row_id = " + intBookmarkReference + ";";

        RemoteDatabaseHelperHttp remoteDb = new RemoteDatabaseHelperHttp(context);

        remoteDb.runInsert(sql, new InsertCallback() {
            @Override
            public void onInsertSuccess() {
                Log.d("bookmark", "✅ Bookmark deactivated in SQLiteCloud.");
            }

            @Override
            public void onInsertFailure(Throwable e) {
                Log.e("bookmark", "❌ Failed to deactivate bookmark", e);
            }
        });
    }


    // Get script
    public ArrayList getScript_2d(Boolean boolAtPrologue, Boolean boolAtEpilogue) {

        ArrayList<ArrayList<String>> scriptLinesList_2d = new ArrayList<>();
        ArrayList<String> scriptLinesList = new ArrayList<>();
        // this will contain the character name and the script text for the particular script line
        ArrayList<String> strScriptIndividualRow = new ArrayList<>();

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
            selectQuery = "SELECT p.scene_line_number, p.script_text, p.character_short_name, p.play_code, p.play_line_number, b.bookmark_count, p.line_text, p.indent_text FROM " + TABLE_PLAY + " p LEFT OUTER JOIN (SELECT play_code, play_line_nr,  count(distinct bookmark_row_id) as bookmark_count from bookmark where active_0_or_1 = 1 group by play_code, play_line_nr) b on p.play_code = b.play_code and p.play_line_number = b.play_line_nr WHERE p.play_code='" + GlobalClass.selectedPlayCode + "' AND act_nr_roman='Prologue' ORDER BY p.play_line_number;";

        } else if (boolAtEpilogue) {
            selectQuery = "SELECT p.scene_line_number, p.script_text, p.character_short_name, p.play_code, p.play_line_number, b.bookmark_count, p.line_text, p.indent_text FROM " + TABLE_PLAY + " p LEFT OUTER JOIN (SELECT play_code, play_line_nr, count(distinct bookmark_row_id) as bookmark_count from bookmark where active_0_or_1 = 1 group by play_code, play_line_nr) b on p.play_code = b.play_code and p.play_line_number = b.play_line_nr WHERE p.play_code='" + GlobalClass.selectedPlayCode + "' AND act_nr_roman='Epilogue' ORDER BY p.play_line_number;";

        } else {
            selectQuery = "SELECT p.scene_line_number, p.script_text, p.character_short_name, p.play_code, p.play_line_number, b.bookmark_count, p.line_text, p.indent_text FROM " + TABLE_PLAY + " p LEFT OUTER JOIN (SELECT play_code, play_line_nr, count(distinct bookmark_row_id) as bookmark_count from bookmark where active_0_or_1 = 1 group by play_code, play_line_nr) b on p.play_code = b.play_code and p.play_line_number = b.play_line_nr WHERE p.play_code='" + GlobalClass.selectedPlayCode + "' AND act_nr=" + GlobalClass.selectedActNumber + " AND scene_nr=" + GlobalClass.selectedSceneNumber + " ORDER BY p.play_line_number;";

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

//                strScriptIndividualRow = null;
                strCharacter = cursor.getString(2)+"+";
                intLineNumber = cursor.getInt(0);
                // add line reference which will be included as a hidden row for reference purposes
                intPlayLineNumber = cursor.getInt(4);
                intBookmarkCount = cursor.getInt(5);
                intIndentFlag = cursor.getInt(7);
                strScriptIndividualRow.clear();

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

                    // Add the script line to the 2-dimensional array with
                    // first entry the character name and the second entry the script text.
                    strScriptIndividualRow.add(cursor.getString(2)); // add character name
                    strScriptIndividualRow.add(cursor.getString(1)); // add script text
                    scriptLinesList_2d.add(strScriptIndividualRow);

//                    Log.d("list info","2d list size: " + scriptLinesList_2d.size() + "; character entry added: " + scriptLinesList_2d.get(scriptLinesList_2d.size()-1).get(0));
//                    Log.d("list info","2d list size: " + scriptLinesList_2d.size() + "; script text entry added: " + scriptLinesList_2d.get(scriptLinesList_2d.size()-1).get(1));

                }

                if(intBookmarkCount>0){
                    strScriptText += " <" + String.valueOf(intBookmarkCount) + ">";
                    Log.d("indicate bookmark exists", "bookmark(s): " + String.valueOf(intBookmarkCount));
                }

                scriptLinesList.add("play_code: " + GlobalClass.selectedPlayCode + " Act " + GlobalClass.selectedActNumber + " Scene " + GlobalClass.selectedSceneNumber + " scene_line_nr " + intLineNumber + " play_line_nr " + String.valueOf(intPlayLineNumber));

                //Log.d("character update", "line nr "+intLineNumber +" previous line nr "+ intPreviousLineNumber + ", current: "+strCharacter + ", previous: " + strPreviousCharacter);

                if(GlobalClass.selectedActNumber==0 && GlobalClass.selectedSceneNumber==0){
                    scriptLinesList.add(strScriptText);

                }else {

                    // Adding user record to list
                    if(!strCharacter.equalsIgnoreCase("N.A.+") && !strCharacter.equalsIgnoreCase(strPreviousCharacter)){

                        //Log.d("flag", "check lines numbers to decide whether to add not NA new character " +strCharacter+" "+strScriptText);
                        if(intLineNumber == intPreviousLineNumber){
//                        scriptLinesList.add(strCharacter + "\n" + strScriptText );
                            scriptLinesList.add(strCharacter);

                            if(GlobalClass.intShowLineNumbers==1){
                                scriptLinesList.add(toString().valueOf(intLineNumber) + ' ' + strScriptText);
                                //Log.d("flag", "option 1: add script text without line number " +strCharacter + " line nr " + strScriptText);
                            } else {
                                scriptLinesList.add(strScriptText);

                            }

                        } else{
//                        scriptLinesList.add(strCharacter + "\n" + toString().valueOf(intLineNumber) + ' ' + strScriptText );
                            scriptLinesList.add(strCharacter);
                            //Log.d("flag", "NA *or* non-new character added " +strCharacter);
                            if(intLineNumber==0){
                                scriptLinesList.add(strScriptText);
                            } else {
                                if(GlobalClass.intShowLineNumbers==1){
                                    scriptLinesList.add(toString().valueOf(intLineNumber) + ' ' + strScriptText);
                                    //Log.d("flag", "option 1: add script text without line number " +strCharacter + " line nr " + strScriptText);
                                } else {
                                    scriptLinesList.add(strScriptText);

                                }
                            }

                        }
//                    Log.d("character update", "current != N.A.");
                    }else {

//                    Log.d("flag", "line numbers: " + intLineNumber +", "+ intPreviousLineNumber+", difference: "+String.valueOf(intLineNumber-intPreviousLineNumber));
                        if((intLineNumber-intPreviousLineNumber)==0){
//                        Log.d("flag", "option 2: add script text without line number " +strCharacter + " line nr " + strScriptText);

                            scriptLinesList.add(strScriptText);

                        } else{
                            if(intLineNumber==0){
                                scriptLinesList.add(strScriptText );
                            } else {
                                if(GlobalClass.intShowLineNumbers==1){
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

//                Log.d("flag", strPreviousCharacter + ", " + strCharacter + "; " + "intLineNumber " + toString().valueOf(intLineNumber) + ", " + " intPreviousLineNumber " + toString().valueOf(intPreviousLineNumber) + "; " + strScriptText);

                strPreviousCharacter = strCharacter;
                intPreviousLineNumber = intLineNumber;

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

//        Log.d("list info", "2d list size: " + scriptLinesList_2d.size());
//        Log.d("list info", "list size: " + scriptLinesList.size());

        // As at 13 April 2025:
        // The 2d (2-dimensional) version is still in development.
        // This gives us the character name the script information.
        // In this way, we can check which character is speaking and allow pauses for the user's
        // character to allow the user to say their own character lines.
        return scriptLinesList_2d;

    }

    public void updateBookmarkShareStatus(int bookmarkId, boolean share) {
        int shareFlag = share ? 1 : 0;
        String sql = "UPDATE bookmark SET share_with_others = " + shareFlag +
                " WHERE bookmark_row_id = " + bookmarkId;

        RemoteDatabaseHelperHttp db = new RemoteDatabaseHelperHttp(context);
        db.runInsert(sql, new InsertCallback() {

            @Override
            public void onInsertSuccess() {
                // Handle success, e.g. show a Toast or update UI
                Log.e("BookmarkShareUpdate", "Insert succeeded");

            }

            @Override
            public void onInsertFailure(Throwable e) {
                Log.e("BookmarkShareUpdate", "Failed to update share status", e);
            }
        });
    }

    public interface UsernameListCallback {
        void onUsernamesFetched(List<String> usernames);
    }

//    public void getAllUsernames(UsernameListCallback callback) {
//        String sql = "SELECT DISTINCT username FROM bookmark WHERE active_0_or_1 = 1";
//        remoteHelper.runQueryFromJava(sql, result -> {
//            List<String> usernames = new ArrayList<>();
//            for (List<String> row : result) {
//                usernames.add(row.get(0));
//            }
//            callback.onUsernamesFetched(usernames);
//        });
//    }

    public void getAllUsernames(UsernameListCallback callback) {
        String sql = "SELECT DISTINCT username FROM bookmark WHERE active_0_or_1 = 1";

        remoteHelper.runQueryFromJava(sql, result -> {
            List<String> usernames = new ArrayList<>();

            if (result.isSuccess()) {

//                List<Map<String, String>> rows = result.getData();
                // 01 Jun 2025: This produces the error:
                //"Incompatible types. Found: 'java.lang.Object', required:
                // 'java.util.List<java.util.Map<java.lang.String,java.lang.String>>'"
// 01 Jun 2025: This is a classic Java type safety issue.
// The root cause of the error is that result.getData() returns Object, because QueryResult<T> is a generic class from Kotlin,
// and Java cannot infer the actual generic type when calling getData() due to type erasure.
// Java says: “I don’t know if getData() actually returns a List<Map<String, String>> — all I see is Object.”
// You need to safely cast the result to the correct type, acknowledging that Java can't verify it at compile time:
                // Updated version is here:
                @SuppressWarnings("unchecked")
                List<Map<String, String>> rows = (List<Map<String, String>>) result.getData();

                if (rows != null) {
                    for (Map<String, String> row : rows) {
                        usernames.add(row.get("username"));
                    }
                }
            } else {
                Log.e("DatabaseHandler", "Failed to fetch usernames", result.exceptionOrNull());
            }

            // ✅ Always call the callback, even if it's empty
            callback.onUsernamesFetched(usernames);

        });

    }
}

