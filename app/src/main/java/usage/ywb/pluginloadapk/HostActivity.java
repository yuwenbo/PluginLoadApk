package usage.ywb.pluginloadapk;

import androidx.annotation.NonNull;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class HostActivity extends Activity implements View.OnClickListener, PermissionUtils.PermissionCallbacks {

    private static final String[] PERMISSIONS = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.self).setOnClickListener(this);
        findViewById(R.id.plugin).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.self) {
            Intent intent = new Intent(this, SelfActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.plugin) {
            PermissionUtils.requestPermissions(HostActivity.this, 1, PERMISSIONS);
        }
    }

    private File copyToCache(String name) {
        File source = new File(Environment.getExternalStorageDirectory(), "Download" + File.separator + name);
        if (!source.exists() || !source.isFile()) {
            return null;
        }
        File dir = getCacheDir();
        if (!dir.exists() && dir.mkdirs()) {
            Log.i("MainActivity", "创建新文件夹");
        }
        File plugin = new File(dir, name);
        FileChannel input = null;
        FileChannel output = null;
        try {
            if (!plugin.exists() || plugin.createNewFile()) {
                Log.i("MainActivity", "创建新文件");
            }
            if (plugin.length() != source.length()) {
                input = new FileInputStream(source).getChannel();
                output = new FileOutputStream(plugin).getChannel();
                output.transferFrom(input, 0, input.size());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return plugin;
    }

    @Override
    public final void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, String[] perms) {
        File file = copyToCache("mylibrary.apk");
        LoadedPlugin plugin = PluginManager.getInstance().loadPlugin(file);
        Class clazz = plugin.loadClass("usage.ywb.mylibrary.MainActivity");
        Intent intent = new Intent();
        intent.setClassName(plugin.getPackageInfo().packageName, clazz.getName());
        startActivity(intent);
    }

    @Override
    public void onPermissionsDenied(int requestCode, String[] perms) {

    }

}
