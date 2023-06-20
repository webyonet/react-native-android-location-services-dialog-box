import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  checkLocationServicesIsEnabled: (config:Object) => Promise<{ status:string, enabled:boolean, alreadyEnabled:boolean }>;
  forceCloseDialog: () => void;
  stopListener: () => void;
}

export default TurboModuleRegistry.getEnforcing<Spec>('LocationServicesDialogBox');