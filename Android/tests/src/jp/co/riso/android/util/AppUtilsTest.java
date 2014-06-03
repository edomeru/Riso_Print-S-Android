package jp.co.riso.android.util;

import java.util.Locale;

import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartprint.test.R;
import jp.co.riso.smartdeviceapp.view.MainActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Point;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

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
    
    //================================================================================
    // Tests - changeChildrenFont
    //================================================================================
    
    public void testChangeChildrenFont_ValidViewGroupValidFont() {
        LinearLayout ll = new LinearLayout(getActivity());
        LinearLayout ll2 = new LinearLayout(getActivity());
        
        ll.addView(new TextView(getActivity()));
        ll.addView(new View(getActivity()));
        ll.addView(new Spinner(getActivity()));
        ll.addView(new EditText(getActivity()));
        ll.addView(new Switch(getActivity()));
        ll.addView(ll2);

        TextView typeFace = new TextView(getActivity());
        typeFace.setTypeface(null);
        ll.addView(typeFace);
        typeFace = new TextView(getActivity());
        typeFace.setTypeface(SmartDeviceApp.getAppFont());
        ll.addView(typeFace);
        
        ll2.addView(new TextView(getActivity()));
        ll2.addView(new View(getActivity()));
        ll2.addView(new Spinner(getActivity()));
        ll2.addView(new EditText(getActivity()));
        ll2.addView(new Switch(getActivity()));
        
        AppUtils.changeChildrenFont(ll, SmartDeviceApp.getAppFont());
    }
    
    public void testChangeChildrenFont_NullViewGroupValidFont() {
        AppUtils.changeChildrenFont(null, SmartDeviceApp.getAppFont());
    }
    
    public void testChangeChildrenFont_ValidViewGroupNullFont() {
        LinearLayout ll = new LinearLayout(getActivity());
        
        ll.addView(new TextView(getActivity()));
        ll.addView(new View(getActivity()));
        ll.addView(new Spinner(getActivity()));
        ll.addView(new EditText(getActivity()));
        ll.addView(new Switch(getActivity()));

        AppUtils.changeChildrenFont(ll, null);
    }
    
    public void testChangeChildrenFont_NullViewGroupNullFont() {
        AppUtils.changeChildrenFont(null, null);
    }
    
    public void testChangeChildrenFont_InvalidAccess() {
        LinearLayout ll = new LinearLayout(getActivity());
        
        ll.addView(new MockClass(getActivity()));
        
        AppUtils.changeChildrenFont(ll, SmartDeviceApp.getAppFont());
    }
    
    public void testChangeChildrenFont_TypeFaceNull() {
        LinearLayout ll = new LinearLayout(getActivity());

        TextView nullTypeFace = new TextView(getActivity());
        nullTypeFace.setTypeface(null);
        ll.addView(nullTypeFace);
        
        AppUtils.changeChildrenFont(ll, SmartDeviceApp.getAppFont());
    }
    
    //================================================================================
    // Tests - getResourseId
    //================================================================================
    
    public void testGetResourceId_Valid() {
        int value = AppUtils.getResourseId("app_name", R.string.class, -1);
        
        assertTrue(-1 != value);
    }

    public void testGetResourceId_Null() {
        int value = AppUtils.getResourseId(null, null, -1);
        
        assertEquals(-1, value);
    }

    public void testGetResourceId_NullVariableName() {
        int value = AppUtils.getResourseId(null, R.string.class, -1);
        
        assertEquals(-1, value);
    }

    public void testGetResourceId_NullClass() {
        int value = AppUtils.getResourseId("app_name", null, -1);
        
        assertEquals(-1, value);
    }

    public void testGetResourceId_InvalidClass() {
        int value = AppUtils.getResourseId("app_name", this.getClass(), -1);
        
        assertEquals(-1, value);
    }

    public void testGetResourceId_InvalidAccess() {
        int value = AppUtils.getResourseId("app_name", MockClass.class, -1);
        
        assertEquals(-1, value);
    }

    public void testGetResourceId_InvalidArgumentAccess() {
        int value = AppUtils.getResourseId("app_name_2", MockClass.class, -1);
        
        assertEquals(-1, value);
    }
    
    //================================================================================
    // Tests - getFitToAspectRatioSize
    //================================================================================

    public void testGetFitToAspectRatioSize_SmallerSrc_WillFitToWidth() {
        float srcWidth = 20.0f;
        float srcHeight = 10.0f;
        
        int destWidth = 80;
        int destHeight = 80;
        
        int newDimensions[] = AppUtils.getFitToAspectRatioSize(srcWidth, srcHeight, destWidth, destHeight);
        
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
        
        int newDimensions[] = AppUtils.getFitToAspectRatioSize(srcWidth, srcHeight, destWidth, destHeight);

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
        
        int newDimensions[] = AppUtils.getFitToAspectRatioSize(srcWidth, srcHeight, destWidth, destHeight);

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
        
        int newDimensions[] = AppUtils.getFitToAspectRatioSize(srcWidth, srcHeight, destWidth, destHeight);
        
        assertTrue(newDimensions.length == 2);
        assertEquals(newDimensions[0] / (float)newDimensions[1], srcWidth / srcHeight, 0.0001f);
        assertEquals(40, newDimensions[0]);
        assertEquals(80, newDimensions[1]);
    }
    
    //================================================================================
    // Test getNextIntegerMultiple
    //================================================================================
    
    public void testGetNextIntegerMultiple_Valid() {
        int val = 0;

        val = AppUtils.getNextIntegerMultiple(12, 2);
        assertEquals(12, val);
        
        val = AppUtils.getNextIntegerMultiple(10, 4);
        assertEquals(12, val);
        
        val = AppUtils.getNextIntegerMultiple(124, 3);
        assertEquals(126, val);
        
        val = AppUtils.getNextIntegerMultiple(-3, 2);
        assertEquals(0, val);
        
        val = AppUtils.getNextIntegerMultiple(-3, -6);
        assertEquals(-6, val);
    }
    
    public void testGetNextIntegerMultiple_Invalid() {
        int val = 0;
        
        val = AppUtils.getNextIntegerMultiple(-3, 0);
        assertEquals(-3, val);
    }
    
    //================================================================================
    // Test getAuthenticationString
    //================================================================================

    public void testGetAuthenticationString_Valid() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.getAppContext());
        SharedPreferences.Editor editor = prefs.edit();
        
        editor.putBoolean(AppConstants.PREF_KEY_AUTH_SECURE_PRINT, false);
        editor.putString(AppConstants.PREF_KEY_LOGIN_ID, "test");
        editor.putString(AppConstants.PREF_KEY_AUTH_PIN_CODE, "1234");
        
        editor.apply();
        
        assertNotNull(AppUtils.getAuthenticationString());
        assertTrue(AppUtils.getAuthenticationString().isEmpty());
        
        editor.putBoolean(AppConstants.PREF_KEY_AUTH_SECURE_PRINT, true); // secure print ON
        
        editor.apply();
        
        assertNotNull(AppUtils.getAuthenticationString());
        assertFalse(AppUtils.getAuthenticationString().isEmpty());
        assertTrue(AppUtils.getAuthenticationString().equals("loginId=test\npinCode=1234\n"));
    }
    
    public void testGetAuthenticationString_Invalid() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.getAppContext());
        SharedPreferences.Editor editor = prefs.edit();
        
        editor.putBoolean(AppConstants.PREF_KEY_AUTH_SECURE_PRINT, true);
        editor.putString(AppConstants.PREF_KEY_LOGIN_ID, "test");
        editor.putString(AppConstants.PREF_KEY_AUTH_PIN_CODE, "abcd"); // pincode is not numeric

        editor.apply();
        
        assertNotNull(AppUtils.getAuthenticationString());
        assertTrue(AppUtils.getAuthenticationString().isEmpty());
        
        // missing keys
        editor.remove(AppConstants.PREF_KEY_AUTH_SECURE_PRINT);
        editor.remove(AppConstants.PREF_KEY_LOGIN_ID);
        editor.remove(AppConstants.PREF_KEY_AUTH_PIN_CODE);
        
        editor.apply();
        
        assertNotNull(AppUtils.getAuthenticationString());
        assertTrue(AppUtils.getAuthenticationString().isEmpty());
    }
    
    //================================================================================
    // Test getAuthenticationString
    //================================================================================

    public void testGetOwnerName_Valid() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.getAppContext());
        SharedPreferences.Editor editor = prefs.edit();
        String expected = "testLogin";
        editor.clear();
        editor.apply();
        String ownerName = AppUtils.getOwnerName();

        assertNotNull(ownerName);
        assertFalse(ownerName.isEmpty());
        assertEquals(getActivity().getString(jp.co.riso.smartprint.R.string.ids_app_name),
                ownerName);

        editor.putString(AppConstants.PREF_KEY_LOGIN_ID, expected);

        editor.apply();
        ownerName = AppUtils.getOwnerName();

        assertNotNull(ownerName);
        assertFalse(ownerName.isEmpty());
        assertEquals(expected, ownerName);
    }
    
    //================================================================================
    // Mock Classes
    //================================================================================
    
    public final class MockClass extends View {
        @SuppressWarnings("unused") // Invoked
        private static final int app_name = 0x7f030000;
        public static final float app_name_2 = 0x7f030000;
        
        public MockClass(Context context) {
            super(context);
        }
        
        protected Typeface getTypeface() {
            return SmartDeviceApp.getAppFont();
        }
        
        protected void setTypeface(Typeface tf) {
        }
    }
}
