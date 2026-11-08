package app.soundheart.player;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Size;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

@CapacitorPlugin(
    name = "Music",
    permissions = {
        @Permission(alias = "audio", strings = { "android.permission.READ_MEDIA_AUDIO" }),
        @Permission(alias = "storage", strings = { "android.permission.READ_EXTERNAL_STORAGE" })
    }
)
public class MusicPlugin extends Plugin {

    private static Long idOf(PluginCall call) {
        Object v = call.getData().opt("id");
        if (v instanceof Number) {
            return ((Number) v).longValue();
        }
        if (v instanceof String) {
            try {
                return Long.parseLong((String) v);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private String permAlias() {
        return Build.VERSION.SDK_INT >= 33 ? "audio" : "storage";
    }

    @PluginMethod
    public void list(PluginCall call) {
        if (getPermissionState(permAlias()) != PermissionState.GRANTED) {
            requestPermissionForAlias(permAlias(), call, "permDone");
            return;
        }
        doList(call);
    }

    @PermissionCallback
    private void permDone(PluginCall call) {
        if (getPermissionState(permAlias()) == PermissionState.GRANTED) {
            doList(call);
        } else {
            call.reject("denied");
        }
    }

    private void doList(PluginCall call) {
        try {
            JSArray items = new JSArray();
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String[] proj = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.ALBUM_ID
            };
            String sel = MediaStore.Audio.Media.IS_RINGTONE + "=0 AND "
                + MediaStore.Audio.Media.IS_NOTIFICATION + "=0 AND "
                + MediaStore.Audio.Media.IS_ALARM + "=0 AND "
                + MediaStore.Audio.Media.DURATION + ">=20000";
            try (Cursor c = getContext().getContentResolver().query(uri, proj, sel, null, MediaStore.Audio.Media.DATE_ADDED + " DESC")) {
                if (c != null) {
                    while (c.moveToNext()) {
                        JSObject o = new JSObject();
                        o.put("id", c.getLong(0));
                        o.put("name", c.getString(1));
                        o.put("title", c.getString(2));
                        String artist = c.getString(3);
                        o.put("artist", (artist == null || "<unknown>".equals(artist)) ? "" : artist);
                        o.put("dur", c.getLong(4));
                        o.put("size", c.getLong(5));
                        o.put("albumId", c.getLong(6));
                        items.put(o);
                    }
                }
            }
            JSObject res = new JSObject();
            res.put("items", items);
            call.resolve(res);
        } catch (Exception e) {
            call.reject("fail: " + e.getMessage());
        }
    }

    @PluginMethod
    public void copy(PluginCall call) {
        Long id = idOf(call);
        if (id == null) {
            call.reject("no id");
            return;
        }
        try {
            Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
            ContentResolver cr = getContext().getContentResolver();
            File dir = new File(getContext().getCacheDir(), "incoming");
            dir.mkdirs();
            File out = new File(dir, System.currentTimeMillis() + "_" + id + ".mp3");
            try (InputStream in = cr.openInputStream(uri); FileOutputStream os = new FileOutputStream(out)) {
                if (in == null) {
                    call.reject("open failed");
                    return;
                }
                byte[] buf = new byte[65536];
                int n;
                while ((n = in.read(buf)) > 0) {
                    os.write(buf, 0, n);
                }
            }
            JSObject res = new JSObject();
            res.put("path", out.getAbsolutePath());
            call.resolve(res);
        } catch (Exception e) {
            call.reject("copy failed: " + e.getMessage());
        }
    }

    @PluginMethod
    public void thumb(PluginCall call) {
        Long id = idOf(call);
        JSObject res = new JSObject();
        if (id == null || Build.VERSION.SDK_INT < 29) {
            call.resolve(res);
            return;
        }
        try {
            Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
            Bitmap bmp = getContext().getContentResolver().loadThumbnail(uri, new Size(160, 160), null);
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, bo);
            res.put("data", "data:image/jpeg;base64," + Base64.encodeToString(bo.toByteArray(), Base64.NO_WRAP));
        } catch (Exception ignored) {
            // no cover for this song
        }
        call.resolve(res);
    }
}
