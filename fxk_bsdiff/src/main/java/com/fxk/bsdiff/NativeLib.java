package com.fxk.bsdiff;

public class NativeLib {

    // Used to load the 'bsdiff' library on application startup.
    static {
        System.loadLibrary("bsdiff");
    }

    /**
     * A native method that is implemented by the 'bsdiff' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}