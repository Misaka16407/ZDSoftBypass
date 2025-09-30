package com.example.zdsoftbypass;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ZDSoftBypass implements IXposedHookLoadPackage {

    // 目标包名
    private static final String TARGET_PACKAGE = "com.zdsoft.newsquirrel.android";

    // 小米平板5的设备属性
    private static final String[][] XIAOMI_PAD5_PROPERTIES = {
            {"ro.product.manufacturer", "Xiaomi"},
            {"ro.product.model", "Xiaomi Pad 5"},
            {"ro.product.name", "nabu"},
            {"ro.product.device", "nabu"},
            {"ro.build.product", "nabu"},
            {"ro.build.description", "nabu-user 13 RKQ1.211001.001 V14.0.5.0.TKXCNXM release-keys"},
            {"ro.build.fingerprint", "Xiaomi/nabu/nabu:13/RKQ1.211001.001/V14.0.5.0.TKXCNXM:user/release-keys"},
            {"ro.linspirer.mdm.enabled", "0"},
            {"ro.config.linspirer_disabled", "1"}
    };

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(TARGET_PACKAGE)) {
            return;
        }

        XposedBridge.log("ZDSoftBypass: Targeting package " + lpparam.packageName);

        // Hook SystemProperties.get方法
        try {
            Class<?> systemPropertiesClass = XposedHelpers.findClass("android.os.SystemProperties", lpparam.classLoader);
            XposedBridge.hookMethod(XposedHelpers.findMethodBestMatch(systemPropertiesClass, "get", String.class, String.class), new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String key = (String) param.args[0];
                    for (String[] prop : XIAOMI_PAD5_PROPERTIES) {
                        if (prop[0].equals(key)) {
                            param.setResult(prop[1]);
                            XposedBridge.log("ZDSoftBypass: Hooked SystemProperties.get for key: " + key + " -> " + prop[1]);
                            return;
                        }
                    }
                }
            });
        } catch (Throwable t) {
            XposedBridge.log("ZDSoftBypass: Failed to hook SystemProperties: " + t);
        }

        // Hook BaseActivity的方法
        try {
            Class<?> baseActivityClass = XposedHelpers.findClass("com.zdsoft.newsquirrel.android.activity.BaseActivity", lpparam.classLoader);
            
            // hook checkPermissionReadPhoneState
            XposedBridge.hookMethod(XposedHelpers.findMethodBestMatch(baseActivityClass, "checkPermissionReadPhoneState", Context.class), new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("ZDSoftBypass: Bypassing checkPermissionReadPhoneState");
                    param.setResult(true);
                }
            });

            // hook checkPermissionREAD_EXTERNAL_STORAGE
            XposedBridge.hookMethod(XposedHelpers.findMethodBestMatch(baseActivityClass, "checkPermissionREAD_EXTERNAL_STORAGE", Context.class), new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("ZDSoftBypass: Bypassing checkPermissionREAD_EXTERNAL_STORAGE");
                    param.setResult(true);
                }
            });

            // hook isLin
            XposedBridge.hookMethod(XposedHelpers.findMethodBestMatch(baseActivityClass, "isLin"), new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("ZDSoftBypass: Bypassing isLin");
                    param.setResult(false);
                }
            });

            // hook isLinAndEight
            XposedBridge.hookMethod(XposedHelpers.findMethodBestMatch(baseActivityClass, "isLinAndEight"), new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("ZDSoftBypass: Bypassing isLinAndEight");
                    param.setResult(false);
                }
            });

            // hook isLinAndTen
            XposedBridge.hookMethod(XposedHelpers.findMethodBestMatch(baseActivityClass, "isLinAndTen"), new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("ZDSoftBypass: Bypassing isLinAndTen");
                    param.setResult(false);
                }
            });

        } catch (Throwable t) {
            XposedBridge.log("ZDSoftBypass: Failed to hook BaseActivity methods: " + t);
        }

        // Hook PackageManager.getPackageInfo
        try {
            Class<?> packageManagerClass = XposedHelpers.findClass("android.content.pm.PackageManager", lpparam.classLoader);
            XposedBridge.hookMethod(XposedHelpers.findMethodBestMatch(packageManagerClass, "getPackageInfo", String.class, int.class), new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String packageName = (String) param.args[0];
                    if ("com.android.launcher3".equals(packageName)) {
                        XposedBridge.log("ZDSoftBypass: Blocking query for com.android.launcher3");
                        throw new PackageManager.NameNotFoundException("Package not found: com.android.launcher3");
                    }
                }
            });
        } catch (Throwable t) {
            XposedBridge.log("ZDSoftBypass: Failed to hook PackageManager.getPackageInfo: " + t);
        }

        // Hook Context.sendBroadcast
        try {
            Class<?> contextClass = XposedHelpers.findClass("android.content.Context", lpparam.classLoader);
            XposedBridge.hookMethod(XposedHelpers.findMethodBestMatch(contextClass, "sendBroadcast", Intent.class), new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Intent intent = (Intent) param.args[0];
                    String action = intent.getAction();
                    if (action != null) {
                        if (action.equals("com.linspirer.edu.class.over") ||
                            action.equals("com.linspirer.edu.no_control_screen_shoot") ||
                            action.equals("com.linspirer.edu.enablenavigationbar") ||
                            action.equals("com.linspirer.edu.getdevicesn")) {
                            XposedBridge.log("ZDSoftBypass: Blocking MDM broadcast: " + action);
                            param.setResult(null); // 阻止广播发送
                        }
                    }
                }
            });
        } catch (Throwable t) {
            XposedBridge.log("ZDSoftBypass: Failed to hook Context.sendBroadcast: " + t);
        }
    }
}
