package com.takfireapp.takapp;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.takfireapp.takapp.databinding.ActivityMainBinding;

import java.util.Map;

public class MainActivity extends AppCompatActivity {


    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private Bardb helper;
    private SQLiteDatabase db;
    Context mContext = null;	// mContextをnullで初期化.
    InputMethodManager inputMethodManager;

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

        //データ部のスクロール表示
        ((TextView) findViewById(R.id.alldata)).setMovementMethod(new ScrollingMovementMethod());

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

        /* キーボードをEnterで閉じる */

        EditText           editCount;
//        LinearLayout       mainLayout;
        //キーボードを閉じたいEditTextオブジェクト
        editCount           = (EditText) findViewById(R.id.editCount);
        //画面全体のレイアウト
//        mainLayout         = findViewById(R.id.coordinatorLayout);
        //キーボード表示を制御するためのオブジェクト
        inputMethodManager =  (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        //EditTextにリスナーをセット
        editCount.setOnKeyListener(new View.OnKeyListener() {

            //コールバックとしてonKey()メソッドを定義
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //イベントを取得するタイミングには、ボタンが押されてなおかつエンターキーだったときを指定
                if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                    //キーボードを閉じる
                    inputMethodManager.hideSoftInputFromWindow(editCount.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);

                    return true;
                }

                return false;
            }

        });

    }

    /**
     * EditText編集時に背景をタップしたらキーボードを閉じるようにするタッチイベントの処理
     */
    public boolean onTouchEvent(MotionEvent event) {
        //キーボードを隠す
        inputMethodManager.hideSoftInputFromWindow(findViewById(R.id.coordinatorLayout).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        inputMethodManager.hideSoftInputFromWindow(findViewById(R.id.alldata).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        //背景にフォーカスを移す
        findViewById(R.id.coordinatorLayout).requestFocus();

        return false;
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
                //読み取ったバーコードを表示する
                TextView barcode = findViewById(R.id.barcode);
                barcode.setText(result.getContents());

                //バーコードに該当する個数を表示する
                TextView count = findViewById(R.id.count);
                count.setText(selectDataBarcode(db,result.getContents()));

                //Yahooに接続
                Yahoo yr =  new Yahoo();
                AsyncHttpRequest task = new AsyncHttpRequest(this);
                task.execute(yr.getYahooUrl("4901777247680"));

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



    //データベース追加するボタン
    public void insert(View view) {
        if (helper == null) {
            helper = new Bardb(getApplicationContext());
        }
        if (db == null) {
            db = helper.getReadableDatabase();
        }

        String barcode = ((TextView) findViewById(R.id.barcode)).getText().toString();
        String count = ((TextView) findViewById(R.id.count)).getText().toString();
        CharSequence ceditCount = ((TextView) findViewById(R.id.editCount)).getText().toString();

        if(barcode == null || count == null){
            return;
        }

        int icount = 0;
        if (!"".equals(count)) {
            icount = Integer.parseInt(count);
        }

        int ieditcount = 0;
        if (!"".equals(ceditCount)) {
            ieditcount = Integer.parseInt(String.valueOf(ceditCount));
        }

        int isum = 0;
        isum = icount + ieditcount;

        String sum = String.valueOf(isum);
        insertData(db, barcode,sum);
    }

    //データベースへ挿入するメソッド
    public void insertData(SQLiteDatabase db,
                           String barcorde,
                           String count) {
        ContentValues values = new ContentValues();
        values.put(helper.COLUMN_NAME_BARCODE, barcorde);
        values.put(helper.COLUMN_NAME_COUNT, count);
        db.insert(helper.TABLE_NAME, null, values);
    }


    //データベースを読み込むメソッド
    public void selectData(View view) {
        if (helper == null) {
            helper = new Bardb(getApplicationContext());
        }
        if (db == null) {
            db = helper.getReadableDatabase();
        }
        Cursor cursor = null;
        try{
            cursor = db.query(
                    "bardb",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            cursor.moveToFirst();
            StringBuilder sbuilder = new StringBuilder();
            for (int i = 0; i < cursor.getCount(); i++) {
                sbuilder.append(cursor.getString(0));
                sbuilder.append(":");
                sbuilder.append(cursor.getString(1));
                sbuilder.append(":");
                sbuilder.append(cursor.getString(2));
                sbuilder.append("/\n");
                cursor.moveToNext();
            }

            TextView alldata = (TextView)findViewById(R.id.alldata);
            alldata.setText(sbuilder);
        } finally {
            if( cursor != null ){
                cursor.close();
            }
        }
    }

    //データベースを読み込むメソッド
    public String  selectDataBarcode(SQLiteDatabase db,
                                     String barcorde) {
        if (helper == null) {
            helper = new Bardb(getApplicationContext());
        }
        if (db == null) {
            db = helper.getReadableDatabase();
        }
        Cursor cursor = null;
        try{
            cursor = db.query(
                    "bardb",
                    new String[]{ "count" },
                    "barcord = ?",
                    new String[]{ barcorde },
                    null,
                    null,
                    null );

            // まず、Cursorからcountカラム
            // 取り出すためのインデクス値を確認しておく
            int indexCount = cursor.getColumnIndex( "count" );

            String result = "";
            while( cursor.moveToNext() ){
                // 検索結果をCursorから取り出す
                result = cursor.getString( indexCount );
            }
            return result;
        } finally {
            if( cursor != null ){
                cursor.close();
            }
        }
    }


    //テーブルデータ削除
    public void delete(View view){
        db.delete("bardb", null, null);
    }

//    /**
//     * JSON文字列をMapに
//     * @param json json文字列
//     * @return json文字列を読み込んだMapオブジェクト。失敗した場合はnull
//     */
//    public static Map<String, Object> jsonStringToMap(String json) {
//        Map<String, Object> map = null;
//
//        // com.fasterxml.jackson.databind.ObjectMapperを使います
//        ObjectMapper mapper = new ObjectMapper();
//
//        try {
//            // キーがString、値がObjectのマップに読み込みます。
//            map = mapper.readValue(json, new TypeReference<Map<String, Object>>(){});
//        } catch (Exception e) {
//            // エラー
//            e.printStackTrace();
//        }
//
//        return map;
//    }

}