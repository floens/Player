package org.floens.mpv.egl;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

public interface EGLRenderer {
    void create(EGLContext eglContext, EGL10 egl, EGLDisplay display);

    void bind(EGLSurface surface, int width, int height);

    void resize(int width, int height);

    void unbind();

    void destroy();
}
