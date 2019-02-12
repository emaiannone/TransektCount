/*
 * Copyright (c) 2016. Wilhelm Stein, Bonn, Germany.
 */

package com.wmstein.transektcount.widgets;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wmstein.transektcount.R;
import com.wmstein.transektcount.database.Count;

/****************************************************
 * Interface for widget_counting_head1.xml
 * Created by wmstein 18.12.2016
 * Last edited on 2019-02-12
 */
public class CountingWidget_head1 extends ArrayAdapter<String>
{
    public static String TAG = "transektcountCountingWidget_head1";

    private String[] idArray;
    private String[] contentArray1;
    private String[] contentArray2;
    private Integer[] imageArray;

    public Count count;
    LayoutInflater inflater;

    public CountingWidget_head1(Context context, int resource, String[] idArray, String[] nameArray, String[] codeArray, Integer[] imageArray)
    {
        super(context, resource, R.id.countName, nameArray);
        this.idArray = idArray;
        this.contentArray1 = nameArray;
        this.contentArray2 = codeArray;
        this.imageArray = imageArray;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent)
    {
        return getCustomView(position, parent);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent)
    {
        return getCustomView(position, parent);
    }

    private View getCustomView(int position, ViewGroup parent)
    {
        View row = inflater.inflate(R.layout.widget_counting_head1, parent, false);

        TextView countId = row.findViewById(R.id.countId);
        countId.setText(idArray[position]);

        TextView countName = row.findViewById(R.id.countName);
        countName.setText(contentArray1[position]);

        TextView countCode = row.findViewById(R.id.countCode);
        countCode.setText(contentArray2[position]);

        ImageView pSpecies = row.findViewById(R.id.pSpecies);
        pSpecies.setImageResource(imageArray[position]);

        return row;
    }

}
