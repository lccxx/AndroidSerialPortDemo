package me.liuchong.android.ndkdemo3;

import android.util.Log;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Created by liuchong on 4/20/17.
 */

public class Application extends android.app.Application {
    public static final String TAG = "ndkdemo";

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("serial_port");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        FileDescriptor mFd = open("/dev/ttyS2", 115200, 0);
        final FileInputStream mInputStream = new FileInputStream(mFd);
        final FileOutputStream mOutputStream = new FileOutputStream(mFd);

        // READ
        new Thread(new Runnable() {
            @Override
            public void run() {
                int size;
                try {
                    byte[] buffer = new byte[64];
                    if (mInputStream == null) return;
                    size = mInputStream.read(buffer);
                    if (size > 0) {
                        onDataReceived(buffer, size);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // WRITE
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] mBuffer = new byte[1024];
                Arrays.fill(mBuffer, (byte) 0x55);
                try {
                    while(!isInterrupted())
                        mOutputStream.write(mBuffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onTerminate() {
        close();
        super.onTerminate();
    }

    private void onDataReceived(byte[] buffer, int size) {
        Log.d(TAG, new String(buffer, 0, size, Charset.defaultCharset()));
    }

    public boolean isInterrupted() {
        return false;
    }

    public native FileDescriptor open(String path, int baudrate, int flags);
    public native void close();

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
