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
import org.junit.Before
import org.junit.Test

class FileUtilsContentProviderTest : ProviderTestCase2<TestContentProvider>(
    TestContentProvider::class.java, TEST_AUTHORITY
) {
    private val TEST_FILENAME_PDF = "testFileName.pdf"
    private val TEST_FILENAME_NON_PDF = "testFileName.png"
    private val TEST_DISPLAYNAME_PDF = "testDisplayName.pdf"
    private val TEST_DISPLAYNAME_NON_PDF = "testDisplayName.png"
    private val TEST_FILESIZE = 999
    private val TEST_CONTENT_PREFIX = "content://"
    private val TEST_URI_PDF = "$TEST_CONTENT_PREFIX$TEST_AUTHORITY/$TEST_FILENAME_PDF"
    private val TEST_URI_NON_PDF =
        "$TEST_CONTENT_PREFIX$TEST_AUTHORITY/$TEST_FILENAME_NON_PDF"
    private val INVALID_COLUMNS = arrayOf("testColumn")
    private val INVALID_DATA = arrayOf("testData")
    private val VALID_COLUMNS = arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
    private val VALID_DATA_PDF = arrayOf(TEST_DISPLAYNAME_PDF, TEST_FILESIZE.toString())
    private val VALID_DATA_NON_PDF = arrayOf(TEST_DISPLAYNAME_NON_PDF, TEST_FILESIZE.toString())
    private var _testContext: Context? = null
    private var _testUri: Uri? = null
    private var _testCursor: MatrixCursor? = null

    @Deprecated("Deprecated in Java")
    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        _testContext = this.mockContext
    }

    // ================================================================================
    // Tests - get filename
    // ================================================================================
    @Test
    fun testGetFileName_NullContext() {
        TestCase.assertNull(getFileName(null, null, false))
        TestCase.assertNull(getFileName(null, null, true))
    }

    @Test
    fun testGetFileName_NullUri() {
        TestCase.assertNull(getFileName(_testContext, null, false))
        TestCase.assertNull(getFileName(_testContext, null, true))
    }

    @Test
    fun testGetFileName_NullScheme() {
        _testUri = Uri.parse("")
        TestCase.assertNull(getFileName(_testContext, _testUri, false))
        TestCase.assertNull(getFileName(_testContext, _testUri, true))
    }

    @Test
    fun testGetFileName_NullCursor_UriNoFileName() {
        this.provider!!.setQueryResult(null)
        _testUri = Uri.parse(TEST_CONTENT_PREFIX + TEST_AUTHORITY)
        TestCase.assertEquals(getFileName(_testContext, _testUri, false), "")
        TestCase.assertEquals(getFileName(_testContext, _testUri, true), "")
    }

    @Test
    fun testGetFileName_NullCursor_UriWithFileName() {
        this.provider!!.setQueryResult(null)
        _testUri = Uri.parse(TEST_URI_NON_PDF)
        TestCase.assertEquals(getFileName(_testContext, _testUri, false), TEST_FILENAME_NON_PDF)
        TestCase.assertEquals(getFileName(_testContext, _testUri, true), TEST_FILENAME_PDF)
        _testUri = Uri.parse(TEST_URI_PDF)
        TestCase.assertEquals(getFileName(_testContext, _testUri, false), TEST_FILENAME_PDF)
        TestCase.assertEquals(getFileName(_testContext, _testUri, true), TEST_FILENAME_PDF)
    }

    @Test
    fun testGetFileName_EmptyCursor_UriNoFileName() {
        _testCursor = MatrixCursor(VALID_COLUMNS, 1)
        this.provider!!.setQueryResult(_testCursor)
        _testUri = Uri.parse(TEST_CONTENT_PREFIX + TEST_AUTHORITY)
        TestCase.assertEquals(getFileName(_testContext, _testUri, false), "")
        TestCase.assertEquals(getFileName(_testContext, _testUri, true), "")
    }

    @Test
    fun testGetFileName_EmptyCursor_UriWithFileName() {
        _testCursor = MatrixCursor(VALID_COLUMNS, 1)
        this.provider!!.setQueryResult(_testCursor)
        _testUri = Uri.parse(TEST_URI_NON_PDF)
        TestCase.assertEquals(getFileName(_testContext, _testUri, false), TEST_FILENAME_NON_PDF)
        TestCase.assertEquals(getFileName(_testContext, _testUri, true), TEST_FILENAME_PDF)
        _testUri = Uri.parse(TEST_URI_PDF)
        TestCase.assertEquals(getFileName(_testContext, _testUri, false), TEST_FILENAME_PDF)
        TestCase.assertEquals(getFileName(_testContext, _testUri, true), TEST_FILENAME_PDF)
    }

    @Test
    fun testGetFileName_NoDisplayName_UriNoFileName() {
        _testCursor = MatrixCursor(INVALID_COLUMNS, 1)
        _testCursor!!.addRow(INVALID_DATA)
        this.provider!!.setQueryResult(_testCursor)
        _testUri = Uri.parse(TEST_CONTENT_PREFIX + TEST_AUTHORITY)
        TestCase.assertEquals(getFileName(_testContext, _testUri, false), "")
        TestCase.assertEquals(getFileName(_testContext, _testUri, true), "")
        _testUri = Uri.parse(TEST_CONTENT_PREFIX + TEST_AUTHORITY)
        TestCase.assertEquals(getFileName(_testContext, _testUri, false), "")
        TestCase.assertEquals(getFileName(_testContext, _testUri, true), "")
    }

    @Test
    fun testGetFileName_NoDisplayName_UriWithFileName() {
        _testCursor = MatrixCursor(INVALID_COLUMNS, 1)
        _testCursor!!.addRow(INVALID_DATA)
        this.provider!!.setQueryResult(_testCursor)
        _testUri = Uri.parse(TEST_URI_NON_PDF)
        TestCase.assertEquals(getFileName(_testContext, _testUri, false), TEST_FILENAME_NON_PDF)
        TestCase.assertEquals(getFileName(_testContext, _testUri, true), TEST_FILENAME_PDF)
        _testUri = Uri.parse(TEST_URI_PDF)
        TestCase.assertEquals(getFileName(_testContext, _testUri, false), TEST_FILENAME_PDF)
        TestCase.assertEquals(getFileName(_testContext, _testUri, true), TEST_FILENAME_PDF)
    }

    @Test
    fun testGetFileName_WithDisplayName() {
        _testCursor = MatrixCursor(VALID_COLUMNS, 1)
        _testCursor!!.addRow(VALID_DATA_NON_PDF)
        this.provider!!.setQueryResult(_testCursor)
        _testUri = Uri.parse(TEST_URI_NON_PDF)
        TestCase.assertEquals(getFileName(_testContext, _testUri, false), TEST_DISPLAYNAME_NON_PDF)
        TestCase.assertEquals(getFileName(_testContext, _testUri, true), TEST_DISPLAYNAME_PDF)
        _testCursor = MatrixCursor(VALID_COLUMNS, 1)
        _testCursor!!.addRow(VALID_DATA_PDF)
        this.provider!!.setQueryResult(_testCursor)
        _testUri = Uri.parse(TEST_URI_PDF)
        TestCase.assertEquals(getFileName(_testContext, _testUri, false), TEST_DISPLAYNAME_PDF)
        TestCase.assertEquals(getFileName(_testContext, _testUri, true), TEST_DISPLAYNAME_PDF)
    }

    // ================================================================================
    // Tests - get filesize
    // ================================================================================
    @Test
    fun testGetFileSize_NullContext() {
        TestCase.assertEquals(getFileSize(null, null), 0)
    }

    @Test
    fun testGetFileSize_NullUri() {
        TestCase.assertEquals(getFileSize(_testContext, null), 0)
    }

    @Test
    fun testGetFileSize_NullScheme() {
        _testUri = Uri.parse("")
        TestCase.assertEquals(getFileSize(_testContext, _testUri), 0)
    }

    @Test
    fun testGetFileSize_NullCursor() {
        this.provider!!.setQueryResult(null)
        _testUri = Uri.parse(TEST_URI_PDF)
        TestCase.assertEquals(getFileSize(_testContext, _testUri), 0)
    }

    @Test
    fun testGetFileSize_NullPath() {
        // https://stackoverflow.com/questions/18519260/is-it-ever-possible-for-the-getpath-method-of-a-java-net-uri-object-to-return-nu
        _testUri = Uri.parse("news:$TEST_FILENAME_PDF")
        TestCase.assertEquals(getFileSize(_testContext, _testUri), 0)
        _testUri = Uri.parse("mailto:$TEST_FILENAME_PDF")
        TestCase.assertEquals(getFileSize(_testContext, _testUri), 0)
        _testUri = Uri.parse("urn:isbn:$TEST_FILENAME_PDF")
        TestCase.assertEquals(getFileSize(_testContext, _testUri), 0)
    }

    @Test
    fun testGetFileSize_EmptyCursor() {
        _testCursor = MatrixCursor(VALID_COLUMNS, 1)
        this.provider!!.setQueryResult(_testCursor)
        _testUri = Uri.parse(TEST_URI_PDF)
        TestCase.assertEquals(getFileSize(_testContext, _testUri), 0)
    }

    @Test
    fun testGetFileSize_NoSize() {
        _testCursor = MatrixCursor(INVALID_COLUMNS, 1)
        _testCursor!!.addRow(INVALID_DATA)
        this.provider!!.setQueryResult(_testCursor)
        _testUri = Uri.parse(TEST_URI_PDF)
        TestCase.assertEquals(getFileSize(_testContext, _testUri), 0)
    }

    @Test
    fun testGetFileSize_WithSize() {
        _testCursor = MatrixCursor(VALID_COLUMNS, 1)
        _testCursor!!.addRow(VALID_DATA_PDF)
        this.provider!!.setQueryResult(_testCursor)
        _testUri = Uri.parse(TEST_URI_PDF)
        TestCase.assertEquals(getFileSize(_testContext, _testUri), TEST_FILESIZE)
    }

    // ================================================================================
    // Tests - get mimetype
    // ================================================================================
    @Test
    fun testGetMimeType_NullContext() {
        TestCase.assertEquals(getMimeType(null, null), "")
    }

    @Test
    fun testGetMimeType_NullUri() {
        TestCase.assertEquals(getMimeType(_testContext, null), "")
    }

    @Test
    fun testGetMimeType_NullScheme() {
        _testUri = Uri.parse("")
        TestCase.assertEquals(getMimeType(_testContext, _testUri), "")
    }

    @Test
    fun testGetMimeType() {
        val testType = "testType"
        this.provider!!.setMimeType(testType)
        _testUri = Uri.parse(TEST_URI_PDF)
        TestCase.assertEquals(getMimeType(_testContext, _testUri), testType)
        _testUri = Uri.parse(TEST_URI_NON_PDF)
        TestCase.assertEquals(getMimeType(_testContext, _testUri), testType)
    }

    // ================================================================================
    // Test class
    // ================================================================================
    class TestContentProvider : MockContentProvider() {
        private var _queryResult: Cursor? = null
        private var _mimeType: String? = null
        fun setQueryResult(result: Cursor?) {
            _queryResult = result
        }

        fun setMimeType(mimeType: String?) {
            _mimeType = mimeType
        }

        override fun query(
            uri: Uri,
            projection: Array<String>?,
            selection: String?,
            selectionArgs: Array<String>?,
            sortOrder: String?
        ): Cursor? {
            return _queryResult
        }

        override fun getType(uri: Uri): String? {
            return _mimeType
        }
    }

    companion object {
        const val TEST_AUTHORITY = "testAuthority"
    }
}