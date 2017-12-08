package com.showlocationservicesdialogbox;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.text.Html;

import com.facebook.react.bridge.*;

class LocationServicesDialogBoxModule extends ReactContextBaseJavaModule implements ActivityEventListener {
    private Promise promiseCallback;
    private ReadableMap map;
    private Activity currentActivity;
    private static final int ENABLE_LOCATION_SERVICES = 1009;
    private static AlertDialog alertDialog;

    LocationServicesDialogBoxModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addActivityEventListener(this);
    }

    @Override
    public void onNewIntent(Intent intent) {
    }

    @Override
    public String getName() {
        return "LocationServicesDialogBox";
    }

    @ReactMethod
    public void checkLocationServicesIsEnabled(ReadableMap configMap, Promise promise) {
        promiseCallback = promise;
        map = configMap;
        currentActivity = getCurrentActivity();
        checkLocationService(false);
    }

    @ReactMethod
    public void forceCloseDialog() {
        if (alertDialog != null) {
            promiseCallback.reject(new Throwable("disabled"));
            alertDialog.cancel();
        }
    }

    private void checkLocationService(Boolean activityResult) {
        if (currentActivity == null || map == null || promiseCallback == null) return;
        LocationManager locationManager = (LocationManager) currentActivity.getSystemService(Context.LOCATION_SERVICE);
        WritableMap result = Arguments.createMap();

        Boolean isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (map.hasKey("enableHighAccuracy") && map.getBoolean("enableHighAccuracy")) {
            // High accuracy needed. Require NETWORK_PROVIDER.
            isEnabled = isEnabled && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } else {
            // Either highAccuracy is not a must which means any location will suffice
            // or it is not specified which means again that any location will do.
            isEnabled = isEnabled || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }

        if (!isEnabled) {
            if (activityResult || map.hasKey("openLocationServices") && !map.getBoolean("openLocationServices")) {
                promiseCallback.reject(new Throwable("disabled"));
            } else if (!map.hasKey("showDialog") || map.getBoolean("showDialog")) {
                displayPromptForEnablingGPS(currentActivity, map, promiseCallback);
            } else {
                newActivity(currentActivity);
            }
        } else {
            result.putString("status", "enabled");
            result.putBoolean("enabled", true);
            result.putBoolean("alreadyEnabled", !activityResult);

            promiseCallback.resolve(result);
        }
    }

    private static void displayPromptForEnablingGPS(final Activity activity, final ReadableMap configMap, final Promise promise) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setMessage(Html.fromHtml(configMap.getString("message")))
                .setPositiveButton(configMap.getString("ok"),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int id) {
                                newActivity(activity);
                                dialogInterface.dismiss();
                            }
                        })
                .setNegativeButton(configMap.getString("cancel"),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int id) {
                                promise.reject(new Throwable("disabled"));
                                dialogInterface.cancel();
                            }
                        });

        alertDialog = builder.create();
        if (!map.hasKey("preventOutSideTouch") || !map.getBoolean("preventOutSideTouch")){        
        alertDialog.setCanceledOnTouchOutside(false);
        }
        if (!map.hasKey("preventBackClick") || !map.getBoolean("preventBackClick")){
        alertDialog.setCancelable(false);
        }
        alertDialog.show();
    }

    private static void newActivity(final Activity activity) {
        final String action = android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS;
        activity.startActivityForResult(new Intent(action), ENABLE_LOCATION_SERVICES);
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode == ENABLE_LOCATION_SERVICES) {
            currentActivity = activity;
            checkLocationService(true);
        }
    }
}
