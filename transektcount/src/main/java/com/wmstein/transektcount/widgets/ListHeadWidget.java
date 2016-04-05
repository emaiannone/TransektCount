package com.wmstein.transektcount.widgets;

/*
 * EditHeadWidget.java used by EditMetaActivity.java
 * Created by wmstein for com.wmstein.transektcount on 03.04.2016
 */

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wmstein.transektcount.R;

public class ListHeadWidget extends LinearLayout
{
    TextView widget_lno; // used for transect_no title
    TextView widget_lno1; // used for transect_no
    TextView widget_lname; // used for inspector_name title
    TextView widget_lname1; // used for inspector_name

    public ListHeadWidget(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.widget_list_head, this, true);
        widget_lno = (TextView) findViewById(R.id.widgetLNo);
        widget_lno1 = (TextView) findViewById(R.id.widgetLNo1);
        widget_lname = (TextView) findViewById(R.id.widgetLName);
        widget_lname1 = (TextView) findViewById(R.id.widgetLName1);
    }

    public void setWidgetLNo(String title)
    {
        widget_lno.setText(title);
    }

    public void setWidgetLNo1(String name)
    {
        widget_lno1.setText(name);
    }

    public void setWidgetLName(String title)
    {
        widget_lname.setText(title);
    }

    public void setWidgetLName1(String name)
    {
        widget_lname1.setText(name);
    }

}