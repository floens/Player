package org.floens.mpv.renderer;

import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;

import org.floens.mpv.MpvCore;
import org.floens.mpv.egl.EGLRenderer;

public class MpvRenderer implements EGLRenderer {
    private static final String TAG = "MpvRenderer";

    private final MpvCore mpvCore;
    private final Callback callback;

    private EGLContext eglContext;
    private EGLDisplay display;
    private EGLSurface surface;

    public MpvRenderer(MpvCore mpvCore, Callback callback) {
        this.mpvCore = mpvCore;
        this.callback = callback;
    }

    @Override
    public void create(EGLContext eglContext, EGLDisplay display) {
        this.eglContext = eglContext;
        this.display = display;
    }

    @Override
    public void destroy() {
        this.eglContext = null;
        this.display = null;
    }

    @Override
    public void bind(EGLSurface surface, int width, int height) {
        this.surface = surface;
        // will unbind the egl surface from this thread
        mpvCore.bind(width, height);

        if (callback != null) {
            callback.mpvRendererBound();
        }
    }

    @Override
    public void resize(int width, int height) {
        mpvCore.resize(width, height);
    }

    @Override
    public void unbind() {
        if (callback != null) {
            callback.mpvRendererUnbound();
        }

        mpvCore.unbind();
        this.surface = null;
    }

    public interface Callback {
        void mpvRendererBound();

        void mpvRendererUnbound();
    }
}
