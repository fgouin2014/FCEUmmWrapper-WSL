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
        Log.i(TAG, "🎨 **DIAGNOSTIC** Initialisation EmulatorView");
        
        setEGLContextClientVersion(2);
        renderer = new EmulatorRenderer();
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        
        // **CORRECTION** : Restaurer l'aspect ratio correct
        setLayoutParams(new android.view.ViewGroup.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
        ));
        
        Log.i(TAG, "🎨 **DIAGNOSTIC** EmulatorView initialisée - FORCÉ plein écran");
        
        // **DIAGNOSTIC** : Vérifier la visibilité
        Log.i(TAG, "🎨 **DIAGNOSTIC** Visibilité: " + getVisibility());
    }
    
    public void updateFrame(byte[] frameData, int width, int height) {
        renderer.updateFrame(frameData, width, height);
        requestRender();
    }
    
    /**
     * **NOUVEAU** : Forcer le redimensionnement de l'émulation
     */
    public void forceResize() {
        requestLayout();
        invalidate();
        requestRender();
        
        // **NOUVEAU** : Forcer le redimensionnement de la surface OpenGL
        if (renderer != null) {
            // Attendre que le layout soit appliqué
            post(new Runnable() {
                @Override
                public void run() {
                    // Forcer le recalcul de la surface OpenGL
                    requestLayout();
                    invalidate();
                    requestRender();
                    
                    Log.i(TAG, "✅ Émulation forcée au redimensionnement - Nouveau viewport: " + 
                          getWidth() + "x" + getHeight());
                }
            });
        }
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
            
        // **TEST** : Shader simple pour diagnostic
        private static final String TEST_VERTEX_SHADER =
            "attribute vec4 position;\n" +
            "void main() {\n" +
            "  gl_Position = position;\n" +
            "}\n";
            
        private static final String TEST_FRAGMENT_SHADER =
            "precision mediump float;\n" +
            "void main() {\n" +
            "  gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0); // Blanc (plus visible)\n" +
            "}\n";
        
        private int program;
        private int testProgram; // **TEST** : Programme de test
        private int textureId;
        private FloatBuffer vertexBuffer;
        private FloatBuffer texCoordBuffer;
        private byte[] currentFrame;
        private int frameWidth = 256;
        private int frameHeight = 240;
        private boolean frameUpdated = false;
        
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            Log.i(TAG, "🎨 **DIAGNOSTIC** Surface OpenGL créée");
            
            try {
                // **DEBUG COMPLET** : Créer le programme shader avec validation
                int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
                int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
                
                // **VALIDATION** : Vérifier les shaders
                if (vertexShader == 0 || fragmentShader == 0) {
                    throw new RuntimeException("Erreur compilation shader");
                }
                
                program = createProgram(vertexShader, fragmentShader);
                
                // **VALIDATION** : Vérifier le programme
                if (program == 0) {
                    throw new RuntimeException("Erreur liaison programme");
                }
                
                // **TEST** : Créer le programme de test
                int testVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, TEST_VERTEX_SHADER);
                int testFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, TEST_FRAGMENT_SHADER);
                testProgram = createProgram(testVertexShader, testFragmentShader);
                
                Log.i(TAG, "✅ Shaders compilés et liés avec succès");
                Log.i(TAG, "🎨 **TEST** Programme de test créé: " + testProgram);
                
            } catch (Exception e) {
                Log.e(TAG, "❌ Erreur création surface OpenGL: " + e.getMessage());
                // Fallback vers mode de récupération
                initFallbackRendering();
            }
            
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
            Log.i(TAG, "🎨 **100% RETROARCH** Surface redimensionnée: " + width + "x" + height);
            
            // **100% RETROARCH AUTHENTIQUE** : Positionner l'écran de jeu en haut comme RetroArch
            float aspectRatio = (float) width / height;
            float gameAspectRatio = 256.0f / 240.0f; // 4:3 pour NES
            
            // **100% RETROARCH AUTHENTIQUE** : Détecter l'orientation
            boolean isPortrait = height > width;
            
            // **100% RETROARCH AUTHENTIQUE** : Calculer la zone de jeu (haut de l'écran)
            float gameZoneHeight;
            if (isPortrait) {
                // **100% RETROARCH AUTHENTIQUE** : En portrait, utiliser 70% de la hauteur pour le jeu
                gameZoneHeight = height * 0.7f;
                Log.i(TAG, "📱 **100% RETROARCH AUTHENTIQUE** - Mode portrait: Zone de jeu = 70% de la hauteur");
            } else {
                // **100% RETROARCH AUTHENTIQUE** : En landscape, utiliser 100% de la hauteur pour le jeu
                gameZoneHeight = height;
                Log.i(TAG, "🖥️ **100% RETROARCH AUTHENTIQUE** - Mode landscape: Zone de jeu = 100% de la hauteur");
            }
            
            // **100% RETROARCH AUTHENTIQUE** : Calculer les dimensions du jeu dans la zone
            float gameAspectRatioInZone = gameAspectRatio;
            float gameWidth, gameHeight;
            
            if (aspectRatio > gameAspectRatioInZone) {
                // Écran plus large - utiliser toute la hauteur de la zone de jeu
                gameHeight = gameZoneHeight;
                gameWidth = gameHeight * gameAspectRatioInZone;
            } else {
                // Écran plus haut - utiliser toute la largeur
                gameWidth = width;
                gameHeight = gameWidth / gameAspectRatioInZone;
            }
            
            // **100% RETROARCH AUTHENTIQUE** : Centrer horizontalement et positionner en haut
            int offsetX = Math.max(0, (int)((width - gameWidth) / 2));
            // **100% RETROARCH AUTHENTIQUE** : En OpenGL, (0,0) est en bas à gauche, donc pour positionner en haut
            int offsetY = (int)(height - gameHeight); // Positionner en haut de l'écran
            
            // **100% RETROARCH AUTHENTIQUE** : Définir le viewport pour l'écran de jeu
            gl.glViewport(offsetX, offsetY, (int)gameWidth, (int)gameHeight);
            
            Log.i(TAG, "🎮 **100% RETROARCH AUTHENTIQUE** - Écran de jeu positionné en haut: " + 
                  offsetX + "," + offsetY + "," + (int)gameWidth + "x" + (int)gameHeight);
            
            // **100% RETROARCH AUTHENTIQUE** : Configurer les vertices pour le rendu
            float scaleX = 1.0f;
            float scaleY = 1.0f;
            
            float[] vertices = {
                -scaleX, -scaleY, 0.0f,
                 scaleX, -scaleY, 0.0f,
                 scaleX,  scaleY, 0.0f,
                -scaleX,  scaleY, 0.0f
            };
            
            vertexBuffer.clear();
            vertexBuffer.put(vertices);
            vertexBuffer.position(0);
            
            Log.i(TAG, "✅ **100% RETROARCH AUTHENTIQUE** - Configuration complète: " +
                  "Zone de jeu = " + (int)gameZoneHeight + "px, " +
                  "Jeu = " + (int)gameWidth + "x" + (int)gameHeight + "px, " +
                  "Espace contrôles = " + (int)(height - gameZoneHeight) + "px");
        }
        
        @Override
        public void onDrawFrame(GL10 gl) {
            // **CRITIQUE** : Effacer avec une couleur de fond différente pour voir le carré
            gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f); // Fond noir
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
            
            // **DIAGNOSTIC** : Log réduit pour éviter le spam
            if (currentFrame == null) {
                Log.d(TAG, "🎨 **DIAGNOSTIC** onDrawFrame - Frame: null");
            }
            
            // **CORRECTION** : Rendu de test simple pour voir l'image
            if (currentFrame != null && frameUpdated && currentFrame.length > 0) {
                
                // **RENDU AVEC LOGS DE PERFORMANCE**
                long renderStart = System.nanoTime();
                
                // Mettre à jour la texture avec les nouvelles données
                gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
                gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, frameWidth, frameHeight, 
                               0, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, 
                               ByteBuffer.wrap(currentFrame));
                frameUpdated = false;
                
                // **CORRECTION** : Rendu de test simple pour voir l'image
                // Log supprimé pour éviter le spam
                
                // **TEST** : Rendu simple sans texture pour voir si les vertices fonctionnent
                // Log supprimé pour éviter le spam
                
                // **TEST** : Utiliser le programme de test pour voir si les vertices fonctionnent
                GLES20.glUseProgram(testProgram);
                
                int testPositionHandle = GLES20.glGetAttribLocation(testProgram, "position");
                
                if (testPositionHandle != -1) {
                    GLES20.glEnableVertexAttribArray(testPositionHandle);
                    GLES20.glVertexAttribPointer(testPositionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
                    gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, 4);
                    GLES20.glDisableVertexAttribArray(testPositionHandle);
                    // Log supprimé pour éviter le spam
                } else {
                    Log.e(TAG, "❌ **TEST** Handle position invalide: " + testPositionHandle);
                }
                
                // Dessiner le quad avec la texture seulement si on a des données
                GLES20.glUseProgram(program);
                
                int positionHandle = GLES20.glGetAttribLocation(program, "position");
                int texCoordHandle = GLES20.glGetAttribLocation(program, "texCoord");
                int textureHandle = GLES20.glGetUniformLocation(program, "texture");
                
                // **VALIDATION** : Vérifier les handles
                if (positionHandle == -1 || texCoordHandle == -1 || textureHandle == -1) {
                    Log.e(TAG, "❌ **CORRECTION** Handles invalides - position: " + positionHandle + 
                          " - texCoord: " + texCoordHandle + " - texture: " + textureHandle);
                    return;
                }
                
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
                
                // Log supprimé pour éviter le spam
                
                long renderEnd = System.nanoTime();
                // Log supprimé pour éviter le spam
                
            } else {
                Log.w(TAG, "⚠️ Frame ignorée - données invalides");
            }
        }
        
        public void updateFrame(byte[] frameData, int width, int height) {
            this.currentFrame = frameData;
            this.frameWidth = width;
            this.frameHeight = height;
            this.frameUpdated = true;
            
            // **DIAGNOSTIC** : Log réduit pour éviter le spam
            if (currentFrame == null || !frameUpdated) {
                // Log supprimé pour éviter le spam
            }
        }
        
        // **FALLBACK** : Mode de récupération en cas d'erreur OpenGL
        private void initFallbackRendering() {
            Log.w(TAG, "⚠️ Initialisation du mode de récupération");
            // Mode de récupération simple - affichage noir
            program = 0; // Pas de programme shader
        }
        
        // **GESTION D'ERREURS OPENGL ROBUSTE**
        private int loadShader(int type, String shaderCode) {
            int shader = GLES20.glCreateShader(type);
            GLES20.glShaderSource(shader, shaderCode);
            GLES20.glCompileShader(shader);
            
            // **VALIDATION** : Vérifier la compilation
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            
            if (compiled[0] == 0) {
                String error = GLES20.glGetShaderInfoLog(shader);
                Log.e(TAG, "❌ Erreur compilation shader: " + error);
                GLES20.glDeleteShader(shader);
                return 0;
            }
            
            Log.d(TAG, "✅ Shader compilé avec succès");
            return shader;
        }
        
        private int createProgram(int vertexShader, int fragmentShader) {
            int program = GLES20.glCreateProgram();
            GLES20.glAttachShader(program, vertexShader);
            GLES20.glAttachShader(program, fragmentShader);
            GLES20.glLinkProgram(program);
            
            // **VALIDATION** : Vérifier la liaison
            int[] linked = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linked, 0);
            
            if (linked[0] == 0) {
                String error = GLES20.glGetProgramInfoLog(program);
                Log.e(TAG, "❌ Erreur liaison programme: " + error);
                GLES20.glDeleteProgram(program);
                return 0;
            }
            
            Log.d(TAG, "✅ Programme lié avec succès");
            return program;
        }
    }
} 