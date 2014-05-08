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
    
    private static final String ASSET = "html/help.html";
    private static final String INVALID_VAL = "invalid";

    private static final String FOLDER = "html";
    private static final String RESOURCE = "help.html";
    private static final String RELATIVE_PATH = "html/help.html";
    private static final String RELATIVE_PATH_JA = "html-ja/help.html";
    private static final String FULL_PATH = "file:///android_asset/html/help.html";
    private static final String FULL_PATH_JA = "file:///android_asset/html-ja/help.html";
    private static final String INVALID_FOLDER_PATH = "invalid/help.html";
    private static final String INVALID_FOLDER_FULLPATH = "file:///android_asset/invalid/help.html";
    
    public AppUtilsTest() {
        super(MainActivity.class);
    }
    
    public AppUtilsTest(Class<MainActivity> activityClass) {
		super(activityClass);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

        Locale.setDefault(Locale.US);
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
    
	/*
    public void testStartActivityIntent_ValidContextAndActivity() {
    	try {
    		AppUtils.startActivityIntent(getActivity(), MainActivity.class);
    	} catch (NullPointerException e) {
    		fail(); // Error should not be thrown
    	} catch (ActivityNotFoundException e) {
    		fail(); // Error should not be thrown
    	}
    }
    */
	
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

    //================================================================================
    // Tests - getFileContentsFromAssets
    //================================================================================

    public void testGetFileContentsFromAssets_ContextNull() {
        String str = AppUtils.getFileContentsFromAssets(null, null);
        
        assertNull(str);
    }
    
    public void testGetFileContentsFromAssets_Valid() {
        String str = AppUtils.getFileContentsFromAssets(getActivity(), "db/SmartDeviceAppDB.sql");
        
        assertNotNull(str);
    }
    
    public void testGetFileContentsFromAssets_InvalidAsset() {
        String str = AppUtils.getFileContentsFromAssets(getActivity(), "db/non-existent.sql");
        
        assertNull(str);
    }

    //================================================================================
    // Tests - assetExists
    //================================================================================
    
    public void testAssetExists_Valid() {
        boolean isExists = AppUtils.assetExists(getActivity(), ASSET);
        
        assertTrue(isExists);
    }
    
    public void testAssetExists_ContextNull() {
        boolean isExists = AppUtils.assetExists(null, ASSET);
        
        assertFalse(isExists);
    }
    
    public void testAssetExists_PathNull() {
        boolean isExists = AppUtils.assetExists(getActivity(), null);
        
        assertFalse(isExists);
    }
    
    public void testAssetExists_PathEmptyString() {
        boolean isExists = AppUtils.assetExists(getActivity(), "");
        
        assertFalse(isExists);
    }
    
    public void testAssetExists_PathNotExisting() {
        boolean isExists = AppUtils.assetExists(getActivity(), INVALID_VAL);
        
        assertFalse(isExists);
    }
    
    //================================================================================
    // Tests - getLocalizedAssetRelativePath
    //================================================================================
    
    public void testGetLocalizedAssetRelativePath_Valid() {
        String localized = AppUtils.getLocalizedAssetRelativePath(getActivity(), FOLDER, RESOURCE);
        
        assertEquals(RELATIVE_PATH, localized);
    }
    
    public void testGetLocalizedAssetRelativePath_Valid_ja() {
        Locale.setDefault(Locale.JAPANESE);
        String localized = AppUtils.getLocalizedAssetRelativePath(getActivity(), FOLDER, RESOURCE);
        
        assertEquals(RELATIVE_PATH_JA, localized);
    }
    
    public void testGetLocalizedAssetRelativePath_Valid_missingLocale() {
        Locale.setDefault(Locale.KOREAN);
        String localized = AppUtils.getLocalizedAssetRelativePath(getActivity(), FOLDER, RESOURCE);
        
        assertEquals(RELATIVE_PATH, localized);
    }
    
    public void testGetLocalizedAssetRelativePath_ContexNull() {
        String localized = AppUtils.getLocalizedAssetRelativePath(null, FOLDER, RESOURCE);
        
        assertNull(localized);
    }
    
    public void testGetLocalizedAssetRelativePath_FolderNull() {
        String localized = AppUtils.getLocalizedAssetRelativePath(getActivity(), null, RESOURCE);
        
        assertNull(localized);
    }
    
    public void testGetLocalizedAssetRelativePath_FolderEmptyString() {
        String localized = AppUtils.getLocalizedAssetRelativePath(getActivity(), "", RESOURCE);
        
        assertNull(localized);
    }
    
    public void testGetLocalizedAssetRelativePath_FolderNotExisting() {
        String localized = AppUtils.getLocalizedAssetRelativePath(getActivity(), INVALID_VAL, RESOURCE);
        
        assertEquals(INVALID_FOLDER_PATH, localized);
    }
    
    public void testGetLocalizedAssetRelativePath_ResourceNull() {
        String localized = AppUtils.getLocalizedAssetRelativePath(getActivity(), FOLDER, null);
        
        assertNull(localized);
    }
    
    public void testGetLocalizedAssetRelativePath_ResourceEmptyString() {
        String localized = AppUtils.getLocalizedAssetRelativePath(getActivity(), FOLDER, "");
        
        assertNull(localized);
    }
    
    //================================================================================
    // Tests - getLocalizedAssetFullPath
    //================================================================================
    
    public void testGetLocalizedAssetFullPath_Valid() {
        String localized = AppUtils.getLocalizedAssetFullPath(getActivity(), FOLDER, RESOURCE);
        
        assertEquals(FULL_PATH, localized);
    }

    public void testGetLocalizedAssetFullPath_Valid_ja() {
        Locale.setDefault(Locale.JAPANESE);
        String localized = AppUtils.getLocalizedAssetFullPath(getActivity(), FOLDER, RESOURCE);
        
        assertEquals(FULL_PATH_JA, localized);
    }

    public void testGetLocalizedAssetFullPath_Valid_missingLocale() {
        Locale.setDefault(Locale.KOREAN);
        String localized = AppUtils.getLocalizedAssetFullPath(getActivity(), FOLDER, RESOURCE);
        
        assertEquals(FULL_PATH, localized);
    }
    
    public void testGetLocalizedAssetFullPath_ContexNull() {
        String localized = AppUtils.getLocalizedAssetFullPath(null, FOLDER, RESOURCE);
        
        assertNull(localized);
    }
    
    public void testGetLocalizedAssetFullPath_FolderNull() {
        String localized = AppUtils.getLocalizedAssetFullPath(getActivity(), null, RESOURCE);
        
        assertNull(localized);
    }
    
    public void testGetLocalizedAssetFullPath_FolderEmptyString() {
        String localized = AppUtils.getLocalizedAssetFullPath(getActivity(), "", RESOURCE);
        
        assertNull(localized);
    }
    
    public void testGetLocalizedAssetFullPath_FolderNotExisting() {
        String localized = AppUtils.getLocalizedAssetFullPath(getActivity(), INVALID_VAL, RESOURCE);
        
        assertEquals(INVALID_FOLDER_FULLPATH, localized);
    }
    
    public void testGetLocalizedAssetFullPath_ResourceNull() {
        String localized = AppUtils.getLocalizedAssetFullPath(getActivity(), FOLDER, null);
        
        assertNull(localized);
    }
    
    public void testGetLocalizedAssetFullPath_ResourceEmptyString() {
        String localized = AppUtils.getLocalizedAssetFullPath(getActivity(), FOLDER, "");
        
        assertNull(localized);
    }
}
