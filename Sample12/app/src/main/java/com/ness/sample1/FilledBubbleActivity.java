package com.ness.sample1;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.core.CvType.CV_8UC3;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.RETR_TREE;

public class FilledBubbleActivity extends AppCompatActivity {

    private ImageView imgDisplay;
    public static final String TAG = "filledBubble";
    private TextView txtQuestions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_omr);
        InitView();
    }


    private void InitView() {
        imgDisplay = (ImageView) findViewById(R.id.img_omr_sheet);
        txtQuestions = (TextView) findViewById(R.id.txt_total_questions);
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
            InputStream inputFileStream = assetManager.open("scan_bubble_1_2.jpg");
            Bitmap bitmap = BitmapFactory.decodeStream(inputFileStream);

            Mat ogImage = new Mat();
            Utils.bitmapToMat(bitmap, ogImage);

            //bitmap to MAT
            Mat imgMat = new Mat();
            Utils.bitmapToMat(bitmap, imgMat);

            //Gray Color
            Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_BGR2GRAY);

            //Blur
            Imgproc.GaussianBlur(imgMat, imgMat, new Size(5, 5), 0);

            //Threshold
            Mat threshMat = new Mat();
            Imgproc.threshold(imgMat, threshMat, 0, 255, Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU);

            //showImage(threshMat);
            showImage(getContour(threshMat));

        } catch (Exception ex) {
            Log.d(TAG, "loadImage: " + ex);
        }
    }


    //find & draw contour
    private Mat getContour(Mat imgMat) {

        // passed the canny image
        List<MatOfPoint> contours = new ArrayList<>();

        Mat hierarchy = new Mat();
        Imgproc.findContours(imgMat, contours, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE, new Point(0, 0));
        Mat drawing = Mat.zeros(imgMat.size(), CV_8UC3);

        for (int i = 0; i < contours.size(); i++) {
            Imgproc.drawContours(drawing, contours, i, new Scalar(255, 0, 0), 2, -1, hierarchy, -1, new Point());
        }

        return drawing;
    }

    /*
    TODO :
    If current totalPixel count has larger number of "total non-zero" pixel in a row, then that particular
    bubble is marked by the student
    */

    private void getPixelCount(Mat circleImage) {

        Mat mask_inv = new Mat();
        Core.bitwise_not(circleImage, mask_inv);

        Mat result = new Mat();
        Core.bitwise_and(circleImage, circleImage, result, mask_inv);

        int totalPix = Core.countNonZero(result);
        Log.d(TAG, "totalPix: " + totalPix);

        showImage(result);
    }

    private void showImage(Mat matFinal) {

        Bitmap bitmapFinal = Bitmap.createBitmap(matFinal.cols(), matFinal.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(matFinal, bitmapFinal);

        imgDisplay.setImageBitmap(bitmapFinal);

    }
}
