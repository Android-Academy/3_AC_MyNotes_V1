package com.vullnetlimani.mynotes_v1;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.vullnetlimani.mynotes_v1.HelperUtils.colourBackground;

public class NoteListAdapter extends RecyclerView.Adapter<NoteListAdapter.ViewHolder> {

    int colorText;
    int colorBackground;
    private List<File> fullList, filesList;

    public NoteListAdapter(int colorText, int colorBackground) {
        filesList = new ArrayList<>();
        fullList = new ArrayList<>();
        this.colorText = colorText;
        this.colorBackground = colorBackground;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflateView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(inflateView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File file = filesList.get(position);
        String fileName = file.getName().substring(0, file.getName().length() - 4);
        String fileDate = DateFormat.getDateInstance(DateFormat.MEDIUM).format(file.lastModified());
        String fileTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(file.lastModified());
        holder.setData(fileName, fileDate, fileTime);
    }

    @Override
    public int getItemCount() {
        return filesList.size();
    }

    public void updateList(List<File> files, boolean sortAlphabetical) {

        filesList = files;
        sortList(sortAlphabetical);
        fullList = new ArrayList<>(filesList);
        // notifyDataSetChanged();

    }


    public void sortList(boolean sortAlphabetical) {

        if (sortAlphabetical) {
            sortAlphabetical(filesList);
        } else {
            sortDate(filesList);
        }

        DiffUtil.calculateDiff(new NotesDiffCallback(fullList, filesList)).dispatchUpdatesTo(this);
        fullList = new ArrayList<>(filesList);

    }

    private void sortAlphabetical(List<File> files) {
        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return (o1.getName().compareTo(o2.getName()));
            }
        });

    }

    private void sortDate(List<File> files) {

        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Long.compare(o1.lastModified(), o2.lastModified());
            }
        });

    }

    public void deleteFile(int position) {
        File file = filesList.get(position);
        fullList.remove(file);
        filesList.remove(file);
        notifyItemRemoved(position);
        file.delete();
    }

    public void cancelDelete(int layoutPosition) {
        notifyItemChanged(layoutPosition);
    }

    public void filterList(String query) {
        if (TextUtils.isEmpty(query)) {
            DiffUtil.calculateDiff(new NotesDiffCallback(filesList, fullList)).dispatchUpdatesTo(this);
            filesList = new ArrayList<>(fullList);
        } else {
            filesList.clear();

            for (int i = 0; i < fullList.size(); i++) {
                final File file = fullList.get(i);
                String filename = file.getName().substring(0, file.getName().length() - 4).toLowerCase();
                if (filename.contains(query)) {
                    filesList.add(fullList.get(i));
                }
            }
            DiffUtil.calculateDiff(new NotesDiffCallback(fullList, filesList)).dispatchUpdatesTo(this);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView noteTitle, noteDate, noteTime;
        ConstraintLayout constraintLayout;
        CardView mCardView;
        private String stringTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            noteTitle = itemView.findViewById(R.id.text_title);
            noteDate = itemView.findViewById(R.id.text_date);
            noteTime = itemView.findViewById(R.id.text_time);

            constraintLayout = itemView.findViewById(R.id.layout_constraint);
            constraintLayout.setBackgroundColor(colorBackground);

            mCardView = itemView.findViewById(R.id.mCardView);

            mCardView.setCardBackgroundColor(Color.WHITE);
            noteTitle.setTextColor(Color.DKGRAY);
            noteDate.setTextColor(Color.GRAY);
            noteTime.setTextColor(Color.GRAY);

            itemView.setOnClickListener(this);

        }

        void setData(String title, String data, String time) {
            stringTitle = title;
            noteTitle.setText(title);
            noteDate.setText(data);
            noteTime.setText(time);
        }

        @Override
        public void onClick(View v) {
            itemView.getContext().startActivity(NoteActivity.getStartIntent(itemView.getContext(), stringTitle));
        }
    }
}
