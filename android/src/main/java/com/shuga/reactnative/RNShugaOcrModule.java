package com.shuga.reactnative;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

public class RNShugaOcrModule extends ReactContextBaseJavaModule {
  private RNShugaOcrClient rnShugaOcrClient;
  private final ReactApplicationContext reactContext;

  public RNShugaOcrModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    this.rnShugaOcrClient = new RNShugaOcrClient();
  }

  @Override
  public String getName() {
    return "RNShugaOcr";
  }

  @ReactMethod
  public void scanTextInImage(String data, Callback successCallback, Callback errorCallback) {
    rnShugaOcrClient.scanTextInImage(data, successCallback, errorCallback);
  }
}