package com.RobustSystems;

import android.content.Context;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class PatientArrayAdapter extends TwoLineArrayAdapter<PatientData> 
{
    public PatientArrayAdapter(Context context, int listItemLayoutResourceId, int txt1ResId, int txt2ResId, PatientData[] patientData) {
        super(context, listItemLayoutResourceId, txt1ResId, txt2ResId, patientData);
    }
    
    public PatientArrayAdapter(Context context, int listItemLayoutResourceId, int txt1ResId, int txt2ResId, ArrayList<PatientData> patientData) {
        super(context, listItemLayoutResourceId, txt1ResId, txt2ResId, patientData);
    } 

    @Override
    public String lineOneText(PatientData patientData) {
        return patientData.getPatientName();
    }

    @Override
    public String lineTwoText(PatientData patientData) {
    	DateFormat dateFormat = android.text.format.DateFormat.getMediumDateFormat(getContext().getApplicationContext());
    	//* SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
    	
    	return dateFormat.format(patientData.getCreateDate());
    }
}
