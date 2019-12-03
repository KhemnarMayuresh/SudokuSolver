package com.mit.miniproject.sudokusolver;


import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.mit.miniproject.sudokusolver.imagerec.DigitRecognizer;
import com.mit.miniproject.sudokusolver.imagerec.PortraitCameraView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ShowCameraActivity extends AppCompatActivity implements CvCameraViewListener2 {

    //Dialog
    ProgressDialog pd;

    // Used for logging success or failure messages
    private static final String TAG = "OCVSample::Activity";

    // Loads camera view of OpenCV for us to use. This lets us see using OpenCV
    //private CameraBridgeViewBase mOpenCvCameraView;
    private PortraitCameraView mOpenCvCameraView;

    private boolean mIsJavaCamera = true;
    private MenuItem mItemSwitchCamera = null;

    private Mat mRgba;
    private Mat mIntermediateMat;
    private Mat mGray;
    Mat hierarchy;

    Mat cropped;
    TessBaseAPI tessBaseApi;

    private static final String lang = "eng";
    private static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/SudokuSolver/";

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public ShowCameraActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_show_camera);

        mOpenCvCameraView = findViewById(R.id.camera);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

        pd=new ProgressDialog(this);
        pd.setTitle("Processing Image");
        pd.setMessage("Loading.....");
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_10, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
        hierarchy = new Mat();
    }

    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
        mIntermediateMat.release();
        hierarchy.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        Mat grayMat= inputFrame.gray();
        Mat blurMat = new Mat();
        Imgproc.GaussianBlur(grayMat, blurMat, new Size(5,5), 0);
        Mat thresh = new Mat();
        Imgproc.adaptiveThreshold(blurMat, thresh, 255,1,1,11,2);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hier = new Mat();
        Imgproc.findContours(thresh, contours, hier, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        hier.release();

        MatOfPoint2f biggest = new MatOfPoint2f();
        double max_area = 0;
        for (MatOfPoint i : contours) {
            double area = Imgproc.contourArea(i);
            if (area > 100) {
                MatOfPoint2f m = new MatOfPoint2f(i.toArray());
                double peri = Imgproc.arcLength(m, true);
                MatOfPoint2f approx = new MatOfPoint2f();
                Imgproc.approxPolyDP(m, approx, 0.02 * peri, true);
                if (area > max_area && approx.total() == 4) {
                    biggest = approx;
                    max_area = area;
                }
            }
        }

        // find the outer box
        Mat displayMat = inputFrame.rgba();
        Point[] points = biggest.toArray();
        cropped = new Mat();
        int t = 3;
        if (points.length >= 4) {
            // draw the outer box
            Core.line(displayMat, new Point(points[0].x, points[0].y), new Point(points[1].x, points[1].y), new Scalar(255, 0, 0), 2);
            Core.line(displayMat, new Point(points[1].x, points[1].y), new Point(points[2].x, points[2].y), new Scalar(255, 0, 0), 2);
            Core.line(displayMat, new Point(points[2].x, points[2].y), new Point(points[3].x, points[3].y), new Scalar(255, 0, 0), 2);
            Core.line(displayMat, new Point(points[3].x, points[3].y), new Point(points[0].x, points[0].y), new Scalar(255, 0, 0), 2);
            // crop the image
            Rect R = new Rect(new Point(points[0].x - t, points[0].y - t), new Point(points[2].x + t, points[2].y + t));
            if (displayMat.width() > 1 && displayMat.height() > 1) {
                cropped = new Mat(displayMat, R);
            }
        }

        return displayMat;
    }

    public void capture(View v) {
        pd.show();

        if (cropped.width() < 1 || cropped.height() < 1) {
            finish();
        }

        mOpenCvCameraView.setVisibility(View.GONE);
        ImageView iv = findViewById(R.id.solve_img);
        iv.setVisibility(View.VISIBLE);

        // initialize the TessBase
        tessBaseApi = new TessBaseAPI();
        tessBaseApi.init(DATA_PATH, lang);
        tessBaseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK);
        tessBaseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "1234567890");
        tessBaseApi.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!@#$%^&*()_+=-qwertyuiop[]}{POIU" +
                "YTREWQasdASDfghFGHjklJKLl;L:'\"\\|~`xcvXCVbnmBNM,./<>?");
        tessBaseApi.setVariable("classify_bin_numeric_mode", "1");

        Mat output = cropped.clone();

        int SUDOKU_SIZE = 9;
        int IMAGE_WIDTH = output.width();
        int IMAGE_HEIGHT = output.height();
        double PADDING = IMAGE_WIDTH/20;
        int HSIZE = IMAGE_HEIGHT/SUDOKU_SIZE;
        int WSIZE = IMAGE_WIDTH/SUDOKU_SIZE;
        DigitRecognizer digitRecognizer = new DigitRecognizer();
        digitRecognizer.ReadMNISTData();

        int[][] sudos = new int[SUDOKU_SIZE][SUDOKU_SIZE];

        // Divide the image to 81 small grid and do the digit recognition
        for (int y = 0, iy = 0; y < IMAGE_HEIGHT - HSIZE ; y+= HSIZE,iy++) {
            for (int x = 0, ix = 0; x < IMAGE_WIDTH - WSIZE; x += WSIZE, ix++) {
                sudos[iy][ix] = 0;
                int cx = (x + WSIZE / 2);
                int cy = (y + HSIZE / 2);
                Point p1 = new Point(cx - PADDING, cy - PADDING);
                Point p2 = new Point(cx + PADDING, cy + PADDING);

                //for correct 9*9 matrix block use this code
//                int cx = x ;
//                int cy = y;
//                Point p1 = new Point(cx , cy );
//                Point p2 = new Point(cx+WSIZE , cy +HSIZE);

                Rect R = new Rect(p1, p2);
                Mat digit_cropped = new Mat(output, R);
                Imgproc.GaussianBlur(digit_cropped,digit_cropped,new Size(5,5),0);//to reduce noise
                Core.rectangle(output, p1, p2, new Scalar(0, 0, 0));
                Bitmap digit_bitmap = Bitmap.createBitmap(digit_cropped.cols(), digit_cropped.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(digit_cropped, digit_bitmap);

                tessBaseApi.setImage(digit_bitmap);
                String recognizedText = tessBaseApi.getUTF8Text();
                if (recognizedText.length() == 1) {
                    sudos[iy][ix] = Integer.valueOf(recognizedText);

                }
                //Core.putText(output, recognizedText, new Point(cx, cy), 1, 3.0f, new Scalar(0));//rewrite read numbers on the bitmap
                tessBaseApi.clear();
            }
            Log.i("testing",""+ Arrays.toString(sudos[iy]));
        }

        //Imgproc.cvtColor(output, output, Imgproc.COLOR_GRAY2RGBA);

        tessBaseApi.end();


        // Copy the captured array
        int[][] test_sudo = Arrays.copyOf(sudos, sudos.length);

        // Testing data
        //int[][] test_sudo = {{5,3,0,0,7,0,0,0,0}, {6,0,0,1,9,5,0,0,0}, {0,9,8,0,0,0,0,6,0},
         //       {8,0,0,0,6,0,0,0,3}, {4,0,0,8,0,3,0,0,1}, {7,0,0,0,2,0,0,0,6}, {0,6,0,0,0,0,2,8,0}, {0,0,0,4,1,9,0,0,5}, {0,0,0,0,8,0,0,7,9}};


        // make a copy of the captured array
        int[][] temp = new int[9][9];
        for (int i = 0; i < 9; i++) {
            for (int y = 0; y < 9; y++) {
                temp[i][y] = test_sudo[i][y];
            }
            Log.i("Temp data "," "+ Arrays.toString(temp[i]));
        }

        // Solve the puzzle
        Solver solver = new Solver(test_sudo, this);
        int[][] result = solver.mainSolver();


        // Print the result to screen
        for (int y = 0, iy = 0; y < IMAGE_HEIGHT - HSIZE ; y+= HSIZE,iy++) {
            for (int x = 0, ix = 0; x < IMAGE_WIDTH - WSIZE; x += WSIZE, ix++) {
                if (temp[iy][ix]==0) {
                    int cx = (x + WSIZE / 2);
                    int cy = (y + HSIZE / 2);
                    Point p = new Point(cx, cy);
                    Core.putText(output, result[iy][ix]+"", p, 1, 3.0f, new Scalar(255));
                }
            }
        }


        // Display the image
        Bitmap b = Bitmap.createBitmap(output.cols(), output.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(output, b);
        iv.setImageBitmap(b);


        findViewById(R.id.captureButton).setVisibility(View.INVISIBLE);
        findViewById(R.id.SaveImage).setVisibility(View.VISIBLE);

        pd.dismiss();

    }

    //code to save image
    public void save(View v)
    {
        ImageView iv = findViewById(R.id.solve_img);
        //to get the image from the ImageView (say iv)
        BitmapDrawable draw = (BitmapDrawable) iv.getDrawable();
        Bitmap bitmap = draw.getBitmap();

        FileOutputStream outStream = null;
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + "/SudokuSolver");
        dir.mkdirs();
        String fileName = String.format("%d.jpg", System.currentTimeMillis());
        File outFile = new File(dir, fileName);
        try {
            outStream = new FileOutputStream(outFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
        try {
            outStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Image Saved Sucessfully", Toast.LENGTH_SHORT).show();
    }



    /** Soduku Solver */
    private class Solver{

        int[][] puzzle;
        Context context;
        public Solver(int[][] puzzle, Context context) {
            this.puzzle = puzzle;
            this.context = context;
        }

        public int check (int row, int col, int num){

            int rowStart = (row / 3) * 3;
            int colStart = (col / 3) * 3;
            int i;
            for (i = 0; i < 9; i++) {
                if (puzzle[row][i] == num) {
                    return 0;
                }
                if (puzzle[i][col] == num) {
                    return 0;
                }
                if (puzzle[rowStart + (i % 3)][colStart + (i / 3)] == num) {
                    return 0;
                }
            }
            return 1;
        }



        public int solve(int row, int col) {
            if (row < 9 && col < 9) {
                if (puzzle[row][col] != 0) {
                    if ((col + 1) < 9)
                        return solve(row, col + 1);
                    else if ((row + 1) < 9)
                        return solve(row + 1, 0);
                    else
                        return 1;
                } else {
                    for (int i = 0; i < 9; i++) {
                        if (check(row, col, i + 1) == 1) {
                            puzzle[row][col] = i + 1;
                            if (solve(row, col) == 1)
                                return 1;
                            else
                                puzzle[row][col] = 0;
                        }
                    }
                }
                return 0;
            } else return 1;
        }

        public int[][] mainSolver() {
            int[][] result = new int[9][9];

            if (solve(0, 0) == 1) {
                for (int i = 0; i < 9; i++) {
                    for (int j = 0; j < 9; j++) {
                        result[i][j] = puzzle[i][j];
                    }
                }
                String s="";
                for (int i = 0; i < 9; i++) {
                    s = s + Arrays.toString(puzzle[i]) + " \n";
                }
//                Toast toast = Toast.makeText(context, s, Toast.LENGTH_LONG);
//                toast.show();

            } else {
                Toast toast = Toast.makeText(context, "Not Valid!", Toast.LENGTH_SHORT);
                toast.show();
            }


            return puzzle;
        }
    }
}