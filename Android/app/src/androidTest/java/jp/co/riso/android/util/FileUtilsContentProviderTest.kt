package jp.co.riso.android.util

import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.provider.OpenableColumns
import android.test.ProviderTestCase2
import android.test.mock.MockContentProvider
import jp.co.riso.android.util.FileUtils.getFileName
import jp.co.riso.android.util.FileUtils.getFileSize
import jp.co.riso.android.util.FileUtils.getMimeType
import jp.co.riso.android.util.FileUtilsContentProviderTest.TestContentProvider
import junit.framework.TestCase

class FileUtilsContentProviderTest : ProviderTestCase2<TestContentProvider>(
    TestContentProvider::class.java, TEST_AUTHORITY
) {
    private val TEST_FILENAME_PDF = "testFileName.pdf"
    private val TEST_FILENAME_NON_PDF = "testFileName.png"
    private val TEST_DISPLAYNAME_PDF = "testDisplayName.pdf"
    private val TEST_DISPLAYNAME_NON_PDF = "testDisplayName.png"
    private val TEST_FILESIZE = 999
    private val TEST_CONTENT_PREFIX = "content://"
    private val TEST_URI_PDF = TEST_CONTENT_PREFIX + TEST_AUTHORITY + '/' + TEST_FILENAME_PDF
    private val TEST_URI_NON_PDF =
        TEST_CONTENT_PREFIX + TEST_AUTHORITY + '/' + TEST_FILENAME_NON_PDF
    private val INVALID_COLUMNS = arrayOf("testColumn")
    private val INVALID_DATA = arrayOf("testData")
    private val VALID_COLUMNS = arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
    private val VALID_DATA_PDF = arrayOf(TEST_DISPLAYNAME_PDF, TEST_FILESIZE.toString())
    private val VALID_DATA_NON_PDF = arrayOf(TEST_DISPLAYNAME_NON_PDF, TEST_FILESIZE.toString())
    private var mTestContext: Context? = null
    private var testUri: Uri? = null
    private var testCursor: MatrixCursor? = null
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        mTestContext = this.mockContext
    }

    @Throws(Exception::class)
    override fun tearDown() {
        super.tearDown()
    }

    // ================================================================================
    // Tests - get filename
    // ================================================================================
    fun testGetFileName_NullContext() {
        TestCase.assertNull(getFileName(null, null, false))
        TestCase.assertNull(getFileName(null, null, true))
    }

    fun testGetFileName_NullUri() {
        TestCase.assertNull(getFileName(mTestContext, null, false))
        TestCase.assertNull(getFileName(mTestContext, null, true))
    }

    fun testGetFileName_NullScheme() {
        testUri = Uri.parse("")
        TestCase.assertNull(getFileName(mTestContext, testUri, false))
        TestCase.assertNull(getFileName(mTestContext, testUri, true))
    }

    fun testGetFileName_NullCursor_UriNoFileName() {
        this.provider!!.setQueryResult(null)
        testUri = Uri.parse(TEST_CONTENT_PREFIX + TEST_AUTHORITY)
        TestCase.assertEquals(getFileName(mTestContext, testUri, false), "")
        TestCase.assertEquals(getFileName(mTestContext, testUri, true), "")
    }

    fun testGetFileName_NullCursor_UriWithFileName() {
        this.provider!!.setQueryResult(null)
        testUri = Uri.parse(TEST_URI_NON_PDF)
        TestCase.assertEquals(getFileName(mTestContext, testUri, false), TEST_FILENAME_NON_PDF)
        TestCase.assertEquals(getFileName(mTestContext, testUri, true), TEST_FILENAME_PDF)
        testUri = Uri.parse(TEST_URI_PDF)
        TestCase.assertEquals(getFileName(mTestContext, testUri, false), TEST_FILENAME_PDF)
        TestCase.assertEquals(getFileName(mTestContext, testUri, true), TEST_FILENAME_PDF)
    }

    fun testGetFileName_EmptyCursor_UriNoFileName() {
        testCursor = MatrixCursor(VALID_COLUMNS, 1)
        this.provider!!.setQueryResult(testCursor)
        testUri = Uri.parse(TEST_CONTENT_PREFIX + TEST_AUTHORITY)
        TestCase.assertEquals(getFileName(mTestContext, testUri, false), "")
        TestCase.assertEquals(getFileName(mTestContext, testUri, true), "")
    }

    fun testGetFileName_EmptyCursor_UriWithFileName() {
        testCursor = MatrixCursor(VALID_COLUMNS, 1)
        this.provider!!.setQueryResult(testCursor)
        testUri = Uri.parse(TEST_URI_NON_PDF)
        TestCase.assertEquals(getFileName(mTestContext, testUri, false), TEST_FILENAME_NON_PDF)
        TestCase.assertEquals(getFileName(mTestContext, testUri, true), TEST_FILENAME_PDF)
        testUri = Uri.parse(TEST_URI_PDF)
        TestCase.assertEquals(getFileName(mTestContext, testUri, false), TEST_FILENAME_PDF)
        TestCase.assertEquals(getFileName(mTestContext, testUri, true), TEST_FILENAME_PDF)
    }

    fun testGetFileName_NoDisplayName_UriNoFileName() {
        testCursor = MatrixCursor(INVALID_COLUMNS, 1)
        testCursor!!.addRow(INVALID_DATA)
        this.provider!!.setQueryResult(testCursor)
        testUri = Uri.parse(TEST_CONTENT_PREFIX + TEST_AUTHORITY)
        TestCase.assertEquals(getFileName(mTestContext, testUri, false), "")
        TestCase.assertEquals(getFileName(mTestContext, testUri, true), "")
        testUri = Uri.parse(TEST_CONTENT_PREFIX + TEST_AUTHORITY)
        TestCase.assertEquals(getFileName(mTestContext, testUri, false), "")
        TestCase.assertEquals(getFileName(mTestContext, testUri, true), "")
    }

    fun testGetFileName_NoDisplayName_UriWithFileName() {
        testCursor = MatrixCursor(INVALID_COLUMNS, 1)
        testCursor!!.addRow(INVALID_DATA)
        this.provider!!.setQueryResult(testCursor)
        testUri = Uri.parse(TEST_URI_NON_PDF)
        TestCase.assertEquals(getFileName(mTestContext, testUri, false), TEST_FILENAME_NON_PDF)
        TestCase.assertEquals(getFileName(mTestContext, testUri, true), TEST_FILENAME_PDF)
        testUri = Uri.parse(TEST_URI_PDF)
        TestCase.assertEquals(getFileName(mTestContext, testUri, false), TEST_FILENAME_PDF)
        TestCase.assertEquals(getFileName(mTestContext, testUri, true), TEST_FILENAME_PDF)
    }

    fun testGetFileName_WithDisplayName() {
        testCursor = MatrixCursor(VALID_COLUMNS, 1)
        testCursor!!.addRow(VALID_DATA_NON_PDF)
        this.provider!!.setQueryResult(testCursor)
        testUri = Uri.parse(TEST_URI_NON_PDF)
        TestCase.assertEquals(getFileName(mTestContext, testUri, false), TEST_DISPLAYNAME_NON_PDF)
        TestCase.assertEquals(getFileName(mTestContext, testUri, true), TEST_DISPLAYNAME_PDF)
        testCursor = MatrixCursor(VALID_COLUMNS, 1)
        testCursor!!.addRow(VALID_DATA_PDF)
        this.provider!!.setQueryResult(testCursor)
        testUri = Uri.parse(TEST_URI_PDF)
        TestCase.assertEquals(getFileName(mTestContext, testUri, false), TEST_DISPLAYNAME_PDF)
        TestCase.assertEquals(getFileName(mTestContext, testUri, true), TEST_DISPLAYNAME_PDF)
    }

    // ================================================================================
    // Tests - get filesize
    // ================================================================================
    fun testGetFileSize_NullContext() {
        TestCase.assertEquals(getFileSize(null, null), 0)
    }

    fun testGetFileSize_NullUri() {
        TestCase.assertEquals(getFileSize(mTestContext, null), 0)
    }

    fun testGetFileSize_NullScheme() {
        testUri = Uri.parse("")
        TestCase.assertEquals(getFileSize(mTestContext, testUri), 0)
    }

    fun testGetFileSize_NullCursor() {
        this.provider!!.setQueryResult(null)
        testUri = Uri.parse(TEST_URI_PDF)
        TestCase.assertEquals(getFileSize(mTestContext, testUri), 0)
    }

    fun testGetFileSize_NullPath() {
        // https://stackoverflow.com/questions/18519260/is-it-ever-possible-for-the-getpath-method-of-a-java-net-uri-object-to-return-nu
        testUri = Uri.parse("news:$TEST_FILENAME_PDF")
        TestCase.assertEquals(getFileSize(mTestContext, testUri), 0)
        testUri = Uri.parse("mailto:$TEST_FILENAME_PDF")
        TestCase.assertEquals(getFileSize(mTestContext, testUri), 0)
        testUri = Uri.parse("urn:isbn:$TEST_FILENAME_PDF")
        TestCase.assertEquals(getFileSize(mTestContext, testUri), 0)
    }

    fun testGetFileSize_EmptyCursor() {
        testCursor = MatrixCursor(VALID_COLUMNS, 1)
        this.provider!!.setQueryResult(testCursor)
        testUri = Uri.parse(TEST_URI_PDF)
        TestCase.assertEquals(getFileSize(mTestContext, testUri), 0)
    }

    fun testGetFileSize_NoSize() {
        testCursor = MatrixCursor(INVALID_COLUMNS, 1)
        testCursor!!.addRow(INVALID_DATA)
        this.provider!!.setQueryResult(testCursor)
        testUri = Uri.parse(TEST_URI_PDF)
        TestCase.assertEquals(getFileSize(mTestContext, testUri), 0)
    }

    fun testGetFileSize_WithSize() {
        testCursor = MatrixCursor(VALID_COLUMNS, 1)
        testCursor!!.addRow(VALID_DATA_PDF)
        this.provider!!.setQueryResult(testCursor)
        testUri = Uri.parse(TEST_URI_PDF)
        TestCase.assertEquals(getFileSize(mTestContext, testUri), TEST_FILESIZE)
    }

    // ================================================================================
    // Tests - get mimetype
    // ================================================================================
    fun testGetMimeType_NullContext() {
        TestCase.assertEquals(getMimeType(null, null), "")
    }

    fun testGetMimeType_NullUri() {
        TestCase.assertEquals(getMimeType(mTestContext, null), "")
    }

    fun testGetMimeType_NullScheme() {
        testUri = Uri.parse("")
        TestCase.assertEquals(getMimeType(mTestContext, testUri), "")
    }

    fun testGetMimeType() {
        val TEST_TYPE = "testType"
        this.provider!!.setMimeType(TEST_TYPE)
        testUri = Uri.parse(TEST_URI_PDF)
        TestCase.assertEquals(getMimeType(mTestContext, testUri), TEST_TYPE)
        testUri = Uri.parse(TEST_URI_NON_PDF)
        TestCase.assertEquals(getMimeType(mTestContext, testUri), TEST_TYPE)
    }

    // ================================================================================
    // Test class
    // ================================================================================
    class TestContentProvider : MockContentProvider() {
        private var mQueryResult: Cursor? = null
        private var mMimeType: String? = null
        fun setQueryResult(result: Cursor?) {
            mQueryResult = result
        }

        fun setMimeType(mimeType: String?) {
            mMimeType = mimeType
        }

        override fun query(
            uri: Uri,
            projection: Array<String>?,
            selection: String?,
            selectionArgs: Array<String>?,
            sortOrder: String?
        ): Cursor? {
            return mQueryResult
        }

        override fun getType(uri: Uri): String? {
            return mMimeType
        }
    }

    companion object {
        private const val TEST_AUTHORITY = "testAuthority"
    }
}