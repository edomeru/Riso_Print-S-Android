
package jp.co.riso.android.util;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentProvider;

public class FileUtilsContentProviderTest extends ProviderTestCase2<FileUtilsContentProviderTest.TestContentProvider> {
    final private static String TEST_AUTHORITY = "testAuthority";
    final private String TEST_FILENAME_PDF = "testFileName.pdf";
    final private String TEST_FILENAME_NON_PDF = "testFileName.png";
    final private String TEST_DISPLAYNAME_PDF = "testDisplayName.pdf";
    final private String TEST_DISPLAYNAME_NON_PDF = "testDisplayName.png";
    final private int TEST_FILESIZE = 999;
    final private String TEST_CONTENT_PREFIX = "content://";
    final private String TEST_URI_PDF = TEST_CONTENT_PREFIX+TEST_AUTHORITY+'/'+TEST_FILENAME_PDF;
    final private String TEST_URI_NON_PDF = TEST_CONTENT_PREFIX+TEST_AUTHORITY+'/'+TEST_FILENAME_NON_PDF;
    final private String[] INVALID_COLUMNS = {"testColumn"};
    final private String[] INVALID_DATA = {"testData"};
    final private String[] VALID_COLUMNS = {OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE};
    final private String[] VALID_DATA_PDF = {TEST_DISPLAYNAME_PDF, String.valueOf(TEST_FILESIZE)};
    final private String[] VALID_DATA_NON_PDF = {TEST_DISPLAYNAME_NON_PDF, String.valueOf(TEST_FILESIZE)};

    private Context testContext = null;
    private Uri testUri = null;
    private MatrixCursor testCursor = null;

    public FileUtilsContentProviderTest() {
        super(TestContentProvider.class, TEST_AUTHORITY);
    }

    protected void setUp() throws Exception {
        super.setUp();
        testContext = this.getMockContext();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    // ================================================================================
    // Tests - get filename
    // ================================================================================

    public void testGetFileName_NullContext() {
        assertNull(FileUtils.getFileName(null, null, false));
        assertNull(FileUtils.getFileName(null, null, true));
    }

    public void testGetFileName_NullUri() {
        assertNull(FileUtils.getFileName(testContext, null, false));
        assertNull(FileUtils.getFileName(testContext, null, true));
    }

    public void testGetFileName_NullScheme() {
        testUri = Uri.parse("");
        assertNull(FileUtils.getFileName(testContext, testUri, false));
        assertNull(FileUtils.getFileName(testContext, testUri, true));
    }

    public void testGetFileName_NullCursor_UriNoFileName() {
        this.getProvider().setQueryResult(null);

        testUri = Uri.parse(TEST_CONTENT_PREFIX+TEST_AUTHORITY);
        assertEquals(FileUtils.getFileName(testContext, testUri, false), "");
        assertEquals(FileUtils.getFileName(testContext, testUri, true), "");
    }

    public void testGetFileName_NullCursor_UriWithFileName() {
        this.getProvider().setQueryResult(null);

        testUri = Uri.parse(TEST_URI_NON_PDF);
        assertEquals(FileUtils.getFileName(testContext, testUri, false), TEST_FILENAME_NON_PDF);
        assertEquals(FileUtils.getFileName(testContext, testUri, true), TEST_FILENAME_PDF);

        testUri = Uri.parse(TEST_URI_PDF);
        assertEquals(FileUtils.getFileName(testContext, testUri, false), TEST_FILENAME_PDF);
        assertEquals(FileUtils.getFileName(testContext, testUri, true), TEST_FILENAME_PDF);
    }

    public void testGetFileName_EmptyCursor_UriNoFileName() {
        testCursor = new MatrixCursor(VALID_COLUMNS, 1);
        this.getProvider().setQueryResult(testCursor);

        testUri = Uri.parse(TEST_CONTENT_PREFIX+TEST_AUTHORITY);
        assertEquals(FileUtils.getFileName(testContext, testUri, false), "");
        assertEquals(FileUtils.getFileName(testContext, testUri, true), "");
    }

    public void testGetFileName_EmptyCursor_UriWithFileName() {
        testCursor = new MatrixCursor(VALID_COLUMNS, 1);
        this.getProvider().setQueryResult(testCursor);

        testUri = Uri.parse(TEST_URI_NON_PDF);
        assertEquals(FileUtils.getFileName(testContext, testUri, false), TEST_FILENAME_NON_PDF);
        assertEquals(FileUtils.getFileName(testContext, testUri, true), TEST_FILENAME_PDF);

        testUri = Uri.parse(TEST_URI_PDF);
        assertEquals(FileUtils.getFileName(testContext, testUri, false), TEST_FILENAME_PDF);
        assertEquals(FileUtils.getFileName(testContext, testUri, true), TEST_FILENAME_PDF);
    }

    public void testGetFileName_NoDisplayName_UriNoFileName() {
        testCursor = new MatrixCursor(INVALID_COLUMNS, 1);
        testCursor.addRow(INVALID_DATA);
        this.getProvider().setQueryResult(testCursor);

        testUri = Uri.parse(TEST_CONTENT_PREFIX+TEST_AUTHORITY);
        assertEquals(FileUtils.getFileName(testContext, testUri, false), "");
        assertEquals(FileUtils.getFileName(testContext, testUri, true), "");

        testUri = Uri.parse(TEST_CONTENT_PREFIX+TEST_AUTHORITY);
        assertEquals(FileUtils.getFileName(testContext, testUri, false), "");
        assertEquals(FileUtils.getFileName(testContext, testUri, true), "");
    }

    public void testGetFileName_NoDisplayName_UriWithFileName() {
        testCursor = new MatrixCursor(INVALID_COLUMNS, 1);
        testCursor.addRow(INVALID_DATA);
        this.getProvider().setQueryResult(testCursor);

        testUri = Uri.parse(TEST_URI_NON_PDF);
        assertEquals(FileUtils.getFileName(testContext, testUri, false), TEST_FILENAME_NON_PDF);
        assertEquals(FileUtils.getFileName(testContext, testUri, true), TEST_FILENAME_PDF);

        testUri = Uri.parse(TEST_URI_PDF);
        assertEquals(FileUtils.getFileName(testContext, testUri, false), TEST_FILENAME_PDF);
        assertEquals(FileUtils.getFileName(testContext, testUri, true), TEST_FILENAME_PDF);
    }

    public void testGetFileName_WithDisplayName() {
        testCursor = new MatrixCursor(VALID_COLUMNS, 1);
        testCursor.addRow(VALID_DATA_NON_PDF);
        this.getProvider().setQueryResult(testCursor);

        testUri = Uri.parse(TEST_URI_NON_PDF);
        assertEquals(FileUtils.getFileName(testContext, testUri, false), TEST_DISPLAYNAME_NON_PDF);
        assertEquals(FileUtils.getFileName(testContext, testUri, true), TEST_DISPLAYNAME_PDF);

        testCursor = new MatrixCursor(VALID_COLUMNS, 1);
        testCursor.addRow(VALID_DATA_PDF);
        this.getProvider().setQueryResult(testCursor);

        testUri = Uri.parse(TEST_URI_PDF);
        assertEquals(FileUtils.getFileName(testContext, testUri, false), TEST_DISPLAYNAME_PDF);
        assertEquals(FileUtils.getFileName(testContext, testUri, true), TEST_DISPLAYNAME_PDF);
    }

    // ================================================================================
    // Tests - get filesize
    // ================================================================================

    public void testGetFileSize_NullContext() {
        assertEquals(FileUtils.getFileSize(null, null), 0);
    }

    public void testGetFileSize_NullUri() {
        assertEquals(FileUtils.getFileSize(testContext, null), 0);
    }

    public void testGetFileSize_NullScheme() {
        testUri = Uri.parse("");
        assertEquals(FileUtils.getFileSize(testContext, testUri), 0);
    }

    public void testGetFileSize_NullCursor() {
        this.getProvider().setQueryResult(null);
        testUri = Uri.parse(TEST_URI_PDF);
        assertEquals(FileUtils.getFileSize(testContext, testUri), 0);
    }

    public void testGetFileSize_NullPath() {
        // https://stackoverflow.com/questions/18519260/is-it-ever-possible-for-the-getpath-method-of-a-java-net-uri-object-to-return-nu
        testUri = Uri.parse("news:"+TEST_FILENAME_PDF);
        assertEquals(FileUtils.getFileSize(testContext, testUri), 0);

        testUri = Uri.parse("mailto:"+TEST_FILENAME_PDF);
        assertEquals(FileUtils.getFileSize(testContext, testUri), 0);

        testUri = Uri.parse("urn:isbn:"+TEST_FILENAME_PDF);
        assertEquals(FileUtils.getFileSize(testContext, testUri), 0);
    }

    public void testGetFileSize_EmptyCursor() {
        testCursor = new MatrixCursor(VALID_COLUMNS, 1);
        this.getProvider().setQueryResult(testCursor);

        testUri = Uri.parse(TEST_URI_PDF);
        assertEquals(FileUtils.getFileSize(testContext, testUri), 0);
    }

    public void testGetFileSize_NoSize() {
        testCursor = new MatrixCursor(INVALID_COLUMNS, 1);
        testCursor.addRow(INVALID_DATA);
        this.getProvider().setQueryResult(testCursor);

        testUri = Uri.parse(TEST_URI_PDF);
        assertEquals(FileUtils.getFileSize(testContext, testUri), 0);
    }

    public void testGetFileSize_WithSize() {
        testCursor = new MatrixCursor(VALID_COLUMNS, 1);
        testCursor.addRow(VALID_DATA_PDF);
        this.getProvider().setQueryResult(testCursor);

        testUri = Uri.parse(TEST_URI_PDF);
        assertEquals(FileUtils.getFileSize(testContext, testUri), TEST_FILESIZE);
    }

    // ================================================================================
    // Tests - get mimetype
    // ================================================================================

    public void testGetMimeType_NullContext() {
        assertEquals(FileUtils.getMimeType(null, null), "");
    }

    public void testGetMimeType_NullUri() {
        assertEquals(FileUtils.getMimeType(testContext, null), "");
    }

    public void testGetMimeType_NullScheme() {
        testUri = Uri.parse("");
        assertEquals(FileUtils.getMimeType(testContext, testUri), "");
    }

    public void testGetMimeType() {
        final String TEST_TYPE = "testType";
        this.getProvider().setMimeType(TEST_TYPE);

        testUri = Uri.parse(TEST_URI_PDF);
        assertEquals(FileUtils.getMimeType(testContext, testUri), TEST_TYPE);

        testUri = Uri.parse(TEST_URI_NON_PDF);
        assertEquals(FileUtils.getMimeType(testContext, testUri), TEST_TYPE);
    }

    // ================================================================================
    // Test class
    // ================================================================================

    public static class TestContentProvider extends MockContentProvider {
        private Cursor mQueryResult = null;
        private String mMimeType = null;

        public TestContentProvider() {}

        public void setQueryResult(Cursor result) {
            mQueryResult = result;
        }

        public void setMimeType(String mimeType) {
            mMimeType = mimeType;
        }

        @Override
        public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
            return mQueryResult;
        }

        @Override
        public String getType(Uri uri) {
            return mMimeType;
        }
    }

}
