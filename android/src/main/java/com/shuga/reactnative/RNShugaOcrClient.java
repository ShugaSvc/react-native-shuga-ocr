package com.shuga.reactnative;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.util.Base64;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.List;

public class RNShugaOcrClient {
    // Method for React Native frontend, will invoke given callback upon success or error
    public void scanTextInImage(String base64String, final Callback successCallback, final Callback errorCallback) {
        byte[] bytes = Base64.decode(base64String, Base64.DEFAULT);

        //Convert to bitmap instead of calling .fromByteArray for base64 images, as for some reason, it'll throw "Invalid image data size" error
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        this.scanTextInImage(image, new RNShugaOcrClientAsyncResultDelegate() {
            @Override
            public void onRNShugaOcrFailure(String message) {
                errorCallback.invoke(message);
            }

            @Override
            public void onRNShugaOcrSuccess(RNShugaTextBlockResultWrapper resultWrapper) {
                successCallback.invoke(resultWrapper.getResultForReactNative());
            }
        });
    }

    //Method for RNCameraView to call
    public void scanTextInImage(byte[] bytes,
                                int width,
                                int height,
                                int rotation,
                                RNShugaOcrClientAsyncResultDelegate delegate) {
        int firebaseRotation = FirebaseVisionImageMetadata.ROTATION_0;
        switch (rotation) {
            case (0):
                firebaseRotation = FirebaseVisionImageMetadata.ROTATION_0;
                break;
            case (90):
                firebaseRotation = FirebaseVisionImageMetadata.ROTATION_90;
                break;
            case (180):
                firebaseRotation = FirebaseVisionImageMetadata.ROTATION_180;
                break;
            case (270):
                firebaseRotation = FirebaseVisionImageMetadata.ROTATION_270;
                break;
        }

        FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
                .setWidth(width)
                .setHeight(height)
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_YV12)
                .setRotation(firebaseRotation)
                .build();

        FirebaseVisionImage image = FirebaseVisionImage.fromByteArray(bytes, metadata);
        this.scanTextInImage(image, delegate);
    }

    private void scanTextInImage(FirebaseVisionImage image, final RNShugaOcrClientAsyncResultDelegate delegate) {
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        detector.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        RNShugaTextBlockResultWrapper resultWrapper = createResultObjectFromFirebaseVisionText(firebaseVisionText);
                        delegate.onRNShugaOcrSuccess(resultWrapper);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        delegate.onRNShugaOcrFailure(e.getMessage());
                    }
                })
        ;
    }

    /**
     * Convert FirebaseVisionText to WritableArray object for React Native to send to frontend.
     */
    public static RNShugaTextBlockResultWrapper createResultObjectFromFirebaseVisionText(FirebaseVisionText firebaseVisionText) {
        WritableArray rnResult = Arguments.createArray();

        //Holder values for later on, we can reference these values to draw bounding rectangle if needed
        float minLeft = Integer.MAX_VALUE, minTop = Integer.MAX_VALUE, maxRight = 0, maxBottom = 0;

        List<FirebaseVisionText.TextBlock> textBlocks = firebaseVisionText.getTextBlocks();
        for (FirebaseVisionText.TextBlock block : textBlocks) {
            List<FirebaseVisionText.Line> lines = block.getLines();
            for (FirebaseVisionText.Line line : lines) {
                List<FirebaseVisionText.Element> elements = line.getElements();
                for (FirebaseVisionText.Element element : elements) {
                    WritableMap elementMap = Arguments.createMap();
                    elementMap.putString("description", element.getText());
                    Point[] points = element.getCornerPoints();
                    WritableMap boundingPoly = Arguments.createMap();
                    WritableArray vertices = Arguments.createArray();
                    for (Point point : points) {
                        WritableMap vertex = Arguments.createMap();
                        vertex.putInt("x", point.x);
                        vertex.putInt("y", point.y);
                        vertices.pushMap(vertex);

                        minLeft = Math.min(minLeft, point.x);
                        minTop = Math.min(minTop, point.y);
                        maxRight = Math.max(maxRight, point.x);
                        maxBottom = Math.max(maxBottom, point.y);
                    }
                    boundingPoly.putArray("vertices", vertices);
                    elementMap.putMap("boundingPoly", boundingPoly);
                    rnResult.pushMap(elementMap);
                }
            }
        }

        return new RNShugaTextBlockResultWrapper(rnResult, minLeft, minTop, maxRight, maxBottom);
    }
}
