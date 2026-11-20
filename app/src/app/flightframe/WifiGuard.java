package app.flightframe;

import android.content.Context;
import android.net.wifi.WifiManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Keeps the wifi healthy on cheap frames:
 *  - switches the chip's power saving off (it causes silent drop-outs on many budget chips)
 *  - checks the router and the internet every 20 s
 *  - if the router can't be reached for 2 minutes, resets wifi (root) or asks Android to reconnect
 *  - keeps a history of drop-outs for the phone app (survives restarts)
 */
final class WifiGuard {
    private static int failStreak = 0, netFailStreak = 0;
    private static long dropStart = 0;
    private static long lastReset = 0;
    static volatile String lastCheck = "";
    private static final SimpleDateFormat F = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.US);

    static File histFile(Context c) { return new File(c.getFilesDir(), "wifi_history.json"); }

    static synchronized JSONArray history(Context c) {
        try { String s = Web.read(histFile(c)); return s.length() > 0 ? new JSONArray(s) : new JSONArray(); } catch (Exception e) { return new JSONArray(); }
    }

    private static synchronized void addEvent(Context c, String kind, String detail, long secs) {
        try {
            JSONArray h = history(c);
            h.put(new JSONObject().put("t", System.currentTimeMillis()).put("kind", kind).put("detail", detail).put("secs", secs));
            while (h.length() > 300) h.remove(0);
            FileOutputStream o = new FileOutputStream(histFile(c)); o.write(h.toString().getBytes("UTF-8")); o.close();
        } catch (Exception ignored) {}
        MainActivity.log("wifi: " + kind + " " + detail + (secs > 0 ? " (" + secs + " s)" : ""));
    }

    static String sh(String cmd) {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd});
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder b = new StringBuilder(); String l; while ((l = r.readLine()) != null) b.append(l).append('\n');
            p.waitFor();
            return b.toString();
        } catch (Exception e) { return ""; }
    }

    private static boolean ping(String host, int sec) {
        try { Process p = Runtime.getRuntime().exec(new String[]{"ping", "-c", "1", "-W", String.valueOf(sec), host}); return p.waitFor() == 0; }
        catch (Exception e) { return false; }
    }

    private static String gateway(Context c) {
        try {
            WifiManager wm = (WifiManager) c.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            int g = wm.getDhcpInfo().gateway;
            if (g != 0) return (g & 0xff) + "." + (g >> 8 & 0xff) + "." + (g >> 16 & 0xff) + "." + (g >> 24 & 0xff);
        } catch (Exception ignored) {}
        return "";
    }

    /** Called every ~20 s from the service (background thread). */
    static void check(Context c) {
        if (Screen.hasRoot()) {
            String ps = sh("su -c 'iw dev wlan0 get power_save' 2>/dev/null");
            if (ps.contains(": on")) { Screen.run("iw dev wlan0 set power_save off"); addEvent(c, "powersave", "chip power saving switched off", 0); }
        }
        String gw = gateway(c);
        boolean gwOk = gw.length() > 0 && ping(gw, 2);
        boolean netOk = gwOk && (ping("1.1.1.1", 3) || ping("8.8.8.8", 3));
        int rssi = 0;
        try { rssi = ((WifiManager) c.getApplicationContext().getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getRssi(); } catch (Exception ignored) {}
        lastCheck = F.format(new Date()) + " router " + (gwOk ? "ok" : "FAIL") + ", internet " + (netOk ? "ok" : "FAIL") + ", signal " + rssi + " dBm";

        if (!gwOk) {
            if (failStreak == 0) dropStart = System.currentTimeMillis();
            failStreak++;
            if (failStreak == 2) addEvent(c, "drop", "router not reachable" + (gw.length() == 0 ? " (no wifi connection)" : "") + ", signal " + rssi + " dBm", 0);
            if (failStreak >= 6 && System.currentTimeMillis() - lastReset > 300000) {   // 2 minutes down -> reset wifi
                lastReset = System.currentTimeMillis();
                addEvent(c, "reset", Screen.hasRoot() ? "restarting wifi" : "asking Android to reconnect", 0);
                if (Screen.hasRoot()) Screen.run("svc wifi disable; sleep 3; svc wifi enable");
                else try { ((WifiManager) c.getApplicationContext().getSystemService(Context.WIFI_SERVICE)).reconnect(); } catch (Exception ignored) {}
            }
        } else {
            if (failStreak >= 2) addEvent(c, "back", "wifi back", (System.currentTimeMillis() - dropStart) / 1000);
            failStreak = 0;
            if (!netOk) { netFailStreak++; if (netFailStreak == 3) addEvent(c, "internet", "router ok but no internet", 0); }
            else { if (netFailStreak >= 3) addEvent(c, "internet-back", "internet back", netFailStreak * 20L); netFailStreak = 0; }
        }
    }

    /** Summary for the phone app. */
    static JSONObject summary(Context c) throws Exception {
        JSONArray h = history(c);
        long day = System.currentTimeMillis() - 86400000L, week = System.currentTimeMillis() - 7 * 86400000L;
        int d24 = 0, d7 = 0, resets = 0;
        JSONArray recent = new JSONArray();
        for (int i = 0; i < h.length(); i++) {
            JSONObject e = h.getJSONObject(i);
            String k = e.optString("kind");
            if (k.equals("drop") || k.equals("internet")) { if (e.getLong("t") > day) d24++; if (e.getLong("t") > week) d7++; }
            if (k.equals("reset") && e.getLong("t") > week) resets++;
        }
        for (int i = Math.max(0, h.length() - 15); i < h.length(); i++) recent.put(h.get(i));
        return new JSONObject().put("drops24h", d24).put("drops7d", d7).put("resets7d", resets).put("last", lastCheck).put("recent", recent);
    }
}
