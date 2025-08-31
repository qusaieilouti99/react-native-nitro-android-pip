import type { HybridObject } from 'react-native-nitro-modules';

export type IPipAction = {
  id: string;
  iconResourceName: string;
  title: string;
  contentDescription: string;
  onPress: () => void;
};

export type IAspectRatio = {
  width: number;
  height: number;
};

export type ISourceRectHint = {
  left: number;
  top: number;
  right: number;
  bottom: number;
};

export type IPipOptions = {
  /**
   * Aspect ratio for the PiP window.
   * Default: { width: 16, height: 9 }
   */
  aspectRatio?: IAspectRatio;

  /**
   * Defines which part of your screen should be visible in PiP for smoother animations.
   */
  sourceRectHint?: ISourceRectHint;

  /**
   * If true, the library will automatically enter PiP when the user navigates away.
   * - On Android 12+, this uses the native `autoEnterEnabled` system feature.
   * - On older versions, this uses the `onUserLeaveHint` callback as a reliable fallback.
   * You must still forward the `onUserLeaveHint` event from your MainActivity for this to work.
   * Default: false
   */
  autoEnterEnabled?: boolean;
};

export interface NitroAndroidPip
  extends HybridObject<{ ios: 'swift'; android: 'kotlin' }> {
  /**
   * Configures or updates the Picture-in-Picture parameters.
   * Call this when a call starts to "prime" the PiP settings.
   * If PiP is already active, calling this will update the window's appearance and actions live.
   * @param options The visual options for the PiP window.
   * @param actions An array of up to 3 actions (buttons) to display in the PiP window.
   */
  setPipOptions(options?: IPipOptions, actions?: IPipAction[]): void;

  /**
   * Manually enters Picture-in-Picture mode using the last configured options.
   * Throws an error if the activity is not in a state where PiP can be entered.
   */
  startPip(): void;

  /**
   * Manually exits Picture-in-Picture mode by bringing the app to the foreground.
   */
  stopPip(): void;

  /**
   * Checks if the device supports Picture-in-Picture.
   */
  isPipSupported(): boolean;

  /**
   * Checks if the app is currently in Picture-in-Picture mode.
   */
  isPipActive(): boolean;

  /**
   * Adds a listener to be notified of PiP mode changes.
   * @returns A function to remove the listener.
   */
  addPipListener(callback: (isPipActive: boolean) => void): () => void;
}
