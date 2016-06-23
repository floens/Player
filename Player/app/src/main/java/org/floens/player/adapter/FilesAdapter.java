package org.floens.player.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.floens.player.R;
import org.floens.player.model.FileItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FileViewHolder> {
    private List<FileItem> items = new ArrayList<>();
    private Callback callback;

    public FilesAdapter(Callback callback) {
        this.callback = callback;
    }

    public void setFiles(List<File> files) {
        items.clear();
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            FileItem fileItem = new FileItem(file);
            items.add(fileItem);
        }
        notifyDataSetChanged();
    }

    @Override
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FileViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cell_file, parent, false));
    }

    @Override
    public void onBindViewHolder(FileViewHolder holder, int position) {
        FileItem item = getItem(position);
        holder.text.setText(item.file.getName());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public FileItem getItem(int position) {
        return items.get(position);
    }

    private void onItemClicked(FileItem fileItem) {
        callback.onFileItemClicked(fileItem);
    }

    public class FileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView image;
        private TextView text;

        public FileViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            text = (TextView) itemView.findViewById(R.id.text);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            FileItem item = getItem(getAdapterPosition());
            onItemClicked(item);
        }
    }

    public interface Callback {
        void onFileItemClicked(FileItem fileItem);
    }
}
