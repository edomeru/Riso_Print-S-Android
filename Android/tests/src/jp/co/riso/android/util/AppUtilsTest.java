package jp.co.riso.android.util;

import java.util.Locale;

import jp.co.riso.smartdeviceapp.view.MainActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Point;
import android.test.ActivityInstrumentationTestCase2;
import android.util.AndroidRuntimeException;
import android.view.Display;

public class AppUtilsTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public AppUtilsTest() {
        super(MainActivity.class);
    }
    
    public AppUtilsTest(Class<MainActivity> activityClass) {
		super(activityClass);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
	}

    @Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

    //================================================================================
    // Tests - constructors
    //================================================================================

    public void testConstructor() {
        AppUtils appUtils = new AppUtils();
        assertNotNull(appUtils);
    }
    
    //================================================================================
    // Tests - createActivityIntent
    //================================================================================
    
	public void testCreateActivityIntent_ValidContextAndClass() {
		Intent testIntent;
		
		testIntent = AppUtils.createActivityIntent(getActivity(), MainActivity.class);
        assertNotNull(testIntent);
	}
	
	public void testCreateActivityIntent_NullContextOrClass() {
		Intent testIntent;
		
		// Both null
		testIntent = AppUtils.createActivityIntent(null, null);
        assertNull(testIntent);

		// Context is null
		testIntent = AppUtils.createActivityIntent(null, MainActivity.class);
        assertNull(testIntent);
        
		// Class is null
		testIntent = AppUtils.createActivityIntent(getInstrumentation().getContext(), null);
        assertNull(testIntent);
	}

    //================================================================================
    // Tests - startActivityIntent
    //================================================================================
    
    public void testStartActivityIntent_ValidContextAndActivity() {
    	try {
    		AppUtils.startActivityIntent(getActivity(), MainActivity.class);
    	} catch (NullPointerException e) {
    		fail(); // Error should not be thrown
    	} catch (ActivityNotFoundException e) {
    		fail(); // Error should not be thrown
    	}
    }
	
    public void testStartActivityIntent_IntentWillBeNull() {
    	try {
    		AppUtils.startActivityIntent(null, null);
    		fail(); // Error should be thrown
    	} catch (NullPointerException e) {
    		
    	}
    }
    
    public void testStartActivityIntent_ClassNotAnActivity() {
    	try {
    		AppUtils.startActivityIntent(getActivity(), AppUtils.class);
    		fail(); // Error should be thrown
    	} catch (ActivityNotFoundException e) {
    		
    	}
    }
    
    public void testStartActivityIntent_InvalidContext() {
    	try {
    		AppUtils.startActivityIntent(getInstrumentation().getContext(), AppUtils.class);
    		fail(); // Error should be thrown
    	} catch (AndroidRuntimeException e) {
    		
    	}
    }

    //================================================================================
    // Tests - getLocaleCode
    //================================================================================

    public void testGetLocaleCode_Default() {
    	Locale.setDefault(Locale.getDefault());
    	String str = AppUtils.getLocaleCode();
    	assertEquals(2, str.length());
    }
    
    public void testGetLocaleCode_EN() {
    	Locale.setDefault(Locale.ENGLISH);
    	String str = AppUtils.getLocaleCode();
    	assertEquals("en", str);
    	
    	Locale.setDefault(Locale.US);
    	str = AppUtils.getLocaleCode();
    	assertEquals("en", str);
    }
    
    public void testGetLocaleCode_FR() {
    	Locale.setDefault(Locale.FRENCH);
    	String str = AppUtils.getLocaleCode();
    	assertEquals("fr", str);
    	
    	Locale.setDefault(Locale.FRANCE);
    	str = AppUtils.getLocaleCode();
    	assertEquals("fr", str);
    }
    public void testGetLocaleCode_IT() {
    	Locale.setDefault(Locale.ITALIAN);
    	String str = AppUtils.getLocaleCode();
    	assertEquals("it", str);
    	
    	Locale.setDefault(Locale.ITALY);
    	str = AppUtils.getLocaleCode();
    	assertEquals("it", str);
    }
    public void testGetLocaleCode_DE() {
    	Locale.setDefault(Locale.GERMAN);
    	String str = AppUtils.getLocaleCode();
    	assertEquals("de", str);
    	
    	Locale.setDefault(Locale.GERMANY);
    	str = AppUtils.getLocaleCode();
    	assertEquals("de", str);
    }
    public void testGetLocaleCode_JA() {
    	Locale.setDefault(Locale.JAPANESE);
    	String str = AppUtils.getLocaleCode();
    	assertEquals("ja", str);
    	
    	Locale.setDefault(Locale.JAPAN);
    	str = AppUtils.getLocaleCode();
    	assertEquals("ja", str);
    }

    //================================================================================
    // Tests - getApplicationLastInstallDate
    //================================================================================
    
    public void testGetApplicationPackageName_ValidContext() {
        String packageName = AppUtils.getApplicationPackageName(getActivity());
        assertNotNull(packageName);
    }
    
    public void testGetApplicationPackageName_NullContext() {
        String packageName = AppUtils.getApplicationPackageName(null);
        assertNull(packageName);
    }

    //================================================================================
    // Tests - getApplicationLastInstallDate
    //================================================================================
    
    public void testGetApplicationLastInstallDate_ValidContextAndPackageName() {
        String packageName = AppUtils.getApplicationPackageName(getActivity());
        
        try {
            long result = AppUtils.getApplicationLastInstallDate(getActivity(), packageName);
            assertFalse(result == 0);
        } catch (NameNotFoundException e) {
            fail();
        }
    }
    
    public void testGetApplicationLastInstallDate_NullContext() {
        String packageName = AppUtils.getApplicationPackageName(getActivity());

        try {
            long result = AppUtils.getApplicationLastInstallDate(null, packageName);
            assertTrue(result == 0);
        } catch (NameNotFoundException e) {
            fail();
        }
    }
    
    public void testGetApplicationLastInstallDate_InvalidPackageName() {
        String packageName = "this is an invalid package name";

        try {
            AppUtils.getApplicationLastInstallDate(getActivity(), packageName);
            fail(); // Should throw exception
        } catch (NameNotFoundException e) {
        }
    }
    
    public void testGetApplicationLastInstallDate_NullPackageName() {
        String packageName = null;

        try {
            AppUtils.getApplicationLastInstallDate(getActivity(), packageName);
            fail(); // Should throw exception
        } catch (NameNotFoundException e) {
        }
    }

    //================================================================================
    // Tests - getScreenDimensions
    //================================================================================

    public void testGetScreenDimensions_Valid() {
        Point expected = new Point();
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        display.getSize(expected);
        
        Point size = AppUtils.getScreenDimensions(getActivity());
        
        assertEquals(expected.x, size.x);
        assertEquals(expected.y, size.y);
    }
    
    public void testGetScreenDimensions_ContextNull() {
        Point size = AppUtils.getScreenDimensions(null);
        
        assertNull(size);
    }
}
