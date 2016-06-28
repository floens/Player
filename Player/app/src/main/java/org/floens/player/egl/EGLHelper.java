package org.floens.player.egl;

import android.opengl.EGL14;
import android.util.Log;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

public class EGLHelper {
    private static final String TAG = "EGLHelper";

    private static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

    private static int CLIENT_VERSION = 2;

    public EGLDisplay initialize(EGL10 egl) {
        EGLDisplay eglDisplay = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        if (eglDisplay == EGL10.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetDisplay failed");
        }
        int[] version = new int[2];
        if (!egl.eglInitialize(eglDisplay, version)) {
            throw new RuntimeException("eglInitialize failed");
        }
        Log.i(TAG, "initialized egl version " + version[0] + "." + version[1]);
        return eglDisplay;
    }

    public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
        int redSize = 8;
        int greenSize = 8;
        int blueSize = 8;
        int alphaSize = 0;
        int depthSize = 0;
        int stencilSize = 0;

        int[] configSpec = new int[]{
                EGL10.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL10.EGL_RED_SIZE, redSize,
                EGL10.EGL_GREEN_SIZE, greenSize,
                EGL10.EGL_BLUE_SIZE, blueSize,
                EGL10.EGL_ALPHA_SIZE, alphaSize,
                EGL10.EGL_DEPTH_SIZE, depthSize,
                EGL10.EGL_STENCIL_SIZE, stencilSize,
                EGL10.EGL_NONE};

        int[] numberOfConfigs = new int[1];
        if (!egl.eglChooseConfig(display, configSpec, null, 0, numberOfConfigs)) {
            throwEglException(egl, "eglChooseConfig#1");
        }
        Log.i(TAG, "We have " + numberOfConfigs[0] + " configs available in total");
        EGLConfig[] configs = new EGLConfig[numberOfConfigs[0]];
        if (!egl.eglChooseConfig(display, configSpec, configs, configs.length, numberOfConfigs)) {
            throwEglException(egl, "eglChooseConfig#2");
        }

        EGLConfig chosenConfig = null;
        Log.i(TAG, "Choosing between " + configs.length + " configs");
        for (EGLConfig config : configs) {
            int d = findConfigAttrib(egl, display, config, EGL10.EGL_DEPTH_SIZE, 0);
            int s = findConfigAttrib(egl, display, config, EGL10.EGL_STENCIL_SIZE, 0);
            int r = findConfigAttrib(egl, display, config, EGL10.EGL_RED_SIZE, 0);
            int g = findConfigAttrib(egl, display, config, EGL10.EGL_GREEN_SIZE, 0);
            int b = findConfigAttrib(egl, display, config, EGL10.EGL_BLUE_SIZE, 0);
            int a = findConfigAttrib(egl, display, config, EGL10.EGL_ALPHA_SIZE, 0);
            Log.i(TAG, "choosing d = " + d + ", s = " + s + ", r = " + r + ", g = " + g + ", b = " + b + ", a = " + a);
            if (d >= depthSize && s >= stencilSize && r == redSize && g == greenSize && b == blueSize && a == alphaSize) {
                chosenConfig = config;
                break;
            }
        }

        if (chosenConfig == null) {
            throw new RuntimeException("Could not choose egl config");
        }

        return chosenConfig;
    }

    public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig config) {
        int[] spec = {
                EGL_CONTEXT_CLIENT_VERSION, CLIENT_VERSION,
                EGL10.EGL_NONE};

        EGLContext context = egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, spec);
        if (context == null || context == EGL10.EGL_NO_CONTEXT) {
            throwEglException(egl, "createContext");
        }
        return context;
    }

    public EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display, EGLConfig config, Object nativeWindow) {
        EGLSurface eglSurface = egl.eglCreateWindowSurface(display, config, nativeWindow, null);
        if (eglSurface == null || eglSurface == EGL10.EGL_NO_SURFACE) {
            throwEglException(egl, "eglCreateWindowSurface");
        }
        return eglSurface;
    }

    public void destroySurface(EGL10 egl, EGLDisplay display, EGLSurface nativeWindow) {
        if (!egl.eglDestroySurface(display, nativeWindow)) {
            throwEglException(egl, "eglDestroySurface");
        }
    }

    public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
        if (!egl.eglDestroyContext(display, context)) {
            throwEglException(egl, "eglDestroyContext");
        }
    }

    public void terminate(EGL10 egl, EGLDisplay display) {
        if (!egl.eglTerminate(display)) {
            throwEglException(egl, "eglTerminate");
        }
    }

    public static void throwEglException(EGL10 egl, String function) {
        throwEglException(function, egl.eglGetError());
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
            case EGL10.EGL_SUCCESS:
                return "EGL_SUCCESS";
            case EGL10.EGL_NOT_INITIALIZED:
                return "EGL_NOT_INITIALIZED";
            case EGL10.EGL_BAD_ACCESS:
                return "EGL_BAD_ACCESS";
            case EGL10.EGL_BAD_ALLOC:
                return "EGL_BAD_ALLOC";
            case EGL10.EGL_BAD_ATTRIBUTE:
                return "EGL_BAD_ATTRIBUTE";
            case EGL10.EGL_BAD_CONFIG:
                return "EGL_BAD_CONFIG";
            case EGL10.EGL_BAD_CONTEXT:
                return "EGL_BAD_CONTEXT";
            case EGL10.EGL_BAD_CURRENT_SURFACE:
                return "EGL_BAD_CURRENT_SURFACE";
            case EGL10.EGL_BAD_DISPLAY:
                return "EGL_BAD_DISPLAY";
            case EGL10.EGL_BAD_MATCH:
                return "EGL_BAD_MATCH";
            case EGL10.EGL_BAD_NATIVE_PIXMAP:
                return "EGL_BAD_NATIVE_PIXMAP";
            case EGL10.EGL_BAD_NATIVE_WINDOW:
                return "EGL_BAD_NATIVE_WINDOW";
            case EGL10.EGL_BAD_PARAMETER:
                return "EGL_BAD_PARAMETER";
            case EGL10.EGL_BAD_SURFACE:
                return "EGL_BAD_SURFACE";
            case EGL11.EGL_CONTEXT_LOST:
                return "EGL_CONTEXT_LOST";
            default:
                return "0x" + Integer.toHexString(error);
        }
    }

    private int findConfigAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attribute, int defaultValue) {
        int[] value = new int[1];
        if (egl.eglGetConfigAttrib(display, config, attribute, value)) {
            return value[0];
        }
        return defaultValue;
    }
}
