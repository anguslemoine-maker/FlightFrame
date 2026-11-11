package app.flightframe;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;

/** Background service: runs the phone-control server and the screen schedule, and keeps the display in front. */
public class KioskService extends Service {
    static volatile KioskService instance;
    static final long started = System.currentTimeMillis();
    private ControlServer server;
    private PowerManager.WakeLock cpu;
    private final Handler h = new Handler(Looper.getMainLooper());
    private Boolean lastScheduled = null;

    @Override public IBinder onBind(Intent i) { return null; }

    @Override public void onCreate() {
        super.onCreate();
        instance = this;
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel("ff", "Flight Frame", NotificationManager.IMPORTANCE_MIN);
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(ch);
            startForeground(1, new Notification.Builder(this, "ff").setContentTitle("Flight Frame").setContentText("Display running").setSmallIcon(R.drawable.icon).build());
        } else {
            startForeground(1, new Notification.Builder(this).setContentTitle("Flight Frame").setContentText("Display running").setSmallIcon(R.drawable.icon).setPriority(Notification.PRIORITY_MIN).build());
        }
        cpu = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "flightframe:cpu");
        cpu.acquire();
        server = new ControlServer(this, 8080);
        server.start();
        h.postDelayed(tick, 5000);
    }

    @Override public int onStartCommand(Intent i, int f, int id) { return START_STICKY; }

    @Override public void onDestroy() {
        if (server != null) server.shutdown();
        if (cpu != null && cpu.isHeld()) cpu.release();
        instance = null;
        super.onDestroy();
    }

    /** What the schedule says right now: true = on, false = off, null = no schedule. */
    static Boolean scheduled(Context c) {
        JSONObject s = Settings.get(c).optJSONObject("schedule");
        if (s == null || !s.optBoolean("enabled")) return null;
        Calendar cal = Calendar.getInstance();
        int dow = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7;   // Mon=0 .. Sun=6
        JSONArray days = s.optJSONArray("days");
        int m = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
        int on = Settings.minutes(s.optString("on", "07:00")), off = Settings.minutes(s.optString("off", "22:00"));
        boolean inHours = on < off ? (m >= on && m < off) : (m >= on || m < off);
        // a window that started yesterday (overnight) belongs to yesterday's day setting
        int dayIdx = (on > off && m < off) ? (dow + 6) % 7 : dow;
        boolean dayOn = days == null || days.optInt(dayIdx, 1) == 1;
        return inHours && dayOn;
    }

    private final Runnable tick = new Runnable() {
        public void run() {
            try {
                Boolean want = scheduled(KioskService.this);
                if (want != null && !want.equals(lastScheduled)) {
                    // only act when the schedule changes, so a manual on/off from the phone sticks until the next change
                    if (lastScheduled != null || !want) {
                        if (want) Screen.on(KioskService.this, "schedule"); else Screen.off(KioskService.this, "schedule");
                    }
                }
                lastScheduled = want;
                if (MainActivity.instance == null && !Screen.isOff) {
                    Intent i = new Intent(KioskService.this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }
            } catch (Exception e) { MainActivity.log("tick: " + e); }
            h.postDelayed(this, 20000);
        }
    };
}
