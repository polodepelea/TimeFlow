package com.example.timeflow.Adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.timeflow.HelpersClass.Notepad;
import com.example.timeflow.R;

import java.util.List;

public class NotepadAdapter extends RecyclerView.Adapter<NotepadAdapter.NotepadViewHolder> {

    private Context mContext;
    private List<Notepad> mNotepads;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public NotepadAdapter(Context context, List<Notepad> notepads, OnItemClickListener listener) {
        mContext = context;
        mNotepads = notepads;
        mListener = listener;
    }

    @NonNull
    @Override
    public NotepadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_notepad, parent, false);
        return new NotepadViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull NotepadViewHolder holder, int position) {
            Notepad notepad1 = mNotepads.get(position);
            holder.title.setText(notepad1.getTitle());
            holder.text.setText(notepad1.getText());
    }

    @Override
    public int getItemCount() {
        return mNotepads.size();
    }

    public static class NotepadViewHolder extends RecyclerView.ViewHolder {
        TextView title, text;

        public NotepadViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            title = itemView.findViewById(R.id.cardTitle);
            text = itemView.findViewById(R.id.cardContent);

            itemView.setOnClickListener(view -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });

        }
    }
}


