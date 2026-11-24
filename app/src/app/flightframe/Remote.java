package app.flightframe;

import android.content.Context;
import android.util.Base64;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Remote access without opening the home network: the frame listens on a private ntfy.sh topic.
 * Every message is AES-256-GCM encrypted with a key derived from the PIN and the frame's secret remote id
 * (PBKDF2-SHA256, 150 000 rounds), so the relay only ever sees scrambled data.
 *   phone -> https://ntfy.sh/ffr-<id>-c   {"r":reqId,"t":time,"m":method,"p":path,"b":body}
 *   frame -> https://ntfy.sh/ffr-<id>-s   {"r":reqId,"s":status,"ct":type,"b":base64 body}
 */
final class Remote {
    static final String RELAY = "https://ntfy.sh/";
    static final int ROUNDS = 150000;
    static volatile String status = "off";
    static volatile long lastMsg = 0;
    private static volatile boolean running = false;
    private static byte[] key; private static String keyFor = "";
    private static final Map<String, Long> seen = new LinkedHashMap<String, Long>() {
        protected boolean removeEldestEntry(Map.Entry<String, Long> e) { return size() > 200; }
    };

    static String newId() {
        byte[] b = new byte[12]; new SecureRandom().nextBytes(b);
        return Base64.encodeToString(b, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING).replace('-', 'x').replace('_', 'y');
    }

    static synchronized byte[] key(String pin, String id) throws Exception {
        String k = pin + "|" + id;
        if (k.equals(keyFor) && key != null) return key;
        key = pbkdf2(pin.getBytes("UTF-8"), ("flightframe:" + id).getBytes("UTF-8"), ROUNDS);
        keyFor = k;
        return key;
    }

    /** PBKDF2-HMAC-SHA256, one 32-byte block (works on every Android version). */
    static byte[] pbkdf2(byte[] pw, byte[] salt, int rounds) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(pw, "HmacSHA256"));
        mac.update(salt); mac.update(new byte[]{0, 0, 0, 1});
        byte[] u = mac.doFinal(), out = u.clone();
        for (int i = 1; i < rounds; i++) { u = mac.doFinal(u); for (int j = 0; j < out.length; j++) out[j] ^= u[j]; }
        return out;
    }

    static String encrypt(byte[] k, byte[] plain) throws Exception {
        byte[] iv = new byte[12]; new SecureRandom().nextBytes(iv);
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(k, "AES"), new GCMParameterSpec(128, iv));
        byte[] ct = c.doFinal(plain), all = new byte[12 + ct.length];
        System.arraycopy(iv, 0, all, 0, 12); System.arraycopy(ct, 0, all, 12, ct.length);
        return Base64.encodeToString(all, Base64.NO_WRAP);
    }

    static byte[] decrypt(byte[] k, String b64) throws Exception {
        byte[] all = Base64.decode(b64.trim(), Base64.DEFAULT);
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(k, "AES"), new GCMParameterSpec(128, all, 0, 12));
        return c.doFinal(all, 12, all.length - 12);
    }

    static void start(final Context ctx, final ControlServer api) {
        if (running) return;
        running = true;
        new Thread(new Runnable() { public void run() {
            long backoff = 5000;
            while (true) {
                try {
                    JSONObject s = Settings.get(ctx);
                    JSONObject r = s.optJSONObject("remote");
                    String pin = s.optString("pin", "");
                    if (r == null || !r.optBoolean("enabled") || r.optString("id").length() < 8 || pin.length() < 4) {
                        status = pin.length() < 4 && r != null && r.optBoolean("enabled") ? "needs a PIN" : "off";
                        Thread.sleep(10000); continue;
                    }
                    String id = r.optString("id");
                    byte[] k = key(pin, id);
                    status = "connecting";
                    HttpURLConnection h = (HttpURLConnection) new URL(RELAY + "ffr-" + id + "-c/json?since=" + (System.currentTimeMillis() / 1000 - 60)).openConnection();
                    h.setConnectTimeout(15000); h.setReadTimeout(90000);   // ntfy sends keepalives every ~45 s
                    BufferedReader in = new BufferedReader(new InputStreamReader(h.getInputStream(), "UTF-8"));
                    status = "online"; backoff = 5000;
                    String line;
                    while ((line = in.readLine()) != null) {
                        JSONObject ev = new JSONObject(line);
                        if (!"message".equals(ev.optString("event"))) continue;
                        // settings may have changed (new PIN / remote switched off): reconnect
                        JSONObject s2 = Settings.get(ctx), r2 = s2.optJSONObject("remote");
                        if (r2 == null || !r2.optBoolean("enabled") || !id.equals(r2.optString("id")) || !pin.equals(s2.optString("pin"))) break;
                        handle(ctx, api, k, id, ev.optString("message"));
                    }
                    in.close(); h.disconnect();
                } catch (Exception e) {
                    status = "offline (" + e.getClass().getSimpleName() + ")";
                    try { Thread.sleep(backoff); } catch (InterruptedException ignored) {}
                    backoff = Math.min(backoff * 2, 120000);
                }
            }
        }}, "ff-remote").start();
    }

    private static void handle(Context ctx, ControlServer api, byte[] k, String id, String msg) {
        try {
            JSONObject q = new JSONObject(new String(decrypt(k, msg), "UTF-8"));
            String rid = q.optString("r");
            long t = q.optLong("t");
            if (Math.abs(System.currentTimeMillis() - t) > 180000) return;           // stale or replayed
            synchronized (seen) { if (seen.containsKey(rid)) return; seen.put(rid, t); }
            lastMsg = System.currentTimeMillis();
            String path = q.optString("p"), query = "";
            int i = path.indexOf('?'); if (i >= 0) { query = path.substring(i + 1); path = path.substring(0, i); }
            if (!path.startsWith("/api/") || path.equals("/api/backup") || path.equals("/api/restore") || path.equals("/api/file")) {
                reply(k, id, rid, 403, "application/json", "{\"error\":\"not available remotely\"}".getBytes("UTF-8")); return;
            }
            byte[] body = q.optString("b", "").getBytes("UTF-8");
            Object[] res = api.apiCall(q.optString("m", "GET"), path, query, body);
            reply(k, id, rid, (Integer) res[0], (String) res[1], (byte[]) res[2]);
        } catch (javax.crypto.AEADBadTagException e) {
            MainActivity.log("remote: message with wrong PIN ignored");
        } catch (Exception e) {
            MainActivity.log("remote: " + e);
        }
    }

    private static void reply(byte[] k, String id, String rid, int code, String type, byte[] body) throws Exception {
        JSONObject r = new JSONObject().put("r", rid).put("s", code).put("ct", type).put("b", Base64.encodeToString(body, Base64.NO_WRAP));
        String enc = encrypt(k, r.toString().getBytes("UTF-8"));
        HttpURLConnection h = (HttpURLConnection) new URL(RELAY + "ffr-" + id + "-s").openConnection();
        h.setConnectTimeout(15000); h.setReadTimeout(30000); h.setDoOutput(true);
        byte[] data = enc.getBytes("UTF-8");
        if (data.length > 3800) { h.setRequestMethod("PUT"); h.setRequestProperty("Filename", "r.txt"); }   // big replies go as an attachment
        else h.setRequestMethod("POST");
        OutputStream o = h.getOutputStream(); o.write(data); o.close();
        int sc = h.getResponseCode(); h.disconnect();
        if (sc != 200) MainActivity.log("remote: relay answered " + sc);
    }
}
