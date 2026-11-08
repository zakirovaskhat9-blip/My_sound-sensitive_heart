package app.soundheart.player;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import com.getcapacitor.BridgeActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BridgeActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(IntakePlugin.class);
        registerPlugin(MusicPlugin.class);
        super.onCreate(savedInstanceState);
        cleanOld();
        handle(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handle(intent);
    }

    private void handle(Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        final List<Uri> uris = new ArrayList<>();
        final boolean view = Intent.ACTION_VIEW.equals(action);
        if (view && intent.getData() != null) {
            uris.add(intent.getData());
        } else if (Intent.ACTION_SEND.equals(action)) {
            Uri u = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (u != null) uris.add(u);
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            ArrayList<Uri> l = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            if (l != null) uris.addAll(l);
        }
        if (uris.isEmpty()) return;
        intent.setAction(Intent.ACTION_MAIN);
        new Thread(() -> {
            for (Uri uri : uris) {
                String path = copy(uri);
                if (path != null) {
                    synchronized (IntakePlugin.pending) {
                        IntakePlugin.pending.add(path);
                        if (view) IntakePlugin.play = true;
                    }
                }
            }
            runOnUiThread(() -> {
                if (getBridge() != null && getBridge().getWebView() != null) {
                    getBridge().getWebView().evaluateJavascript("window.onNativeIntake&&window.onNativeIntake()", null);
                }
            });
        }).start();
    }

    private String copy(Uri uri) {
        try {
            String name = null;
            try (Cursor c = getContentResolver().query(uri, null, null, null, null)) {
                if (c != null && c.moveToFirst()) {
                    int i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (i >= 0) name = c.getString(i);
                }
            } catch (Exception ignored) {
            }
            if (name == null) name = uri.getLastPathSegment();
            if (name == null) name = "audio.mp3";
            name = name.replaceAll("[\\\\/:*?\"<>|]", "_");
            File dir = new File(getCacheDir(), "incoming");
            dir.mkdirs();
            File out = new File(dir, System.currentTimeMillis() + "_" + name);
            try (InputStream in = getContentResolver().openInputStream(uri);
                 FileOutputStream os = new FileOutputStream(out)) {
                if (in == null) return null;
                byte[] buf = new byte[65536];
                int n;
                while ((n = in.read(buf)) > 0) os.write(buf, 0, n);
            }
            return out.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }

    private void cleanOld() {
        File[] files = new File(getCacheDir(), "incoming").listFiles();
        if (files == null) return;
        long limit = System.currentTimeMillis() - 10 * 60 * 1000;
        for (File f : files) {
            if (f.lastModified() < limit) f.delete();
        }
    }
}
