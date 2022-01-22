package com.takfireapp.takapp;

import java.util.Date;



public class PlayerData {
    private int _no;
    private String _name;
    private String _position;
    private Date _birth;

    public PlayerData(
            int no,
            String name,
            String position,
            Date birth) {
        _no = no;
        _name = name;
        _position = position;
        _birth = birth;
    }

    public int getNo() {
        return _no;
    }
    public String getName() {
        return _name;
    }
    public String getPosition() {
        return _position;
    }
    public Date getBirth() {
        return _birth;
    }
}

