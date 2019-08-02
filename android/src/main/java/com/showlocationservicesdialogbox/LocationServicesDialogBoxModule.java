package com.showlocationservicesdialogbox;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.text.Html;
import android.text.Spanned;
import android.view.Window;
import android.widget.Button;
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
    private ReactApplicationContext RNContext;

    LocationServicesDialogBoxModule(ReactApplicationContext reactContext) {
        super(reactContext);
        RNContext = reactContext;
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
        try {
            if (alertDialog != null && alertDialog.isShowing() && promiseCallback != null) {
                alertDialog.cancel();
            }
        } catch (Exception ignored) {}
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

        Boolean isEnabled = this.isEnabled(locationManager);

        if (!isEnabled) {
            if (activityResult || map.hasKey("openLocationServices") && !map.getBoolean("openLocationServices")) {
                promiseCallback.reject(new Throwable("disabled"));
                if (map.hasKey("providerListener") && map.getBoolean("providerListener")) {
                    startListener();
                }
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

        if (configMap.hasKey("style")) {
            colorAdjustment(alertDialog, configMap.getMap("style"));
        }
    }

    private void colorAdjustment(AlertDialog dialog, ReadableMap colorMap) {
        Window window = dialog.getWindow();

        if (window != null) {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            if (colorMap.hasKey("backgroundColor")) {
                window.setBackgroundDrawable(new ColorDrawable(Color.parseColor(colorMap.getString("backgroundColor"))));
            }

            if (colorMap.hasKey("positiveButtonTextColor")) {
                positiveButton.setTextColor(Color.parseColor(colorMap.getString("positiveButtonTextColor")));
            }

            if (colorMap.hasKey("positiveButtonBackgroundColor")) {
                positiveButton.setBackgroundColor(Color.parseColor(colorMap.getString("positiveButtonBackgroundColor")));
            }

            if (colorMap.hasKey("negativeButtonTextColor")) {
                negativeButton.setTextColor(Color.parseColor(colorMap.getString("negativeButtonTextColor")));
            }

            if (colorMap.hasKey("negativeButtonBackgroundColor")) {
                negativeButton.setBackgroundColor(Color.parseColor(colorMap.getString("negativeButtonBackgroundColor")));
            }
        }
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

    private Boolean isEnabled(LocationManager locationManager) {
        Boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (map != null) {
            if (map.hasKey("enableHighAccuracy") && map.getBoolean("enableHighAccuracy")) {
                // High accuracy needed. Require NETWORK_PROVIDER.
                enabled = enabled && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } else {
                // Either highAccuracy is not a must which means any location will suffice
                // or it is not specified which means again that any location will do.
                enabled = enabled || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            }
        }

        return enabled;
    }

    private void sendEvent() {
        if (isReceive) {
            LocationManager locationManager = (LocationManager) currentActivity.getSystemService(Context.LOCATION_SERVICE);
            WritableMap params = Arguments.createMap();
            if (locationManager != null) {
                boolean enabled = this.isEnabled(locationManager);

                params.putString("status", (enabled ? "enabled" : "disabled"));
                params.putBoolean("enabled", enabled);

                if (RNContext != null) {
                    RNContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("locationProviderStatusChange", params);
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
