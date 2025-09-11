package com.margelo.nitro.nitroandroidpip

import android.os.Build
import android.util.Log
import com.facebook.proguard.annotations.DoNotStrip

@DoNotStrip
class NitroAndroidPip : HybridNitroAndroidPipSpec() {

    companion object {
        private const val TAG = "NitroAndroidPip"
    }

    override fun setPipOptions(options: IPipOptions?, actions: Array<IPipAction>?) {
        val list = actions?.toList()?.take(3) ?: emptyList()
        NitroAndroidPipManager.setPipOptions(options, list)
    }

    override fun startPip() {
        Log.d(TAG, "startPip() called")
        NitroAndroidPipManager.startPip()
    }

    override fun stopPip() {
        Log.d(TAG, "stopPip() called")
        NitroAndroidPipManager.stopPip()
    }

    override fun isPipSupported(): Boolean {
        return NitroAndroidPipManager.isPipSupported()
    }

    override fun isPipActive(): Boolean {
        return NitroAndroidPipManager.isPipActive()
    }

    override fun addPipListener(callback: (isPipActive: Boolean) -> Unit): Unit {
        // Wrap the JS callback to catch exceptions; the manager will invoke on main thread.
        val safeCallback: (Boolean) -> Unit = { active ->
            try {
                callback(active)
            } catch (e: Exception) {
                Log.e(TAG, "Error executing JS PiP callback", e)
            }
        }
        NitroAndroidPipManager.setListener(safeCallback)
    }

    override fun removePipListener(): Unit {
        NitroAndroidPipManager.clearListener()
    }
}
