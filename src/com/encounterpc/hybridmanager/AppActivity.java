package com.encounterpc.hybridmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.viewpagerindicator.TitlePageIndicator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class AppActivity extends Activity {
    private static final String PROP_PATH = "/system/aokp.prop";

    private PackageManager pm;
    private Intent main = new Intent(Intent.ACTION_MAIN, null);
    private List<ResolveInfo> userActivities = new ArrayList<ResolveInfo>();
    private List<ResolveInfo> systemActivities = new ArrayList<ResolveInfo>();
    //private HashMap<String, ResolveInfo> resolveMap = new HashMap<String, ResolveInfo>();
    private List<Drawable> userIcons = new ArrayList<Drawable>();
    private List<String> userLabels = new ArrayList<String>();
    private List<Drawable> systemIcons = new ArrayList<Drawable>();
    private List<String> systemLabels = new ArrayList<String>();
    private List<Integer> systemDensities = new ArrayList<Integer>();
    private List<Integer> userDensities = new ArrayList<Integer>();

    int defaultSystemDensity;
    int defaultUserDensity;
    Map<String, Integer> appDensities = new LinkedHashMap<String, Integer>();
    Map<String, String> systemAppMap = new HashMap<String, String>();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        File prop = new File(PROP_PATH);
        if (prop.exists()) {
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
                        appDensities.put(split[0], Integer.valueOf(split[1]));
                    }
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            final ViewPagerAdapter adapter = new ViewPagerAdapter(this, new String[]{"User", "System", "Special"},
                    new Integer[]{R.layout.listlayout, R.layout.listlayout, R.layout.listlayout});
            final ViewPager pager = (ViewPager) findViewById(R.id.viewpager);
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

                        if (info.activityInfo.name.equals(getClass().getName())) {
                            iterator.remove();
                            continue;
                        }

                        if (((info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) ||
                                ((info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0))
                            userActivities.add(info);
                        else {
                            systemActivities.add(info);
                            systemAppMap.put(info.activityInfo.packageName, info.activityInfo.applicationInfo.sourceDir
                                    .replaceFirst("/system/app/", "").split("\\.apk")[0]);
                        }
                    }

                    for (ResolveInfo info : userActivities) {
                        userIcons.add(info.loadIcon(pm));
                        userLabels.add(info.loadLabel(pm).toString());
                        if (appDensities.containsKey(info.activityInfo.packageName))
                            userDensities.add(appDensities.get(info.activityInfo.packageName));
                        else userDensities.add(defaultUserDensity);
                    }

                    for (ResolveInfo info : systemActivities) {
                        systemIcons.add(info.loadIcon(pm));
                        systemLabels.add(info.loadLabel(pm).toString());
                        if (appDensities.containsKey(info.activityInfo.packageName))
                            systemDensities.add(appDensities.get(info.activityInfo.packageName));
                        else if (systemAppMap.containsKey(info.activityInfo.packageName) &&
                                appDensities.containsKey(systemAppMap.get(info.activityInfo.packageName)))
                            systemDensities.add(appDensities.get(systemAppMap.get(info.activityInfo.packageName)));
                        else systemDensities.add(defaultSystemDensity);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ListView listView = (ListView) adapter.getView(pager, 0).findViewById(R.id.listview);
                            listView.setAdapter(new AppAdapter(AppActivity.this, userIcons, userLabels, userDensities));
                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    Intent intent = new Intent(Intent.ACTION_MAIN);
                                    ResolveInfo info = userActivities.get(i);
                                    intent.setClassName(info.activityInfo.packageName, info.activityInfo.name);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                                            | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                                    startActivity(intent);
                                }
                            });
                            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                                @Override
                                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    Toast.makeText(AppActivity.this, userActivities.get(i).activityInfo.packageName + '\n' +
                                            userActivities.get(i).activityInfo.applicationInfo.sourceDir, 5).show();
                                    return true;
                                }
                            });

                            listView = (ListView) adapter.getView(pager, 1).findViewById(R.id.listview);
                            listView.setAdapter(new AppAdapter(AppActivity.this, systemIcons, systemLabels, systemDensities));
                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    Intent intent = new Intent(Intent.ACTION_MAIN);
                                    ResolveInfo info = systemActivities.get(i);
                                    intent.setClassName(info.activityInfo.packageName, info.activityInfo.name);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                                            | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                                    startActivity(intent);
                                }
                            });
                            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                                @Override
                                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    Toast.makeText(AppActivity.this, systemActivities.get(i).activityInfo.packageName + '\n' +
                                            systemActivities.get(i).activityInfo.applicationInfo.sourceDir + '\n' +
                                            systemActivities.get(i).activityInfo.applicationInfo.sourceDir
                                                    .replaceFirst("/system/app/", "").split("\\.apk")[0], 5).show();
                                    return true;
                                }
                            });

                            /*listView = (ListView) adapter.getView(pager, 1).findViewById(R.id.listview);
                          listView.setAdapter(new AppAdapter(AppActivity.this, ));*/

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

    @Override
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
                        Toast.makeText(AppActivity.this, "test", 5).show();
                    }
                });
                return true;
            }
        });

        return true;
    }
}