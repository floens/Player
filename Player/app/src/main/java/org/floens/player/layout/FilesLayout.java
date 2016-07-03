package org.floens.player.layout;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.floens.controller.toolbar.FloatingMenu;
import org.floens.controller.toolbar.FloatingMenuItem;
import org.floens.controller.ui.drawable.DropdownArrowDrawable;
import org.floens.player.R;
import org.floens.player.RecyclerViewUtils;
import org.floens.player.adapter.FilesAdapter;
import org.floens.player.core.StorageHelper;
import org.floens.player.model.FileItem;
import org.floens.player.model.FileItems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.floens.controller.AndroidUtils.dp;
import static org.floens.controller.AndroidUtils.getAttrColor;

public class FilesLayout extends LinearLayout implements FilesAdapter.Callback, View.OnClickListener, FloatingMenu.FloatingMenuCallback {
    private ViewGroup backLayout;
    private ImageView backImage;
    private TextView backText;
    private RecyclerView recyclerView;
    private TextView storageText;

    private LinearLayoutManager layoutManager;
    private FilesAdapter filesAdapter;

    private List<StorageHelper.Storage> storageDirs;

    private Map<String, FileItemHistory> history = new HashMap<>();
    private FileItemHistory currentHistory;
    private FileItems currentFileItems;

    private Callback callback;

    public FilesLayout(Context context) {
        this(context, null);
    }

    public FilesLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FilesLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        backLayout = (ViewGroup) findViewById(R.id.back_layout);
        backImage = (ImageView) backLayout.findViewById(R.id.back_image);
        backImage.setImageDrawable(DrawableCompat.wrap(backImage.getDrawable()));
        backText = (TextView) backLayout.findViewById(R.id.back_text);
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        storageText = (TextView) findViewById(R.id.storage_text);

        backLayout.setOnClickListener(this);
    }

    public void initialize() {
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        filesAdapter = new FilesAdapter(this);
        recyclerView.setAdapter(filesAdapter);

        storageDirs = StorageHelper.externalFolders(getContext());

        if (storageDirs.size() <= 1) {
            storageText.setVisibility(View.GONE);
        } else {
            storageText.setOnClickListener(this);
            storageText.setText(storageDirs.get(0).description);
            Drawable drawable = new DropdownArrowDrawable(dp(12), dp(12), true,
                    getAttrColor(getContext(), R.attr.dropdown_light_color),
                    getAttrColor(getContext(), R.attr.dropdown_light_pressed_color));
            storageText.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
        }
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void setFiles(FileItems fileItems) {
        // Save the associated list position
        if (currentFileItems != null) {
            int[] indexTop = RecyclerViewUtils.getIndexAndTop(recyclerView);
            currentHistory.index = indexTop[0];
            currentHistory.top = indexTop[1];
            history.put(currentFileItems.path.getAbsolutePath(), currentHistory);
        }

        filesAdapter.setFiles(fileItems);
        currentFileItems = fileItems;

        // Restore any previous list position
        currentHistory = history.get(fileItems.path.getAbsolutePath());
        if (currentHistory != null) {
            layoutManager.scrollToPositionWithOffset(currentHistory.index, currentHistory.top);
            filesAdapter.setHighlightedItem(currentHistory.clickedItem);
        } else {
            currentHistory = new FileItemHistory();
            filesAdapter.setHighlightedItem(null);
        }

        boolean enabled = fileItems.canNavigateUp;
        backLayout.setEnabled(enabled);
        Drawable wrapped = DrawableCompat.wrap(backImage.getDrawable());
        backImage.setImageDrawable(wrapped);
        int color = getAttrColor(getContext(), enabled ? R.attr.text_color_primary : R.attr.text_color_hint);
        DrawableCompat.setTint(wrapped, color);
        backText.setEnabled(enabled);
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public ViewGroup getBackLayout() {
        return backLayout;
    }

    public TextView getStorageText() {
        return storageText;
    }

    @Override
    public void onFileItemClicked(FileItem fileItem) {
        currentHistory.clickedItem = fileItem;
        callback.onFileItemClicked(fileItem);
    }

    @Override
    public void onClick(View view) {
        if (view == backLayout) {
            currentHistory.clickedItem = null;
            callback.onBackClicked();
        } else if (view == storageText) {
            List<FloatingMenuItem> items = new ArrayList<>();
            for (StorageHelper.Storage storage : storageDirs) {
                items.add(new FloatingMenuItem(storage, storage.description));
            }

            FloatingMenu floatingMenu = new FloatingMenu(getContext(), storageText, items);
            floatingMenu.setCallback(this);
            floatingMenu.show();
        }
    }

    @Override
    public void onFloatingMenuItemClicked(FloatingMenu menu, FloatingMenuItem item) {
        StorageHelper.Storage storage = (StorageHelper.Storage) item.getId();
        storageText.setText(storage.description);
        callback.onStorageClicked(new FileItem(storage.file));
    }

    @Override
    public void onFloatingMenuDismissed(FloatingMenu menu) {
    }

    private class FileItemHistory {
        int index, top;
        FileItem clickedItem;
    }

    public interface Callback {
        void onBackClicked();

        void onFileItemClicked(FileItem fileItem);

        void onStorageClicked(FileItem fileItem);
    }
}
