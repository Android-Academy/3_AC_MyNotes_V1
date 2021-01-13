package com.vullnetlimani.mynotes_v1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.ColorUtils;
import androidx.core.widget.CompoundButtonCompat;
import androidx.fragment.app.DialogFragment;

import com.enrico.colorpicker.colorDialog;

import static com.vullnetlimani.mynotes_v1.HelperUtils.colourBackground;
import static com.vullnetlimani.mynotes_v1.HelperUtils.colourFont;
import static com.vullnetlimani.mynotes_v1.HelperUtils.colourNavBar;
import static com.vullnetlimani.mynotes_v1.HelperUtils.colourPrimary;
import static com.vullnetlimani.mynotes_v1.HelperUtils.oppositeColor;

public class SettingsActivity extends AppCompatActivity implements colorDialog.ColorSelectedListener {

    private ImageView primary_imageView, font_imageView, background_imageView;
    private CheckBox nav_checkBox;
    private SharedPreferences sharedPreferences;
    private Toolbar toolbar;

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
        HelperUtils.getSettings(SettingsActivity.this, sharedPreferences);

        toolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        Drawable drawable = toolbar.getNavigationIcon();
        if (drawable != null)
            drawable.setColorFilter(oppositeColor, PorterDuff.Mode.SRC_ATOP);

        primary_imageView = findViewById(R.id.primary_imageView);
        font_imageView = findViewById(R.id.font_imageView);
        background_imageView = findViewById(R.id.background_imageView);

        applySettings();
    }

    private void applySettings() {

        HelperUtils.applyColours(SettingsActivity.this, toolbar);

        findViewById(R.id.layout_container).setBackgroundColor(colourBackground);

        primary_imageView.setColorFilter(colourPrimary);
        font_imageView.setColorFilter(colourFont);
        background_imageView.setColorFilter(colourBackground);

        int tempBorderColor;
        if (HelperUtils.isDark(colourBackground)) {
            tempBorderColor = Color.WHITE;
        } else {
            tempBorderColor = Color.BLACK;
        }

        HelperUtils.setBorderColor(primary_imageView, tempBorderColor);
        HelperUtils.setBorderColor(font_imageView, tempBorderColor);
        HelperUtils.setBorderColor(background_imageView, tempBorderColor);

        ((TextView) findViewById(R.id.primary_textView)).setTextColor(colourFont);
        ((TextView) findViewById(R.id.font_textView)).setTextColor(colourFont);
        ((TextView) findViewById(R.id.background_textView)).setTextColor(colourFont);
        ((TextView) findViewById(R.id.nav_textView)).setTextColor(colourFont);

        Button applyBtn = findViewById(R.id.btn_apply);
        applyBtn.setBackgroundColor(colourPrimary);
        applyBtn.setTextColor(oppositeColor);

        nav_checkBox = findViewById(R.id.nav_checkBox);
        nav_checkBox.setChecked(colourNavBar);
        CompoundButtonCompat.setButtonTintList(nav_checkBox, ColorStateList.valueOf(colourPrimary));

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void showPicker1(View view) {
        colorDialog.setPickerColor(SettingsActivity.this, 1, colourPrimary);
        colorDialog.showColorPicker(SettingsActivity.this, 1);
    }

    public void showPicker2(View view) {
        colorDialog.setPickerColor(SettingsActivity.this, 2, colourFont);
        colorDialog.showColorPicker(SettingsActivity.this, 2);
    }

    public void showPicker3(View view) {
        colorDialog.setPickerColor(SettingsActivity.this, 3, colourBackground);
        colorDialog.showColorPicker(SettingsActivity.this, 3);
    }

    public void toggleCheckBox(View view) {
        nav_checkBox.toggle();
    }

    public void saveSettings(View view) {

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(HelperUtils.PREF_COLOUR_PRIMARY, colourPrimary);
        editor.putInt(HelperUtils.PREF_OPPOSITE_COLOR, oppositeColor);
        editor.putInt(HelperUtils.PREF_COLOUR_FONT, colourFont);
        editor.putInt(HelperUtils.PREF_COLOUR_BACKGROUND, colourBackground);
        editor.putBoolean(HelperUtils.PREF_COLOUR_NAVBAR, nav_checkBox.isChecked());
        editor.apply();


        startActivity(new Intent(SettingsActivity.this, NotesListActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }

    @Override
    public void onColorSelection(DialogFragment dialogFragment, int selectedColor) {
        int tag = Integer.valueOf(dialogFragment.getTag());


        switch (tag) {
            case 1:
                colourPrimary = ColorUtils.setAlphaComponent(selectedColor, 255);
                primary_imageView.setColorFilter(colourPrimary);

                if (HelperUtils.isDark(colourPrimary)) {
                    oppositeColor = Color.WHITE;
                } else {
                    oppositeColor = Color.BLACK;
                }

                break;
            case 2:
                colourFont = ColorUtils.setAlphaComponent(selectedColor, 255);
                font_imageView.setColorFilter(colourFont);
                break;
            case 3:
                colourBackground = ColorUtils.setAlphaComponent(selectedColor, 255);
                background_imageView.setColorFilter(colourBackground);
                break;
        }

    }
}