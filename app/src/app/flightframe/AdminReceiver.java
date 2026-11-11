package app.flightframe;

import android.app.admin.DeviceAdminReceiver;

/** Lets the app switch the screen off (lockNow) on frames without root. Enable with: adb shell dpm set-active-admin app.flightframe/.AdminReceiver */
public class AdminReceiver extends DeviceAdminReceiver {}
