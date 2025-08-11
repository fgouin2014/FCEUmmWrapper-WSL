#ifndef RETROARCH_AUDIO_MANAGER_H
#define RETROARCH_AUDIO_MANAGER_H

#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include <jni.h>
#include <android/log.h>
#include <mutex>
#include <vector>
#include <atomic>

// **100% RETROARCH NATIF** : Gestionnaire audio conforme aux standards RetroArch
class RetroArchAudioManager {
private:
    // OpenSL ES objects
    SLObjectItf engineObject;
    SLEngineItf engineEngine;
    SLObjectItf outputMixObject;
    SLObjectItf bqPlayerObject;
    SLPlayItf bqPlayerPlay;
    SLAndroidSimpleBufferQueueItf bqPlayerBufferQueue;
    
    // Audio buffer management
    static const int AUDIO_BUFFER_SIZE = 4096;
    static const int CHANNELS = 2;
    static const int SAMPLE_RATE = 44100;
    
    struct AudioBuffer {
        int16_t buffer[AUDIO_BUFFER_SIZE * CHANNELS];
        size_t writePos;
        size_t readPos;
        size_t size;
        std::mutex mutex;
        bool initialized;
    };
    
    AudioBuffer audioBuffer;
    std::atomic<bool> audioEnabled;
    std::atomic<bool> audioMuted;
    std::atomic<int> masterVolume;
    std::atomic<int> audioQuality;
    std::atomic<int> sampleRate;
    
    // Thread safety
    std::mutex audioMutex;
    
    // Callback for buffer queue
    static void bqPlayerCallback(SLAndroidSimpleBufferQueueItf bq, void *context);
    
    // Audio processing methods
    void processAudioBuffer();
    void applyAudioSettings();
    void initializeOpenSLES();
    void cleanupOpenSLES();
    
public:
    RetroArchAudioManager();
    ~RetroArchAudioManager();
    
    // **100% RETROARCH** : Interface conforme aux standards RetroArch
    bool initialize();
    void shutdown();
    
    // Audio control
    void setEnabled(bool enabled);
    void setMuted(bool muted);
    void setMasterVolume(int volume);
    void setAudioQuality(int quality);
    void setSampleRate(int rate);
    
    // Audio processing
    size_t writeAudioData(const int16_t* data, size_t frames);
    void processAudioFrame();
    
    // Getters
    bool isEnabled() const { return audioEnabled.load(); }
    bool isMuted() const { return audioMuted.load(); }
    int getMasterVolume() const { return masterVolume.load(); }
    int getAudioQuality() const { return audioQuality.load(); }
    int getSampleRate() const { return sampleRate.load(); }
    
    // **100% RETROARCH** : Callback pour libretro
    static size_t audioSampleBatchCallback(const int16_t* data, size_t frames);
    
    // Singleton pattern
    static RetroArchAudioManager& getInstance();
};

// **100% RETROARCH** : Interface JNI pour Java
extern "C" {
    JNIEXPORT jboolean JNICALL Java_com_fceumm_wrapper_audio_RetroArchAudioManager_initAudio(JNIEnv* env, jobject thiz);
    JNIEXPORT void JNICALL Java_com_fceumm_wrapper_audio_RetroArchAudioManager_shutdownAudio(JNIEnv* env, jobject thiz);
    JNIEXPORT void JNICALL Java_com_fceumm_wrapper_audio_RetroArchAudioManager_setAudioEnabledNative(JNIEnv* env, jobject thiz, jboolean enabled);
    JNIEXPORT void JNICALL Java_com_fceumm_wrapper_audio_RetroArchAudioManager_setAudioMutedNative(JNIEnv* env, jobject thiz, jboolean muted);
    JNIEXPORT void JNICALL Java_com_fceumm_wrapper_audio_RetroArchAudioManager_setMasterVolumeNative(JNIEnv* env, jobject thiz, jint volume);
    JNIEXPORT void JNICALL Java_com_fceumm_wrapper_audio_RetroArchAudioManager_setAudioQualityNative(JNIEnv* env, jobject thiz, jint quality);
    JNIEXPORT void JNICALL Java_com_fceumm_wrapper_audio_RetroArchAudioManager_setSampleRateNative(JNIEnv* env, jobject thiz, jint rate);
    JNIEXPORT jboolean JNICALL Java_com_fceumm_wrapper_audio_RetroArchAudioManager_isAudioEnabledNative(JNIEnv* env, jobject thiz);
    JNIEXPORT jboolean JNICALL Java_com_fceumm_wrapper_audio_RetroArchAudioManager_isAudioMutedNative(JNIEnv* env, jobject thiz);
    JNIEXPORT jint JNICALL Java_com_fceumm_wrapper_audio_RetroArchAudioManager_getMasterVolumeNative(JNIEnv* env, jobject thiz);
    JNIEXPORT jint JNICALL Java_com_fceumm_wrapper_audio_RetroArchAudioManager_getAudioQualityNative(JNIEnv* env, jobject thiz);
    JNIEXPORT jint JNICALL Java_com_fceumm_wrapper_audio_RetroArchAudioManager_getSampleRateNative(JNIEnv* env, jobject thiz);
}

#endif // RETROARCH_AUDIO_MANAGER_H
