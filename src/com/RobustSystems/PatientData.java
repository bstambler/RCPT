package com.RobustSystems;

import java.util.*;

import android.content.Context;

import java.text.DateFormat;
import java.text.SimpleDateFormat;


public final class PatientData extends HashMap<String, Object> {
	
	public PatientData()
	{
	}
	
	public String getPatientName()
	{
		return (String) this.get("PatientName");
	}
	
	public Date getCreateDate()
	{
		return (Date) this.get("CreateDate");
	}
	
	public PatientData(String s) {
		String[] items = s.split("\0");
		
		this.put("Key", items[0]);
		
	    String[] names = items[0].split("%");
	    						
	    this.put("CreateDate", Utils.ConvertToDate(names[0]));		
		this.put("PatientName", names[1]);
		this.put("Flag", Utils.ConvertToInt(items[1], 0));		
		this.put("Code", items[2]);
	    //* this.put("DOB", Utils.ConvertToDate(items[3], context, null));
	    this.put("DOB", Utils.ConvertToDate(items[3]));
	    this.put("Procedure", Utils.ToSafeStr(items[4]));
	    this.put("Procedure2", Utils.ToSafeStr(items[5]));
	    this.put("Outcome", Utils.ToSafeStr(items[6]));
	    this.put("Physician", Utils.ToSafeStr(items[7]));
	    this.put("Reference", Utils.ToSafeStr(items[8]));
	    this.put("Procedure3", Utils.ToSafeStr(items[9]));
	    this.put("ID1", Utils.ConvertToInt(items[10], 0));
	    this.put("ID2", Utils.ConvertToInt(items[11], 0));
	    
	    if (items.length > 12)
	    {
		    this.put("Notes", Utils.ToSafeStr(items[12]));	    	
	    }
	}
		
	public void Clone(final PatientData data)
	{
		Iterator<String> keys = data.keySet().iterator();
		
		while(keys.hasNext()) 
		{
			String key = keys.next(); 
			Object value = (Object) data.get(key);
			
			if (value instanceof String)
			{
				String s = value.toString();
				
				this.put(key, s);
			}
			else if (value instanceof Integer)
			{
				int i = Integer.valueOf(value.toString());
				
				this.put(key, i);
			}
			else if (value instanceof Date)
			{
				Date dt = new Date(((Date) value).getTime());
				
				this.put(key, dt);
			}
		}
		
		this.put("Key", Utils.ConvertFromDate(this.get("CreateDate")) + '%' + this.get("PatientName"));
	}
	
	@Override
	public String toString()
	{
		return this.get("Key").toString();
	}
	
	public String GetData()
	{
		StringBuilder builder = new StringBuilder();
		
		String key = Utils.ConvertFromDate(this.get("CreateDate")) + '%' + this.get("PatientName");

		builder.append(Utils.ToSafeStr(key)); builder.append('\0');
		builder.append(Utils.ToSafeStr(this.get("Flag"))); builder.append('\0');
		builder.append(Utils.ToSafeStr(this.get("Code"))); builder.append('\0');
		builder.append(Utils.ToSafeStr(this.get("DOB"))); builder.append('\0');
		builder.append(Utils.ToSafeStr(this.get("Procedure"))); builder.append('\0');
		builder.append(Utils.ToSafeStr(this.get("Procedure2"))); builder.append('\0');
		builder.append(Utils.ToSafeStr(this.get("Outcome"))); builder.append('\0');
		builder.append(Utils.ToSafeStr(this.get("Physician"))); builder.append('\0');
		builder.append(Utils.ToSafeStr(this.get("Reference"))); builder.append('\0');
		builder.append(Utils.ToSafeStr(this.get("Procedure3"))); builder.append('\0');
		builder.append(Utils.ToSafeStr(this.get("ID1"))); builder.append('\0');
		builder.append(Utils.ToSafeStr(this.get("ID2"))); builder.append('\0');
		builder.append(Utils.ToSafeStr(this.get("Notes"))); builder.append('\0');
		
		return builder.toString();
	}
	
	public void InitDefault()
	{
	    this.put("CreateDate", new Date());		
		this.put("PatientName", "");
		this.put("Flag", 0);		
		this.put("Code", "");
	    //* this.put("DOB", Utils.ConvertToDate(items[3], context, null));
	    this.put("DOB", new Date());
	    this.put("Procedure", "");
	    this.put("Procedure2", "");
	    this.put("Outcome", "");
	    this.put("Physician", "");
	    this.put("Reference", "");
	    this.put("Procedure3", "");
	    this.put("ID1", 0);
	    this.put("ID2", 0);
	    
		this.put("Key", Utils.ConvertFromDate(this.get("CreateDate")) + '%' + this.get("PatientName"));
	}
}