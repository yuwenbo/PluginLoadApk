package usage.ywb.pluginloadapk;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;


import com.didi.virtualapk.internal.utils.PluginUtil;

import java.io.File;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

/**
 * @author yuwenbo
 * @version [ V.2.9.6  2020/12/4 ]
 */
public class LoadedPlugin {

    private Context mHostContext;

    private Application mApplication;
    private Context mPluginContext;
    private File mFile;
    private DexClassLoader mClassLoader;
    private Resources mResource;
    private PackageInfo mPackageInfo;
    private ApplicationInfo mApplicationInfo;

    public LoadedPlugin(Context hostContext, File file) {
        this.mHostContext = hostContext;
        this.mFile = file;
        if (mClassLoader == null) {
            // 根据apk路径加载apk代码到DexClassLoader中
            mClassLoader = new DexClassLoader(file.getPath(), file.getAbsolutePath(), null, ClassLoader.getSystemClassLoader());
        }
        mResource = loadResources(file.getAbsolutePath());
        this.mPluginContext = createPluginContext(null);
    }

    public DexClassLoader getClassLoader() {
        return mClassLoader;
    }

    public Resources getResource() {
        return mResource;
    }

    public Application getApplication() {
        return mApplication;
    }

    public Context getPluginContext() {
        return mPluginContext;
    }

    public PackageInfo getPackageInfo() {
        return mPackageInfo;
    }

    public void setPackageInfo(PackageInfo mPackageInfo) {
        this.mPackageInfo = mPackageInfo;
        this.mApplicationInfo = mPackageInfo.applicationInfo;
        makeApplication(false, PluginManager.getInstance().getHostInstrumentation());
    }

    public Resources.Theme getTheme() {
        Resources.Theme theme = this.mResource.newTheme();
        theme.applyStyle(PluginUtil.selectDefaultTheme(this.mApplicationInfo.theme, Build.VERSION.SDK_INT), false);
        return theme;
    }

    public PluginContext createPluginContext(Context context) {
        if (context == null) {
            return new PluginContext(this);
        }

        return new PluginContext(this, context);
    }

    protected Application makeApplication(boolean forceDefaultAppClass, Instrumentation instrumentation) {
        if (null != this.mApplication) {
            return this.mApplication;
        }

        String appClass = this.mApplicationInfo.className;
        if (forceDefaultAppClass || null == appClass) {
            appClass = "android.app.Application";
        }

        try {
            this.mApplication = instrumentation.newApplication(this.mClassLoader, appClass, this.getPluginContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        instrumentation.callApplicationOnCreate(this.mApplication);
        return this.mApplication;
    }

    private Resources loadResources(String path) {
        Resources hostResources = PluginManager.getInstance().getHostContext().getResources();
        AssetManager assetManager = null;
        try {
            assetManager = AssetManager.class.newInstance();
            Method addAssetPathMethod = AssetManager.class.getMethod("addAssetPath", String.class);
            addAssetPathMethod.setAccessible(true);
            addAssetPathMethod.invoke(assetManager, path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Resources(assetManager, hostResources.getDisplayMetrics(), hostResources.getConfiguration());
    }

    public Class loadClass(String className) {
        if (mClassLoader == null) {
            return null;
        }
        try {
            return mClassLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


}
