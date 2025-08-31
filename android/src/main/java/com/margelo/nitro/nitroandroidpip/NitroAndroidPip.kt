package com.margelo.nitro.nitroandroidpip

import com.facebook.proguard.annotations.DoNotStrip

/**
 * The concrete implementation of the HybridNitroAndroidPipSpec.
 * This class acts as a bridge, delegating all calls to the singleton NitroAndroidPipManager.
 */
@DoNotStrip
class NitroAndroidPip : HybridNitroAndroidPipSpec() {

    override fun setPipOptions(options: IPipOptions?, actions: Array<IPipAction>?) {
        // Convert the nullable Array from the bridge to a non-null List for the manager.
        val actionsList = actions?.toList() ?: emptyList()
        NitroAndroidPipManager.setPipOptions(options, actionsList)
    }

    override fun startPip() {
        NitroAndroidPipManager.startPip()
    }

    override fun stopPip() {
        NitroAndroidPipManager.stopPip()
    }

    override fun isPipSupported(): Boolean {
        return NitroAndroidPipManager.isPipSupported()
    }

    override fun isPipActive(): Boolean {
        return NitroAndroidPipManager.isPipActive()
    }

    override fun addPipListener(callback: (isPipActive: Boolean) -> Unit): () -> Unit {
        NitroAndroidPipManager.setListener(callback)
        // Return the cleanup function that will be called when the listener is removed in JS.
        return {
            NitroAndroidPipManager.clearListener()
        }
    }
}
