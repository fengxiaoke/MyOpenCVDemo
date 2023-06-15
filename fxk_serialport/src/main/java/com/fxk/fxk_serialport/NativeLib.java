package com.fxk.fxk_serialport;

public class NativeLib {

    // Used to load the 'fxk_serialport' library on application startup.
    static {
        System.loadLibrary("fxk_serialport");
    }

    /**
     * A native method that is implemented by the 'fxk_serialport' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

}