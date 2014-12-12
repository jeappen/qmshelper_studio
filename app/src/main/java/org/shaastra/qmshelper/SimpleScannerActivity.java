package org.shaastra.qmshelper;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class SimpleScannerActivity extends Activity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
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
    public void handleResult(Result rawResult) {
        // Do something with the result here
        String result = rawResult.getText();

        Log.v("scanresult", rawResult.getText()); // Prints scan results
        Log.v("scanresult", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
       // Toast.makeText(getApplication(), "Contents = " + rawResult.getText() +
      //         ", Format = " + rawResult.getBarcodeFormat().toString(), Toast.LENGTH_SHORT).show();
        Toast.makeText(getApplication(), "Added barcode.", Toast.LENGTH_SHORT).show();
        mScannerView.stopCamera();
        Intent data = new Intent();
        data.putExtra("barcode",result);
        setResult(RESULT_OK,data);
        finish();
    }
}