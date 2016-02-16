package com.example.kevin.japanesememoryapp;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.renderscript.ScriptGroup;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class InputActivity extends AppCompatActivity {
    public static int DEFAULT_DIFFICULTY = 5;

    FeedReaderDbHelper dbHelper;

    TextView lbl_addKanji;
    EditText tbx_furigana;
    EditText tbx_kanji;
    EditText tbx_meaning;
    Button btn_addNewKanji;
    Button btn_editKanji;

    Kanji currentKanji;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences settings = getSharedPreferences(SettingsActivity.PREFERENCES_FILE_NAME, 0);
        applyTheme(settings);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        lbl_addKanji = (TextView)findViewById(R.id.lbl_addKanji);
        tbx_furigana = (EditText)findViewById(R.id.tbx_addFurigana);
        tbx_kanji = (EditText)findViewById(R.id.tbx_addKanji);
        tbx_meaning = (EditText)findViewById(R.id.tbx_addMeaning);
        btn_addNewKanji = (Button)findViewById(R.id.btn_addNewKanji);
        btn_editKanji = (Button)findViewById(R.id.btn_editKanji);

        Button btn_addKanji = (Button)findViewById(R.id.btn_addNewKanji);
        btn_addKanji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(verifyInput()) {
                    saveKanji();
                }
                else {
                    MainActivity.showAlert(InputActivity.this, getString(R.string.error), getString(R.string.inputFillInAllPlease));
                }
            }
        });

        dbHelper = new FeedReaderDbHelper(InputActivity.this);


        Intent intent = getIntent();
        currentKanji = (Kanji)intent.getParcelableExtra("incoming_kanji");

        if(currentKanji != null) {
            btn_addKanji.setVisibility(View.GONE);
            btn_editKanji.setVisibility(View.VISIBLE);

            lbl_addKanji.setText(getString(R.string.inputEditKanji) + " ID:" + currentKanji.getKanjiID());
            tbx_furigana.setText(currentKanji.getFurigana());
            tbx_kanji.setText(currentKanji.getKanji());
            tbx_meaning.setText(currentKanji.getMeaning());

            btn_editKanji.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateKanji(currentKanji.getKanjiID());
                }
            });
        }
    }

    private void applyTheme(SharedPreferences settings) {
        switch((int)settings.getLong("themeSelection", 0L)) {
            case 0:
                setTheme(R.style.AppThemeLight);
                break;
            case 1:
                setTheme(R.style.AppThemeDark);
                break;
            default:
                setTheme(R.style.AppThemeBlue);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_input, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveKanji() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_FURIGANA, tbx_furigana.getText().toString());
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_KANJI, tbx_kanji.getText().toString());
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_MEANING, tbx_meaning.getText().toString());
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_DIFFICULTY, DEFAULT_DIFFICULTY);

        long newRowId;
        newRowId = db.insert(
                FeedReaderContract.FeedEntry.TABLE_NAME,
                FeedReaderContract.FeedEntry.COLUMN_NAME_KANJI,
                values
        );
        if(newRowId == -1) {
            MainActivity.showAlert(InputActivity.this, getString(R.string.error), getString(R.string.inputUnableToAdd));
        }
        else {
            MainActivity.showAlert(InputActivity.this, getString(R.string.inputNewKanjiAdded), "(id: " + newRowId + ")");
            tbx_furigana.setText("");
            tbx_kanji.setText("");
            tbx_meaning.setText("");
        }
    }

    private void updateKanji(int index) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_FURIGANA, tbx_furigana.getText().toString());
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_KANJI, tbx_kanji.getText().toString());
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_MEANING, tbx_meaning.getText().toString());

        if(db.update(FeedReaderContract.FeedEntry.TABLE_NAME, values, " id=" + index, null) > 0) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            finish();
                            break;
                    }
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(InputActivity.this);
            builder.setMessage(getString(R.string.inputUpdateSuccess)).setPositiveButton(getString(R.string.ok), dialogClickListener).show();
        }
        else {
            MainActivity.showAlert(InputActivity.this, getString(R.string.error), getString(R.string.inputUpdateWrong));
        }
    }

    private boolean verifyInput() {
        if(tbx_furigana.getText().toString().equals("")){
            return false;
        }
        if(tbx_kanji.getText().toString().equals("")){
            return false;
        }
        if(tbx_meaning.getText().toString().equals("")){
            return false;
        }
        return true;
    }
}
