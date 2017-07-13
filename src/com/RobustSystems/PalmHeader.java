package com.RobustSystems;

public class PalmHeader {
    public String name;
    public short attr;
    public short version;
    public int createDate;
    public int modDate;
    public int backupDate;
    public int modNum;
    public int appInfoID;
    public int sortInfoID;
    public int appType;
    public int createID;
    public int uniqueID;
    
    public PalmHeader()
    {
        name = new String("123456789012345678901234567890123");
        attr = 1;
        version = 2;
        createDate = 3;
        modDate = 4;
        backupDate = 5;
        modNum = 6;
        appInfoID = 7;
        sortInfoID = 8;
        appType = 10;
        createID = 11;
        uniqueID = 12;
    }
}
