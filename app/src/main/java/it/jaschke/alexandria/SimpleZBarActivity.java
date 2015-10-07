package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class SimpleZBarActivity extends Activity implements ZBarScannerView.ResultHandler {
    private ZBarScannerView mScannerView;
    private String TAG = SimpleZBarActivity.class.getSimpleName();
    private List<BarcodeFormat> format = new ArrayList<BarcodeFormat>();


    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        mScannerView = new ZBarScannerView(this);    // Programmatically initialize the scanner view
        format.add(BarcodeFormat.EAN13);
        mScannerView.setFormats(format);

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
        Log.d(TAG, rawResult.getContents()); // Prints scan results
        Log.d(TAG, rawResult.getBarcodeFormat().getName()); // Prints the scan format (qrcode, pdf417 etc.)
        String result = rawResult.getContents();

        Intent returnIntent = new Intent();
        returnIntent.putExtra("scanResult", result);
        setResult(Activity.RESULT_OK, returnIntent);
        //Log.d("bungbagong zbar", "intent scan=" + result);
        this.finish();
    }
}
