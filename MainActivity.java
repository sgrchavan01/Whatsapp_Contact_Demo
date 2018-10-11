package mywork.ort.chaitali.democontact;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    EditText phno;
    Button btn;
    String myContactId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phno = (EditText) findViewById(R.id.phno);
        btn = (Button) findViewById(R.id.btn);

        if(!checkAndRequestPermissions()){
            requestPerems();
        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkAndRequestPermissions()){
                    requestPerems();
                } else {
                    if (phno.getText().toString().trim().length() == 10) {
                        if (contactExists(getApplicationContext(), phno.getText().toString().trim())) {
                            if(hasWhatsApp(myContactId)){
                                openWhatsApp(phno.getText().toString().trim());
                            } else {
                                Toast.makeText(getApplicationContext(), "Contact Not On Watsapp !", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            addNewContact(phno.getText().toString().trim());
                            Toast.makeText(getApplicationContext(), "Contact Does Not Exist", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Enter Valid Mobile Number !", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public boolean hasWhatsApp(String contactID) {
        int whatsAppExists = 0;
        boolean hasWhatsApp = false;

        String[] projection = new String[]{ContactsContract.RawContacts._ID};
        String selection = ContactsContract.Data.CONTACT_ID + " = ? AND account_type IN (?)";
        String[] selectionArgs = new String[]{contactID, "com.whatsapp"};
        Cursor cursor = getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, projection, selection, selectionArgs, null);
        if (cursor != null) {
            hasWhatsApp = cursor.moveToNext();
            if (hasWhatsApp) {
                return true;
            }
            cursor.close();
        }
        return hasWhatsApp;
    }

    private void addNewContact(String number) {
        Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
        intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
        intent.putExtra(ContactsContract.Intents.Insert.PHONE, number)
                .putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK);
        startActivity(intent);
    }

    private void openWhatsApp(String number) {
        String smsNumber = "91" + number; // E164 format without '+' sign
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
        sendIntent.putExtra("jid", smsNumber + "@s.whatsapp.net"); //phone number without "+" prefix
        sendIntent.setPackage("com.whatsapp");
        if (sendIntent.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(this, "Error/n", Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(sendIntent);
    }

    private boolean contactExists(Context context, String number) {
        Uri lookupUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number));
        String[] mPhoneNumberProjection = {ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME};
        Cursor cur = context.getContentResolver().query(lookupUri, mPhoneNumberProjection, null, null, null);
        try {
            if (cur.moveToFirst()) {
                myContactId = cur.getString(cur.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
                return true;
            }
        } finally {
            if (cur != null)
                cur.close();
        }
        return false;
    }

    private boolean checkAndRequestPermissions() {
        int res;
        String[] permissions = new String[2];
        permissions[0] = Manifest.permission.READ_CONTACTS;
        permissions[1] = Manifest.permission.WRITE_CONTACTS;

        for (String perms : permissions) {
            res = MainActivity.this.checkCallingOrSelfPermission(perms);
            if (!(res == PackageManager.PERMISSION_GRANTED)) {
                return false;
            }
        }
        return true;
    }

    private void requestPerems() {
        String[] permissions = new String[2];
        permissions[0] = Manifest.permission.READ_CONTACTS;
        permissions[1] = Manifest.permission.WRITE_CONTACTS;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, 1001);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        boolean allowed = true;
        switch (requestCode) {
            case 1001:
                for (int res : grantResults) {
                    allowed = allowed && (res == PackageManager.PERMISSION_GRANTED);
                }
                break;
            default:
                allowed = false;
                break;
        }
        if (allowed) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS) ||
                        !shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS)) {
                    Toast.makeText(getApplicationContext(), "Go to settings and enable permissions", Toast
                            .LENGTH_LONG).show();
                }
            }
        }
    }
}
