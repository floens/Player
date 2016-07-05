package org.floens.player.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.floens.player.R;
import org.floens.player.core.model.PlaylistItem;

import java.util.ArrayList;
import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int ITEM_TYPE_ITEM = 0;

    private Callback callback;
    private List<PlaylistItem> items = new ArrayList<>();

    public PlaylistAdapter() {
    }

    public void setItems(List<PlaylistItem> items) {
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_TYPE_ITEM: {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_playlist, parent, false);
                return new PlaylistItemHolder(view);
            }
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        switch (position) {
            default:
                return ITEM_TYPE_ITEM;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case ITEM_TYPE_ITEM:
                PlaylistItem item = getItem(position);
                PlaylistItemHolder itemHolder = (PlaylistItemHolder) holder;
                itemHolder.text.setText(item.text);
        }
    }

    private PlaylistItem getItem(int position) {
        return items.get(position);
    }

    private void onItemClicked(PlaylistItem item) {
        if (callback != null) {
            callback.onItemClicked(item);
        }
    }

    public class PlaylistItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView image;
        private TextView text;

        public PlaylistItemHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            text = (TextView) itemView.findViewById(R.id.text);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            PlaylistItem item = getItem(getAdapterPosition());
            onItemClicked(item);
        }
    }

    public interface Callback {
        void onItemClicked(PlaylistItem item);
    }
}
