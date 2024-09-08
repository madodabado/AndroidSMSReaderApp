package com.example.smsreader;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final int REQUEST_CODE_DEFAULT_SMS_APP = 101;
    private RecyclerView recyclerView;
    private SmsAdapter smsAdapter;
    private List<SmsMessage> smsMessageList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        smsAdapter = new SmsAdapter(smsMessageList, this);
        recyclerView.setAdapter(smsAdapter);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED)  {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS},
                    PERMISSIONS_REQUEST_CODE);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                checkDefaultSmsApp();
            } else {
                loadSmsMessages();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void checkDefaultSmsApp() {
        String defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(this);

        if (!getPackageName().equals(defaultSmsPackage)) {
            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
            startActivityForResult(intent, REQUEST_CODE_DEFAULT_SMS_APP);
        } else {
            loadSmsMessages();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_DEFAULT_SMS_APP) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                String defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(this);
                if (getPackageName().equals(defaultSmsPackage)) {
                    Toast.makeText(this, "App is now set as default SMS app.", Toast.LENGTH_SHORT).show();
                    loadSmsMessages();
                } else {
                    Toast.makeText(this, "Failed to set default SMS app.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void loadSmsMessages() {
        ContentResolver contentResolver = getContentResolver();
        Uri uri = Telephony.Sms.Inbox.CONTENT_URI;
        String[] projection = new String[]{Telephony.Sms._ID, Telephony.Sms.BODY, Telephony.Sms.DATE};
        Cursor cursor = contentResolver.query(uri, projection, null, null, Telephony.Sms.DEFAULT_SORT_ORDER);

        if (cursor != null) {
            int idIndex = cursor.getColumnIndex(Telephony.Sms._ID);
            int bodyIndex = cursor.getColumnIndex(Telephony.Sms.BODY);
            int dateIndex = cursor.getColumnIndex(Telephony.Sms.DATE);

            if (idIndex == -1 || bodyIndex == -1 || dateIndex == -1) {
                Log.e("MainActivity", "Column indices are invalid.");
                cursor.close();
                return;
            }

            smsMessageList.clear();
            while (cursor.moveToNext()) {
                String id = cursor.getString(idIndex);
                String body = cursor.getString(bodyIndex);
                String date = cursor.getString(dateIndex);
                smsMessageList.add(new SmsMessage(id, body, date));
            }
            cursor.close();
            smsAdapter.notifyDataSetChanged();
        } else {
            Log.e("MainActivity", "Cursor is null.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    checkDefaultSmsApp();
                } else {
                    loadSmsMessages();
                }
            } else {
                Toast.makeText(this, "Permissions denied. Unable to access SMS.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Handle deleting SMS
    public void deleteSms(String id) {
        Uri uri = Uri.withAppendedPath(Telephony.Sms.CONTENT_URI, id);
        int rowsDeleted = getContentResolver().delete(uri, null, null);
        if (rowsDeleted > 0) {
            Toast.makeText(this, "SMS deleted.", Toast.LENGTH_SHORT).show();
            loadSmsMessages();  // Refresh the list
        } else {
            Toast.makeText(this, "Failed to delete SMS.", Toast.LENGTH_SHORT).show();
        }
    }
}
