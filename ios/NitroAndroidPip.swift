import Foundation
import NitroModules
import OSLog

class NitroAndroidPip: HybridNitroAndroidPipSpec {

    private let logger = Logger(subsystem: "com.margelo.nitro.nitroandroidpip", category: "NitroAndroidPip")

    public func setPipOptions(options: IPipOptions?, actions: [IPipAction]?) throws -> Void {
        // iOS: Android-specific functionality; no-op
        logger.info("[NitroAndroidPip] setPipOptions called on iOS - no-op")
    }

    public func startPip() throws -> Void {
        logger.info("[NitroAndroidPip] startPip called on iOS - no-op")
    }

    public func stopPip() throws -> Void {
        logger.info("[NitroAndroidPip] stopPip called on iOS - no-op")
    }

    public func isPipSupported() throws -> Bool {
        // iOS-side of this Android-specific module: report false
        return false
    }

    public func isPipActive() throws -> Bool {
        return false
    }

    public func addPipListener(callback: @escaping (Bool) -> Void) throws -> Void {
        logger.info("[NitroAndroidPip] addPipListener called on iOS - invoking callback(false) and no-op")
        DispatchQueue.main.async {
            callback(false)
        }
    }

    public func removePipListener() throws -> Void {
        logger.info("[NitroAndroidPip] removePipListener called on iOS - no-op")
    }
}
