package org.floens.player.egl;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLES20;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;

public class EGLView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "EGLView";

    private static final int STATE_DESTROYED = 0;
    private static final int STATE_CREATED = 1;
    private static final int STATE_BOUND = 2;

    private EGLHelper eglHelper;
    private int state = STATE_DESTROYED;

    private EGL10 egl;
    private EGLDisplay display;
    private EGLConfig config;
    private EGLContext context;
    private EGLSurface windowSurface;
    private int surfaceWidth = 0;
    private int surfaceHeight = 0;

    public EGLView(Context context) {
        this(context, null);
    }

    public EGLView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EGLView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        SurfaceHolder holder = getHolder();
        holder.setFormat(PixelFormat.RGB_888);
        holder.addCallback(this);

        eglHelper = new EGLHelper();
        egl = (EGL10) EGLContext.getEGL();
        if (egl == null) {
            throw new RuntimeException("Could not get EGL context");
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        goToState(STATE_CREATED);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        goToState(STATE_DESTROYED);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated() called with: " + "holder = [" + holder + "]");

        goToState(STATE_BOUND);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged() called with: " + "holder = [" + holder + "], format = [" + format + "], width = [" + width + "], height = [" + height + "]");

        surfaceWidth = width;
        surfaceHeight = height;

        getHolder().setFixedSize(surfaceWidth, surfaceHeight);
        drawAndSwap();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed() called with: " + "holder = [" + holder + "]");
        goToState(STATE_CREATED);
    }

    private void drawAndSwap() {
        glViewport(0, 0, surfaceWidth, surfaceHeight);
        glClearColor(1f, 0f, 0f, 1f);
        glClear(GLES20.GL_COLOR_BUFFER_BIT);

        if (!egl.eglSwapBuffers(display, windowSurface)) {
            Log.e(TAG, EGLHelper.formatEglError("eglSwapBuffers", egl.eglGetError()));
        }
    }

    private void goToState(int newState) {
        if (state != newState) {
            Log.i(TAG, "go to state " + newState);
            if (newState > state) {
                if (newState >= STATE_CREATED && state < STATE_CREATED) {
                    create();
                    state = STATE_CREATED;
                }
                if (newState >= STATE_BOUND && state < STATE_BOUND) {
                    bind();
                    state = STATE_BOUND;
                }
            } else {
                if (newState <= STATE_CREATED && state > STATE_CREATED) {
                    unbind();
                    state = STATE_CREATED;
                }
                if (newState <= STATE_DESTROYED && state > STATE_DESTROYED) {
                    destroy();
                    state = STATE_DESTROYED;
                }
            }
        }
    }

    private void create() {
        Log.d(TAG, "create()");
        display = eglHelper.initialize(egl);
        config = eglHelper.chooseConfig(egl, display);
        context = eglHelper.createContext(egl, display, config);
    }

    private void destroy() {
        Log.d(TAG, "destroy()");
        eglHelper.destroyContext(egl, display, context);
        context = null;
        eglHelper.terminate(egl, display);
        display = null;
        egl = null;
        config = null;
    }

    private void bind() {
        Log.d(TAG, "bind()");

        windowSurface = eglHelper.createWindowSurface(egl, display, config, getHolder());

        if (!egl.eglMakeCurrent(display, windowSurface, windowSurface, context)) {
            Log.e(TAG, "eglMakeCurrent failed");
        }

        GL gl = context.getGL();
        Log.i(TAG, "gl: " + gl);

        for (Class<?> aClass : gl.getClass().getInterfaces()) {
            Log.i(TAG, "surfaceCreated: " + aClass);
        }

        if (gl instanceof GLES20) {
            Log.i(TAG, "GLES20");
        }

        GL10 gl10 = (GL10) gl;
        String glVersion = gl10.glGetString(GL10.GL_VERSION);
        Log.i(TAG, "glVersion: " + glVersion);
    }

    private void unbind() {
        Log.d(TAG, "unbind()");

        egl.eglMakeCurrent(display, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        eglHelper.destroySurface(egl, display, windowSurface);

        windowSurface = null;
    }
}
