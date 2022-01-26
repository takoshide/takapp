package com.takfireapp.takapp;

import java.util.Date;

public class StockData {
    private String _category;
    private String _stockname;
    private String _stock;
    private String _bar;
    private Date _date;

    public StockData(
            String category,
            String stockname,
            String stock,
            String bar,
            Date date) {
        _category = category;
        _stockname = stockname;
        _stock = stock;
        _bar = bar;
        _date = date;
    }

    public String getBar() {
        return _bar;
    }
    public String getCategory() {
        return _category;
    }
    public String getStockName() {
        return _stockname;
    }
    public String getStock() {
        return _stock;
    }
    public Date getDate() { return _date; }
}
