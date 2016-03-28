package com.wmstein.transektcount;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.wmstein.transektcount.database.AlertDataSource;
import com.wmstein.transektcount.database.Count;
import com.wmstein.transektcount.database.CountDataSource;
import com.wmstein.transektcount.database.Section;
import com.wmstein.transektcount.database.SectionDataSource;
import com.wmstein.transektcount.widgets.CountEditWidget;
import com.wmstein.transektcount.widgets.EditTitleWidget;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by milo on 05/05/2014.
 * Changed by wmstein on 18.02.2016
 */

/***********************************************************************************************************************/
// Edit section list, module is called from CountingActivity 
public class EditSectionActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
    TransektCountApplication transektCount;
    SharedPreferences prefs;
    public static String TAG = "TransektCountEditSectionActivity";

    // the actual data
    Section section;
    List<Count> counts;

    private SectionDataSource sectionDataSource;
    private CountDataSource countDataSource;
    private AlertDataSource alertDataSource;

    int section_id;
    LinearLayout counts_area;
    LinearLayout notes_area;
    EditTitleWidget etw;
    EditTitleWidget enw;
    private View markedForDelete;
    private int idToDelete;
    private AlertDialog.Builder areYouSure;
    public ArrayList<String> countNames;
    public ArrayList<Integer> countIds;
    public ArrayList<CountEditWidget> savedCounts;

    //added for dupPref ToDo
    private boolean dupPref;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_section);

        countNames = new ArrayList<>();
        countIds = new ArrayList<>();
        savedCounts = new ArrayList<>();

        notes_area = (LinearLayout) findViewById(R.id.editingNotesLayout);
        counts_area = (LinearLayout) findViewById(R.id.editingCountsLayout);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            section_id = extras.getInt("section_id");
        }

        /*
         * Restore any edit widgets the user has added previously
         */
        if (savedInstanceState != null)
        {
            if (savedInstanceState.getSerializable("savedCounts") != null)
            {
                savedCounts = (ArrayList<CountEditWidget>) savedInstanceState.getSerializable("savedCounts");
            }
        }

        transektCount = (TransektCountApplication) getApplication();
        //section_id = transektCount.section_id;
        prefs = TransektCountApplication.getPrefs();
        prefs.registerOnSharedPreferenceChangeListener(this);

        //added for dupPref
        dupPref = prefs.getBoolean("pref_duplicate", true);

        ScrollView counting_screen = (ScrollView) findViewById(R.id.editingScreen);
        counting_screen.setBackground(transektCount.getBackground());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
    /*
     * Before these widgets can be serialised they must be removed from their parent, or else
     * trying to add them to a new parent causes a crash because they've already got one.
     */
        super.onSaveInstanceState(outState);
        for (CountEditWidget cew : savedCounts)
        {
            ((ViewGroup) cew.getParent()).removeView(cew);
        }
        outState.putSerializable("savedCounts", savedCounts);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // clear any existing views
        counts_area.removeAllViews();
        notes_area.removeAllViews();

        // setup the data sources
        sectionDataSource = new SectionDataSource(this);
        sectionDataSource.open();
        countDataSource = new CountDataSource(this);
        countDataSource.open();
        alertDataSource = new AlertDataSource(this);
        alertDataSource.open();

        // load the sections data
        section = sectionDataSource.getSection(section_id);
        getSupportActionBar().setTitle(section.name);

        // display an editable section title
        etw = new EditTitleWidget(this, null);
        etw.setSectionName(section.name);
        etw.setWidgetTitle(getString(R.string.titleEdit));
        notes_area.addView(etw);

        // display editable section notes; the same class
        // is being used for both due to being lazy
        enw = new EditTitleWidget(this, null);
        enw.setSectionName(section.notes);
        enw.setWidgetTitle(getString(R.string.notesHere));
        enw.setHint(getString(R.string.notesHint));
        enw.requestFocus();
        notes_area.addView(enw);

        // load the counts data
        counts = countDataSource.getAllCountsForSection(section.id);

        // display all the counts by adding them to countCountLayout
        for (Count count : counts)
        {
            // widget
            CountEditWidget cew = new CountEditWidget(this, null);
            cew.setCountName(count.name);
            cew.setCountId(count.id);
            counts_area.addView(cew);
        }
        for (CountEditWidget cew : savedCounts)
        {
            counts_area.addView(cew);
        }
        getCountNames();
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        // close the data sources
        sectionDataSource.close();
        countDataSource.close();
        alertDataSource.close();

    }

    public void getCountNames()
    {
    /*
     * My plan here is that both the names and ids arrays contain the entries in the same
     * order, so I can link a count name to its id by knowing the index.
     */
        countNames.clear();
        countIds.clear();
        int childcount = counts_area.getChildCount();
        for (int i = 0; i < childcount; i++)
        {
            CountEditWidget cew = (CountEditWidget) counts_area.getChildAt(i);
            String name = cew.getCountName();
            // ignore count widgets where the user has filled nothing in. Id will be 0
            // if this is a new count.
            if (StringUtils.isNotEmpty(name))
            {
                countNames.add(name);
                countIds.add(cew.countId);
            }
        }
    }

    public void saveAndExit(View view)
    {
        if (saveData())
            savedCounts.clear();
        super.finish();
    }

    public boolean saveData()
    {
        //added for dupPref ToDo
        String count_name;

        // save title and notes only if they have changed
        boolean savesection = false;
        String newtitle = etw.getSectionName();

        if (StringUtils.isNotEmpty(newtitle))
        {
            section.name = newtitle;
            savesection = true;
        }
        String newnotes = enw.getSectionName();
        // Always add notes if the user has written some...
        if (StringUtils.isNotEmpty(newnotes))
        {
            section.notes = newnotes;
            savesection = true;
        }
        //...if they haven't, only save if the current notes have a value (i.e.
        else
        {
            if (StringUtils.isNotEmpty(section.notes))
            {
                section.notes = newnotes;
                savesection = true;
            }
        }
        if (savesection)
        {
            sectionDataSource.saveSection(section);
        }

        // save counts
        boolean retValue = false; //---------- <- this sort of comment shows: added for dupPref

        int childcount;
        childcount = counts_area.getChildCount(); //No. of species in list

        for (int i = 0; i < childcount; i++)
        {
            Log.i(TAG, "Childcount: " + String.valueOf(childcount));
            CountEditWidget cew = (CountEditWidget) counts_area.getChildAt(i);
            if (StringUtils.isNotEmpty(cew.getCountName()))
            {
                Log.i(TAG, "cew: " + String.valueOf(cew.countId) + ", " + cew.getCountName());
/********
                // check for unique names ----------
                if (dupPref) //----------
                { //----------
                    //getCountNames(); // refreshes countNames??? ----------
                    for (CountEditWidget c : savedCounts) //----------
                    { //-----
                        count_name = c.getCountName(); //----------
                        Log.i(TAG, "count_name = " + count_name); //----------
                        if (countNames.contains(count_name)) //----------
                        { //----------
                            retValue = false; //----------
                            Log.i(TAG, "found duplicate"); //----------
                        } //----------
                        else //----------
                        { //----------
********/
                            // create or save
                            if (cew.countId == 0)
                            {
                                Log.i(TAG, "Creating!");
                                //returns newCount
                                countDataSource.createCount(section_id, cew.getCountName());
                            }
                            else
                            {
                                Log.i(TAG, "Updating!");
                                countDataSource.updateCountName(cew.countId, cew.getCountName());
                            }
/*********
                            retValue = true; //----------
                        } //----------
                    } //----------
                } //----------
***********/
            }
        }
/**********
        if (retValue) //----------
        { //----------

            Toast.makeText(EditSectionActivity.this, getString(R.string.sectSaving) + " " + section.name + "!", Toast.LENGTH_SHORT).show(); //----------

        } //----------
        else //----------
        { //----------
            Toast.makeText(this, getString(R.string.duplicate), Toast.LENGTH_SHORT).show(); //----------
        } //----------
        return retValue; //----------
 *********/
    Toast.makeText(EditSectionActivity.this, getString(R.string.sectSaving) + " " + section.name + "!", Toast.LENGTH_SHORT).show();
    return true;        
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
        while(!pageend)
        {
            scrlV.scrollTo(0, scroll_amount);            //scroll
            scroll_amount=scroll_amount + scroll_amount; //increase scroll_amount 
            scrollY = scrollY + scrlV.getScrollY();      //scroll position 1. row
            if (scroll_amount > scrollY)
            {
                pageend = true;
            }
        }
    }        
    
    public void newCount(View view)
    {
        CountEditWidget cew = new CountEditWidget(this, null);
        counts_area.addView(cew); // adds a child view cew
        // Scroll to end of view, added by wmstein
        View scrollV = findViewById(R.id.editingScreen);
        ScrollToEndOfView(scrollV);
        cew.requestFocus();       // set focus to cew added by wmstein

        savedCounts.add(cew);
    }

  /*
   * These are required for purging counts (with associated alerts)
   */
    public void deleteCount(View view)
    {
    /*
     * These global variables keep a track of the view containing an alert to be deleted and also the id
     * of the alert itself, to make sure that they're available inside the code for the alert dialog by
     * which they will be deleted.
     */
        markedForDelete = view;
        idToDelete = (Integer) view.getTag();
        if (idToDelete == 0)
        {
            // the actual CountEditWidget is two levels up from the button in which it is embedded
            counts_area.removeView((CountEditWidget) view.getParent().getParent());
        }
        else
        {
            //Log.i(TAG, "(2) View tag was " + String.valueOf(deleteAnAlert));
            // before removing this widget it is necessary to do the following:
            // (1) Check the user is sure they want to delete it and, if so...
            // (2) Delete the associated alert from the database.
            areYouSure = new AlertDialog.Builder(this);
            areYouSure.setTitle(getString(R.string.deleteCount));
            areYouSure.setMessage(getString(R.string.reallyDeleteCount));
            areYouSure.setPositiveButton(R.string.yesDeleteIt, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int whichButton)
                {
                    // go ahead for the delete
                    countDataSource.deleteCountById(idToDelete);
                    counts_area.removeView((CountEditWidget) markedForDelete.getParent().getParent());
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
        getCountNames();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_section, menu);
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
            if (saveData())
                super.finish();
        }
        else if (id == R.id.newCount)
        {
            newCount(findViewById(R.id.editingCountsLayout));
        }
        return super.onOptionsItemSelected(item);
    }

    public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
    {
        ScrollView counting_screen = (ScrollView) findViewById(R.id.editingScreen);
        counting_screen.setBackground(null);
        counting_screen.setBackground(transektCount.setBackground());

        //added for dupPref
        dupPref = prefs.getBoolean("duplicate_counts", true);
    }
}
