package com.shakespeare.new_app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASENAME = "play_navigation.db"; //<<<<<<<<< MUST match file name in assets/databases
//    private static String DATABASEPATH = Environment.getDataDirectory().getAbsolutePath() + "/data/com.example.new_app/databases/";
    private static String DATABASEPATH = Environment.getDataDirectory().getAbsolutePath() + "/data/com.shakespeare.new_app/databases/";
    public static final int DATABASEVERSION = 1;
    private final Context dbContext;

    public DatabaseHelper(Context context) throws IOException {
        super(context, DATABASENAME, null, DATABASEVERSION);

        String strCheckDb = String.valueOf(checkDataBase());
        Log.d("check database", "check database: " + strCheckDb);
//        this.getWritableDatabase(); //<<<<< will force database access and thus copy database from assets
//        Log.d("sqllite","**DatabaseHelper** getWritableDatabase");

        this.dbContext = context;

        if (!checkDataBase()){
            SQLiteDatabase db;
            db = this.getReadableDatabase(); // added by Dan M 1/1/2025 4.54am
            Log.d("sqllite","**DatabaseHelper**");

            // copy database, using guidance here:
            // https://stackoverflow.com/questions/52489640/android-copying-populated-sqlite-database-from-assets-folder-not-working-correc
            InputStream inputStream = dbContext.getAssets().open("databases/" + DATABASENAME);
            String outFileName = DATABASEPATH + DATABASENAME;
            OutputStream outputStream = new FileOutputStream(outFileName);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0){
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
//        outputStream.close();
            inputStream.close();

            Log.d("sqllite","**DatabaseHelper** db copied");
        } else {
            Log.d("sqllite","**DatabaseHelper** db already exists so no need to copy");
        }


    }

    /**
     * Check if the database exist and can be read.
     *
     * @return true if it exists and can be read, false if it doesn't
     */
    private boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        try {
            checkDB = SQLiteDatabase.openDatabase(DATABASEPATH + DATABASENAME, null,
                    SQLiteDatabase.OPEN_READONLY);
            checkDB.close();
            // database exists already.
            return true;
        } catch (SQLiteException e) {
            // database doesn't exist yet.
            return false;
        }
    }

//    @Override
//    public void onOpen(SQLiteDatabase db) {
//        super.onOpen(db);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//            db.disableWriteAheadLogging();
//            Log.d("sqllite","**DatabaseHelper** disableWriteAheadLogging");
//        }
//    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("sqllite","**DatabaseHelper** onCreate");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("sqllite","**DatabaseHelper** onUpgrade");

    }

}