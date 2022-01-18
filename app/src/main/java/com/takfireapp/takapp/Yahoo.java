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

}


