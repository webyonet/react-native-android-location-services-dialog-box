package com.showlocationservicesdialogbox;

import android.os.Build;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.Promise;

import com.showlocationservicesdialogbox.NativeLocationServicesDialogBoxSpec;

public class LocationServicesDialogBox extends NativeLocationServicesDialogBoxSpec {

    private final LocationServicesDialogBoxImpl delegate;

    public LocationServicesDialogBox(ReactApplicationContext reactContext) {
        super(reactContext);
        delegate = new LocationServicesDialogBoxImpl(reactContext);
    }

    @NonNull
    @Override
    public String getName() {
        return LocationServicesDialogBoxImpl.NAME;
    }

    @Override
    public void checkLocationServicesIsEnabled(ReadableMap configMap, Promise promise) {
        delegate.checkLocationServicesIsEnabled(configMap,promise);
    }

    @Override
    public void forceCloseDialog() {
        delegate.forceCloseDialog();
    }

    @Override
    public void stopListener() {
        delegate.stopListener();
    }
}