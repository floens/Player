package org.floens.mpv.egl;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class EGLView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "EGLView";

    private static final int STATE_DESTROYED = 0;
    private static final int STATE_CREATED = 1;
    private static final int STATE_BOUND = 2;

    private EGLHelper eglHelper;
    private int state = STATE_DESTROYED;

    private EGLRenderer renderer;

    private EGLDisplay display;
    private EGLConfig config;
    private EGLContext eglContext;
    private EGLSurface windowSurface;
    private int surfaceWidth;
    private int surfaceHeight;

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
    }

    public void setRenderer(EGLRenderer renderer) {
        this.renderer = renderer;
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
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged() called with: " + "holder = [" + holder + "], format = [" + format + "], width = [" + width + "], height = [" + height + "]");

        boolean sendResize = false;
        if (width != surfaceWidth || height != surfaceHeight) {
            surfaceWidth = width;
            surfaceHeight = height;
            // Only send a resize() to the renderer if it was already bound
            // Otherwise bind() gives the correct dimensions
            if (state == STATE_BOUND) {
                sendResize = true;
            }
        }

//        getHolder().setFixedSize(surfaceWidth, surfaceHeight);

        goToState(STATE_BOUND);

        if (sendResize) {
            renderer.resize(surfaceWidth, surfaceHeight);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed() called with: " + "holder = [" + holder + "]");
        goToState(STATE_CREATED);
    }

    private void goToState(int newState) {
        if (state != newState) {
            if (newState > state) {
                if (newState >= STATE_CREATED && state < STATE_CREATED) {
                    create();
                    renderer.create(eglContext, display);
                    state = STATE_CREATED;
                }
                if (newState >= STATE_BOUND && state < STATE_BOUND) {
                    bind();
                    renderer.bind(windowSurface, surfaceWidth, surfaceHeight);
                    state = STATE_BOUND;
                }
            } else {
                if (newState <= STATE_CREATED && state > STATE_CREATED) {
                    renderer.unbind();
                    unbind();
                    state = STATE_CREATED;
                }
                if (newState <= STATE_DESTROYED && state > STATE_DESTROYED) {
                    renderer.destroy();
                    destroy();
                    state = STATE_DESTROYED;
                }
            }
        }
    }

    private void create() {
        Log.d(TAG, "create()");
        display = eglHelper.initialize();
        config = eglHelper.chooseConfig(display);
        eglContext = eglHelper.createContext(display, config);
    }

    private void destroy() {
        Log.d(TAG, "destroy()");
        eglHelper.destroyContext(display, eglContext);
        eglContext = null;
        eglHelper.terminate(display);
        display = null;
        config = null;
    }

    private void bind() {
        Log.d(TAG, "bind()");

        windowSurface = eglHelper.createWindowSurface(display, config, getHolder());
        if (!EGL14.eglMakeCurrent(display, windowSurface, windowSurface, eglContext)) {
            Log.e(TAG, "eglMakeCurrent failed");
        }

        String glVersion = GLES20.glGetString(GLES20.GL_VERSION);
        Log.i(TAG, "GL_VERSION: " + glVersion);
    }

    private void unbind() {
        Log.d(TAG, "unbind()");

        eglHelper.destroySurface(display, windowSurface);

        windowSurface = null;
    }
}
