package usage.ywb.pluginloadapk;

import android.app.Application;
import android.content.Context;

/**
 * @author yuwenbo
 * @version [ V.2.9.3  2020/9/29 ]
 */
public class MyApplication extends Application {

    private static Application application;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        application = this;
        PluginManager.getInstance().init(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static Application getApplication() {
        return application;
    }

}
