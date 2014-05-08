
package jp.co.riso.android.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import android.content.res.AssetManager;
import android.test.ActivityInstrumentationTestCase2;

public class FileUtilsTest extends ActivityInstrumentationTestCase2<MainActivity> {
    final private static String TEST_SRC_FILE = "PDF-squarish.pdf";
    final private static String TEST_DST_FILE = "sample_dst.pdf";

    private File mSrcFile = null;
    private File mDstFile = null;

    public FileUtilsTest() {
        super(MainActivity.class);
    }

    public FileUtilsTest(Class<MainActivity> activityClass) {
        super(activityClass);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    // ================================================================================
    // Tests - constructors
    // ================================================================================

    public void testConstructor() {
        FileUtils utils = new FileUtils();
        assertNotNull(utils);
    }

    // ================================================================================
    // Tests - copy
    // ================================================================================

    public void testCopy_ValidParams() {
        try {
            String pdfPath = getAssetPath(TEST_SRC_FILE);

            mSrcFile = new File(pdfPath);
            if (mSrcFile == null) {
                fail();
            }

            mDstFile = new File(SmartDeviceApp.getAppContext().getExternalFilesDir("pdfs") + "/"
                    + TEST_DST_FILE);
            if (mDstFile == null) {
                fail();
            }

            FileUtils.copy(mSrcFile, mDstFile);
        } catch (NullPointerException e) {
            fail();
        } catch (Exception e) {
            fail();
        }
    }

    public void testCopy_NullSrc() {
        try {
            FileUtils.copy(null, mDstFile);
        } catch (IOException e) {
            fail();
        } catch (Exception e) {
            fail();
        }
    }

    public void testCopy_NullDst() {
        try {
            FileUtils.copy(mSrcFile, null);
        } catch (IOException e) {
            fail();
        } catch (Exception e) {
            fail();
        }
    }

    // ================================================================================
    // Private
    // ================================================================================

    private String getAssetPath(String filename) {
        File f = new File(SmartDeviceApp.getAppContext().getCacheDir() + "/" + filename);
        AssetManager assetManager = SmartDeviceApp.getAppContext().getAssets();

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

}
