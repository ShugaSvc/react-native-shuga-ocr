package com.reactlibrary;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
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
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.List;

public class RNShugaOcrClient {
  public void scanTextInImage(String data, final Callback successCallback, final Callback errorCallback) {
    byte[] bytes = Base64.decode(data, Base64.DEFAULT);

    //Convert to bitmap instead of calling .fromByteArray as for some reason, it'll throw "Invalid image data size" error
    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

    FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
    detector.processImage(image)
      .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
        @Override
        public void onSuccess(FirebaseVisionText firebaseVisionText) {
          WritableArray result = Arguments.createArray();

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
                }
                boundingPoly.putArray("vertices", vertices);
                elementMap.putMap("boundingPoly", boundingPoly);
                result.pushMap(elementMap);
              }
            }
          }

          successCallback.invoke(result);
        }
      })
      .addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
          errorCallback.invoke(e.getMessage());
        }
      })
    ;
  }
}
