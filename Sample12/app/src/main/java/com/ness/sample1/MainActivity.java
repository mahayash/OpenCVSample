package com.ness.sample1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private Mat mRgba, mRgbaF, mRgbaT;

    private static final String TAG = MainActivity.class.getSimpleName();
    private CameraBridgeViewBase cameraBridgeViewBase;
    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        position = getIntent().getIntExtra(Constants.POSITION, 0);

        setContentView(R.layout.activity_main);

        cameraBridgeViewBase = (CameraBridgeViewBase) findViewById(R.id.main_surface);
        cameraBridgeViewBase.setVisibility(View.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);


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
                    cameraBridgeViewBase.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
            }
        }
    };

    @Override
    public void onCameraViewStarted(int width, int height) {

        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mRgbaF = new Mat(height, width, CvType.CV_8UC4);
        mRgbaT = new Mat(width, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        disableCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disableCamera();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        Mat mGray = inputFrame.gray();
        switch (position) {
            case 0:
                Gray(mGray.getNativeObjAddr(), 2000);
                return mGray;
            case 1:
                CircleDetect(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr());
                break;
            case 2:
                CannyEdgeDetection(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr());
                break;
            case 3:


                break;
        }

        return mRgba;


    }

    public native void CannyEdgeDetection(long matAddrGr, long matAddrRgba);

    public native void CircleDetect(long matAddrGr, long matAddrRgba);

    public native void Gray(long matAddrGray, int nbrElem);

    public void disableCamera() {
        if (cameraBridgeViewBase != null)
            cameraBridgeViewBase.disableView();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
