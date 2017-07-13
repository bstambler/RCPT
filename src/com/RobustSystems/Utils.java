package com.RobustSystems;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.content.Context;
import android.net.ParseException;
import android.os.Environment;

public class Utils 
{
	public static DateFormat patientDateFormat = null;
	public static DateFormat defaultDateFormat = null;
	
	/*
    #C Palm DB HEADER 
    #  0(32) name 
    #  32(2) Attributes 2-ReadOnly 4-Dirty AppInfoArea 8-BackupDBase 
    #         16-OK to install newer over existing 32-Reset after install 
    #         64-Don't allow beamed to other 
    #  34(2) Version 
    #  36(4) Creation Date No of secs from 1/1/1904 --- 
    #  40(4) Modification date -- 
    #  44(4) Last Back-up date 
    #  48(4) Modification Number 
    #  52(4) AppInfoID offset to start of App info or null 
    #  56(4) SortInfoID offset to start of Sort info or null 
    #  60(4) Type Application=appl 
    #  64(4) creator 
    #  68(4) UniqueIDseed-internal use 
    #  72(4) nectRecordListID - used internally 
    #  76(2) Number of records 
     
    #C Palm DB RECORD 
    #  78+(4) recordDataOffset from start 
    #  78+(1) Attribute 16-Secret bit 32 Record in use (busy bit) 
    #           64-Dirty record bit  128-Delete record on next HotSync 
    #  78+(3) UniqueID for record. Often count from 0 
    # Comment:ADD ?2 bytes? GAP to data 2 zero bytes 
    #             ?AppInfo if present 
    #             ?SortInfo if present 
	*/
	
	private static final String LOG_TAG = "IOUtilities";
	
	public static File getExternalFile(String file) {
        return new File(Environment.getExternalStorageDirectory(), file);
	}
	
	public static void closeStream(Closeable stream) {
		if (stream != null) {
                try {
                        stream.close();
                } catch (IOException e) {
                        android.util.Log.e(LOG_TAG, "Could not close stream", e);
                }
        }
	}
	
	public static void readFile() {
	}
	
	private static Boolean readHeader(FileInputStream fs) throws IOException {
		
		String name = readBytesStr(fs, 32);
		short attr = readBytesShort(fs, 2);
		short ver = readBytesShort(fs, 2); 
	    int createDate = readBytesInt(fs, 4); 	// Creation Date No of secs from 1/1/1904 --- 
	    int modDate = readBytesInt(fs, 4); 		// Modification date -- 
	    int backupDate = readBytesInt(fs, 4);	// Last Back-up date 
	    int modNumber = readBytesInt(fs, 4);	// Modification Number 
	    int appInfoID = readBytesInt(fs, 4);	// AppInfoID offset to start of App info or null 
	    int sortInfoID = readBytesInt(fs, 4);	// SortInfoID offset to start of Sort info or null 
	    int typeApp = readBytesInt(fs, 4); 		// Type Application
	    int createID = readBytesInt(fs, 4); 
	    int uniqueID = readBytesInt(fs, 4); 	// UniqueIDseed-internal use
	    
	    readBytesInt(fs, 4);	// skip, used internally
	    
	    short count = readBytesShort(fs, 2);		// Number of records 
		
		return true;
	}
	
	public static int writeHeader(FileOutputStream fs, PalmHeader hdr, short count) throws IOException {
		
		int pos = 0;

		pos += WriteString(fs, hdr.name, 32);
		pos += WriteShort(fs, hdr.attr, 2);
		pos += WriteShort(fs, hdr.version, 2);
		pos += WriteInt(fs, hdr.createDate, 4);
		pos += WriteInt(fs, hdr.modDate, 4);
		pos += WriteInt(fs, hdr.backupDate, 4);
		pos += WriteInt(fs, hdr.modNum, 4);
		pos += WriteInt(fs, hdr.appInfoID, 4);
		pos += WriteInt(fs, hdr.sortInfoID, 4);
		pos += WriteInt(fs, hdr.appType, 4);
		pos += WriteInt(fs, hdr.createID, 4);
		pos += WriteInt(fs, hdr.uniqueID, 4);
		
		int dummy = -1;

		pos += WriteInt(fs, dummy, 4);

		pos += WriteShort(fs, count, 2);

		return pos;
	}
	
    private static int WriteString(FileOutputStream fs, String val, int len) throws IOException
    {
    	byte[] bytes = null;
    	
		bytes = val.getBytes("UTF-8");
		
    	if (bytes.length < len)
    	{
    		return -1;
    	}

		fs.write(bytes, 0, len);

    	return len;
    }

    private static int WriteShort(FileOutputStream fs, short val, int len) throws IOException
    {
      int b0 = val >> 8;
      int b1 = val;

	  fs.write((byte) b0);
	  fs.write((byte) b1); 

      return 2;
    }

    private static int WriteInt(FileOutputStream fs, int val, int len) throws IOException
    {
      int b0 = val >> 24;
      int b1 = val >> 16;
      int b2 = val >> 8;
      int b3 = val;

      fs.write((byte) b0);
      fs.write((byte) b1);
      fs.write((byte) b2);
      fs.write((byte)b3);

      return 4;
    }
    
    public static void WriteDataIdx(FileOutputStream fs, int pos) throws IOException
    {
      int attr = 1086740224;

      WriteInt(fs, pos, 4);
      WriteInt(fs, attr, 4);
    }

	private static String readBytesStr(FileInputStream fs, int len) throws IOException {
		byte[] bytes = new byte[len];
		int length = fs.read(bytes);
		
		if (length < len) {
			throw new EOFException();
		}
		
		return new String(bytes);
	}
	
	private static short readBytesShort(FileInputStream fs, int len) throws IOException {
		return 0;
	}
	
	private static int readBytesInt(FileInputStream fs, int len) throws IOException {
		return 0;
	}

	/*public static boolean WriteFile(String path, PalmDb palmDb)
    {
      try (FileOutputStream outputStream = new FileOutputStream(path, false))
      {
        short count = (short) palmDb.size();
        int pos = WriteHeader(fs, palmDb.header, (short) (count + 1));

        pos += 8 * (count + 1);   // add space for indexes

        pos += 2;   // add a few???

        Dictionary<int, int> fileIdx = new Dictionary<int, int>();

        WriteDataIdx(fs, pos);

        for (int i = 0; i < count; i++)
        {
          int len =  palmDb.pockList[i].GetData().Length;

          fileIdx.Add(i, pos);
          pos += len;

          WriteDataIdx(fs, pos);
        }

        // start from the beginning of the file
        fs.Seek(0, SeekOrigin.Begin);

        pos = 0;

        for (int i = 0; i < count; i++)
        {
          string data = palmDb.pockList[i].GetData();
          int len = data.Length;

          fs.Seek(fileIdx[i] - pos, SeekOrigin.Current);

          WriteString(fs, data, len);

          pos = fileIdx[i] + len;
        }
      }

      return true;
    }*/
	
	public static boolean isExternalStorageReadOnly() {  
		String extStorageState = Environment.getExternalStorageState();
		
		if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {  
		   return true;  
		}
		
		return false;  
	}  
		 
	public static boolean isExternalStorageAvailable() {  
		String extStorageState = Environment.getExternalStorageState();  
		
		if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {  
			return true;  
		}
		
		return false;  
	}
	
	//* http://www.informit.com/articles/article.aspx?p=2066699&seqNum=4	
	
	public static DateFormat GetDefaultDateFormat(Context ctx)
	{
		if (defaultDateFormat == null)
		{
			if (ctx != null)
			{
				defaultDateFormat = android.text.format.DateFormat.getDateFormat(ctx.getApplicationContext());
			}
			else
			{
				defaultDateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US); 				
			}
		}
		
		return defaultDateFormat;
		
	}
	
	public static int ConvertToInt(String s, int defValue)
	{
		if (s == null)
		{
			return defValue;
		}
		
		try 
		{			
		    return Integer.parseInt(s);
		} 
		catch(NumberFormatException nfe) 
		{
			return defValue;
		} 
	}
	
	public static Date ConvertToDate(String s, Context context, Date defValue)
	{
		if (s == null)
		{
			return defValue;
		}
	
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context.getApplicationContext());
        
        //* String pattern = ((SimpleDateFormat) dateFormat).toLocalizedPattern();
            
		try 
		{		
			return dateFormat.parse(s);
		} 
		catch(Exception e) 
		{
			return defValue;
		} 
	}
	
	public static Date ConvertToDate(String s, DateFormat dateFormat, Date defValue)
	{
		if (s == null)
		{
			return defValue;
		}
            
		try 
		{		
			return dateFormat.parse(s);
		} 
		catch(Exception e) 
		{
			return defValue;
		} 
	}
	
	public static Date ConvertToDate(String s)
	{
		if (s == null)
		{
			return null;
		}
		
		if (Utils.patientDateFormat == null)
		{
			patientDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);
		}
		
		try 
		{
			return patientDateFormat.parse(s);
		} 
		catch (java.text.ParseException e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static String ConvertFromDate(Date dt)
	{
		if (dt == null)
		{
			return "";
		}
		
		if (Utils.patientDateFormat == null)
		{
			patientDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);
		}

		return patientDateFormat.format(dt);
	}
	
	public static String ConvertFromDate(Object dt)
	{
		if (dt == null)
		{
			return "";
		}
		
		if (dt instanceof Date)
		{
			return ConvertFromDate((Date) dt);
		}
		
		return "";
	}

	public static String ToSafeStr(String s)
	{
		if (s == null)
		{
			return "";
		}
		else
		{
			return s;
		}		
	}
	
	public static String ToSafeStr(Object s)
	{
		if (s == null)
		{
			return "";
		}
		else
		{
			return s.toString();
		}		
	}
}
