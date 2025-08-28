import { NitroModules } from 'react-native-nitro-modules';
import type { NitroAndroidPip } from './NitroAndroidPip.nitro';

const NitroAndroidPipHybridObject =
  NitroModules.createHybridObject<NitroAndroidPip>('NitroAndroidPip');

export function multiply(a: number, b: number): number {
  return NitroAndroidPipHybridObject.multiply(a, b);
}
