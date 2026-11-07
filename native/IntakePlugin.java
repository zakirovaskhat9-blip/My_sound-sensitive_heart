package app.soundheart.player;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;

@CapacitorPlugin(name = "Intake")
public class IntakePlugin extends Plugin {
    static final List<String> pending = new ArrayList<>();
    static boolean play = false;

    @PluginMethod
    public void take(PluginCall call) {
        JSObject res = new JSObject();
        synchronized (pending) {
            res.put("files", new JSONArray(pending));
            res.put("play", play);
            pending.clear();
            play = false;
        }
        call.resolve(res);
    }
}
