package jp.co.riso.smartdeviceapp.controller.pdf

import androidx.preference.PreferenceManager
import androidx.test.platform.app.InstrumentationRegistry
import com.radaee.pdf.Global
import jp.co.riso.android.util.FileUtils
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class PDFFileManagerTest : BaseActivityTestUtil(), PDFFileManagerInterface {

    private var _pdfPath: String? = null
    private var _pdfPageCount = 0
    private var _largePdfPath: String? = null
    private var _pdfInSandboxPath: String? = null
    private var _encryptedPdfPath: String? = null
    private var _printingDisallowed: String? = null
    private var _landscapePDF: String? = null
    private var _status = 0
    private var _pdfManager: PDFFileManager? = null

    @Before
    fun setUp() {
        //Initialize Radaee
        Global.Init(mainActivity)
        _pdfPath = getAssetPath("PDF-squarish.pdf")
        _pdfPageCount = 36 // page count of mPdfPath file
        _encryptedPdfPath = getAssetPath("40RC4_Nitro.pdf")
        _printingDisallowed = getAssetPath("PDF-0010pages.pdf")
        _landscapePDF = getAssetPath("4pages_Landscape_TestData.pdf")
        _pdfInSandboxPath = PDFFileManager.getSandboxPath()
        FileUtils.copy(File(_pdfPath!!), File(_pdfInSandboxPath!!))
        _largePdfPath = _pdfPath
        //Environment.getExternalStorageDirectory()+"/TestPDFs/PDF-270MB_134pages.pdf";
        _status = 0
        _pdfManager = PDFFileManager(this)
        TestCase.assertNotNull(_pdfManager)
        PDFFileManager.setHasNewPDFData(SmartDeviceApp.getAppContext(), true)
    }

    //================================================================================
    // Tests - get/set has new PDF Data
    //================================================================================
    @Test
    fun testGetSetNewPDFData_Valid() {
        val `val` = PDFFileManager.hasNewPDFData(SmartDeviceApp.getAppContext())
        TestCase.assertTrue(`val`)
        PDFFileManager.setHasNewPDFData(SmartDeviceApp.getAppContext(), false)
        TestCase.assertFalse(PDFFileManager.hasNewPDFData(SmartDeviceApp.getAppContext()))
    }

    @Test
    fun testGetSetNewPDFData_NullContext() {
        val `val` = PDFFileManager.hasNewPDFData(null)
        TestCase.assertFalse(`val`)
        PDFFileManager.setHasNewPDFData(null, false)
        TestCase.assertFalse(PDFFileManager.hasNewPDFData(null))
    }

    //================================================================================
    // Tests - get/set pdf
    //================================================================================
    @Test
    fun testPath_NullValues() {
        _pdfManager!!.setPDF(null)
        TestCase.assertNull(_pdfManager!!.path)
        TestCase.assertNull(_pdfManager!!.fileName)
    }

    @Test
    fun testPath_EmptyValues() {
        _pdfManager!!.setPDF("")
        TestCase.assertEquals(_pdfManager!!.path, "")
        TestCase.assertEquals(_pdfManager!!.fileName, "")
    }

    @Test
    fun testPath_Values() {
        _pdfManager!!.setPDF("/this/is/a/path")
        TestCase.assertEquals(_pdfManager!!.path, "/this/is/a/path")
        TestCase.assertEquals(_pdfManager!!.fileName, "path")
    }

    @Test
    fun testSetPdfFilename() {
        _pdfManager!!.fileName = "test.pdf"
        TestCase.assertEquals(_pdfManager!!.fileName, "test.pdf")
    }

    //================================================================================
    // Tests - getSandboxPath
    //================================================================================
    @Test
    fun testGetSandboxPath() {
        val sandbox = PDFFileManager.getSandboxPath()
        TestCase.assertEquals(
            SmartDeviceApp.getAppContext().getExternalFilesDir(AppConstants.CONST_PDF_DIR)
                .toString() + "/" + AppConstants.CONST_TEMP_PDF_PATH, sandbox
        )
    }

    //================================================================================
    // Tests - clearSandboxPDFName
    //================================================================================
    @Test
    fun testClearSandboxPDFName() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.getAppContext())
        val edit = prefs.edit()
        edit.putString(PDFFileManager.KEY_SANDBOX_PDF_NAME, "NOT")
        edit.apply()
        PDFFileManager.clearSandboxPDFName(SmartDeviceApp.getAppContext())
        TestCase.assertFalse(prefs.contains(PDFFileManager.KEY_SANDBOX_PDF_NAME))
    }

    @Test
    fun testClearSandboxPDFName_NoKey() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.getAppContext())
        val edit = prefs.edit()
        edit.remove(PDFFileManager.KEY_SANDBOX_PDF_NAME)
        edit.apply()
        PDFFileManager.clearSandboxPDFName(SmartDeviceApp.getAppContext())
        TestCase.assertFalse(prefs.contains(PDFFileManager.KEY_SANDBOX_PDF_NAME))
    }

    @Test
    fun testClearSandboxPDFName_NullContext() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.getAppContext())
        val edit = prefs.edit()
        edit.putString(PDFFileManager.KEY_SANDBOX_PDF_NAME, "NOT")
        edit.apply()
        PDFFileManager.clearSandboxPDFName(null)
        TestCase.assertTrue(prefs.contains(PDFFileManager.KEY_SANDBOX_PDF_NAME))
    }

    //================================================================================
    // Tests - get/setSandboxPDFName
    //================================================================================
    @Test
    fun testGetSetSandboxPDFName_Valid() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.getAppContext())
        PDFFileManager.clearSandboxPDFName(SmartDeviceApp.getAppContext())
        PDFFileManager.setSandboxPDF(SmartDeviceApp.getAppContext(), "NO.pdf")
        TestCase.assertTrue(prefs.contains(PDFFileManager.KEY_SANDBOX_PDF_NAME))
        val sandboxName = PDFFileManager.getSandboxPDFName(SmartDeviceApp.getAppContext())
        TestCase.assertEquals("NO.pdf", sandboxName)
    }

    @Test
    fun testGetSetSandboxPDFName_NoKeyInPreferences() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.getAppContext())
        PDFFileManager.clearSandboxPDFName(SmartDeviceApp.getAppContext())
        PDFFileManager.setSandboxPDF(SmartDeviceApp.getAppContext(), null)
        TestCase.assertFalse(prefs.contains(PDFFileManager.KEY_SANDBOX_PDF_NAME))
        val sandboxName = PDFFileManager.getSandboxPDFName(SmartDeviceApp.getAppContext())
        TestCase.assertNull(sandboxName)
    }

    @Test
    fun testGetSetSandboxPDFName_NullContext() {
        PDFFileManager.clearSandboxPDFName(SmartDeviceApp.getAppContext())
        PDFFileManager.setSandboxPDF(null, null)
        val sandboxName = PDFFileManager.getSandboxPDFName(null)
        TestCase.assertNull(sandboxName)
    }

    //================================================================================
    // Tests - clearSandboxPDFName
    //================================================================================
    //================================================================================
    // Tests - is opened
    //================================================================================
    @Test
    fun testIsOpenable() {
        TestCase.assertFalse(_pdfManager!!.isInitialized)
        _pdfManager!!.setPDF(_pdfPath)
        val status = _pdfManager!!.openDocument()
        TestCase.assertEquals(status, PDFFileManager.PDF_OK)
        TestCase.assertTrue(_pdfManager!!.isInitialized)
        _pdfManager!!.closeDocument()
        TestCase.assertTrue(_pdfManager!!.isInitialized)
    }

    @Test
    fun testIsOpen_invalidPath() {
        TestCase.assertFalse(_pdfManager!!.isInitialized)
        _pdfManager!!.setPDF(null)
        val status = _pdfManager!!.openDocument()
        TestCase.assertEquals(status, PDFFileManager.PDF_OPEN_FAILED)
        TestCase.assertFalse(_pdfManager!!.isInitialized)
    }

    @Test
    fun testIsOpen_sandboxPath() {
        TestCase.assertFalse(_pdfManager!!.isInitialized)
        _pdfManager!!.setPDF(_pdfInSandboxPath)
        val status = _pdfManager!!.openDocument()
        TestCase.assertEquals(status, PDFFileManager.PDF_OK)
        TestCase.assertTrue(_pdfManager!!.isInitialized)
        _pdfManager!!.closeDocument()
        TestCase.assertTrue(_pdfManager!!.isInitialized)
    }

    //================================================================================
    // Tests - get page count
    //================================================================================
    @Test
    fun testGetPageCount() {
        _pdfManager!!.setPDF(_pdfPath)
        val status = _pdfManager!!.openDocument()
        TestCase.assertEquals(status, PDFFileManager.PDF_OK)
        TestCase.assertEquals(_pdfManager!!.pageCount, _pdfPageCount)
    }

    @Test
    fun testGetPageCount_InvalidPath() {
        _pdfManager!!.setPDF(null)
        val status = _pdfManager!!.openDocument()
        TestCase.assertEquals(status, PDFFileManager.PDF_OPEN_FAILED)
        TestCase.assertEquals(_pdfManager!!.pageCount, 0)
    }

    @Test
    fun testGetPageCount_invalidPath() {
        _pdfManager!!.setPDF(null)
        val status = _pdfManager!!.openDocument()
        TestCase.assertEquals(status, PDFFileManager.PDF_OPEN_FAILED)
        TestCase.assertEquals(_pdfManager!!.pageCount, 0)
    }

    //================================================================================
    // Tests - get page dimensions
    //================================================================================
    @Test
    fun testGetPageDimensions_Valid() {
        _pdfManager!!.setPDF(_pdfPath)
        val status = _pdfManager!!.openDocument()
        TestCase.assertEquals(status, PDFFileManager.PDF_OK)
        val width = _pdfManager!!.getPageWidth(0)
        val height = _pdfManager!!.getPageHeight(0)
        TestCase.assertTrue(width != 0f)
        TestCase.assertTrue(height != 0f)
    }

    @Test
    fun testGetPageDimensions_Invalid() {
        _pdfManager!!.setPDF("")
        val status = _pdfManager!!.openDocument()
        TestCase.assertEquals(status, PDFFileManager.PDF_OPEN_FAILED)
        val width = _pdfManager!!.getPageWidth(0)
        val height = _pdfManager!!.getPageHeight(0)
        TestCase.assertTrue(width == 0f)
        TestCase.assertTrue(height == 0f)
    }

    @Test
    fun testGetPageDimensions_NegativePage() {
        _pdfManager!!.setPDF(_pdfPath)
        val status = _pdfManager!!.openDocument()
        TestCase.assertEquals(status, PDFFileManager.PDF_OK)
        val width = _pdfManager!!.getPageWidth(-1)
        val height = _pdfManager!!.getPageHeight(-1)
        TestCase.assertTrue(width == 0f)
        TestCase.assertTrue(height == 0f)
    }

    @Test
    fun testGetPageDimensions_GreaterThanPageCount() {
        _pdfManager!!.setPDF(_pdfPath)
        val status = _pdfManager!!.openDocument()
        TestCase.assertEquals(status, PDFFileManager.PDF_OK)
        val width = _pdfManager!!.getPageWidth(_pdfManager!!.pageCount)
        val height = _pdfManager!!.getPageHeight(_pdfManager!!.pageCount)
        TestCase.assertTrue(width == 0f)
        TestCase.assertTrue(height == 0f)
    }

    //================================================================================
    // Tests - is PDF landscape
    //================================================================================
    @Test
    fun testIsPDFLandscape_Valid() {
        _pdfManager!!.setPDF(_pdfPath)
        val status = _pdfManager!!.openDocument()
        TestCase.assertEquals(status, PDFFileManager.PDF_OK)
        val isLandscape = _pdfManager!!.isPDFLandscape
        TestCase.assertFalse(isLandscape)
    }

    @Test
    fun testIsPDFLandscape_Invalid() {
        _pdfManager!!.setPDF("")
        val status = _pdfManager!!.openDocument()
        TestCase.assertEquals(status, PDFFileManager.PDF_OPEN_FAILED)
        val isLandscape = _pdfManager!!.isPDFLandscape
        TestCase.assertFalse(isLandscape)
    }

    @Test
    fun testIsPDFLandscape_LandscapeValid() {
        _pdfManager!!.setPDF(_landscapePDF)
        val status = _pdfManager!!.openDocument()
        TestCase.assertEquals(status, PDFFileManager.PDF_OK)
        val isLandscape = _pdfManager!!.isPDFLandscape
        TestCase.assertTrue(isLandscape)
    }

    //================================================================================
    // Tests - get page
    //================================================================================
    @Test
    fun testGetPageBitmap_Valid() {
        _pdfManager!!.setPDF(_pdfPath)
        val status = _pdfManager!!.openDocument()
        TestCase.assertEquals(status, PDFFileManager.PDF_OK)
        val bmp = _pdfManager!!.getPageBitmap(0)
        TestCase.assertNotNull(bmp)
    }

    @Test
    fun testGetPageBitmap_Flip() {
        _pdfManager!!.setPDF(_pdfPath)
        val status = _pdfManager!!.openDocument()
        TestCase.assertEquals(status, PDFFileManager.PDF_OK)
        val bmp = _pdfManager!!.getPageBitmap(0, 1.0f, true, true)
        TestCase.assertNotNull(bmp)
    }

    @Test
    fun testGetPageBitmap_Uninitialized() {
        _pdfManager!!.setPDF(_pdfPath)
        val bmp = _pdfManager!!.getPageBitmap(0)
        TestCase.assertNull(bmp)
    }

    @Test
    fun testGetPageBitmap_NegativePageNumber() {
        _pdfManager!!.setPDF(_pdfPath)
        val status = _pdfManager!!.openDocument()
        TestCase.assertEquals(status, PDFFileManager.PDF_OK)
        val bmp = _pdfManager!!.getPageBitmap(-1)
        TestCase.assertNull(bmp)
    }

    @Test
    fun testGetPageBitmap_GreaterThanPageCount() {
        _pdfManager!!.setPDF(_pdfPath)
        val status = _pdfManager!!.openDocument()
        TestCase.assertEquals(status, PDFFileManager.PDF_OK)
        val bmp = _pdfManager!!.getPageBitmap(_pdfManager!!.pageCount)
        TestCase.assertNull(bmp)
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
    @Test
    fun testTestDocument_NullPdfPath() {
        val status = _pdfManager!!.testDocument(null)
        TestCase.assertEquals(status, PDFFileManager.PDF_OPEN_FAILED)
    }

    @Test
    fun testTestDocument_InvalidPdfPath() {
        val status = _pdfManager!!.testDocument("not_a_valid_path")
        TestCase.assertEquals(status, PDFFileManager.PDF_OPEN_FAILED)
    }

    @Test
    fun testTestDocument_Valid() {
        val status = _pdfManager!!.testDocument(_pdfPath)
        TestCase.assertEquals(status, PDFFileManager.PDF_OK)
    }

    @Test
    fun testTestDocument_EncryptedPath() {
        val status = _pdfManager!!.testDocument(_encryptedPdfPath)
        TestCase.assertEquals(status, PDFFileManager.PDF_ENCRYPTED)
    }

    @Test
    fun testTestDocument_PrintingDisallowed() {
        val status = _pdfManager!!.testDocument(_printingDisallowed)
        TestCase.assertEquals(status, PDFFileManager.PDF_PRINT_RESTRICTED)
    }

    @Test
    fun testTestDocument_EmptyPdfPath() {
        val status = _pdfManager!!.testDocument("")
        TestCase.assertEquals(status, PDFFileManager.PDF_OPEN_FAILED)
    }

    //================================================================================
    // Tests - open
    //================================================================================
    @Test
    fun testOpenDocument_NullPdfPath() {
        _pdfManager!!.setPDF(null)
        val status = _pdfManager!!.openDocument()
        TestCase.assertEquals(status, PDFFileManager.PDF_OPEN_FAILED)
    }

    @Test
    fun testOpenDocument_InvalidPdfPath() {
        _pdfManager!!.setPDF("not_a_valid_path")
        val status = _pdfManager!!.openDocument()
        TestCase.assertEquals(status, PDFFileManager.PDF_OPEN_FAILED)
    }

    @Test
    fun testOpenDocument_Valid() {
        _pdfManager!!.setPDF(_pdfPath)
        val status = _pdfManager!!.openDocument()
        TestCase.assertEquals(status, PDFFileManager.PDF_OK)
    }

    @Test
    fun testOpenDocument_EncryptedPath() {
        _pdfManager!!.setPDF(_encryptedPdfPath)
        val status = _pdfManager!!.openDocument()
        TestCase.assertEquals(status, PDFFileManager.PDF_ENCRYPTED)
    }

    @Test
    fun testOpenDocument_PrintingDisallowed() {
        _pdfManager!!.setPDF(_printingDisallowed)
        val status = _pdfManager!!.openDocument()
        TestCase.assertEquals(status, PDFFileManager.PDF_PRINT_RESTRICTED)
    }

    @Test
    fun testOpenDocument_EmptyPdfPath() {
        _pdfManager!!.setPDF("")
        val status = _pdfManager!!.openDocument()
        TestCase.assertEquals(status, PDFFileManager.PDF_OPEN_FAILED)
    }

    @Test
    fun testOpenDocument_Opened() {
        _pdfManager!!.setPDF(_pdfPath)
        var status = _pdfManager!!.openDocument()
        TestCase.assertEquals(status, 0)
        status = _pdfManager!!.openDocument()
        TestCase.assertEquals(status, PDFFileManager.PDF_OK)
    }

    //================================================================================
    // Tests - close
    //================================================================================
    @Test
    fun testCloseDocument_Opened() {
        _pdfManager!!.setPDF(_pdfPath)
        val status = _pdfManager!!.openDocument()
        TestCase.assertEquals(status, PDFFileManager.PDF_OK)
        try {
            _pdfManager!!.closeDocument()
        } catch (e: Exception) {
            TestCase.fail() // No exception expected
        }
    }

    @Test
    fun testCloseDocument_NotOpened() {
        try {
            _pdfManager!!.setPDF(_pdfPath)
            _pdfManager!!.closeDocument()
        } catch (e: Exception) {
            TestCase.fail() // No exception expected
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
    @Test
    fun testDuration_SmallPDF() {
        _pdfManager!!.setPDF(_pdfPath)
        var time = System.currentTimeMillis().toDouble()
        val status = _pdfManager!!.openDocument()
        var duration = System.currentTimeMillis() - time
        TestCase.assertEquals(status, 0)
        TestCase.assertTrue(duration < 2000)
        time = System.currentTimeMillis().toDouble()
        _pdfManager!!.closeDocument()
        duration = System.currentTimeMillis() - time
        TestCase.assertTrue(duration < 500)
    }

    @Test
    fun testDuration_LargePDF() {
        _pdfManager!!.setPDF(_largePdfPath)
        var time = System.currentTimeMillis().toDouble()
        val status = _pdfManager!!.openDocument()
        var duration = System.currentTimeMillis() - time
        TestCase.assertEquals(status, 0)
        TestCase.assertTrue(duration < 5000)
        time = System.currentTimeMillis().toDouble()
        _pdfManager!!.closeDocument()
        duration = System.currentTimeMillis() - time
        TestCase.assertTrue(duration < 500)
    }

    //================================================================================
    // Private
    //================================================================================
    private fun getAssetPath(filename: String): String {
        val f = File(mainActivity!!.cacheDir.toString() + "/" + filename)
        val assetManager = InstrumentationRegistry.getInstrumentation().context.assets
        try {
            val `is` = assetManager.open(filename)
            val size = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            val fos = FileOutputStream(f)
            fos.write(buffer)
            fos.close()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
        return f.path
    }

    override fun onFileInitialized(status: Int) {
        _status = status
    }
}