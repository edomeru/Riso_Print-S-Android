package jp.co.riso.smartdeviceapp.view.contentprint

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import jp.co.riso.smartdeviceapp.MockTestUtil
import jp.co.riso.smartdeviceapp.model.ContentPrintFile
import jp.co.riso.smartprint.R
import org.junit.AfterClass
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

class ContentPrintFileAdapterTest {
    class TestContentPrintFileAdapterInterface() :
        ContentPrintFileAdapter.ContentPrintFileAdapterInterface {
        var selectedFile: ContentPrintFile? = null

        override fun onFileSelect(contentPrintFile: ContentPrintFile?): Int {
            selectedFile = contentPrintFile
            return _success
        }
    }

    class TestViewHolder {
        var textView: TextView? = null
        var imageView: ImageView? = null
        var position = 0
    }

    @Test
    fun testGetView_WithView() {
        val layoutId = 0
        val values = listOf(ContentPrintFile(0, TEST_FILE_NAME, null))
        val adapter = ContentPrintFileAdapter(_context, layoutId, values)

        val position = 0
        val view = mockView()
        val parent = mockk<ViewGroup>()
        val convertView = adapter.getView(position, view, parent)
        Assert.assertNotNull(convertView)
    }

    @Test
    fun testGetView_NullView() {
        val layoutId = 0
        val values = listOf(
            ContentPrintFile(0, TEST_FILE_NAME, null),
            ContentPrintFile(0, TEST_FILE_NAME, null)
        )
        val adapter = ContentPrintFileAdapter(_context, layoutId, values)

        val position = 0
        val view = null
        val parent = mockk<ViewGroup>()
        val convertView = adapter.getView(position, view, parent)
        Assert.assertNotNull(convertView)
    }

    @Test
    fun testInitializeView_NullViewHolder() {
        val layoutId = 0
        val values = listOf(ContentPrintFile(0, TEST_FILE_NAME, null))
        val adapter = ContentPrintFileAdapter(_context, layoutId, values)

        val viewHolder = null
        val convertView = mockView()
        val contentPrintFile = ContentPrintFile(0, TEST_FILE_NAME, null)
        val position = 0
        adapter.initializeView(viewHolder, convertView, contentPrintFile, position)
    }

    @Test
    fun testInitializeView_NullView() {
        val layoutId = 0
        val values = listOf(ContentPrintFile(0, TEST_FILE_NAME, null))
        val adapter = ContentPrintFileAdapter(_context, layoutId, values)

        val testViewHolder = TestViewHolder()
        val viewHolder = mockViewHolder(testViewHolder)
        val convertView = null
        val contentPrintFile = ContentPrintFile(0, TEST_FILE_NAME, null)
        val position = 0
        adapter.initializeView(viewHolder, convertView, contentPrintFile, position)
    }

    @Test
    fun testInitializeView_NullContentPrintFile() {
        val layoutId = 0
        val values = listOf(ContentPrintFile(0, TEST_FILE_NAME, null))
        val adapter = ContentPrintFileAdapter(_context, layoutId, values)

        val testViewHolder = TestViewHolder()
        val viewHolder = mockViewHolder(testViewHolder)
        val convertView = mockView()
        val contentPrintFile: ContentPrintFile? = null
        val position = 0
        adapter.initializeView(viewHolder, convertView, contentPrintFile, position)
    }

    @Test
    fun testInitializeView_WithThumbnailImagePath() {
        val layoutId = 0
        val values = listOf(ContentPrintFile(0, TEST_FILE_NAME, null))
        val adapter = ContentPrintFileAdapter(_context, layoutId, values)

        val testViewHolder = TestViewHolder()
        val viewHolder = mockViewHolder(testViewHolder)
        val convertView = mockView()
        val contentPrintFile = ContentPrintFile(0, TEST_FILE_NAME, null)
        contentPrintFile.thumbnailImagePath = TEST_FILE_NAME
        val position = 0
        adapter.initializeView(viewHolder, convertView, contentPrintFile, position)
    }

    @Test
    fun testInitializeView_WithoutThumbnailImage() {
        val layoutId = 0
        val values = listOf(ContentPrintFile(0, TEST_FILE_NAME, null))
        val adapter = ContentPrintFileAdapter(_context, layoutId, values)

        val testViewHolder = TestViewHolder()
        val viewHolder = mockViewHolder(testViewHolder)
        val convertView = mockk<View>()
        val mockTextView = mockk<TextView>()
        every { convertView.findViewById<TextView>(R.id.filenameText) } returns mockTextView
        every { convertView.findViewById<ImageView>(R.id.thumbnailImage) } returns null
        val mockView = mockk<View>()
        every { mockView.visibility = any() } just Runs
        every { convertView.findViewById<View>(R.id.contentprint_separator) } returns mockView
        every { convertView.setOnClickListener(any()) } just Runs
        every { convertView.tag = any() } just Runs
        val contentPrintFile = ContentPrintFile(0, TEST_FILE_NAME, null)
        contentPrintFile.thumbnailImagePath = TEST_FILE_NAME
        val position = 0
        adapter.initializeView(viewHolder, convertView, contentPrintFile, position)
    }

    @Test
    fun testOnClick_Outside() {
        val layoutId = 0
        val values = listOf(ContentPrintFile(0, TEST_FILE_NAME, null))
        val adapter = ContentPrintFileAdapter(_context, layoutId, values)
        val adapterInterface = TestContentPrintFileAdapterInterface()
        adapter.setAdapterInterface(adapterInterface)

        val mockView = mockk<View>()
        every { mockView.id } returns 0
        adapter.onClick(mockView)
        Assert.assertNull(adapterInterface.selectedFile)
    }

    @Test
    fun testOnClick_Selected() {
        val layoutId = 0
        val values = listOf(ContentPrintFile(0, TEST_FILE_NAME, null))
        val adapter = ContentPrintFileAdapter(_context, layoutId, values)
        val adapterInterface = TestContentPrintFileAdapterInterface()
        adapter.setAdapterInterface(adapterInterface)

        _success = 0
        var isActivated = false
        val mockView = mockk<View>()
        every { mockView.id } returns R.id.content_print_row
        val testViewHolder = TestViewHolder()
        val viewHolder = mockViewHolder(testViewHolder)
        every { mockView.tag } returns viewHolder
        every { mockView.isActivated = any() } answers {
            isActivated = firstArg()
        }
        adapter.onClick(mockView)
        Assert.assertTrue(isActivated)
        Assert.assertNotNull(adapterInterface.selectedFile)
    }

    @Test
    fun testOnClick_NotSelected() {
        val layoutId = 0
        val values = listOf(ContentPrintFile(0, TEST_FILE_NAME, null))
        val adapter = ContentPrintFileAdapter(_context, layoutId, values)
        val adapterInterface = TestContentPrintFileAdapterInterface()
        adapter.setAdapterInterface(adapterInterface)

        _success = -1
        var isActivated = false
        val mockView = mockk<View>()
        every { mockView.id } returns R.id.content_print_row
        val testViewHolder = TestViewHolder()
        val viewHolder = mockViewHolder(testViewHolder)
        every { mockView.tag } returns viewHolder
        every { mockView.isActivated = any() } answers {
            isActivated = firstArg()
        }
        adapter.onClick(mockView)
        Assert.assertFalse(isActivated)
        Assert.assertNotNull(adapterInterface.selectedFile)
    }

    companion object {
        private const val TEST_FILE_NAME = "test.pdf"

        private var _context: Context? = null
        // The return value of the ContentPrintFileAdapterInterface
        private var _success = 0

        private fun mockViewHolder(testViewHolder: TestViewHolder): ContentPrintFileAdapter.ViewHolder {
            // Cannot initialize an inner class from the unit tests
            val mockViewHolder = mockk<ContentPrintFileAdapter.ViewHolder>()
            every { mockViewHolder.contentPrintFilename = any() } answers {
                testViewHolder.textView = firstArg()
            }
            every { mockViewHolder.contentPrintFilename } returns testViewHolder.textView
            every { mockViewHolder.thumbnailImage = any() } answers {
                testViewHolder.imageView = firstArg()
            }
            every { mockViewHolder.thumbnailImage } returns testViewHolder.imageView
            every { mockViewHolder.position = any() } answers {
                testViewHolder.position = firstArg()
            }
            every { mockViewHolder.position } returns testViewHolder.position
            return mockViewHolder
        }

        private fun mockView(): View {
            val mockView = mockk<View>()
            val testViewHolder = TestViewHolder()
            val mockViewHolder = mockViewHolder(testViewHolder)
            every { mockView.tag } returns mockViewHolder
            val mockTextView = mockk<TextView>()
            every { mockTextView.text = any() } just Runs
            every { mockView.findViewById<TextView>(R.id.filenameText) } returns mockTextView
            val mockImageView = mockk<ImageView>()
            every { mockImageView.setImageBitmap(any()) } just Runs
            every { mockView.findViewById<ImageView>(R.id.thumbnailImage) } returns mockImageView
            val mockView2 = mockk<View>()
            every { mockView2.visibility = any() } just Runs
            every { mockView.findViewById<View>(R.id.contentprint_separator) } returns mockView2
            every { mockView.setOnClickListener(any()) } just Runs
            every { mockView.tag = any() } just Runs
            return mockView
        }

        private fun mockLayoutInflater(): LayoutInflater {
            val mockInflater = mockk<LayoutInflater>()
            val mockView = mockView()
            every { mockInflater.inflate(any<Int>(), any(), any() )} returns mockView
            return mockInflater
        }

        @JvmStatic
        @BeforeClass
        fun set() {
            _context = MockTestUtil.mockContext()
            val mockInflater = mockLayoutInflater()
            every { _context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) } returns mockInflater

            val mockBitmap = mockk<Bitmap>()
            mockkStatic(BitmapFactory::class)
            every { BitmapFactory.decodeFile(any()) } returns mockBitmap
        }

        @JvmStatic
        @AfterClass
        fun tearDown() {
            unmockkStatic(BitmapFactory::class)
        }
    }
}