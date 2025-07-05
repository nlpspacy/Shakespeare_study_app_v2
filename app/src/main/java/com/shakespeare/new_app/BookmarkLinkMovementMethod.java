package com.shakespeare.new_app;

import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.text.Layout;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.TextView;
import android.widget.Toast;

import com.shakespeare.new_app.models.Bookmark;

import java.util.List;

public class BookmarkLinkMovementMethod extends LinkMovementMethod {
//    private boolean isWaitingForDoubleTap = false;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final int DOUBLE_TAP_TIMEOUT_MS = 300;

    private final GestureDetector gestureDetector;
    private static final long DOUBLE_TAP_TIMEOUT = ViewConfiguration.getDoubleTapTimeout(); // Usually 300ms
    private Handler clickHandler = new Handler(Looper.getMainLooper());
    private boolean isDoubleTap = false;

    private static BookmarkLinkMovementMethod instance;

    public static MovementMethod getInstance(GestureDetector gestureDetector) {
        if (instance == null) {
            instance = new BookmarkLinkMovementMethod(gestureDetector);
        }
        return instance;
    }

    public static BookmarkLinkMovementMethod getTypedInstance(GestureDetector gestureDetector) {
        if (instance == null) {
            instance = new BookmarkLinkMovementMethod(gestureDetector);
        }
        return instance;
    }

    // 5 July 2025 commented this out because we have created another version
    // as we move from double tap detection on bookmark references to single tap.
    //
////    public void handleBookmarkClick(String ref, Context context, ScriptLine scriptLine) {
//public void handleBookmarkClick(String ref, Context context) {
//    Log.d("click response", "bookmark reference clicked");
//
//    AlertDialog.Builder builder = new AlertDialog.Builder(context);
//    builder.setTitle("Bookmark Reference")
//            .setMessage("Bookmark " + ref + " clicked.") // You can enhance this later with scriptLine
//            .setPositiveButton("OK", null)
//            .show();
//}

    // 5 July 2025, 7.38am.
    // We don't need handleClickWithDelay anymore because using single tap not double tap.
//    private void handleClickWithDelay(Runnable singleClickAction, Runnable doubleClickAction) {
//
//        if (isWaitingForDoubleTap) {
//            isWaitingForDoubleTap = false;
//            doubleClickAction.run();
//        } else {
//            isWaitingForDoubleTap = true;
//            new Handler(Looper.getMainLooper()).postDelayed(() -> {
//                if (isWaitingForDoubleTap) {
//                    isWaitingForDoubleTap = false;
//                    singleClickAction.run();
//                }
//            }, DOUBLE_TAP_TIMEOUT_MS);
//        }
//    }
    public BookmarkLinkMovementMethod(GestureDetector gestureDetector) {
        this.gestureDetector = gestureDetector;
    }

    // 5 July 2025, 7.39am.
    // Removing onTouchEvent because we are using single click, not double tap anymore.
//    @Override
//    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
//
//        Log.d("check", "onTouchEvent detected in BookmarkLinkMovementMethod Java class.");
//        boolean result = gestureDetector.onTouchEvent(event);
//        return result || super.onTouchEvent(widget, buffer, event);
//
//    }

    public class BookmarkUtils {
//        public static void showBookmarkDialog(String ref, Context context, ScriptLine scriptLine) {
        public static void showBookmarkDialog(String ref, Context context) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Bookmark Reference");
//            builder.setMessage("Reference: " + ref + "\n\nScript Line: " + scriptLine.getScriptText());
            builder.setMessage("Reference: " + ref + "\n\nScript Line: ");
            builder.setPositiveButton("OK", null);
            builder.show();
        }
    }

    // 5 July 2025: Moved here from MyRecyclerViewAdapter.java
    // and modified. We are using single click on bookmark references now,
    // not double tap.
    public void handleBookmarkClick(String ref, Context context) {
        Log.d("bookmark reference", "User tapped bookmark ref: <" + ref + ">");

        String fullText = "Bookmark text for reference <" + ref + "> (placeholder)";

        new AlertDialog.Builder(context)
                .setTitle("Bookmark <" + ref + ">")
                .setMessage(fullText)
                .setPositiveButton("Edit", (dialog, which) -> {
                    // TODO: Hook up edit functionality
                })
                .setNegativeButton("Delete", (dialog, which) -> {
                    // TODO: Hook up delete functionality
                })
                .setNeutralButton("Close", null)
                .show();
    }



}

