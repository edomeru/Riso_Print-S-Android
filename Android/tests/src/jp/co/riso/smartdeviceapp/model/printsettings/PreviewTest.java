
package jp.co.riso.smartdeviceapp.model.printsettings;

import jp.co.riso.smartdeviceapp.model.printsettings.Preview.BookletFinish;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.BookletLayout;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.ColorMode;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Duplex;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.FinishingSide;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Imposition;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.ImpositionOrder;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Orientation;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.OutputTray;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.PaperSize;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Punch;
import jp.co.riso.smartdeviceapp.model.printsettings.Preview.Sort;
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

    public void testConstructor() {
        assertNotNull(new Preview());
    }

    public void testColorMode() {
        assertEquals(0, ColorMode.AUTO.ordinal());
        assertEquals(1, ColorMode.FULL_COLOR.ordinal());
        assertEquals(2, ColorMode.MONOCHROME.ordinal());
        assertEquals(ColorMode.AUTO, ColorMode.valueOf("AUTO"));
        assertEquals(ColorMode.FULL_COLOR, ColorMode.valueOf("FULL_COLOR"));
        assertEquals(ColorMode.MONOCHROME, ColorMode.valueOf("MONOCHROME"));
    }

    public void testOrientation() {
        assertEquals(0, Orientation.PORTRAIT.ordinal());
        assertEquals(1, Orientation.LANDSCAPE.ordinal());
        assertEquals(Orientation.PORTRAIT, Orientation.valueOf("PORTRAIT"));
        assertEquals(Orientation.LANDSCAPE, Orientation.valueOf("LANDSCAPE"));
    }

    public void testDuplex() {
        assertEquals(0, Duplex.OFF.ordinal());
        assertEquals(1, Duplex.LONG_EDGE.ordinal());
        assertEquals(2, Duplex.SHORT_EDGE.ordinal());
        assertEquals(Duplex.OFF, Duplex.valueOf("OFF"));
        assertEquals(Duplex.LONG_EDGE, Duplex.valueOf("LONG_EDGE"));
        assertEquals(Duplex.SHORT_EDGE, Duplex.valueOf("SHORT_EDGE"));
    }

    public void testPaperSizeValueOf() {
        assertEquals(PaperSize.A3, PaperSize.valueOf("A3"));
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

    public void testImpositionValueOf() {
        assertEquals(Imposition.OFF, Imposition.valueOf("OFF"));
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

    public void testImpositionOrderValueOf() {
        assertEquals(ImpositionOrder.L_R, ImpositionOrder.valueOf("L_R"));
    }
    
    public void testSort() {
        assertEquals(0, Sort.PER_PAGE.ordinal());
        assertEquals(1, Sort.PER_COPY.ordinal());
        assertEquals(Sort.PER_PAGE, Sort.valueOf("PER_PAGE"));
        assertEquals(Sort.PER_COPY, Sort.valueOf("PER_COPY"));
    }

    public void testBookletFinish() {
        assertEquals(0, BookletFinish.PAPER_FOLDING.ordinal());
        assertEquals(1, BookletFinish.FOLD_AND_STAPLE.ordinal());
        assertEquals(BookletFinish.PAPER_FOLDING, BookletFinish.valueOf("PAPER_FOLDING"));
        assertEquals(BookletFinish.FOLD_AND_STAPLE, BookletFinish.valueOf("FOLD_AND_STAPLE"));
    }

    public void testBookletLayout() {
        assertEquals(0, BookletLayout.L_R.ordinal());
        assertEquals(1, BookletLayout.R_L.ordinal());
        assertEquals(2, BookletLayout.T_B.ordinal());
        assertEquals(BookletLayout.L_R, BookletLayout.valueOf("L_R"));
        assertEquals(BookletLayout.R_L, BookletLayout.valueOf("R_L"));
        assertEquals(BookletLayout.T_B, BookletLayout.valueOf("T_B"));
    }

    public void testFinishingSide() {
        assertEquals(0, FinishingSide.LEFT.ordinal());
        assertEquals(1, FinishingSide.TOP.ordinal());
        assertEquals(2, FinishingSide.RIGHT.ordinal());
        assertEquals(FinishingSide.LEFT, FinishingSide.valueOf("LEFT"));
        assertEquals(FinishingSide.TOP, FinishingSide.valueOf("TOP"));
        assertEquals(FinishingSide.RIGHT, FinishingSide.valueOf("RIGHT"));
    }

    public void testStapleValueOf() {
        assertEquals(Staple.OFF, Staple.valueOf("OFF"));
    }
    
    public void testStapleGetCount() {
        assertEquals(0, Staple.OFF.getCount());
        assertEquals(1, Staple.ONE_UL.getCount());
        assertEquals(2, Staple.TWO.getCount());
    }

    public void testPunchValueOf() {
        assertEquals(Punch.OFF, Punch.valueOf("OFF"));
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
        assertEquals(OutputTray.AUTO, OutputTray.valueOf("AUTO"));
        assertEquals(OutputTray.FACEDOWN, OutputTray.valueOf("FACEDOWN"));
        assertEquals(OutputTray.FACEUP, OutputTray.valueOf("FACEUP"));
        assertEquals(OutputTray.TOP, OutputTray.valueOf("TOP"));
        assertEquals(OutputTray.STACKING, OutputTray.valueOf("STACKING"));
    }

}
