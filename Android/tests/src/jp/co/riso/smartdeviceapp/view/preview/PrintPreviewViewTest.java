
package jp.co.riso.smartdeviceapp.view.preview;

import android.test.AndroidTestCase;

public class PrintPreviewViewTest extends AndroidTestCase {
    PrintPreviewView mPrintPreviewView;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        mPrintPreviewView = new PrintPreviewView(getContext());
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    //================================================================================
    // Tests - getFitToAspectRatioSize
    //================================================================================

    public void testGetFitToAspectRatioSize_SmallerSrc_WillFitToWidth() {
        float srcWidth = 20.0f;
        float srcHeight = 10.0f;
        
        int destWidth = 80;
        int destHeight = 80;
        
        int newDimensions[] = mPrintPreviewView.getFitToAspectRatioSize(srcWidth, srcHeight, destWidth, destHeight);
        
        assertTrue(newDimensions.length == 2);
        assertEquals(newDimensions[0] / (float)newDimensions[1], srcWidth / srcHeight, 0.0001f);
        assertEquals(80, newDimensions[0]);
        assertEquals(40, newDimensions[1]);
    }
    
    public void testGetFitToAspectRatioSize_SmallerSrc_WillFitToHeight() {
        float srcWidth = 10.0f;
        float srcHeight = 20.0f;
        
        int destWidth = 80;
        int destHeight = 80;
        
        int newDimensions[] = mPrintPreviewView.getFitToAspectRatioSize(srcWidth, srcHeight, destWidth, destHeight);

        assertTrue(newDimensions.length == 2);
        assertEquals(newDimensions[0] / (float)newDimensions[1], srcWidth / srcHeight, 0.0001f);
        assertEquals(40, newDimensions[0]);
        assertEquals(80, newDimensions[1]);
    }
    
    public void testGetFitToAspectRatioSize_BiggerSrc_WillFitToWidth() {
        float srcWidth = 400.0f;
        float srcHeight = 200.0f;
        
        int destWidth = 80;
        int destHeight = 80;
        
        int newDimensions[] = mPrintPreviewView.getFitToAspectRatioSize(srcWidth, srcHeight, destWidth, destHeight);

        assertTrue(newDimensions.length == 2);
        assertEquals(newDimensions[0] / (float)newDimensions[1], srcWidth / srcHeight, 0.0001f);
        assertEquals(80, newDimensions[0]);
        assertEquals(40, newDimensions[1]);
    }
    
    public void testGetFitToAspectRatioSize_BiggerSrc_WillFitToHeight() {
        float srcWidth = 200.0f;
        float srcHeight = 400.0f;
        
        int destWidth = 80;
        int destHeight = 80;
        
        int newDimensions[] = mPrintPreviewView.getFitToAspectRatioSize(srcWidth, srcHeight, destWidth, destHeight);
        
        assertTrue(newDimensions.length == 2);
        assertEquals(newDimensions[0] / (float)newDimensions[1], srcWidth / srcHeight, 0.0001f);
        assertEquals(40, newDimensions[0]);
        assertEquals(80, newDimensions[1]);
    }
}
