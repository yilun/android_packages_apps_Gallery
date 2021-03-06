package com.android.camera.stress;

import android.app.Instrumentation;
import android.content.Intent;
import android.os.Debug;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.LargeTest;
import android.app.Activity;
import android.util.Log;
import java.io.FileWriter;
import java.io.BufferedWriter;

/**
 * Test cases to measure the camera and video recorder startup time.
 */
public class CameraStartUp extends InstrumentationTestCase {

    private static final int TOTAL_NUMBER_OF_STARTUP = 20;

    private String TAG = "CameraStartUp";
    private static final String CAMERA_TEST_OUTPUT_FILE = "/sdcard/mediaStressOut.txt";
    private static final String CAMERA_PACKAGE_NAME = "com.android.camera";
    private static final String CAMERA_ACTIVITY_NAME = "com.android.camera.Camera";
    private static final String VIDEORECORDER_ACTIVITY_NAME = "com.android.camera.VideoCamera";
    private static int WAIT_TIME_FOR_PREVIEW = 1500; //1.5 second

    private long launchCamera() {
        long startupTime = 0;
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setClassName(CAMERA_PACKAGE_NAME, CAMERA_ACTIVITY_NAME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            long beforeStart = System.currentTimeMillis();
            Instrumentation inst = getInstrumentation();
            Activity cameraActivity = inst.startActivitySync(intent);
            long cameraStarted = System.currentTimeMillis();
            cameraActivity.finish();
            startupTime = cameraStarted - beforeStart;
            Thread.sleep(1000);
            Log.v(TAG, "camera startup time: " + startupTime);
        } catch (Exception e) {
            Log.v(TAG, e.toString());
            fail("Fails to get the output file");
        }
        return startupTime;
    }

    private long launchVideo() {
        long startupTime = 0;

        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setClassName(CAMERA_PACKAGE_NAME, VIDEORECORDER_ACTIVITY_NAME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            long beforeStart = System.currentTimeMillis();
            Instrumentation inst = getInstrumentation();
            Activity recorderActivity = inst.startActivitySync(intent);
            long cameraStarted = System.currentTimeMillis();
            recorderActivity.finish();
            startupTime = cameraStarted - beforeStart;
            Log.v(TAG, "Video Startup Time = " + startupTime);
            // wait for 1s to make sure it reach a clean stage
            Thread.sleep(WAIT_TIME_FOR_PREVIEW);
            Log.v(TAG, "video startup time: " + startupTime);
        } catch (Exception e) {
            Log.v(TAG, e.toString());
            fail("Fails to launch video output file");
        }
        return startupTime;
    }

    private void writeToOutputFile(String startupTag, long totalStartupTime,
            String individualStartupTime) throws Exception {
        //TODO (yslau) : Need to integrate the output data with central dashboard
        try {
            FileWriter fstream = null;
            fstream = new FileWriter(CAMERA_TEST_OUTPUT_FILE, true);
            long averageStartupTime = totalStartupTime / TOTAL_NUMBER_OF_STARTUP;
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(startupTag + "\n");
            out.write("Number of loop: " + TOTAL_NUMBER_OF_STARTUP + "\n");
            out.write(individualStartupTime + "\n\n");
            out.write("Average startup time :" + averageStartupTime + " ms\n\n");
            out.close();
            fstream.close();
        } catch (Exception e) {
            fail("Camera write output to file");
        }
    }

    @LargeTest
    public void testLaunchVideo() throws Exception {
        String individualStartupTime;
        individualStartupTime = "Individual Video Startup Time = ";
        long totalStartupTime = 0;
        long startupTime = 0;
        for ( int i =0; i< TOTAL_NUMBER_OF_STARTUP; i++){
            startupTime = launchVideo();
            totalStartupTime += startupTime;
            individualStartupTime += startupTime + " ,";
        }
        Log.v(TAG, "totalStartupTime =" + totalStartupTime);
        writeToOutputFile("Video Recorder Startup Time: ", totalStartupTime,
                individualStartupTime);
    }

    @LargeTest
    public void testLaunchCamera() throws Exception {
        String individualStartupTime;
        individualStartupTime = "Individual Camera Startup Time = ";
        long totalStartupTime =0;
        long startupTime = 0;
        for ( int i =0; i< TOTAL_NUMBER_OF_STARTUP; i++){
            startupTime = launchCamera();
            totalStartupTime += startupTime;
            individualStartupTime += startupTime + " ,";
        }
        Log.v(TAG, "totalStartupTime =" + totalStartupTime);
        writeToOutputFile("Camera Startup Time: ", totalStartupTime,
                individualStartupTime);
    }
}