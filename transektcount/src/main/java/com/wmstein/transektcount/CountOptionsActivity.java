package com.wmstein.transektcount;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.wmstein.transektcount.database.Alert;
import com.wmstein.transektcount.database.AlertDataSource;
import com.wmstein.transektcount.database.Count;
import com.wmstein.transektcount.database.CountDataSource;
import com.wmstein.transektcount.widgets.AddAlertWidget;
import com.wmstein.transektcount.widgets.AlertCreateWidget;
import com.wmstein.transektcount.widgets.EditTitleWidget;
import com.wmstein.transektcount.widgets.OptionsWidget;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by milo on 05/05/2014.
 * Changed by wmstein on 18.02.2016
 * Supplemented with functions for transect externel counter
 */

public class CountOptionsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private static String TAG = "transektcountCountOptionsActivity";
    TransektCountApplication transektCount;
    SharedPreferences prefs;

    private Count count;
    private int count_id;
    private List<Alert> alerts;
    private CountDataSource countDataSource;
    private AlertDataSource alertDataSource;
    private AlertDialog.Builder areYouSure;
    private View markedForDelete;
    private int deleteAnAlert;
    private int section_id;

    private Bitmap bMap;
    private BitmapDrawable bg;

    // preferences
    private boolean brightPref;

    LinearLayout static_widget_area;
    LinearLayout dynamic_widget_area;
    OptionsWidget curr_val_widget;
    EditTitleWidget enw;
    AddAlertWidget aa_widget;

    ArrayList<AlertCreateWidget> savedAlerts;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count_options);

        transektCount = (TransektCountApplication) getApplication();
        prefs = com.wmstein.transektcount.TransektCountApplication.getPrefs();
        prefs.registerOnSharedPreferenceChangeListener(this);
        brightPref = prefs.getBoolean("pref_bright", true);

        // Set full brightness of screen
        if (brightPref)
        {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.screenBrightness = 1.0f;
            getWindow().setAttributes(params);
        }

        ScrollView counting_screen = (ScrollView) findViewById(R.id.count_options);
        bMap = transektCount.decodeBitmap(R.drawable.kbackground, transektCount.width, transektCount.height);
        bg = new BitmapDrawable(counting_screen.getResources(), bMap);
        counting_screen.setBackground(bg);

        static_widget_area = (LinearLayout) findViewById(R.id.static_widget_area);
        dynamic_widget_area = (LinearLayout) findViewById(R.id.dynamic_widget_area);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            count_id = extras.getInt("count_id");
            section_id = extras.getInt("section_id");
        }

        savedAlerts = new ArrayList<>();
        if (savedInstanceState != null)
        {
            if (savedInstanceState.getSerializable("savedAlerts") != null)
            {
                savedAlerts = (ArrayList<AlertCreateWidget>) savedInstanceState.getSerializable("savedAlerts");
            }
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // clear any existing views
        static_widget_area.removeAllViews();
        dynamic_widget_area.removeAllViews();

        // get the data sources
        countDataSource = new CountDataSource(this);
        countDataSource.open();
        alertDataSource = new AlertDataSource(this);
        alertDataSource.open();

        count = countDataSource.getCountById(count_id);
        getSupportActionBar().setTitle(count.name);

        List<Alert> alerts = alertDataSource.getAllAlertsForCount(count_id);

        // setup the static widgets in the following order
        // 1. Current count value (internal counter)
        // 2. Current counta value (external counter)
        // 3. Alert add/remove
        curr_val_widget = new OptionsWidget(this, null);
        curr_val_widget.setInstructions(String.format(getString(R.string.editCountValue), count.name, count.count));
        curr_val_widget.setInstructionsa(String.format(getString(R.string.editCountValuea), count.name, count.counta));
        curr_val_widget.setParameterValue(count.count);
        curr_val_widget.setParameterValuea(count.counta);
        static_widget_area.addView(curr_val_widget);

        enw = new EditTitleWidget(this, null);
        enw.setSectionName(count.notes);
        enw.setWidgetTitle(getString(R.string.notesSpecies));
        enw.setHint(getString(R.string.notesHint));
        enw.requestFocus();

        static_widget_area.addView(enw);

        aa_widget = new AddAlertWidget(this, null);
        static_widget_area.addView(aa_widget);

    /*
     * There should be a method to add all counts in order to re-draw when one is deleted.
     */
        for (Alert alert : alerts)
        {
            AlertCreateWidget acw = new AlertCreateWidget(this, null);
            acw.setAlertName(alert.alert_text);
            acw.setAlertValue(alert.alert);
            acw.setAlertId(alert.id);
            dynamic_widget_area.addView(acw);
        }

    /*
     * Add saved alert create widgets
     */
        for (AlertCreateWidget acw : savedAlerts)
        {
            dynamic_widget_area.addView(acw);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
    /*
     * Before these widgets can be serialised they must be removed from their parent, or else
     * trying to add them to a new parent causes a crash because they've already got one.
     */
        super.onSaveInstanceState(outState);
        for (AlertCreateWidget acw : savedAlerts)
        {
            ((ViewGroup) acw.getParent()).removeView(acw);
        }
        outState.putSerializable("savedAlerts", savedAlerts);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        // finally, close the database
        countDataSource.close();
        alertDataSource.close();

    }

    public void saveAndExit(View view)
    {
        saveData();
        savedAlerts.clear();
        super.finish();
    }

    public void saveData()
    {
        // don't crash if the user hasn't filled things in...
        Toast.makeText(CountOptionsActivity.this, getString(R.string.sectSaving) + " " + count.name + "!", Toast.LENGTH_SHORT).show();
        count.counta = curr_val_widget.getParameterValuea();
        count.count = curr_val_widget.getParameterValue();
        count.notes = enw.getSectionName();

        countDataSource.saveCount(count);

    /*
     * Get all the alerts from the dynamic_widget_area and save each one.
     * If it has an id value set to anything higher than 0 then it should be an update, if it is 0
     * then it's a new alert and should be created instead.
     */
        int childcount = dynamic_widget_area.getChildCount();
        for (int i = 0; i < childcount; i++)
        {
            AlertCreateWidget acw = (AlertCreateWidget) dynamic_widget_area.getChildAt(i);
            if (StringUtils.isNotEmpty(acw.getAlertName()))
            {
                // save or create
                if (acw.getAlertId() == 0)
                {
                    alertDataSource.createAlert(count_id, acw.getAlertValue(), acw.getAlertName());
                }
                else
                {
                    alertDataSource.saveAlert(acw.getAlertId(), acw.getAlertValue(), acw.getAlertName());
                }
            }
            else
            {
                Log.i(TAG, "Failed to save alert: " + acw.getAlertId());
            }
        }
    }

    /*
     * Scroll to end of view
     * by wmstein
     */
    public void ScrollToEndOfView(View scrlV)
    {
        int scroll_amount = scrlV.getBottom();
        int scrollY = scroll_amount;
        boolean pageend = false;
        while (!pageend)
        {
            scrlV.scrollTo(0, scroll_amount);              //scrollen
            scroll_amount = scroll_amount + scroll_amount; //scroll_amount erhöhen
            scrollY = scrollY + scrlV.getScrollY();        //scroll-Position der 1. Zeile
            if (scroll_amount > scrollY)
            {
                pageend = true;
            }
        }
    }

    public void addAnAlert(View view)
    {
        AlertCreateWidget acw = new AlertCreateWidget(this, null);
        savedAlerts.add(acw);
        // Scroll to end of view
        View scrollV = findViewById(R.id.count_options);
        ScrollToEndOfView(scrollV);
        acw.requestFocus();
        dynamic_widget_area.addView(acw);
    }

    public void deleteWidget(View view)
    {
    /*
     * These global variables keep a track of the view containing an alert to be deleted and also the id
     * of the alert itself, to make sure that they're available inside the code for the alert dialog by
     * which they will be deleted.
     */
        markedForDelete = view;
        deleteAnAlert = (Integer) view.getTag();
        if (deleteAnAlert == 0)
        {
            //Log.i(TAG, "(1) View tag was " + String.valueOf(deleteAnAlert));
            // the actual AlertCreateWidget is two levels up from the button in which it is embedded
            dynamic_widget_area.removeView((AlertCreateWidget) view.getParent().getParent());
        }
        else
        {
            //Log.i(TAG, "(2) View tag was " + String.valueOf(deleteAnAlert));
            // before removing this widget it is necessary to do the following:
            // (1) Check the user is sure they want to delete it and, if so...
            // (2) Delete the associated alert from the database.
            areYouSure = new AlertDialog.Builder(this);
            areYouSure.setTitle(getString(R.string.deleteAlert));
            areYouSure.setMessage(getString(R.string.reallyDeleteAlert));
            areYouSure.setPositiveButton(R.string.yesDeleteIt, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int whichButton)
                {
                    // go ahead for the delete
                    try
                    {
                        alertDataSource.deleteAlertById(deleteAnAlert);
                        dynamic_widget_area.removeView((AlertCreateWidget) markedForDelete.getParent().getParent());
                    } catch (Exception e)
                    {
                        Log.i(TAG, "Failed to delete a widget: " + e.toString());
                    }
                }
            });
            areYouSure.setNegativeButton(R.string.noCancel, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int whichButton)
                {
                    // Cancelled.
                }
            });
            areYouSure.show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.count_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            startActivity(new Intent(this, SettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }
        else if (id == R.id.home)
        {
            Intent intent = NavUtils.getParentActivityIntent(this);
            intent.putExtra("section_id", section_id);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            NavUtils.navigateUpTo(this, intent);
        }
        else if (id == R.id.menuSaveExit)
        {
            saveData();
            super.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
    {
        ScrollView counting_screen = (ScrollView) findViewById(R.id.count_options);
        counting_screen.setBackground(null);
        bMap = transektCount.decodeBitmap(R.drawable.kbackground, transektCount.width, transektCount.height);
        bg = new BitmapDrawable(counting_screen.getResources(), bMap);
        counting_screen.setBackground(bg);
    }

}
