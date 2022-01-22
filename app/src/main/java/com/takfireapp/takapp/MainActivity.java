package com.takfireapp.takapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {


    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private Bardb helper;
    private SQLiteDatabase db;
    Context mContext = null;	// mContextをnullで初期化.
    InputMethodManager inputMethodManager;
    private TableLayout mTableLayout;
    ProgressDialog mProgressBar;

    StockData[] stockData = new StockData[99];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        // mContextに自身をセット.
        mContext = this;	// mContextにthisを格納.

//        setContentView(R.layout.activity_main);
        mProgressBar = new ProgressDialog(this);
        mTableLayout = (TableLayout) findViewById(R.id.tablePlayers);
        mTableLayout.setStretchAllColumns(true);

        selectData();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        //データ部のスクロール表示
//        ((TextView) findViewById(R.id.alldata)).setMovementMethod(new ScrollingMovementMethod());

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
        editCount.setText("0");
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
//        inputMethodManager.hideSoftInputFromWindow(findViewById(R.id.alldata).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        //背景にフォーカスを移す
        findViewById(R.id.coordinatorLayout).requestFocus();

        return false;
    }

//読み取ったバーコードの商品名を取得
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
                barcode.setText(String.valueOf(result.getContents()));

                //バーコードに該当する個数を表示する
                TextView count = findViewById(R.id.count);
                String stock = selectDataBarcode(db,result.getContents());
                if("".equals(stock)) {
                    count.setText("0");
                }else{
                    count.setText(stock);
                }

                //1個数を表示する
                EditText editCount = findViewById(R.id.editCount);
                editCount.setText("1");

                //Yahooに接続
                Yahoo yr =  new Yahoo();
                AsyncHttpRequest task = new AsyncHttpRequest(this);
                task.execute(yr.getYahooUrl(result.getContents()));

            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    //読み取った項目のクリア
    public void clear(){
        TextView barcode = findViewById(R.id.barcode);
        barcode.setText("読み取ったバーコードの値が表示されます。");
        TextView count = findViewById(R.id.count);
        count.setText("0");
        EditText editCount = findViewById(R.id.editCount);
        editCount.setText("0");
        EditText proname = findViewById(R.id.proname);
        proname.setText("読み取ったバーコードの商品名が表示されます。");
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
            delete();
            clear();
            selectData();
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

    //count upボタン
    public void countup(View view) {
        //+1
        EditText editCount = findViewById(R.id.editCount);
        int a = Integer.parseInt(String.valueOf(editCount.getText()));
        a = a+1;
        editCount.setText(String.valueOf(a));
    }
    //count downボタン
    public void countdown(View view) {
        //-1
        EditText editCount = findViewById(R.id.editCount);
        int a = Integer.parseInt(String.valueOf(editCount.getText()));
        a = a-1;
        editCount.setText(String.valueOf(a));
    }

    //データ登録ボタン
    public void insert(View view) {
        if (helper == null) {
            helper = new Bardb(getApplicationContext());
        }
        if (db == null) {
            db = helper.getReadableDatabase();
        }

        //バーコード
        String barcode = ((TextView) findViewById(R.id.barcode)).getText().toString();
        //在庫数
        String count = ((TextView) findViewById(R.id.count)).getText().toString();
        //変更数
        CharSequence ceditCount = ((TextView) findViewById(R.id.editCount)).getText().toString();
        //商品名
        String product = ((TextView) findViewById(R.id.proname)).getText().toString();

        if(barcode == null || count == null){
            return;
        }

        //在庫数
        int icount = 0;
        if (!"".equals(count)) {
            icount = Integer.parseInt(count);
        }

        //変更数
        int ieditcount = 0;
        if (!"".equals(ceditCount)) {
            ieditcount = Integer.parseInt(String.valueOf(ceditCount));
        }

        int isum = 0;
        isum = icount + ieditcount;
        //在庫がマイナスの場合は０個
        if(isum < 0){
            isum = 0;
        }

        String sum = String.valueOf(isum);

        long recodeCount = DatabaseUtils.queryNumEntries(db, helper.TABLE_NAME,"barcord = ?",new String[]{barcode});

        if(recodeCount == 0) {
            insertData(db, barcode, sum, product);
        }else {
            updateData(db, barcode, sum);
        }

        selectData();

        //表示クリア
        clear();

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //データベースへ挿入するメソッド
    public void insertData(SQLiteDatabase db,
                           String barcorde,
                           String count,
                           String product) {

        if(!"0".equals(count) ) {
            ContentValues values = new ContentValues();
            values.put(helper.COLUMN_NAME_BARCODE, barcorde);
            values.put(helper.COLUMN_NAME_COUNT, count);
            values.put(helper.COLUMN_NAME_PRODUCT, product);
            db.insert(helper.TABLE_NAME, null, values);
        }
    }


    //データベースへ更新するメソッド
    public void updateData(SQLiteDatabase db,
                           String barcorde,
                           String count
                           ) {
// New value for one column
        String title = "MyNewTitle";
        ContentValues values = new ContentValues();
        values.put(helper.COLUMN_NAME_COUNT, count);

// Which row to update, based on the title
        String selection = helper.COLUMN_NAME_BARCODE + " LIKE ?";
        String[] selectionArgs = { barcorde };

        db.update(helper.TABLE_NAME,values,selection,selectionArgs);
    }

    //データベースを読み込むメソッド
    public void selectData() {
        if (helper == null) {
            helper = new Bardb(getApplicationContext());
        }
        if (db == null) {
            db = helper.getReadableDatabase();
        }
        Cursor cursor = null;
        try{
            cursor = db.query(
                    helper.TABLE_NAME,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            cursor.moveToFirst();
            stockData = new StockData[cursor.getCount()];
            for (int i = 0; i < cursor.getCount(); i++) {
                Date now = new Date();
                stockData[i] = new StockData(
                        cursor.getString(0),
                        cursor.getString(2),
                        cursor.getString(1),
                        new Date(now.getYear(),now.getMonth(),now.getDay()));

                cursor.moveToNext();
            }

            //一覧表示
            startLoadData();

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
                    helper.TABLE_NAME,
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
    public void delete(){
        db.delete(helper.TABLE_NAME, null, null);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public void startLoadData() {
//        mProgressBar.setCancelable(false);
//        mProgressBar.setMessage("読み込み中");
//        mProgressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        mProgressBar.show();
        new LoadDataTask().execute(0);
    }

    public void loadData() {
        int leftRowMargin=0;
        int topRowMargin=0;
        int rightRowMargin=0;
        int bottomRowMargin = 0;
        int miniSize = 0,textSize = 0, smallTextSize =0, mediumTextSize = 0;
        miniSize =(int) getResources().getDimension(R.dimen.font_size_veryminismall);
        textSize = (int) getResources().getDimension(R.dimen.font_size_verysmall);
        smallTextSize = (int) getResources().getDimension(R.dimen.font_size_small);
        mediumTextSize = (int) getResources().getDimension(R.dimen.font_size_medium);

        int rows = stockData.length;
        getSupportActionBar().setTitle("登録済み商品数：" + String.valueOf(rows));
        TextView textSpacer = null;
        mTableLayout.removeAllViews();
        // -1 はヘッダー行
        for(int i = -1; i < rows; i ++) {
            StockData row = null;
            if (i > -1) {
                row = stockData[i];
                if (row == null) {
                    break;
                }
            }else {
                textSpacer = new TextView(this);
                textSpacer.setText("");
            }
            // 1列目(CODE)
            final TextView tv = new TextView(this);
            tv.setLayoutParams(new
                    TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tv.setGravity(Gravity.LEFT);
            tv.setPadding(5, 15, 0, 15);
            if (i == -1) {
                tv.setText("ﾊﾞｰｺｰﾄﾞ");
                tv.setBackgroundColor(Color.parseColor("#d9d9d9"));
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
            }
            else {
                tv.setText(row.getBar());
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, miniSize);
            }
            // 2列目(商品名)
            final TextView tv2 = new TextView(this);
            tv2.setLayoutParams(new
                    TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tv2.setGravity(Gravity.LEFT);
            tv2.setPadding(5, 15, 0, 15);
            if (i == -1) {
                tv2.setText("商品名");
                tv2.setBackgroundColor(Color.parseColor("#d9d9d9"));
                tv2.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
            }
            else {
                tv2.setText(row.getStockName());
                tv2.setTextSize(TypedValue.COMPLEX_UNIT_PX, miniSize);
            }
            // 3列目(ストック数)
            final TextView tv3 = new TextView(this);
            tv3.setLayoutParams(new
                    TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tv3.setGravity(Gravity.LEFT);
            tv3.setPadding(5, 15, 0, 15);
            if (i == -1) {
                tv3.setText("在庫");
                tv3.setBackgroundColor(Color.parseColor("#d9d9d9"));
                tv3.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
            }
            else {
                tv3.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                tv3.setText(row.getStock());
            }
            // 4列目(更新日)
            final TextView tv4 = new TextView(this);
            tv4.setLayoutParams(new
                    TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tv4.setGravity(Gravity.LEFT);
            tv4.setPadding(5, 15, 0, 15);
            if (i == -1) {
                tv4.setText("リンク");
                tv4.setBackgroundColor(Color.parseColor("#d9d9d9"));
                tv4.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
            }
            else {
                String yahoolink = "https://shopping.yahoo.co.jp/search?X=2&sc_i=shp_pc_search_sort_sortitem&p=" + row.getBar();

                tv4.setText("Yahoo");
                Pattern pattern = Pattern.compile("Yahoo");

                Linkify.TransformFilter filter = new Linkify.TransformFilter() {
                    @Override
                    public String transformUrl(Matcher match, String url) {
                        return yahoolink;
                    }
                };

                Linkify.addLinks(tv4, pattern, yahoolink, null, filter);
                tv4.setTextSize(TypedValue.COMPLEX_UNIT_PX, miniSize);

            }
            // テーブルに行を追加
            final TableRow tr = new TableRow(this);
            tr.setId(i + 1);
            TableLayout.LayoutParams trParams = new
                    TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT);
            trParams.setMargins(leftRowMargin, topRowMargin, rightRowMargin, bottomRowMargin);
            tr.setPadding(0,0,0,0);
            tr.setLayoutParams(trParams);
            tr.addView(tv);
            tr.addView(tv2);
            tr.addView(tv3);
            tr.addView(tv4);
            mTableLayout.addView(tr, trParams);
            // 罫線を追加
            if (i > -1) {
                final TableRow trSep = new TableRow(this);
                TableLayout.LayoutParams trParamsSep = new
                        TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT);
                trParamsSep.setMargins(leftRowMargin, topRowMargin, rightRowMargin, bottomRowMargin);
                trSep.setLayoutParams(trParamsSep);
                TextView tvSep = new TextView(this);
                TableRow.LayoutParams tvSepLay = new
                        TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT);
                tvSepLay.span = 4;
                tvSep.setLayoutParams(tvSepLay);
                tvSep.setBackgroundColor(Color.parseColor("#d9d9d9"));
                tvSep.setHeight(1);
                trSep.addView(tvSep);
                mTableLayout.addView(trSep, trParamsSep);
            }
        }
    }

    class LoadDataTask extends AsyncTask<Integer, Integer, String> {
        @Override
        protected String doInBackground(Integer... params) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "Task Completed.";
        }
        @Override
        protected void onPostExecute(String result) {
            mProgressBar.hide();
            loadData();
        }
        @Override
        protected void onPreExecute() {
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
        }
    }

}