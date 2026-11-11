package app.flightframe;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** Full-screen display: hosts the Flight Frame page in a WebView and keeps it running. */
public class MainActivity extends Activity {
    static volatile MainActivity instance;
    static volatile long lastHeartbeat = System.currentTimeMillis();
    static volatile String lastReport = "{}";
    static volatile long lastReportAt = 0;
    static volatile long pageLoadedAt = 0;
    static volatile int reloads = 0;
    static final StringBuilder log = new StringBuilder();

    private FrameLayout root;
    private WebView web;
    private View blackout;
    private TextView banner;
    private final Handler ui = new Handler(Looper.getMainLooper());

    static void log(String s) {
        synchronized (log) {
            log.append(android.text.format.DateFormat.format("HH:mm:ss ", System.currentTimeMillis())).append(s).append('\n');
            if (log.length() > 20000) log.delete(0, log.length() - 15000);
        }
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        instance = this;
        Web.install(this);
        Window w = getWindow();
        w.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= 27) { setShowWhenLocked(true); setTurnScreenOn(true); }

        root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        if (Build.VERSION.SDK_INT >= 26) root.setDefaultFocusHighlightEnabled(false);
        setContentView(root);
        buildWebView();
        blackout = new View(this);
        blackout.setBackgroundColor(Color.BLACK);
        blackout.setVisibility(View.GONE);
        root.addView(blackout, new FrameLayout.LayoutParams(-1, -1));
        banner = new TextView(this);
        banner.setTextColor(0xFFFFB238);
        banner.setTextSize(28);
        banner.setPadding(40, 30, 40, 30);
        banner.setBackgroundColor(0xE0101418);
        banner.setVisibility(View.GONE);
        FrameLayout.LayoutParams bl = new FrameLayout.LayoutParams(-2, -2);
        bl.gravity = android.view.Gravity.CENTER;
        root.addView(banner, bl);

        immersive();
        applyAppSettings();
        startService(new Intent(this, KioskService.class));
        ui.postDelayed(watchdog, 60000);
        log("display started");
    }

    private void buildWebView() {
        if (web != null) { root.removeView(web); web.destroy(); }
        web = new WebView(this);
        web.setBackgroundColor(Color.BLACK);
        if (Build.VERSION.SDK_INT >= 26) web.setDefaultFocusHighlightEnabled(false);
        web.setFocusable(false);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setAllowFileAccessFromFileURLs(true);
        s.setAllowUniversalAccessFromFileURLs(true);   // the page talks to flight APIs directly
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        if (Build.VERSION.SDK_INT >= 21) s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setUserAgentString(s.getUserAgentString() + " FlightFrame/" + BuildInfo.VERSION);
        web.addJavascriptInterface(new Bridge(), "FFApp");
        web.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onConsoleMessage(ConsoleMessage m) {
                if (m.messageLevel() == ConsoleMessage.MessageLevel.ERROR) log("page: " + m.message() + " (" + m.lineNumber() + ")");
                return true;
            }
        });
        web.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView v, String url) { pageLoadedAt = System.currentTimeMillis(); lastHeartbeat = pageLoadedAt; }
            @Override public boolean shouldOverrideUrlLoading(WebView v, String url) { return !url.startsWith("file:"); }
            @Override public void onReceivedError(WebView v, WebResourceRequest r, WebResourceError e) {
                if (Build.VERSION.SDK_INT >= 23 && r.isForMainFrame()) { log("load error " + e.getDescription()); ui.postDelayed(new Runnable() { public void run() { reload("load error"); } }, 15000); }
            }
            @Override public void onReceivedSslError(WebView v, SslErrorHandler h, SslError e) { h.cancel(); }
            @Override public boolean onRenderProcessGone(WebView v, RenderProcessGoneDetail d) {
                log("display engine crashed, rebuilding");
                ui.post(new Runnable() { public void run() { buildWebView(); } });
                return true;
            }
        });
        root.addView(web, 0, new FrameLayout.LayoutParams(-1, -1));
        load();
    }

    private void load() {
        File idx = new File(Web.dir(this), "index.html");
        web.loadUrl("file://" + idx.getAbsolutePath());
        lastHeartbeat = System.currentTimeMillis();
    }

    void reload(String why) {
        reloads++;
        log("reload: " + why);
        if (web != null) { web.clearCache(false); load(); }
    }

    /** If the page stops sending heartbeats for 3 minutes, reload it; after 3 tries rebuild the WebView. */
    private final Runnable watchdog = new Runnable() {
        int strikes = 0;
        public void run() {
            long quiet = System.currentTimeMillis() - lastHeartbeat;
            if (quiet > 180000 && !Screen.isOff) {
                strikes++;
                if (strikes >= 3) { log("watchdog: rebuilding display"); buildWebView(); strikes = 0; }
                else reload("watchdog (no heartbeat " + quiet / 1000 + "s)");
            } else if (quiet < 60000) strikes = 0;
            immersive();
            ui.postDelayed(this, 60000);
        }
    };

    void immersive() {
        View d = getWindow().getDecorView();
        d.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    /** Settings the app applies itself (no page reload). */
    void applyAppSettings() {
        JSONObject s = Settings.get(this);
        int b = s.optInt("brightness", -1);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = (b < 0 || Screen.isOff) ? (Screen.isOff && Screen.softOff ? 0.01f : WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE) : Math.max(0.01f, b / 100f);
        getWindow().setAttributes(lp);
        String rot = s.optString("rotation", "auto");
        int o = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        if (s.optBoolean("forceOrientation", false)) {
            if (rot.equals("P")) o = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            else if (rot.equals("L")) o = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            else if (rot.equals("P2")) o = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
            else if (rot.equals("L2")) o = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
        }
        setRequestedOrientation(o);
    }

    void setBlackout(boolean on) {
        blackout.setVisibility(on ? View.VISIBLE : View.GONE);
        applyAppSettings();
    }

    void showBanner(final String text, int ms) {
        banner.setText(text);
        banner.setVisibility(View.VISIBLE);
        ui.postDelayed(new Runnable() { public void run() { banner.setVisibility(View.GONE); } }, ms);
    }

    /** Called from other threads: run on the UI thread and wait. */
    static void onUi(final Runnable r) {
        final MainActivity a = instance;
        if (a == null) return;
        final CountDownLatch l = new CountDownLatch(1);
        a.ui.post(new Runnable() { public void run() { try { r.run(); } finally { l.countDown(); } } });
        try { l.await(5, TimeUnit.SECONDS); } catch (InterruptedException ignored) {}
    }

    static byte[] screenshot(final int maxW) {
        final byte[][] out = new byte[1][];
        onUi(new Runnable() { public void run() {
            MainActivity a = instance; if (a == null) return;
            View v = a.root; int w = v.getWidth(), h = v.getHeight(); if (w == 0 || h == 0) return;
            float sc = Math.min(1f, maxW / (float) w);
            Bitmap bm = Bitmap.createBitmap(Math.round(w * sc), Math.round(h * sc), Bitmap.Config.RGB_565);
            Canvas c = new Canvas(bm); c.scale(sc, sc); v.draw(c);
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 80, bo); bm.recycle();
            out[0] = bo.toByteArray();
        }});
        return out[0];
    }

    @Override protected void onResume() { super.onResume(); instance = this; immersive(); }
    @Override public void onWindowFocusChanged(boolean f) { super.onWindowFocusChanged(f); if (f) immersive(); }
    @Override public void onBackPressed() { /* kiosk: ignore */ }
    @Override public boolean onKeyDown(int code, KeyEvent e) {
        if (code == KeyEvent.KEYCODE_BACK) return true;
        return super.onKeyDown(code, e);
    }
    @Override protected void onDestroy() { if (instance == this) instance = null; super.onDestroy(); }

    /** JavaScript bridge: window.FFApp in the page. */
    final class Bridge {
        @JavascriptInterface public String getSettings() {
            try { return Settings.get(MainActivity.this).toString(); } catch (Exception e) { return "{}"; }
        }
        @JavascriptInterface public void heartbeat() { lastHeartbeat = System.currentTimeMillis(); }
        @JavascriptInterface public void report(String json) { lastReport = json; lastReportAt = System.currentTimeMillis(); lastHeartbeat = lastReportAt; }
        @JavascriptInterface public String version() { return BuildInfo.VERSION; }
        @JavascriptInterface public void log(String s) { MainActivity.log("page: " + s); }
    }
}
