package com.encounterpc.hybridmanager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

@SuppressWarnings("deprecation")
public class HybridPreferenceActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RootShell.execute("mount -o remount,rw /system");

        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("hybridEnabled", new File("/system/aokp.prop").exists());
        editor.commit();

        addPreferencesFromResource(R.xml.preferences);

        findPreference("chooseApps").setEnabled(settings.getBoolean("hybridEnabled", false));
        findPreference("backup").setEnabled(settings.getBoolean("hybridEnabled", false));
        findPreference("restore").setEnabled(new File("/mnt/sdcard/hybrid.backup").exists());
        findPreference("hybridEnabled").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                if ((Boolean) o) {
                    if (new File("/system/aokp.unicorn").exists()) {
                        RootShell.execute("mv /system/aokp.unicorn /system/aokp.prop");
                        findPreference("chooseApps").setEnabled(true);
                        findPreference("backup").setEnabled(true);
                    } else new AlertDialog.Builder(HybridPreferenceActivity.this)
                            .setTitle("Oops!")
                            .setMessage("Default hybrid settings (/system/aokp.unicorn) not found. Are you running the latest version of AOKP?")
                            .setPositiveButton("OK", null)
                            .create().show();
                } else {
                    RootShell.execute("mv /system/aokp.prop /system/aokp.unicorn");
                    findPreference("chooseApps").setEnabled(false);
                    findPreference("backup").setEnabled(false);
                }
                showRebootDialog();
                return true;
            }
        });

        findPreference("backup").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (new File("/system/aokp.prop").exists()) {
                    try {
                        FileInputStream in = new FileInputStream(new File("/system/aokp.prop"));
                        FileOutputStream out = new FileOutputStream(new File(Environment.getExternalStorageDirectory(), "hybrid.backup"));
                        byte[] buf = new byte[4096];
                        int i;
                        while ((i = in.read(buf)) != -1) {
                            out.write(buf, 0, i);
                        }
                        in.close();
                        out.close();
                        findPreference("restore").setEnabled(true);
                        new AlertDialog.Builder(HybridPreferenceActivity.this)
                                .setTitle("Finished!")
                                .setMessage("Backup complete!")
                                .setPositiveButton("OK", null)
                                .create().show();
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        new AlertDialog.Builder(HybridPreferenceActivity.this)
                                .setTitle("Error")
                                .setMessage("Unknown error occurred while backing up.")
                                .setPositiveButton("OK", null)
                                .create().show();
                    }
                }
                return false;
            }
        });
        findPreference("restore").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                File hybridBackup = new File(Environment.getExternalStorageDirectory(), "hybrid.backup");
                if (hybridBackup.exists()) {
                    RootShell.execute("cp " + hybridBackup + " /system/aokp.prop");
                    if (hybridBackup.exists()) {
                        findPreference("chooseApps").setEnabled(true);
                        findPreference("backup").setEnabled(true);
                        ((CheckBoxPreference) findPreference("hybridEnabled")).setChecked(true);
                        new AlertDialog.Builder(HybridPreferenceActivity.this)
                                .setTitle("Finished!")
                                .setMessage("Restore complete!")
                                .setPositiveButton("OK", null)
                                .create().show();
                    } else {
                        new AlertDialog.Builder(HybridPreferenceActivity.this)
                                .setTitle("Error")
                                .setMessage("Restore from backup failed.")
                                .setPositiveButton("OK", null)
                                .create().show();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    public void showRebootDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Reboot now?")
                .setMessage("This setting requires a reboot for changes to take effect.")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        RootShell.execute("reboot");
                    }
                })
                .create().show();
    }

    @Override
    public void finish() {
        RootShell.execute("mount -o remount,ro /system");
        super.finish();
    }
}
