package org.floens.player.ui.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.floens.player.R;
import org.floens.player.core.model.Track;
import org.floens.player.ui.view.ReactiveButton;

import java.util.List;

public class TrackSwitcher extends LinearLayout implements ReactiveButton.Callback {
    private ReactiveButton button;
    private TextView text;

    private List<Track> tracks;
    private int selectedId;

    private Callback callback;

    public TrackSwitcher(Context context) {
        super(context);
    }

    public TrackSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TrackSwitcher(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void setTracks(List<Track> tracks) {
        this.tracks = tracks;
        updateText();
    }

    public void setSelectedId(int selectedId) {
        this.selectedId = selectedId;
        updateText();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        button = (ReactiveButton) findViewById(R.id.track_switcher_button);
        button.setCallback(this);
        text = (TextView) findViewById(R.id.track_switcher_text);
    }

    @Override
    public void onButtonClicked(ReactiveButton button, int selected) {
        if (tracks == null) {
            return;
        }

        int selectedIndex = -1;
        for (int i = 0; i < tracks.size(); i++) {
            Track t = tracks.get(i);
            if (t.id == selectedId) {
                selectedIndex = i;
            }
        }
        int nextId;
        if (selectedIndex < 0) {
            nextId = tracks.get(0).id;
        } else if (selectedIndex < tracks.size() - 1) {
            nextId = tracks.get(selectedIndex + 1).id;
        } else {
            nextId = 0;
        }

        if (callback != null) {
            callback.onTrackChanged(this, nextId);
        }
    }

    private void updateText() {
        if (tracks == null) {
            return;
        }

        String left = "-";
        if (selectedId > 0) {
            for (int i = 0; i < tracks.size(); i++) {
                Track track = tracks.get(i);
                if (track.id == selectedId) {
                    left = String.valueOf(i + 1);
                    break;
                }
            }
        }
        text.setText(left + "/" + tracks.size());
    }

    public interface Callback {
        void onTrackChanged(TrackSwitcher trackSwitcher, int id);
    }
}
