import { NitroModules } from 'react-native-nitro-modules';
import type {
  NitroAndroidPip,
  IPipAction,
  IAspectRatio,
  ISourceRectHint,
  IPipOptions,
} from './NitroAndroidPip.nitro';

// Create the hybrid object. This remains the same.
const NitroAndroidPipHybridObject =
  NitroModules.createHybridObject<NitroAndroidPip>('NitroAndroidPip');

/**
 * Configure or update Picture-in-Picture parameters.
 * - Call when a feature (e.g., video call) starts to prime PiP settings.
 * - If PiP is active, calling this will update the PiP UI and actions live.
 *
 * @param options Visual options (aspect ratio, source rect hint, auto-enter, etc.)
 * @param actions Up to 3 actions to show in the PiP window (each may contain an onPress callback).
 */
export const setPipOptions = (
  options?: IPipOptions,
  actions?: IPipAction[]
): void => {
  NitroAndroidPipHybridObject.setPipOptions(options, actions);
};

/**
 * Manually enter Picture-in-Picture using the last configured options.
 * Throws (on native side) if the activity cannot enter PiP.
 */
export const startPip = (): void => {
  NitroAndroidPipHybridObject.startPip();
};

/**
 * Manually exit Picture-in-Picture and bring the app to foreground.
 */
export const stopPip = (): void => {
  NitroAndroidPipHybridObject.stopPip();
};

/**
 * Returns true if the device and OS support Picture-in-Picture.
 */
export const isPipSupported = (): boolean => {
  return NitroAndroidPipHybridObject.isPipSupported();
};

/**
 * Returns true when the app is currently in Picture-in-Picture mode.
 */
export const isPipActive = (): boolean => {
  return NitroAndroidPipHybridObject.isPipActive();
};

/**
 * Register a listener that will be called when PiP mode changes (true = active, false = not active).
 * - Per the spec this function returns void; call removePipListener() to stop listening.
 * - The callback will be invoked on the main/UI thread from native.
 *
 * @param callback Invoked with the current isPipActive state.
 */
export const addPipListener = (
  callback: (isPipActive: boolean) => void
): void => {
  NitroAndroidPipHybridObject.addPipListener(callback);
};

/**
 * Remove the previously registered PiP listener.
 * Call this when the JS component unmounts (or on logout) to avoid receiving further events.
 */
export const removePipListener = (): void => {
  NitroAndroidPipHybridObject.removePipListener();
};

// --- Exports ---

// Export the hybrid object for advanced usage if needed
export { NitroAndroidPipHybridObject };

// Export all relevant types for consumers of the library
export type {
  NitroAndroidPip,
  IPipAction,
  IAspectRatio,
  ISourceRectHint,
  IPipOptions,
};

// Convenience default export (named exports are preferred)
export default {
  setPipOptions,
  startPip,
  stopPip,
  isPipSupported,
  isPipActive,
  addPipListener,
  removePipListener,
  NitroAndroidPipHybridObject,
};
