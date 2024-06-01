package com.example.timeflow.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timeflow.HelpersClass.Event;
import com.example.timeflow.R;

import java.util.List;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventsViewHolder> {

    private Context mContext;
    private List<Event> mEvents;
    private OnItemClickListener mListener;


    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public EventsAdapter(Context context, List<Event> events, OnItemClickListener listener) {
        mContext = context;
        mEvents = events;
        mListener = listener;
    }

    @NonNull
    @Override
    public EventsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_event, parent, false);
        return new EventsViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull EventsViewHolder holder, int position) {
        Event event = mEvents.get(position);
        holder.eventName.setText(event.getEventName());
        holder.eventDate.setText(event.getEventDate());
        holder.eventTime.setText(event.getEventTime());
    }

    @Override
    public int getItemCount() {
        return mEvents.size();
    }

    public void scrollToBottom(RecyclerView recyclerView) {
        if (getItemCount() > 0) {
            recyclerView.smoothScrollToPosition(getItemCount() - 1);
        }
    }

    public static class EventsViewHolder extends RecyclerView.ViewHolder {
        TextView eventName;
        TextView eventTime;
        TextView eventDate;

        public EventsViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            eventName = itemView.findViewById(R.id.text_event_name);
            eventDate = itemView.findViewById(R.id.text_event_date);
            eventTime = itemView.findViewById(R.id.text_event_time);

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




