package com.wmstein.transektcount;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

/**********************************************************
 * Set the Settings parameters for TransektCount
 * Based on SettingsActivity created by milo on 05/05/2014.
 * Adapted for TransektCount by wmstein on 18.02.2016
 */
public class SettingsActivity extends PreferenceActivity
{
    private static String TAG = "transektcountPreferenceActivity";
    private static final int SELECT_PICTURE = 1;
    String imageFilePath;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    Uri alert_uri;
    Uri alert_button_uri;

    @Override
    @SuppressLint("CommitPrefEdits")
    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        Preference button = (Preference) findPreference("button");
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference arg0)
            {
                getImage();
                return true;
            }
        });

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Sound for alerts
        String strRingtonePreference = prefs.getString("alert_sound", "DEFAULT_SOUND");
        alert_uri = Uri.parse(strRingtonePreference);
        //Log.i(TAG,"ALERT_URI: " + String.valueOf(alert_uri));

        Preference alert_sound = (Preference) findPreference("alert_sound");
        alert_sound.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference arg0)
            {
                getSound(alert_uri, 5);
                return true;
            }
        });

        // Sound for keypresses
        String strButtonSoundPreference = prefs.getString("alert_button_sound", "DEFAULT_SOUND");
        alert_button_uri = Uri.parse(strButtonSoundPreference);

        Preference alert_button_sound = (Preference) findPreference("alert_button_sound");
        alert_button_sound.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference arg0)
            {
                getSound(alert_button_uri, 10);
                return true;
            }
        });

        editor = prefs.edit(); // will be committed on pause

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        String strRingtonePreference = prefs.getString("alert_sound", "DEFAULT_SOUND");
        alert_uri = Uri.parse(strRingtonePreference);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        editor.commit();
    }

    public void getImage()
    {
        Intent pickIntent = new Intent();
        pickIntent.setType("image/*");
        pickIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(pickIntent, SELECT_PICTURE);
    }

    public void getSound(Uri tmp_alert_uri, int requestCode)
    {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.pref_sound));
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) tmp_alert_uri);
        this.startActivityForResult(intent, requestCode);
    }


    @Override
    @SuppressLint("CommitPrefEdits")
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == SELECT_PICTURE && data != null && data.getData() != null)
        {
            Uri _uri = Uri.parse(data.getDataString());

            if (_uri != null)
            {
                //User did pick an image.
        /*
         * The try is here because this action fails if the user uses a file manager; the gallery
         * seems to work nicely, though.
         */
                Cursor cursor = getContentResolver().query(_uri, new String[]{android.provider.MediaStore.Images.ImageColumns.DATA}, null, null, null);
                try
                {
                    cursor.moveToFirst(); // blows up here if file manager used
                } catch (Exception e)
                {
                    Log.e(TAG, "Failed to select image: " + e.toString());
                    Toast.makeText(this, getString(R.string.image_error), Toast.LENGTH_LONG).show();
                    return;
                }

                //Link to the image
                imageFilePath = cursor.getString(0);
                cursor.close();

                // save the image path
                editor.putString("imagePath", imageFilePath);
                //editor.commit();
                try
                {
                    Log.i(TAG, "IMAGE (in Settings): " + imageFilePath);
                } catch (Exception e)
                {
                    Log.e(TAG, "Failed to upload image: " + e.toString());
                    Toast.makeText(this, getString(R.string.image_error), Toast.LENGTH_LONG).show();
                }
            }
        }
        else if (resultCode == Activity.RESULT_OK)
        {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            String ringtone = null;
            if (uri != null)
            {
                ringtone = uri.toString();
                if (requestCode == 5)
                {
                    editor.putString("alert_sound", ringtone);
                }
                else if (requestCode == 10)
                {
                    editor.putString("alert_button_sound", ringtone);
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        case android.R.id.home:
            startActivity(new Intent(this, WelcomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            break;
        default:
            return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
