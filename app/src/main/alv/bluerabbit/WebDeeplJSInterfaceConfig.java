package alv.bluerabbit.web;

import android.util.Log;
import android.webkit.JavascriptInterface;
import java.lang.ref.WeakReference;

public class WebDeeplJSInterfaceConfig {
    private static final String TAG = "DeepLJS-Interface";
    public interface Listener {
        // lifecycle / readiness
        default void onDeepLReady() {}

        // streaming teks terjemahan (output)
        default void onOutputTextChanged(String text) {}

        // Stream perubahan pilihan bahasa dl-selected-lang (label & kode)
        default void onSourceLangChanged(String codeOrLabel) {}
        default void onTargetLangChanged(String codeOrLabel) {}

        // logging JS
        default void onJsLog(String msg) {}

        default void onElementFound(String elementType, boolean found, String details) {}
        default void onOperationResult(String operation, boolean success, String message) {}
        default void onInputTextSet(boolean success, String actualText) {}
        default void onOutputTextRetrieved(boolean success, String text) {}
        default void onLanguagePickResult(String type, String requestedLang, boolean success, String actualLang) {}

        // === Observasi atribut <body> & dialog & daftar bahasa ===
        default void onBodyAttrs(String styleValue, String dataScrollLockedValue) {}
        
        /** which: "source" | "target"; open: true jika dialog ada/muncul */
        default void onDialogStateChanged(String which, boolean open) {}
        
        /** which: "source" | "target"; json: string JSON array [{code,label,selected}] */
        default void onLangList(String which, String json) {}
    }

    private WeakReference<Listener> listenerRef;

    public WebDeeplJSInterfaceConfig(Listener listener) {
        this.listenerRef = new WeakReference<>(listener);
    }

    public void setListener(Listener listener) {
        this.listenerRef = new WeakReference<>(listener);
    }

    private Listener getL() {
        return listenerRef != null ? listenerRef.get() : null;
    }

    @JavascriptInterface
    public void onDeepLReady() {
        Listener l = getL();
        if (l != null) l.onDeepLReady();
    }

    @JavascriptInterface
    public void onOutputTextChanged(String text) {
        Listener l = getL();
        Log.i(TAG, "onOutputTextChanged: " + text);
        if (l != null) l.onOutputTextChanged(text);
    }

    @JavascriptInterface
    public void onSourceLangChanged(String codeOrLabel) {
        Listener l = getL();
        if (l != null) l.onSourceLangChanged(codeOrLabel);
    }

    @JavascriptInterface
    public void onTargetLangChanged(String codeOrLabel) {
        Listener l = getL();
        if (l != null) l.onTargetLangChanged(codeOrLabel);
    }

    @JavascriptInterface
    public void onLog(String msg) {
        Listener l = getL();
        if (l != null) l.onJsLog(msg);
    }

    // === DEBUG ===
    @JavascriptInterface
    public void onElementFound(String elementType, boolean found, String details) {
        Listener l = getL();
        if (l != null) l.onElementFound(elementType, found, details);
    }

    @JavascriptInterface
    public void onOperationResult(String operation, boolean success, String message) {
        Listener l = getL();
        if (l != null) l.onOperationResult(operation, success, message);
    }

    @JavascriptInterface
    public void onInputTextSet(boolean success, String actualText) {
        Listener l = getL();
        if (l != null) l.onInputTextSet(success, actualText);
    }

    @JavascriptInterface
    public void onOutputTextRetrieved(boolean success, String text) {
        Listener l = getL();
        if (l != null) l.onOutputTextRetrieved(success, text);
    }

    @JavascriptInterface
    public void onLanguagePickResult(String type, String requestedLang, boolean success, String actualLang) {
        Listener l = getL();
        if (l != null) l.onLanguagePickResult(type, requestedLang, success, actualLang);
    }

    // === BODY ATTRS, DIALOG STATE, LANG LIST ===
    @JavascriptInterface
    public void onBodyAttrs(String styleValue, String dataScrollLockedValue) {
        Listener l = getL();
        if (l != null) l.onBodyAttrs(styleValue, dataScrollLockedValue);
    }

    @JavascriptInterface
    public void onDialogStateChanged(String which, boolean open) {
        Listener l = getL();
        if (l != null) l.onDialogStateChanged(which, open);
    }

    @JavascriptInterface
    public void onLangList(String which, String json) {
        Listener l = getL();
        if (l != null) l.onLangList(which, json);
    }
}
