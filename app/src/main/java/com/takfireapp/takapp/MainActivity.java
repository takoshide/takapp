package com.takfireapp.takapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
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
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.takfireapp.takapp.databinding.ActivityMainBinding;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements SimpleDialogFragment.SimpleDialogListener{


    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    public Bardb helper;
    public SQLiteDatabase db;
    Context mContext = null;	// mContextをnullで初期化
    InputMethodManager inputMethodManagerCount;
    InputMethodManager inputMethodManager;
    private TableLayout mTableLayout;
    ProgressDialog mProgressBar;

    static final int BARCODE_READ = 49374;

    StockData[] stockData = new StockData[99];

    //ファイルアクセスここから
    static final int REQUEST_OPEN_FILE = 1001;
    static final int REQUEST_CREATE_FILE = 1002;
    static final int REQUEST_DELETE_FILE = 1003;
    enum Mode {OPEN, CREATE, DELETE};
    EditText editText;
    //ファイルアクセスここまで


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        // mContextに自身をセット.
        mContext = this;	// mContextにthisを格納.

        mProgressBar = new ProgressDialog(this);
        mTableLayout = (TableLayout) findViewById(R.id.tablePlayers);
        mTableLayout.setStretchAllColumns(true);

        selectData();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){    // 許可.
//                    Toast.makeText(mContext, "CAMERA PERMISSION_GRANTED", Toast.LENGTH_LONG).show();  // "CAMERA PERMISSION_GRANTED"と表示.
                }
                else{   // 拒否.
                    Toast.makeText(mContext, "カメラの使用許可を設定してください。", Toast.LENGTH_LONG).show();  // "CAMERA Not PERMISSION_GRANTED"と表示.
                }

                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
                integrator.setPrompt("バーコードをスキャンしてください。");
                integrator.setResultDisplayDuration(0);
                integrator.setWide();
                integrator.setCameraId(0);
                integrator.initiateScan();

            }

        });

        EditText editCount           = (EditText) findViewById(R.id.editCount);
        editCount.setText("0");

        //キーボード表示を制御するためのオブジェクト
        inputMethodManagerCount =  (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        //EditTextにリスナーをセット
        editCount.setOnKeyListener(new View.OnKeyListener() {
            //コールバックとしてonKey()メソッドを定義
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //イベントを取得するタイミングには、ボタンが押されてなおかつエンターキーだったときを指定
                if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                    //キーボードを閉じる
                    inputMethodManagerCount.hideSoftInputFromWindow(editCount.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);

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
        inputMethodManager.hideSoftInputFromWindow(findViewById(R.id.players_layout).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        //背景にフォーカスを移す
        findViewById(R.id.coordinatorLayout).requestFocus();

        return false;
    }

//読み取ったバーコードの商品名を取得
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == BARCODE_READ){
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                if (result.getContents() == null) {
                    Log.d("MainActivity", "Cancelled scan");
                    Toast.makeText(mContext, "NG", Toast.LENGTH_LONG).show();
                } else {
                    Log.d("MainActivity", "Scanned");

                    selectBar(result.getContents());

                }
            } else {
                // This is important, otherwise the result will not be passed to the fragment
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
        //ファイルアクセスここから
        // File load
        else if (requestCode == REQUEST_OPEN_FILE) {
            if (resultCode == RESULT_OK && data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    delete();
                    csvReader(uri);
                    selectData();
                }
            }
        }
        // File delete
        else if (requestCode == REQUEST_DELETE_FILE) {
            if (resultCode == RESULT_OK && data != null) {
                ClipData clipData = data.getClipData();
                if(clipData==null){  // single selection
                    Uri uri = data.getData();
                    deleteUri(uri);
                }else {  // multiple selection
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        Uri uri = clipData.getItemAt(i).getUri();
                        deleteUri(uri);
                    }
                }
            }
        }
        //ファイルアクセスここまで
    }

    void deleteUri(Uri uri) {
        try {
            DocumentsContract.deleteDocument(getContentResolver(), uri);
        } catch(FileNotFoundException e){
            Toast.makeText(this, "Cannot find the file:" + e.toString(), Toast.LENGTH_LONG).show();
        }
    }
    //ファイルアクセスここまで

    public void selectBar(String result){
        Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();

        //読み取ったバーコードを表示する
        TextView barcode = findViewById(R.id.barcode);
        barcode.setText(String.valueOf(result));

        //バーコードに該当する個数を表示する
        TextView count = findViewById(R.id.count);
        String stock = selectDataBarcode(db,result);
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
        task.execute(yr.getYahooUrl(result));

    }

    //項目のクリア
    public void clear(){
        TextView barcode = findViewById(R.id.barcode);
        barcode.setText("読み取ったバーコードの値が表示されます。");
        TextView count = findViewById(R.id.count);
        count.setText("0");
        EditText editCount = findViewById(R.id.editCount);
        editCount.setText("0");
        EditText proname = findViewById(R.id.proname);
        proname.setInputType( InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_NORMAL
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        proname.setText("読み取ったバーコードの商品名が表示されます。");
        proname.setTextColor(Color.BLACK);
        proname.setTextSize(14.0f);
        EditText category = findViewById(R.id.category);
        category.setText("分類名");
        category.setTextColor(Color.BLACK);
        category.setTextSize(16.0f);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // OKボタン押下時コールバックされて実行される処理
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        delete();
        clear();
        selectData();
    }
    public void showNoticeDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new SimpleDialogFragment();
        dialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            showNoticeDialog();

            return true;


        }else if (id == R.id.action_settings_del) {
            TextView barcode = findViewById(R.id.barcode);
            deleteData(barcode.getText().toString());
            clear();
            selectData();
            return true;
        }else if (id == R.id.action_settings_backup) {

//            Intent sendIntent = new Intent(Intent.ACTION_SEND);
//            sendIntent.putExtra(Intent.EXTRA_TEXT, "Hello!");
//
//            // (Optional) Here we're setting the title of the content
//            sendIntent.putExtra(Intent.EXTRA_TITLE, "Send message");
//
//            // (Optional) Here we're passing a content URI to an image to be displayed
//            sendIntent.setData(contentUri);
//            sendIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//            // Show the Sharesheet
//            startActivity(Intent.createChooser(sendIntent, null));

            openChooserToShareThisApp();
            return true;
        }

        //ファイルアクセスここから
        switch (id) {
            case R.id.action_load:
                startFileBrowser(Mode.OPEN);
                return true;
            case R.id.action_delete:
                startFileBrowser(Mode.DELETE);
                return true;
        }
        //ファイルアクセスここまで

        return super.onOptionsItemSelected(item);
    }

    //ファイルアクセスここから
    private void startFileBrowser(Mode mode) {
        Intent intent = null;
        try {
            switch (mode) {
                case OPEN:
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.setType("text/plain");   //TEXT file only
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(Intent.createChooser(intent, "Open a file"), REQUEST_OPEN_FILE);
                    break;
                case DELETE:
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.setType("text/plain");   //TEXT file only
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    startActivityForResult(Intent.createChooser(intent, "Delete a file"), REQUEST_DELETE_FILE);
                    break;
                default:
            }
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Browser/Manager", Toast.LENGTH_LONG).show();
        }
    }
    //ファイルアクセスここまで

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
        //カテゴリ名
        String category = ((TextView) findViewById(R.id.category)).getText().toString();

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
        long recodeCount = DatabaseUtils.queryNumEntries(db, helper.TABLE_NAME,"barcode = ?",new String[]{barcode});

        if(recodeCount == 0) {
            insertData(db, barcode, sum, product, category);
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
                           String barcode,
                           String count,
                           String product,
                           String category) {

        if(!"0".equals(count) ) {
            ContentValues values = new ContentValues();
            values.put(helper.COLUMN_NAME_BARCODE, barcode);
            values.put(helper.COLUMN_NAME_COUNT, count);
            values.put(helper.COLUMN_NAME_PRODUCT, product);
            values.put(helper.COLUMN_NAME_CATEGORY, category);
            db.insert(helper.TABLE_NAME, null, values);
        }
    }

    //データベースへ更新するメソッド
    public void updateData(SQLiteDatabase db,
                           String barcode,
                           String count
                           ) {
        ContentValues values = new ContentValues();
        values.put(helper.COLUMN_NAME_COUNT, count);

        String selection = helper.COLUMN_NAME_BARCODE + " LIKE ?";
        String[] selectionArgs = { barcode };

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
                        cursor.getString(3),
                        cursor.getString(2),
                        cursor.getString(1),
                        cursor.getString(0),
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
                                     String barcode) {
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
                    "barcode = ?",
                    new String[]{ barcode },
                    null,
                    null,
                    "barcode asc" );

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

    //１件削除するメソッド
    public void deleteData(String barcode
    ) {
        String selection = helper.COLUMN_NAME_BARCODE + " LIKE ?";
        String[] selectionArgs = { barcode };

        db.delete(helper.TABLE_NAME,selection,selectionArgs);
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
        int font_size_10dp = 0, font_size_12dp = 0, font_size_14dp = 0, font_size_16dp = 0, font_size_18dp =0, font_size_20dp = 0;
        font_size_10dp =(int) getResources().getDimension(R.dimen.font_size_veryminismall);
        font_size_12dp = (int) getResources().getDimension(R.dimen.font_size_verysmall);
        font_size_14dp = (int) getResources().getDimension(R.dimen.font_size_asmall);
        font_size_16dp = (int) getResources().getDimension(R.dimen.font_size_littlesmall);
        font_size_18dp = (int) getResources().getDimension(R.dimen.font_size_small);
        font_size_20dp = (int) getResources().getDimension(R.dimen.font_size_medium);

        int rows = stockData.length;
        getSupportActionBar().setTitle(String.valueOf(rows)+ "件");
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
            //バーコードデータ
            final TextView tv0 = new TextView(this);

            // 1列目(カテゴリ)
            final TextView tv = new TextView(this);
            tv.setLayoutParams(new
                    TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tv.setGravity(Gravity.LEFT);
            tv.setPadding(5, 15, 0, 15);
            if (i == -1) {
                tv.setText("分類");
                tv.setBackgroundColor(Color.parseColor("#d9d9d9"));
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, font_size_18dp);
            }
            else {
                tv0.setText(row.getBar());
                int size2 = row.getCategory().length();
                if (size2 < 5) {
                    tv.setText(row.getCategory().substring(0, size2));
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_PX,font_size_16dp);
                } else if (size2 < 9) {
                    tv.setText(row.getCategory().substring(0, 4) + "\n" + row.getCategory().substring(4, size2));
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_PX,font_size_12dp);
                } else {
                    tv.setText(row.getCategory().substring(0, 4) + "\n" + row.getCategory().substring(4, 8) + "\n" + row.getCategory().substring(8, size2));
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, font_size_10dp);
                }
                tv.setTextColor(Color.RED);
                tv.setClickable(true);
                tv.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        selectBar(tv0.getText().toString());
                    }
                });
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
                tv2.setTextSize(TypedValue.COMPLEX_UNIT_PX, font_size_18dp);
            }
            else {
                int size = row.getStockName().length();
                if (size < 14) {
                    tv2.setText(row.getStockName().substring(0, size));
                    tv2.setTextSize(TypedValue.COMPLEX_UNIT_PX, font_size_16dp);
                } else if (size < 27) {
                    tv2.setText(row.getStockName().substring(0, 13) + "\n" + row.getStockName().substring(13, size));
                    tv2.setTextSize(TypedValue.COMPLEX_UNIT_PX, font_size_12dp);
                } else {
                    tv2.setText(row.getStockName().substring(0, 13) + "\n" + row.getStockName().substring(13, 26) + "\n" + row.getStockName().substring(26, size));
                    tv2.setTextSize(TypedValue.COMPLEX_UNIT_PX, font_size_10dp);
                }

            }
            // 3列目(在庫数)
            final TextView tv3 = new TextView(this);
            tv3.setLayoutParams(new
                    TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tv3.setGravity(Gravity.LEFT);
            tv3.setPadding(5, 15, 0, 15);
            if (i == -1) {
                tv3.setText("在庫");
                tv3.setBackgroundColor(Color.parseColor("#d9d9d9"));
                tv3.setTextSize(TypedValue.COMPLEX_UNIT_PX, font_size_18dp);
            }
            else {
                tv3.setTextSize(TypedValue.COMPLEX_UNIT_PX, font_size_16dp);
                tv3.setGravity(Gravity.CENTER);
                tv3.setText("\n" + row.getStock());
            }
            // 4列目(リンク)
            final TextView tv4 = new TextView(this);
            tv4.setLayoutParams(new
                    TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tv4.setGravity(Gravity.LEFT);
            tv4.setPadding(5, 15, 0, 15);
            if (i == -1) {
                tv4.setText("リンク");
                tv4.setBackgroundColor(Color.parseColor("#d9d9d9"));
                tv4.setTextSize(TypedValue.COMPLEX_UNIT_PX, font_size_18dp);
            }
            else {
                String yahoolink = "https://shopping.yahoo.co.jp/search?sc_i=shp_sp_search_sort_sortitem&p=" + row.getBar();
                String amazonlink = "https://www.amazon.co.jp/s?s=price-asc-rank&k=" + row.getBar();
                String rakutenlink = "https://search.rakuten.co.jp/search/mall/" + row.getBar();

                tv4.setText("yahoo"+"\n"+"amazon"+"\n"+"rakuten");
                tv4.setTextSize(TypedValue.COMPLEX_UNIT_PX, font_size_14dp);
                Pattern pattern = Pattern.compile("yahoo");
                Pattern pattern2 = Pattern.compile("amazon");
                Pattern pattern3 = Pattern.compile("rakuten");
                Linkify.TransformFilter filter = new Linkify.TransformFilter() {
                    @Override
                    public String transformUrl(Matcher match, String url) {
                        return yahoolink + "&X=2";
                    }
                };
                Linkify.TransformFilter filter2 = new Linkify.TransformFilter() {
                    @Override
                    public String transformUrl(Matcher match, String url) {
                        return amazonlink;
                    }
                };
                Linkify.TransformFilter filter3 = new Linkify.TransformFilter() {
                    @Override
                    public String transformUrl(Matcher match, String url) {
                        return rakutenlink + "?s=2";
                    }
                };
                Linkify.addLinks(tv4, pattern, yahoolink, null, filter);
                Linkify.addLinks(tv4, pattern2, amazonlink, null, filter2);
                Linkify.addLinks(tv4, pattern3, rakutenlink, null, filter3);
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
                Thread.sleep(200);
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

////////////////////////////////////////////////////////////////////////////////////////////////////
    private void openChooserToShareThisApp() {
        ShareCompat.IntentBuilder builder
                = ShareCompat.IntentBuilder.from(MainActivity.this);

        String subject = "【" + getNow() +"】日用品の在庫リスト\n";

        StringBuilder body = new StringBuilder();
        body.setLength(0);
//        body.append(subject + "\n\n");

//        body.append("バーコード,カテゴリ,商品名,在庫数\n");

        int rows = stockData.length;
        for(int i = 0; i < rows; i ++) {

            StockData row = null;
            row = stockData[i];
            body.append(
                            row.getBar() + "," +
                            row.getCategory().replaceAll("\\r\\n|\\r|\\n", "") + "," +
                            row.getStockName().replaceAll("\\r\\n|\\r|\\n", "") + "," +
                            row.getStock() + "\n"
            );
        }
        String bodyText = body.toString();
        builder.setSubject(subject) /// 件名
                .setText(bodyText)  /// 本文
                .setType("text/plain");
        Intent intent = builder.createChooserIntent();

        /// 結果を受け取らずに起動
        builder.startChooser();
    }

    // CSVファイルの読み込み
    public void csvReader(Uri uri) {
//            AssetManager assetManager = context.getResources().getAssets();
        try {
            if (uri.getScheme().equals("content")) {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferReader = new BufferedReader(inputStreamReader);
                String line;

                while ((line = bufferReader.readLine()) != null) {

                    //カンマ区切りで１つづつ配列に入れる
                    String[] RowData = line.split(",");

                    insertData(db, RowData[0], RowData[3], RowData[2], RowData[1]);

                }
                bufferReader.close();
            }
        } catch(FileNotFoundException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        }
    }


    public String getNow(){

        //日付の設定
        StringBuilder today = new StringBuilder();
        today.setLength(0);
        Calendar dd = Calendar.getInstance();
        today.append(dd.get(Calendar.YEAR));
        today.append("年");
        today.append(dd.get(Calendar.MONTH) + 1);
        today.append("月");
        today.append(dd.get(Calendar.DAY_OF_MONTH));
        today.append("日 ");
        today.append(dd.get(Calendar.HOUR));
        today.append("時");
        today.append(dd.get(Calendar.MINUTE));
        today.append("分");

        return today.toString();

    }

}