import Foundation
import NitroModules

class NitroAndroidPip: HybridNitroAndroidPipSpec {

    public func setPipOptions(options: IPipOptions?, actions: [IPipAction]?) throws -> Void {
        // iOS implementation not available - PiP functionality is Android-specific
        print("[NitroAndroidPip] setPipOptions called on iOS - no-op")
    }

    public func startPip() throws -> Void {
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
        // The callback is not stored or invoked later as PiP is not active on iOS
        return { }
    }
}
