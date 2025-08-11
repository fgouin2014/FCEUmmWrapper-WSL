#ifndef RETROARCH_VIDEO_MANAGER_H
#define RETROARCH_VIDEO_MANAGER_H

#include <GLES3/gl3.h>
#include <EGL/egl.h>
#include <android/native_window.h>
#include <jni.h>
#include <android/log.h>
#include <mutex>
#include <atomic>
#include <vector>

// **100% RETROARCH NATIF** : Gestionnaire vidéo conforme aux standards RetroArch
class RetroArchVideoManager {
private:
    // EGL objects
    EGLDisplay eglDisplay;
    EGLContext eglContext;
    EGLSurface eglSurface;
    EGLConfig eglConfig;
    
    // OpenGL objects
    GLuint program;
    GLuint vertexShader;
    GLuint fragmentShader;
    GLuint vbo;
    GLuint vao;
    GLuint texture;
    
    // Video settings
    static const int DEFAULT_WIDTH = 256;
    static const int DEFAULT_HEIGHT = 240;
    static const int MAX_WIDTH = 1920;
    static const int MAX_HEIGHT = 1080;
    
    struct VideoSettings {
        int width;
        int height;
        int aspectRatio;
        int filter;
        bool vsync;
        bool fullscreen;
        int scale;
        int rotation;
        bool bilinear;
        bool integerScale;
    };
    
    VideoSettings settings;
    std::atomic<bool> videoEnabled;
    std::atomic<bool> videoInitialized;
    std::mutex videoMutex;
    
    // Frame buffer
    uint8_t* frameBuffer;
    size_t frameBufferSize;
    
    // Shader sources
    static const char* vertexShaderSource;
    static const char* fragmentShaderSource;
    
    // Initialization methods
    bool initializeEGL(ANativeWindow* window);
    bool initializeOpenGL();
    void cleanupEGL();
    void cleanupOpenGL();
    
    // Shader methods
    bool compileShader(GLuint shader, const char* source);
    bool linkProgram(GLuint program);
    void checkGLError(const char* operation);
    
    // Rendering methods
    void setupViewport();
    void renderFrame();
    void updateTexture();
    
public:
    RetroArchVideoManager();
    ~RetroArchVideoManager();
    
    // **100% RETROARCH** : Interface conforme aux standards RetroArch
    bool initialize(ANativeWindow* window);
    void shutdown();
    
    // Video control
    void setEnabled(bool enabled);
    void setResolution(int width, int height);
    void setAspectRatio(int ratio);
    void setFilter(int filter);
    void setVSync(bool vsync);
    void setFullscreen(bool fullscreen);
    void setScale(int scale);
    void setRotation(int rotation);
    void setBilinear(bool bilinear);
    void setIntegerScale(bool integerScale);
    
    // Video processing
    void updateFrame(const uint8_t* data, int width, int height);
    void render();
    void swapBuffers();
    
    // Getters
    bool isEnabled() const { return videoEnabled.load(); }
    bool isInitialized() const { return videoInitialized.load(); }
    int getWidth() const { return settings.width; }
    int getHeight() const { return settings.height; }
    int getAspectRatio() const { return settings.aspectRatio; }
    int getFilter() const { return settings.filter; }
    bool isVSyncEnabled() const { return settings.vsync; }
    bool isFullscreen() const { return settings.fullscreen; }
    int getScale() const { return settings.scale; }
    int getRotation() const { return settings.rotation; }
    bool isBilinear() const { return settings.bilinear; }
    bool isIntegerScale() const { return settings.integerScale; }
    
    // **100% RETROARCH** : Callback pour libretro
    static void videoRefreshCallback(const void* data, unsigned width, unsigned height, size_t pitch);
    
    // Singleton pattern
    static RetroArchVideoManager& getInstance();
};

// **100% RETROARCH** : Interface JNI pour Java
extern "C" {
    JNIEXPORT jboolean JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_initVideo(JNIEnv* env, jobject thiz, jobject surface);
    JNIEXPORT void JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_shutdownVideo(JNIEnv* env, jobject thiz);
    JNIEXPORT void JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_setVideoEnabled(JNIEnv* env, jobject thiz, jboolean enabled);
    JNIEXPORT void JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_setResolution(JNIEnv* env, jobject thiz, jint width, jint height);
    JNIEXPORT void JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_setAspectRatio(JNIEnv* env, jobject thiz, jint ratio);
    JNIEXPORT void JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_setFilter(JNIEnv* env, jobject thiz, jint filter);
    JNIEXPORT void JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_setVSync(JNIEnv* env, jobject thiz, jboolean vsync);
    JNIEXPORT void JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_setFullscreen(JNIEnv* env, jobject thiz, jboolean fullscreen);
    JNIEXPORT void JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_setScale(JNIEnv* env, jobject thiz, jint scale);
    JNIEXPORT void JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_setRotation(JNIEnv* env, jobject thiz, jint rotation);
    JNIEXPORT void JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_setBilinear(JNIEnv* env, jobject thiz, jboolean bilinear);
    JNIEXPORT void JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_setIntegerScale(JNIEnv* env, jobject thiz, jboolean integerScale);
    JNIEXPORT jboolean JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_isVideoEnabled(JNIEnv* env, jobject thiz);
    JNIEXPORT jboolean JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_isVideoInitialized(JNIEnv* env, jobject thiz);
    JNIEXPORT jint JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_getWidth(JNIEnv* env, jobject thiz);
    JNIEXPORT jint JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_getHeight(JNIEnv* env, jobject thiz);
    JNIEXPORT jint JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_getAspectRatio(JNIEnv* env, jobject thiz);
    JNIEXPORT jint JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_getFilter(JNIEnv* env, jobject thiz);
    JNIEXPORT jboolean JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_isVSyncEnabled(JNIEnv* env, jobject thiz);
    JNIEXPORT jboolean JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_isFullscreen(JNIEnv* env, jobject thiz);
    JNIEXPORT jint JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_getScale(JNIEnv* env, jobject thiz);
    JNIEXPORT jint JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_getRotation(JNIEnv* env, jobject thiz);
    JNIEXPORT jboolean JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_isBilinear(JNIEnv* env, jobject thiz);
    JNIEXPORT jboolean JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_isIntegerScale(JNIEnv* env, jobject thiz);
}

#endif // RETROARCH_VIDEO_MANAGER_H
