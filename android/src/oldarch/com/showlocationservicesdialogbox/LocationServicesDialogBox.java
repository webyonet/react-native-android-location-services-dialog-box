package com.showlocationservicesdialogbox;

import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.Promise;

public class LocationServicesDialogBox extends ReactContextBaseJavaModule {

    private final LocationServicesDialogBoxImpl delegate;

    LocationServicesDialogBox(ReactApplicationContext reactContext) {
        super(reactContext);
        delegate = new LocationServicesDialogBoxImpl(reactContext);
    }

    @Override
    public String getName() {
        return LocationServicesDialogBoxImpl.NAME;
    }

    @ReactMethod
    public void checkLocationServicesIsEnabled(ReadableMap configMap, Promise promise) {
        delegate.checkLocationServicesIsEnabled(configMap,promise);
    }

    @ReactMethod
    public void forceCloseDialog() {
        delegate.forceCloseDialog();
    }

    @ReactMethod
    public void stopListener() {
        delegate.stopListener();
    }
}
