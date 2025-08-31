import Foundation
import NitroModules

class NitroAndroidPip: HybridNitroAndroidPipSpec {

    public func startPip(options: IStartPipOptions?) throws -> Void {
        // iOS implementation not available - PiP functionality is Android-specific
        print("[NitroAndroidPip] startPip called on iOS - no-op")
    }

    public func stopPip() throws -> Void {
        // iOS implementation not available - PiP functionality is Android-specific
        print("[NitroAndroidPip] stopPip called on iOS - no-op")
    }

    public func isPipSupported() throws -> Bool {
        // PiP is not supported on iOS through this Android-specific module
        return false
    }

    public func isPipActive() throws -> Bool {
        // PiP is never active on iOS through this Android-specific module
        return false
    }

    public func addPipListener(callback: @escaping (_ isPipActive: Bool) -> Void) throws -> () -> Void {
        print("[NitroAndroidPip] addPipListener called on iOS - callback invoked with false")
        return { }
    }

    public func updatePip(options: IStartPipOptions) throws -> Void {
        // iOS implementation not available - PiP functionality is Android-specific
        print("[NitroAndroidPip] updatePip called on iOS - no-op")
    }
}
