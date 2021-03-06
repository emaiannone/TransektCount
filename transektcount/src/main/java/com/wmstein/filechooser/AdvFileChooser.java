package com.wmstein.filechooser;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.wmstein.transektcount.R;

import java.io.File;
import java.io.FileFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AdvFileChooser lets you select files from user's basic directory.
 * It will be called within WelcomeActivity and uses FileArrayAdapter and Option.
 * Based on android-file-chooser, 2011, Google Code Archiv, GNU GPL v3.
 * Adopted by wmstein on 2016-06-18,
 * last change on 2018-04-05
 */

public class AdvFileChooser extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private File currentDir;
    private FileArrayAdapter adapter;
    private FileFilter fileFilter;
    private ArrayList<String> extensions;
    private String filterFileName;
    private boolean screenOrientL; // option for screen orientation

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_view);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs.registerOnSharedPreferenceChangeListener(this);
        screenOrientL = prefs.getBoolean("screen_Orientation", false);

        if (screenOrientL)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            if (extras.getStringArrayList("filterFileExtension") != null)
            {
                extensions = extras.getStringArrayList("filterFileExtension");
                filterFileName = extras.getString("filterFileName");
                fileFilter = new FileFilter()
                {
                    @Override
                    public boolean accept(File pathname)
                    {
                        return
                            (
                                (pathname.getName().contains(".") &&
                                    pathname.getName().contains(filterFileName) &&
                                    extensions.contains(pathname.getName().substring(pathname.
                                        getName().lastIndexOf(".")))
                                )
                            );
                    }
                };
            }
        }

        // currentDir = new File ("/storage/emulated/0/")
        currentDir = new File(Environment.getExternalStorageDirectory().getPath());
        fill(currentDir);
    }

    // Back-key return from filechooser
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            finish();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    // List only files in user's home directory
    private void fill(File f)
    {
        File[] dirs = null;

        if (fileFilter != null)
            dirs = f.listFiles(fileFilter);
        else
            dirs = f.listFiles();

        this.setTitle(getString(R.string.currentDir) + ": " + f.getName());
        List<Option> fls = new ArrayList<>();
        DateFormat dform = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try
        {
            for (File ff : dirs)
            {
                if (!ff.isHidden())
                {
                    fls.add(new Option(ff.getName(), getString(R.string.fileSize) + ": "
                        + ff.length() + " B,  " + getString(R.string.date) + ": "
                        + dform.format(ff.lastModified()), ff.getAbsolutePath(), false));
                }
            }
        } catch (Exception e)
        {
            // do nothing
        }

        Collections.sort(fls);
        ListView listView = findViewById(R.id.lvFiles);

        adapter = new FileArrayAdapter(listView.getContext(), R.layout.file_view, fls);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> l, View v, int position, long id)
            {
                Option o = adapter.getItem(position);
                if (!o.isBack())
                    doSelect(o);
                else
                {
                    currentDir = new File(o.getPath());
                    fill(currentDir);
                }
            }
        });
    }

    private void doSelect(final Option o)
    {
        // onFileClick(o);
        File fileSelected = new File(o.getPath());
        Intent intent = new Intent();
        intent.putExtra("fileSelected", fileSelected.getAbsolutePath());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    public void onStop()
    {
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
    {
        screenOrientL = prefs.getBoolean("screen_Orientation", false);
    }

}
