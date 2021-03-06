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
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SimpleDialogFragment.SimpleDialogListener{


    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    public Bardb helper;
    public SQLiteDatabase db;
    Context mContext = null;	// mContext???null????????????
    InputMethodManager inputMethodManagerCount;
    InputMethodManager inputMethodManager;
    private TableLayout mTableLayout;
    ProgressDialog mProgressBar;

    private String PRODUCTNAME = "";
    private String CATEGORY = "";


    static final int BARCODE_READ = 49374;

    StockData[] stockData = new StockData[999];

    //????????????????????????????????????
    static final int REQUEST_OPEN_FILE = 1001;
    static final int REQUEST_CREATE_FILE = 1002;
    static final int REQUEST_DELETE_FILE = 1003;
    enum Mode {OPEN, CREATE, DELETE};
    EditText editText;
    //????????????????????????????????????

    JustTextSize jts = new JustTextSize();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        // mContext?????????????????????.
        mContext = this;	// mContext???this?????????.

        mProgressBar = new ProgressDialog(this);
        mTableLayout = (TableLayout) findViewById(R.id.tablePlayers);
        mTableLayout.setStretchAllColumns(true);
        clear();
        selectData();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){    // ??????.
//                    Toast.makeText(mContext, "CAMERA PERMISSION_GRANTED", Toast.LENGTH_LONG).show();  // "CAMERA PERMISSION_GRANTED"?????????.
                }
                else{   // ??????.
                    Toast.makeText(mContext, "??????????????????????????????????????????????????????", Toast.LENGTH_LONG).show();  // "CAMERA Not PERMISSION_GRANTED"?????????.
                }

                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
                integrator.setPrompt("???????????????????????????????????????????????????");
                integrator.setResultDisplayDuration(0);
                integrator.setWide();
                integrator.setCameraId(0);
                integrator.initiateScan();

            }

        });

        EditText editCount           = (EditText) findViewById(R.id.editCount);
        editCount.setText("0");

        //???????????????????????????????????????????????????????????????
        inputMethodManagerCount =  (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        //EditText???????????????????????????
        editCount.setOnKeyListener(new View.OnKeyListener() {
            //???????????????????????????onKey()?????????????????????
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                    //???????????????????????????
                    inputMethodManagerCount.hideSoftInputFromWindow(editCount.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);

                    return true;
                }

                return false;
            }

        });

    }

    /**
     * EditText???????????????????????????????????????????????????????????????????????????????????????????????????????????????
     */
    public boolean onTouchEvent(MotionEvent event) {
        //????????????????????????
        inputMethodManagerCount.hideSoftInputFromWindow(findViewById(R.id.coordinatorLayout).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        inputMethodManagerCount.hideSoftInputFromWindow(findViewById(R.id.players_layout).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        //?????????????????????????????????
        findViewById(R.id.coordinatorLayout).requestFocus();

        return false;
    }

//???????????????????????????????????????????????????
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
        //????????????????????????????????????
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
        //????????????????????????????????????
    }

    void deleteUri(Uri uri) {
        try {
            DocumentsContract.deleteDocument(getContentResolver(), uri);
        } catch(FileNotFoundException e){
            Toast.makeText(this, "Cannot find the file:" + e.toString(), Toast.LENGTH_LONG).show();
        }
    }
    //????????????????????????????????????

    public void selectBar(String result){
        Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();

        //?????????????????????????????????????????????
        TextView barcode = findViewById(R.id.barcode);
        barcode.setText(String.valueOf(result));
        jts.resizeTextView(barcode,30);
        barcode.setTextColor(Color.BLUE);

        //Yahoo????????????????????????
        Yahoo yr =  new Yahoo();
        YahooBClinkAsyncHttpRequest task = new YahooBClinkAsyncHttpRequest(this);
        task.execute(yr.getYahooBCUrl(result));








        //???????????????????????????????????????????????????
        TextView count = findViewById(R.id.count);
        String stock = selectDataBarcode(db,result);
        if("".equals(stock)) {
            count.setText("0");
            //Yahoo?????????
//            Yahoo yr =  new Yahoo();
            YahooProductNameAsyncHttpRequest ypnahrTask = new YahooProductNameAsyncHttpRequest(this);
            ypnahrTask.execute(yr.getYahooUrl(result));
        }else{
            count.setText(stock);

            EditText proname = findViewById(R.id.proname);
            proname.setText(PRODUCTNAME);
            jts.resizeTextView(proname,30);
            proname.setTextColor(Color.BLUE);
            EditText category = findViewById(R.id.category);
            category.setText(CATEGORY);
            jts.resizeTextView(category,30);
            category.setTextColor(Color.BLUE);
        }

        //1?????????????????????
        EditText editCount = findViewById(R.id.editCount);
        editCount.setText("1");

    }

    //??????????????????
    public void clear(){
        EditText barcode = findViewById(R.id.barcode);
        barcode.setText("??????????????????");
        barcode.setTextColor(Color.BLUE);
        jts.resizeTextView(barcode,30);
        barcode.setBackgroundColor(Color.parseColor("#FFDEAD"));
        TextView count = findViewById(R.id.count);
        count.setText("0");
        EditText editCount = findViewById(R.id.editCount);
        editCount.setText("0");
        EditText proname = findViewById(R.id.proname);
        proname.setInputType( InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_NORMAL
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        proname.setText("??????????????????????????????????????????????????????????????????");
        proname.setBackgroundColor(Color.parseColor("#FFDEAD"));
        proname.setTextColor(Color.BLUE);
        jts.resizeTextView(proname,30);

        EditText category = findViewById(R.id.category);
        category.setText("?????????");
        category.setTextColor(Color.BLUE);
        category.setBackgroundColor(Color.parseColor("#FFDEAD"));
        jts.resizeTextView(category,30);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // OK??????????????????????????????????????????????????????????????????
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
        }else if (id == R.id.action_clear) {
            clear();
            return true;
        }

        //????????????????????????????????????
        switch (id) {
            case R.id.action_load:
                startFileBrowser(Mode.OPEN);
                return true;
            case R.id.action_delete:
                startFileBrowser(Mode.DELETE);
                return true;
        }
        //????????????????????????????????????

        return super.onOptionsItemSelected(item);
    }

    //????????????????????????????????????
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
    //????????????????????????????????????

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    //count up?????????
    public void countup(View view) {
        //+1
        EditText editCount = findViewById(R.id.editCount);
        int a = Integer.parseInt(String.valueOf(editCount.getText()));
        a = a+1;
        editCount.setText(String.valueOf(a));
    }
    //count down?????????
    public void countdown(View view) {
        //-1
        EditText editCount = findViewById(R.id.editCount);
        int a = Integer.parseInt(String.valueOf(editCount.getText()));
        a = a-1;
        editCount.setText(String.valueOf(a));
    }

    //????????????????????????
    public void insert(View view) {
        if (helper == null) {
            helper = new Bardb(getApplicationContext());
        }
        if (db == null) {
            db = helper.getReadableDatabase();
        }

        //???????????????
        String barcode = ((TextView) findViewById(R.id.barcode)).getText().toString();
        //?????????
        String count = ((TextView) findViewById(R.id.count)).getText().toString();
        //?????????
        CharSequence ceditCount = ((TextView) findViewById(R.id.editCount)).getText().toString();
        //?????????
        String product = ((TextView) findViewById(R.id.proname)).getText().toString();
        //???????????????
        String category = ((TextView) findViewById(R.id.category)).getText().toString();

        if(barcode == null || count == null){
            return;
        }

        //?????????
        int icount = 0;
        if (!"".equals(count)) {
            icount = Integer.parseInt(count);
        }

        //?????????
        int ieditcount = 0;
        if (!"".equals(ceditCount)) {
            ieditcount = Integer.parseInt(String.valueOf(ceditCount));
        }

        int isum = 0;
        isum = icount + ieditcount;
        //???????????????????????????????????????
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
        //???????????????
        clear();

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //?????????????????????????????????????????????
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

    //?????????????????????????????????????????????????????????
    public void allInsertData(SQLiteDatabase db,
                           String barcode,
                           String count,
                           String product,
                           String category) {

            ContentValues values = new ContentValues();
            values.put(helper.COLUMN_NAME_BARCODE, barcode);
            values.put(helper.COLUMN_NAME_COUNT, count);
            values.put(helper.COLUMN_NAME_PRODUCT, product);
            values.put(helper.COLUMN_NAME_CATEGORY, category);
            db.insert(helper.TABLE_NAME, null, values);
    }

    //?????????????????????????????????????????????
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

    //?????????????????????????????????????????????
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

            //????????????
            startLoadData();

        } finally {
            if( cursor != null ){
                cursor.close();
            }
        }
    }

    //?????????????????????????????????????????????
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
                    new String[]{ "count","product","category" },
                    "barcode = ?",
                    new String[]{ barcode },
                    null,
                    null,
                    "barcode asc" );

            // ?????????Cursor??????count?????????
            // ????????????????????????????????????????????????????????????
            int indexCount = cursor.getColumnIndex( "count" );
            int product = cursor.getColumnIndex( "product" );
            int category = cursor.getColumnIndex( "category" );

            String result = "";


            while( cursor.moveToNext() ){
                // ???????????????Cursor??????????????????
                result = cursor.getString( indexCount );
                PRODUCTNAME = cursor.getString( product );
                CATEGORY = cursor.getString( category );
            }
            return result;
        } finally {
            if( cursor != null ){
                cursor.close();
            }
        }
    }

    //??????????????????????????????
    public void deleteData(String barcode
    ) {
        String selection = helper.COLUMN_NAME_BARCODE + " LIKE ?";
        String[] selectionArgs = { barcode };

        db.delete(helper.TABLE_NAME,selection,selectionArgs);
    }

    //???????????????????????????
    public void delete(){
        db.delete(helper.TABLE_NAME, null, null);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public void startLoadData() {
//        mProgressBar.setCancelable(false);
//        mProgressBar.setMessage("???????????????");
//        mProgressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        mProgressBar.show();
        new LoadDataTask().execute(0);
    }

    public void loadData() {
        int leftRowMargin=0;
        int topRowMargin=0;
        int rightRowMargin=0;
        int bottomRowMargin = 0;

        int rows = stockData.length;
        getSupportActionBar().setTitle(String.valueOf(rows)+ "???");
        TextView textSpacer = null;
        mTableLayout.removeAllViews();
        // -1 ??????????????????
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
            //????????????????????????
            final TextView tv0 = new TextView(this);
            final TextView tv = new TextView(this);
            final TextView tv2 = new TextView(this);
            final TextView tv3 = new TextView(this);

            tv.setLayoutParams(new
                    TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tv.setGravity(Gravity.LEFT);
            tv.setPadding(5, 15, 0, 15);
            if (i == -1) {
                tv.setText("??????");
                tv.setBackgroundColor(Color.parseColor("#ADD8E6"));
                tv.setTextColor(Color.parseColor("#000000"));
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimension(R.dimen.font_size_littlesmall));
            }
            else {
                tv0.setText(row.getBar());
                int size2 = row.getCategory().length();
                if (size2 < 6) {
                    tv.setText("\n" + row.getCategory().substring(0, size2)+"\n");
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimension(R.dimen.font_size_asmall));
                } else if (size2 < 11) {
                    tv.setText(row.getCategory().substring(0, 4) + "\n" + row.getCategory().substring(4, size2)+"\n");
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimension(R.dimen.font_size_asmall));
                } else {
                    tv.setText(row.getCategory().substring(0, 4) + "\n" + row.getCategory().substring(4, 8) + "\n" + row.getCategory().substring(8, size2));
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimension(R.dimen.font_size_asmall));
                }
                tv.setTextColor(Color.parseColor("#000000"));
                tv.setGravity(Gravity.CENTER_VERTICAL);

            }

                // 2??????(?????????)
//            final TextView tv2 = new TextView(this);
            tv2.setLayoutParams(new
                    TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tv2.setGravity(Gravity.LEFT);
            tv2.setPadding(5, 15, 0, 15);
            if (i == -1) {
                tv2.setText("?????????");
                tv2.setTextColor(Color.parseColor("#000000"));
                tv2.setBackgroundColor(Color.parseColor("#ADD8E6"));
                tv2.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimension(R.dimen.font_size_littlesmall));
            }
            else {
                int size = row.getStockName().length();
                tv2.setGravity(Gravity.CENTER_VERTICAL);
                tv2.setTextColor(Color.parseColor("#000000"));
                if (size < 14) {
                    tv2.setText("\n" +row.getStockName().substring(0, size)+"\n");
                    tv2.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimension(R.dimen.font_size_asmall));
                } else if (size < 27) {
                    tv2.setText(row.getStockName().substring(0, 13) + "\n" + row.getStockName().substring(13, size)+"\n");
                    tv2.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimension(R.dimen.font_size_asmall));
                } else {
                    tv2.setText(row.getStockName().substring(0, 13) + "\n" + row.getStockName().substring(13, 26) + "\n" + row.getStockName().substring(26, size));
                    tv2.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimension(R.dimen.font_size_asmall));
                }

//                tv2.setClickable(true);
//                tv2.setOnClickListener(new View.OnClickListener() {
//                    public void onClick(View v) {
//
////                        selectBar(tv0.getText().toString());
//
//                    }
//                });

            }
            // 3??????(?????????)
//            final TextView tv3 = new TextView(this);
            tv3.setLayoutParams(new
                    TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tv3.setGravity(Gravity.LEFT);
            tv3.setPadding(5, 15, 0, 15);
            if (i == -1) {
                tv3.setText("??????");
                tv3.setBackgroundColor(Color.parseColor("#ADD8E6"));
                tv3.setTextColor(Color.parseColor("#000000"));

                tv3.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimension(R.dimen.font_size_littlesmall));
            }
            else {
                tv3.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimension(R.dimen.font_size_medium));
                tv3.setGravity(Gravity.CENTER);
                tv3.setTextColor(Color.parseColor("#000000"));
                tv3.setText("\n" + row.getStock() + "\n");
            }
            // 4??????(?????????)
//            final TextView tv4 = new TextView(this);
//            tv4.setLayoutParams(new
//                    TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
//                    TableRow.LayoutParams.WRAP_CONTENT));
//            tv4.setGravity(Gravity.LEFT);
//            tv4.setPadding(5, 15, 0, 15);
//            if (i == -1) {
//                tv4.setText("?????????");
//                tv4.setBackgroundColor(Color.parseColor("#d9d9d9"));
//                tv4.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimension(R.dimen.font_size_asmall));
//            }
//            else {
//                String yahoolink = "https://shopping.yahoo.co.jp/search?sc_i=shp_sp_search_sort_sortitem&p=" + row.getBar();
//                String amazonlink = "https://www.amazon.co.jp/s?s=price-asc-rank&k=" + row.getBar();
//                String rakutenlink = "https://search.rakuten.co.jp/search/mall/" + row.getBar();
//
//                tv4.setText("yahoo"+"\n"+"amazon"+"\n"+"rakuten");
//                tv4.setTextSize(jts.resizeTextView(tv4,20));
//                Pattern pattern = Pattern.compile("yahoo");
//                Pattern pattern2 = Pattern.compile("amazon");
//                Pattern pattern3 = Pattern.compile("rakuten");
//                Linkify.TransformFilter filter = new Linkify.TransformFilter() {
//                    @Override
//                    public String transformUrl(Matcher match, String url) {
//                        return yahoolink + "&X=2";
//                    }
//                };
//                Linkify.TransformFilter filter2 = new Linkify.TransformFilter() {
//                    @Override
//                    public String transformUrl(Matcher match, String url) {
//                        return amazonlink;
//                    }
//                };
//                Linkify.TransformFilter filter3 = new Linkify.TransformFilter() {
//                    @Override
//                    public String transformUrl(Matcher match, String url) {
//                        return rakutenlink + "?s=2";
//                    }
//                };
//                Linkify.addLinks(tv4, pattern, yahoolink, null, filter);
//                Linkify.addLinks(tv4, pattern2, amazonlink, null, filter2);
//                Linkify.addLinks(tv4, pattern3, rakutenlink, null, filter3);
//            }
            // ???????????????????????????
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
//            tr.addView(tv4);
            mTableLayout.addView(tr, trParams);

            tr.setClickable(true);
            tr.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    selectBar(tv0.getText().toString());

                    tr.setBackgroundColor(Color.parseColor("#FFBEE6BE"));
                    Timer timer1;
                    timer1 = new Timer();
                    timer1.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            Log.d("run", "TimerTask Thread id = " + Thread.currentThread().getId());
                            tr.setBackgroundColor(Color.parseColor("#FFFFFF"));;
                        }
                    }, 2000);
                }
            });




            // ???????????????
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
                tvSepLay.span = 3;
                tvSep.setLayoutParams(tvSepLay);
                tvSep.setBackgroundColor(Color.parseColor("#d9d9d9"));
                tvSep.setHeight(2);
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
    //yahoo??????????????????
    public void getYahoo(View view) {

        TextView yr = findViewById(R.id.yahooURL);

        if(!"".equals(yr.getText().toString())) {
            Intent intent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(yr.getText().toString()));

            startActivity(intent);
        }
    }

    //amazon??????????????????
    public void getAmazon(View view) {

        EditText bar = findViewById(R.id.barcode);
        String bc = bar.getText().toString();
        if(bc.matches("[+-]?\\d*(\\.\\d+)?")) {
            String amazonlink = "https://www.amazon.co.jp/s?s=price-asc-rank&k=" + bc;


            Intent intent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(amazonlink));

            startActivity(intent);
        }
    }

    //rakuten??????????????????
    public void getRakuten(View view) {

        EditText bar = findViewById(R.id.barcode);
        String bc = bar.getText().toString();
        if(bc.matches("[+-]?\\d*(\\.\\d+)?")) {
            String rakutenlink = "https://search.rakuten.co.jp/search/mall/" + bc + "?s=2";

            Intent intent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(rakutenlink));

            startActivity(intent);
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////
    private void openChooserToShareThisApp() {
        ShareCompat.IntentBuilder builder
                = ShareCompat.IntentBuilder.from(MainActivity.this);

        String subject = "???" + getNow() +"??????????????????????????????\n";

        StringBuilder body = new StringBuilder();
        body.setLength(0);
//        body.append(subject + "\n\n");

//        body.append("???????????????,????????????,?????????,?????????\n");

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
        builder.setSubject(subject) /// ??????
                .setText(bodyText)  /// ??????
                .setType("text/plain");
        Intent intent = builder.createChooserIntent();

        /// ?????????????????????????????????
        builder.startChooser();
    }

    // CSV???????????????????????????
    public void csvReader(Uri uri) {
//            AssetManager assetManager = context.getResources().getAssets();
        try {
            if (uri.getScheme().equals("content")) {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferReader = new BufferedReader(inputStreamReader);
                String line;

                while ((line = bufferReader.readLine()) != null) {

                    //???????????????????????????????????????????????????
                    String[] RowData = line.split(",");

                    allInsertData(db, RowData[0], RowData[3], RowData[2], RowData[1]);

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

        //???????????????
        StringBuilder today = new StringBuilder();
        today.setLength(0);
        Calendar dd = Calendar.getInstance();
        today.append(dd.get(Calendar.YEAR));
        today.append("???");
        today.append(dd.get(Calendar.MONTH) + 1);
        today.append("???");
        today.append(dd.get(Calendar.DAY_OF_MONTH));
        today.append("??? ");
        today.append(dd.get(Calendar.HOUR));
        today.append("???");
        today.append(dd.get(Calendar.MINUTE));
        today.append("???");

        return today.toString();

    }

}