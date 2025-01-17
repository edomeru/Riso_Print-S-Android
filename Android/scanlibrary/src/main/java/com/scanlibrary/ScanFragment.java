package com.scanlibrary;

import android.app.Activity;
// aLINK edit - Start
// android.app.Fragment was deprecated in API level 28
// Use androidx.fragment.app.Fragment instead
import androidx.fragment.app.Fragment;
// android.app.FragmentManager was deprecated in API level 28
// Use androidx.fragment.app.FragmentManager instead
import androidx.fragment.app.FragmentManager;

// androidx.fragment.app.Fragment.onAttach(Activity activity) is deprecated
// Use androidx.fragment.app.Fragment.onAttach(Context context) instead
import android.content.Context;
// aLINK edit - End
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
// aLINK edit - Start
// android.os.AsyncTask was deprecated in API level 30.
// Use threading instead
// aLINK edit - End
import android.os.Build; // aLINK edit: Android 13 New OS Support: Deprecation fixes
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.scanlibrary.Utils.isChromeBook;

/**
 * Created by jhansi on 29/03/15.
 */
public class ScanFragment extends Fragment {

    private Button scanButton;
    private ImageView sourceImageView;
    private FrameLayout sourceFrame;
    private PolygonView polygonView;
    private View view;
    private ProgressDialogFragment progressDialogFragment;
    private IScanner scanner;
    private Bitmap original;

    // aLINK edit - Start
    // androidx.fragment.app.Fragment.onAttach(Activity activity) is deprecated
    // Use androidx.fragment.app.Fragment.onAttach(Context context) instead
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof IScanner)) {
            throw new ClassCastException("Activity must implement IScanner");
        }
        this.scanner = (IScanner) context;
    }
    // aLINK edit - End

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.scan_fragment_layout, null);
        init();
        return view;
    }

    // aLINK edit: RM#912 for chromebook, reset the fragment when configuration changes
    // reset of fragment is needed because layout need to adjust to new config correctly
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (Utils.isChromeBook(getActivity())) {
            final FragmentManager fm = getFragmentManager();
            if (this == fm.findFragmentById(R.id.content)) {
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        ScanFragment newScanFragment = new ScanFragment();
                        newScanFragment.passBitmap(original);
                        fm.beginTransaction()
                                .replace(R.id.content, newScanFragment)
                                .commit();
                    }
                });
            }
        }
    }

    public ScanFragment() {

    }

    // aLINK edit: RM#912 for chromebook, for passing the bitmap when resetting the fragment
    public void passBitmap(Bitmap bitmap) {
        original = bitmap;
    }

    private void init() {
        // aLINK edit: RM#907 for chromebook, set to portrait only after photo is captured
        // aLINK edit: RM#912 must be set to portrait here so orientation is also updated on fragment reset
        if (isChromeBook(getActivity())) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        sourceImageView = (ImageView) view.findViewById(R.id.sourceImageView);
        scanButton = (Button) view.findViewById(R.id.scanButton);
        scanButton.setOnClickListener(new ScanButtonClickListener());
        sourceFrame = (FrameLayout) view.findViewById(R.id.sourceFrame);
        polygonView = (PolygonView) view.findViewById(R.id.polygonView);
        sourceFrame.post(new Runnable() {
            @Override
            public void run() {
                // aLINK edit: RM#912 when fragment is reset due to display change the bitmap is available
                if (original == null) {
                    original = getBitmap();
                }
                if (original != null) {
                    setBitmap(original);
                }
            }
        });
    }

    private Bitmap getBitmap() {
        Uri uri = getUri();
        try {
            Bitmap bitmap = Utils.getBitmap(getActivity(), uri);
            getActivity().getContentResolver().delete(uri, null, null);
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // aLINK edit: Android 13 New OS Support: Deprecation fixes
    @SuppressWarnings("deprecation")
    private Uri getUri() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // TIRAMISU onwards
            return getArguments() != null ? getArguments().getParcelable(ScanConstants.SELECTED_BITMAP, Uri.class) : null;
        } else {
            return getArguments() != null ? getArguments().getParcelable(ScanConstants.SELECTED_BITMAP) : null;
        }
    }

    private void setBitmap(Bitmap original) {
        Bitmap scaledBitmap = scaledBitmap(original, sourceFrame.getWidth(), sourceFrame.getHeight());
        sourceImageView.setImageBitmap(scaledBitmap);
        Bitmap tempBitmap = ((BitmapDrawable) sourceImageView.getDrawable()).getBitmap();
        Map<Integer, PointF> pointFs = getEdgePoints(tempBitmap);
        polygonView.setPoints(pointFs);
        polygonView.setVisibility(View.VISIBLE);
        int padding = (int) getResources().getDimension(R.dimen.scanPadding);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(tempBitmap.getWidth() + 2 * padding, tempBitmap.getHeight() + 2 * padding);
        layoutParams.gravity = Gravity.CENTER;
        polygonView.setLayoutParams(layoutParams);
    }

    private Map<Integer, PointF> getEdgePoints(Bitmap tempBitmap) {
        List<PointF> pointFs = getContourEdgePoints(tempBitmap);
        Map<Integer, PointF> orderedPoints = orderedValidEdgePoints(tempBitmap, pointFs);
        return orderedPoints;
    }

    private List<PointF> getContourEdgePoints(Bitmap tempBitmap) {
        float[] points = ((ScanActivity) getActivity()).getPoints(tempBitmap);
        float x1 = points[0];
        float x2 = points[1];
        float x3 = points[2];
        float x4 = points[3];

        float y1 = points[4];
        float y2 = points[5];
        float y3 = points[6];
        float y4 = points[7];

        List<PointF> pointFs = new ArrayList<>();
        pointFs.add(new PointF(x1, y1));
        pointFs.add(new PointF(x2, y2));
        pointFs.add(new PointF(x3, y3));
        pointFs.add(new PointF(x4, y4));
        return pointFs;
    }

    private Map<Integer, PointF> getOutlinePoints(Bitmap tempBitmap) {
        Map<Integer, PointF> outlinePoints = new HashMap<>();
        outlinePoints.put(0, new PointF(0, 0));
        outlinePoints.put(1, new PointF(tempBitmap.getWidth(), 0));
        outlinePoints.put(2, new PointF(0, tempBitmap.getHeight()));
        outlinePoints.put(3, new PointF(tempBitmap.getWidth(), tempBitmap.getHeight()));
        return outlinePoints;
    }

    private Map<Integer, PointF> orderedValidEdgePoints(Bitmap tempBitmap, List<PointF> pointFs) {
        Map<Integer, PointF> orderedPoints = polygonView.getOrderedPoints(pointFs);
        if (!polygonView.isValidShape(orderedPoints)) {
            orderedPoints = getOutlinePoints(tempBitmap);
        }
        return orderedPoints;
    }

    private class ScanButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            // aLINK edit: RM#923 prevent onClick by Enter key when ScanFragment is not on top
            if (!(getFragmentManager().findFragmentById(R.id.content) instanceof ScanFragment)) {
                return;
            }

            Map<Integer, PointF> points = polygonView.getPoints();
            if (isScanPointsValid(points)) {
                // aLINK edit - Start
                // android.os.AsyncTask was deprecated in API level 30.
                // Use threading instead
                new ScanAsyncTask(points).start();
                // aLINK edit - End
            } else {
                showErrorDialog();
            }
        }
    }

    private void showErrorDialog() {
        SingleButtonDialogFragment fragment = new SingleButtonDialogFragment(R.string.ok, getString(R.string.cantCrop), "Error", true);
        // aLINK edit - Start
        // android.app.FragmentManager was deprecated in API level 28
        // Call the getSupportFragmentManager() API to get androidx.fragment.app.FragmentManager
        // instead of android.app.FragmentManager
        FragmentManager fm = getActivity().getSupportFragmentManager();
        // aLINK edit - End
        fragment.show(fm, SingleButtonDialogFragment.class.toString());
    }

    private boolean isScanPointsValid(Map<Integer, PointF> points) {
        return points.size() == 4;
    }

    private Bitmap scaledBitmap(Bitmap bitmap, int width, int height) {
        Matrix m = new Matrix();
        m.setRectToRect(new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight()), new RectF(0, 0, width, height), Matrix.ScaleToFit.CENTER);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
    }

    private Bitmap getScannedBitmap(Bitmap original, Map<Integer, PointF> points) {
        int width = original.getWidth();
        int height = original.getHeight();
        float xRatio = (float) original.getWidth() / sourceImageView.getWidth();
        float yRatio = (float) original.getHeight() / sourceImageView.getHeight();

        float x1 = (points.get(0).x) * xRatio;
        float x2 = (points.get(1).x) * xRatio;
        float x3 = (points.get(2).x) * xRatio;
        float x4 = (points.get(3).x) * xRatio;
        float y1 = (points.get(0).y) * yRatio;
        float y2 = (points.get(1).y) * yRatio;
        float y3 = (points.get(2).y) * yRatio;
        float y4 = (points.get(3).y) * yRatio;
        Log.d("", "POints(" + x1 + "," + y1 + ")(" + x2 + "," + y2 + ")(" + x3 + "," + y3 + ")(" + x4 + "," + y4 + ")");
        Bitmap _bitmap = ((ScanActivity) getActivity()).getScannedBitmap(original, x1, y1, x2, y2, x3, y3, x4, y4);
        return _bitmap;
    }

    // aLINK edit - Start
    // android.os.AsyncTask was deprecated in API level 30.
    // Use threading instead
    private class ScanAsyncTask extends Thread {
    // aLINK edit - End

        private Map<Integer, PointF> points;

        public ScanAsyncTask(Map<Integer, PointF> points) {
            this.points = points;
        }

        // aLINK edit - Start
        // android.os.AsyncTask was deprecated in API level 30.
        // Use threading instead
        @Override
        public void run() {
            showProgressDialog(getString(R.string.scanning));

            Bitmap bitmap =  getScannedBitmap(original, points);
            Uri uri = Utils.getUri(getActivity(), bitmap);
            scanner.onScanFinish(uri);

            bitmap.recycle();
            dismissDialog();
        }
        // aLINK edit - End
    }

    protected void showProgressDialog(String message) {
        progressDialogFragment = new ProgressDialogFragment(message);
        FragmentManager fm = getFragmentManager();
        progressDialogFragment.show(fm, ProgressDialogFragment.class.toString());
    }

    protected void dismissDialog() {
        progressDialogFragment.dismissAllowingStateLoss();
    }

}