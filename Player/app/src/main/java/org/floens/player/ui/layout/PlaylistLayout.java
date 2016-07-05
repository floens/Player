package org.floens.player.ui.layout;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import org.floens.player.R;

public class PlaylistLayout extends FrameLayout {
    private static final String TAG = "PlaylistLayout";

    private RecyclerView recyclerView;

    public PlaylistLayout(Context context) {
        this(context, null);
    }

    public PlaylistLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlaylistLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        recyclerView = (RecyclerView) findViewById(R.id.recycler);
    }
}
