package org.shaastra.qmshelper;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.Result;

import java.util.ArrayList;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class BulkScannerActivity extends Activity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    Singleton app;
    String result;
    int count=1;
    ArrayList<String> bulk = new ArrayList<String>();

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        app = (Singleton) getApplicationContext();
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }
    @Override
    public void onStop(){
        super.onStop();
        String[] bulkArr = new String[bulk.size()];
        bulkArr = bulk.toArray(bulkArr);
        app.setBulk(bulkArr);
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here

            result= rawResult.getText();
            //Log.v("test", rawResult.getText()); // Prints scan results
            //Log.v("test", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
            // Toast.makeText(getApplication(), "Contents = " + rawResult.getText() +
            //         ", Format = " + rawResult.getBarcodeFormat().toString(), Toast.LENGTH_SHORT).show();
            Toast.makeText(getApplication(), "Added barcode. Press the back button to exit.", Toast.LENGTH_SHORT).show();
            bulk.add(Integer.toString(count)+". "+result);
            mScannerView.startCamera();
            count=count+1;


    }
}