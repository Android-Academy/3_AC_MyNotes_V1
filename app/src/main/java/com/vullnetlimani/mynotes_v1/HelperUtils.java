package com.vullnetlimani.mynotes_v1;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HelperUtils {

    public static final String TEXT_FILE_EXTENSION = ".txt";
    public static String PREF_COLOUR_PRIMARY = "colourPrimary";
    public static String PREF_OPPOSITE_COLOR = "oppositeColor";
    public static String PREF_COLOUR_FONT = "colourFont";
    public static String PREF_COLOUR_BACKGROUND = "colourBackground";
    public static String PREF_COLOUR_NAVBAR = "colourNavbar";
    public static String PREF_SORT_ALPHABETICAL = "sortAlphabetical";

    public static boolean colourNavBar, sortAlphabetical;
    @ColorInt
    public static int colourPrimary, oppositeColor, colourFont, colourBackground;

    public static void applyColours(AppCompatActivity activity, Toolbar toolbar) {

        Window window = activity.getWindow();

        if (colourNavBar)
            window.setNavigationBarColor(colourPrimary);


        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(HelperUtils.darkenColor(colourPrimary, 0.2));

        toolbar.setBackgroundColor(colourPrimary);
        toolbar.setTitleTextColor(oppositeColor);

        if (isDark(colourPrimary)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View decor = window.getDecorView();
                decor.setSystemUiVisibility(0);
            }
        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View decor = window.getDecorView();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                } else {
                    decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
            }
        }


    }

    public static boolean isDark(int color) {
        return ColorUtils.calculateLuminance(color) < 0.5;
    }

    public static int darkenColor(int color, double fraction) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        red = darken(red, fraction);
        green = darken(green, fraction);
        blue = darken(blue, fraction);

        int alpha = Color.alpha(color);

        return Color.argb(alpha, red, green, blue);
    }

    private static int darken(int color, double fraction) {
        return (int) Math.max(color - (color * fraction), 0);
    }

    public static void getSettings(Context context, SharedPreferences sharedPreferences) {
        colourPrimary = sharedPreferences.getInt(HelperUtils.PREF_COLOUR_PRIMARY, ContextCompat.getColor(context, R.color.primary));
        oppositeColor = sharedPreferences.getInt(HelperUtils.PREF_OPPOSITE_COLOR, Color.BLACK);
        colourFont = sharedPreferences.getInt(HelperUtils.PREF_COLOUR_FONT, Color.BLACK);
        colourBackground = sharedPreferences.getInt(HelperUtils.PREF_COLOUR_BACKGROUND, ContextCompat.getColor(context, R.color.background_color));
        colourNavBar = sharedPreferences.getBoolean(HelperUtils.PREF_COLOUR_NAVBAR, false);
        sortAlphabetical = sharedPreferences.getBoolean(HelperUtils.PREF_SORT_ALPHABETICAL, false);
    }

    public static void setBorderColor(ImageView borderImage, int tempBorderColor) {
        GradientDrawable drawable = (GradientDrawable) borderImage.getBackground();
        drawable.setStroke(5, tempBorderColor);
    }

    public static Drawable ColorIconDrawable(Context context, int ID, int mColor) {

        Drawable colorDrawable;
        colorDrawable = VectorDrawableCompat.create(context.getResources(), ID, null);
        if (colorDrawable != null) {
            colorDrawable = DrawableCompat.wrap(colorDrawable);
            DrawableCompat.setTint(colorDrawable, mColor);
        }
        return colorDrawable;
    }

    public static boolean fileExists(Context context, String fileName) {
        File file = context.getFileStreamPath(fileName + HelperUtils.TEXT_FILE_EXTENSION);
        return file.exists();
    }

    public static Bitmap convertVector(Context context, int drawID,int yourColor) {
        Drawable drawable = HelperUtils.ColorIconDrawable(context,drawID, yourColor);

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof VectorDrawableCompat || drawable instanceof VectorDrawable) {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } else {
            throw new IllegalArgumentException("Unsuported Drawble Type");
        }

    }

    public static void writeFile(Context context, String fileName, String fileContent) {

        try {
            OutputStreamWriter out = new OutputStreamWriter(context.openFileOutput(fileName + HelperUtils.TEXT_FILE_EXTENSION, 0));
            out.write(fileContent);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Exception: " + e.toString(), Toast.LENGTH_LONG).show();
        }

    }

    public static String readFile(Context context, String fileName) {
        String content = "";

        if (fileExists(context, fileName)) {

            try {
                InputStream in = context.openFileInput(fileName + HelperUtils.TEXT_FILE_EXTENSION);
                if (in != null) {

                    InputStreamReader tmp = new InputStreamReader(in);
                    BufferedReader reader = new BufferedReader(tmp);
                    String str;

                    StringBuilder buf = new StringBuilder();
                    while ((str = reader.readLine()) != null) {
                        buf.append(str).append("\n");
                    }
                    in.close();
                    content = buf.toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Exception: " + e.toString(), Toast.LENGTH_LONG).show();
            }

        }

        return content.trim();
    }

    public static List<File> getFiles(Context context) {
        File[] files = context.getFilesDir().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(HelperUtils.TEXT_FILE_EXTENSION);
            }
        });

        assert files != null;
        return new ArrayList<>(Arrays.asList(files));

    }

    public static Spanned returnColorString(String text) {
        return Html.fromHtml("<font color='" + oppositeColor + "'>" + text + "</font>");
    }
}

