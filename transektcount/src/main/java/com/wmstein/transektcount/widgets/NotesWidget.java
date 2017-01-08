package com.wmstein.transektcount.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wmstein.transektcount.R;

/****************************************************
 * Created by milo on 26/05/2014.
 * Adopted for TransektCount by wmstein on 18.02.2016
 */
public class NotesWidget extends LinearLayout
{
    public static String TAG = "TransektCount Notes Widget";
    public String section_notes;
    private TextView textView;

    public NotesWidget(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.widget_notes, this, true);
        textView = (TextView) findViewById(R.id.notes_text);
    }

    public void setNotes(String notes)
    {
        section_notes = notes;
        textView.setText(section_notes);
    }

    public void setFont(Boolean large)
    {
        if (large)
        {
            //Log.i(TAG, "Setzt große Schrift.");
            textView.setTextSize(14);
        }
        else
        {
            //Log.i(TAG, "Setzt kleine Schrift.");
            textView.setTextSize(12);
        }
    }

}

