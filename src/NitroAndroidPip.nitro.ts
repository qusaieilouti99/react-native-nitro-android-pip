import type { HybridObject } from 'react-native-nitro-modules';

export type IPipAction = {
  /** unique identifier for the action (used by native) */
  id: string;
  /** resource name for the icon (native drawable / resource) */
  iconResourceName: string;
  /** visible title for the action button */
  title: string;
  /** accessibility description */
  contentDescription: string;
  /** callback invoked when the action is pressed in the PiP UI */
  onPress: () => void;
};

export type IAspectRatio = {
  /** width component of the aspect ratio */
  width: number;
  /** height component of the aspect ratio */
  height: number;
};

export type ISourceRectHint = {
  /** left pixel coordinate (window/screen coordinates) */
  left: number;
  /** top pixel coordinate (window/screen coordinates) */
  top: number;
  /** right pixel coordinate (window/screen coordinates) */
  right: number;
  /** bottom pixel coordinate (window/screen coordinates) */
  bottom: number;
};

export type IPipOptions = {
  /**
   * Aspect ratio for the PiP window.
   * Default: { width: 16, height: 9 } when omitted on native side.
   */
  aspectRatio?: IAspectRatio;

  /**
   * Rectangle hint for the source area on screen to use for smoother
   * PiP animations (optional).
   */
  sourceRectHint?: ISourceRectHint;

  /**
   * If true, the module will attempt to auto-enter PiP when the user leaves the activity.
   * - On Android 12+, this uses native auto-enter.
   * - On older versions it uses onUserLeaveHint fallback.
   * Default: false
   */
  autoEnterEnabled?: boolean;
};

export interface NitroAndroidPip
  extends HybridObject<{ ios: 'swift'; android: 'kotlin' }> {
  /**
   * Configure or update PiP parameters.
   * - `actions` can include up to 3 action items; their `onPress` callbacks
   *   are called by native on the main/UI thread.
   * - Passing `undefined` for options leaves native defaults in place.
   */
  setPipOptions(options?: IPipOptions, actions?: IPipAction[]): void;

  /**
   * Enter PiP mode using the last configured options.
   * Throws on native if the Activity is not able to enter PiP.
   */
  startPip(): void;

  /**
   * Exit PiP mode and bring the app to foreground.
   */
  stopPip(): void;

  /**
   * Returns whether the device supports PiP.
   */
  isPipSupported(): boolean;

  /**
   * Returns whether the app is currently in PiP mode.
   */
  isPipActive(): boolean;

  /**
   * Add a PiP state listener. This method returns void; use removePipListener()
   * to unregister the listener.
   *
   * The callback will be invoked on the main/UI thread from native.
   */
  addPipListener(callback: (isPipActive: boolean) => void): void;

  /**
   * Remove the registered PiP listener.
   */
  removePipListener(): void;
}
