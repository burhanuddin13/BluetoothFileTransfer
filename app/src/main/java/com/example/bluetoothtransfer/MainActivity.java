package com.example.bluetoothtransfer;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int FILE_REQUEST_CODE = 120;
    private static final int REQUEST_BLUETOOTH = 226;
    private static final int DISCOVER_DURATION = 300;

    TextView txtLocation;
    Button btnSelect, btnSend;

    BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();

    Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtLocation = (TextView) findViewById(R.id.txtFileLocation); //Referencing File Location TextView
        btnSelect = (Button) findViewById(R.id.btnSelectFile); //Referencing Select File Button
        btnSelect.setOnClickListener(v -> { //When Select File button is clicked
            txtLocation.setText("");
            openFile(); //Function Call
        });

        btnSend = (Button) findViewById(R.id.btnSendFile); //Referencing Send File Button
        btnSend.setOnClickListener(v -> sendViaBluetooth()); //When Send File button is clicked
    }

    //Function will be called automatically when Quit Button is clicked
    public void exit(View v)
    {
        BTAdapter.disable(); //Turns Off Bluetooth
        Toast.makeText(this, "Turning off BlueTooth\nThank You for using our product",Toast.LENGTH_SHORT).show();
        finish();
    }

    //Function which is called when using outside application functions
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case FILE_REQUEST_CODE: //When Selecting File
                if (resultCode == RESULT_OK && data != null) {
                    fileUri = data.getData(); //Location of File as URI
                    txtLocation.setText(""+fileUri); //Displaying URI Location
                    btnSend.setVisibility(View.VISIBLE); //Making Send File Button Visible
                }
                break;
            case REQUEST_BLUETOOTH: //When Requesting Bluetooth
                if (resultCode == DISCOVER_DURATION) {
                    Intent BTIntent = new Intent(); //Intent for Sending via Bluetooth
                    BTIntent.setAction(Intent.ACTION_SEND);
                    BTIntent.setType("*/*");
                    BTIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                    PackageManager pm = getPackageManager();
                    List<ResolveInfo> list = pm.queryIntentActivities(BTIntent, 0);
                    if (list.size() > 0) {
                        String packageName = null;
                        String className = null;
                        boolean found = false;

                        for (ResolveInfo info : list) {
                            packageName = info.activityInfo.packageName;
                            if (packageName.equals("com.android.bluetooth")) {
                                className = info.activityInfo.name;
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            Toast.makeText(this, "Could not find Bluetooth", Toast.LENGTH_LONG).show();
                        } else {
                            BTIntent.setClassName(packageName, className);
                            startActivity(BTIntent);
                        }
                    }
                }
                break;

            default:
                Toast.makeText(MainActivity.this, "Unknown Request Code Encountered", Toast.LENGTH_SHORT).show();
        }
    }

    //Function Called when Send File button clicked
    //Helps in Sending File
    public void sendViaBluetooth() {
        if(!txtLocation.getText().toString().equals("")){ //if location is blank
            if (BTAdapter == null) { //If Bluetooth functionality doesn't exist
                Toast.makeText(this, "Device does not support Bluetooth", Toast.LENGTH_LONG).show();
            } else {
                enableBluetooth(); //Function Call to enable Bluetooth
            }
        }else{
            Toast.makeText(this,"Please Select a File first",Toast.LENGTH_LONG).show();
        }
    }

    //Function to enable Bluetooth
    public void enableBluetooth() {
        Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE); //Intent to request Discoverable Bluetooth Devices
        discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVER_DURATION);
        startActivityForResult(discoveryIntent, REQUEST_BLUETOOTH);
    }

    //Function to Open File Explorer to Select Files
    private void openFile() {
        Intent fileIntent=new Intent(); //Intent to select File
        fileIntent.setType("*/*"); //Setting File type any type with any extension
        fileIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(fileIntent, FILE_REQUEST_CODE);
    }


}