package com.finalproject.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class Event {
    Context context;
    ProgressDialog dialog;
    static public boolean debug = true;
    private static final int STACK_TRACE_LEVELS_UP = 5;

    public Event(Context context){
        this.context = context;
    }

    public static void info(String tag, String message)
    {
        Log.i(tag, getClassNameMethodNameAndLineNumber() + message);
    }

    static public void toast(Context context, String str){
        Toast.makeText(context, str, Toast.LENGTH_LONG).show();
    }

    static public void debugToast(Context context, String str){
        if(debug)
            Toast.makeText(context, str, Toast.LENGTH_LONG).show();
    }

    public void showloadingDialog(){
        dialog = new ProgressDialog(context);
        dialog.setMessage("Loading...");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public void dismiss(){
        dialog.dismiss();
    }

    public Boolean isShowing(){
        return dialog.isShowing();
    }

    /**
     * Get the current line number. Note, this will only work as called from
     * this class as it has to go a predetermined number of steps up the stack
     * trace. In this case 5.
     *
     * @author kvarela
     * @return int - Current line number.
     */
    private static int getLineNumber()
    {
        return Thread.currentThread().getStackTrace()[STACK_TRACE_LEVELS_UP].getLineNumber();
    }

    /**
     * Get the current class name. Note, this will only work as called from this
     * class as it has to go a predetermined number of steps up the stack trace.
     * In this case 5.
     *
     * @author kvarela
     * @return String - Current line number.
     */
    private static String getClassName()
    {
        String fileName = Thread.currentThread().getStackTrace()[STACK_TRACE_LEVELS_UP].getFileName();

        // kvarela: Removing ".java" and returning class name
        return fileName.substring(0, fileName.length() - 5);
    }

    /**
     * Get the current method name. Note, this will only work as called from
     * this class as it has to go a predetermined number of steps up the stack
     * trace. In this case 5.
     *
     * @author kvarela
     * @return String - Current line number.
     */
    private static String getMethodName()
    {
        return Thread.currentThread().getStackTrace()[STACK_TRACE_LEVELS_UP].getMethodName();
    }

    /**
     * Returns the class name, method name, and line number from the currently
     * executing log call in the form .()-
     *
     * @author kvarela
     * @return String - String representing class name, method name, and line
     *         number.
     */
    private static String getClassNameMethodNameAndLineNumber()
    {
        return "[" + getClassName() + "." + getMethodName() + "():" + getLineNumber() + "]: ";
    }
}
