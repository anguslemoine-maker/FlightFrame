package app.flightframe;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/** The display's web files live in the app's folder so they can be updated without reinstalling. */
final class Web {
    /** Program files: always replaced by a newer app version. Everything else (map, settings.js) is only added if missing. */
    static final String[] CODE = {"index.html", "planes.js", "satellite.min.js"};

    static File dir(Context c) {
        File d = c.getExternalFilesDir("www");
        if (d == null) d = new File(c.getFilesDir(), "www");
        d.mkdirs();
        return d;
    }

    static void install(Context c) {
        File d = dir(c);
        File stamp = new File(d, ".version");
        String have = read(stamp);
        boolean upgrade = !BuildInfo.VERSION.equals(have);
        try {
            AssetManager am = c.getAssets();
            String[] files = am.list("www");
            if (files == null) return;
            for (String f : files) {
                File out = new File(d, f);
                boolean code = false;
                for (String x : CODE) if (x.equals(f)) code = true;
                if (out.exists() && !(upgrade && code)) continue;
                copy(am.open("www/" + f), out);
            }
            write(stamp, BuildInfo.VERSION);
        } catch (Exception e) { MainActivity.log("install web files: " + e); }
    }

    /** Download the program files from updateBase (e.g. the GitHub repo's display folder). */
    static String update(Context c, String base) throws Exception {
        if (base == null || base.length() == 0) base = BuildInfo.UPDATE_BASE;
        if (!base.endsWith("/")) base += "/";
        File d = dir(c);
        StringBuilder r = new StringBuilder();
        for (String f : CODE) {
            File tmp = new File(d, f + ".dl");
            HttpURLConnection h = (HttpURLConnection) new URL(base + f + "?t=" + System.currentTimeMillis()).openConnection();
            h.setConnectTimeout(15000); h.setReadTimeout(30000);
            if (h.getResponseCode() != 200) throw new Exception(f + ": HTTP " + h.getResponseCode());
            copy(h.getInputStream(), tmp);
            if (tmp.length() < 1000) throw new Exception(f + ": file too small");
            r.append(f).append(' ').append(tmp.length()).append(" bytes\n");
        }
        for (String f : CODE) new File(d, f + ".dl").renameTo(new File(d, f));
        return r.toString();
    }

    static void copy(InputStream in, File out) throws Exception {
        File tmp = new File(out.getPath() + ".part");
        OutputStream o = new FileOutputStream(tmp);
        byte[] b = new byte[16384]; int n;
        while ((n = in.read(b)) > 0) o.write(b, 0, n);
        o.close(); in.close();
        if (!tmp.renameTo(out)) { out.delete(); tmp.renameTo(out); }
    }

    static String read(File f) {
        try { InputStream in = new java.io.FileInputStream(f); byte[] b = new byte[(int) f.length()]; int n = in.read(b); in.close(); return new String(b, 0, Math.max(0, n), "UTF-8"); }
        catch (Exception e) { return ""; }
    }

    static void write(File f, String s) {
        try { OutputStream o = new FileOutputStream(f); o.write(s.getBytes("UTF-8")); o.close(); } catch (Exception ignored) {}
    }
}
