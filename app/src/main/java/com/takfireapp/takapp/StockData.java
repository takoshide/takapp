package com.takfireapp.takapp;

import java.util.Date;

public class StockData {
    private String _bar;
    private String _stockname;
    private String _stock;
    private Date _date;

    public StockData(
            String bar,
            String stockname,
            String stock,
            Date date) {
        _bar = bar;
        _stockname = stockname;
        _stock = stock;
        _date = date;
    }

    public String getBar() {
        return _bar;
    }
    public String getStockName() {
        return _stockname;
    }
    public String getStock() {
        return _stock;
    }
    public Date getDate() { return _date; }
}
