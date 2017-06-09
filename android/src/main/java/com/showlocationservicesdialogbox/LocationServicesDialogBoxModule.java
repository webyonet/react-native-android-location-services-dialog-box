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

    private void checkLocationService(Boolean activityResult) {
        if (currentActivity == null || map == null || promiseCallback == null) return;
        LocationManager locationManager = (LocationManager) currentActivity.getSystemService(Context.LOCATION_SERVICE);

        Boolean isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!map.hasKey("enableHighAccuracy") || map.getBoolean("enableHighAccuracy")) {
            isEnabled = isEnabled && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }
        if (!isEnabled) {
            if (activityResult) {
                promiseCallback.reject(new Throwable("disabled"));
            } else {
                displayPromptForEnablingGPS(currentActivity, map, promiseCallback);
            }
        } else {
            promiseCallback.resolve("enabled");
        }
    }

    private static void displayPromptForEnablingGPS(final Activity activity, final ReadableMap configMap, final Promise promise) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final String action = android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS;

        builder.setMessage(Html.fromHtml(configMap.getString("message")))
                .setPositiveButton(configMap.getString("ok"),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int id) {
                                activity.startActivityForResult(new Intent(action), ENABLE_LOCATION_SERVICES);
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
        builder.create().show();
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if(requestCode == ENABLE_LOCATION_SERVICES) {
            currentActivity = activity;
            checkLocationService(true);
        }
    }
}
