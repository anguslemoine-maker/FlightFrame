package app.flightframe;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import java.io.DataOutputStream;

/**
 * Turning the panel off and on.
 * Order of preference: root ("su") key events -> device-admin lock + wake lock -> software blackout (backlight at minimum).
 */
final class Screen {
    static volatile boolean isOff = false;
    static volatile boolean softOff = false;
    static volatile String method = "";
    private static Boolean root;

    static boolean hasRoot() {
        if (root != null) return root;
        root = run("id") ;
        return root;
    }

    static boolean run(String cmd) {
        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream o = new DataOutputStream(p.getOutputStream());
            o.writeBytes(cmd + "\nexit\n"); o.flush(); o.close();
            boolean done = false;
            for (int i = 0; i < 50 && !done; i++) { try { p.exitValue(); done = true; } catch (IllegalThreadStateException e) { Thread.sleep(100); } }
            if (!done) { p.destroy(); return false; }
            return p.exitValue() == 0;
        } catch (Exception e) { return false; }
    }

    static boolean adminActive(Context c) {
        DevicePolicyManager dpm = (DevicePolicyManager) c.getSystemService(Context.DEVICE_POLICY_SERVICE);
        return dpm != null && dpm.isAdminActive(new ComponentName(c, AdminReceiver.class));
    }

    static synchronized void off(Context c, String why) {
        if (isOff) return;
        String mode = Settings.get(c).optString("screenOffMode", "auto");
        MainActivity.log("screen off (" + why + ")");
        isOff = true; softOff = false;
        if (!mode.equals("dim") && !mode.equals("lock") && hasRoot() && run("input keyevent 223")) { method = "root"; return; }
        if (!mode.equals("dim") && adminActive(c)) {
            try {
                ((DevicePolicyManager) c.getSystemService(Context.DEVICE_POLICY_SERVICE)).lockNow();
                method = "admin"; return;
            } catch (Exception e) { MainActivity.log("lockNow failed: " + e); }
        }
        method = "dim"; softOff = true;
        MainActivity.onUi(new Runnable() { public void run() { if (MainActivity.instance != null) MainActivity.instance.setBlackout(true); } });
    }

    @SuppressWarnings("deprecation")
    static synchronized void on(Context c, String why) {
        MainActivity.log("screen on (" + why + ")");
        boolean was = isOff; isOff = false;
        if (method.equals("root")) run("input keyevent 224");
        try {
            PowerManager pm = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "flightframe:wake");
            wl.acquire(3000);
        } catch (Exception e) { MainActivity.log("wake: " + e); }
        if (softOff) {
            softOff = false;
            MainActivity.onUi(new Runnable() { public void run() { if (MainActivity.instance != null) MainActivity.instance.setBlackout(false); } });
        }
        // bring the display back to the front (after lock / other apps)
        Intent i = new Intent(c, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        c.startActivity(i);
        if (was && MainActivity.instance != null) MainActivity.onUi(new Runnable() { public void run() { MainActivity.instance.reload("screen on"); } });
        method = "";
    }

    static boolean panelOn(Context c) {
        PowerManager pm = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
        return pm.isInteractive() && !softOff;
    }
}
