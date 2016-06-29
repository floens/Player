package org.floens.player.egl;

import android.opengl.Matrix;
import android.util.Log;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_NO_ERROR;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_TRUE;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGenBuffers;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetError;
import static android.opengl.GLES20.glGetProgramInfoLog;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderInfoLog;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

public class DummyRenderer implements EGLRenderer {
    private static final String TAG = "DummyRenderer";

    private static final String VERTEX_SHADER = "" +
            "uniform mat4 uProj;\n" +
            "uniform mat4 uView;\n" +
            "attribute vec2 aPosition;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "    gl_Position = uProj * uView * vec4(aPosition, 0.0, 1.0);\n" +
            "}";

    private static final String FRAGMENT_SHADER = "" +
            "precision mediump float;" +
            "void main()\n" +
            "{\n" +
            "    gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);\n" +
            "}";

    private EGLContext eglContext;
    private EGL10 egl;
    private EGLDisplay display;
    private EGLSurface surface;

    private final Object lock = new Object();
    private boolean drawing = false;
    private int program;
    private int triangleBuffer;

    public DummyRenderer() {
    }

    @Override
    public void create(EGLContext eglContext, EGL10 egl, EGLDisplay display) {
        this.eglContext = eglContext;
        this.egl = egl;
        this.display = display;
    }

    @Override
    public void destroy() {
        this.egl = null;
    }

    @Override
    public void bind(EGLSurface surface) {
        this.surface = surface;

        if (!egl.eglMakeCurrent(display, surface, surface, eglContext)) {
            Log.e(TAG, "eglMakeCurrent failed");
        }

        program = glCreateProgram();
        int vertexShader = createShader(VERTEX_SHADER, GL_VERTEX_SHADER);
        int fragmentShader = createShader(FRAGMENT_SHADER, GL_FRAGMENT_SHADER);
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
        checkGlError("glAttachShader");
        glLinkProgram(program);
        checkGlError("glLinkProgram");

        int[] linkStatus = new int[1];
        glGetProgramiv(program, GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GL_TRUE) {
            Log.e(TAG, "Error linking program: " + glGetProgramInfoLog(program));
        }

        glUseProgram(program);
        checkGlError("glUseProgram");

        final float w = 0.02f;
        float[] vertices = {
                -w, 0.5f,
                w, 0.5f,
                w, -0.5f,
                -w, 0.5f,
                w, -0.5f,
                -w, -0.5f
        };

        FloatBuffer buffer = FloatBuffer.wrap(vertices);

        int[] buffers = new int[1];
        glGenBuffers(1, buffers, 0);
        glBindBuffer(GL_ARRAY_BUFFER, buffers[0]);
        glBufferData(GL_ARRAY_BUFFER, 6 * 2 * 4, buffer, GL_STATIC_DRAW);
        checkGlError("glBufferData");

        int positionAttr = glGetAttribLocation(program, "aPosition");
        checkGlError("glGetAttribLocation");
        glEnableVertexAttribArray(positionAttr);
        checkGlError("glEnableVertexAttribArray");

        glVertexAttribPointer(positionAttr, 2, GL_FLOAT, false, 0, 0);
        checkGlError("glVertexAttribPointer");

        triangleBuffer = buffers[0];

        if (!egl.eglMakeCurrent(display, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT)) {
            Log.e(TAG, "eglMakeCurrent unbind failed");
        }

        synchronized (lock) {
            drawing = true;
        }

        new Thread(drawLoop).start();
    }

    @Override
    public void unbind() {
        synchronized (lock) {
            drawing = false;
        }
    }

    private Runnable drawLoop = new Runnable() {
        private long lastReportTime = System.nanoTime();
        private int frames = 0;

        private float[] projMatrix = new float[16];
        private float[] viewMatrix = new float[16];

        @Override
        public void run() {
            if (!egl.eglMakeCurrent(display, surface, surface, eglContext)) {
                Log.e(TAG, "eglMakeCurrent failed");
            }

            int proj = glGetUniformLocation(program, "uProj");
            Matrix.perspectiveM(projMatrix, 0, 45f, 1080f / 1920f, 1f, 10f);
            Matrix.translateM(projMatrix, 0, 0f, 0f, -3f);
            glUniformMatrix4fv(proj, 1, false, projMatrix, 0);

            int view = glGetUniformLocation(program, "uView");
            Matrix.setIdentityM(viewMatrix, 0);

            while (true) {
                glClearColor(0f, 0f, 0.2f, 1f);
                glClear(GL_COLOR_BUFFER_BIT);

                glBindBuffer(GL_ARRAY_BUFFER, triangleBuffer);

                Matrix.rotateM(viewMatrix, 0, -10f, 0f, 0f, 1f);

                glUniformMatrix4fv(view, 1, false, viewMatrix, 0);

                long x = System.nanoTime();
                glDrawArrays(GL_TRIANGLES, 0, 6);
                checkGlError("glDrawArrays");
//                Log.i(TAG, "took: " + ((System.nanoTime() - x) / 1_000_000d) + "ms");

                synchronized (lock) {
                    if (!drawing) {
                        break;
                    }
                }

                if (!egl.eglSwapBuffers(display, surface)) {
                    Log.e(TAG, EGLHelper.formatEglError("eglSwapBuffers", egl.eglGetError()));
                }

                frames++;

                long now = System.nanoTime();
                if (now - lastReportTime >= 1_000_000_000) {
                    Log.d(TAG, "frames: " + frames + "/s");
                    frames = 0;
                    lastReportTime += 1_000_000_000;
                }
            }
        }
    };

    private int createShader(String source, int type) {
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);
        int[] statusRes = new int[1];
        glGetShaderiv(shader, GL_COMPILE_STATUS, statusRes, 0);
        if (statusRes[0] != GL_TRUE) {
            String status = glGetShaderInfoLog(shader);
            Log.e(TAG, "Error compiling shader: " + status);
            return -1;
        }
        return shader;
    }

    private void checkGlError(String op) {
        int error = glGetError();
        if (error != GL_NO_ERROR) {
            String msg = op + ": glError 0x" + Integer.toHexString(error);
            Log.e(TAG, msg);
            throw new RuntimeException(msg);
        }
    }
}
