package com.encounterpc.hybridmanager.util;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

public class AppInfo {
    private ResolveInfo info;
    private Type type;
    private String altName;

    private String packageName;
    private Drawable icon;
    private String label;

    private Integer customDensity; // Integer instead of int for nullability

    public AppInfo(ResolveInfo info) {
        this.info = info;
        this.packageName = info.activityInfo.packageName;
        this.type = (info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0 ||
                (info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 ?
                Type.USER : Type.SYSTEM;
        if (type == Type.SYSTEM) {
            altName = info.activityInfo.applicationInfo.sourceDir.replaceFirst("/system/app/", "").split("\\.apk")[0];
        }
    }

    public void retrieveInfo(PackageManager pm) {
        this.icon = info.loadIcon(pm);
        this.label = (String) info.loadLabel(pm);
    }

    public ResolveInfo getInfo() {
        return info;
    }

    public Type getType() {
        return type;
    }

    public String getAltName() {
        return altName;
    }

    public String getPackageName() {
        return packageName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public String getLabel() {
        return label;
    }

    public Integer getCustomDensity() {
        return customDensity;
    }

    public void setCustomDensity(Integer customDensity) {
        this.customDensity = customDensity;
    }

    // Non-null version of getCustomDensity
    public int getDensity(int defaultDensity) {
        return customDensity != null ? customDensity : defaultDensity;
    }

    public enum Type {
        USER, SYSTEM, SPECIAL
    }
}
