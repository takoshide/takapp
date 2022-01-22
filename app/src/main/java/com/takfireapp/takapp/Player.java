package com.takfireapp.takapp;

import java.util.Date;

public class Player {
    public PlayerData[] getPlayers() {
        PlayerData[] data = new PlayerData[30];
        data[0] = new PlayerData(11,"小笠原　慎之介","投手",new Date(97,10,8));
        data[1] = new PlayerData(12,"田島　慎二","投手",new Date(89,12,21));
        data[2] = new PlayerData(13,"橋本　侑樹","投手",new Date(98,1,8));
        data[3] = new PlayerData(14,"谷元　圭介","投手",new Date(85,1,28));
        data[4] = new PlayerData(16,"又吉　克樹","投手",new Date(90,11,4));
        data[5] = new PlayerData(17,"柳　裕也","投手",new Date(94,4,22));
        data[6] = new PlayerData(18,"梅津　晃大","投手",new Date(96,10,24));
        data[7] = new PlayerData(19,"髙橋　宏斗","投手",new Date(102,8,9));
        data[8] = new PlayerData(21,"岡田　俊哉","投手",new Date(91,12,5));
        data[9] = new PlayerData(22,"大野　雄大","投手",new Date(88,9,26));
        data[10] = new PlayerData(24,"福谷　浩司","投手",new Date(91,1,9));
        data[11] = new PlayerData(25,"佐藤　優","投手",new Date(93,6,29));
        data[12] = new PlayerData(28,"森　博人","投手",new Date(98,5,25));
        data[13] = new PlayerData(29,"山井　大介","投手",new Date(78,5,10));
        data[14] = new PlayerData(33,"祖父江　大輔","投手",new Date(87,8,11));
        data[15] = new PlayerData(34,"福　敬登","投手",new Date(92,6,16));
        data[16] = new PlayerData(36,"岡野　祐一郎","投手",new Date(94,4,16));
        data[17] = new PlayerData(38,"松葉　貴大","投手",new Date(90,8,14));
        data[18] = new PlayerData(40,"石川　翔","投手",new Date(99,12,14));
        data[19] = new PlayerData(41,"勝野　昌慶","投手",new Date(97,6,12));
        data[20] = new PlayerData(42,"ロサリオ","投手",new Date(94,5,18));
        data[21] = new PlayerData(43,"三ツ間　卓也","投手",new Date(92,7,22));
        data[22] = new PlayerData(46,"鈴木　博志","投手",new Date(97,3,22));
        data[23] = new PlayerData(47,"笠原　祥太郎","投手",new Date(95,3,17));
        data[24] = new PlayerData(50,"清水　達也","投手",new Date(99,11,3));
        data[25] = new PlayerData(53,"マルク","投手",new Date(95,7,18));
        data[26] = new PlayerData(54,"藤嶋　健人","投手",new Date(98,5,8));
        data[27] = new PlayerData(59,"山本　拓実","投手",new Date(100,1,31));
        data[28] = new PlayerData(64,"福島　章太","投手",new Date(102,10,24));
        data[29] = new PlayerData(65,"加藤　翼","投手",new Date(102,12,14));
        return data;
    }
}
