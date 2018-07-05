package com.showlocationservicesdialogbox;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.location.LocationManager;
import android.text.Html;
import android.text.Spanned;
import com.facebook.react.bridge.*;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class LocationServicesDialogBoxModule extends ReactContextBaseJavaModule implements ActivityEventListener {
    private Promise promiseCallback;
    private ReadableMap map;
    private Activity currentActivity;
    private static final int ENABLE_LOCATION_SERVICES = 1009;
    private static AlertDialog alertDialog;
    private Boolean isReceive = false;
    private BroadcastReceiver providerReceiver = null;
    private ReactApplicationContext reactContext;

    LocationServicesDialogBoxModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
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
        if (alertDialog != null && promiseCallback != null) {
            promiseCallback.reject(new Throwable("disabled"));
            alertDialog.cancel();
        }
    }

    @ReactMethod
    public void stopListener() {
        isReceive = false;
        try {
            if (providerReceiver != null) {
                getReactApplicationContext().unregisterReceiver(providerReceiver);
                providerReceiver = null;
            }
        } catch (Exception ignored) {
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
            if (map.hasKey("providerListener") && map.getBoolean("providerListener")) {
                startListener();
            }

            result.putString("status", "enabled");
            result.putBoolean("enabled", true);
            result.putBoolean("alreadyEnabled", !activityResult);

            promiseCallback.resolve(result);
        }
    }

    private void displayPromptForEnablingGPS(final Activity activity, final ReadableMap configMap, final Promise promise) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        final Spanned message = (
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N ?
                        Html.fromHtml(configMap.getString("message"), Html.FROM_HTML_MODE_LEGACY) :
                        Html.fromHtml(configMap.getString("message"))
        );

        builder.setMessage(message)
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

        if (!configMap.hasKey("preventOutSideTouch") || configMap.getBoolean("preventOutSideTouch")) {
            alertDialog.setCanceledOnTouchOutside(false);
        }

        if (!configMap.hasKey("preventBackClick") || configMap.getBoolean("preventBackClick")) {
            alertDialog.setCancelable(false);
        }

        alertDialog.show();
    }

    private void newActivity(final Activity activity) {
        final String action = android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS;
        activity.startActivityForResult(new Intent(action), ENABLE_LOCATION_SERVICES);
    }

    private void startListener() {
        try {
            providerReceiver = new LocationProviderChangedReceiver();
            getReactApplicationContext().registerReceiver(providerReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
            isReceive = true;
        } catch (Exception ignored) {
        }
    }

    private void sendEvent() {
        if (isReceive) {
            LocationManager locationManager = (LocationManager) currentActivity.getSystemService(Context.LOCATION_SERVICE);
            WritableMap params = Arguments.createMap();
            if (locationManager != null) {
                boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                params.putString("status", (enabled ? "enabled" : "disabled"));
                params.putBoolean("enabled", enabled);

                if (this.reactContext != null) {
                    this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("locationProviderStatusChange", params);
                }
            }
        }
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode == ENABLE_LOCATION_SERVICES) {
            currentActivity = activity;
            checkLocationService(true);
        }
    }

    private final class LocationProviderChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action != null && action.matches("android.location.PROVIDERS_CHANGED")) {
                sendEvent();
            }
        }
    }
}
