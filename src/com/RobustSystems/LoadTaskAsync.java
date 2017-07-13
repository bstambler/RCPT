package com.RobustSystems;

import java.net.URL;
import java.util.Collection;
import java.util.concurrent.Callable;

import android.os.AsyncTask;

public class LoadTaskAsync extends AsyncTask<String, Integer, Long> implements ITaskCanceller {
	
	private String _path;
	private Collection<PatientData> _coll;
	private ITaskListener _listener;
	
	public LoadTaskAsync(String strPath, Collection<PatientData> coll, ITaskListener listener)
	{
		_path = strPath;
		_coll = coll;
		_listener = listener;
	}
	
	public void setListener(ITaskListener listener)
	{
		_listener = listener;
	}
	
	public boolean isTaskCancelled()
	{
		return isCancelled();
	}
	
	public void onTaskProgress(int progress)
	{
		publishProgress(progress);
	}
	
    protected Long doInBackground(String... params)
    {
    	if (this.isCancelled())
    	{
    		return (long) -1;
    	}
    	
    	_listener.onTaskStart();
    	
    	boolean result = PalmDb.LoadData(_path, _coll, this);
    	
    	if (result)
    	{
    		return (long) _coll.size();
    	}
		        
        return (long) -1;
    }

    // This is called each time you call publishProgress()
    protected void onProgressUpdate(Integer... progress) {
        //* setProgressPercent(progress[0]);
    	_listener.onTaskProgress(progress[0]);
    }

    // This is called when doInBackground() is finished
    protected void onPostExecute(Long result) {
        //* showNotification("Downloaded " + result + " bytes");
    	_listener.onTaskCompleted();
    }
}