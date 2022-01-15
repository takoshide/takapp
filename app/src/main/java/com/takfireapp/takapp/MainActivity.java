package com.takfireapp.takapp;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.takfireapp.takapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {


    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    Context mContext = null;	// mContextをnullで初期化.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        // mContextに自身をセット.
        mContext = this;	// mContextにthisを格納.

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){    // 許可.
                    Toast.makeText(mContext, "CAMERA PERMISSION_GRANTED", Toast.LENGTH_LONG).show();  // "CAMERA PERMISSION_GRANTED"と表示.
                }
                else{   // 拒否.
                    Toast.makeText(mContext, "CAMERA Not PERMISSION_GRANTED", Toast.LENGTH_LONG).show();  // "CAMERA Not PERMISSION_GRANTED"と表示.
                }

                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
                integrator.setPrompt("Scan a barcode");
                integrator.setResultDisplayDuration(0);
                integrator.setWide();
                integrator.setCameraId(0);
                integrator.initiateScan();

            }

        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {



        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Log.d("MainActivity", "Cancelled scan");
                Toast.makeText(mContext, "NG" , Toast.LENGTH_LONG).show();
            } else {
                Log.d("MainActivity", "Scanned");
                Toast.makeText(mContext, result.getContents(), Toast.LENGTH_LONG).show();

//                setContentView(R.layout.activity_main);
                TextView barcode = findViewById(R.id.barcode);
                barcode.setText(result.getContents());
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
//
//    private EditText editText, editText2;
//    private Bardb helper;
//    private SQLiteDatabase db;
//    private TextView textView;
//
//    //データベース追加するボタン
//    public void insert(View view) {
//        if (helper == null) {
//            helper = new Bardb(getApplicationContext());
//        }
//        if (db == null) {
//            db = helper.getReadableDatabase();
//        }
//        editText = (EditText) findViewById(R.id.edit_text);
//        String edittext = editText.getText().toString();
//        editText2 = (EditText) findViewById(R.id.edit_text2);
//        String edittext2 = editText2.getText().toString();
//        insertData(db, edittext, edittext2);
//    }
//
//    //データベースへ挿入するメソッド
//    public void insertData(SQLiteDatabase db,
//                           String barcord,
//                           String count) {
//        ContentValues values = new ContentValues();
//        values.put(helper.COLUMN_NAME_BARCODE, barcord);
//        values.put(helper.COLUMN_NAME_COUNT, count);
//        db.insert(helper.TABLE_NAME, null, values);
//    }
//
//
//    //データベースを読み込むメソッド
//    public void read(View view) {
//        if (helper == null) {
//            helper = new Bardb(getApplicationContext());
//        }
//        if (db == null) {
//            db = helper.getReadableDatabase();
//        }
//        Cursor cursor = db.query(
//                "bardb",
//                null,
//                null,
//                null,
//                null,
//                null,
//                null
//        );
//        cursor.moveToFirst();
//        StringBuilder sbuilder = new StringBuilder();
//        for (int i = 0; i < cursor.getCount(); i++) {
//            sbuilder.append(cursor.getString(0));
//            sbuilder.append(":");
//            sbuilder.append(cursor.getString(1));
//            sbuilder.append("/\n");
//            cursor.moveToNext();
//        }
//        cursor.close();
//
//    }
}