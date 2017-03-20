package com.ness.sample1;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.InputStream;

import static org.opencv.imgcodecs.Imgcodecs.CV_LOAD_IMAGE_COLOR;
import static org.opencv.imgcodecs.Imgcodecs.imread;


public class OMRActivity extends AppCompatActivity {

    private ImageView imgDisplay;
    public static final String TAG = "OMR";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_omr);
        InitView();
    }

    private void InitView() {
        imgDisplay = (ImageView) findViewById(R.id.img_omr_sheet);
        loadImage();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, baseLoaderCallback);
        } else {

            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {

            switch (status) {

                case LoaderCallbackInterface.SUCCESS:

                    System.loadLibrary("native-lib");
                    break;
                default:
                    super.onManagerConnected(status);
            }
        }
    };

    private void loadImage() {

        try {

            //Load native opencv library
            Uri path = Uri.parse("file:///android_asset/blank_circle_sheet.png");
            String newPath = path.toString();

            AssetManager assetManager = getAssets();
            InputStream inputFileStream = assetManager.open("blank_circle_sheet.jpg");
            Bitmap bitmap = BitmapFactory.decodeStream(inputFileStream);

            Mat imgMat = new Mat();
            Utils.bitmapToMat(bitmap, imgMat);

            CannyEdgeDetection(imgMat.getNativeObjAddr(), imgMat.getNativeObjAddr());

            Bitmap cannyBitmap = Bitmap.createBitmap(imgMat.cols(), imgMat.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(imgMat, cannyBitmap);

            imgDisplay.setImageBitmap(cannyBitmap);
          /*  Mat imgMat = imread(newPath, CV_LOAD_IMAGE_COLOR);
            Log.d(TAG, "loadImage: " + imgMat.size());

            CannyEdgeDetection(imgMat.getNativeObjAddr(), imgMat.getNativeObjAddr());
            Bitmap bitmap = Bitmap.createBitmap(imgMat.cols(), imgMat.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(imgMat, bitmap);*/

            //imgDisplay.setImageBitmap(img);
            //If the image is successfully read.
            /*if (img.size() == 0) {
            System.exit(1);
            }*/

        } catch (Exception ex) {
            Log.d(TAG, "loadImage: " + ex);
        }
    }


    public native void CannyEdgeDetection(long matAddrGr, long matAddrRgba);
}
