package app.flightframe;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.webkit.WebView;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Tiny HTTP server on port 8080 for the phone app.
 *   GET  /                   phone app (web app, add to home screen)
 *   GET  /api/state          settings + status
 *   POST /api/settings       partial settings JSON
 *   POST /api/action         {"action":"screen_on|screen_off|reload|restart|identify|update|clear_cache|reset"}
 *   GET  /api/screenshot.jpg live picture of the display
 *   POST /api/file?name=...  upload a display file (e.g. radarmap.jpg / radarmap.js)
 *   GET  /api/log            recent app log
 *   GET  /api/backup         full settings (incl. keys) / POST /api/restore
 */
final class ControlServer extends Thread {
    private final Context ctx;
    private final int port;
    private ServerSocket ss;
    private volatile boolean running = true;

    ControlServer(Context c, int port) { this.ctx = c.getApplicationContext(); this.port = port; setName("ff-http"); setDaemon(true); }

    void shutdown() { running = false; try { ss.close(); } catch (Exception ignored) {} }

    @Override public void run() {
        while (running) {
            try {
                ss = new ServerSocket(port);
                ss.setReuseAddress(true);
                MainActivity.log("phone control on port " + port);
                while (running) {
                    final Socket s = ss.accept();
                    new Thread(new Runnable() { public void run() { handle(s); } }).start();
                }
            } catch (Exception e) {
                if (!running) return;
                MainActivity.log("server: " + e);
                try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
            }
        }
    }

    private void handle(Socket s) {
        try {
            s.setSoTimeout(20000);
            InputStream in = s.getInputStream();
            String line = readLine(in);
            if (line == null) { s.close(); return; }
            String[] rl = line.split(" ");
            String method = rl[0], target = rl.length > 1 ? rl[1] : "/";
            Map<String, String> hdr = new HashMap<>();
            String l;
            while ((l = readLine(in)) != null && l.length() > 0) {
                int i = l.indexOf(':');
                if (i > 0) hdr.put(l.substring(0, i).trim().toLowerCase(), l.substring(i + 1).trim());
            }
            int len = 0;
            try { len = Integer.parseInt(hdr.containsKey("content-length") ? hdr.get("content-length") : "0"); } catch (Exception ignored) {}
            if (len > 20 * 1024 * 1024) { send(s, 413, "text/plain", "too large".getBytes()); return; }
            byte[] body = new byte[len];
            int n = 0; while (n < len) { int r = in.read(body, n, len - n); if (r < 0) break; n += r; }
            String path = target, query = "";
            int q = target.indexOf('?');
            if (q >= 0) { path = target.substring(0, q); query = target.substring(q + 1); }
            route(s, method, path, query, hdr, body);
        } catch (Exception e) {
            try { send(s, 500, "text/plain", ("error: " + e).getBytes("UTF-8")); } catch (Exception ignored) {}
        } finally {
            try { s.close(); } catch (Exception ignored) {}
        }
    }

    private void route(Socket s, String m, String p, String q, Map<String, String> hdr, byte[] body) throws Exception {
        if (p.startsWith("/api/")) {
            String pin = Settings.get(ctx).optString("pin", "");
            String given = hdr.containsKey("x-pin") ? hdr.get("x-pin") : param(q, "pin");
            if (pin.length() > 0 && !pin.equals(given) && !p.equals("/api/ping")) { json(s, 401, "{\"error\":\"pin\"}"); return; }
        }
        if (p.equals("/api/ping")) { json(s, 200, "{\"ok\":true,\"app\":\"flightframe\",\"version\":\"" + BuildInfo.VERSION + "\",\"pin\":" + (Settings.get(ctx).optString("pin").length() > 0) + "}"); return; }
        if (p.equals("/api/state")) { json(s, 200, state().toString()); return; }
        if (p.equals("/api/settings") && m.equals("POST")) {
            JSONObject patch = new JSONObject(new String(body, "UTF-8"));
            JSONObject sch = patch.optJSONObject("schiphol");
            if (sch != null && "********".equals(sch.optString("secret"))) sch.remove("secret");
            if ("set".equals(patch.optString("pin"))) patch.remove("pin");
            boolean display = Settings.update(ctx, patch);
            applyAfterChange(display);
            json(s, 200, "{\"ok\":true,\"reloaded\":" + display + "}");
            return;
        }
        if (p.equals("/api/action") && m.equals("POST")) {
            JSONObject a = new JSONObject(new String(body, "UTF-8"));
            json(s, 200, action(a.optString("action"), a).toString());
            return;
        }
        if (p.equals("/api/screenshot.jpg")) {
            byte[] img = MainActivity.screenshot(Math.max(200, Math.min(1080, intParam(q, "w", 540))));
            if (img == null) { send(s, 503, "text/plain", "display not running".getBytes()); return; }
            send(s, 200, "image/jpeg", img);
            return;
        }
        if (p.equals("/api/file") && m.equals("POST")) {
            String name = param(q, "name");
            if (!name.matches("^[A-Za-z0-9_.-]{1,64}$") || name.startsWith(".")) { json(s, 400, "{\"error\":\"bad name\"}"); return; }
            File out = new File(Web.dir(ctx), name);
            FileOutputStream o = new FileOutputStream(out); o.write(body); o.close();
            MainActivity.log("uploaded " + name + " (" + body.length + " bytes)");
            applyAfterChange(true);
            json(s, 200, "{\"ok\":true}");
            return;
        }
        if (p.equals("/api/log")) { synchronized (MainActivity.log) { send(s, 200, "text/plain; charset=utf-8", MainActivity.log.toString().getBytes("UTF-8")); } return; }
        if (p.equals("/api/backup")) {
            String json = Settings.get(ctx).toString(2);
            send(s, 200, "application/json", json.getBytes("UTF-8"), "Content-Disposition: attachment; filename=\"flightframe-settings.json\"\r\n");
            return;
        }
        if (p.equals("/api/restore") && m.equals("POST")) {
            Settings.replace(ctx, new JSONObject(new String(body, "UTF-8")));
            applyAfterChange(true);
            json(s, 200, "{\"ok\":true}");
            return;
        }
        // phone app (static files from assets/companion)
        if (m.equals("GET")) {
            String f = p.equals("/") ? "index.html" : p.substring(1);
            if (f.contains("..")) { send(s, 404, "text/plain", "not found".getBytes()); return; }
            try {
                AssetManager am = ctx.getAssets();
                InputStream a = am.open("companion/" + f);
                ByteArrayOutputStream bo = new ByteArrayOutputStream();
                byte[] b = new byte[8192]; int r; while ((r = a.read(b)) > 0) bo.write(b, 0, r); a.close();
                send(s, 200, mime(f), bo.toByteArray(), f.equals("index.html") || f.endsWith(".webmanifest") ? "Cache-Control: no-cache\r\n" : "Cache-Control: max-age=86400\r\n");
            } catch (Exception e) { send(s, 404, "text/plain", "not found".getBytes()); }
            return;
        }
        send(s, 404, "text/plain", "not found".getBytes());
    }

    private void applyAfterChange(final boolean reloadDisplay) {
        MainActivity.onUi(new Runnable() { public void run() {
            MainActivity a = MainActivity.instance; if (a == null) return;
            a.applyAppSettings();
            if (reloadDisplay) a.reload("settings changed");
        }});
    }

    private JSONObject action(String act, JSONObject a) throws Exception {
        JSONObject r = new JSONObject().put("ok", true);
        switch (act) {
            case "screen_on": Screen.on(ctx, "phone"); break;
            case "screen_off": Screen.off(ctx, "phone"); break;
            case "reload": MainActivity.onUi(new Runnable() { public void run() { if (MainActivity.instance != null) MainActivity.instance.reload("phone"); } }); break;
            case "clear_cache":
                MainActivity.onUi(new Runnable() { public void run() {
                    MainActivity x = MainActivity.instance; if (x == null) return;
                    android.webkit.WebStorage.getInstance().deleteAllData(); x.reload("cache cleared");
                }});
                break;
            case "identify":
                final String txt = "Flight Frame\n" + ip() + ":8080";
                MainActivity.onUi(new Runnable() { public void run() { if (MainActivity.instance != null) MainActivity.instance.showBanner(txt, 15000); } });
                break;
            case "update":
                r.put("result", Web.update(ctx, Settings.get(ctx).optString("updateBase")));
                applyAfterChange(true);
                break;
            case "restart":
                new Thread(new Runnable() { public void run() {
                    try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                    Intent i = new Intent(ctx, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    android.app.AlarmManager am = (android.app.AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
                    am.set(android.app.AlarmManager.RTC, System.currentTimeMillis() + 1500,
                            android.app.PendingIntent.getActivity(ctx, 1, i, android.app.PendingIntent.FLAG_CANCEL_CURRENT));
                    System.exit(0);
                }}).start();
                break;
            case "reboot":
                if (!Screen.hasRoot() || !Screen.run("reboot")) { r.put("ok", false).put("error", "Rebooting needs root on this frame"); }
                break;
            case "reset":
                Settings.replace(ctx, new JSONObject());
                applyAfterChange(true);
                break;
            default: r.put("ok", false).put("error", "unknown action");
        }
        return r;
    }

    @SuppressWarnings("deprecation")
    private JSONObject state() throws Exception {
        JSONObject st = new JSONObject();
        st.put("settings", Settings.publicCopy(ctx));
        JSONObject x = new JSONObject();
        x.put("version", BuildInfo.VERSION);
        x.put("model", Build.MANUFACTURER + " " + Build.MODEL);
        x.put("android", Build.VERSION.RELEASE);
        try { if (Build.VERSION.SDK_INT >= 26) { android.content.pm.PackageInfo wv = WebView.getCurrentWebViewPackage(); if (wv != null) x.put("webview", wv.versionName); } } catch (Exception ignored) {}
        x.put("ip", ip());
        x.put("uptimeDevice", SystemClock.elapsedRealtime() / 1000);
        x.put("uptimeApp", (System.currentTimeMillis() - KioskService.started) / 1000);
        x.put("screenOn", !Screen.isOff && Screen.panelOn(ctx));
        x.put("screenMethod", Screen.method);
        x.put("root", Screen.hasRoot());
        x.put("admin", Screen.adminActive(ctx));
        Boolean sch = KioskService.scheduled(ctx);
        x.put("scheduledOn", sch == null ? JSONObject.NULL : sch);
        x.put("heartbeatAgo", (System.currentTimeMillis() - MainActivity.lastHeartbeat) / 1000);
        x.put("reloads", MainActivity.reloads);
        x.put("time", System.currentTimeMillis());
        x.put("timezone", java.util.TimeZone.getDefault().getID());
        try {
            WifiManager wm = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wi = wm.getConnectionInfo();
            if (wi != null) {
                x.put("ssid", wi.getSSID() == null ? "" : wi.getSSID().replace("\"", ""));
                x.put("rssi", wi.getRssi());
                x.put("signal", WifiManager.calculateSignalLevel(wi.getRssi(), 5));
                x.put("linkMbps", wi.getLinkSpeed());
                if (Build.VERSION.SDK_INT >= 21) x.put("freqMHz", wi.getFrequency());
            }
        } catch (Exception ignored) {}
        try {
            StatFs fs = new StatFs(Environment.getDataDirectory().getPath());
            x.put("freeMB", fs.getAvailableBytes() / 1048576);
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            ((ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryInfo(mi);
            x.put("memFreeMB", mi.availMem / 1048576);
            x.put("memTotalMB", mi.totalMem / 1048576);
        } catch (Exception ignored) {}
        try { String t = Web.read(new File("/sys/class/thermal/thermal_zone0/temp")).trim(); if (t.length() > 0) { double v = Double.parseDouble(t); x.put("tempC", v > 1000 ? v / 1000 : v); } } catch (Exception ignored) {}
        st.put("status", x);
        try { st.put("display", new JSONObject(MainActivity.lastReport)); } catch (Exception e) { st.put("display", new JSONObject()); }
        st.put("displayAgo", MainActivity.lastReportAt == 0 ? -1 : (System.currentTimeMillis() - MainActivity.lastReportAt) / 1000);
        File map = new File(Web.dir(ctx), "radarmap.js");
        st.put("mapFile", map.exists() ? Web.read(map).replaceAll("(?s).*window.FF_MAP=", "").replaceAll(";\\s*$", "") : "");
        return st;
    }

    static String ip() {
        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!ni.isUp() || ni.isLoopback()) continue;
                for (InetAddress a : Collections.list(ni.getInetAddresses())) if (a instanceof Inet4Address) return a.getHostAddress();
            }
        } catch (Exception ignored) {}
        return "";
    }

    private static String param(String q, String k) {
        for (String kv : q.split("&")) {
            int i = kv.indexOf('=');
            if (i > 0 && kv.substring(0, i).equals(k)) try { return URLDecoder.decode(kv.substring(i + 1), "UTF-8"); } catch (Exception e) { return ""; }
        }
        return "";
    }
    private static int intParam(String q, String k, int d) { try { return Integer.parseInt(param(q, k)); } catch (Exception e) { return d; } }

    private static String mime(String f) {
        if (f.endsWith(".html")) return "text/html; charset=utf-8";
        if (f.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (f.endsWith(".css")) return "text/css; charset=utf-8";
        if (f.endsWith(".png")) return "image/png";
        if (f.endsWith(".svg")) return "image/svg+xml";
        if (f.endsWith(".webmanifest") || f.endsWith(".json")) return "application/manifest+json";
        return "application/octet-stream";
    }

    private static String readLine(InputStream in) throws Exception {
        StringBuilder b = new StringBuilder(); int c;
        while ((c = in.read()) >= 0) { if (c == '\n') break; if (c != '\r') b.append((char) c); if (b.length() > 8192) break; }
        if (c < 0 && b.length() == 0) return null;
        return b.toString();
    }

    private static void json(Socket s, int code, String body) throws Exception { send(s, code, "application/json; charset=utf-8", body.getBytes("UTF-8"), "Cache-Control: no-store\r\n"); }
    private static void send(Socket s, int code, String type, byte[] body) throws Exception { send(s, code, type, body, ""); }
    private static void send(Socket s, int code, String type, byte[] body, String extra) throws Exception {
        OutputStream o = s.getOutputStream();
        String st = code == 200 ? "OK" : code == 401 ? "Unauthorized" : code == 404 ? "Not Found" : "Error";
        String h = "HTTP/1.1 " + code + " " + st + "\r\nContent-Type: " + type + "\r\nContent-Length: " + body.length + "\r\nConnection: close\r\n" + extra + "\r\n";
        o.write(h.getBytes("UTF-8")); o.write(body); o.flush();
    }
}
