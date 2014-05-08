package jp.co.riso.smartdeviceapp.model.printsettings;

import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Imposition;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.ImpositionOrder;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.OutputTray;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.PaperSize;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Punch;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Staple;
import android.test.AndroidTestCase;

public class PreviewTest extends AndroidTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testPaperSizeGetTag() {
        assertEquals("paperSize", PaperSize.getTag());
    }

    public void testPaperSizeGetWidth() {
        assertEquals(297.0f, PaperSize.A3.getWidth());
        assertEquals(210.0f, PaperSize.A4.getWidth());
    }

    public void testPaperSizeGetHeight() {
        assertEquals(420.0f, PaperSize.A3.getHeight());
        assertEquals(297.0f, PaperSize.A4.getHeight());
    }

    public void testImpositionGetPerPage() {
        assertEquals(1, Imposition.OFF.getPerPage());
    }

    public void testImpositionGetRows() {
        assertEquals(1, Imposition.OFF.getRows());
    }

    public void testImpositionGetCols() {
        assertEquals(1, Imposition.OFF.getCols());
    }

    public void testImpositionIsFlipLandscape() {
        assertEquals(false, Imposition.OFF.isFlipLandscape());
    }

    public void testImpositionOrderIsLeftToRight() {
        assertEquals(true, ImpositionOrder.L_R.isLeftToRight());
    }

    public void testImpositionOrderIsTopToBottom() {
        assertEquals(true, ImpositionOrder.L_R.isTopToBottom());
    }

    public void testImpositionOrderIsHorizontalFlow() {
        assertEquals(true, ImpositionOrder.L_R.isHorizontalFlow());
    }

    public void testStapleGetCount() {
        assertEquals(0, Staple.OFF.getCount());
        assertEquals(1, Staple.ONE_UL.getCount());
        assertEquals(2, Staple.TWO.getCount());
    }

    public void testPunchGetCount() {
        assertEquals(0, Punch.OFF.getCount());
        assertEquals(2, Punch.HOLES_2.getCount());
        assertEquals(4, Punch.HOLES_4.getCount());
    }

    public void testOutputTray() {
        assertEquals(0, OutputTray.AUTO.ordinal());
        assertEquals(1, OutputTray.FACEDOWN.ordinal());
        assertEquals(2, OutputTray.FACEUP.ordinal());
        assertEquals(3, OutputTray.TOP.ordinal());
        assertEquals(4, OutputTray.STACKING.ordinal());
    }
}
