package com.margelo.nitro.nitroandroidpip
  
import com.facebook.proguard.annotations.DoNotStrip

@DoNotStrip
class NitroAndroidPip : HybridNitroAndroidPipSpec() {
  override fun multiply(a: Double, b: Double): Double {
    return a * b
  }
}
