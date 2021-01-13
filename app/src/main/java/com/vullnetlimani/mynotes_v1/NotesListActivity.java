package com.vullnetlimani.mynotes_v1;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import static com.vullnetlimani.mynotes_v1.HelperUtils.PREF_SORT_ALPHABETICAL;
import static com.vullnetlimani.mynotes_v1.HelperUtils.colourBackground;
import static com.vullnetlimani.mynotes_v1.HelperUtils.colourFont;
import static com.vullnetlimani.mynotes_v1.HelperUtils.colourPrimary;
import static com.vullnetlimani.mynotes_v1.HelperUtils.oppositeColor;
import static com.vullnetlimani.mynotes_v1.HelperUtils.sortAlphabetical;

public class NotesListActivity extends AppCompatActivity {

    private TextView tv_empty;
    private FloatingActionButton myFabButton;
    private SharedPreferences sharedPreferences;
    private Toolbar toolbar, searchToolbar;
    private FrameLayout mFrameLayout;
    private Menu search_menu;
    private MenuItem item_search;
    private NoteListAdapter noteListAdapter;
    private AlertDialog dialog;

    @Override
    public void onBackPressed() {

        if (item_search != null && item_search.isActionViewExpanded()) {
            item_search.collapseActionView();
        } else {
            super.onBackPressed();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.ThemeMyNotes_V1);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_list);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(NotesListActivity.this);
        HelperUtils.getSettings(NotesListActivity.this, sharedPreferences);

        mFrameLayout = findViewById(R.id.mFrameLayout);
        toolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(toolbar);
        setSearchToolbar();

        tv_empty = findViewById(R.id.tv_empty);
        myFabButton = findViewById(R.id.myFabButton);


        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(NotesListActivity.this);
        recyclerView.setLayoutManager(layoutManager);

        noteListAdapter = new NoteListAdapter(colourFont, colourBackground);
        recyclerView.setAdapter(noteListAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    myFabButton.show();

            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {

                if (dy > 0 || dy < 0 && myFabButton.isShown()) {
                    myFabButton.hide();
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });


        setItemTouchHelper(recyclerView);
        applySettings();
    }

    private void setItemTouchHelper(RecyclerView recyclerView) {

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onChildDraw(@NonNull Canvas canvas, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                    View itemView = viewHolder.itemView;

                    Paint p = new Paint();
                    p.setColor(ContextCompat.getColor(NotesListActivity.this, R.color.colorDelete));
                    Bitmap icon = HelperUtils.convertVector(NotesListActivity.this, R.drawable.ic_delete, Color.WHITE);

                    try {
                        if (dX > 0) {

                            canvas.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom(), p);

                            canvas.drawBitmap(icon,
                                    (float) itemView.getLeft() + Math.round(16 * (getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT)),
                                    (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - (float) icon.getHeight()) / 2, p);

                        } else {

                            canvas.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom(), p);

                            canvas.drawBitmap(icon,
                                    (float) itemView.getRight() - Math.round(16 * (getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT)) - icon.getWidth(),
                                    (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - (float) icon.getHeight()) / 2, p);

                        }
                    } catch (NullPointerException e) {

                        Log.e("ErrorLog", e.toString());

                    }

                }

                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                dialog = new AlertDialog.Builder(NotesListActivity.this)
                        .setTitle(HelperUtils.returnColorString(getString(R.string.confrim_del)))
                        .setMessage(HelperUtils.returnColorString(getString(R.string.del_message)))
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                noteListAdapter.deleteFile(viewHolder.getLayoutPosition());
                                showEmptyListMessage();
                            }
                        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                noteListAdapter.cancelDelete(viewHolder.getLayoutPosition());
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                noteListAdapter.cancelDelete(viewHolder.getLayoutPosition());
                            }
                        })
                        .setIcon(HelperUtils.ColorIconDrawable(NotesListActivity.this, R.drawable.ic_delete, oppositeColor))
                        .show();

                if (dialog.getWindow() != null)
                    dialog.getWindow().getDecorView().setBackgroundColor(colourPrimary);


                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(oppositeColor);
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(oppositeColor);


            }
        };

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);

    }

    private void setSearchToolbar() {

        mFrameLayout.setBackgroundColor(colourPrimary);

        searchToolbar = findViewById(R.id.searchToolbar);

        if (searchToolbar != null) {

            searchToolbar.inflateMenu(R.menu.menu_search);
            search_menu = searchToolbar.getMenu();

            searchToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    circleReveal(true, false);
                }
            });

            item_search = search_menu.findItem(R.id.action_filter_search);

            item_search.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    toolbar.setVisibility(View.GONE);
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    circleReveal(true, false);
                    return true;
                }
            });

            initSearchView();

        }

    }

    private void initSearchView() {

        int tempWidgetColor;
        if (HelperUtils.isDark(oppositeColor)) {
            tempWidgetColor = Color.WHITE;
        } else {
            tempWidgetColor = Color.BLACK;
        }

        searchToolbar.setBackgroundColor(oppositeColor);
        searchToolbar.setTitleTextColor(tempWidgetColor);
        searchToolbar.setCollapseIcon(R.drawable.ic_back);

        Drawable backIcon = searchToolbar.getCollapseIcon();
        if (backIcon != null)
            backIcon.setColorFilter(tempWidgetColor, PorterDuff.Mode.SRC_ATOP);

        MenuItem search = search_menu.findItem(R.id.action_filter_search);
        search.setIcon(HelperUtils.ColorIconDrawable(NotesListActivity.this, R.drawable.ic_search, tempWidgetColor));
        SearchView searchView = (SearchView) search.getActionView();

        EditText textSearch = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        textSearch.setHint(R.string.search);
        textSearch.setHintTextColor(Color.GRAY);
        textSearch.setTextColor(tempWidgetColor);

        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        ImageView searchCloseIcon = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        searchCloseIcon.setColorFilter(tempWidgetColor);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                callSearch(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                callSearch(newText);
                return true;
            }


            public void callSearch(String query) {
                noteListAdapter.filterList(query.toLowerCase());
            }

        });


    }

    public void circleReveal(final boolean containsOverflow, final boolean isShow) {

        // make the view visible and start the animation
        final int startAnimFrom = 2;

        searchToolbar.post(new Runnable() {
            @Override
            public void run() {

                int width = searchToolbar.getWidth();

                width -= (startAnimFrom * getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_material)) - (getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_material) / 2);

                if (containsOverflow)
                    width -= getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_overflow_material);

                int cx = width;
                int cy = searchToolbar.getHeight() / 2;

                Animator anim;

                if (isShow)
                    anim = ViewAnimationUtils.createCircularReveal(searchToolbar, cx, cy, 0, (float) width);
                else
                    anim = ViewAnimationUtils.createCircularReveal(searchToolbar, cx, cy, (float) width, 0);

                // make the view invisible when the animation is done
                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (!isShow) {
                            searchToolbar.setVisibility(View.GONE);
                            toolbar.setVisibility(View.VISIBLE);
                            super.onAnimationEnd(animation);
                        }
                    }
                });

                anim.setDuration(220);

                if (isShow)
                    searchToolbar.setVisibility(View.VISIBLE);
                // start the animation
                anim.start();

            }
        });

    }

    private void applySettings() {
        HelperUtils.applyColours(NotesListActivity.this, toolbar);
        findViewById(R.id.main_layout_coordinate).setBackgroundColor(colourBackground);
        tv_empty.setTextColor(colourFont);
        myFabButton.setBackgroundTintList(ColorStateList.valueOf(colourPrimary));
        myFabButton.setColorFilter(oppositeColor);
    }

    private void showEmptyListMessage() {

        if (noteListAdapter.getItemCount() == 0) {
            tv_empty.setVisibility(View.VISIBLE);
        } else if (tv_empty.getVisibility() == View.VISIBLE) {
            tv_empty.setVisibility(View.GONE);
        }

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.btn_sort).setIcon(HelperUtils.ColorIconDrawable(NotesListActivity.this, R.drawable.numeric_to_alphabetical, oppositeColor));
        menu.findItem(R.id.btn_settings).setIcon(HelperUtils.ColorIconDrawable(NotesListActivity.this, R.drawable.ic_settings, oppositeColor));
        menu.findItem(R.id.btn_search).setIcon(HelperUtils.ColorIconDrawable(NotesListActivity.this, R.drawable.ic_search, oppositeColor));

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_notes_list, menu);

        if (sortAlphabetical)
            menu.findItem(R.id.btn_sort).setIcon(R.drawable.ic_sort_alphabetical);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.btn_settings:
                startActivity(new Intent(NotesListActivity.this, SettingsActivity.class));
                return true;
            case R.id.btn_sort:


                if (sortAlphabetical) {
                    item.setIcon(R.drawable.alphabetical_to_numerical);
                    sortAlphabetical = false;
                } else {
                    item.setIcon(R.drawable.numeric_to_alphabetical);
                    sortAlphabetical = true;
                }

                noteListAdapter.sortList(sortAlphabetical);

                Drawable drawable = item.getIcon();
                drawable.setColorFilter(oppositeColor, PorterDuff.Mode.SRC_ATOP);
                if (drawable instanceof Animatable)
                    ((Animatable) drawable).start();
                return true;
            case R.id.btn_search:

                circleReveal(true, true);

                item_search.expandActionView();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        if (item_search != null && item_search.isActionViewExpanded())
            item_search.collapseActionView();

        noteListAdapter.updateList(HelperUtils.getFiles(NotesListActivity.this), sortAlphabetical);

        showEmptyListMessage();

        findViewById(R.id.main_layout_coordinate).clearFocus();

    }

    @Override
    protected void onPause() {

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PREF_SORT_ALPHABETICAL, sortAlphabetical);
        editor.apply();

        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
        dialog = null;

        super.onPause();
    }

    public void newNote(View view) {
        startActivity(NoteActivity.getStartIntent(NotesListActivity.this, ""));
    }

}
