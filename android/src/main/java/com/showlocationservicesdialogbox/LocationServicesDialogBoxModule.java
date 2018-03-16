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
        // Robustness check
        if (currentActivity == null || map == null || promiseCallback == null) return;
        LocationManager locationManager = (LocationManager) currentActivity.getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
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
		final String action = android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS;
		String show_Dialog = configMap.getString("showDialog");
		if(show_Dialog.equals("false"))
		{
		activity.startActivityForResult(new Intent(action), 1);
			
		}else{
		final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        //final String action = android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS;

        builder.setMessage(Html.fromHtml(configMap.getString("message")))
                .setPositiveButton(configMap.getString("ok"),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int id) {
                                activity.startActivityForResult(new Intent(action), 1);
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
	}
	


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        checkLocationService(true);
    }
}
