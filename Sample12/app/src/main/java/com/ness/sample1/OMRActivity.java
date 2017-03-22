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
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static org.opencv.imgproc.Imgproc.RETR_EXTERNAL;
import static org.opencv.imgproc.Imgproc.contourArea;
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

            List<MatOfPoint> docMapContour = new ArrayList<>();

            // Identify contour of paper
            Imgproc.findContours(imgMat, docMapContour, imgMat, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            // Find max area
            if (docMapContour.size() > 0) {

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
            }

            //getPerspectiveTransform(imgMat, paperContour.reshape(4, 2));


            //Threshold of Wrapped Image
            Imgproc.threshold(paperContour, imgMat, 0, 255, Imgproc.THRESH_OTSU);

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

    private void four_point_tranformation() {

    }
}
