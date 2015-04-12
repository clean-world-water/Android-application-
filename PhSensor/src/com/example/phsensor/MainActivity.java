package com.example.phsensor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.support.v7.app.ActionBarActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View.OnClickListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;




import org.apache.http.HttpResponse;
//needed for HTTP post
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class MainActivity extends ActionBarActivity {

	private static final int REQUEST_ENABLE_BT = 1;
	//UI elements
	Button connect;
	Button post;
	TextView label;
	
	//global variable readMessage to hold the data from the sensor
	String readMessage;
	
	//bluetooth adapter, socket and device
	private BluetoothAdapter btAdapter = null;
	private BluetoothSocket btSocket = null;
	private BluetoothDevice btDevice = null;
	private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	private ConnectedThread mConnectedThread;  //thread needed for the bluetooth
	
	//----------------------------------------------------------------------------------------------------------------
	//http post request
	HttpClient httpclient = new DefaultHttpClient();
    HttpPost httppost = new HttpPost("https://pure-water-mapper.herokuapp.com/userinputs/list"); //your URL here!!!!
	//----------------------------------------------------------------------------------------------------------------
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		label = (TextView)findViewById(R.id.connectingLabel);
		post = (Button)findViewById(R.id.postButton);
		post.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				postRequest(readMessage);
				
			}
			
		});
		post.setEnabled(false);
		
		connect = (Button)findViewById(R.id.connectButton);
		connect.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				//establish connection
				try{
					btSocket = btDevice.createRfcommSocketToServiceRecord(BTMODULEUUID);
					label.setText("Connecting to Bluetooth...");
					mConnectedThread = new ConnectedThread(btSocket);
				    mConnectedThread.start();
				    
					
				}
				catch(IOException e){
					
				}
				
			}
			
		});
		
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		if(btAdapter == null)
			Toast.makeText(this, "Device does not support a bluetooth!" , Toast.LENGTH_SHORT).show();
		
		if(!btAdapter.isEnabled()){
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		
		
	}

	@Override
	public void onPause(){
		super.onPause();
		try{
			btSocket.close();
		}
		catch(IOException e){
			
		}
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	

	class ConnectedThread extends Thread{
		private final InputStream mmInStream;
       
 
        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
           
 
            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                
            } catch (IOException e) { }
 
            mmInStream = tmpIn;
            
        }
 
        public void run() {
            byte[] buffer = new byte[256];
            int bytes; 
 
            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);            //read bytes from input buffer
                    readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the serverside with post method
                    label.setText("Water PH: " + readMessage);
                    post.setEnabled(true);
                    
                } catch (IOException e) {
                    break;
                }
            }
        }//end of run
	}//end of connectedThread class
	
	
	public void postRequest(String message){
		 try {
		        // Add your data
		        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		        nameValuePairs.add(new BasicNameValuePair("water PH level", readMessage));
		        nameValuePairs.add(new BasicNameValuePair("Langtitude", "0.0"));
		        nameValuePairs.add(new BasicNameValuePair("lattitude", "0.0"));
		        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

		        // Execute HTTP Post Request
		        HttpResponse response = httpclient.execute(httppost);
		        
		    } catch (ClientProtocolException e) {
		        // TODO Auto-generated catch block
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		    }
		
	}
	}

	

