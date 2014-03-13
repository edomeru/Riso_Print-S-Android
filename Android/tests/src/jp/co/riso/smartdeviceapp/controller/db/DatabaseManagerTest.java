
package jp.co.riso.smartdeviceapp.controller.db;

import jp.co.riso.smartdeviceapp.view.MainActivity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.ActivityInstrumentationTestCase2;

public class DatabaseManagerTest extends ActivityInstrumentationTestCase2<MainActivity> {
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
        
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type = ?", new String[] {"table"});
        assertEquals(cursor.getCount(), 6); //4 tables + 2 default
        cursor.close();
        db.close();
    }
    
    public void testInsert(){
    	//mDBManager.insert(table, nullColumnHack, values)
    }
        
    public void testQuery(){
    	//mDBManager.query(table, columns, selection, selectionArgs, groupBy, having, orderBy)
    }
    
    public void testDelete(){
    	//mDBManager.delete(table, whereClause, whereArgs)
    }
}
