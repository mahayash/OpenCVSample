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
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.imgproc.Imgproc.RETR_EXTERNAL;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.floodFill;
import static org.opencv.imgproc.Imgproc.getPerspectiveTransform;


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
            InputStream inputFileStream = assetManager.open("scan_bubble.jpg");
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
            Mat threshMat = new Mat();
            Imgproc.threshold(imgMat, threshMat, 0, 255, Imgproc.THRESH_OTSU | Imgproc.THRESH_BINARY_INV);

            //find the contour

            List<MatOfPoint> docMapContour = new ArrayList<>();
            List<MatOfPoint> questContour = new ArrayList<>();

            Imgproc.findContours(threshMat, docMapContour, imgMat,
                    Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
            if (docMapContour.size() > 0) {
                for (MatOfPoint matOfPoint : docMapContour) {
                    Rect rect = Imgproc.boundingRect(matOfPoint);
                    double aspectRatio = (double) rect.width / rect.height;
                    if (rect.width > 20 && rect.height > 20 && aspectRatio >= 0.9 && aspectRatio <= 1.1) {
                        questContour.add(matOfPoint);
                    }
                }
            }
            Log.d(TAG, "Total: " + questContour.size()); // Find total # of circles
/*
            for (int i = 0; i < questContour.size(); i++) {

                Imgproc.drawContours(threshMat, questContour, -1, Scalar.all(255), -1);
                Mat maskImage = new Mat(threshMat.size(), CV_8UC1, Scalar.all(0));
                Core.bitwise_and(threshMat, threshMat, maskImage);

                //threshMat.copyTo(threshMat, maskImage);
                int total = Core.countNonZero(maskImage);
                if (total > 0) {
                    Log.d(TAG, "loadImage: filled " + i);
                }

                *//*Core.bitwise_and(threshMat, threshMat, mask2);
                int total = Core.countNonZero(mask2);
                if (total > 0) {
                    Log.d(TAG, "loadImage: filled " + i);
                }*//*

            }*/

        /*

            maskImage.setTo(Scalar.all(155));*/

            showImage(threshMat);


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


//region

//List<MatOfPoint> docMapContour = new ArrayList<>();

// Identify contour of paper
//Imgproc.findContours(imgMat, docMapContour, imgMat, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

// Find max area
            /*if (docMapContour.size() > 0) {

                Collections.sort(docMapContour, new Comparator<MatOfPoint>() {
                    @Override
                    public int compare(MatOfPoint o1, MatOfPoint o2) {
                        return Imgproc.contourArea(o1) < Imgproc.contourArea(o2) ? -1 : (Imgproc.contourArea(o1) == Imgproc.contourArea(o2) ? 0 : 1);
                    }
                });
            }

            MatOfPoint2f paperContour = new MatOfPoint2f();

            for (MatOfPoint mapPoint : docMapContour) {

                MatOfPoint2f mapPoint2 = new MatOfPoint2f();
                mapPoint.convertTo(mapPoint2, CvType.CV_32FC2);

                double peri = Imgproc.arcLength(mapPoint2, true);

                MatOfPoint2f mapPointApprox = new MatOfPoint2f();
                Imgproc.approxPolyDP(mapPoint2, mapPointApprox, 0.02 * peri, true);

                if (mapPointApprox.size().height == 4) {
                    paperContour = mapPointApprox;
                }
            }*/

//getPerspectiveTransform(imgMat, paperContour.reshape(4, 2));


//Threshold of Wrapped Image


           /* Mat counterMat = new Mat();
            List<MatOfPoint> counterList = new ArrayList<>();
            Imgproc.findContours(imgMat, counterList, counterMat, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
            List<MatOfPoint> questionList = new ArrayList<>();*/

           /* for (MatOfPoint mapPoint : counterList) {
                Rect rect = Imgproc.boundingRect(mapPoint);
                float aspectRatio = rect.width / (rect.height);
                //if (rect.width >= 20 && rect.height >= 20 && aspectRatio >= 1.1) {
                questionList.add(mapPoint);
                //}
            }*/
//endregion