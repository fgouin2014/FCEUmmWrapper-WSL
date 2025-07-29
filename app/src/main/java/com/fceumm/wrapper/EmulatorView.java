package com.fceumm.wrapper;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLES20;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class EmulatorView extends GLSurfaceView {
    private static final String TAG = "EmulatorView";
    private EmulatorRenderer renderer;
    
    static {
        System.loadLibrary("fceummwrapper");
    }
    
    public EmulatorView(Context context) {
        super(context);
        init();
    }
    
    public EmulatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        setEGLContextClientVersion(2);
        renderer = new EmulatorRenderer();
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        Log.i(TAG, "EmulatorView initialisée");
    }
    
    public void updateFrame(byte[] frameData, int width, int height) {
        renderer.updateFrame(frameData, width, height);
        requestRender();
    }
    
    // Méthodes natives
    public native byte[] getFrameBuffer();
    public native int getFrameWidth();
    public native int getFrameHeight();
    public native boolean isFrameUpdated();
    
    private static class EmulatorRenderer implements GLSurfaceView.Renderer {
        private static final String TAG = "EmulatorRenderer";
        
        // Shaders OpenGL
        private static final String VERTEX_SHADER =
            "attribute vec4 position;\n" +
            "attribute vec2 texCoord;\n" +
            "varying vec2 vTexCoord;\n" +
            "void main() {\n" +
            "  gl_Position = position;\n" +
            "  vTexCoord = texCoord;\n" +
            "}\n";
            
        private static final String FRAGMENT_SHADER =
            "precision mediump float;\n" +
            "uniform sampler2D texture;\n" +
            "varying vec2 vTexCoord;\n" +
            "void main() {\n" +
            "  gl_FragColor = texture2D(texture, vTexCoord);\n" +
            "}\n";
        
        private int program;
        private int textureId;
        private FloatBuffer vertexBuffer;
        private FloatBuffer texCoordBuffer;
        private byte[] currentFrame;
        private int frameWidth = 256;
        private int frameHeight = 240;
        private boolean frameUpdated = false;
        
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            Log.i(TAG, "Surface OpenGL créée");
            
            // Créer le programme shader
            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
            int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
            
            program = createProgram(vertexShader, fragmentShader);
            
            // Créer les buffers
            float[] vertices = {
                -1.0f, -1.0f, 0.0f,
                 1.0f, -1.0f, 0.0f,
                 1.0f,  1.0f, 0.0f,
                -1.0f,  1.0f, 0.0f
            };
            
            float[] texCoords = {
                0.0f, 1.0f,
                1.0f, 1.0f,
                1.0f, 0.0f,
                0.0f, 0.0f
            };
            
            vertexBuffer = ByteBuffer.allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
            vertexBuffer.put(vertices);
            vertexBuffer.position(0);
            
            texCoordBuffer = ByteBuffer.allocateDirect(texCoords.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
            texCoordBuffer.put(texCoords);
            texCoordBuffer.position(0);
            
            // Créer la texture
            int[] textures = new int[1];
            gl.glGenTextures(1, textures, 0);
            textureId = textures[0];
            
            gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        }
        
        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            Log.i(TAG, "Surface redimensionnée: " + width + "x" + height);
            gl.glViewport(0, 0, width, height);
            
            // Calculer le ratio d'aspect pour maintenir 256x240
            float aspectRatio = (float) width / height;
            float nesAspectRatio = 256.0f / 240.0f;
            
            // Ajuster les vertices pour maintenir le ratio d'aspect
            float scaleX = 1.0f;
            float scaleY = 1.0f;
            
            if (aspectRatio > nesAspectRatio) {
                // L'écran est plus large que nécessaire
                scaleX = nesAspectRatio / aspectRatio;
            } else {
                // L'écran est plus haut que nécessaire
                scaleY = aspectRatio / nesAspectRatio;
            }
            
            float[] vertices = {
                -scaleX, -scaleY, 0.0f,
                 scaleX, -scaleY, 0.0f,
                 scaleX,  scaleY, 0.0f,
                -scaleX,  scaleY, 0.0f
            };
            
            vertexBuffer.clear();
            vertexBuffer.put(vertices);
            vertexBuffer.position(0);
        }
        
        @Override
        public void onDrawFrame(GL10 gl) {
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
            
            if (currentFrame != null && frameUpdated) {
                // Mettre à jour la texture avec les nouvelles données
                gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
                gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, frameWidth, frameHeight, 
                               0, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, 
                               ByteBuffer.wrap(currentFrame));
                frameUpdated = false;
                
                // Dessiner le quad avec la texture seulement si on a des données
                GLES20.glUseProgram(program);
                
                int positionHandle = GLES20.glGetAttribLocation(program, "position");
                int texCoordHandle = GLES20.glGetAttribLocation(program, "texCoord");
                int textureHandle = GLES20.glGetUniformLocation(program, "texture");
                
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
                GLES20.glUniform1i(textureHandle, 0);
                
                GLES20.glEnableVertexAttribArray(positionHandle);
                GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
                
                GLES20.glEnableVertexAttribArray(texCoordHandle);
                GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer);
                
                gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, 4);
                
                GLES20.glDisableVertexAttribArray(positionHandle);
                GLES20.glDisableVertexAttribArray(texCoordHandle);
            }
        }
        
        public void updateFrame(byte[] frameData, int width, int height) {
            this.currentFrame = frameData;
            this.frameWidth = width;
            this.frameHeight = height;
            this.frameUpdated = true;
        }
        
        private int loadShader(int type, String shaderCode) {
            int shader = GLES20.glCreateShader(type);
            GLES20.glShaderSource(shader, shaderCode);
            GLES20.glCompileShader(shader);
            return shader;
        }
        
        private int createProgram(int vertexShader, int fragmentShader) {
            int program = GLES20.glCreateProgram();
            GLES20.glAttachShader(program, vertexShader);
            GLES20.glAttachShader(program, fragmentShader);
            GLES20.glLinkProgram(program);
            return program;
        }
    }
} 