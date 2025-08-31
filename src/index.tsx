import { NitroModules } from 'react-native-nitro-modules';
import type {
  NitroAndroidPip,
  IPipAction,
  IAspectRatio,
  ISourceRectHint,
  IPipOptions, // Use the new IPipOptions type
} from './NitroAndroidPip.nitro';

// Create the hybrid object. This remains the same.
const NitroAndroidPipHybridObject =
  NitroModules.createHybridObject<NitroAndroidPip>('NitroAndroidPip');

// --- New Declarative API Functions ---

/**
 * Configures or updates the Picture-in-Picture parameters.
 * Call this when a feature (like a video call) starts to "prime" the PiP settings.
 * If PiP is already active, calling this will update the window's appearance and actions live.
 *
 * @param options The visual options for the PiP window (aspect ratio, auto-enter, etc.).
 * @param actions An array of up to 3 actions (buttons) to display in the PiP window.
 */
export const setPipOptions = (
  options?: IPipOptions,
  actions?: IPipAction[]
): void => {
  return NitroAndroidPipHybridObject.setPipOptions(options, actions);
};

/**
 * Manually enters Picture-in-Picture mode using the last configured options
 * set by `setPipOptions`.
 */
export const startPip = (): void => {
  return NitroAndroidPipHybridObject.startPip();
};

/**
 * Manually exits Picture-in-Picture mode by bringing the app to the foreground.
 */
export const stopPip = (): void => {
  return NitroAndroidPipHybridObject.stopPip();
};

/**
 * Checks if the device supports Picture-in-Picture.
 */
export const isPipSupported = (): boolean => {
  return NitroAndroidPipHybridObject.isPipSupported();
};

/**
 * Checks if the app is currently in Picture-in-Picture mode.
 */
export const isPipActive = (): boolean => {
  return NitroAndroidPipHybridObject.isPipActive();
};

/**
 * Adds a listener to be notified of PiP mode changes (e.g., when the user
 * closes the PiP window manually).
 * @returns A function to remove the listener.
 */
export const addPipListener = (
  callback: (isPipActive: boolean) => void
): (() => void) => {
  return NitroAndroidPipHybridObject.addPipListener(callback);
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
  IPipOptions, // Export the new options type
};

// Default export for convenience, containing all the new functions
export default {
  setPipOptions,
  startPip,
  stopPip,
  isPipSupported,
  isPipActive,
  addPipListener,
  NitroAndroidPipHybridObject,
};
