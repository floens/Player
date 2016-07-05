package org.floens.controller.utils;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Field;

public class RecyclerViewUtils {
    private static final String TAG = "RecyclerViewUtils";

    public static int[] getIndexAndTop(RecyclerView recyclerView) {
        int index = 0, top = 0;
        if (recyclerView.getLayoutManager().getChildCount() > 0) {
            View topChild = recyclerView.getLayoutManager().getChildAt(0);
            index = ((RecyclerView.LayoutParams) topChild.getLayoutParams()).getViewLayoutPosition();
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) topChild.getLayoutParams();
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            top = layoutManager.getDecoratedTop(topChild) - params.topMargin - recyclerView.getPaddingTop();
        }
        return new int[]{index, top};
    }

    public static void clearRecyclerCache(RecyclerView recyclerView) {
        try {
            Field field = RecyclerView.class.getDeclaredField("mRecycler");
            field.setAccessible(true);
            RecyclerView.Recycler recycler = (RecyclerView.Recycler) field.get(recyclerView);
            recycler.clear();
        } catch (Exception e) {
            Log.e(TAG, "Error clearing RecyclerView cache with reflection", e);
        }
    }
}
