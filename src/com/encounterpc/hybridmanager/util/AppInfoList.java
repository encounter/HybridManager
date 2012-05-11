package com.encounterpc.hybridmanager.util;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AppInfoList extends ArrayList<AppInfo> {
    private HashMap<String, AppInfo> packageNameMap = new HashMap<String, AppInfo>();
    private HashMap<String, AppInfo> altNameMap = new HashMap<String, AppInfo>();

    public List<AppInfo> getUserApps() {
        ArrayList<AppInfo> arrayList = new ArrayList<AppInfo>();
        for (AppInfo app : this) {
            if (app.getType() == AppInfo.Type.USER) arrayList.add(app);
        }
        return arrayList;
    }

    public List<AppInfo> getSystemApps() {
        ArrayList<AppInfo> arrayList = new ArrayList<AppInfo>();
        for (AppInfo app : this) {
            if (app.getType() == AppInfo.Type.SYSTEM) arrayList.add(app);
        }
        return arrayList;
    }

    public AppInfo get(String packageName) {
        return altNameMap.containsKey(packageName) ? altNameMap.get(packageName) : packageNameMap.get(packageName);
    }

    public void add(ResolveInfo info) {
        AppInfo app = new AppInfo(info);
        packageNameMap.put(app.getPackageName(), app);
        if (app.getType() == AppInfo.Type.SYSTEM) {
            altNameMap.put(app.getAltName(), app);
        }
        add(app);
    }

    public void loadPackages(PackageManager pm) {
        for (AppInfo app : this) {
            app.retrieveInfo(pm);
        }
    }

    public List<AppInfo> getCustomDensities() {
        ArrayList<AppInfo> list = new ArrayList<AppInfo>();
        for (AppInfo app : this) {
            if (app.getCustomDensity() != null) list.add(app);
        }
        return list;
    }
}
