import { NativeModules, Platform } from 'react-native';
const LocationServicesDialogBox = Platform.OS === 'android' ?
    NativeModules.LocationServicesDialogBox :
    {
        checkLocationServicesIsEnabled: () => Promise.resolve({
            alreadyEnabled: true,
            enabled: true,
            status: "enabled"
        }),
        forceCloseDialog: () => {}
    }
export default LocationServicesDialogBox;
