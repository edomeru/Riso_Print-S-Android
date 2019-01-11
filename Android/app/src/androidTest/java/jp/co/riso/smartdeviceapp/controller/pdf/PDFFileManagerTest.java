package jp.co.riso.smartdeviceapp.controller.pdf;

import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;

import com.radaee.pdf.Global;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import jp.co.riso.android.util.FileUtils;
import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.view.MainActivity;

public class PDFFileManagerTest extends  ActivityInstrumentationTestCase2<MainActivity> implements PDFFileManagerInterface {
    String mPdfPath;
    int mPdfPageCount;
    String mLargePdfPath;
    String mPdfInSandboxPath;
    String mEncryptedPdfPath;
    String mPrintingDisallowed;
    String mLandscapePDF;
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

        //Initialize Radaee
        Global.Init(getActivity());

        mPdfPath = getAssetPath("PDF-squarish.pdf");
        mPdfPageCount = 36; // page count of mPdfPath file
        
        mEncryptedPdfPath = getAssetPath("40RC4_Nitro.pdf");
        
        mPrintingDisallowed = getAssetPath("PDF-0010pages.pdf");
        
        mLandscapePDF = getAssetPath("4pages_Landscape_TestData.pdf");
        
        mPdfInSandboxPath = PDFFileManager.getSandboxPath();
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
    }
    
    public void testPath_EmptyValues() {
        mPdfManager.setPDF("");
        assertEquals(mPdfManager.getPath(), "");
        assertEquals(mPdfManager.getFileName(), "");
    }
    
    public void testPath_Values() {
        mPdfManager.setPDF("/this/is/a/path");
        assertEquals(mPdfManager.getPath(), "/this/is/a/path");
        assertEquals(mPdfManager.getFileName(), "path");
    }
    
    //================================================================================
    // Tests - getSandboxPath
    //================================================================================

    public void testGetSandboxPath() {
        String sandbox = PDFFileManager.getSandboxPath();
        assertEquals(SmartDeviceApp.getAppContext().getExternalFilesDir(AppConstants.CONST_PDF_DIR) + "/" + AppConstants.CONST_TEMP_PDF_PATH, sandbox);
    }
    
    //================================================================================
    // Tests - clearSandboxPDFName
    //================================================================================
    
    public void testClearSandboxPDFName() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.getAppContext());
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(PDFFileManager.KEY_SANDBOX_PDF_NAME, "NOT");
        edit.apply();
        
        PDFFileManager.clearSandboxPDFName(SmartDeviceApp.getAppContext());
        
        assertFalse(prefs.contains(PDFFileManager.KEY_SANDBOX_PDF_NAME));
    }
    
    public void testClearSandboxPDFName_NoKey() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.getAppContext());
        SharedPreferences.Editor edit = prefs.edit();
        edit.remove(PDFFileManager.KEY_SANDBOX_PDF_NAME);
        edit.apply();
        
        PDFFileManager.clearSandboxPDFName(SmartDeviceApp.getAppContext());
        
        assertFalse(prefs.contains(PDFFileManager.KEY_SANDBOX_PDF_NAME));
    }
    
    public void testClearSandboxPDFName_NullContext() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.getAppContext());
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(PDFFileManager.KEY_SANDBOX_PDF_NAME, "NOT");
        edit.apply();
        
        PDFFileManager.clearSandboxPDFName(null);

        assertTrue(prefs.contains(PDFFileManager.KEY_SANDBOX_PDF_NAME));
    }
    
    //================================================================================
    // Tests - get/setSandboxPDFName
    //================================================================================
    
    public void testGetSetSandboxPDFName_Valid() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.getAppContext());
        PDFFileManager.clearSandboxPDFName(SmartDeviceApp.getAppContext());
        
        PDFFileManager.setSandboxPDF(SmartDeviceApp.getAppContext(), "NO.pdf");
        
        assertTrue(prefs.contains(PDFFileManager.KEY_SANDBOX_PDF_NAME));
        
        String sandboxName = PDFFileManager.getSandboxPDFName(SmartDeviceApp.getAppContext());
        assertEquals("NO.pdf", sandboxName);
    }
    
    public void testGetSetSandboxPDFName_NoKeyInPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.getAppContext());
        PDFFileManager.clearSandboxPDFName(SmartDeviceApp.getAppContext());

        PDFFileManager.setSandboxPDF(SmartDeviceApp.getAppContext(), null);
        assertFalse(prefs.contains(PDFFileManager.KEY_SANDBOX_PDF_NAME));
        
        String sandboxName = PDFFileManager.getSandboxPDFName(SmartDeviceApp.getAppContext());
        assertNull(sandboxName);
    }
    
    public void testGetSetSandboxPDFName_NullContext() {
        PDFFileManager.clearSandboxPDFName(SmartDeviceApp.getAppContext());

        PDFFileManager.setSandboxPDF(null, null);
        String sandboxName = PDFFileManager.getSandboxPDFName(null);
        assertNull(sandboxName);
    }
    
    //================================================================================
    // Tests - clearSandboxPDFName
    //================================================================================
    
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
        
        float width = mPdfManager.getPageWidth(0);
        float height = mPdfManager.getPageHeight(0);

        assertTrue(width != 0);
        assertTrue(height != 0);
    }

    public void testGetPageDimensions_Invalid() {
        mPdfManager.setPDF("");
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_OPEN_FAILED);
        
        float width = mPdfManager.getPageWidth(0);
        float height = mPdfManager.getPageHeight(0);
        
        assertTrue(width == 0);
        assertTrue(height == 0);
    }

    public void testGetPageDimensions_NegativePage() {
        mPdfManager.setPDF(mPdfPath);
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_OK);
        
        float width = mPdfManager.getPageWidth(-1);
        float height = mPdfManager.getPageHeight(-1);
        
        assertTrue(width == 0);
        assertTrue(height == 0);
    }

    public void testGetPageDimensions_GreaterThanPageCount() {
        mPdfManager.setPDF(mPdfPath);
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_OK);
        
        float width = mPdfManager.getPageWidth(mPdfManager.getPageCount());
        float height = mPdfManager.getPageHeight(mPdfManager.getPageCount());
        
        assertTrue(width == 0);
        assertTrue(height == 0);
    }

    
    //================================================================================
    // Tests - is PDF landscape
    //================================================================================
    
    public void testIsPDFLandscape_Valid() {
        mPdfManager.setPDF(mPdfPath);
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_OK);
        
        boolean isLandscape = mPdfManager.isPDFLandscape();

        assertFalse(isLandscape);
    }

    public void testIsPDFLandscape_Invalid() {
        mPdfManager.setPDF("");
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_OPEN_FAILED);
        
        boolean isLandscape = mPdfManager.isPDFLandscape();
        
        assertFalse(isLandscape);
    }

    public void testIsPDFLandscape_LandscapeValid() {
        mPdfManager.setPDF(mLandscapePDF);
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_OK);
        
        boolean isLandscape = mPdfManager.isPDFLandscape();
        
        assertTrue(isLandscape);
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
    // Tests - test
    //================================================================================
    
    public void testTestDocument_NullPdfPath() {
        int status = mPdfManager.testDocument(null);
        assertEquals(status, PDFFileManager.PDF_OPEN_FAILED);
    }
    
    public void testTestDocument_InvalidPdfPath() {
        int status = mPdfManager.testDocument("not_a_valid_path");
        assertEquals(status, PDFFileManager.PDF_OPEN_FAILED);
    }

    public void testTestDocument_Valid() {
        int status = mPdfManager.testDocument(mPdfPath);
        assertEquals(status, PDFFileManager.PDF_OK);
    }
    
    public void testTestDocument_EncrpyptedPath() {
        int status = mPdfManager.testDocument(mEncryptedPdfPath);
        assertEquals(status, PDFFileManager.PDF_ENCRYPTED);
    }
    
    public void testTestDocument_PrintingDisallowed() {
        int status = mPdfManager.testDocument(mPrintingDisallowed);
        assertEquals(status, PDFFileManager.PDF_PRINT_RESTRICTED);
    }
    
    public void testTestDocument_EmptyPdfPath() {
        int status = mPdfManager.testDocument("");
        assertEquals(status, PDFFileManager.PDF_OPEN_FAILED);
    }
    
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
    
    public void testOpenDocument_PrintingDisallowed() {
        mPdfManager.setPDF(mPrintingDisallowed);
        int status = mPdfManager.openDocument();
        assertEquals(status, PDFFileManager.PDF_PRINT_RESTRICTED);
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

    /*
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

        assertEquals(PDFFileManager.PDF_OPEN_FAILED, mStatus);
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
        
        assertEquals(PDFFileManager.PDF_OPEN_FAILED, mStatus);
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
        
        assertEquals(PDFFileManager.PDF_OK, mStatus);
    }
    */

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
