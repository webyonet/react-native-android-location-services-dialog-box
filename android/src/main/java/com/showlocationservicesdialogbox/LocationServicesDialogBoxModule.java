package com.showlocationservicesdialogbox;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import com.facebook.react.bridge.*;

import static android.content.Context.LOCATION_SERVICE;

public class LocationServicesDialogBoxModule extends ReactContextBaseJavaModule {
    public LocationServicesDialogBoxModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "LocationServicesDialogBox";
    }

    @ReactMethod
    public void checkLocationServicesIsEnabled(ReadableMap textMap, Callback callback) {
        Activity currentActivity = getCurrentActivity();

        LocationManager locationManager = (LocationManager) currentActivity.getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            displayPromptForEnablingGPS(currentActivity, textMap, callback);
        } else {
            callback.invoke("enabled");
        }
    }

    private static void displayPromptForEnablingGPS(final Activity activity, final ReadableMap textMap, final Callback callback) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;

        builder.setMessage(textMap.getString("message"))
                .setPositiveButton(textMap.getString("ok"),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int id) {
                                activity.startActivity(new Intent(action));
                                callback.invoke("ok");
                                dialogInterface.dismiss();
                            }
                        })
                .setNegativeButton(textMap.getString("cancel"),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int id) {
                                callback.invoke("cancel");
                                dialogInterface.cancel();
                            }
                        });
        builder.create().show();
    }
}
