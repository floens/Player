package org.floens.mpv.egl;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.util.Log;

import javax.microedition.khronos.egl.EGL10;

public class EGLHelper {
    private static final String TAG = "EGLHelper";

    private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
    private static final int CLIENT_VERSION = 2;

    public EGLDisplay initialize() {
        EGL10 egl10 = (EGL10) javax.microedition.khronos.egl.EGLContext.getEGL();
        javax.microedition.khronos.egl.EGLDisplay egl10Display = egl10.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        String versionString = egl10.eglQueryString(egl10Display, EGL10.EGL_VERSION);
        Log.i(TAG, "EGL_VERSION: " + versionString);
        EGLDisplay eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);

        if (eglDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetDisplay failed");
        }

        int[] major = new int[1];
        int[] minor = new int[1];
        if (!EGL14.eglInitialize(eglDisplay, major, 0, minor, 0)) {
            throw new RuntimeException("eglInitialize failed");
        }
        if (minor[0] < 4) {
            throw new RuntimeException("EGL 1.4 required");
        }
        Log.i(TAG, "Initialized EGL version " + major[0] + "." + minor[0]);
        return eglDisplay;
    }

    public EGLConfig chooseConfig(EGLDisplay display) {
        int redSize = 8;
        int greenSize = 8;
        int blueSize = 8;
        int alphaSize = 0;
        int depthSize = 8;
        int stencilSize = 0;

        int[] configSpec = new int[]{
                EGL10.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_RED_SIZE, redSize,
                EGL14.EGL_GREEN_SIZE, greenSize,
                EGL14.EGL_BLUE_SIZE, blueSize,
                EGL14.EGL_ALPHA_SIZE, alphaSize,
                EGL14.EGL_DEPTH_SIZE, depthSize,
                EGL14.EGL_STENCIL_SIZE, stencilSize,
                EGL14.EGL_NONE};

        int[] numberOfConfigs = new int[1];
        EGLConfig[] configs = new EGLConfig[100];
        // Spec conflict: spec allows passing NULL to the configs to see how many configs there
        // are for the attribs, but the wrapper throws illegalargumentexception instead.
        // Call it with a huge number instead as workaround.
        if (!EGL14.eglChooseConfig(display, configSpec, 0, configs, 0, 100, numberOfConfigs, 0)) {
            throwEglException("eglChooseConfig#1");
        }
        Log.i(TAG, "We have " + numberOfConfigs[0] + " matching configs available in total");
        configs = new EGLConfig[numberOfConfigs[0]];
        if (!EGL14.eglChooseConfig(display, configSpec, 0, configs, 0, configs.length, numberOfConfigs, 0)) {
            throwEglException("eglChooseConfig#2");
        }

        EGLConfig chosenConfig = null;
        for (EGLConfig config : configs) {
            int d = findConfigAttrib(display, config, EGL14.EGL_DEPTH_SIZE, 0);
            int s = findConfigAttrib(display, config, EGL14.EGL_STENCIL_SIZE, 0);
            int r = findConfigAttrib(display, config, EGL14.EGL_RED_SIZE, 0);
            int g = findConfigAttrib(display, config, EGL14.EGL_GREEN_SIZE, 0);
            int b = findConfigAttrib(display, config, EGL14.EGL_BLUE_SIZE, 0);
            int a = findConfigAttrib(display, config, EGL14.EGL_ALPHA_SIZE, 0);
            if (d >= depthSize && s >= stencilSize && r == redSize && g == greenSize && b == blueSize && a == alphaSize) {
//                Log.i(TAG, "choosing d = " + d + ", s = " + s + ", r = " + r + ", g = " + g + ", b = " + b + ", a = " + a);
                chosenConfig = config;
                break;
            }
        }

        if (chosenConfig == null) {
            throw new RuntimeException("Could not choose egl config");
        }

        return chosenConfig;
    }

    public EGLContext createContext(EGLDisplay display, EGLConfig config) {
        int[] spec = {
                EGL_CONTEXT_CLIENT_VERSION, CLIENT_VERSION,
                EGL14.EGL_NONE};

        EGLContext context = EGL14.eglCreateContext(display, config, EGL14.EGL_NO_CONTEXT, spec, 0);
        if (context == null) {
            throwEglException("createContext");
        }
        return context;
    }

    public EGLSurface createWindowSurface(EGLDisplay display, EGLConfig config, Object nativeWindow) {
        EGLSurface eglSurface = EGL14.eglCreateWindowSurface(display, config, nativeWindow, new int[]{
                EGL14.EGL_NONE
        }, 0);
        if (eglSurface == null) {
            throwEglException("eglCreateWindowSurface");
        }
        return eglSurface;
    }

    public void destroySurface(EGLDisplay display, EGLSurface nativeWindow) {
        if (!EGL14.eglDestroySurface(display, nativeWindow)) {
            throwEglException("eglDestroySurface");
        }
    }

    public void destroyContext(EGLDisplay display, EGLContext context) {
        if (!EGL14.eglDestroyContext(display, context)) {
            throwEglException("eglDestroyContext");
        }
    }

    public void terminate(EGLDisplay display) {
        if (!EGL14.eglTerminate(display)) {
            throwEglException("eglTerminate");
        }
    }

    public static void throwEglException(String function) {
        throwEglException(function, EGL14.eglGetError());
    }

    public static void throwEglException(String function, int error) {
        String message = formatEglError(function, error);
        throw new RuntimeException(message);
    }

    public static String formatEglError(String function, int error) {
        return function + " failed: " + getErrorString(error);
    }

    public static String getErrorString(int error) {
        switch (error) {
            case EGL14.EGL_SUCCESS:
                return "EGL_SUCCESS";
            case EGL14.EGL_NOT_INITIALIZED:
                return "EGL_NOT_INITIALIZED";
            case EGL14.EGL_BAD_ACCESS:
                return "EGL_BAD_ACCESS";
            case EGL14.EGL_BAD_ALLOC:
                return "EGL_BAD_ALLOC";
            case EGL14.EGL_BAD_ATTRIBUTE:
                return "EGL_BAD_ATTRIBUTE";
            case EGL14.EGL_BAD_CONFIG:
                return "EGL_BAD_CONFIG";
            case EGL14.EGL_BAD_CONTEXT:
                return "EGL_BAD_CONTEXT";
            case EGL14.EGL_BAD_CURRENT_SURFACE:
                return "EGL_BAD_CURRENT_SURFACE";
            case EGL14.EGL_BAD_DISPLAY:
                return "EGL_BAD_DISPLAY";
            case EGL14.EGL_BAD_MATCH:
                return "EGL_BAD_MATCH";
            case EGL14.EGL_BAD_NATIVE_PIXMAP:
                return "EGL_BAD_NATIVE_PIXMAP";
            case EGL14.EGL_BAD_NATIVE_WINDOW:
                return "EGL_BAD_NATIVE_WINDOW";
            case EGL14.EGL_BAD_PARAMETER:
                return "EGL_BAD_PARAMETER";
            case EGL14.EGL_BAD_SURFACE:
                return "EGL_BAD_SURFACE";
            case EGL14.EGL_CONTEXT_LOST:
                return "EGL_CONTEXT_LOST";
            default:
                return "0x" + Integer.toHexString(error);
        }
    }

    private int findConfigAttrib(EGLDisplay display, EGLConfig config, int attribute, int defaultValue) {
        int[] value = new int[1];
        if (EGL14.eglGetConfigAttrib(display, config, attribute, value, 0)) {
            return value[0];
        }
        return defaultValue;
    }
}
