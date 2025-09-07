package alv.bluerabbit.nlp.punctuation;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.SystemClock;
import android.util.Log;

// Available (works):
import com.k2fsa.sherpa.onnx.OfflinePunctuation;
import com.k2fsa.sherpa.onnx.OfflinePunctuationConfig;
import com.k2fsa.sherpa.onnx.OfflinePunctuationModelConfig;

// Not found in my Android project (fail to import):
import com.k2fsa.sherpa.onnx.OnlinePunctuation;
import com.k2fsa.sherpa.onnx.OnlinePunctuationConfig;
import com.k2fsa.sherpa.onnx.OnlinePunctuationModelConfig;


public final class PunctuationEngine {
    private static final String TAG = "BR-PUNCT";
  
    private static final String MODEL_DIR  = "punctuation/sherpa-onnx-punct-ct-transformer-zh-en-vocab272727-2024-04-12-int8";
    private static final String MODEL_FILE = MODEL_DIR + "/model.int8.onnx";

    private static PunctuationEngine sInstance;
    private OfflinePunctuation ctPunctZHEN;
    private volatile boolean initialized = false;

    private PunctuationEngine() {}

    public static synchronized PunctuationEngine getInstance() {
        if (sInstance == null) sInstance = new PunctuationEngine();
        return sInstance;
    }

    public synchronized void init(Context ctx) {
        if (initialized) return;
        try {
            AssetManager am = ctx.getAssets();

            OfflinePunctuationModelConfig mcCT = new OfflinePunctuationModelConfig();
            // Model ct-transformer zh-en punctuation
            mcCT.setCtTransformer(MODEL_FILE);
            mcCT.setNumThreads(1);
            mcCT.setProvider("cpu");
            mcCT.setDebug(false);
          
            OfflinePunctuationConfig cfgCT = new OfflinePunctuationConfig(mcCT);
            cfgCT.setModel(mcCT);

            ctPunctZHEN = new OfflinePunctuation(am, cfgCT);
            initialized = true;
            Log.i(TAG, "Punctuation initialized (" + MODEL_FILE + ")");
        } catch (Throwable t) {
            Log.e(TAG, "Init punctuation failed: " + t.getMessage(), t);
        }
    }

    public boolean isReady() { return initialized && ctPunctZHEN != null; }

    public String punctuate(String text) {
        if (!isReady() || text == null || text.trim().isEmpty()) return text;
        long t0 = SystemClock.uptimeMillis();
        String out = ctPunctZHEN.addPunctuation(text);
        return out;
    }

    public synchronized void close() {
        try {
            if (ctPunctZHEN != null) ctPunctZHEN.release();
        } catch (Throwable ignore) {}
        ctPunctZHEN = null;
        initialized = false;
    }
}
