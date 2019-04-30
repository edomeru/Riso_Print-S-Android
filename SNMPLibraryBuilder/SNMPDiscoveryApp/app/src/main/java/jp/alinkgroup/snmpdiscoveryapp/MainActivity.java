package jp.alinkgroup.snmpdiscoveryapp;

import java.util.ArrayList;

import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends ListActivity {
	
	private ListView listView;
	//array of ip addresses discovered in ndk
	ArrayList<String> ipAddress = new ArrayList<String>();
	
	ArrayAdapter<String> arrayAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_main);		
		listView = getListView();
		arrayAdapter = new ArrayAdapter<String>(this,
                R.layout.list_item, ipAddress);
		
		listView.setAdapter(arrayAdapter);
		
		new SNMPTask().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	//add ip address to listview
	public void printerAdded(String ndkIpAddress)
	{
		ipAddress.add(ndkIpAddress);
		this.runOnUiThread(new Runnable(){

			@Override
			public void run() {
				arrayAdapter.notifyDataSetChanged();
			}
		});
	}

    public native void startSNMPDeviceDiscovery();

    static {
        System.loadLibrary("snmp");
    }
    
    
    class SNMPTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
        	startSNMPDeviceDiscovery();
			return null;
        }      

        protected void onPostExecute(Void result) {
        	super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
        	super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    	
    }
    
    
}
