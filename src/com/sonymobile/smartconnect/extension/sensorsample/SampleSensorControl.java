/*
 Copyright (c) 2011, Sony Ericsson Mobile Communications AB
 Copyright (c) 2011-2013, Sony Mobile Communications AB

 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 * Neither the name of the Sony Ericsson Mobile Communications AB nor the names
 of its contributors may be used to endorse or promote products derived from
 this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.sonymobile.smartconnect.extension.sensorsample;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.aef.registration.Registration;
import com.sonyericsson.extras.liveware.aef.registration.Registration.SensorTypeValue;
import com.sonyericsson.extras.liveware.aef.sensor.Sensor;
import com.sonyericsson.extras.liveware.aef.sensor.Sensor.SensorAccuracy;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.control.ControlTouchEvent;
import com.sonyericsson.extras.liveware.extension.util.registration.DeviceInfoHelper;
import com.sonyericsson.extras.liveware.extension.util.sensor.AccessorySensor;
import com.sonyericsson.extras.liveware.extension.util.sensor.AccessorySensorEvent;
import com.sonyericsson.extras.liveware.extension.util.sensor.AccessorySensorEventListener;
import com.sonyericsson.extras.liveware.extension.util.sensor.AccessorySensorException;
import com.sonyericsson.extras.liveware.extension.util.sensor.AccessorySensorManager;

import java.util.ArrayList;
import java.util.List;

/**
 * The sample sensor control handles the accelerometer sensor on an accessory.
 * This class exists in one instance for every supported host application that
 * we have registered to
 */
class SampleSensorControl extends ControlExtension {

    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.RGB_565;

    private int mWidth = 220;
    private int mHeight = 176;
    private int mCurrentSensor = 0;

    private List<AccessorySensor> mSensors = new ArrayList<AccessorySensor>();

    private final AccessorySensorEventListener mListener = new AccessorySensorEventListener() {

        @Override
        public void onSensorEvent(AccessorySensorEvent sensorEvent) {
            Log.d(SampleExtensionService.LOG_TAG, "Listener: OnSensorEvent");
            updateCurrentDisplay(sensorEvent);
        }

    };

    /**
     * Create sample sensor control.
     * 
     * @param hostAppPackageName Package name of host application.
     * @param context The context.
     */
    SampleSensorControl(final String hostAppPackageName, final Context context) {
        super(context, hostAppPackageName);

        AccessorySensorManager manager = new AccessorySensorManager(context, hostAppPackageName);
        // Add accelerometer, if supported
        if (DeviceInfoHelper.isSensorSupported(context, hostAppPackageName,
                SensorTypeValue.ACCELEROMETER)) {
            mSensors.add(manager.getSensor(SensorTypeValue.ACCELEROMETER));
        }
        // Add magnetic field sensor, if supported
        if (DeviceInfoHelper.isSensorSupported(context, hostAppPackageName,
                SensorTypeValue.MAGNETIC_FIELD)) {
            mSensors.add(manager.getSensor(SensorTypeValue.MAGNETIC_FIELD));
        }
        // Add light sensor, if supported
        if (DeviceInfoHelper.isSensorSupported(context, hostAppPackageName, SensorTypeValue.LIGHT)) {
            mSensors.add(manager.getSensor(SensorTypeValue.LIGHT));
        }

        // Determine screen size
        determineSize(context, hostAppPackageName);
    }

    @Override
    public void onResume() {
        Log.d(SampleExtensionService.LOG_TAG, "Starting control");

        // Note: Setting the screen to be always on will drain the accessory
        // battery. It is done here solely for demonstration purposes
        setScreenState(Control.Intents.SCREEN_STATE_ON);

        // Start listening for sensor updates.
        register();

        updateCurrentDisplay(null);
    }

    @Override
    public void onPause() {
        // Stop sensor
        unregister();
    }

    @Override
    public void onDestroy() {
        // Stop sensor
        unregisterAndDestroy();
    }

    /**
     * Check if control supports the given width
     * 
     * @param context The context.
     * @param int width The width.
     * @return true if the control supports the given width
     */
    public static boolean isWidthSupported(Context context, int width) {
        return width == context.getResources().getDimensionPixelSize(
                R.dimen.smart_watch_2_control_width)
                || width == context.getResources().getDimensionPixelSize(
                        R.dimen.smart_watch_control_width);
    }

    /**
     * Check if control supports the given height
     * 
     * @param context The context.
     * @param int height The height.
     * @return true if the control supports the given height
     */
    public static boolean isHeightSupported(Context context, int height) {
        return height == context.getResources().getDimensionPixelSize(
                R.dimen.smart_watch_2_control_height)
                || height == context.getResources().getDimensionPixelSize(
                        R.dimen.smart_watch_control_height);
    }

    @Override
    public void onTouch(ControlTouchEvent event) {
        super.onTouch(event);
        if (event.getAction() == Control.Intents.TOUCH_ACTION_RELEASE) {
            toggleSensor();
        }
    }

    private void determineSize(Context context, String hostAppPackageName) {
        Log.d(SampleExtensionService.LOG_TAG, "Now determine screen size.");

        boolean smartWatch2Supported = DeviceInfoHelper.isSmartWatch2ApiAndScreenDetected(context,
                hostAppPackageName);
        if (smartWatch2Supported) {
            mWidth = context.getResources().getDimensionPixelSize(
                    R.dimen.smart_watch_2_control_width);
            mHeight = context.getResources().getDimensionPixelSize(
                    R.dimen.smart_watch_2_control_height);
        }
        else {
            mWidth = context.getResources()
                    .getDimensionPixelSize(R.dimen.smart_watch_control_width);
            mHeight = context.getResources().getDimensionPixelSize(
                    R.dimen.smart_watch_control_height);
        }
    }

    private AccessorySensor getCurrentSensor() {
        return mSensors.get(mCurrentSensor);
    }

    private void register() {
        Log.d(SampleExtensionService.LOG_TAG, "Register listener");
        AccessorySensor sensor = getCurrentSensor();
        if (sensor != null) {
            try {
                if (sensor.isInterruptModeSupported()) {
                    sensor.registerInterruptListener(mListener);
                } else {
                    sensor.registerFixedRateListener(mListener,
                            Sensor.SensorRates.SENSOR_DELAY_UI);
                }
            } catch (AccessorySensorException e) {
                Log.d(SampleExtensionService.LOG_TAG, "Failed to register listener", e);
            }
        }
    }

    private void unregister() {
        AccessorySensor sensor = getCurrentSensor();
        if (sensor != null) {
            sensor.unregisterListener();
        }
    }

    private void unregisterAndDestroy() {
        unregister();
        mSensors.clear();
        mSensors = null;
    }

    private void toggleSensor() {
        // Unregister the current sensor.
        unregister();

        // Toggle type
        nextSensor();

        // Register new sensor.
        register();

        // Update display
        updateCurrentDisplay(null);
    }

    private void nextSensor() {
        if (mCurrentSensor == (mSensors.size() - 1)) {
            mCurrentSensor = 0;
        }
        else {
            mCurrentSensor++;
        }
    }

    private void updateCurrentDisplay(AccessorySensorEvent sensorEvent) {
        AccessorySensor sensor = getCurrentSensor();
        if (sensor.getType().getName().equals(Registration.SensorTypeValue.ACCELEROMETER) ||
                sensor.getType().getName().equals(Registration.SensorTypeValue.MAGNETIC_FIELD)) {
            updateGenericSensorDisplay(sensorEvent, sensor.getType().getName());
        } else {
            updateLightSensorDisplay(sensorEvent);
        }
    }

    /**
     * Update the display with new accelerometer data.
     * 
     * @param sensorEvent The sensor event.
     */
    private void updateGenericSensorDisplay(AccessorySensorEvent sensorEvent, String sensorType) {
        // Create bitmap to draw in.
        Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, BITMAP_CONFIG);

        // Set default density to avoid scaling.
        bitmap.setDensity(DisplayMetrics.DENSITY_DEFAULT);

//        LinearLayout root = new LinearLayout(mContext);
//        root.setLayoutParams(new ViewGroup.LayoutParams(mWidth, mHeight));
//        root.setGravity(Gravity.CENTER);
//
//        LayoutInflater inflater = (LayoutInflater) mContext
//                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        LinearLayout sampleLayout = (LinearLayout) inflater.inflate(R.layout.generic_sensor_values,
//                root, true);
//
//        TextView title = (TextView) sampleLayout.findViewById(R.id.sensor_title);
//        title.setText(sensorType);

        // Update the values.
        if (sensorEvent != null) {
            float[] values = sensorEvent.getSensorValues();

            if (values != null && values.length == 3) {
            	  MainActivity.updateDraw(values[0], values[1], values[2]);
                /*TextView xView = (TextView) sampleLayout.findViewById(R.id.sensor_value_x);
                TextView yView = (TextView) sampleLayout.findViewById(R.id.sensor_value_y);
                TextView zView = (TextView) sampleLayout.findViewById(R.id.sensor_value_z);*/

          /*      // Show values with one decimal.
                xView.setText(String.format("%.1f", values[0]));
                yView.setText(String.format("%.1f", values[1]));
                zView.setText(String.format("%.1f", values[2]));*/
                
              
            }

//            // Show time stamp in milliseconds. (Reading is in nanoseconds.)
//            TextView timeStampView = (TextView) sampleLayout
//                    .findViewById(R.id.sensor_value_timestamp);
//            timeStampView.setText(String.format("%d", (long) (sensorEvent.getTimestamp() / 1e9)));
//
//            // Show sensor accuracy.
//            TextView accuracyView = (TextView) sampleLayout
//                    .findViewById(R.id.sensor_value_accuracy);
//            accuracyView.setText(getAccuracyText(sensorEvent.getAccuracy()));
        }

        /*root.measure(mWidth, mHeight);
        root.layout(0, 0, mWidth, mHeight);

        Canvas canvas = new Canvas(bitmap);
        sampleLayout.draw(canvas);
*/
        showBitmap(bitmap);
    }

    /**
     * Update the display with new light sensor data.
     * 
     * @param sensorEvent The sensor event.
     */
    private void updateLightSensorDisplay(AccessorySensorEvent sensorEvent) {
        // Create bitmap to draw in.
        Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, BITMAP_CONFIG);

        // Set default density to avoid scaling.
        bitmap.setDensity(DisplayMetrics.DENSITY_DEFAULT);

        LinearLayout root = new LinearLayout(mContext);
        root.setLayoutParams(new LayoutParams(mWidth, mHeight));

        LinearLayout sampleLayout = (LinearLayout) LinearLayout.inflate(mContext,
                R.layout.lightsensor_values, root);

        // Update the values.
        if (sensorEvent != null) {
            float[] values = sensorEvent.getSensorValues();

            if (values != null && values.length == 1) {
                TextView xView = (TextView) sampleLayout.findViewById(R.id.light_value);

                // Show values with one decimal.
                xView.setText(String.format("%.1f", values[0]));
            }

            // Show time stamp in milliseconds. (Reading is in nanoseconds.)
            TextView timeStampView = (TextView) sampleLayout
                    .findViewById(R.id.light_value_timestamp);
            timeStampView.setText(String.format("%d", (long) (sensorEvent.getTimestamp() / 1e9)));

            // Show sensor accuracy.
            TextView accuracyView = (TextView) sampleLayout
                    .findViewById(R.id.light_value_accuracy);
            accuracyView.setText(getAccuracyText(sensorEvent.getAccuracy()));
        }

        sampleLayout.measure(mWidth, mHeight);
        sampleLayout
                .layout(0, 0, sampleLayout.getMeasuredWidth(), sampleLayout.getMeasuredHeight());

        Canvas canvas = new Canvas(bitmap);
        sampleLayout.draw(canvas);

        showBitmap(bitmap);
    }

    /**
     * Convert an accuracy value to a text.
     * 
     * @param accuracy The accuracy value.
     * @return The text.
     */
    @SuppressLint("DefaultLocale")
    private String getAccuracyText(int accuracy) {

        switch (accuracy) {
            case SensorAccuracy.SENSOR_STATUS_UNRELIABLE:
                return mContext.getString(R.string.accuracy_unreliable);
            case SensorAccuracy.SENSOR_STATUS_ACCURACY_LOW:
                return mContext.getString(R.string.accuracy_low);
            case SensorAccuracy.SENSOR_STATUS_ACCURACY_MEDIUM:
                return mContext.getString(R.string.accuracy_medium);
            case SensorAccuracy.SENSOR_STATUS_ACCURACY_HIGH:
                return mContext.getString(R.string.accuracy_high);
            default:
                return String.format("%d", accuracy);
        }
    }
}
