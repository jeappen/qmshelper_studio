package org.shaastra.qmshelper;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.shaastra.qmshelper.reused.Database;
import org.shaastra.qmshelper.reused.Display;
import org.shaastra.qmshelper.reused.EventDatabase;
import org.shaastra.qmshelper.reused.Register;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressLint("HandlerLeak")
public class FeedbackMain extends ListActivity {
	int pos, count = 0;
	Thread networkThread;
	Boolean httpRes = false;
	String result="", resPage, user, pass;
	EventDatabase db;
	FeedbackDatabase info;
	Context context;
	JSONParser jsonParser=new JSONParser();
	Singleton app;

    String[] extra_depts;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (Singleton) getApplicationContext();
		user = getIntent().getStringExtra("user");
		pass = getIntent().getStringExtra("pass");
        extra_depts=getResources().getStringArray(R.array.ExtraDept);
		Log.i("chooseUser", user);
		Log.i("choosePass", pass);

		String[] choices = { "Get Feedback", //"Get list from server",
				"Refresh Event List", "Show Feedback data", "Export to csv and send"//,"Send exported csv"
				,"Delete Feedback Data database"};
		setListAdapter(new ArrayAdapter<String>(FeedbackMain.this,
				android.R.layout.simple_list_item_1, choices));
		setContentView(R.layout.choose_re);
	}

	public ProgressDialog mDialog;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0)
				mDialog.dismiss();
			else if (msg.what == 1)
				mDialog = ProgressDialog.show(FeedbackMain.this, "Fetching Data",
						"Loading Please Wait", true);
			else if (msg.what == 2)
				mDialog = ProgressDialog.show(FeedbackMain.this, "Connection Error",
						"Cannot connect to server", true);
			else if (msg.what == 3)
				mDialog = ProgressDialog.show(FeedbackMain.this,
						"Refreshing Event List", "Loading Please Wait", true);
		}
	};

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		pos = position + 1;
		db = new EventDatabase(this);
		info = new FeedbackDatabase(this);
		context = getApplicationContext();
		/*networkThread = new Thread(new Runnable() {
			
			@Override
			public void run() {*/
				switch (pos+1) {
				case 2:
					Intent reg = new Intent(FeedbackMain.this, FeedbackForm.class);
					reg.putExtra("position", (long) (pos));
					reg.putExtra("user", user);
					reg.putExtra("pass", pass);
					startActivity(reg);
					break;
//					case 2:
//						Log.v("in choose","case 2");
//						// TODO fix app crash on no internet connection
//						// Intent list = new Intent(Choose.this, GetList.class);
//						// list.putExtra("user", user);
//						// list.putExtra("pass", pass);
//						Toast.makeText(context,
//								"Disabled,  wait for it!", Toast.LENGTH_SHORT)
//								.show();
//						// startActivity(list);
//						break;
				case 3:
					//handler.sendEmptyMessage(3);
                    ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext()
                            .getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetworkInfo = connectivityManager
                            .getActiveNetworkInfo();
                    final boolean a = activeNetworkInfo != null && activeNetworkInfo.isConnected();
					if(a){
                        new getEvents().execute();
                    } else {
                        Toast.makeText(
                                getApplicationContext(),
                                "No internet connection. Check your connection and "
                                        + "try again later", Toast.LENGTH_SHORT).show();
                    }
					
					break;
				case 4:
					Intent table = new Intent(FeedbackMain.this, DisplayFeedback.class);
					table.putExtra("user", user);
					table.putExtra("pass", pass);
					startActivity(table);
					break;
				case 5:
					info.open();
					try {
						info.dbToCsv();
						sendcsv();
					} catch (IOException e) {
						e.printStackTrace();
					}
					info.close();
					break;
				case 6:
					info.delete();
					
				}
			/*}
		});
		networkThread.start();*/
	}
	public void sendcsv(){
		
		File file = new File("/sdcard/data.csv");
		if(file.exists() && file.length()>24){
		String filelocation = "/mnt/sdcard/data.csv";
		Intent sharingIntent = new Intent(Intent.ACTION_SEND);
		//sharingIntent.setType("vnd.android.cursor.dir/email");
		sharingIntent.setType("text/html");
		String[] to = {"webops@shaastra.org"};
		sharingIntent.putExtra(Intent.EXTRA_EMAIL, to);
		sharingIntent.putExtra(Intent.EXTRA_STREAM,
				Uri.fromFile(file));
		sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Data from barcode scanner app");
		startActivity(Intent.createChooser(
				sharingIntent, "Send email"));
		}
		else
			Toast.makeText(getApplicationContext(), "ERR: No Data to send", Toast.LENGTH_SHORT).show();
	}
	
	class getEvents extends AsyncTask<String, String, String> {
		private ProgressDialog pDialog;
		JSONParser jsonParser = new JSONParser();
		int error;
		String errmsg,user_id;
		@Override
		protected void onPreExecute() {
			Log.d("milestone01submitreg","got here");
			super.onPreExecute();
			error=0;result="start"; errmsg="Generic Error"; user_id=null;
			pDialog = new ProgressDialog(FeedbackMain.this);
			pDialog.setMessage("Retrieving event list...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}
		
		
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			Log.d("milestone02","got here");
			final List<NameValuePair> paramse = new ArrayList<NameValuePair>();
					String url = "api/mobile/events/";
					JSONObject json = jsonParser.makeHttpRequest(url, "GET", paramse,app.getadmintoken() );
            int n,last_id=0;
            try{
					JSONArray events=json.getJSONArray("data");
					for(n= 0; n < events.length(); n++)
					{	String team="-1";
					    JSONObject event = events.getJSONObject(n);
					    Log.d("event",event.toString());
					    if(event.getString("team_size_max").equals("1"))
					    	team="0";
					    else
					    	team="1";

                        String add="";
                        if(!event.getString("name").equals("Startup Hive"))
                        add=":"+event.getString("name")+":"+event.getString("id")+":"+team;
                        last_id= Integer.valueOf(event.getString("id"));
					    result=result+add;
					    // do some stuff....
					}
                for(int k=0;k<extra_depts.length;k++){
                    String add="";
                    add=":"+extra_depts[k]+":"+String.valueOf(++last_id)+":"+"0";
                    result=result+add;
                }

					}
					catch(JSONException e){
						;
					}
					Log.d("events",result);
	
					
			return null;
		}

		@Override
		protected void onPostExecute(String file_url) {
			pDialog.dismiss();
			Log.d("milestoneend","got here");
			
			if (error==0) {
				List<String> myStrings =Arrays.asList(result.split(":"));
				//myStrings.remove(0);
				db.open();
				db.update();
				for (int i = 1; i < myStrings.size(); i++) {
					// Log.e(""+i, ""+myStrings[i]);
					if (i % 3 == 0)
						db.createEntry(myStrings.get(i-2),
								myStrings.get(i-1), myStrings.get(i));
				}
				db.close();
				//handler.sendEmptyMessage(0);
			} else {
				/*handler.sendEmptyMessage(0);
				handler.sendEmptyMessage(2);
				try {
					Thread.sleep(2500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				handler.sendEmptyMessage(0);
				*/
			}
			
			
			
			
			if(error==1){
				TextView msg = new TextView(FeedbackMain.this);
				msg.setText(Html.fromHtml(errmsg));
				msg.setTextSize(16);
				msg.setTextColor(Color.GRAY);
				msg.setMovementMethod(LinkMovementMethod.getInstance());
				AlertDialog.Builder builder1 = new AlertDialog.Builder(FeedbackMain.this);
	            builder1.setView(msg);
	            builder1.setCancelable(true);
	            builder1.setPositiveButton("OK",
	                    new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int id) {
	                    dialog.cancel();
	                }
	            });

	            AlertDialog alert11 = builder1.create();
	            alert11.show();
			}
			else{
				Toast.makeText(getApplicationContext(), "Event Details Acquired", Toast.LENGTH_SHORT).show();
				
				//Intent open = new Intent("org.shaastra.eventregistration.Menu");
				//startActivity(open);
			}
			/*Intent i=new Intent(PostView.this,PostView.class);
            i.putExtra("post_id", post_id);
           i.putExtra("WallName", wall_name);
            i.putExtra("passed", "notif");
            startActivity(i);8*/
		}
	}

	public void postData() throws InterruptedException {
		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(
				"http://erp.saarang.org/mobile/barcode/");
		try {
			Log.i("Inside postData", "reached");
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("user", user));
			nameValuePairs.add(new BasicNameValuePair("pass", pass));
			nameValuePairs.add(new BasicNameValuePair("actionType", String
					.valueOf(4)));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			InputStream is = entity.getContent();
			result = convertStreamToString(is);
			httpRes = true;
			resPage = response.getStatusLine().getReasonPhrase();
			Log.i("httpsRes true", "reached");
			Log.i("RESULT", result);
			Log.i("Page", response.getStatusLine().getReasonPhrase());
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
	}

	private String convertStreamToString(InputStream is) {

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append((line + "\n"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
}
