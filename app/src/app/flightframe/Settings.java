package app.flightframe;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Iterator;

/** All Flight Frame settings, stored as one JSON file. The display page reads the same JSON. */
public final class Settings {
    private static JSONObject cur;

    /** Keys that only the app uses; changing them does not reload the display. */
    static final String[] APP_ONLY = {"brightness", "brightnessNight", "schedule", "pin", "frameName", "keepAwake", "updateBase", "screenOffMode"};

    static JSONObject defaults() {
        try {
            JSONObject d = new JSONObject();
            // place
            d.put("name", "Leiderdorp");
            d.put("lat", 52.1583);
            d.put("lon", 4.5333);
            d.put("tz", "");
            // display
            d.put("theme", "midnight");
            d.put("accent", "");
            d.put("units", "metric");
            d.put("clock24", true);
            d.put("rotation", "auto");
            d.put("radiusKm", 18);
            d.put("radarRange", 20);
            d.put("showMap", true);
            d.put("mapCredit", true);
            d.put("showRadar", true);
            d.put("showList", true);
            d.put("listCount", 4);
            d.put("showPlane", true);
            d.put("showWeather", true);
            d.put("showRain", true);
            d.put("showISS", true);
            d.put("featured", "nearest");
            d.put("cycleSec", 20);
            d.put("minAltFt", 0);
            d.put("maxAltFt", 60000);
            d.put("hideNoCallsign", false);
            d.put("pollSec", 8);
            d.put("nightDim", true);
            d.put("nightFrom", "23:00");
            d.put("nightTo", "07:00");
            d.put("nightLevel", 65);
            JSONObject sch = new JSONObject();
            sch.put("id", "");
            sch.put("secret", "");
            d.put("schiphol", sch);
            // app
            d.put("frameName", "Flight Frame");
            d.put("brightness", -1);          // -1 = system default, else 1..100
            JSONObject s = new JSONObject();
            s.put("enabled", true);
            s.put("on", "07:00");
            s.put("off", "22:00");
            s.put("days", new JSONArray("[1,1,1,1,1,1,1]")); // Mon..Sun
            d.put("schedule", s);
            d.put("screenOffMode", "auto");   // auto | lock | dim
            d.put("pin", "");
            d.put("updateBase", "");
            return d;
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    static File file(Context c) { return new File(c.getFilesDir(), "settings.json"); }

    static synchronized JSONObject get(Context c) {
        if (cur != null) return cur;
        JSONObject d = defaults();
        try {
            File f = file(c);
            if (f.exists()) {
                byte[] b = new byte[(int) f.length()];
                FileInputStream in = new FileInputStream(f);
                int n = 0; while (n < b.length) { int r = in.read(b, n, b.length - n); if (r < 0) break; n += r; }
                in.close();
                merge(d, new JSONObject(new String(b, "UTF-8")));
            }
        } catch (Exception ignored) {}
        cur = d;
        return cur;
    }

    /** Merge a partial update; returns true if any display (non app-only) key changed. */
    static synchronized boolean update(Context c, JSONObject patch) throws Exception {
        JSONObject s = get(c);
        boolean displayChanged = false;
        Iterator<String> it = patch.keys();
        while (it.hasNext()) {
            String k = it.next();
            Object nv = patch.get(k);
            Object ov = s.opt(k);
            if (ov != null && ov.toString().equals(nv.toString())) continue;
            if (nv instanceof JSONObject && ov instanceof JSONObject) merge((JSONObject) ov, (JSONObject) nv);
            else s.put(k, nv);
            boolean appOnly = false;
            for (String a : APP_ONLY) if (a.equals(k)) appOnly = true;
            if (!appOnly) displayChanged = true;
        }
        save(c);
        return displayChanged;
    }

    static synchronized void replace(Context c, JSONObject all) throws Exception {
        JSONObject d = defaults();
        merge(d, all);
        cur = d;
        save(c);
    }

    static synchronized void save(Context c) throws Exception {
        File f = file(c), tmp = new File(f.getPath() + ".tmp");
        FileOutputStream o = new FileOutputStream(tmp);
        o.write(cur.toString(2).getBytes("UTF-8"));
        o.getFD().sync();
        o.close();
        tmp.renameTo(f);
    }

    private static void merge(JSONObject into, JSONObject from) throws Exception {
        Iterator<String> it = from.keys();
        while (it.hasNext()) {
            String k = it.next();
            Object v = from.get(k);
            Object o = into.opt(k);
            if (v instanceof JSONObject && o instanceof JSONObject) merge((JSONObject) o, (JSONObject) v);
            else into.put(k, v);
        }
    }

    /** Settings without secrets, for showing in the phone app. */
    static JSONObject publicCopy(Context c) throws Exception {
        JSONObject s = new JSONObject(get(c).toString());
        s.put("pin", s.optString("pin").length() > 0 ? "set" : "");
        JSONObject sch = s.optJSONObject("schiphol");
        if (sch != null && sch.optString("secret").length() > 0) sch.put("secret", "********");
        return s;
    }

    static int minutes(String hhmm) {
        try { String[] p = hhmm.split(":"); return Integer.parseInt(p[0].trim()) * 60 + Integer.parseInt(p[1].trim()); }
        catch (Exception e) { return 0; }
    }
}
