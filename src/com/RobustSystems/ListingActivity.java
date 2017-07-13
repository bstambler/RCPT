package com.RobustSystems;

import com.RobustSystems.PatientData;
import com.RobustSystems.PalmDb;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Locale;

import android.content.Context;
import android.content.ContextWrapper;
import java.io.*;
import java.nio.channels.FileChannel;
import android.os.Environment;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.text.Editable;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.PopupMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;

import java.util.concurrent.Callable;
import android.os.Bundle;
import android.text.TextWatcher;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.PorterDuff;

import java.util.concurrent.CopyOnWriteArrayList;
import android.util.Log;

public class ListingActivity extends Activity implements OnScrollListener, ITaskListener {
	private String filename = "POCKeALL.pdb";
	private String filepath = "MyFileStorage";
	
	File _myInternalFile;
	File _myExternalFile;
	
	//* private ArrayList<String> _listItems = new ArrayList<String>();
	private PatientArrayAdapter _adapter;
	//* private SimpleAdapter _simpleAdapter;
	private View mFooterView;
	private ProgressDialog _progressDialog;
	
	private int _currentCount;
	
	//* private ArrayList<PatientData> _patients = new ArrayList<PatientData>();
	private static CopyOnWriteArrayList<PatientData> _patients = null;
	private static boolean _loaded = false;
	private static LoadTaskAsync _loader;
	private static boolean _loadCompleted = false;
	private boolean _inFilter = false;
	private Drawable _selectorDrawable;
	
	//* private ArrayAdapter<String> dataAdapter = null;
	
	public static PatientData[] GetPatientData()
	{
		int size = _patients.size();
		
		return _patients.toArray(new PatientData[size]);		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());

		//* displayListView();
		
		displayPatients(contextWrapper);
		
		//* addListenerOnButton();
	}
	
	public void onTaskStart()
	{
	}
	
	public void onTaskProgress(int counter)
	{
		try
		{
			ListView listView = (ListView) findViewById(R.id.listViewPatients);
			ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
		
			int size = _patients.size();
			
			if (_adapter == null)
			{
				//* _progressDialog = ProgressDialog.show(this, "Title", "Message");
				
				ArrayList<PatientData> list = new ArrayList<PatientData>();
				
				_adapter = new PatientArrayAdapter(this, R.layout.two_lines_list, R.id.line_a, R.id.line_b, list);
			
				List<PatientData> newlist = _patients.subList(_currentCount, size);
			
				for (PatientData d : newlist)
				{
					_adapter.add(d);
				}
			
				listView.setAdapter(_adapter);
				_adapter.notifyDataSetChanged();
				_currentCount = size;
				progressBar.setProgress(size);
			}
			else
			{
				if (size - _currentCount > _currentCount)
				{
					List<PatientData> newlist = _patients.subList(_currentCount, size);
					
					for (PatientData d : newlist)
					{
						_adapter.add(d);
					}
				
					//* _adapter.notifyDataSetChanged();
				
					_currentCount = size;
					
					progressBar.setProgress(size);
					_adapter.notifyDataSetChanged();
				}
			}
		}
		catch(Exception ex)
		{
			Log.d("Debug", ex.getMessage());
		}
	}
	
	public void onTaskCompleted()
	{
		try
		{
			int size = _patients.size();	
			List<PatientData> newlist = _patients.subList(_currentCount, size);
			
			for (PatientData d : newlist)
			{
				_adapter.add(d);
			}
			
			_adapter.notifyDataSetChanged();
			
			_currentCount = size;
			
			if (_progressDialog != null)
			{
				_progressDialog.dismiss();
			}
			
			ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
			
			progressBar.setVisibility(View.GONE);
			
			_loadCompleted = true;
			
			_loader.cancel(true);
			_loader = null;
		}
		catch(Exception ex)
		{
			Log.d("Debug", ex.getMessage());
		}
	}
	
	/*private void addListenerOnButton() {	 
		ImageView imageButton = (ImageView) findViewById(R.id.menuButton);
 
		imageButton.setOnClickListener(new OnClickListener() {
 
			@Override
			public void onClick(View v) {
			   Toast.makeText(getApplicationContext(), "ImageButton is clicked!", Toast.LENGTH_SHORT).show();
			}
		});
	}*/
	
	public void CopyStm(InputStream in, OutputStream out) throws IOException {
	    // Transfer bytes from in to out
	    byte[] buf = new byte[1024];
	    int len;
	    while ((len = in.read(buf)) > 0) {
	        out.write(buf, 0, len);
	    }
	}
	
	public void CopyFile(File src, File dst) throws IOException {
	    InputStream in = new FileInputStream(src);
	    OutputStream out = new FileOutputStream(dst);

	    // Transfer bytes from in to out
	    byte[] buf = new byte[1024];
	    int len;
	    while ((len = in.read(buf)) > 0) {
	        out.write(buf, 0, len);
	    }
	    
	    in.close();
	    out.close();
	}
	
	private void CopyFast(File src, File dst) throws IOException {
	    FileInputStream inStream = new FileInputStream(src);
	    FileOutputStream outStream = new FileOutputStream(dst);
	    
	    FileChannel inChannel = inStream.getChannel();
	    FileChannel outChannel = outStream.getChannel();
	    
	    inChannel.transferTo(0, inChannel.size(), outChannel);
	    
	    inStream.close();
	    outStream.close();
	}
	
	private void displayPatients(final ContextWrapper contextWrapper)
	{		
		AssetManager assetMgr = getAssets();
		
		if (_patients == null && !_loaded)
		{
			InputStream istm;
			
			//check if external storage is available and not read only  
			if (!Utils.isExternalStorageAvailable() || Utils.isExternalStorageReadOnly())
			{  
				File directory = contextWrapper.getDir(filepath, Context.MODE_PRIVATE);
				
				_myInternalFile = new File(directory , filename);
			} 
			else 
			{
			   _myExternalFile = new File(getExternalFilesDir(filepath), filename);
			   
				File directory = contextWrapper.getDir("Rcpt", Context.MODE_PRIVATE);
	
			   _myInternalFile = new File(directory , filename);
			}

			//* FileOutputStream stm = contextWrapper.openFileOutput(filename, MODE_PRIVATE);
			//* _myInternalFile = contextWrapper.getFileStreamPath(filename);
			
			if (!_myInternalFile.exists())
			{
				try
				{
					istm = assetMgr.open("POCKeALL.pdb");
					
					//* OutputStream ostm = getApplicationContext().openFileOutput("POCKeALL.pdb", Context.MODE_PRIVATE);
					OutputStream ostm = new FileOutputStream(_myInternalFile);
				
					CopyStm(istm, ostm);
					
					istm.close();
					ostm.close();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}			
		}
		
		//* File outputFile = new File(getExternalFilesDir(), "POCKeALL.pdb");
		
		//* OutputStream os = new FileOutputStream(outputFile);
		//* org.apache.commons.io.IOUtils.copy(istm, ostm);
		
		//* ilePath = getApplicationContext().getFilesDir().getAbsolutePath();
				
		//* setContentView(R.layout.activity_listing);
		setContentView(R.layout.filter_and_listing);
		
		final ListView listView = (ListView) findViewById(R.id.listViewPatients);
		
		listView.requestFocus();
		
		_selectorDrawable = listView.getSelector();
		
		if (_loadCompleted)
		{
			ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
			
			progressBar.setVisibility(View.GONE);			
			
			int size = _patients.size();
			ArrayList<PatientData> list = new ArrayList<PatientData>();
			
			_adapter = new PatientArrayAdapter(this, R.layout.two_lines_list, R.id.line_a, R.id.line_b, list);
		
			List<PatientData> newlist = _patients.subList(_currentCount, size);
		
			for (PatientData d : newlist)
			{
				_adapter.add(d);
			}
		
			listView.setAdapter(_adapter);
		}
		else
		{
			if (_patients == null)
			{
				_patients = new CopyOnWriteArrayList<PatientData>();
				
				_loader = new LoadTaskAsync(_myInternalFile.getPath(), _patients, this);
				
				_loader.execute();
				
				_loaded = true;
			}
			else
			{
				_loader.setListener(this);
				_currentCount = 0;
				//* _adapter = new PatientArrayAdapter(this, R.layout.two_lines_list, R.id.line_a, R.id.line_b, _patients.toArray(new PatientData[_currentCount]));			
				//* _adapter.notifyDataSetChanged();
			}				
		}
		
		final Context ctx = this;
		
		listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
            	//* listView.setSelection(position);
            	//* view.setSelected(true);
            	//* view.setBackgroundColor(Color.GREEN);
                //*** Toast.makeText(getApplicationContext(), "hello", Toast.LENGTH_LONG).show();
            	
            	if (_inFilter)
            	{
                	listView.requestFocus();
            		return;
            	}          	
            	
            	final CustomDialog dlg = new CustomDialog(ctx);
            	
            	PatientData data = (PatientData) parent.getItemAtPosition(position);
            	
            	dlg.Init(data, false, new IDialogResult<PatientData>() {

					@Override
					public void onSave(PatientData result) 
					{
						//* _patients.set(position, result);
						//_patients.get(position).Clone(result);
						_patients.set(position, result);
						
						int size = _patients.size();
						
						//* _adapter.notifyDataSetChanged();
						
						ArrayList<PatientData> list = new ArrayList<PatientData>();				
						List<PatientData> newlist = _patients.subList(0, size);
											
						//* ((PatientArrayAdapter) listView.getAdapter()).clear();
						//* ((PatientArrayAdapter) listView.getAdapter()).add(result);
						
						//* _adapter.clear();
						
						for (PatientData d : newlist)
						{
							list.add(d);
							//* _adapter.add(d);
						}
						
						_adapter = new PatientArrayAdapter(getApplicationContext(), R.layout.two_lines_list, R.id.line_a, R.id.line_b, list);						
						listView.setAdapter(_adapter);
						//* _adapter.notifyDataSetChanged();
						//* _currentCount = size;*/						
					}
            	});
              	            	
            	dlg.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            	
            	dlg.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            	
            	//* Drawable d = new ColorDrawable(Color.BLACK);
            	Drawable d = new ColorDrawable(Color.BLUE);
            	
            	//* d.setAlpha(130);
            	d.setAlpha(2);
            	
            	//* dlg.getWindow().setBackgroundDrawable(d);
            	
            	//* dlg.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            	dlg.setCancelable(true);
            	
            	dlg.setTitle("Enter Details");
            	
            	dlg.show();
            }
        });
		
		final ImageView btnAdd = (ImageView) findViewById(R.id.addButton);
		
        btnAdd.setOnClickListener(new ImageView.OnClickListener() {
			@Override
			public void onClick(View v) {								
				
            	if (_inFilter)
            	{
                	listView.requestFocus();
            		return;
            	}          	
            	
            	final CustomDialog dlg = new CustomDialog(ctx);
            	
				PatientData data = new PatientData();
				
				data.InitDefault();
            	
            	dlg.Init(data, true, new IDialogResult<PatientData>() {

					@Override
					public void onSave(PatientData result) 
					{
						//* _patients.set(position, result);
						//_patients.get(position).Clone(result);
						//* _patients.set(position, result);
						
						int size = _patients.size();
						
						//* _adapter.notifyDataSetChanged();
						
						ArrayList<PatientData> list = new ArrayList<PatientData>();				
						List<PatientData> newlist = _patients.subList(0, size);
											
						//* ((PatientArrayAdapter) listView.getAdapter()).clear();
						//* ((PatientArrayAdapter) listView.getAdapter()).add(result);
						
						//* _adapter.clear();
																		
						list.add(result);
						
						for (PatientData d : newlist)
						{
							list.add(d);
							//* _adapter.add(d);
						}						
						
						_currentCount = size + 1;
						
						_adapter = new PatientArrayAdapter(getApplicationContext(), R.layout.two_lines_list, R.id.line_a, R.id.line_b, list);						
						listView.setAdapter(_adapter);
						//* _adapter.notifyDataSetChanged();
						//* _currentCount = size;*/						
					}
            	});
              	            	
            	//* dlg.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            	dlg.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            	
            	dlg.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            	
            	//* Drawable d = new ColorDrawable(Color.BLACK);
            	Drawable d = new ColorDrawable(Color.BLUE);
            	
            	//* d.setAlpha(130);
            	d.setAlpha(2);
            	
            	// * dlg.getWindow().setBackgroundDrawable(d);
            	
            	//* dlg.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            	dlg.setCancelable(true);
            	
            	dlg.setTitle("Enter Details");
            	
            	dlg.show();
			}  
        });

				
		listView.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
	        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            	listView.setSelection(position);
            	view.setSelected(true);
            	view.setBackgroundColor(Color.GREEN);
	        }

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				//* listView.setSelection(-1);				
			}  
		});
		
		final EditText myFilter = (EditText) findViewById(R.id.myFilter);
		myFilter.addTextChangedListener(new TextWatcher() {
 
			public void afterTextChanged(Editable s) {
				String text = myFilter.getText().toString().toLowerCase(Locale.getDefault());
				//* _adapter.filter(text);
			}
 
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
 
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String text = s.toString();
				
				if (_adapter != null) {
					_adapter.getFilter().filter(text);	
				}
			}
		});
		
		myFilter.setOnFocusChangeListener(new View.OnFocusChangeListener() {
	        public void onFocusChange(View v, boolean hasFocus) {
	        	
	        	_inFilter = hasFocus;
	            if (hasFocus) {
	                /*myHandler.postAtFrontOfQueue(new Runnable() {
	                    public void run() {
	                        myList.setSelection(0);
	                    }
	                });*/
	            	
	            	listView.setSelector(new ColorDrawable(Color.TRANSPARENT));
	            }
	            else
	            {
	            	listView.setSelector(_selectorDrawable);
                    InputMethodManager mImMan = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    mImMan.hideSoftInputFromWindow(v.getWindowToken(), 0);
	            }
	        }
	    });
                
        final ImageView btn = (ImageView) findViewById(R.id.menuButton);
        
        //* btn.setOnCreateContextMenuListener(this);
                
        btn.setOnClickListener(new View.OnClickListener() {
			@Override  
            public void onClick(View v) {
				registerForContextMenu(v);
				openContextMenu(v);
				
				/*PopupMenu popup = new PopupMenu(getApplicationContext(), btn);
             
				popup.getMenuInflater().inflate(R.menu.options, popup.getMenu());  

				popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {  
					public boolean onMenuItemClick(MenuItem item) {  
						Toast.makeText(getApplicationContext(), "You Clicked : " + item.getTitle(),Toast.LENGTH_SHORT).show();  
						return true;  
					}  
				});  

				popup.show();*/
            }  
        });
        
        btn.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                    	ImageView view = (ImageView) v;
                        //overlay is black with transparency of 0x77 (119)
                        view.getDrawable().setColorFilter(0x770000FF, PorterDuff.Mode.SRC_ATOP);
                        view.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                    {
                    	registerForContextMenu(v);
        				openContextMenu(v);
        				ImageView view = (ImageView) v;
                        //clear the overlay
                        view.getDrawable().clearColorFilter();
                        view.invalidate();
        				break;
                    }
                    	
                    case MotionEvent.ACTION_CANCEL: {
                    	ImageView view = (ImageView) v;
                        //clear the overlay
                        view.getDrawable().clearColorFilter();
                        view.invalidate();
                        break;
                    }
                }

                return true;
            }
        });
        
        //* listView.setOnScrollListener(this);
	}	
	
	final int CONTEXT_MENU_VIEW = 1;
	final int CONTEXT_MENU_EDIT = 2;
	final int CONTEXT_MENU_ARCHIVE = 3;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    
	    inflater.inflate(R.menu.options, menu);
	    
    	final CustomDialog dlg = new CustomDialog(this);
    	
    	//* dlg.Init();
    	
    	dlg.show();
	    
	    return true;
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) 
	{
		//* Toast.makeText(getApplicationContext(), "You Clicked : " + item.getTitle(),Toast.LENGTH_SHORT).show();
		
		switch(item.getItemId())
	    {
	    	case CONTEXT_MENU_VIEW:
	    	{
	    	}
	    	break;
	    	
	    	case CONTEXT_MENU_EDIT:
	    	{
	    		// Edit Action
	    	}
	    	break;
	    	
	    	case CONTEXT_MENU_ARCHIVE:
	    	{

	    	}
	    	break;
	    }

	    return super.onContextItemSelected(item);
	}
	
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo)
    {
		super.onCreateContextMenu(menu, view, menuInfo);
		
		MenuInflater inflater = getMenuInflater();
		
	    inflater.inflate(R.menu.options, menu);
    }
	
	/*public void addItemsOnMySort() {		 
		Spinner mySpinner = (Spinner) findViewById(R.id.mySort);
		List<String> list = new ArrayList<String>();
		
		list.add("Sort by name");
		list.add("Sort by creation date");
		list.add("Sort by name descending");
		list.add("Sort by creation date descending");
		
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
			android.R.layout.simple_spinner_item, list);
		
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mySpinner.setAdapter(dataAdapter);
	}*/
	
	/*private void showPopupMenu(View v) {
		   PopupMenu popupMenu = new PopupMenu(AndroidPopupMenuActivity.this, v);
		      popupMenu.getMenuInflater().inflate(R.menu.popupmenu, popupMenu.getMenu());
		    
		      popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
		   
		   @Override
		   public boolean onMenuItemClick(MenuItem item) {
		    Toast.makeText(AndroidPopupMenuActivity.this,
		      item.toString(),
		      Toast.LENGTH_LONG).show();
		    return true;
		   }
		  });
		    
		      popupMenu.show();
		  }*/

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {		
	}

}
