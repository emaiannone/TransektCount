package com.wmstein.transektcount.widgets;

/*
 * EditMetaWidget.java used by EditMetaActivity.java
 */

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wmstein.transektcount.R;

import org.apache.commons.lang3.StringUtils;

/*
 * Created by wmstein for com.wmstein.transektcount on 02.04.2016.
 */
public class EditMetaWidget extends LinearLayout
{
    TextView widget_meta1; // temperature
    EditText widget_item1;
    TextView widget_meta2; // wind
    EditText widget_item2;
    TextView widget_meta3; // clouds
    EditText widget_item3;
    TextView widget_date1; // date
    TextView widget_date2;
    TextView widget_time1; // start_tm
    TextView widget_item4;
    TextView widget_time2; // end_tm
    TextView widget_item5;

    public EditMetaWidget(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.widget_edit_meta, this, true);
        widget_meta1 = (TextView) findViewById(R.id.widgetMeta1);
        widget_item1 = (EditText) findViewById(R.id.widgetItem1);
        widget_meta2 = (TextView) findViewById(R.id.widgetMeta2);
        widget_item2 = (EditText) findViewById(R.id.widgetItem2);
        widget_meta3 = (TextView) findViewById(R.id.widgetMeta3);
        widget_item3 = (EditText) findViewById(R.id.widgetItem3);
        widget_date1 = (TextView) findViewById(R.id.widgetDate1);
        widget_date2 = (TextView) findViewById(R.id.widgetDate2);
        widget_time1 = (TextView) findViewById(R.id.widgetTime1);
        widget_item4 = (TextView) findViewById(R.id.widgetItem4);
        widget_time2 = (TextView) findViewById(R.id.widgetTime2);
        widget_item5 = (TextView) findViewById(R.id.widgetItem5);
    }

    // Following the SETS
    // temperature
    public void setWidgetMeta1(String title)
    {
        widget_meta1.setText(title);
    }

    public void setWidgetItem1(int name)
    {
        widget_item1.setText(String.valueOf(name));
    }

    // wind
    public void setWidgetMeta2(String title)
    {
        widget_meta2.setText(title);
    }

    public void setWidgetItem2(int name)
    {
        widget_item2.setText(String.valueOf(name));
    }

    // clouds
    public void setWidgetMeta3(String title)
    {
        widget_meta3.setText(title);
    }

    public void setWidgetItem3(int name)
    {
        widget_item3.setText(String.valueOf(name));
    }

    // date
    public void setWidgetDate1(String title)
    {
        widget_date1.setText(title);
    }

    public void setWidgetDate2(String name)
    {
        widget_date2.setText(name);
    }

    // start_tm
    public void setWidgetTime1(String title)
    {
        widget_time1.setText(title);
    }

    public void setWidgetItem4(String name)
    {
        widget_item4.setText(name);
    }

    // end_tm
    public void setWidgetTime2(String title)
    {
        widget_time2.setText(title);
    }

    public void setWidgetItem5(String name)
    {
        widget_item5.setText(name);
    }

    // following the GETS
    public int getWidgetItem1()
    {
        String text = widget_item1.getText().toString();
        if (StringUtils.isEmpty(text))
            return 0;
        else
            return Integer.parseInt(text);
    }

    public int getWidgetItem2()
    {
        String text = widget_item2.getText().toString();
        if (StringUtils.isEmpty(text))
            return 0;
        else
            return Integer.parseInt(text);
    }

    public int getWidgetItem3()
    {
        String text = widget_item3.getText().toString();
        if (StringUtils.isEmpty(text))
            return 0;
        else
            return Integer.parseInt(text);
    }

    public String getWidgetDate2()
    {
        return widget_date2.getText().toString();
    }

    public String getWidgetItem4()
    {
        return widget_item4.getText().toString();
    }

    public String getWidgetItem5()
    {
        return widget_item5.getText().toString();
    }

    // Following the HINTS
    public void setHint(String hint)
    {
        widget_meta1.setHint(hint);
    }
    
}