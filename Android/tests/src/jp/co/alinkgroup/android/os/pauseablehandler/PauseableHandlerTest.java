package jp.co.alinkgroup.android.os.pauseablehandler;

import android.os.Message;
import android.test.AndroidTestCase;

public class PauseableHandlerTest extends AndroidTestCase implements PauseableHandlerCallback  {

    public static final int MESSAGE_FORSTORING = 0x10001;
    public static final int MESSAGE_DONOTSTORE = 0x10001;
    
    boolean mMessageProcessed = false;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        mMessageProcessed = false;
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testConstructor_NullCallback() {
        PauseableHandler handler = new PauseableHandler(null);
        assertNotNull(handler);
    }
    
    public void testMessage_Immediately() {
        
        PauseableHandler handler = new PauseableHandler(null);

        handler.sendEmptyMessageDelayed(MESSAGE_FORSTORING, 100);
        
        assertTrue(mMessageProcessed);
    }

    // ================================================================================
    // INTERFACE - PauseableHandlerCallback
    // ================================================================================
    

    @Override
    public boolean storeMessage(Message message) {
        return false;
    }

    @Override
    public void processMessage(Message message) {
        mMessageProcessed = true;
    }
}