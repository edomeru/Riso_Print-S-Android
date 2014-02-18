package jp.co.riso.smartdeviceapp.controller.pdf;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManager;
import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManagerInterface;
import jp.co.riso.smartdeviceapp.view.MainActivity;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

public class PDFFileManagerTest extends  ActivityInstrumentationTestCase2<MainActivity> implements PDFFileManagerInterface {
    String mPdfPath;
    int mPdfPageCount;
    String mLargePdfPath;
    int mStatus;
    PDFFileManager mPdfManager;

    public PDFFileManagerTest() {
        super(MainActivity.class);
    }
    
    public PDFFileManagerTest(Class<MainActivity> activityClass) {
        super(activityClass);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        
        mPdfPath = getAssetPath("PDF-squarish.pdf");
        mPdfPageCount = 36; // page count of mPdfPath file
        
        mLargePdfPath = Environment.getExternalStorageDirectory()+"/TestPDFs/PDF-270MB_134pages.pdf";
        mStatus = 0;
        
        mPdfManager = new PDFFileManager(this);
        assertNotNull(mPdfManager);
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    //================================================================================
    // Tests - get/set path
    //================================================================================
    
    public void testPath_NullValues() {
        mPdfManager.setPath(null);
        assertNull(mPdfManager.getPath());
    }
    
    public void testPath_EmptyValues() {
        mPdfManager.setPath("");
        assertEquals(mPdfManager.getPath(), "");
    }
    
    public void testPath_Values() {
        mPdfManager.setPath("/this/is/a/path");
        assertEquals(mPdfManager.getPath(), "/this/is/a/path");
    }
    
    //================================================================================
    // Tests - is opened
    //================================================================================

    public void testIsOpenable() {
        assertFalse(mPdfManager.isOpenable());
        
        mPdfManager.setPath(mPdfPath);
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_OK);
        assertTrue(mPdfManager.isOpenable());
        
        mPdfManager.closeDocument();
        assertTrue(mPdfManager.isOpenable());
    }

    public void testIsOpen_invalidPath() {
        assertFalse(mPdfManager.isOpenable());
        
        mPdfManager.setPath(null);
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_INVALID_PATH);
        assertFalse(mPdfManager.isOpenable());
    }
    
    //================================================================================
    // Tests - get page count
    //================================================================================

    public void testGetPageCount() {
        mPdfManager.setPath(mPdfPath);
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_OK);
        assertEquals(mPdfManager.getPageCount(), mPdfPageCount);
    }

    public void testGetPageCount_InvalidPath() {
        mPdfManager.setPath(null);
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_INVALID_PATH);
        assertEquals(mPdfManager.getPageCount(), 0);
    }

    public void testGetPageCount_invalidPath() {
        mPdfManager.setPath(null);
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_INVALID_PATH);
        assertEquals(mPdfManager.getPageCount(), 0);
    }
    
    //================================================================================
    // Tests - get page
    //================================================================================

    public void testGetPageBitmap_Valid() {
        mPdfManager.setPath(mLargePdfPath);
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_OK);
        
        Bitmap bmp = mPdfManager.getPageBitmap(0);
        assertNotNull(bmp);
    }
    
    public void testGetPageBitmap_NegativePageNumber() {
        mPdfManager.setPath(mLargePdfPath);
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_OK);
        
        Bitmap bmp = mPdfManager.getPageBitmap(-1);
        assertNull(bmp);
    }
    
    public void testGetPageBitmap_GreaterThanPageCount() {
        mPdfManager.setPath(mLargePdfPath);
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_OK);
        
        Bitmap bmp = mPdfManager.getPageBitmap(mPdfManager.getPageCount());
        assertNull(bmp);
    }
    
    public void testGetPageBitmap_MultipleTimes() {
        mPdfManager.setPath(mLargePdfPath);
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_OK);
        
        for (int i = 0; i < 100; i++) {
            long time = System.currentTimeMillis();
            Bitmap bmp = mPdfManager.getPageBitmap(i);
            assertNotNull(bmp);
            long duration = (System.currentTimeMillis() - time);
            Log.e("testGetPageBitmap_MultipleTimes", "Page: " + i + " Duration: " + duration);

            File dir = Environment.getExternalStorageDirectory();// getActivity().getExternalFilesDir(null);
            File filepath = new File(dir, "png" + i + ".png");
            
            OutputStream out = null;
            try {
                out = new BufferedOutputStream(new FileOutputStream(filepath));
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.close();
                
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                
            }
            
            assertTrue(duration > 400);
        }
    }
    
    //================================================================================
    // Tests - open
    //================================================================================
    
    public void testOpenDocument_NullPdfPath() {
        mPdfManager.setPath(null);
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_INVALID_PATH);
    }
    
    public void testOpenDocument_EmptyPdfPath() {
        mPdfManager.setPath("");
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_INVALID_PATH);
    }
    
    public void testOpenDocument_InvalidPdfPath() {
        mPdfManager.setPath("not_a_valid_path");
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_INVALID_PATH);
    }

    public void testOpenDocument_Valid() {
        mPdfManager.setPath(mPdfPath);
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_OK);
    }

    public void testOpenDocument_Opened() {
        mPdfManager.setPath(mPdfPath);
        int status = mPdfManager.openDocument();
        assertEquals(status, 0);
        status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_OK);
    }
    
    //================================================================================
    // Tests - close
    //================================================================================

    public void tesCloseDocument_Opened() {
        mPdfManager.setPath(mPdfPath);
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_OK);
        
        try {
            mPdfManager.closeDocument();
        } catch(Exception e) {
            fail(); // No exception expected
        }
    }

    public void tesCloseDocument_NotOpened() {
        try {
            mPdfManager.setPath(mPdfPath);
            mPdfManager.closeDocument();
        } catch(Exception e) {
            fail(); // No exception expected
        }
    }
    //================================================================================
    // Tests - open async
    //================================================================================
    
    public void testOpenAsync_Valid() {
        mPdfManager.setPath(mPdfPath);
        mPdfManager.openAsync();

        getInstrumentation().waitForIdleSync();
        // assume AsyncTask will be finished in 2 seconds.
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        getInstrumentation().waitForIdleSync();
        
        assertEquals(mStatus, PDFFileManager.PDF_OK);
    }
    
    public void testOpenAsync_NullPdfPath() {
        mPdfManager.setPath(null);
        mPdfManager.openAsync();

        getInstrumentation().waitForIdleSync();
        // assume AsyncTask will be finished in 2 seconds.
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        getInstrumentation().waitForIdleSync();
        
        assertEquals(PDFFileManager.PDF_INVALID_PATH, mStatus);
    }
    
    public void testOpenAsync_EmptyPdfPath() {
        mPdfManager.setPath("");
        mPdfManager.openAsync();

        getInstrumentation().waitForIdleSync();
        // assume AsyncTask will be finished in 2 seconds.
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        getInstrumentation().waitForIdleSync();
        
        assertEquals(mStatus, PDFFileManager.PDF_INVALID_PATH);
    }
    
    public void testOpenAsync_InvalidPdfPath() {
        mPdfManager.setPath("not_a_valid_path");
        mPdfManager.openAsync();

        getInstrumentation().waitForIdleSync();
        // assume AsyncTask will be finished in 2 seconds.
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        getInstrumentation().waitForIdleSync();
        
        assertEquals(mStatus, PDFFileManager.PDF_INVALID_PATH);
    }
    
    public void testOpenAsync_Consecutive() {
        mPdfManager.setPath(mLargePdfPath);
        
        mPdfManager.openAsync();
        mPdfManager.openAsync();

        getInstrumentation().waitForIdleSync();
        // assume AsyncTask will be finished in 2 seconds.
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        getInstrumentation().waitForIdleSync();
        
        assertEquals(mStatus, 0);
    }
    
    //================================================================================
    // Tests - open-close duration
    //================================================================================

    public void testDuration_SmallPDF() {
        mPdfManager.setPath(mPdfPath);
        
        double time = System.currentTimeMillis();
        int status = mPdfManager.openDocument();
        double duration = System.currentTimeMillis() - time;
        assertEquals(status, 0);
        assertTrue(duration < 2000);

        time = System.currentTimeMillis();
        mPdfManager.closeDocument();
        duration = System.currentTimeMillis() - time;
        assertTrue(duration < 500);
    }
    
    public void testDuration_LargePDF() {
        mPdfManager.setPath(mLargePdfPath);
        
        double time = System.currentTimeMillis();
        int status = mPdfManager.openDocument();
        double duration = System.currentTimeMillis() - time;
        assertEquals(status, 0);
        assertTrue(duration < 5000);
        
        time = System.currentTimeMillis();
        mPdfManager.closeDocument();
        duration = System.currentTimeMillis() - time;
        assertTrue(duration < 500);
    }
    
    //================================================================================
    // Private
    //================================================================================
    
    private String getAssetPath(String filename) {
        File f = new File(getActivity().getCacheDir() + "/" + filename);
        AssetManager assetManager = getInstrumentation().getContext().getAssets();
        
        if (!f.exists()) {
            try {
                InputStream is = assetManager.open(filename);
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                
                FileOutputStream fos = new FileOutputStream(f);
                fos.write(buffer);
                fos.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        
        return f.getPath();
    }

    @Override
    public void onFileOpenedResult(int status) {
        mStatus = status;
    }

    @Override
    public void onPreBufferFinished() {
    }
}
