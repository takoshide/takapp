package com.takfireapp.takapp;

import android.net.Uri;

public class Yahoo {

    /**
     * Yahoo!ディベロッパーのAPP ID
     */
    private static String APP_ID = "dj00aiZpPTllc29SZTJjWGxNRCZzPWNvbnN1bWVyc2VjcmV0Jng9ZjE-";

    /**
     * Yahoo!ショッピングAPIのベースURI
     */
    private static String BASE_URI = "https://shopping.yahooapis.jp/ShoppingWebService/V3/itemSearch";

    public String getYahooUrl(String args){

            Uri.Builder builder = Uri.parse(BASE_URI).buildUpon();
            builder.appendQueryParameter("appid", APP_ID)
                    .appendQueryParameter("jan_code", args)
                    .appendQueryParameter("results", "1")
                    .appendQueryParameter("sort", "+price")
                    .build();

            return builder.toString();

    }

    public String getYahooBCUrl(String args){

        Uri.Builder builder = Uri.parse(BASE_URI).buildUpon();
        builder.appendQueryParameter("appid", APP_ID)
                .appendQueryParameter("affiliate_type", "vc")
//                .appendQueryParameter("affiliate_id", "https%3A%2F%2Fck.jp.ap.valuecommerce.com%2Fservlet%2Freferral%3Fsid%3D3634447%26pid%3D887640716%26vc_url%3D")
                .appendQueryParameter("affiliate_id", "https://ck.jp.ap.valuecommerce.com/servlet/referral?sid=3634447&pid=887640716&vc_url=")
                .appendQueryParameter("sort", "+price")
                .appendQueryParameter("jan_code", args)
                .build();

        return builder.toString();

    }

}


