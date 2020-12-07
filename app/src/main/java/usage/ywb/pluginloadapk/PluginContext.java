package usage.ywb.pluginloadapk;

import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.didi.virtualapk.internal.PluginContentResolver;


/**
 * @author yuwenbo
 * @version [ V.2.9.6  2020/12/7 ]
 */
public class PluginContext extends ContextWrapper {
    private final LoadedPlugin mPlugin;

    public PluginContext(LoadedPlugin plugin) {
        super(PluginManager.getInstance().getHostContext());
        this.mPlugin = plugin;
    }

    public PluginContext(LoadedPlugin plugin, Context base) {
        super(base);
        this.mPlugin = plugin;
    }

    @Override
    public Context getApplicationContext() {
        return this.mPlugin.getApplication();
    }


    private Context getHostContext() {
        return getBaseContext();
    }

    @Override
    public ContentResolver getContentResolver() {
        return new PluginContentResolver(getHostContext());
    }

    @Override
    public ClassLoader getClassLoader() {
        return this.mPlugin.getClassLoader();
    }

    @Override
    public Object getSystemService(String name) {
        if (name.equals(Context.CLIPBOARD_SERVICE)) {
            return getHostContext().getSystemService(name);
        } else if (name.equals(Context.NOTIFICATION_SERVICE)) {
            return getHostContext().getSystemService(name);
        }

        return super.getSystemService(name);
    }

    @Override
    public Resources getResources() {
        return this.mPlugin.getResource();
    }

    @Override
    public AssetManager getAssets() {
        return this.mPlugin.getResource().getAssets();
    }

    @Override
    public Resources.Theme getTheme() {
        return this.mPlugin.getResource().newTheme();
    }

}
