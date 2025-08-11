#include "RetroArchVideoManager.h"
#include <android/log.h>
#include <android/native_window_jni.h>

#define LOG_TAG "RetroArchVideoManager"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Shader sources pour le rendu vidéo RetroArch
const char* RetroArchVideoManager::vertexShaderSource = R"(
#version 300 es
precision mediump float;

layout(location = 0) in vec2 aPosition;
layout(location = 1) in vec2 aTexCoord;

out vec2 vTexCoord;

void main() {
    gl_Position = vec4(aPosition, 0.0, 1.0);
    vTexCoord = aTexCoord;
}
)";

const char* RetroArchVideoManager::fragmentShaderSource = R"(
#version 300 es
precision mediump float;

in vec2 vTexCoord;
out vec4 fragColor;

uniform sampler2D uTexture;
uniform float uAspectRatio;
uniform float uScaleX;
uniform float uScaleY;

void main() {
    vec2 texCoord = vTexCoord;
    
    // Application des paramètres de mise à l'échelle
    texCoord.x *= uScaleX;
    texCoord.y *= uScaleY;
    
    // Gestion du ratio d'aspect
    if (uAspectRatio > 1.0) {
        texCoord.x = (texCoord.x - 0.5) / uAspectRatio + 0.5;
    } else {
        texCoord.y = (texCoord.y - 0.5) * uAspectRatio + 0.5;
    }
    
    fragColor = texture(uTexture, texCoord);
}
)";

RetroArchVideoManager::RetroArchVideoManager()
    : eglDisplay(EGL_NO_DISPLAY)
    , eglContext(EGL_NO_CONTEXT)
    , eglSurface(EGL_NO_SURFACE)
    , eglConfig(nullptr)
    , program(0)
    , vertexShader(0)
    , fragmentShader(0)
    , vbo(0)
    , vao(0)
    , texture(0)
    , frameBuffer(nullptr)
    , frameBufferSize(0)
    , videoEnabled(false)
    , videoInitialized(false)
{
    // Initialisation des paramètres vidéo par défaut
    settings.width = DEFAULT_WIDTH;
    settings.height = DEFAULT_HEIGHT;
    settings.aspectRatio = 4; // 4:3
    settings.filter = 1; // LINEAR
    settings.vsync = true;
    settings.fullscreen = false;
    settings.scale = 1;
    settings.rotation = 0;
    settings.bilinear = true;
    settings.integerScale = false;
    
    LOGI("RetroArchVideoManager: Constructeur appelé");
}

RetroArchVideoManager::~RetroArchVideoManager() {
    shutdown();
}

RetroArchVideoManager& RetroArchVideoManager::getInstance() {
    static RetroArchVideoManager instance;
    return instance;
}

bool RetroArchVideoManager::initialize(ANativeWindow* window) {
    if (videoInitialized.load()) {
        LOGI("RetroArchVideoManager: Déjà initialisé");
        return true;
    }

    LOGI("RetroArchVideoManager: Initialisation...");
    
    if (!window) {
        LOGE("RetroArchVideoManager: Fenêtre native invalide");
        return false;
    }

    if (!initializeEGL(window)) {
        LOGE("RetroArchVideoManager: Échec de l'initialisation EGL");
        return false;
    }

    if (!initializeOpenGL()) {
        LOGE("RetroArchVideoManager: Échec de l'initialisation OpenGL");
        cleanupEGL();
        return false;
    }

    videoInitialized.store(true);
    videoEnabled.store(true);
    LOGI("RetroArchVideoManager: Initialisation réussie");
    return true;
}

void RetroArchVideoManager::shutdown() {
    if (!videoInitialized.load()) {
        return;
    }

    LOGI("RetroArchVideoManager: Arrêt...");
    
    cleanupOpenGL();
    cleanupEGL();
    
    videoInitialized.store(false);
    videoEnabled.store(false);
    LOGI("RetroArchVideoManager: Arrêt terminé");
}

bool RetroArchVideoManager::initializeEGL(ANativeWindow* window) {
    LOGI("RetroArchVideoManager: Initialisation EGL...");
    
    eglDisplay = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if (eglDisplay == EGL_NO_DISPLAY) {
        LOGE("RetroArchVideoManager: Impossible d'obtenir le display EGL");
        return false;
    }

    EGLint majorVersion, minorVersion;
    if (!eglInitialize(eglDisplay, &majorVersion, &minorVersion)) {
        LOGE("RetroArchVideoManager: Impossible d'initialiser EGL");
        return false;
    }

    LOGI("RetroArchVideoManager: EGL version %d.%d", majorVersion, minorVersion);

    // Configuration EGL
    const EGLint configAttribs[] = {
        EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
        EGL_RED_SIZE, 8,
        EGL_GREEN_SIZE, 8,
        EGL_BLUE_SIZE, 8,
        EGL_ALPHA_SIZE, 8,
        EGL_DEPTH_SIZE, 0,
        EGL_STENCIL_SIZE, 0,
        EGL_NONE
    };

    EGLConfig config;
    EGLint numConfigs;
    if (!eglChooseConfig(eglDisplay, configAttribs, &config, 1, &numConfigs)) {
        LOGE("RetroArchVideoManager: Impossible de choisir la configuration EGL");
        return false;
    }

    // Contexte EGL
    const EGLint contextAttribs[] = {
        EGL_CONTEXT_CLIENT_VERSION, 3,
        EGL_NONE
    };

    eglContext = eglCreateContext(eglDisplay, config, EGL_NO_CONTEXT, contextAttribs);
    if (eglContext == EGL_NO_CONTEXT) {
        LOGE("RetroArchVideoManager: Impossible de créer le contexte EGL");
        return false;
    }

    // Surface EGL
    eglSurface = eglCreateWindowSurface(eglDisplay, config, window, nullptr);
    if (eglSurface == EGL_NO_SURFACE) {
        LOGE("RetroArchVideoManager: Impossible de créer la surface EGL");
        return false;
    }

    if (!eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
        LOGE("RetroArchVideoManager: Impossible de rendre le contexte EGL actuel");
        return false;
    }

    // Obtenir les dimensions de la fenêtre
    EGLint width, height;
    eglQuerySurface(eglDisplay, eglSurface, EGL_WIDTH, &width);
    eglQuerySurface(eglDisplay, eglSurface, EGL_HEIGHT, &height);
    
    LOGI("RetroArchVideoManager: Fenêtre %dx%d", width, height);
    
    return true;
}

bool RetroArchVideoManager::initializeOpenGL() {
    LOGI("RetroArchVideoManager: Initialisation OpenGL...");
    
    // Compilation des shaders
    vertexShader = glCreateShader(GL_VERTEX_SHADER);
    if (!compileShader(vertexShader, vertexShaderSource)) {
        return false;
    }

    fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
    if (!compileShader(fragmentShader, fragmentShaderSource)) {
        glDeleteShader(vertexShader);
        return false;
    }

    // Création du programme
    program = glCreateProgram();
    glAttachShader(program, vertexShader);
    glAttachShader(program, fragmentShader);
    glLinkProgram(program);

    GLint linkStatus;
    glGetProgramiv(program, GL_LINK_STATUS, &linkStatus);
    if (linkStatus == GL_FALSE) {
        GLint infoLogLength;
        glGetProgramiv(program, GL_INFO_LOG_LENGTH, &infoLogLength);
        if (infoLogLength > 0) {
            std::vector<GLchar> infoLog(infoLogLength);
            glGetProgramInfoLog(program, infoLogLength, nullptr, infoLog.data());
            LOGE("RetroArchVideoManager: Échec de liaison du programme: %s", infoLog.data());
        }
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
        return false;
    }

    glDeleteShader(vertexShader);
    glDeleteShader(fragmentShader);

    // Création de la texture
    glGenTextures(1, &texture);
    glBindTexture(GL_TEXTURE_2D, texture);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

    // Création du VBO et VAO
    glGenVertexArrays(1, &vao);
    glGenBuffers(1, &vbo);

    glBindVertexArray(vao);
    glBindBuffer(GL_ARRAY_BUFFER, vbo);

    // Vertices pour un quad plein écran
    float vertices[] = {
        // Position (x, y)    // TexCoord (u, v)
        -1.0f, -1.0f,         0.0f, 1.0f,
         1.0f, -1.0f,         1.0f, 1.0f,
         1.0f,  1.0f,         1.0f, 0.0f,
        -1.0f,  1.0f,         0.0f, 0.0f
    };

    glBufferData(GL_ARRAY_BUFFER, sizeof(vertices), vertices, GL_STATIC_DRAW);

    // Attributs de position
    glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(float), (void*)0);
    glEnableVertexAttribArray(0);

    // Attributs de texture
    glVertexAttribPointer(1, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(float), (void*)(2 * sizeof(float)));
    glEnableVertexAttribArray(1);

    glBindVertexArray(0);

    // Configuration OpenGL
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    LOGI("RetroArchVideoManager: OpenGL initialisé avec succès");
    return true;
}

bool RetroArchVideoManager::compileShader(GLuint shader, const char* source) {
    glShaderSource(shader, 1, &source, nullptr);
    glCompileShader(shader);

    GLint compileStatus;
    glGetShaderiv(shader, GL_COMPILE_STATUS, &compileStatus);
    if (compileStatus == GL_FALSE) {
        GLint infoLogLength;
        glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLogLength);
        if (infoLogLength > 0) {
            std::vector<GLchar> infoLog(infoLogLength);
            glGetShaderInfoLog(shader, infoLogLength, nullptr, infoLog.data());
            LOGE("RetroArchVideoManager: Échec de compilation du shader: %s", infoLog.data());
        }
        return false;
    }

    return true;
}

void RetroArchVideoManager::cleanupOpenGL() {
    if (vao != 0) {
        glDeleteVertexArrays(1, &vao);
        vao = 0;
    }
    
    if (vbo != 0) {
        glDeleteBuffers(1, &vbo);
        vbo = 0;
    }
    
    if (texture != 0) {
        glDeleteTextures(1, &texture);
        texture = 0;
    }
    
    if (program != 0) {
        glDeleteProgram(program);
        program = 0;
    }
}

void RetroArchVideoManager::cleanupEGL() {
    if (eglDisplay != EGL_NO_DISPLAY) {
        eglMakeCurrent(eglDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        
        if (eglSurface != EGL_NO_SURFACE) {
            eglDestroySurface(eglDisplay, eglSurface);
            eglSurface = EGL_NO_SURFACE;
        }
        
        if (eglContext != EGL_NO_CONTEXT) {
            eglDestroyContext(eglDisplay, eglContext);
            eglContext = EGL_NO_CONTEXT;
        }
        
        eglTerminate(eglDisplay);
        eglDisplay = EGL_NO_DISPLAY;
    }
}

void RetroArchVideoManager::updateFrame(const uint8_t* data, int width, int height) {
    if (!videoInitialized.load() || !data) {
        return;
    }

    settings.width = width;
    settings.height = height;

    glBindTexture(GL_TEXTURE_2D, texture);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);
}

void RetroArchVideoManager::render() {
    if (!videoInitialized.load()) {
        return;
    }

    glClear(GL_COLOR_BUFFER_BIT);

    glUseProgram(program);
    glBindVertexArray(vao);

    // Uniforms
    GLint aspectRatioLocation = glGetUniformLocation(program, "uAspectRatio");
    GLint scaleXLocation = glGetUniformLocation(program, "uScaleX");
    GLint scaleYLocation = glGetUniformLocation(program, "uScaleY");

    float aspectRatio = settings.aspectRatio / 3.0f; // Convertir ratio
    glUniform1f(aspectRatioLocation, aspectRatio);
    glUniform1f(scaleXLocation, settings.scale);
    glUniform1f(scaleYLocation, settings.scale);

    glDrawArrays(GL_TRIANGLE_FAN, 0, 4);

    glBindVertexArray(0);
    glUseProgram(0);
}

void RetroArchVideoManager::swapBuffers() {
    if (videoInitialized.load() && eglDisplay != EGL_NO_DISPLAY && eglSurface != EGL_NO_SURFACE) {
        eglSwapBuffers(eglDisplay, eglSurface);
    }
}

// Setters
void RetroArchVideoManager::setEnabled(bool enabled) {
    videoEnabled.store(enabled);
    LOGI("RetroArchVideoManager: Vidéo %s", enabled ? "activée" : "désactivée");
}

void RetroArchVideoManager::setResolution(int width, int height) {
    settings.width = width;
    settings.height = height;
    LOGI("RetroArchVideoManager: Résolution définie à %dx%d", width, height);
}

void RetroArchVideoManager::setAspectRatio(int ratio) {
    settings.aspectRatio = ratio;
    LOGI("RetroArchVideoManager: Ratio d'aspect défini à %d", ratio);
}

void RetroArchVideoManager::setFilter(int filter) {
    settings.filter = filter;
    
    glBindTexture(GL_TEXTURE_2D, texture);
    GLint glFilter = (filter == 1) ? GL_LINEAR : GL_NEAREST;
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, glFilter);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, glFilter);
    
    LOGI("RetroArchVideoManager: Filtre défini à %d", filter);
}

void RetroArchVideoManager::setVSync(bool vsync) {
    settings.vsync = vsync;
    
    if (eglDisplay != EGL_NO_DISPLAY) {
        eglSwapInterval(eglDisplay, vsync ? 1 : 0);
    }
    
    LOGI("RetroArchVideoManager: V-Sync %s", vsync ? "activé" : "désactivé");
}

void RetroArchVideoManager::setFullscreen(bool fullscreen) {
    settings.fullscreen = fullscreen;
    LOGI("RetroArchVideoManager: Plein écran %s", fullscreen ? "activé" : "désactivé");
}

void RetroArchVideoManager::setScale(int scale) {
    settings.scale = scale;
    LOGI("RetroArchVideoManager: Échelle définie à %d", scale);
}

void RetroArchVideoManager::setRotation(int rotation) {
    settings.rotation = rotation;
    LOGI("RetroArchVideoManager: Rotation définie à %d", rotation);
}

void RetroArchVideoManager::setBilinear(bool bilinear) {
    settings.bilinear = bilinear;
    LOGI("RetroArchVideoManager: Bilinéaire %s", bilinear ? "activé" : "désactivé");
}

void RetroArchVideoManager::setIntegerScale(bool integerScale) {
    settings.integerScale = integerScale;
    LOGI("RetroArchVideoManager: Échelle entière %s", integerScale ? "activée" : "désactivée");
}

// Callback RetroArch
void RetroArchVideoManager::videoRefreshCallback(const void* data, unsigned width, unsigned height, size_t pitch) {
    RetroArchVideoManager& manager = getInstance();
    manager.updateFrame(static_cast<const uint8_t*>(data), width, height);
    manager.render();
    manager.swapBuffers();
}

// JNI Functions
extern "C" {

JNIEXPORT jboolean JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_initVideo(JNIEnv* env, jobject thiz, jobject surface) {
    ANativeWindow* window = ANativeWindow_fromSurface(env, surface);
    if (!window) {
        LOGE("RetroArchVideoManager JNI: Impossible d'obtenir la fenêtre native");
        return JNI_FALSE;
    }

    bool result = RetroArchVideoManager::getInstance().initialize(window);
    ANativeWindow_release(window);
    
    return result ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_shutdownVideo(JNIEnv* env, jobject thiz) {
    RetroArchVideoManager::getInstance().shutdown();
}

JNIEXPORT void JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_setVideoEnabled(JNIEnv* env, jobject thiz, jboolean enabled) {
    RetroArchVideoManager::getInstance().setEnabled(enabled);
}

JNIEXPORT void JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_setResolution(JNIEnv* env, jobject thiz, jint width, jint height) {
    RetroArchVideoManager::getInstance().setResolution(width, height);
}

JNIEXPORT void JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_setAspectRatio(JNIEnv* env, jobject thiz, jint ratio) {
    RetroArchVideoManager::getInstance().setAspectRatio(ratio);
}

JNIEXPORT void JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_setFilter(JNIEnv* env, jobject thiz, jint filter) {
    RetroArchVideoManager::getInstance().setFilter(filter);
}

JNIEXPORT void JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_setVSync(JNIEnv* env, jobject thiz, jboolean vsync) {
    RetroArchVideoManager::getInstance().setVSync(vsync);
}

JNIEXPORT void JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_setFullscreen(JNIEnv* env, jobject thiz, jboolean fullscreen) {
    RetroArchVideoManager::getInstance().setFullscreen(fullscreen);
}

JNIEXPORT void JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_setScale(JNIEnv* env, jobject thiz, jint scale) {
    RetroArchVideoManager::getInstance().setScale(scale);
}

JNIEXPORT void JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_setRotation(JNIEnv* env, jobject thiz, jint rotation) {
    RetroArchVideoManager::getInstance().setRotation(rotation);
}

JNIEXPORT void JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_setBilinear(JNIEnv* env, jobject thiz, jboolean bilinear) {
    RetroArchVideoManager::getInstance().setBilinear(bilinear);
}

JNIEXPORT void JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_setIntegerScale(JNIEnv* env, jobject thiz, jboolean integerScale) {
    RetroArchVideoManager::getInstance().setIntegerScale(integerScale);
}

JNIEXPORT jboolean JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_isVideoEnabled(JNIEnv* env, jobject thiz) {
    return RetroArchVideoManager::getInstance().isEnabled() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_isVideoInitialized(JNIEnv* env, jobject thiz) {
    return RetroArchVideoManager::getInstance().isInitialized() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jint JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_getWidth(JNIEnv* env, jobject thiz) {
    return RetroArchVideoManager::getInstance().getWidth();
}

JNIEXPORT jint JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_getHeight(JNIEnv* env, jobject thiz) {
    return RetroArchVideoManager::getInstance().getHeight();
}

JNIEXPORT jint JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_getAspectRatio(JNIEnv* env, jobject thiz) {
    return RetroArchVideoManager::getInstance().getAspectRatio();
}

JNIEXPORT jint JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_getFilter(JNIEnv* env, jobject thiz) {
    return RetroArchVideoManager::getInstance().getFilter();
}

JNIEXPORT jboolean JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_isVSyncEnabled(JNIEnv* env, jobject thiz) {
    return RetroArchVideoManager::getInstance().isVSyncEnabled() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_isFullscreen(JNIEnv* env, jobject thiz) {
    return RetroArchVideoManager::getInstance().isFullscreen() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jint JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_getScale(JNIEnv* env, jobject thiz) {
    return RetroArchVideoManager::getInstance().getScale();
}

JNIEXPORT jint JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_getRotation(JNIEnv* env, jobject thiz) {
    return RetroArchVideoManager::getInstance().getRotation();
}

JNIEXPORT jboolean JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_isBilinear(JNIEnv* env, jobject thiz) {
    return RetroArchVideoManager::getInstance().isBilinear() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_fceumm_wrapper_video_RetroArchVideoManager_isIntegerScale(JNIEnv* env, jobject thiz) {
    return RetroArchVideoManager::getInstance().isIntegerScale() ? JNI_TRUE : JNI_FALSE;
}

} // extern "C"
