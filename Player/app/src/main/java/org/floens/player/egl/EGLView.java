package org.floens.player.egl;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

public class EGLView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "EGLView";

    private static final int STATE_DESTROYED = 0;
    private static final int STATE_CREATED = 1;
    private static final int STATE_BOUND = 2;

    private EGLHelper eglHelper;
    private int state = STATE_DESTROYED;

    private EGLRenderer renderer;

    private EGL10 egl;
    private EGLDisplay display;
    private EGLConfig config;
    private EGLContext eglContext;
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

        surfaceWidth = width;
        surfaceHeight = height;

        getHolder().setFixedSize(surfaceWidth, surfaceHeight);

        goToState(STATE_BOUND);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed() called with: " + "holder = [" + holder + "]");
        goToState(STATE_CREATED);
    }

    private void goToState(int newState) {
        if (state != newState) {
            Log.i(TAG, "go to state " + newState);
            if (newState > state) {
                if (newState >= STATE_CREATED && state < STATE_CREATED) {
                    create();
                    renderer.create(eglContext, egl, display);
                    state = STATE_CREATED;
                }
                if (newState >= STATE_BOUND && state < STATE_BOUND) {
                    bind();
                    renderer.bind(windowSurface);
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
        display = eglHelper.initialize(egl);
        config = eglHelper.chooseConfig(egl, display);
        eglContext = eglHelper.createContext(egl, display, config);
    }

    private void destroy() {
        Log.d(TAG, "destroy()");
        eglHelper.destroyContext(egl, display, eglContext);
        eglContext = null;
        eglHelper.terminate(egl, display);
        display = null;
        egl = null;
        config = null;
    }

    private void bind() {
        Log.d(TAG, "bind()");

        windowSurface = eglHelper.createWindowSurface(egl, display, config, getHolder());

        GL10 gl10 = (GL10) eglContext.getGL();
        String glVersion = gl10.glGetString(GL10.GL_VERSION);
        Log.i(TAG, "glVersion: " + glVersion);
    }

    private void unbind() {
        Log.d(TAG, "unbind()");

        eglHelper.destroySurface(egl, display, windowSurface);

        windowSurface = null;
    }
}
