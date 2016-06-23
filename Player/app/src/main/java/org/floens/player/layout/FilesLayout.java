package org.floens.player.layout;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import org.floens.player.R;

public class FilesLayout extends FrameLayout {
    private RecyclerView recyclerView;

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
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }
}
