package jp.co.riso.smartdeviceapp.controller.db;

import jp.co.riso.smartdeviceapp.view.MainActivity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.test.ActivityInstrumentationTestCase2;

public class DatabaseManagerTest extends
		ActivityInstrumentationTestCase2<MainActivity> {
	DatabaseManager mDBManager = null;

	public DatabaseManagerTest() {
		super(MainActivity.class);
	}

	public DatabaseManagerTest(Class<MainActivity> activityClass) {
		super(activityClass);
	}

	protected void setUp() throws Exception {
		super.setUp();

		mDBManager = new DatabaseManager(getActivity());
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testCreate() {
		SQLiteDatabase db = SQLiteDatabase.create(null);
		mDBManager.onCreate(db);

		Cursor cursor = db.rawQuery(
				"SELECT name FROM sqlite_master WHERE type = ?",
				new String[] { "table" });
		assertEquals(cursor.getCount(), 6); // 4 tables + 2 default
		cursor.close();
		db.close();
	}

	public void testInsert() {
		SQLiteDatabase db = mDBManager.getWritableDatabase();
		int initialCount = 0;
		Cursor cursor = null;
		boolean result = false;

		cursor = db.query("Printer", null, null, null, null, null, null);
		initialCount = cursor.getCount();

		ContentValues values = new ContentValues();
		values.put("prn_name", "Printer name");

		result = mDBManager.insert("Printer", null, values);
		assertTrue(result);

		db = mDBManager.getReadableDatabase();
		cursor = db.query("Printer", null, null, null, null, null, null);
		assertNotNull(cursor);
		assertEquals(initialCount + 1, cursor.getCount());
		cursor.close();
		db.close();
	}

	public void testQuery() {
		SQLiteDatabase db = mDBManager.getReadableDatabase();
		Cursor c1 = null;
		Cursor c2 = null;

		assertNotNull(db);
		try {
			c1 = db.query("Printer", null, null, null, null, null, null);
			c2 = mDBManager
					.query("Printer", null, null, null, null, null, null);
			assertTrue(c1.getCount() == c2.getCount());

			c1 = db.query("PrintJob", null, null, null, null, null, null);
			c2 = mDBManager.query("PrintJob", null, null, null, null, null,
					null);
			assertTrue(c1.getCount() == c2.getCount());

			c1 = db.query("PrintSetting", null, null, null, null, null, null);
			c2 = mDBManager.query("PrintSetting", null, null, null, null, null,
					null);
			assertTrue(c1.getCount() == c2.getCount());

			c1 = db.query("DefaultPrinter", null, null, null, null, null, null);
			c2 = mDBManager.query("DefaultPrinter", null, null, null, null,
					null, null);
			assertTrue(c1.getCount() == c2.getCount());

		} catch (SQLiteException e) {
			fail("table not exist!");
		}

		c1.close();
		c2.close();
		db.close();
	}

	public void testDelete() {
		SQLiteDatabase db = mDBManager.getWritableDatabase();
		int initialCount = 0;
		Cursor cursor = null;
		boolean result = false;
		
		cursor = db.query("PrintJob", null, null, null, null, null, null);
		initialCount = cursor.getCount();
		assertTrue(initialCount>0);
		
		result = mDBManager.delete("PrintJob", null, null);
		assertTrue(result);
		
		db = mDBManager.getReadableDatabase();
		cursor = db.query("PrintJob", null, null, null, null, null, null);
		assertNotNull(cursor);
		assertEquals(0, cursor.getCount());
		cursor.close();
		db.close();
	}
}
