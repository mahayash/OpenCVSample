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
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.InputStream;


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
                    loadImage();
                    break;
                default:
                    super.onManagerConnected(status);
            }
        }
    };

    private void loadImage() {

        try {

            //Load native opencv library
            AssetManager assetManager = getAssets();
            //InputStream inputFileStream = assetManager.open("digital_image_processing.jpg");
            InputStream inputFileStream = assetManager.open("filled_circle.png");
            Bitmap bitmap = BitmapFactory.decodeStream(inputFileStream);

            //bitmap to MAT
            Mat imgMat = new Mat();
            Utils.bitmapToMat(bitmap, imgMat);

            //Gray Color
            Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_BGR2GRAY);

            //Blur
            Imgproc.GaussianBlur(imgMat, imgMat, new Size(5, 5), 0);

            //Canny
            Imgproc.Canny(imgMat, imgMat, 75, 200);

            //Threshold
            //Imgproc.threshold(imgMat, imgMat, 0, 255, Imgproc.THRESH_OTSU);

            showImage(imgMat);


        } catch (Exception ex) {
            Log.d(TAG, "loadImage: " + ex);
        }
    }

    private void showImage(Mat matFinal) {

        Bitmap bitmapFinal = Bitmap.createBitmap(matFinal.cols(), matFinal.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(matFinal, bitmapFinal);

        imgDisplay.setImageBitmap(bitmapFinal);

    }


    //  public native void CannyEdgeDetection(long matAddrGr, long matAddrRgba);
}
