package com.encounterpc.hybridmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import com.encounterpc.hybridmanager.adapters.AppListAdapter;
import com.encounterpc.hybridmanager.adapters.ViewPagerAdapter;
import com.encounterpc.hybridmanager.util.AppInfo;
import com.encounterpc.hybridmanager.util.AppInfoList;
import com.encounterpc.hybridmanager.util.RootShell;
import com.viewpagerindicator.TitlePageIndicator;

import java.io.*;
import java.util.*;

public class AppListActivity extends Activity {
    private static final String PROP_PATH = "/system/aokp.prop";

    private PackageManager pm;
    private Intent main = new Intent(Intent.ACTION_MAIN, null);
    private AppInfoList appInfos = new AppInfoList();

    private ViewPagerAdapter adapter;
    private ViewPager pager;

    int defaultSystemDensity;
    int defaultUserDensity;
    int stockDensity = 320; // TODO determine by device
    int tabletDensity = 192; // ^

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final File prop = new File(PROP_PATH);
        if (prop.exists()) {
            adapter = new ViewPagerAdapter(this, new String[]{"User", "System", "Special"},
                    new Integer[]{R.layout.listlayout, R.layout.listlayout, R.layout.listlayout});
            pager = (ViewPager) findViewById(R.id.viewpager);
            TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.indicator);
            pager.setAdapter(adapter);
            indicator.setViewPager(pager);
            pager.setOffscreenPageLimit(2);

            new Thread() {
                public void run() {
                    pm = getPackageManager();
                    main.addCategory(Intent.CATEGORY_LAUNCHER);
                    List<ResolveInfo> activities = pm.queryIntentActivities(main, 0);
                    Collections.sort(activities, new ResolveInfo.DisplayNameComparator(pm));

                    for (Iterator<ResolveInfo> iterator = activities.iterator(); iterator.hasNext(); ) {
                        ResolveInfo info = iterator.next();

                        if (info.activityInfo.packageName.equals(getClass().getPackage().toString())) {
                            iterator.remove();
                            continue;
                        }

                        appInfos.add(info);
                    }

                    appInfos.loadPackages(pm);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            findViewById(R.id.progress).setVisibility(View.VISIBLE);
                            findViewById(R.id.appChooser).setVisibility(View.GONE);
                        }
                    });

                    parseProp(prop);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ListView listView = (ListView) adapter.getView(pager, 0).findViewById(R.id.listview);
                            listView.setAdapter(new AppListAdapter(AppListActivity.this, appInfos.getUserApps(), defaultUserDensity));
                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    showDensitySelect(appInfos.getUserApps().get(i));
                                }
                            });

                            listView = (ListView) adapter.getView(pager, 1).findViewById(R.id.listview);
                            listView.setAdapter(new AppListAdapter(AppListActivity.this, appInfos.getSystemApps(), defaultSystemDensity));
                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    showDensitySelect(appInfos.getSystemApps().get(i));
                                }
                            });

                            listView = (ListView) adapter.getView(pager, 2).findViewById(R.id.listview);
                            listView.setAdapter(new AppListAdapter(AppListActivity.this, new ArrayList<AppInfo>(), defaultSystemDensity));
                            // TODO

                            findViewById(R.id.progress).setVisibility(View.GONE);
                            findViewById(R.id.appChooser).setVisibility(View.VISIBLE);
                        }
                    });
                }
            }.start();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Hybrid mode not enabled!")
                    .setMessage("These settings require hybrid mode to be enabled.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    }).create().show();
        }
    }

    private void parseProp(File prop) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(prop));
            while (br.ready()) {
                String line = br.readLine();
                if (line.matches("^aokp\\.(system|user)_default_dpi=\\d{1,3}$")) {
                    String[] split = line.replaceFirst("^aokp\\.", "").split("_default_dpi=");
                    if (split[0].equals("user")) defaultUserDensity = Integer.valueOf(split[1]);
                    else defaultSystemDensity = Integer.valueOf(split[1]);
                } else if (line.matches("^aokp\\.((\\w|\\.)*).dd=\\d{1,3}$")) {
                    String[] split = line.replaceFirst("^aokp\\.", "").split("\\.dd=");
                    if (appInfos.get(split[0]) != null)
                        appInfos.get(split[0]).setCustomDensity(Integer.valueOf(split[1]));
                }
            }
            br.close();
        } catch (IOException e) {
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("An error occurred while reading the property file.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    }).create().show();
        }
    }

    private void showDensitySelect(final AppInfo app) {
        final ArrayList<String> densities = new ArrayList<String>();
        int step = (stockDensity - tabletDensity) / 4;
        densities.add(String.valueOf(tabletDensity));
        densities.add(String.valueOf(tabletDensity + step));
        densities.add(String.valueOf(tabletDensity + (step * 2)));
        densities.add(String.valueOf(stockDensity - step));
        densities.add(String.valueOf(stockDensity));
        densities.add("Custom...");

        int selected = densities.indexOf("Custom...");
        String density = String.valueOf(app.getDensity(app.getType() == AppInfo.Type.USER ? defaultUserDensity : defaultSystemDensity));
        if (densities.contains(density))
            selected = densities.indexOf(density);

        new AlertDialog.Builder(AppListActivity.this)
                .setTitle("Select density")
                .setSingleChoiceItems(Arrays.copyOf(densities.toArray(), densities.size(), String[].class),
                        selected, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        if (item == densities.indexOf("Custom...")) {
                            // TODO
                        } else {
                            if (Integer.valueOf(densities.get(item)) == (app.getType() == AppInfo.Type.USER ? defaultUserDensity : defaultSystemDensity))
                                app.setCustomDensity(null);
                            else app.setCustomDensity(Integer.valueOf(densities.get(item)));
                            writeDensities();
                        }
                        dialog.dismiss();
                    }
                }).create().show();
    }

    public void writeDensities() {
        List<AppInfo> apps = appInfos.getCustomDensities();
        try {
            File file = new File(getFilesDir(), "hybrid.temp");
            PrintWriter out = new PrintWriter(new FileWriter(file));
            out.println("# Generated by HybridManager");
            out.println("aokp.per_app_scaling=1");
            out.println("aokp.system_default_dpi=" + defaultSystemDensity);
            out.println("aokp.user_default_dpi=" + defaultUserDensity);
            out.println("aokp.stock_density=" + stockDensity);
            out.println("aokp.tablet_density=" + tabletDensity);
            out.println();
            for (AppInfo app : apps) {
                if (app.getType() == AppInfo.Type.USER)
                    out.println("aokp." + app.getPackageName() + ".dd=" + app.getCustomDensity());
                else out.println("aokp." + app.getAltName() + ".dd=" + app.getCustomDensity());
            }
            out.flush();
            out.close();
            RootShell.execute("cp " + file + " /system/aokp.prop; chmod 0644 /system/aokp.prop");
            parseProp(new File("/system/aokp.prop"));
            for (int i = 0; i < adapter.getCount(); i++) {
                ((BaseAdapter) ((AdapterView) ((ViewGroup) adapter.getView(pager, i)).getChildAt(0)).getAdapter()).notifyDataSetChanged();
            }
        } catch (IOException e) {
            e.printStackTrace();
            new AlertDialog.Builder(this)
                    .setTitle("Error writing to file")
                    .setMessage("Unknown error occurred.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    }).create().show();
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem mnu1 = menu.add("Advanced Mode");
        mnu1.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        mnu1.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AppListActivity.this, "test", 5).show();
                    }
                });
                return true;
            }
        });

        return true;
    }*/
}