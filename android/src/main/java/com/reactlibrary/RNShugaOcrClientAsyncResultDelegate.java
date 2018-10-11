package com.reactlibrary;

public interface RNShugaOcrClientAsyncResultDelegate {
    void onRNShugaOcrFailure(String message);
    void onRNShugaOcrSuccess(RNShugaTextBlockResultWrapper resultWrapper);
}
