## React Native Android Location Services Dialog Box
<img width="274px" align="right" src="https://raw.githubusercontent.com/webyonet/react-native-android-location-services-dialog-box/master/demo.gif" />

A react-native component for turn on the dialog box from android location services

[![npm version](https://badge.fury.io/js/react-native-android-location-services-dialog-box.svg)](https://badge.fury.io/js/react-native-android-location-services-dialog-box)

### Installation

#### Mostly automatic installation (recommended)

1. `npm install react-native-android-location-services-dialog-box --save`
2. `react-native link react-native-android-location-services-dialog-box`

#### Manual Installation

##### Android

1. `npm install react-native-android-location-services-dialog-box --save`
2. Make the following additions to the given files:

**android/settings.gradle**

```
include ':react-native-android-location-services-dialog-box'
project(':react-native-android-location-services-dialog-box').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-android-location-services-dialog-box/android')
```

**android/app/build.gradle**

```
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
import LocationServicesDialogBox from "react-native-android-location-services-dialog-box";

LocationServicesDialogBox.checkLocationServicesIsEnabled({
    message: "<h2>Use Location ?</h2>This app wants to change your device settings:<br/><br/>Use GPS, Wi-Fi, and cell network for location<br/><br/><a href='#'>Learn more</a>",
    ok: "YES",
    cancel: "NO"
}).then(function(success) {
    console.log(success); // success => "enabled"
}).catch((error) => {
    console.log(error.message); // error.message => "disabled"
});
```

### Usage And Example For Async Method `ES6`

```javascript
import LocationServicesDialogBox from "react-native-android-location-services-dialog-box";

export default class LocationServiceTestPage extends Component {
    constructor(props){
        super(props);
        this.checkIsLocation();
    }
    
    async checkIsLocation():Boolean {
        let check = await LocationServicesDialogBox.checkLocationServicesIsEnabled({
            message: "Use Location ?",
            ok: "YES",
            cancel: "NO"
        }).catch(error => error);

        return Object.is(check, "enabled");
    } 
}
```

### Examples `ES6`
```javascript
import React, { Component } from 'react';
import {
    AppRegistry,
    Text,
    View
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
            cancel: "NO"
        }).then(function(success) {
                navigator.geolocation.getCurrentPosition((position) => {
                    let initialPosition = JSON.stringify(position);
                    this.setState({ initialPosition });
                }, error => console.log(error), { enableHighAccuracy: false, timeout: 20000, maximumAge: 1000 });
            }.bind(this)
        ).catch((error) => {
            console.log(error.message);
        });
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

| Prop             | Type        | Description                    |
|------------------|-------------|--------------------------------|
|`message`         |`HTML`       |Dialog box content text         |
|`ok`              |`String`     |Dialog box ok button text       |
|`cancel`          |`String`     |Dialog box cancel button text   |

### Methods

| Name                               | Return             |
|------------------------------------|--------------------|
|`checkLocationServicesIsEnabled`    | Promise            |


[![NPM](https://nodei.co/npm/react-native-android-location-services-dialog-box.png?downloads=true&downloadRank=true&stars=true)](https://nodei.co/npm/react-native-android-location-services-dialog-box/)
