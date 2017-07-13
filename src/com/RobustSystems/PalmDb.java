package com.RobustSystems;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.nio.*;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.RobustSystems.PatientData;
import com.RobustSystems.Tuple;

public class PalmDb 
{
	private static PalmHeader _header = null;

	public static Boolean LoadData(String path, Collection<PatientData> coll, ITaskCanceller canceller) 
	{
		File cptFile = new java.io.File(path);
		
		try 
		{
			FileInputStream fs = new FileInputStream(cptFile);
			
			int count = readHeader(fs);			
			int start = readBytesInt(fs, 4);
			
			ArrayList<Tuple<Integer, Integer>> posArray = new ArrayList<Tuple<Integer, Integer>>();
			
			for (int i = 0; i < count - 1; i++)
			{
				int end = readRecord(start, fs);
				
				//* if (end > start) {
					posArray.add(new Tuple<Integer, Integer>(start, end - start));
					start = end;
				//* }
					
				try 
				{
					if (canceller != null)
					{
						if (canceller.isTaskCancelled()) 
						{
							return true;
						}
					}
				} 
				catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
						 
			fs.close();
			
			Collections.sort(posArray, new Comparator<Tuple<Integer, Integer>>() 
			{
			    @Override
			    public int compare(Tuple<Integer, Integer> entry1, Tuple<Integer, Integer> entry2)
			    {
			        return entry1.x.compareTo(entry2.x);
			    }
			});
			
			fs = new FileInputStream(cptFile);
			
			start = 0;
			
			String data;
			
			for(Tuple<Integer, Integer> current : posArray)
			{
				fs.skip(current.x - start);
				
				data = readBytesStr(fs, current.y);								
				coll.add(new PatientData(data));
				canceller.onTaskProgress(coll.size());
				
				start = current.x + current.y;
				
				try 
				{
					if (canceller != null)
					{						
						if (canceller.isTaskCancelled())
						{
							return false;
						}
					}
				}
				catch (Exception e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			fs.close();
		}
		catch(IOException ex) 
		{
			return false;
		}
		
		return true;
	}
	
	public static Boolean SaveData(String path, PatientData[] _coll, ITaskCanceller canceller)
	{
		File cptFile = new java.io.File(path);
		FileOutputStream stm = null;
		
		if (_header == null)
		{
			_header = new PalmHeader();
		}
		
		try 
		{
			stm = new FileOutputStream(cptFile, false);			
			short count = (short) _coll.length;
						
	        int pos = Utils.writeHeader(stm, _header, (short) (count + 1));
	        
	        pos += 8 * (count + 1);   // add space for indexes

	        pos += 2;   // add a few???
	        
	        // write indexes
	        	        
	        ArrayList<Tuple<Integer, Integer>> fileIdx = new ArrayList<Tuple<Integer, Integer>>();
	        
	        /*for (Iterator<PatientData> iter = coll.iterator(); iter.hasNext();)
	        {
	        	PatientData item = iter.next();
	        	
	        }*/
	        
	        int i = 0;
	        
	        Utils.WriteDataIdx(stm, pos);
	        
	        for(PatientData item : _coll)
	        {
	        	String strData = item.GetData();
	        	int len =  strData.length();
	        	
	        	fileIdx.add(new Tuple<Integer, Integer>(i, pos));
	        	
	        	pos += len;

	        	Utils.WriteDataIdx(stm, pos);
	        	
				try 
				{
					if (canceller != null)
					{						
						if (canceller.isTaskCancelled())
						{
							return false;
						}
					}
				}
				catch (Exception e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	
	        	canceller.onTaskProgress(i);
	        	
	        	i++;
	        }
	        
	        // start from the beginning of the file
	        //* stm.Seek(0, SeekOrigin.Begin);
	        
	        // add two bytes
	        WriteString(stm, "  ", 2);

	        pos = 0;
	        i = 0;

	        for(PatientData item : _coll)
	        {
	        	String strData = item.GetData();
	        	int len = strData.length();

	        	//* stm.Seek(fileIdx[i] - pos, SeekOrigin.Current);

	        	WriteString(stm, strData, len);

	        	//* pos = fileIdx[i] + len;
	        	i++;
	        }
	        
			stm.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public File getTempFile(Context context, String url) 
	{
	    File file = null;
	    
	    try 
	    {
	        String fileName = Uri.parse(url).getLastPathSegment();
	        file = File.createTempFile(fileName, null, context.getCacheDir());
	    }
	    catch (IOException e)
	    {
	    	// Error while creating file
	    }
	    return file;
	}
	
	/*
	C Palm DB RECORD 
    #  78+(4) recordDataOffset from start 
    #  78+(1) Attribute 16-Secret bit 32 Record in use (busy bit) 
    #           64-Dirty record bit  128-Delete record on next HotSync 
    #  78+(3) UniqueID for record. Often count from 0 
    # Comment:ADD ?2 bytes? GAP to data 2 zero bytes 
    #             ?AppInfo if present 
    #             ?SortInfo if present
    */

	private static int readRecord(int start, FileInputStream fs) throws IOException {
		int dummy3 = readBytesInt(fs, 4);
		int end = readBytesInt(fs, 4);

		return end;
	}

	private static int readHeader(FileInputStream fs) throws IOException {
		
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
	    
	    return count;
	}
	
	private static String readBytesStr(FileInputStream fs, int len) throws IOException {
		byte[] bytes = new byte[len];
		int length = fs.read(bytes);
		
		if (length < len) {
			throw new EOFException();
		}
		
		return new String(bytes);
	}
	
    private static int WriteString(FileOutputStream fs, String val, int len) throws IOException
    {
    	byte[] bytes = val.getBytes("UTF-8");
			
    	if (bytes.length < len)
    	{
    		return -1;
    	}

		fs.write(bytes, 0, len);

    	return len;
    }
	
	private static short readBytesShort(FileInputStream fs, int len) throws IOException {
		byte[] bytes = new byte[len];
		int length = fs.read(bytes);
		
		if (length < len) {
			throw new EOFException();
		}
		
		return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getShort();
	}
	
    private static int writeShort(FileOutputStream fs, short val, int len) throws IOException
    {
      int b0 = val >> 8;
      int b1 = val;

      fs.write((byte) b0);
      fs.write((byte) b1); 

      return 2;
    }
	
	private static int readBytesInt(FileInputStream fs, int len) throws IOException {
		/*byte[] bytes = new byte[len];
		int length = fs.read(bytes);
		
		if (length < len) {
			throw new EOFException();
		}
		
		return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getInt();*/
		
		int ch1 = fs.read();
	    int ch2 = fs.read();
	    int ch3 = fs.read();
	    int ch4 = fs.read();
	    
	    if ((ch1 | ch2 | ch3 | ch4) < 0) {
	        throw new EOFException();
	    }
	    
	    return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
	}
	
    private static int writeInt(FileOutputStream fs, int val, int len) throws IOException
    {
      int b0 = val >> 24;
      int b1 = val >> 16;
      int b2 = val >> 8;
      int b3 = val;

      fs.write((byte)b0);
      fs.write((byte)b1);
      fs.write((byte)b2);
      fs.write((byte)b3);

      return 4;
    }

	public static void SaveData(File _myInternalFile, PalmHeader header, PatientData[] getPatientData, Object canceller) {
		
	}
}

