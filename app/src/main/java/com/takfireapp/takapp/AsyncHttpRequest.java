package com.takfireapp.takapp;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Map;

public class AsyncHttpRequest extends AsyncTask<String, Void, String> {

    private Activity mainActivity;

    public AsyncHttpRequest(Activity activity) {

        // 呼び出し元のアクティビティ
        this.mainActivity = activity;
    }

    // 非同期で処理される部分
    @Override
    protected String doInBackground(String... strURL) {

        try {
            // URLクラスを使用して通信を行う
            URL url = new URL(strURL[0]);
            URLConnection connection = url.openConnection();
            // 動作を入力に設定
            connection.setDoInput(true);
            InputStream stream = connection.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(
                    stream));

            // responsデータの取得
            String data = "";
            String tmp = "";
            while ((tmp = input.readLine()) != null) {
                data += tmp;
            }
            // 終了処理
            stream.close();
            input.close();
            return data;
        } catch (Exception e) {
            // (5)エラー処理
            return e.toString();
        }

    }


    // このメソッドは非同期処理の終わった後に呼び出されます
    @Override
    protected void onPostExecute(String result) {

        // 取得した結果をEditTextに入れる
        EditText tv = (EditText) mainActivity.findViewById(R.id.proname);

        JsonFactory factory = new JsonFactory();
        try {
            JsonParser parser = factory.createParser(result);
            ArrayList<String> title = new ArrayList<>();
            int i = 0;
            boolean break_flag = false;
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (break_flag) {
                    break; // 外側ループを抜ける
                }
                while (parser.nextToken() != JsonToken.END_OBJECT) {
                    String name = parser.getCurrentName();
                    if (name != null) {
                        parser.nextToken();
                        Log.d("name", name);
                        if (name.equals("name")) {
                            title.add(parser.getText());
                            break_flag = true;
                            break;
                        }else if(name.equals("totalResultsAvailable")){
                            if("0".equals(parser.getText())){
                                break_flag = true;
                                break;
                            }
                        }else if(name.equals("Error")){
                                break_flag = true;
                                break;
                        }
                    }
                }
            }
            String titles = "";
            if(title.size() == 0) {
                titles = "商品名が見つかりませんでした。ここから40文字まで登録できます。";
            }else{
                titles = title.get(0);
            }
                tv.setTextSize(12.0f);

                int size = titles.length();

                if (size < 20) {
                    tv.setText(titles.substring(0, size));
                } else if (size < 40) {
                    tv.setText(titles.substring(0, 20) + "\n" + titles.substring(20, size));
                } else {
                    tv.setText(titles.substring(0, 20) + "\n" + titles.substring(20, 40));
                }


        } catch (JsonParseException e) {
            // TODO 自動生成された catch ブロック
            tv.setText("バーコードの読み取りでエラーが発生しました。在庫の登録ができません。");
            tv.setTextSize(12.0f);
            tv.setTextColor(Color.RED);
        } catch (IOException e) {
            // TODO 自動生成された catch ブロック
            tv.setText("バーコードの読み取りでエラーが発生しました。在庫の登録ができません。");
            tv.setTextSize(12.0f);
            tv.setTextColor(Color.RED);
        }

    }

}
