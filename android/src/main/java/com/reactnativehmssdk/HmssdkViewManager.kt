package com.reactnativehmssdk

import android.os.Build
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp

class HmssdkViewManager : SimpleViewManager<HmsView>() {

  private var reactContext: ThemedReactContext? = null

  override fun getName(): String {
    return REACT_CLASS
  }

  public override fun createViewInstance(reactContext: ThemedReactContext): HmsView {
    this.reactContext = reactContext
    return HmsView(reactContext)
  }

  override fun getExportedCustomBubblingEventTypeConstants(): MutableMap<String, Any>? {
    return MapBuilder.builder<String, Any>()
        .put(
            "topChange",
            MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onChange"))
        )
        .build()
  }

  override fun getExportedCustomDirectEventTypeConstants(): MutableMap<String, Any>? {
    return MapBuilder.of(
        "captureFrame",
        MapBuilder.of("registrationName", "onDataReturned"),
    )
  }

  @RequiresApi(Build.VERSION_CODES.N)
  override fun receiveCommand(@NonNull root: HmsView, commandId: String?, args: ReadableArray?) {
    when (commandId) {
      "capture" -> root.captureHmsView(args)
    }
  }

  @RequiresApi(Build.VERSION_CODES.N)
  override fun receiveCommand(@NonNull root: HmsView, commandId: Int, args: ReadableArray?) {
    when (commandId) {
      1 -> root.captureHmsView(args)
    }
  }

  @Nullable
  override fun getCommandsMap(): Map<String, Int>? {
    return MapBuilder.of("capture", 1)
  }

  @ReactProp(name = "data")
  fun setData(view: HmsView, data: ReadableMap) {
    val trackId = data.getString("trackId")
    val id = data.getString("id")
    val mirror = data.getBoolean("mirror")
    val hmsCollection = getHms()
    if (hmsCollection != null) {
      view.setData(id, trackId, hmsCollection, mirror)
    }
  }

  @ReactProp(name = "scaleType")
  fun setScaleType(view: HmsView, data: String?) {
    view.updateScaleType(data)
  }

  @ReactProp(name = "setZOrderMediaOverlay")
  fun setZOrderMediaOverlay(view: HmsView, data: Boolean?) {
    view.updateZOrderMediaOverlay(data)
  }

  private fun getHms(): MutableMap<String, HmsSDK>? {
    return reactContext?.getNativeModule(HmsModule::class.java)?.getHmsInstance()
  }

  companion object {
    const val REACT_CLASS = "HmsView"
  }
}
