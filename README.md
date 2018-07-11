## React Native Android Location Services Dialog Box
<img width="274px" align="right" src="https://raw.githubusercontent.com/webyonet/react-native-android-location-services-dialog-box/master/demo.gif" />

A react-native component for turn on the dialog box from android location services


[![GitHub tag](https://img.shields.io/github/tag/webyonet/react-native-android-location-services-dialog-box.svg)](https://github.com/webyonet/react-native-android-location-services-dialog-box)
[![npm version](https://badge.fury.io/js/react-native-android-location-services-dialog-box.svg)](https://badge.fury.io/js/react-native-android-location-services-dialog-box)
![npm](https://img.shields.io/npm/dm/react-native-android-location-services-dialog-box.svg)
![npm](https://img.shields.io/npm/dt/react-native-android-location-services-dialog-box.svg)
![npm](https://img.shields.io/npm/l/react-native-android-location-services-dialog-box.svg)

### Installation

#### Mostly automatic installation (recommended)

1. `yarn add react-native-android-location-services-dialog-box`
<br/>or<br/>
`npm install react-native-android-location-services-dialog-box --save`
2. `react-native link react-native-android-location-services-dialog-box`

#### Manual Installation

##### Android

1. `yarn add react-native-android-location-services-dialog-box`
<br/>or<br/>
`npm install react-native-android-location-services-dialog-box --save`
2. Make the following additions to the given files:

**android/settings.gradle**

```gradle
include ':react-native-android-location-services-dialog-box'
project(':react-native-android-location-services-dialog-box').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-android-location-services-dialog-box/android')
```

**android/app/build.gradle**

```gradle
dependencies {
   ...
   compile project(':react-native-android-location-services-dialog-box')
}
```

**MainApplication.java**

On top, where imports are:
```java
import com.showlocationservicesdialogbox.LocationServicesDialogBoxPackage;
```

Under `protected List<ReactPackage> getPackages() {`:  
```java
  return Arrays.<ReactPackage>asList(
    new MainReactPackage(),
    new LocationServicesDialogBoxPackage() // <== this
  );
```

### Usage

```javascript
import { BackHandler, DeviceEventEmitter } from 'react-native';
import LocationServicesDialogBox from "react-native-android-location-services-dialog-box";

LocationServicesDialogBox.checkLocationServicesIsEnabled({
    message: "<h2 style='color: #0af13e'>Use Location ?</h2>This app wants to change your device settings:<br/><br/>Use GPS, Wi-Fi, and cell network for location<br/><br/><a href='#'>Learn more</a>",
    ok: "YES",
    cancel: "NO",
    enableHighAccuracy: true, // true => GPS AND NETWORK PROVIDER, false => GPS OR NETWORK PROVIDER
    showDialog: true, // false => Opens the Location access page directly
    openLocationServices: true, // false => Directly catch method is called if location services are turned off
    preventOutSideTouch: false, // true => To prevent the location services window from closing when it is clicked outside
    preventBackClick: false, // true => To prevent the location services popup from closing when it is clicked back button
    providerListener: false // true ==> Trigger locationProviderStatusChange listener when the location state changes
}).then(function(success) {
    console.log(success); // success => {alreadyEnabled: false, enabled: true, status: "enabled"}
}).catch((error) => {
    console.log(error.message); // error.message => "disabled"
});

BackHandler.addEventListener('hardwareBackPress', () => { //(optional) you can use it if you need it
   //do not use this method if you are using navigation."preventBackClick: false" is already doing the same thing.
   LocationServicesDialogBox.forceCloseDialog();
});

DeviceEventEmitter.addListener('locationProviderStatusChange', function(status) { // only trigger when "providerListener" is enabled
    console.log(status); //  status => {enabled: false, status: "disabled"} or {enabled: true, status: "enabled"}
});

componentWillUnmount() {
    // used only when "providerListener" is enabled
    LocationServicesDialogBox.stopListener(); // Stop the "locationProviderStatusChange" listener
}
```

### Configure Colors

```javascript
import LocationServicesDialogBox from "react-native-android-location-services-dialog-box";

LocationServicesDialogBox.checkLocationServicesIsEnabled({
    message: "<font color='#f1eb0a'>Use Location ?</font>",
    ok: "YES",
    cancel: "NO",
    style: { // (optional)
        backgroundColor: '#87a9ea',// (optional)
        
        positiveButtonTextColor: '#ffffff',// (optional)
        positiveButtonBackgroundColor: '#5fba7d',// (optional)
        
        negativeButtonTextColor: '#ffffff',// (optional)
        negativeButtonBackgroundColor: '#ba5f5f'// (optional)
    }
}).then(function(success) {
    console.log(success);
}).catch((error) => {
    console.log(error.message);
});

```
<img width="529" alt="screen shot 2018-07-07 at 16 58 28" src="https://user-images.githubusercontent.com/973302/42411805-3246281a-820b-11e8-862c-8d8cd12d90f3.png"/>

### Usage And Example For Async Method `ES6`

```javascript
import { BackHandler, DeviceEventEmitter } from 'react-native';
import LocationServicesDialogBox from "react-native-android-location-services-dialog-box";

export default class LocationServiceTestPage extends Component {
    constructor(props){
        super(props);
        
        this.checkIsLocation().catch(error => error);
        
        DeviceEventEmitter.addListener('locationProviderStatusChange', function(status) { // only trigger when "providerListener" is enabled
            console.log(status); //  status => {enabled: false, status: "disabled"} or {enabled: true, status: "enabled"}
        });
    }
    
    async checkIsLocation():Promise {
        let check = await LocationServicesDialogBox.checkLocationServicesIsEnabled({
            message: "Use Location ?",
            ok: "YES",
            cancel: "NO",
            enableHighAccuracy: true, // true => GPS AND NETWORK PROVIDER, false => GPS OR NETWORK PROVIDER
            showDialog: true, // false => Opens the Location access page directly
            openLocationServices: true, // false => Directly catch method is called if location services are turned off
            preventOutSideTouch: false, //true => To prevent the location services window from closing when it is clicked outside
            preventBackClick: false, //true => To prevent the location services popup from closing when it is clicked back button
            providerListener: true // true ==> Trigger "locationProviderStatusChange" listener when the location state changes
        }).catch(error => error);

        return Object.is(check.status, "enabled");
    }
    
    componentWillUnmount() {
        // used only when "providerListener" is enabled
        LocationServicesDialogBox.stopListener(); // Stop the "locationProviderStatusChange" listener
    }   
}
```

### Examples `ES6`
```javascript
import React, { Component } from 'react';
import {
    AppRegistry,
    Text,
    View,
    BackHandler,
    DeviceEventEmitter
} from 'react-native';

import LocationServicesDialogBox from "react-native-android-location-services-dialog-box";

class SampleApp extends Component {
    state = {
        initialPosition: 'unknown',
    };

    componentDidMount() {
        LocationServicesDialogBox.checkLocationServicesIsEnabled({
            message: "<h2>Use Location ?</h2>This app wants to change your device settings:<br/><br/>Use GPS, Wi-Fi, and cell network for location<br/><br/><a href='#'>Learn more</a>",
            ok: "YES",
            cancel: "NO",
            enableHighAccuracy: true, // true => GPS AND NETWORK PROVIDER, false => GPS OR NETWORK PROVIDER
            showDialog: true, // false => Opens the Location access page directly
            openLocationServices: true, // false => Directly catch method is called if location services are turned off
            preventOutSideTouch: false, //true => To prevent the location services popup from closing when it is clicked outside
            preventBackClick: false, //true => To prevent the location services popup from closing when it is clicked back button
            providerListener: true // true ==> Trigger "locationProviderStatusChange" listener when the location state changes
        }).then(function(success) {
            // success => {alreadyEnabled: true, enabled: true, status: "enabled"} 
                navigator.geolocation.getCurrentPosition((position) => {
                    let initialPosition = JSON.stringify(position);
                    this.setState({ initialPosition });
                }, error => console.log(error), { enableHighAccuracy: false, timeout: 20000, maximumAge: 1000 });
            }.bind(this)
        ).catch((error) => {
            console.log(error.message);
        });
        
        DeviceEventEmitter.addListener('locationProviderStatusChange', function(status) { // only trigger when "providerListener" is enabled
            console.log(status); //  status => {enabled: false, status: "disabled"} or {enabled: true, status: "enabled"}
        });
    }
    
    componentWillUnmount() {
        // used only when "providerListener" is enabled
        LocationServicesDialogBox.stopListener(); // Stop the "locationProviderStatusChange" listener.
    } 

    render() {
        return (
            <View>
                <Text>
                    Geolocation: {this.state.initialPosition}
                </Text>
            </View>
        );
    }
}
AppRegistry.registerComponent('SampleApp', () => SampleApp);
```

### Props

| Prop                              | Type        | Default     | Description                                                                              |
|-----------------------------------|-------------|-------------|------------------------------------------------------------------------------------------|
|`message`                          |`HTML`       |`null`       |Dialog box content text                                                                   |
|`ok`                               |`String`     |`null`       |Dialog box ok button text                                                                 |
|`cancel`                           |`String`     |`null`       |Dialog box cancel button text                                                             |
|`enableHighAccuracy` (optional)    |`Boolean`    |`true`       |Provider switch (GPS OR NETWORK OR GPS AND NETWORK)                                       |
|`showDialog` (optional)            |`Boolean`    |`true`       |Indicate whether to display the dialog box                                                |
|`openLocationServices` (optional)  |`Boolean`    |`true`       |Indicate whether to display the location services screen                                  |
|`preventOutSideTouch` (optional)   |`Boolean`    |`true`       |To prevent the location services window from closing when it is clicked outside           |
|`preventBackClick` (optional)      |`Boolean`    |`true`       |To prevent the location services popup from closing when it is clicked back button        |
|`providerListener` (optional)      |`Boolean`    |`false`      |Used to trigger `locationProviderStatusChange listener when the location state changes.  |
|`style` (optional)      |`Object`    |`{}`      |Change colors|

### Methods

| Name                               | Return             | Return Value     |
|------------------------------------|--------------------|------------------|
|`checkLocationServicesIsEnabled`    | Promise            | Object           |
|`forceCloseDialog` (optional using) | void               | -                |
|`stopListener` (optional using)     | void               | -                |
