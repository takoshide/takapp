package com.takfireapp.takapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.icu.util.Calendar;
import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Date;

public class BackUpFile extends MainActivity{

    private static final int CREATE_DOCUMENT_REQUEST = 10;
    private static final int RESULT_FAILED           = 4;

    public void fileopen_for_SAF() {
        try {
            //ファイル名を作成。
            StringBuilder fileName = new StringBuilder();
            fileName.append("stock_list_");
            fileName.append(getToday()); //ここで作成日時を取得。
            fileName.append(".csv");

            //「Intent.ACTION_CREATE_DOCUMENT」は、ファイルを選択するためのIntent。
            Intent it = new Intent(Intent.ACTION_CREATE_DOCUMENT);

            //ここで取得するファイルの種類を制限する。（今回はALLタイプ）
            it.setType("*/*");

            //ファイル名をセットして、Intentを起動。
            it.putExtra(Intent.EXTRA_TITLE, fileName.toString());

            /* 「CREATE_DOCUMENT_REQUEST」は、Private intで予め定義しておく。 */
            startActivityForResult(it, CREATE_DOCUMENT_REQUEST);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getToday(){
        try {
            StringBuilder today = new StringBuilder();
            today.setLength(0);

            //日付の設定
            Calendar dd = Calendar.getInstance();
            today.append(dd.get(Calendar.YEAR));
            today.append("_");
            today.append(dd.get(Calendar.MONTH) + 1);
            today.append("_");
            today.append(dd.get(Calendar.DAY_OF_MONTH));
            today.append("_");
            today.append(dd.get(Calendar.HOUR));
            today.append("_");
            today.append(dd.get(Calendar.MINUTE));

            return today.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        /* 「CREATE_DOCUMENT_REQUEST」は、Privateで予め定義しておく。 */

        if (requestCode == CREATE_DOCUMENT_REQUEST) {
            //エクスポート
            if (resultCode == RESULT_OK) {
                Uri create_file = data.getData();  //取得した保存先のパスなど。

                //出力処理を実行。その際の引数に上記のUri変数をセットする。
                if (exportCsv_for_SAF(create_file)) {
                    //出力に成功した時の処理。
                } else {
                    //出力に失敗した時の処理。
                }
            } else if (resultCode == RESULT_FAILED) {
                //そもそもアクセスに失敗したなど、保存処理の前に失敗した時の処理。
            }
        }

        //リストの再読み込み
//        this.LoadData();

        onActivityResult(requestCode, resultCode, data);
    }

    public Boolean exportCsv_for_SAF(Uri openFile) {

        try {
            OutputStream os = getContentResolver().openOutputStream(openFile);
            OutputStreamWriter os_write = new OutputStreamWriter(os, Charset.forName("Shift_JIS")); //後々インポートする際に困るのでエンコードを指定しておく。
            PrintWriter pw = new PrintWriter(os_write);

            StringBuilder rec = new StringBuilder(); //Export_data用

            //Read Data

//            /* ここにExportするデータを取得するためのDBインスタンスなどを記述する。*/
//
//            StringBuilder sql = new StringBuilder();
//            sql.setLength(0);
//            sql.append(/* 取得用のSQL */)
//
//            c = db.rawQuery(sql.toString(), null);
//            while (c.moveToNext()) {
//                rec.setLength(0);
//
//                rec.append(/* 取得カラムデータをExport用Builderにセット。 */);
//          /* ↑をExportするカラム分呼び出す。*/
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
                    rec.append(
                            cursor.getString(0)+","+
                            cursor.getString(1)+","+
                            cursor.getString(2)+","+
                            cursor.getString(3)
                            );

                    cursor.moveToNext();
                }

                pw.println(rec.toString()); //Export

            } finally {
                if( cursor != null ){
                    cursor.close();
                }
            }
            //閉める。
            pw.close();
            os_write.close();
            os.close();

        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (db.isOpen()) {
                db.endTransaction();
            }
        }
        return true;
    }

}
