package com.reactlibrary;
import com.facebook.react.bridge.WritableArray;

public class RNShugaTextBlockResultWrapper {
    private WritableArray resultForReactNative;
    private float minLeft;
    private float minTop;
    private float maxRight;
    private float maxBottom;

    public RNShugaTextBlockResultWrapper(WritableArray resultForReactNative, float minLeft, float minTop, float maxRight, float maxBottom) {
        this.resultForReactNative = resultForReactNative;
        this.minLeft = minLeft;
        this.minTop = minTop;
        this.maxRight = maxRight;
        this.maxBottom = maxBottom;
    }

    public WritableArray getResultForReactNative() {
        return this.resultForReactNative;
    }

    public float getMinLeft() {
        return minLeft;
    }

    public float getMinTop() {
        return minTop;
    }

    public float getMaxRight() {
        return maxRight;
    }

    public float getMaxBottom() {
        return maxBottom;
    }
}
