package jp.co.riso.smartdeviceapp.controller.pdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import jp.co.riso.android.util.FileUtils;
import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManager;
import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManagerInterface;
import jp.co.riso.smartdeviceapp.view.MainActivity;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.test.ActivityInstrumentationTestCase2;

public class PDFFileManagerTest extends  ActivityInstrumentationTestCase2<MainActivity> implements PDFFileManagerInterface {
    String mPdfPath;
    int mPdfPageCount;
    String mLargePdfPath;
    String mPdfInSandboxPath;
    String mEncryptedPdfPath;
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
        
        mEncryptedPdfPath = getAssetPath("40RC4_Nitro.pdf");
        
        mPdfInSandboxPath = PDFFileManager.convertToSandboxPath("PDF-squarish2.pdf");
        FileUtils.copy(new File(mPdfPath), new File(mPdfInSandboxPath));
        
        mLargePdfPath = mPdfPath;
        //Environment.getExternalStorageDirectory()+"/TestPDFs/PDF-270MB_134pages.pdf";
        mStatus = 0;
        
        mPdfManager = new PDFFileManager(this);
        assertNotNull(mPdfManager);
        
        PDFFileManager.setHasNewPDFData(SmartDeviceApp.getAppContext(), true);
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    //================================================================================
    // Tests - get/set has new PDF Data
    //================================================================================
    
    public void testGetSetNewPDFData_Valid() {
        boolean val = PDFFileManager.hasNewPDFData(SmartDeviceApp.getAppContext());
        assertTrue(val);
        
        PDFFileManager.setHasNewPDFData(SmartDeviceApp.getAppContext(), false);
        assertFalse(PDFFileManager.hasNewPDFData(SmartDeviceApp.getAppContext()));
    }
    
    public void testGetSetNewPDFData_NullContext() {
        boolean val = PDFFileManager.hasNewPDFData(null);
        assertFalse(val);
        
        PDFFileManager.setHasNewPDFData(null, false);
        assertFalse(PDFFileManager.hasNewPDFData(null));
    }
    
    //================================================================================
    // Tests - get/set pdf
    //================================================================================
    
    public void testPath_NullValues() {
        mPdfManager.setPDF(null);
        assertNull(mPdfManager.getPath());
        assertNull(mPdfManager.getFileName());
        assertNull(mPdfManager.getSandboxPath());
    }
    
    public void testPath_EmptyValues() {
        mPdfManager.setPDF("");
        assertEquals(mPdfManager.getPath(), "");
        assertEquals(mPdfManager.getFileName(), "");
        assertEquals(mPdfManager.getSandboxPath(), PDFFileManager.convertToSandboxPath(""));
    }
    
    public void testPath_Values() {
        mPdfManager.setPDF("/this/is/a/path");
        assertEquals(mPdfManager.getPath(), "/this/is/a/path");
        assertEquals(mPdfManager.getFileName(), "path");
        assertEquals(mPdfManager.getSandboxPath(), PDFFileManager.convertToSandboxPath(mPdfManager.getFileName()));
    }
    
    //================================================================================
    // Tests - convertToSandboxPath
    //================================================================================

    public void testConvertToSandboxPath_Valid() {
        String sandbox = PDFFileManager.convertToSandboxPath("path.pdf");
        assertEquals(SmartDeviceApp.getAppContext().getExternalFilesDir(AppConstants.CONST_PDF_DIR) + "/path.pdf", sandbox);
    }
    
    public void testConvertToSandboxPath_Empty() {
        String sandbox = PDFFileManager.convertToSandboxPath("");
        assertEquals(SmartDeviceApp.getAppContext().getExternalFilesDir(AppConstants.CONST_PDF_DIR) + "/", sandbox);
    }

    public void testConvertToSandboxPath_Null() {
        String sandbox = PDFFileManager.convertToSandboxPath(null);
        assertEquals(SmartDeviceApp.getAppContext().getExternalFilesDir(AppConstants.CONST_PDF_DIR) + "/", sandbox);
    }
    
    
    //================================================================================
    // Tests - is opened
    //================================================================================

    public void testIsOpenable() {
        assertFalse(mPdfManager.isInitialized());
        
        mPdfManager.setPDF(mPdfPath);
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_OK);
        assertTrue(mPdfManager.isInitialized());
        
        mPdfManager.closeDocument();
        assertTrue(mPdfManager.isInitialized());
    }

    public void testIsOpen_invalidPath() {
        assertFalse(mPdfManager.isInitialized());
        
        mPdfManager.setPDF(null);
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_OPEN_FAILED);
        assertFalse(mPdfManager.isInitialized());
    }
    
    public void testIsOpen_sandboxPath() {
        assertFalse(mPdfManager.isInitialized());

        mPdfManager.setPDF(mPdfInSandboxPath);
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_OK);
        assertTrue(mPdfManager.isInitialized());
        
        mPdfManager.closeDocument();
        assertTrue(mPdfManager.isInitialized());
    }
    
    //================================================================================
    // Tests - get page count
    //================================================================================

    public void testGetPageCount() {
        mPdfManager.setPDF(mPdfPath);
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_OK);
        assertEquals(mPdfManager.getPageCount(), mPdfPageCount);
    }

    public void testGetPageCount_InvalidPath() {
        mPdfManager.setPDF(null);
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_OPEN_FAILED);
        assertEquals(mPdfManager.getPageCount(), 0);
    }

    public void testGetPageCount_invalidPath() {
        mPdfManager.setPDF(null);
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_OPEN_FAILED);
        assertEquals(mPdfManager.getPageCount(), 0);
    }
    
    //================================================================================
    // Tests - get page dimensions
    //================================================================================

    public void testGetPageDimensions_Valid() {
        mPdfManager.setPDF(mPdfPath);
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_OK);
        
        float width = mPdfManager.getPageWidth();
        float height = mPdfManager.getPageHeight();

        assertTrue(width != 0);
        assertTrue(height != 0);
    }

    public void testGetPageDimensions_Invalid() {
        mPdfManager.setPDF("");
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_OPEN_FAILED);
        
        float width = mPdfManager.getPageWidth();
        float height = mPdfManager.getPageHeight();
        
        assertTrue(width == 0);
        assertTrue(height == 0);
    }
    
    //================================================================================
    // Tests - get page
    //================================================================================

    public void testGetPageBitmap_Valid() {
        mPdfManager.setPDF(mPdfPath);
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_OK);
        
        Bitmap bmp = mPdfManager.getPageBitmap(0);
        assertNotNull(bmp);
    }

    public void testGetPageBitmap_Flip() {
        mPdfManager.setPDF(mPdfPath);
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_OK);
        
        Bitmap bmp = mPdfManager.getPageBitmap(0, 1.0f, true, true);
        assertNotNull(bmp);
    }

    public void testGetPageBitmap_Uninitialized() {
        mPdfManager.setPDF(mPdfPath);
        
        Bitmap bmp = mPdfManager.getPageBitmap(0);
        assertNull(bmp);
    }
    
    public void testGetPageBitmap_NegativePageNumber() {
        mPdfManager.setPDF(mPdfPath);
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_OK);
        
        Bitmap bmp = mPdfManager.getPageBitmap(-1);
        assertNull(bmp);
    }
    
    public void testGetPageBitmap_GreaterThanPageCount() {
        mPdfManager.setPDF(mPdfPath);
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_OK);
        
        Bitmap bmp = mPdfManager.getPageBitmap(mPdfManager.getPageCount());
        assertNull(bmp);
    }
    
    /*
    public void testGetPageBitmap_MultipleTimes() {
        mPdfManager.setPDF(mLargePdfPath);
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
    */
    
    //================================================================================
    // Tests - open
    //================================================================================
    
    public void testOpenDocument_NullPdfPath() {
        mPdfManager.setPDF(null);
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_OPEN_FAILED);
    }
    
    public void testOpenDocument_InvalidPdfPath() {
        mPdfManager.setPDF("not_a_valid_path");
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_OPEN_FAILED);
    }

    public void testOpenDocument_Valid() {
        mPdfManager.setPDF(mPdfPath);
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_OK);
    }
    
    public void testOpenDocument_EncrpyptedPath() {
        mPdfManager.setPDF(mEncryptedPdfPath);
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_ENCRYPTED);
    }
    
    public void testOpenDocument_EmptyPdfPath() {
        mPdfManager.setPDF("");
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_OPEN_FAILED);
    }

    public void testOpenDocument_Opened() {
        mPdfManager.setPDF(mPdfPath);
        int status = mPdfManager.openDocument();
        assertEquals(status, 0);
        status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_OK);
    }
    
    //================================================================================
    // Tests - close
    //================================================================================

    public void tesCloseDocument_Opened() {
        mPdfManager.setPDF(mPdfPath);
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
            mPdfManager.setPDF(mPdfPath);
            mPdfManager.closeDocument();
        } catch(Exception e) {
            fail(); // No exception expected
        }
    }
    //================================================================================
    // Tests - open async
    //================================================================================
    
    public void testOpenAsync_Valid() {
        mPdfManager.setPDF(mPdfPath);
        mPdfManager.initializeAsync();

        getInstrumentation().waitForIdleSync();
        // assume AsyncTask will be finished in 10 seconds.
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        getInstrumentation().waitForIdleSync();
        
        assertEquals(mStatus, PDFFileManager.PDF_OK);
    }
    
    public void testOpenAsync_NullPdfPath() {
        mPdfManager.setPDF(null);
        mPdfManager.initializeAsync();

        getInstrumentation().waitForIdleSync();
        // assume AsyncTask will be finished in 10 seconds.
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        getInstrumentation().waitForIdleSync();
        
        assertEquals(PDFFileManager.PDF_OPEN_FAILED, mStatus);
    }
    
    public void testOpenAsync_EmptyPdfPath() {
        mPdfManager.setPDF("");
        mPdfManager.initializeAsync();

        getInstrumentation().waitForIdleSync();
        // assume AsyncTask will be finished in 10 seconds.
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        getInstrumentation().waitForIdleSync();
        
        assertEquals(mStatus, PDFFileManager.PDF_OPEN_FAILED);
    }
    
    public void testOpenAsync_InvalidPdfPath() {
        mPdfManager.setPDF("not_a_valid_path");
        mPdfManager.initializeAsync();

        getInstrumentation().waitForIdleSync();
        // assume AsyncTask will be finished in 10 seconds.
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        getInstrumentation().waitForIdleSync();
        
        assertEquals(mStatus, PDFFileManager.PDF_OPEN_FAILED);
    }
    
    public void testOpenAsync_Consecutive() {
        mPdfManager.setPDF(mLargePdfPath);
        
        mPdfManager.initializeAsync();
        mPdfManager.initializeAsync();

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
        mPdfManager.setPDF(mPdfPath);
        
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
        mPdfManager.setPDF(mLargePdfPath);
        
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
        
        return f.getPath();
    }

    @Override
    public void onFileInitialized(int status) {
        mStatus = status;
    }
}
