package org.floens.player.ui.controller;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import org.floens.controller.Controller;
import org.floens.controller.ControllerTransition;
import org.floens.controller.transition.FadeInTransition;
import org.floens.player.R;
import org.floens.controller.utils.ViewPagerAdapter;
import org.floens.player.ui.layout.InsetsBarsFrameLayout;
import org.floens.player.ui.layout.PlayerBar;
import org.floens.player.ui.view.BottomBar;
import org.floens.player.ui.view.BottomBarItem;

public class PagedNavigationController extends Controller implements View.OnClickListener, ViewPager.OnPageChangeListener, BottomBar.BottomBarCallback {
    private InsetsBarsFrameLayout insetsFrameLayout;
    private ViewPager viewPager;

    private PlayerBar playerBar;
    private BottomBar bottomBar;

    public PagedNavigationController(Context context) {
        super(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        view = inflateRes(R.layout.controller_navigation_paged);
        insetsFrameLayout = (InsetsBarsFrameLayout) view;
        insetsFrameLayout.setDrawBars(true, false, true, true);
        viewPager = (ViewPager) view.findViewById(R.id.paged_container);
        viewPager.setAdapter(new PagedNavigationControllerAdapter());
        viewPager.addOnPageChangeListener(this);
        viewPager.setOffscreenPageLimit(100);
//        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
//        toolbar.setCallback(this);
        playerBar = (PlayerBar) view.findViewById(R.id.music_bar);
        playerBar.setOnClickListener(this);
        bottomBar = (BottomBar) view.findViewById(R.id.bottom_bar);
        bottomBar.setCallback(this);
    }

    public void addController(Controller controller) {
        addChildController(controller);
        controller.attachToParentView(viewPager);
        bottomBar.addItem(controller.bottomBarItem);
        viewPager.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        PlayerController playerController = new PlayerController(context);
        navigationController.pushController(playerController, new FadeInTransition());
    }

    @Override
    public void onBottomBarItemSelected(BottomBarItem item) {
        for (int i = 0; i < childControllers.size(); i++) {
            Controller childController = childControllers.get(i);
            if (childController.bottomBarItem == item) {
                viewPager.setCurrentItem(i, false);
                break;
            }
        }
    }

    @Override
    public void onPageSelected(int position) {
        Controller controller = childControllers.get(position);
        bottomBar.setActive(controller.bottomBarItem, true);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public void transition(Controller from, Controller to, boolean pushing, ControllerTransition controllerTransition) {
//        super.transition(from, to, pushing, controllerTransition);

        if (to instanceof PlayerController) {
            playerBar.animate()
                    .translationY(playerBar.getHeight())
                    .setDuration(350)
                    .setInterpolator(new DecelerateInterpolator(2.5f))
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            playerBar.setVisibility(View.GONE);
                        }
                    })
                    .start();
        } else if (from instanceof PlayerController) {
            playerBar.setVisibility(View.VISIBLE);
            playerBar.animate()
                    .translationY(0f)
                    .setDuration(350)
                    .setInterpolator(new DecelerateInterpolator(2.5f))
                    .start();
        }
    }

    private class PagedNavigationControllerAdapter extends ViewPagerAdapter {
        @Override
        public View getView(int position, ViewGroup parent) {
            return childControllers.get(position).view;
        }

        @Override
        public int getCount() {
            return childControllers.size();
        }
    }
}
