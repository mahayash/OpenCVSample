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
import org.opencv.core.Rect;
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
import static org.opencv.imgproc.Imgproc.THRESH_OTSU;


public class OMRActivity extends AppCompatActivity {

    private ImageView imgDisplay;
    public static final String TAG = "OMR";
    private TextView txtQuestions;
    List<MatOfPoint> questContour = new ArrayList<>();

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
            InputStream inputFileStream = assetManager.open("scan_bubble_1.jpg");
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

            //Canny
            //Imgproc.Canny(threshMat, imgMat, 75, 200);

            //find question count of Threshed Image
            getQuestionCount(threshMat);

            //draw contour on canny image
            //Mat contourMat = getContour(threshMat); // don't remove

            //findCountPixel(threshMat);

            //showImage(ogImage);

        } catch (Exception ex) {
            Log.d(TAG, "loadImage: " + ex);
        }
    }

    private void getQuestionCount(Mat threshMat) {

        List<MatOfPoint> docMapContour = new ArrayList<>();

        //region

        Mat hierarchy = new Mat();
        Imgproc.findContours(threshMat, docMapContour, hierarchy,
                Imgproc.RETR_EXTERNAL, CHAIN_APPROX_SIMPLE, new Point(0, 0));

        if (docMapContour.size() > 0) {
            int counter = 0;
            for (MatOfPoint matOfPoint : docMapContour) {

                Rect rect = Imgproc.boundingRect(matOfPoint);
                double aspectRatio = (double) rect.width / rect.height;

                if (rect.width > 20 && rect.height > 20 && aspectRatio >= 0.9 && aspectRatio <= 1.1) {
                    questContour.add(matOfPoint);
                    matOfPoint.create(matOfPoint.size(), CV_8UC1);
                    findCountPixel(matOfPoint);
                }
                counter++;
            }
        }

        showImage(threshMat);
        txtQuestions.setText("Total Questions " + questContour.size());

    }

    private void getPixelCount(Mat circleImage) {

        Mat mask_inv = new Mat();
        Core.bitwise_not(circleImage, mask_inv);

        Mat result = new Mat();
        Core.bitwise_and(circleImage, circleImage, result, mask_inv);

        int totalPix = Core.countNonZero(result);
        Log.d(TAG, "totalPix: " + totalPix);

    }

    private void showImage(Mat matFinal) {

        Bitmap bitmapFinal = Bitmap.createBitmap(matFinal.cols(), matFinal.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(matFinal, bitmapFinal);

        imgDisplay.setImageBitmap(bitmapFinal);

    }

    //find & draw contour
    private Mat getContour(Mat imgMat) {

        // passed the canny image
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(imgMat, contours, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE, new Point(0, 0));

        Mat drawing = Mat.zeros(imgMat.size(), CV_8UC3);
        for (int i = 0; i < contours.size(); i++) {
            Imgproc.drawContours(drawing, contours, i, new Scalar(255, 0, 0), 2, 8, hierarchy, 0, new Point());
        }

        return drawing;
    }

    //find & draw contour
    private Mat getMaskedContour(Mat imgMat) {

        // passed the canny image
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(imgMat, contours, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE, new Point(0, 0));

        Mat drawing = Mat.zeros(imgMat.size(), CV_8UC3);
        for (int i = 0; i < contours.size(); i++) {
            Imgproc.drawContours(drawing, contours, i, new Scalar(255, 0, 0), 2, 8, hierarchy, 0, new Point());
        }

        return drawing;
    }

    private void maskedImage(Mat ogImage) {


        //Gray
        Mat grayImg = new Mat();
        Imgproc.cvtColor(ogImage, grayImg, Imgproc.COLOR_BGR2GRAY);

        //thresh
        Mat mask = new Mat();
        Imgproc.threshold(grayImg, mask, 10, 255, Imgproc.THRESH_BINARY_INV | THRESH_OTSU); // Imgproc.THRESH_BINARY_INV | THRESH_OTSU works

        Mat mask_inv = new Mat(); // give the inverse of mask
        Core.bitwise_not(mask, mask_inv);

        Mat result = new Mat();
        Core.bitwise_and(ogImage, ogImage, result, mask);

        int total = Core.countNonZero(result);
        Log.d(TAG, "Total pixel count: " + total);

        showImage(result);

    }


    private void findFilledCircle(Mat imageMat) {


        int totalPixel = imageMat.width() * imageMat.height();
        Log.d(TAG, "Total Pixel: " + totalPixel);


        //draw contour


      /*  Mat mat1 = imageMat;
        Mat mat2 = new Mat(mat1.size(), CV_8UC1, Scalar.all(0));
        Imgproc.ellipse(mat2, new Point(0, 0), mat2.size(), 0, 0, 360, Scalar.all(255), -1, 8, 0);

        Mat result = new Mat();
        Core.bitwise_and(mat1, mat2, result);
        Log.d(TAG, "result: " + result.width() * result.height());*/

    }


    private void findCountPixel(Mat threshImage) {

        /*Mat gray = new Mat();
        Imgproc.cvtColor(ogImage, gray, Imgproc.COLOR_BGR2GRAY);*/

        Mat canny = new Mat();
        Imgproc.Canny(threshImage, canny, 100, 200);

        //find contour
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(canny, contours, hierarchy, 0, 1);

        Mat mask = Mat.zeros(threshImage.rows(), threshImage.cols(), CV_8UC1);
        Imgproc.drawContours(mask, contours, -1, Scalar.all(255), -1);

        Core.bitwise_and(threshImage, threshImage, mask);

        int totalPixel = Core.countNonZero(mask);
        Log.d(TAG, "findCountPixel: " + totalPixel);

        /*//create New image
        Mat crop = new Mat(gray.rows(), gray.cols(), CV_8UC3);
        crop.setTo(new Scalar(0, 255, 0));

        ogImage.copyTo(crop, mask);*/

    }


    private void findCountPixel2(Mat ogImage) {

        Mat gray = new Mat();
        Imgproc.cvtColor(ogImage, gray, Imgproc.COLOR_BGR2GRAY);

        Mat canny = new Mat();
        Imgproc.Canny(gray, canny, 100, 200);

        //find contour
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(canny, contours, hierarchy, 0, 1);

        Mat mask = Mat.zeros(gray.rows(), gray.cols(), CV_8UC1);
        Imgproc.drawContours(mask, contours, -1, Scalar.all(255), 1);

        int totalPixel = Core.countNonZero(mask);
        Log.d(TAG, "findCountPixel: " + totalPixel);

        /*//create New image
        Mat crop = new Mat(gray.rows(), gray.cols(), CV_8UC3);
        crop.setTo(new Scalar(0, 255, 0));

        ogImage.copyTo(crop, mask);*/

        //showImage(mask);
    }

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


//Mat mask = Mat.zeros(result.rows(),result.cols(),result.type());
   /* int counter = 0;

            for(int i=0;i<questContour.size();i++){

                /*Imgproc.drawContours(threshMat, questContour, -1, Scalar.all(255), -1);
                Mat maskImage = new Mat(threshMat.size(), CV_8UC1, Scalar.all(0));
                Core.bitwise_and(threshMat, threshMat, maskImage);

                //threshMat.copyTo(threshMat, maskImage);
                int total = Core.countNonZero(maskImage);
                if (total > 0) {
                    Log.d(TAG, "loadImage: filled " + i);
                }

                Core.bitwise_and(threshMat, threshMat, mask2);
                int total = Core.countNonZero(mask2);
                if (total > 0) {
                    Log.d(TAG, "loadImage: filled " + i);
                }

        Mat mask=Mat.zeros(questContour.get(i).rows(),questContour.get(i).cols(),CV_8UC1);
        Imgproc.drawContours(mask,questContour,-1,new Scalar(255,0,0));

             Core.bitwise_and(threshMat, threshMat, mask);

                if (Core.countNonZero(mask) > 0) {
                    Log.d(TAG, "loadImage: " + counter++);
                }


        }

        /*    for (int i = 0; i < questContour.size(); i++) {

                Imgproc.drawContours(threshMat, questContour, -1, Scalar.all(255), -1);
                Mat maskImage = new Mat(threshMat.size(), CV_8UC1, Scalar.all(0));
                Core.bitwise_and(threshMat, threshMat, maskImage);

                //threshMat.copyTo(threshMat, maskImage);
                int total = Core.countNonZero(maskImage);
                if (total > 0) {
                    Log.d(TAG, "loadImage: filled " + i);
                }

                Core.bitwise_and(threshMat, threshMat, mask2);
                int total = Core.countNonZero(mask2);
                if (total > 0) {
                    Log.d(TAG, "loadImage: filled " + i);
                }

            }
*/

//endregion