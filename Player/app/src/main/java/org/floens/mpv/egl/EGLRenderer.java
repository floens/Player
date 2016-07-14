package org.floens.mpv.egl;

import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;

public interface EGLRenderer {
    void create(EGLContext eglContext, EGLDisplay display);

    void bind(EGLSurface surface, int width, int height);

    void resize(int width, int height);

    void unbind();

    void destroy();
}
