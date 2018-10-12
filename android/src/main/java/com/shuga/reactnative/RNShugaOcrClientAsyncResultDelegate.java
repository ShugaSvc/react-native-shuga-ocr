package com.shuga.reactnative;

public interface RNShugaOcrClientAsyncResultDelegate {
    void onRNShugaOcrFailure(String message);
    void onRNShugaOcrSuccess(RNShugaTextBlockResultWrapper resultWrapper);
}
