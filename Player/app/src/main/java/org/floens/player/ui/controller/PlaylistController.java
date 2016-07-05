package org.floens.player.ui.controller;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;

import org.floens.controller.Controller;
import org.floens.controller.transition.FadeInTransition;
import org.floens.controller.utils.InsetsHelper;
import org.floens.player.R;
import org.floens.player.ui.adapter.PlaylistAdapter;
import org.floens.player.ui.layout.PlaylistLayout;
import org.floens.player.core.model.PlaylistItem;

import java.util.ArrayList;
import java.util.List;

public class PlaylistController extends Controller implements PlaylistAdapter.Callback {
    private PlaylistLayout playlistLayout;
    private FloatingActionButton playButton;

    public PlaylistController(Context context) {
        super(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        bottomBarItem.text = "Files";
        bottomBarItem.drawable = context.getResources().getDrawable(R.drawable.ic_audiotrack_black_24dp, null);

        playlistLayout = (PlaylistLayout) inflateRes(R.layout.layout_playlist);
        InsetsHelper.attachInsetsPadding(playlistLayout.getRecyclerView(), false, true, false, true);
        view = playlistLayout;

        playlistLayout.getRecyclerView().setLayoutManager(new LinearLayoutManager(context));
        PlaylistAdapter adapter = new PlaylistAdapter();

        List<PlaylistItem> items = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            items.add(new PlaylistItem("Lorem ipsum " + i));
        }

        adapter.setItems(items);
        adapter.setCallback(this);
        playlistLayout.getRecyclerView().setAdapter(adapter);
    }

    @Override
    public void onItemClicked(PlaylistItem item) {
        PlayerController playerController = new PlayerController(context);
        navigationController.pushController(playerController, new FadeInTransition());
    }
}
