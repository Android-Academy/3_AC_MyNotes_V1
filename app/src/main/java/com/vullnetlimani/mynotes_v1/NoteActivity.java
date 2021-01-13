package com.vullnetlimani.mynotes_v1;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.ColorUtils;

import static com.vullnetlimani.mynotes_v1.HelperUtils.colourBackground;
import static com.vullnetlimani.mynotes_v1.HelperUtils.colourFont;
import static com.vullnetlimani.mynotes_v1.HelperUtils.colourPrimary;
import static com.vullnetlimani.mynotes_v1.HelperUtils.oppositeColor;

public class NoteActivity extends AppCompatActivity {

    private static final String EXTRA_NOTE_TITLE = "EXTRA_NOTE_TITLE";
    private SharedPreferences sharedPreferences;
    private String title;
    private String note;
    private Toolbar toolbar;
    private EditText titleText;
    private EditText noteText;
    private AlertDialog dialog;

    public static Intent getStartIntent(Context context, String title) {
        Intent intent = new Intent(context, NoteActivity.class);
        intent.putExtra(EXTRA_NOTE_TITLE, title);
        return intent;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.btn_delete).setIcon(HelperUtils.ColorIconDrawable(NoteActivity.this, R.drawable.ic_delete, oppositeColor));
        menu.findItem(R.id.btn_undo).setIcon(HelperUtils.ColorIconDrawable(NoteActivity.this, R.drawable.ic_undo, oppositeColor));
        menu.findItem(R.id.btn_share).setIcon(HelperUtils.ColorIconDrawable(NoteActivity.this, R.drawable.ic_share, oppositeColor));

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                return true;
            case R.id.btn_undo:
                noteText.setText(note);
                noteText.setSelection(noteText.getText().length());
                return true;
            case R.id.btn_share:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, noteText.getText().toString());
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getString(R.string.shareto)));
                return true;
            case R.id.btn_delete:

                dialog = new AlertDialog.Builder(NoteActivity.this)
                        .setTitle(HelperUtils.returnColorString(getString(R.string.confrim_del)))
                        .setMessage(HelperUtils.returnColorString(getString(R.string.del_message)))

                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (HelperUtils.fileExists(NoteActivity.this, title))
                                    deleteFile(title + HelperUtils.TEXT_FILE_EXTENSION);

                                title = "";
                                note = "";
                                titleText.setText(title);
                                noteText.setText(note);
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setIcon(HelperUtils.ColorIconDrawable(NoteActivity.this, R.drawable.ic_delete, oppositeColor))

                        .show();

                if (dialog.getWindow() != null)
                    dialog.getWindow().getDecorView().setBackgroundColor(colourPrimary);


                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(oppositeColor);
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(oppositeColor);

                return true;

        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(NoteActivity.this);
        HelperUtils.getSettings(NoteActivity.this, sharedPreferences);

        toolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        Drawable drawable = toolbar.getNavigationIcon();
        if (drawable != null)
            drawable.setColorFilter(oppositeColor, PorterDuff.Mode.SRC_ATOP);

        titleText = findViewById(R.id.edit_title);
        noteText = findViewById(R.id.edit_note);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        //Nqse aktivity starton pi ne share intent
        if (Intent.ACTION_SEND.equals(action) && type != null) {

        } else {

            title = intent.getStringExtra(EXTRA_NOTE_TITLE);
            if (title == null || TextUtils.isEmpty(title)) {
                title = "";
                note = "";
                noteText.requestFocus();
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.new_note);

            } else {
                titleText.setText(title);
                note = HelperUtils.readFile(NoteActivity.this, title);
                noteText.setText(note);
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(title);
            }

        }

        applySettings();
    }


    private void applySettings() {
        HelperUtils.applyColours(NoteActivity.this, toolbar);

        findViewById(R.id.nested_scrollView).setBackgroundColor(colourBackground);

        titleText.setBackgroundTintList(ColorStateList.valueOf(colourPrimary));
        noteText.setBackgroundTintList(ColorStateList.valueOf(colourPrimary));


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setBackgroundDrawable(new ColorDrawable(colourPrimary));


        titleText.setTextColor(colourFont);
        noteText.setTextColor(colourFont);


        titleText.setHintTextColor(ColorUtils.setAlphaComponent(colourFont, 120));
        noteText.setHintTextColor(ColorUtils.setAlphaComponent(colourFont, 120));

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        note = noteText.getText().toString().trim();
        if (getCurrentFocus() != null)
            getCurrentFocus().clearFocus();

    }

    @Override
    protected void onPause() {

        if (!isChangingConfigurations()) {
            saveFile();
        }
        super.onPause();
    }

    private void saveFile() {

        String newTitle = titleText.getText().toString().trim().replace("/", " ");
        String newNote = noteText.getText().toString().trim();

        if (TextUtils.isEmpty(newTitle) && TextUtils.isEmpty(newNote))
            return;

        if (newTitle.equals(title) && newNote.equals(note))
            return;

        if (!title.equals(newTitle) || TextUtils.isEmpty(newTitle)) {
            newTitle = newFileName(newTitle);
            titleText.setText(newTitle);
        }

        HelperUtils.writeFile(NoteActivity.this, newTitle, newNote);

        if (!TextUtils.isEmpty(title) && !newTitle.equals(title)) {
            deleteFile(title + HelperUtils.TEXT_FILE_EXTENSION);
        }

        title = newTitle;

        Toast.makeText(this, title + " saved", Toast.LENGTH_SHORT).show();

    }

    private String newFileName(String name) {

        if (TextUtils.isEmpty(name)) {
            name = getString(R.string.note);
        }

        if (HelperUtils.fileExists(NoteActivity.this, name)) {
            int i = 1;
            while (true) {

                if (!HelperUtils.fileExists(NoteActivity.this, name + " (" + i + ")") || title.equals(name + " (" + i + ")")) {

                    name = (name + " (" + i + ")");

                    break;
                }
                i++;
            }

        }
        return name;
    }
}