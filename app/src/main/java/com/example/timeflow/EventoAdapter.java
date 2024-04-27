package com.example.timeflow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timeflow.HelpersClass.Event;

import java.util.List;

public class EventoAdapter extends RecyclerView.Adapter<EventoAdapter.EventoViewHolder> {

    private Context mContext;
    private List<Event> mEvents;

    public EventoAdapter(Context context, List<Event> events) {
        mContext = context;
        mEvents = events;
    }

    @NonNull
    @Override
    public EventoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_event, parent, false);
        return new EventoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventoViewHolder holder, int position) {
        Event evento = mEvents.get(position);
        holder.eventName.setText(evento.getEventName());
        holder.eventDate.setText(evento.getEventDate());
        holder.eventTime.setText(evento.getEventTime());
    }

    @Override
    public int getItemCount() {
        return mEvents.size();
    }

    public static class EventoViewHolder extends RecyclerView.ViewHolder {
        TextView eventName;
        TextView eventTime;
        TextView eventDate;


        public EventoViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.text_event_name);
            eventDate = itemView.findViewById(R.id.text_event_date);
            eventTime = itemView.findViewById(R.id.text_event_time);

        }
    }
}
