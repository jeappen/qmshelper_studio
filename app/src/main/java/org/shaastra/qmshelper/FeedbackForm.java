package org.shaastra.qmshelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

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
import org.shaastra.qmshelper.reused.EventDatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author aditya.polymath & snugghash 
 */
public class FeedbackForm extends Activity implements OnClickListener {
	String  user, pass,userid="anon";
	String[][] data = new String[6][];
	Spinner spinner;
    RatingBar ratingbar;

    String uid;

    AutoCompleteTextView aut;

	Button buttonEntry,buttonFlush;
	TextView formatTxt, contentTxt, tv1, tvr1, tvr2;
	EditText comments, etAdd;
	int pos;
    float rating;
	long l;
	String[] checkboxStrings,choiceStrings,questions;
	FeedbackDatabase db;
	EventDatabase eveDb;
	StringBuilder team = new StringBuilder();
	public ProgressDialog mDialog;

    int request_Code=1;

    Singleton app;
    Resources res;
    List<String> categories;

    LinearLayout checkboxLayout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        app = (Singleton) getApplicationContext();
        pos = 0;
        uid=Secure.getString(
                getApplication().getContentResolver(), Secure.ANDROID_ID);;
        res = getResources();
        checkboxStrings = res.getStringArray(R.array.FeedbackCheckbox);
        choiceStrings = res.getStringArray(R.array.FeedbackSpinner);
        questions=res.getStringArray(R.array.questions);

        l = getIntent().getLongExtra("position", 0);
        setContentView(R.layout.feedbackform);
        comments = (EditText) findViewById(R.id.comments);
        buttonEntry = (Button) findViewById(R.id.submit_feedback);
        ratingbar=(RatingBar)findViewById(R.id.ratingBar);

        ratingbar.setStepSize(1);

        buttonEntry.setOnClickListener(this);
        //buttonEntry.setText("Add Entry");
        user = getIntent().getStringExtra("user");
        pass = getIntent().getStringExtra("pass");
        Log.i("regUser", user);
        Log.i("regPass", pass);
        spinner = (Spinner) findViewById(R.id.feedback_spinner);
        aut = (AutoCompleteTextView) findViewById(R.id.aut1);
        checkboxLayout=(LinearLayout)findViewById(R.id.checkbox_main_layout);

        formatTxt = (TextView) findViewById(R.id.tvFor);
        contentTxt = (TextView) findViewById(R.id.tvCon);
        tv1 = (TextView) findViewById(R.id.textView01);
        tvr1 = (TextView) findViewById(R.id.tvr1);
        tvr2 = (TextView) findViewById(R.id.tvr2);
        buttonFlush = (Button) findViewById(R.id.clear_feedback);
        db = new FeedbackDatabase(this);
        buttonFlush.setOnClickListener(this);
        Log.d("UniqueID",uid);

        eveDb = new EventDatabase(this);
        eveDb.open();
        data = eveDb.getData();
        categories = new ArrayList<String>();
        for (int i = 0; i < data[0].length; i++) {
            categories.add(data[1][i]);
        }
        eveDb.close();

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                R.layout.dropdown, categories),choiceAdapter =new ArrayAdapter<String>(this,
                R.layout.dropdown, choiceStrings) ;
        dataAdapter
                .setDropDownViewResource(R.layout.dropdown);

        spinner.setAdapter(choiceAdapter);

        aut.setAdapter(dataAdapter);
        aut.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View arg0) {
                //setIs_Team();
                aut.showDropDown();
            }
        });
        aut.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
               chooseQuestions();

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                //setIs_Team();

            }

        });
        aut.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e("OnItemSelected", ">>" + position);

              //  setIs_Team();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.e("OnNothingSelected", ">> nothing selected");

            }
        });

        aut.setVisibility(View.VISIBLE);

        int i=0;
        for (String a : checkboxStrings) {
            CheckBox c=new CheckBox(getApplication());
            c.setText(a);
            c.setTextColor(Color.BLACK);
            c.setId(i);
            checkboxLayout.addView(c,i);
            i++;
        }
      //  spinner.setVisibility(View.GONE);

    }

    public void chooseQuestions(){

        String event=aut.getText().toString();
        int event_there=categories.indexOf(event);
        //Log.d("in the setis_team method?","yep");
        if(event_there!=-1) {
            pos=event_there;
        }


    }





    @SuppressLint("NewApi")
	@Override
	public void onClick(View v) {



		if (v.getId() == R.id.button1) {
            Intent intent = new Intent(getApplicationContext(), SimpleScannerActivity.class);
            startActivityForResult(intent, request_Code);
			/*IntentIntegrator scanIntegrator = new IntentIntegrator(this);
			// start scanning
			try {
				scanIntegrator.initiateScan();
			} catch (Exception e) {
				Log.e("<---Scan Error 1--->", ">>" + e.getMessage());
			}
			tvr1.setText("Turning Camera On...");
			tvr2.setText("");*/
		} else if (v.getId() == R.id.submit_feedback) {
            String event=aut.getText().toString();
            int event_there=categories.indexOf(event);

            if(event_there!=-1) {
                pos=event_there;
                saveData();
            }
            else {
                Toast.makeText(
                        getApplicationContext(),
                        "Please check if the event is there in the list", Toast.LENGTH_SHORT).show();
               // tvr1.setText("ERR: event not in list");
            }

			// Log.i("hjh", "hsdgchjsa");
			//scanContent = et.getText().toString();

				/*if (scanContent != null && !scanContent.isEmpty()) {
					Log.e("test", ">>" + scanContent);
					scanDone = true;
					scanFormat = "1";
					tvr1.setText("Saving...");
					contentTxt.setText("CONTENT: " + scanContent);
					formatTxt.setText("");

				} else {
					tvr1.setText("Empty Field");
					Log.e("<--Emplty Field-->", "empty edit text");	
				}*/

		} /*else if (v.getId() == R.id.sendTeam) {
			Log.i("SEND BUTTON", "PRESSED");
			scanContent = team.toString();
			Log.i("TEAM MEMBERS: ", scanContent);
			scanDone = true;
			scanFormat = "2";
			if (scanContent != null && !scanContent.isEmpty() && scanContent.length()!=0) {
				tvr1.setText("Saving...");
                String event=aut.getText().toString();
                int event_there=categories.indexOf(event);
                if(event_there!=-1) {
                    pos=event_there;
                    //sendData();
                }
                else {
                    Toast.makeText(
                            getApplicationContext(),
                            "Please check if the event is there in the list", Toast.LENGTH_SHORT).show();
                    tvr1.setText("ERR: event not in list");
                }
				Log.v("scanContent is not empty", scanContent);
			}
			else {
				tvr1.setText("Empty Field");
				tvr2.setText("");
				Log.e("<--Emplty Field-->", "empty edit text");
			}
			team.setLength(0);
			contentTxt.setText("");
			formatTxt.setText("");
		}
		// Abort everything, clear everything except spinner and edit ID on pressing
		// abort.*/


		/*else if (v.getId() == R.id.action_abort) {
			Log.i("ABORT BUTTON", "PRESSED");
			tvr1.setText("");
			tvr2.setText("");
			//et.setText(et.getText().append(team));
			team.setLength(0);
			sendTeam.setClickable(true);
			spinner.setClickable(true);
			formatTxt.setText("");
			contentTxt.setText(team);
		}*/
		else if (v.getId() == R.id.clear_feedback) {
			comments.setText("");
            ratingbar.setRating(0);

		}
	}
    public void Scan(View v){
        Intent intent = new Intent(getApplicationContext(), SimpleScannerActivity.class);
        startActivityForResult(intent, request_Code);
        Log.d("got here","yeah");
		/*IntentIntegrator scanIntegrator = new IntentIntegrator(this);
		// start scanning
		try {
			scanIntegrator.initiateScan();
		} catch (Exception e) {
			Log.e("<---Scan Error 1--->", ">>" + e.getMessage());
		}*/
    }
    public void BulkScan(View v){
        Intent intent = new Intent(getApplicationContext(), BulkScannerActivity.class);
        startActivity(intent);

    }


    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // retrieve result of scanning - instantiate ZXing object
        //IntentResult scanningResult = IntentIntegrator.parseActivityResult(
        //		requestCode, resultCode, intent);

        // Check for resultCode showing un-executed activity, fixes appcrash.
        if (requestCode == request_Code) {
            if (resultCode == RESULT_CANCELED) {
                Log.v("debug back crash", resultCode + "");
                // invalid scan data or scan canceled
                Toast toast = Toast.makeText(getApplicationContext(),
                        "No scan data received!", Toast.LENGTH_SHORT);
                toast.show();
                return;
            } else if (resultCode == RESULT_OK) {
                String bcode = intent.getStringExtra("barcode");
                return;
            }
            else {
                // invalid scan data or scan canceled
                Toast toast = Toast.makeText(getApplicationContext(),
                        "No scan data received!", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
		IntentResult scanningResult = IntentIntegrator.parseActivityResult(
				requestCode, resultCode, intent);

		// Check for resultCode showing un-executed activity, fixes appcrash.
		if (resultCode == RESULT_CANCELED) {
			Log.v("debug back crash", resultCode + "");
			// invalid scan data or scan canceled
			Toast toast = Toast.makeText(getApplicationContext(),
					"No scan data received!", Toast.LENGTH_SHORT);
			toast.show();
			return;
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

    private void saveData(){
        int choices = checkboxStrings.length;
        String checkString="";
        for(int i=0;i<choices;i++) {
           CheckBox c =(CheckBox)findViewById(i);
           if(c.isChecked())
               checkString=checkString+"/"+String.valueOf(i+1);
        }
        if(!checkString.isEmpty())
        checkString=checkString.substring(1);
        else
            checkString="0";

        rating=ratingbar.getRating();



        db.open();
        db.createEntry(userid, data[1][pos],
                data[0][pos], String.valueOf(rating),comments.getText().toString(),checkString,spinner.getSelectedItemPosition());
        Log.i("STORE", "DONE");
        Toast.makeText(
                getApplicationContext(),
                "Feedback Stored!\nThank you.", Toast.LENGTH_LONG).show();
        db.close();
    }
/*
	private void sendData() {

		Thread networkThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					//TODO Order of things, Async task.
					//TODO Check whether the next two lines screw up flags; if so set them up to run after request in Asynctask
					httpRes = false;
					resPage = "Bad";
					//postData();
					// If sending to server works
					if (l == 1 && httpRes && resPage.equals("OK")
							&& (result.charAt(0) == 'S' || result.charAt(0)== 'A')) {
						db.open();
						db.createEntry(scanContent, data[1][pos], data[0][pos],
								1);
						Log.i("SEND", "DONE");
						db.close();
					}
					// If sending to server fails
					else if (l == 1 && (!httpRes || !resPage.equals("OK"))) {
						db.open();
                        if(!Is_Team){
                            String[] bcodes=scanContent.split("/");
                            for(int i=0;i<bcodes.length;i++){
                                boolean found = db.findEntry(bcodes[i], data[0][pos]);
                                Log.i("FOUND", "" + found);
                                if (!found) {
                                    db.createEntry(bcodes[i], data[1][pos],
                                            data[0][pos], 0);
                                    Log.i("STORE", "DONE");
                                }
                            }

                        }
                        else {
                            boolean found = db.findEntry(scanContent, data[0][pos]);
                            Log.i("FOUND", "" + found);
                            if (!found) {
                                db.createEntry(scanContent, data[1][pos],
                                        data[0][pos], 0);
                                Log.i("STORE", "DONE");
                            }
                        }
						db.close();
					}
				} catch (Exception e) {
					Log.i("Test", e.getMessage());
				}

				// Show different things on failure and otherwise
				runOnUiThread(new Runnable() {
					@Override
					public void run() {

						tvr1.setText("Stored internally");
						//if (l == 1 && httpRes && resPage.equals("OK"))
						//	tvr2.setText("Sending Succesful!");
						//else if (l == 1 && (!httpRes || !resPage.equals("OK")))
						//	tvr2.setText("Sending Unsuccesful!");
					}
				});
			}
		});
		networkThread.start();
	}*/
}
