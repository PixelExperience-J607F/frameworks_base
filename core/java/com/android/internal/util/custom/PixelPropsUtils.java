/*
 * Copyright (C) 2022 The Pixel Experience Project
 *               2021-2022 crDroid Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.util.custom;

import android.app.Application;
import android.os.Build;
import android.os.SystemProperties;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PixelPropsUtils {

    private static final String TAG = PixelPropsUtils.class.getSimpleName();
    private static final String DEVICE = "org.pixelexperience.device";
    private static final boolean DEBUG = false;

    private static final String SAMSUNG = "com.samsung.";

    private static final Map<String, Object> propsToChangeGeneric;
    private static final Map<String, Object> propsToChangeMeizu;
    private static final Map<String, Object> propsToChangeMiPad;
    private static final Map<String, Object> propsToChangePixel5;
    private static final Map<String, Object> propsToChangePixel7Pro;
    private static final Map<String, Object> propsToChangePixelXL;
    private static final Map<String, ArrayList<String>> propsToKeep;

    private static final String[] packagesToChangeMiPad = {
            "com.tencent.mm"
    };

    private static final String[] meizuPropToChange = {
            "com.netease.cloudmusic",
            "com.tencent.qqmusic",
	        "com.kugou.android",
	        "cmccwm.mobilemusic",
            "cn.kuwo.player",
            "com.meizu.media.music"
    };

    private static final String[] packagesToChangePixel7Pro = {
            "com.google.android.apps.wallpaper",
            "com.google.android.apps.privacy.wildlife"
    };

    private static final String[] packagesToChangePixelXL = {
            "com.google.android.apps.photos",
            "com.google.android.inputmethod.latin"
    };

    private static final String[] extraPackagesToChange = {
            "com.android.chrome",
            "com.android.vending",
            "com.breel.wallpapers20",
            "com.nothing.smartcenter"
    };

    private static final String[] customGoogleCameraPackages = {
            "com.google.android.MTCL83",
            "com.google.android.UltraCVM",
            "com.google.android.apps.cameralite"
    };

    private static final String[] packagesToKeep = {
            "com.google.android.dialer",
            "com.google.android.euicc",
            "com.google.ar.core",
            "com.google.android.youtube",
            "com.google.android.apps.youtube.kids",
            "com.google.android.apps.youtube.music",
            "com.google.android.apps.recorder",
            "com.google.android.apps.wearables.maestro.companion",
            "com.google.android.apps.subscriptions.red",
            "com.google.android.apps.tachyon",
            "com.google.android.apps.tycho"
    };

    // Codenames for currently supported Pixels by Google
    private static final String[] pixelCodenames = {
            "cheetah",
            "panther",
            "bluejay",
            "oriole",
            "raven",
            "barbet",
            "redfin",
            "bramble",
            "sunfish"
    };

    private static volatile boolean sIsGms = false;
    private static volatile boolean sIsFinsky = false;

    static {
        propsToKeep = new HashMap<>();
        propsToKeep.put("com.google.android.settings.intelligence", new ArrayList<>(Collections.singletonList("FINGERPRINT")));
        propsToChangeGeneric = new HashMap<>();
        propsToChangeGeneric.put("TYPE", "user");
        propsToChangeGeneric.put("TAGS", "release-keys");
        propsToChangePixel7Pro = new HashMap<>();
        propsToChangePixel7Pro.put("BRAND", "google");
        propsToChangePixel7Pro.put("MANUFACTURER", "Google");
        propsToChangePixel7Pro.put("DEVICE", "cheetah");
        propsToChangePixel7Pro.put("PRODUCT", "cheetah");
        propsToChangePixel7Pro.put("MODEL", "Pixel 7 Pro");
        propsToChangePixel7Pro.put("FINGERPRINT", "google/cheetah/cheetah:13/TQ2A.230405.003.E1/9802792:user/release-keys");
        propsToChangePixel5 = new HashMap<>();
        propsToChangePixel5.put("BRAND", "google");
        propsToChangePixel5.put("MANUFACTURER", "Google");
        propsToChangePixel5.put("DEVICE", "redfin");
        propsToChangePixel5.put("PRODUCT", "redfin");
        propsToChangePixel5.put("MODEL", "Pixel 5");
        propsToChangePixel5.put("FINGERPRINT", "google/redfin/redfin:13/TQ2A.230405.003/9719927:user/release-keys");
        propsToChangePixelXL = new HashMap<>();
        propsToChangePixelXL.put("BRAND", "google");
        propsToChangePixelXL.put("MANUFACTURER", "Google");
        propsToChangePixelXL.put("DEVICE", "marlin");
        propsToChangePixelXL.put("PRODUCT", "marlin");
        propsToChangePixelXL.put("MODEL", "Pixel XL");
        propsToChangePixelXL.put("FINGERPRINT", "google/marlin/marlin:10/QP1A.191005.007.A3/5972272:user/release-keys");
        propsToChangeMiPad = new HashMap<>();
        propsToChangeMiPad.put("BRAND", "Xiaomi");
        propsToChangeMiPad.put("MANUFACTURER", "Xiaomi");
        propsToChangeMiPad.put("DEVICE", "nabu");
        propsToChangeMiPad.put("PRODUCT", "nabu");
        propsToChangeMiPad.put("MODEL", "21051182C");
        propsToChangeMeizu = new HashMap<>();
	    propsToChangeMeizu.put("BRAND", "meizu");
	    propsToChangeMeizu.put("MANUFACTURER", "meizu");
	    propsToChangeMeizu.put("DEVICE", "meizu18");
	    propsToChangeMeizu.put("PRODUCT", "meizu_18_CN");
	    propsToChangeMeizu.put("MODEL", "MEIZU 18");
	    propsToChangeMeizu.put("FINGERPRINT", "meizu/meizu_18_CN/meizu18:11/RKQ1.201105.002/1607588916:user/release-keys");
    }

    private static boolean isGoogleCameraPackage(String packageName){
        return packageName.startsWith("com.google.android.GoogleCamera") ||
            Arrays.asList(customGoogleCameraPackages).contains(packageName);
    }

    public static void setProps(String packageName) {
        propsToChangeGeneric.forEach((k, v) -> setPropValue(k, v));

        if (packageName == null || packageName.isEmpty()) {
            return;
        }
        if (Arrays.asList(packagesToKeep).contains(packageName)) {
            return;
        }
        if (isGoogleCameraPackage(packageName)) {
            return;
        }
        if (packageName.startsWith("com.google.")
                || packageName.startsWith(SAMSUNG)
                || Arrays.asList(extraPackagesToChange).contains(packageName)) {

            Map<String, Object> propsToChange = new HashMap<>();

            boolean isPixelDevice = Arrays.asList(pixelCodenames).contains(SystemProperties.get(DEVICE));

            if (packageName.equals("com.google.android.apps.photos")) {
                propsToChange.putAll(propsToChangePixelXL);
            } else if (packageName.equals("com.android.vending")) {
                sIsFinsky = true;
                return;
            } else if (!isPixelDevice) {
                if ((Arrays.asList(packagesToChangePixel7Pro).contains(packageName))) {
                    propsToChange.putAll(propsToChangePixel7Pro);
                } else if (Arrays.asList(packagesToChangePixelXL).contains(packageName)) {
                    propsToChange.putAll(propsToChangePixelXL);
                } else {
                    propsToChange.putAll(propsToChangePixel5);
                }
            }

            if (DEBUG) Log.d(TAG, "Defining props for: " + packageName);
            for (Map.Entry<String, Object> prop : propsToChange.entrySet()) {
                String key = prop.getKey();
                Object value = prop.getValue();
                if (propsToKeep.containsKey(packageName) && propsToKeep.get(packageName).contains(key)) {
                    if (DEBUG) Log.d(TAG, "Not defining " + key + " prop for: " + packageName);
                    continue;
                }
                if (DEBUG) Log.d(TAG, "Defining " + key + " prop for: " + packageName);
                setPropValue(key, value);
            }
            if (packageName.equals("com.google.android.gms")) {
                final String processName = Application.getProcessName();
                if (processName.equals("com.google.android.gms.unstable")) {
                    sIsGms = true;
                    setPropValue("FINGERPRINT", "google/marlin/marlin:7.1.2/NJH47F/4146041:user/release-keys");
                    setPropValue("PRODUCT", "marlin");
                    setPropValue("DEVICE", "marlin");
                    setPropValue("MODEL", "Pixel XL");
                    setVersionField("DEVICE_INITIAL_SDK_INT", Build.VERSION_CODES.N_MR1);
                }
                return;
            }
            // Set proper indexing fingerprint
            if (packageName.equals("com.google.android.settings.intelligence")) {
                setPropValue("FINGERPRINT", Build.VERSION.INCREMENTAL);
            }
        }
        if ((Arrays.asList(
                SystemProperties.get("ro.build.characteristics", "")
            ).contains("tablet")) &&
            (Arrays.asList(packagesToChangeMiPad).contains(packageName))) {
            if (DEBUG) Log.d(TAG, "Defining props for: " + packageName);
            for (Map.Entry<String, Object> prop : propsToChangeMiPad.entrySet()) {
                setPropValue(prop.getKey(), prop.getValue());
            }
        }
        // Set Props for StatusBar Lyric
    	if (Arrays.asList(meizuPropToChange).contains(packageName)) {
	        if (DEBUG) Log.d(TAG, "Defining props for: " + packageName);
	        for (Map.Entry<String, Object> prop : propsToChangeMeizu.entrySet()) {
    		String key = prop.getKey();
	    	Object value = prop.getValue();
    		if (DEBUG) Log.d(TAG, "Defining " + key + " prop for: " + packageName);
    		setPropValue(key, value);
	        }
	    }
    }

    private static void setPropValue(String key, Object value) {
        try {
            if (DEBUG) Log.d(TAG, "Defining prop " + key + " to " + value.toString());
            Field field = Build.class.getDeclaredField(key);
            field.setAccessible(true);
            field.set(null, value);
            field.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.e(TAG, "Failed to set prop " + key, e);
        }
    }

    private static void setVersionField(String key, Object value) {
        try {
            if (DEBUG) Log.d(TAG, "Defining version field " + key + " to " + value.toString());
            Field field = Build.VERSION.class.getDeclaredField(key);
            field.setAccessible(true);
            field.set(null, value);
            field.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.e(TAG, "Failed to set version field " + key, e);
        }
    }

    private static boolean isCallerSafetyNet() {
        return sIsGms && Arrays.stream(Thread.currentThread().getStackTrace())
                .anyMatch(elem -> elem.getClassName().contains("DroidGuard"));
    }

    public static void onEngineGetCertificateChain() {
        // Check stack for SafetyNet or Play Integrity
        if (isCallerSafetyNet() || sIsFinsky) {
            Log.i(TAG, "Blocked key attestation sIsGms=" + sIsGms + " sIsFinsky=" + sIsFinsky);
            throw new UnsupportedOperationException();
        }
    }
}
