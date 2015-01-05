package org.shaastra.qmshelper;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.shaastra.qmshelper.reused.Database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author aditya.polymath & snugghash
 * TODO Refresh display when sent, works
 */
public class DisplayFeedback extends Activity {
	String[][] data = new String[7][];
	String[][] sendData = new String[2][];
	String user, pass;
	String result, resPage, myUser;
	boolean flag;
	ProgressDialog progressBar;
	private int progressBarStatus = 0;
	int myEventId, count;
	Boolean httpRes;
	Button b;
	TextView tv;
	FeedbackDatabase db;
	private Handler progressBarHandler = new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		flag = true;
		user = getIntent().getStringExtra("user");
		pass = getIntent().getStringExtra("pass");
		Log.i("disUser", user);
		Log.i("disPass", pass);
		setContentView(R.layout.feedback_table);
		b = (Button) findViewById(R.id.b1);
		tv = (TextView) findViewById(R.id.tv1);
		final GridView grid = (GridView) findViewById(R.id.gridview);
		final ArrayList<String> items = new ArrayList<String>();
		// items.add("1 , Hello11 , Hello12, g");
		// items.add("2 , Hello21 , Hello22, h");
		db = new FeedbackDatabase(this);
		db.open();
		data = db.getData();
		//sendData = db.getDataToSend();
		db.close();

        TableLayout table = (TableLayout) findViewById(R.id.tlGridTable);
        int length=data[0].length;
		for (int i = 0; i <length; i++) {
			/*
			 * String row =data[0][i]; String userid= data[1][i]; String event
			 * =data[2][i]; String sent =data[3][i];
			 */

            TableRow row=new TableRow(this);

            row.setGravity(Gravity.CENTER);
            //double debt = debtList.get(i);
            //double fee = feeList.get(i);
            TextView[] tv=new TextView[data.length];
            for (int k=0; k<data.length; ++k)

            {
                tv[k] = new TextView(this);
                tv[k].setTextSize(15);

            }
            // tv.setTextAppearance(getApplicationContext(),android.at);

            for(int j=0;j<data.length;j++) {
                if(j==2 || j==3)
                    tv[j].setLayoutParams(new TableRow.LayoutParams(200 , TableRow.LayoutParams.WRAP_CONTENT));
                if(j!=1) {
                    if(i==0)
                        tv[j].setText(Html.fromHtml(" <b>" + data[j][i]+"</b> "));
                    else
                        tv[j].setText(" " + data[j][i] +" ");

                    row.addView(tv[j]);
                }
            }
            table.addView(row);
			//items.add(data[0][i] + "," + data[1][i] + "," + data[2][i] + ","+ data[3][i]+ ","+ data[4][i]+ ","+ data[5][i]+ ","+ data[6][i]
         //   );
		}
		//grid.setAdapter(new GridAdapter(items));

		/*b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (flag) {
					progressBar = new ProgressDialog(v.getContext());
					progressBar.setCancelable(true);
					progressBar.setMessage("Sending entries ...");
					progressBar
							.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					progressBar.setProgress(0);
					progressBar.setMax(100);
					progressBar.show();
					Thread networkThread = new Thread(new Runnable() {
						@Override
						public void run() {
							int i = 0;
							while (i < sendData[0].length) {
								if (sendData[0].length != 1)
									progressBarStatus = i * 100
											/ (sendData[0].length - 1);
								else
									progressBarStatus = 100;
								if (progressBarStatus > 100)
									progressBarStatus = 100;
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								try {
									httpRes = false;
									result = resPage = null;
									postData(sendData[1][i], sendData[0][i]);
									
									if (httpRes && resPage.equals("OK")
											&& ((result.charAt(0) == 'S') || (result.charAt(0) == 'A'))) {
										db.open();
										Log.i("eveUp", sendData[1][i]);
										Log.i("eveid", sendData[0][i]);
										db.updateEntry(sendData[1][i],
												sendData[0][i], 1);
										db.close();
									}
									//TODO check condition, workds
									if (httpRes && resPage.equals("OK")
											&& result.charAt(0) != 'S' && result.charAt(0) != 'A' ) {
										db.open();
										// db.deleteEntryAlso(sendData[1][i],
										// Integer.parseInt(sendData[0][i]));
										db.close();
									}
									else if (httpRes == false) {
										progressBar.dismiss();
										runOnUiThread(new Runnable() {
											@Override
											public void run() {
												Toast toast = Toast.makeText(
														getApplicationContext(),
														"Connection Error!",
														Toast.LENGTH_SHORT);
												toast.show();
											}
										});
										break;
									}
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								progressBarHandler.post(new Runnable() {
									public void run() {
										progressBar
												.setProgress(progressBarStatus);
									}
								});
								if (progressBarStatus >= 100) {

									// sleep 2 seconds, so that you can see the
									// 100%
									try {
										Thread.sleep(2000);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}

									// close the progress bar dialog
									progressBar.dismiss();
								}
								i++;
							}
						}
					});
					networkThread.start();
				}
			}
		});*/
	}

	@Override
	protected void onResume() {
		super.onResume();
		Database info = new Database(this);
		info.open();
		count = info.getCount();
//		tv.setText("No of entries to be sent:" + count);
		info.close();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (progressBar != null) {
			progressBar.dismiss();
			progressBar = null;
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

	// Assume it's known
	private static final int ROW_ITEMS = 7;

	private static final class GridAdapter extends BaseAdapter {
		final ArrayList<String> mItems;
		final int mCount;

		/**
		 * Default constructor
		 * 
		 * @param items
		 *            to fill data to
		 */
		private GridAdapter(final ArrayList<String> items) {
			mCount = items.size() * ROW_ITEMS;
			mItems = new ArrayList<String>(mCount);
			// for small size of items it's ok to do it here, sync way
			for (String item : items) {
				// get separate string parts, divided by ,
				final String[] parts = item.split(",");
				// remove spaces from parts
				for (String part : parts) {
					part.replace(" ", "");
					mItems.add(part);
				}
			}
		}

		@Override
		public int getCount() {
			return mCount;
		}

		@Override
		public Object getItem(final int position) {
			return mItems.get(position);
		}

		@Override
		public long getItemId(final int position) {
			return position;
		}

		@Override
		public View getView(final int position, final View convertView,
				final ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				view = LayoutInflater.from(parent.getContext()).inflate(
						android.R.layout.simple_list_item_1, parent, false);
			}
			final TextView text = (TextView) view
					.findViewById(android.R.id.text1);
			text.setTextSize(12);
			text.setGravity(Gravity.CENTER);
			text.setBackgroundColor(Color.WHITE);
			text.setText(mItems.get(position));
			return view;
		}
	}
}