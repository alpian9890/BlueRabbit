package alv.bluerabbit.web;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.ConsoleMessage;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

import java.util.ArrayDeque;
import java.util.Deque;

public class WebDeepL {

    private static final String TAG = "WebDeepL";
    public static final String BRIDGE_NAME = "BR_JSI"; // BlueRabbit JavascriptInterface 
    public static final String BRIDGE_VERSION = "2025-09-13_BlueRabbit-r2";
    private static final String DEEPL_URL = "https://www.deepl.com/translator";

    public interface Listener extends WebDeeplJSInterfaceConfig.Listener {
        default void onPageTitle(String title) {}
        default void onPageStarted(String url) {}
        default void onPageFinished(String url) {}
        default void onConsole(ConsoleMessage cm) {}
    }

    private final Context appCtx;
    private final WebView webView;
    private final Handler main;
    private final WebDeeplJSInterfaceConfig jsBridge;
    private boolean injected = false;
    private boolean pageFinished = false;
    private final Deque<String> pendingJs = new ArrayDeque<>();
    private Listener listener;

    public WebDeepL(@NonNull Context context, @NonNull WebView webView, @NonNull Listener listener) {
        this.appCtx = context.getApplicationContext();
        this.webView = webView;
        this.listener = listener;
        this.main = new Handler(Looper.getMainLooper());
        this.jsBridge = new WebDeeplJSInterfaceConfig(listener);

        new WebDeeplConfig().apply(webView, new WebDeeplConfig.PageEvents() {
            @Override public void onPageStarted(WebView view, String url) {
                pageFinished = false;
                injected = false;
                if (listener != null) listener.onPageStarted(url);
            }
            @Override public void onPageFinished(WebView view, String url) {
                pageFinished = true;
                if (listener != null) listener.onPageFinished(url);
                injectBridgeAndObservers();
            }
            @Override public void onReceivedTitle(WebView view, String title) {
                if (listener != null) listener.onPageTitle(title);
            }
            @Override public void onConsoleMessage(ConsoleMessage cm) {
                if (listener != null) listener.onConsole(cm);
            }
        });

        webView.addJavascriptInterface(jsBridge, BRIDGE_NAME);
    }

    /** Load DeepL translator page */
    @MainThread
    public void load() {
        runOnUi(() -> webView.loadUrl(DEEPL_URL));
    }

    /** Hancurkan WebView (hindari leak) */
    public void destroy() {
        runOnUi(() -> {
            try {
                webView.removeJavascriptInterface(BRIDGE_NAME);
            } catch (Throwable ignored) {}
            webView.stopLoading();
            webView.loadUrl("about:blank");
            webView.clearHistory();
            webView.removeAllViews();
            webView.destroy();
        });
    }

    /** Resume/Pause timers */
    public void onResume() { runOnUi(webView::onResume); }
    public void onPause()  { runOnUi(webView::onPause);  }

    /** Set teks input (stream ke kolom sumber) */
    public void setInputText(@NonNull String text, ValueCallback<String> callback) {
        String js = "Kode Javascript untuk set teks ke kolom teks sumber bahasa di halaman web deepl";
        eval(js, res -> {
            Log.i(TAG, "setInputText result: " + res);
            if (callback != null) callback.onReceiveValue(res);
        });
    }

    public void setInputText(@NonNull String text) { setInputText(text, null); }

    /** Ambil output sekali. Disarankan pakai streaming onOutputTextChanged(). */
    public void getOutputOnce(@NonNull ValueCallback<String> cb) {
        String js = "Kode Javascript untuk mengambil output hasil terjemahan sekali saja";
        eval(js, res -> {
            Log.i(TAG, "getOutputOnce: " + res);
            if (cb != null) cb.onReceiveValue(res);
        });
    }

    /** Klik tombol data-testid="translator-source-lang-btn" */
    public void openSourceLangPicker(ValueCallback<String> cb) {
        String js = "Kode Javascript untuk mengklik button data-testid='translator-source-lang-btn'.";
        eval(js, res -> { if (cb != null) cb.onReceiveValue(res); });
    }
    public void openSourceLangPicker() { openSourceLangPicker(null); }

    /** Klik tombol data-testid="translator-target-lang-btn" */
    public void openTargetLangPicker(ValueCallback<String> cb) {
        String js = "Kode Javascript untuk mengklik button data-testid='translator-target-lang-btn'."; 
        eval(js, res -> { if (cb != null) cb.onReceiveValue(res); });
    }
    public void openTargetLangPicker() { openTargetLangPicker(null); }

    /** Klik pada tombol yang ada didalam list bahasa untuk source bahasa */
    public void pickSourceLang(String codeOrName, ValueCallback<String> cb) {
        String js = "Kode Javascript untuk melakukan klik pada tombol yang ada didalam list bahasa berdasarkan kode bahasa yang ada pada button dengan attribute data-testid='translator-lang-option-(kode)' untuk memilih bahasa sumber";
        eval(js, res -> { if (cb != null) cb.onReceiveValue(res); });
    }
    public void pickSourceLang(String codeOrName) { pickSourceLang(codeOrName, null); }

    /** Klik pada tombol yang ada didalam list bahasa untuk target bahasa */
    public void pickTargetLang(String codeOrName, ValueCallback<String> cb) {
        String js = "Kode Javascript untuk melakukan klik pada tombol yang ada didalam list bahasa berdasarkan kode bahasa yang ada pada button dengan attribute data-testid='translator-lang-option-(kode)' untuk memilih bahasa target";
        eval(js, res -> { if (cb != null) cb.onReceiveValue(res); });
    }
    public void pickTargetLang(String codeOrName) { pickTargetLang(codeOrName, null); }

    /** Cek status elemen sekali. Untuk mengecek apakah elemen berhasil ditemukan atau tidak. */
    public void checkElementsStatus(ValueCallback<String> cb) {
        String js = "Kode Javascript untuk mengecek elemen-elemen yang saya inginkan apakah berhasil ditemukan atau tidak. ";
        eval(js, res -> { if (cb != null) cb.onReceiveValue(res); });
    }

    /** ===== Injeksi JS & Observer ===== */
    private void injectBridgeAndObservers() {
        if (!pageFinished || injected) return;
        Log.i(TAG, BRIDGE_VERSION);
        final String js ="Ini adalah inti kode Javascript untuk memasang jembatan API antara aplikasi saya dan halaman web deepl supaya bisa berkomunikasi dengan sinkron dan streaming.";

        runOnUi(() -> {
            if (Build.VERSION.SDK_INT >= 19) {
                webView.evaluateJavascript(js, res -> {
                    injected = true;
                    flushPendingJs();
                    if (listener != null) listener.onOperationResult("injectBridge", true, String.valueOf(res));
                });
            } else {
                webView.loadUrl("javascript:" + js);
                injected = true;
                flushPendingJs();
                if (listener != null) listener.onOperationResult("injectBridge", true, "legacy loadUrl");
            }
        });
    }

    /** ===== Helpers eksekusi JS ===== */
    private void eval(String js, ValueCallback<String> cb) {
        runOnUi(() -> {
            if (!pageFinished || !injected) {
                pendingJs.addLast(js);
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                webView.evaluateJavascript(js, cb);
            } else {
                webView.loadUrl("javascript:" + js);
                if (cb != null) cb.onReceiveValue(null);
            }
        });
    }

    private void flushPendingJs() {
        runOnUi(() -> {
            if (!pageFinished || !injected) return;
            while (!pendingJs.isEmpty()) {
                String js = pendingJs.pollFirst();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView.evaluateJavascript(js, null);
                } else {
                    webView.loadUrl("javascript:" + js);
                }
            }
        });
    }

    private void runOnUi(Runnable r) {
        if (Looper.myLooper() == Looper.getMainLooper()) r.run();
        else main.post(r);
    }

    }

    public WebView getWebView() { return webView; }
    public void setListener(Listener l) {
        this.listener = l;
        this.jsBridge.setListener(l);
    }
}
