package com.RobustSystems;

//* import android.app.DialogFragment;
import java.io.File;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
//***** import android.widget.TableRow.LayoutParams;
import android.view.View.OnClickListener;
import android.content.res.AssetManager;
import android.view.inputmethod.InputMethodManager;

public class CustomDialog extends Dialog implements OnClickListener, ITaskListener {

	private String filename = "POCKeALL.pdb";
	private String filepath = "MyFileStorage";
	
	File _myInternalFile;
	File _myExternalFile;
	
	private static CopyOnWriteArrayList<EditDefinitionItem> _editDefinitions = null;
	private Map<String, View> _controls = new HashMap<String, View>();
	private Date _currentDate;
	private ProgressDialog _progressDialog;
	
	private DateFormat _dateFormat = null;
	
	SaveTaskAsync _saver = null;
	
	public CustomDialog(Context context)
	{
		super(context);
		
		if (_editDefinitions == null)
		{
			ReadXml(context);
			//* InitXml(context);
		}
		
		if (_dateFormat == null)
		{
			_dateFormat = android.text.format.DateFormat.getMediumDateFormat(getContext().getApplicationContext());
		}		
	}
	
	public static void ReadXml(Context context) 
	{
	   try 
	   {
		   _editDefinitions = new CopyOnWriteArrayList<EditDefinitionItem>(); 
	   
		   AssetManager assetMgr = context.getAssets();
		   InputStream istm = assetMgr.open("PatientEdit.xml");
		 
		   DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();		 
		   Document doc = dBuilder.parse(istm);
		 
		   //* System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
		   
		   Element docElem = doc.getDocumentElement();
		   
		   NodeList nodeList = docElem.getElementsByTagName("Control");
		   
		   for (int i = 0; i < nodeList.getLength(); i++)
		   {
			   _editDefinitions.add(readControl(nodeList.item(i)));
		   }
		 
		   istm.close();		 
	   } 
	   catch (Exception e) {
			System.out.println(e.getMessage());
	   }
	}
	
	private static EditDefinitionItem readControl(Node node) 
	{
		EditDefinitionItem item = new EditDefinitionItem();
		
		if (node.getNodeType() == Node.ELEMENT_NODE) 
		{
			Element elem = (Element) node;
			
			NamedNodeMap attribs = elem.getAttributes();
			
			if (attribs.getNamedItem("name") != null) {
				item.Name = attribs.getNamedItem("name").getNodeValue();
			}
			
			if (attribs.getNamedItem("type") != null) {
				item.Type = attribs.getNamedItem("type").getNodeValue();
			}
						
			if (attribs.getNamedItem("binding") != null) {
				item.Binding = attribs.getNamedItem("binding").getNodeValue();
			}
			
			if (attribs.getNamedItem("prompt") != null) {
				item.Prompt = attribs.getNamedItem("prompt").getNodeValue();
			}
			
			if (attribs.getNamedItem("label") != null) {
				item.Label = attribs.getNamedItem("label").getNodeValue();
			}

			NodeList nodeList = elem.getElementsByTagName("ListItem");
			
			int count = nodeList.getLength();
			
			if (count > 0)
			{
				item.ValueList = new String[count];
				item.DisplayList = new String[count];
				
				for (int i = 0; i < count; i++)
				{
					if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) 
					{
						Element elemList = (Element) nodeList.item(i);
						
						NamedNodeMap attribList = elemList.getAttributes();
						
						if (attribList.getNamedItem("value") != null) {
							item.ValueList[i] = attribList.getNamedItem("value").getNodeValue();
						}
						
						if (attribList.getNamedItem("display") != null) {
							item.DisplayList[i]= attribList.getNamedItem("display").getNodeValue();
						}
					}						
				}
			}
		}
		
		return item;
	}
	
	public Boolean Validate()
	{		
		EditDefinitionItem[] defs = _editDefinitions.toArray(new EditDefinitionItem[_editDefinitions.size()]);
		
		for(int i = 0; i < defs.length; i++)
		{
			View control = _controls.get(defs[i].Binding);
			
			if (control instanceof EditText)
			{
				EditText editText = (EditText) control;
				
				if (editText.getText().length() == 0)
				{
					//* editText.setError("Field cannot be empty");
				}				
			}
		}
		
		return true;
		
		/*for (String key : _controls.keySet()) 
		{					
			View control = _controls.get(key);
		}*/		
	}

	public void SaveData(PatientData data)
	{
		//* ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
		
		_progressDialog = ProgressDialog.show(this.getContext(), null, null);
		
		_progressDialog.setCancelable(false);
		
		//* _progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
		_progressDialog.setProgressStyle(R.layout.progress_dialog);
		_progressDialog.show();
		
        //* _progressDialog.setContentView(new ProgressBar(getContext()));

		//* progressBar.setVisibility(View.VISIBLE);
		
		EditDefinitionItem[] defs = _editDefinitions.toArray(new EditDefinitionItem[_editDefinitions.size()]);
		
		for(int i = 0; i < defs.length; i++)
		{
			String key = defs[i].Binding;
			View control = _controls.get(key);
			
			if (control instanceof EditText)
			{
				EditText editText = (EditText) control;
				String value = editText.getEditableText().toString();
				
				if (defs[i].Type.equalsIgnoreCase("Edit"))
				{
					data.put(key, value);
				}
				else if (defs[i].Type.equalsIgnoreCase("Date"))
				{
					Date dt = Utils.ConvertToDate(value, _dateFormat, null);
					
					data.put(key, dt);
				}
			}
			else if (control instanceof CheckBox)
			{
				CheckBox checkBox = (CheckBox) control;
				boolean isChecked = checkBox.isChecked();
				
				data.put(key, isChecked);
			}
			else if (control instanceof Spinner)
			{
				Spinner spinner = (Spinner) control;
				//* SpinnerAdapter adapter  = spinner.getAdapter();
								
				data.put(key, spinner.getSelectedItem().toString());
			}
		}
		
		if (!Utils.isExternalStorageAvailable() || Utils.isExternalStorageReadOnly())
		{  
			File directory = getContext().getDir(filepath, Context.MODE_PRIVATE);
			
			_myInternalFile = new File(directory , filename);
		} 
		else 
		{
		   _myExternalFile = new File(getContext().getExternalFilesDir(filepath), filename);
		   
			File directory = getContext().getDir("Rcpt", Context.MODE_PRIVATE);

		   _myInternalFile = new File(directory , filename);
		}
		
		_saver = new SaveTaskAsync(_myInternalFile.getPath(), ListingActivity.GetPatientData(), this);
		
		_saver.execute();
				
		/*PalmHeader header = new PalmHeader();		
		
		PalmDb.SaveData(_myInternalFile, header, ListingActivity.GetPatientData(), null);
		//* progressBar.setVisibility(View.INVISIBLE);
		
		//_progressDialog.hide();
		_progressDialog.dismiss();*/
	}
	
	public void Init(final PatientData data, final boolean create, final IDialogResult<PatientData> result)
	{
		this.setContentView(R.layout.properties_dialog);
		
		final Button btnOK = (Button) findViewById(R.id.dialogButtonOK);
		final Button btnCancel = (Button) findViewById(R.id.dialogButtonCancel);
		final Dialog dialog = this;
		
		final TableLayout tblaMain = (TableLayout) findViewById(R.id.maintable);
		
		tblaMain.setOnClickListener(new android.view.View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
		        InputMethodManager mImMan = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		        
		        mImMan.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
			}
		});

        btnOK.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
								
				if (Validate())
				{				
					PatientData newData = new PatientData();
					
					newData.Clone(data);
					
					result.onSave(newData);
					
					SaveData(newData);
					//* dialog.dismiss();					
				}				
			}  
        });
        
        btnCancel.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();					
			}  
        });
        
		EditDefinitionItem[] defs = _editDefinitions.toArray(new EditDefinitionItem[_editDefinitions.size()]);
		TableLayout tl = (TableLayout) findViewById(R.id.maintable);
		
		for(int i = 0; i < defs.length; i++)
		{
			EditDefinitionItem def = defs[i];
			TableRow row;
			
			if (def.Type.equalsIgnoreCase("Edit"))
			{
				row = (TableRow)LayoutInflater.from(getContext()).inflate(R.layout.edit_row, null);
				
	        	TextView textView = (TextView) row.getChildAt(0);
	        	EditText editText = (EditText) row.getChildAt(1);
	        	
	        	textView.setText(def.Label);
	        	
	        	if (def.Binding != null && data.get(def.Binding) != null)
	        	{
	        		editText.setText(data.get(def.Binding).toString());	        			
		        	_controls.put(def.Binding, editText);
	        	}
			
	        	editText.setHint(def.Prompt);
	        	
	            tl.addView(row);
			}
			else if (def.Type.equalsIgnoreCase("Dropdown"))
			{
				row = (TableRow)LayoutInflater.from(getContext()).inflate(R.layout.dropdown_row, null);

				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_item, def.DisplayList);
				
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				//* ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_dropdown_item, def.DisplayList);
				
	        	TextView textView = (TextView) row.getChildAt(0);
	        	Spinner spinner = (Spinner) row.getChildAt(1);
	        	
	        	textView.setText(def.Label);
	        	spinner.setAdapter(adapter);
	        	//* spinner.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
								
	            tl.addView(row);				
			}
			else if (def.Type.equalsIgnoreCase("Checkbox"))
			{
				row = (TableRow)LayoutInflater.from(getContext()).inflate(R.layout.checkbox_row, null);
				
	        	TextView textView = (TextView) row.getChildAt(0);
	        	CheckBox checkBox = (CheckBox) row.getChildAt(1);
				
	        	textView.setText(def.Label);
	        	//* checkBox.setText(data.Name);
				
	            tl.addView(row);				
			}
			else if (def.Type.equalsIgnoreCase("Date"))
			{
				row = (TableRow)LayoutInflater.from(getContext()).inflate(R.layout.date_row, null);
				
	        	TextView textView = (TextView) row.getChildAt(0);
	        	textView.setText(def.Label);
	        	
	            LinearLayout dateLayout = (LinearLayout) row.getChildAt(1);
	            
	            final EditText dateTimeEdit = (EditText) dateLayout.getChildAt(0);
	            final Date dtValue = (Date) data.get(def.Binding);
	            
	            dateTimeEdit.setHint(def.Prompt);
	        	if (def.Binding != null && data.get(def.Binding) != null)
	        	{	        		
	            	DateFormat dateFormat = android.text.format.DateFormat.getMediumDateFormat(getContext().getApplicationContext());
	            	//* SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

	        		dateTimeEdit.setText(dateFormat.format(data.get(def.Binding)));	        			
		        	_controls.put(def.Binding, dateTimeEdit);	        		
	        	}
	        	
	            dateTimeEdit.setOnEditorActionListener(new EditText.OnEditorActionListener() {
	                @Override
	                public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
	                    if (actionId == EditorInfo.IME_ACTION_DONE) {                    
	                        //* validateAndSubmit();
	                        return true;
	                    }
	                    
	                    if (actionId == EditorInfo.IME_ACTION_NEXT) {                    
	                        //* validateAndSubmit();
	                    	dateTimeEdit.requestFocus();
	                        return false;
	                    }
	                    
	                    dateTimeEdit.requestFocus();
	                    return false;
	                }
	            });
	            
	            final Button btn = (Button) dateLayout.getChildAt(1);
	            
	            //* btn.setOnClickListener(this);	            
	            btn.setOnClickListener(new Button.OnClickListener()
	            {
	            	@Override
	            	public void onClick(final View v)
	            	{
	                	// Process to get Current Date
	            		final Calendar c = Calendar.getInstance();
	            	    
	            	    c.setTime(dtValue);
	            			
	            	    int year = c.get(Calendar.YEAR);
	            	    int month = c.get(Calendar.MONTH);
	            	    int day = c.get(Calendar.DAY_OF_MONTH);

	            		// Launch Date Picker Dialog		
	            		DatePickerDialog dialog = new DatePickerDialog(v.getContext(), new DatePickerDialog.OnDateSetListener() {    	
	            	    	@Override
	            	    	public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
	            	    		LinearLayout dateLayout = (LinearLayout) v.getParent();
	            	    			            
	            	            final EditText dateTimeEdit = (EditText) dateLayout.getChildAt(0);
	            	            
	            	            //* DateFormat fmt = DateFormat.getDateInstance();
	            	            //* DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(v.getContext());
	            	            //* SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd HH:mm:ss");
	            	            
	            	            GregorianCalendar dt = new GregorianCalendar(selectedYear, selectedMonth, selectedDay);
	            	            
	            	            //* dateTimeEdit.setText(selectedDay + "-" + (selectedMonth + 1) + "-" + selectedYear);
	            	            dateTimeEdit.setText(_dateFormat.format(dt.getTime()));
	            	    	}
	            	    }, year, month, day);
	            		
	            		dialog.show();
	            	}
	            });
				
	            tl.addView(row);				
			}
		}
		
		tl.requestLayout();
	}
	
	public void InitX(PatientData data) {
		try
		{
			this.setContentView(R.layout.properties_dialog);
			
			// Get the TableLayout
	        TableLayout tl = (TableLayout) findViewById(R.id.maintable);
	        
	        for(int current = 0; current < 3; current++)
	        {	        	
	        	TableRow row = (TableRow)LayoutInflater.from(getContext()).inflate(R.layout.edit_row, null);
	        	
	        	TextView textView = (TextView) row.getChildAt(0);
	        	EditText editText = (EditText) row.getChildAt(1);
	        	
	        	textView.setText("Patient Name");
	        	//* editText.setText(data.Name);

	            //* ((TextView)row.findViewById(R.id.attrib_name)).setText(b.NAME);
	            //* ((TextView)row.findViewById(R.id.attrib_value)).setText(b.VALUE);
	            tl.addView(row);
	        	
	        	TableRow row1 = (TableRow)LayoutInflater.from(getContext()).inflate(R.layout.dropdown_row, null);
	        		        	
	            tl.addView(row1);
	            
	        	TableRow row2 = (TableRow)LayoutInflater.from(getContext()).inflate(R.layout.checkbox_row, null);
	        	
	            tl.addView(row2);
	            
	        	TableRow row3 = (TableRow)LayoutInflater.from(getContext()).inflate(R.layout.date_row, null);
	        	
	            tl.addView(row3);
	            
	            LinearLayout dateLayout = (LinearLayout) row3.getChildAt(1);
	            
	            final EditText dateTimeEdit = (EditText) dateLayout.getChildAt(0);
	            
	            dateTimeEdit.setOnEditorActionListener(new EditText.OnEditorActionListener() {
	                @Override
	                public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
	                    if (actionId == EditorInfo.IME_ACTION_DONE) {                    
	                        //* validateAndSubmit();
	                        return true;
	                    }
	                    
	                    if (actionId == EditorInfo.IME_ACTION_NEXT) {                    
	                        //* validateAndSubmit();
	                    	dateTimeEdit.requestFocus();
	                        return false;
	                    }
	                    
	                    dateTimeEdit.requestFocus();
	                    return false;
	                }
	            });
	            
	            final Button btn = (Button) dateLayout.getChildAt(1);
	            
	            btn.setOnClickListener(this);

	            // Create a TableRow and give it an ID
	            /*TableRow tr = new TableRow(getContext());
	            
	            //* tr.setId(R.id.maintable + 100 + current);            
	            tr.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	            
	            // Create a TextView to house the name of the province
	            TextView labelView = new TextView(getContext());
	            
	            //* labelView.setId(R.id.maintable + 200 + current);
	            labelView.setText("Label1");
	            labelView.setTextColor(Color.GREEN);
	            
	            labelView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, 1));
	            tr.addView(labelView);

	            // Create a TextView to house the value of the after-tax income
	            EditText editText = new EditText(getContext());
	            
	            //* editText.setId(current);
	            editText.setText("$0");
	            editText.setTextColor(Color.BLUE);
	            editText.setLayoutParams(new LayoutParams(6 , LayoutParams.WRAP_CONTENT));
	            
	            tr.addView(editText);
	            
	            //* tr.requestLayout();

	            // Add the TableRow to the TableLayout
	            tl.addView(tr, new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));*/			        
	        }
	        
	        tl.requestLayout();
		}
        catch(Exception ex) {
        	ex.printStackTrace();
        }
	}
        	
    @Override
    public void onClick(final View v)
    {
        InputMethodManager mImMan = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        mImMan.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

	@Override
	public void onTaskStart() 
	{
	}

	@Override
	public void onTaskProgress(int size) 
	{
		_progressDialog.setProgress(size);
	}

	@Override
	public void onTaskCompleted() 
	{		
		final Dialog dialog = this;
				
		_progressDialog.hide();
		_progressDialog.dismiss();

		dialog.dismiss();
	}
}
