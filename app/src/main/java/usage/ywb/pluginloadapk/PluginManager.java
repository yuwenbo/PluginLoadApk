package usage.ywb.pluginloadapk;

import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;


/**
 * @author yuwenbo
 * @version [ V.2.9.6  2020/11/23 ]
 */
public class PluginManager {

    public static final String TARGET_INTENT = "target_intent";

    private volatile static PluginManager pluginManager;

    private Map<String, LoadedPlugin> mPluginMap;

    private Context hostContext;
    private Instrumentation hostInstrumentation;


    public static PluginManager getInstance() {
        if (pluginManager == null) {
            synchronized (PluginManager.class) {
                if (pluginManager == null) {
                    pluginManager = new PluginManager();
                }
            }
        }
        return pluginManager;
    }

    public void init(Context base) {
        this.hostContext = base;
        mPluginMap = new HashMap<>();
        try {
            hookInstrumentation();
//            hookPackageManager();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Context getHostContext() {
        return hostContext;
    }

    private void hookInstrumentation() throws Exception {
        Class<?> contextImplClass = Class.forName("android.app.ContextImpl");
        Object mMainThread = ReflectorUtil.getField(contextImplClass, hostContext, "mMainThread");
        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        hostInstrumentation = (Instrumentation) ReflectorUtil.getField(activityThreadClass, mMainThread, "mInstrumentation");
        ReflectorUtil.setField(activityThreadClass, mMainThread, "mInstrumentation", new InstrumentationProxy(hostInstrumentation));
    }

    @Deprecated
    private void hookPackageManager() throws Exception {
        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        Object currentActivityThread = ReflectorUtil.getField(activityThreadClass, null, "sCurrentActivityThread");
        Field sPackageManagerFiled = ReflectorUtil.getField(activityThreadClass, "sPackageManager");
        Object sPackageManager = sPackageManagerFiled.get(null);
        Object proxyInstance = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                sPackageManager.getClass().getInterfaces(),
                new PackageManagerHandler(sPackageManager));
        sPackageManagerFiled.set(currentActivityThread, proxyInstance);
    }

    public LoadedPlugin loadPlugin(File file) {
        //插件资源独立，该resource只能访问插件自己的资源
        PackageManager mPm = hostContext.getPackageManager();
        PackageInfo mInfo = mPm.getPackageArchiveInfo(file.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
        String key;
        if (mInfo != null && !TextUtils.isEmpty(mInfo.packageName)) {
            key = mInfo.packageName;
        } else {
            key = file.getAbsolutePath();
        }
        if (!mPluginMap.containsKey(key)) {
            LoadedPlugin plugin = new LoadedPlugin(hostContext, file);
            plugin.setPackageInfo(mInfo);
            mPluginMap.put(key, plugin);
            return plugin;
        } else {
            return mPluginMap.get(key);
        }
    }

    public LoadedPlugin getPlugin(String packageName) {
        LoadedPlugin plugin = mPluginMap.get(packageName);
        return plugin;
    }

    public Instrumentation getHostInstrumentation() {
        return hostInstrumentation;
    }

    /**
     * 解决AppCompatActivity无法hook的问题
     */
    @Deprecated
    private class PackageManagerHandler implements InvocationHandler {
        private Object IPackageManager;

        public PackageManagerHandler(Object IPackageManager) {
            this.IPackageManager = IPackageManager;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("getActivityInfo".equals(method.getName())) {
                args[0] = new ComponentName(hostContext.getPackageName(), ProxyActivity.class.getName());
            }
            return method.invoke(IPackageManager, args);
        }
    }

}

