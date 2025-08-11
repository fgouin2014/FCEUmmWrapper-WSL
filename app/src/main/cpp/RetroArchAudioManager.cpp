#include "RetroArchAudioManager.h"
#include <android/log.h>

#define LOG_TAG "RetroArchAudio"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// **100% RETROARCH NATIF** : Implémentation du gestionnaire audio conforme aux standards RetroArch

RetroArchAudioManager::RetroArchAudioManager() 
    : engineObject(nullptr), engineEngine(nullptr), outputMixObject(nullptr),
      bqPlayerObject(nullptr), bqPlayerPlay(nullptr), bqPlayerBufferQueue(nullptr),
      audioEnabled(false), audioMuted(false), masterVolume(100), 
      audioQuality(1), sampleRate(SAMPLE_RATE) {
    
    // Initialiser le buffer audio
    audioBuffer.writePos = 0;
    audioBuffer.readPos = 0;
    audioBuffer.size = AUDIO_BUFFER_SIZE * CHANNELS;
    audioBuffer.initialized = false;
    
    LOGI("🎵 **100%% RETROARCH** - RetroArchAudioManager cree");
}

RetroArchAudioManager::~RetroArchAudioManager() {
    shutdown();
    LOGI("🎵 **100%% RETROARCH** - RetroArchAudioManager detruit");
}

RetroArchAudioManager& RetroArchAudioManager::getInstance() {
    static RetroArchAudioManager instance;
    return instance;
}

bool RetroArchAudioManager::initialize() {
    std::lock_guard<std::mutex> lock(audioMutex);
    
    LOGI("🎵 **100%% RETROARCH** - Initialisation du gestionnaire audio natif");
    
    try {
        initializeOpenSLES();
        audioEnabled = true;
        audioBuffer.initialized = true;
        
        LOGI("✅ **100%% RETROARCH** - Gestionnaire audio initialise avec succes");
        return true;
    } catch (const std::exception& e) {
        LOGE("❌ **100%% RETROARCH** - Erreur d'initialisation audio: %s", e.what());
        return false;
    }
}

void RetroArchAudioManager::shutdown() {
    std::lock_guard<std::mutex> lock(audioMutex);
    
    LOGI("🎵 **100%% RETROARCH** - Arret du gestionnaire audio");
    
    audioEnabled = false;
    cleanupOpenSLES();
    audioBuffer.initialized = false;
}

void RetroArchAudioManager::initializeOpenSLES() {
    // **100% RETROARCH** : Initialisation OpenSL ES conforme aux standards
    
    // Créer le moteur audio
    SLresult result = slCreateEngine(&engineObject, 0, nullptr, 0, nullptr, nullptr);
    if (result != SL_RESULT_SUCCESS) {
        throw std::runtime_error("Impossible de créer le moteur audio OpenSL ES");
    }
    
    result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
    if (result != SL_RESULT_SUCCESS) {
        throw std::runtime_error("Impossible de réaliser le moteur audio");
    }
    
    result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine);
    if (result != SL_RESULT_SUCCESS) {
        throw std::runtime_error("Impossible d'obtenir l'interface moteur");
    }
    
    // Créer le mixeur de sortie
    result = (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 0, nullptr, nullptr);
    if (result != SL_RESULT_SUCCESS) {
        throw std::runtime_error("Impossible de créer le mixeur de sortie");
    }
    
    result = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);
    if (result != SL_RESULT_SUCCESS) {
        throw std::runtime_error("Impossible de réaliser le mixeur de sortie");
    }
    
    // Configuration du buffer queue player
    SLDataLocator_AndroidSimpleBufferQueue loc_bufq = {
        SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2
    };
    
    SLDataFormat_PCM format_pcm = {
        SL_DATAFORMAT_PCM,
        CHANNELS,
        static_cast<SLuint32>(sampleRate * 1000), // OpenSL ES utilise des milliers
        SL_PCMSAMPLEFORMAT_FIXED_16,
        SL_PCMSAMPLEFORMAT_FIXED_16,
        SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,
        SL_BYTEORDER_LITTLEENDIAN
    };
    
    SLDataSource audioSrc = {&loc_bufq, &format_pcm};
    
    SLDataLocator_OutputMix loc_outmix = {
        SL_DATALOCATOR_OUTPUTMIX, outputMixObject
    };
    
    SLDataSink audioSnk = {&loc_outmix, nullptr};
    
    const SLInterfaceID ids[2] = {
        SL_IID_BUFFERQUEUE, SL_IID_VOLUME
    };
    
    const SLboolean req[2] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};
    
    result = (*engineEngine)->CreateAudioPlayer(engineEngine, &bqPlayerObject, &audioSrc, &audioSnk, 2, ids, req);
    if (result != SL_RESULT_SUCCESS) {
        throw std::runtime_error("Impossible de créer le lecteur audio");
    }
    
    result = (*bqPlayerObject)->Realize(bqPlayerObject, SL_BOOLEAN_FALSE);
    if (result != SL_RESULT_SUCCESS) {
        throw std::runtime_error("Impossible de réaliser le lecteur audio");
    }
    
    result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_PLAY, &bqPlayerPlay);
    if (result != SL_RESULT_SUCCESS) {
        throw std::runtime_error("Impossible d'obtenir l'interface de lecture");
    }
    
    result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_BUFFERQUEUE, &bqPlayerBufferQueue);
    if (result != SL_RESULT_SUCCESS) {
        throw std::runtime_error("Impossible d'obtenir l'interface de buffer queue");
    }
    
    result = (*bqPlayerBufferQueue)->RegisterCallback(bqPlayerBufferQueue, bqPlayerCallback, this);
    if (result != SL_RESULT_SUCCESS) {
        throw std::runtime_error("Impossible d'enregistrer le callback");
    }
    
    // Démarrer la lecture
    result = (*bqPlayerPlay)->SetPlayState(bqPlayerPlay, SL_PLAYSTATE_PLAYING);
    if (result != SL_RESULT_SUCCESS) {
        throw std::runtime_error("Impossible de démarrer la lecture");
    }
    
            LOGI("✅ **100%% RETROARCH** - OpenSL ES initialise avec succes");
}

void RetroArchAudioManager::cleanupOpenSLES() {
    if (bqPlayerObject != nullptr) {
        (*bqPlayerObject)->Destroy(bqPlayerObject);
        bqPlayerObject = nullptr;
        bqPlayerPlay = nullptr;
        bqPlayerBufferQueue = nullptr;
    }
    
    if (outputMixObject != nullptr) {
        (*outputMixObject)->Destroy(outputMixObject);
        outputMixObject = nullptr;
    }
    
    if (engineObject != nullptr) {
        (*engineObject)->Destroy(engineObject);
        engineObject = nullptr;
        engineEngine = nullptr;
    }
    
    LOGI("🎵 **100%% RETROARCH** - OpenSL ES nettoye");
}

void RetroArchAudioManager::bqPlayerCallback(SLAndroidSimpleBufferQueueItf bq, void *context) {
    RetroArchAudioManager* manager = static_cast<RetroArchAudioManager*>(context);
    if (manager && manager->audioEnabled.load()) {
        manager->processAudioBuffer();
    }
}

void RetroArchAudioManager::processAudioBuffer() {
    std::lock_guard<std::mutex> lock(audioBuffer.mutex);
    
    if (!audioBuffer.initialized || audioMuted.load()) {
        // Envoyer du silence
        int16_t silence[AUDIO_BUFFER_SIZE * CHANNELS] = {0};
        (*bqPlayerBufferQueue)->Enqueue(bqPlayerBufferQueue, silence, sizeof(silence));
        return;
    }
    
    // Traiter les données audio du buffer
    size_t available = (audioBuffer.writePos - audioBuffer.readPos + audioBuffer.size) % audioBuffer.size;
    size_t toRead = std::min(available, static_cast<size_t>(AUDIO_BUFFER_SIZE * CHANNELS));
    
    if (toRead > 0) {
        int16_t outputBuffer[AUDIO_BUFFER_SIZE * CHANNELS];
        
        for (size_t i = 0; i < toRead; i++) {
            outputBuffer[i] = audioBuffer.buffer[audioBuffer.readPos];
            audioBuffer.readPos = (audioBuffer.readPos + 1) % audioBuffer.size;
        }
        
        // Appliquer le volume
        int volume = masterVolume.load();
        if (volume < 100) {
            for (size_t i = 0; i < toRead; i++) {
                outputBuffer[i] = (outputBuffer[i] * volume) / 100;
            }
        }
        
        (*bqPlayerBufferQueue)->Enqueue(bqPlayerBufferQueue, outputBuffer, toRead * sizeof(int16_t));
    } else {
        // Buffer vide, envoyer du silence
        int16_t silence[AUDIO_BUFFER_SIZE * CHANNELS] = {0};
        (*bqPlayerBufferQueue)->Enqueue(bqPlayerBufferQueue, silence, sizeof(silence));
    }
}

size_t RetroArchAudioManager::writeAudioData(const int16_t* data, size_t frames) {
    if (!audioEnabled.load() || !audioBuffer.initialized) {
        return 0;
    }
    
    std::lock_guard<std::mutex> lock(audioBuffer.mutex);
    
    size_t samples = frames * CHANNELS;
    size_t written = 0;
    
    for (size_t i = 0; i < samples; i++) {
        size_t nextWrite = (audioBuffer.writePos + 1) % audioBuffer.size;
        
        if (nextWrite != audioBuffer.readPos) {
            audioBuffer.buffer[audioBuffer.writePos] = data[i];
            audioBuffer.writePos = nextWrite;
            written++;
        } else {
            // Buffer plein, ignorer les données
            break;
        }
    }
    
    return written / CHANNELS; // Retourner le nombre de frames écrites
}

void RetroArchAudioManager::setEnabled(bool enabled) {
    audioEnabled.store(enabled);
    LOGI("🎵 **100%% RETROARCH** - Audio %s", enabled ? "active" : "desactive");
}

void RetroArchAudioManager::setMuted(bool muted) {
    audioMuted.store(muted);
    LOGI("🎵 **100%% RETROARCH** - Audio %s", muted ? "muet" : "active");
}

void RetroArchAudioManager::setMasterVolume(int volume) {
    volume = std::max(0, std::min(100, volume));
    masterVolume.store(volume);
    LOGI("🎵 **100%% RETROARCH** - Volume maitre: %d%%", volume);
}

void RetroArchAudioManager::setAudioQuality(int quality) {
    audioQuality.store(quality);
    LOGI("🎵 **100%% RETROARCH** - Qualite audio: %d", quality);
}

void RetroArchAudioManager::setSampleRate(int rate) {
    sampleRate.store(rate);
    LOGI("🎵 **100%% RETROARCH** - Taux d'echantillonnage: %d Hz", rate);
}

// **100% RETROARCH** : Callback pour libretro
size_t RetroArchAudioManager::audioSampleBatchCallback(const int16_t* data, size_t frames) {
    return getInstance().writeAudioData(data, frames);
}

// **100% RETROARCH** : Interface JNI pour Java
extern "C" {

JNIEXPORT jboolean JNICALL Java_com_fceumm_wrapper_audio_RetroArchAudioManager_initAudio(JNIEnv* env, jobject thiz) {
    return RetroArchAudioManager::getInstance().initialize();
}

JNIEXPORT void JNICALL Java_com_fceumm_wrapper_audio_RetroArchAudioManager_shutdownAudio(JNIEnv* env, jobject thiz) {
    RetroArchAudioManager::getInstance().shutdown();
}

JNIEXPORT void JNICALL Java_com_fceumm_wrapper_audio_RetroArchAudioManager_setAudioEnabledNative(JNIEnv* env, jobject thiz, jboolean enabled) {
    RetroArchAudioManager::getInstance().setEnabled(enabled);
}

JNIEXPORT void JNICALL Java_com_fceumm_wrapper_audio_RetroArchAudioManager_setAudioMutedNative(JNIEnv* env, jobject thiz, jboolean muted) {
    RetroArchAudioManager::getInstance().setMuted(muted);
}

JNIEXPORT void JNICALL Java_com_fceumm_wrapper_audio_RetroArchAudioManager_setMasterVolumeNative(JNIEnv* env, jobject thiz, jint volume) {
    RetroArchAudioManager::getInstance().setMasterVolume(volume);
}

JNIEXPORT void JNICALL Java_com_fceumm_wrapper_audio_RetroArchAudioManager_setAudioQualityNative(JNIEnv* env, jobject thiz, jint quality) {
    RetroArchAudioManager::getInstance().setAudioQuality(quality);
}

JNIEXPORT void JNICALL Java_com_fceumm_wrapper_audio_RetroArchAudioManager_setSampleRateNative(JNIEnv* env, jobject thiz, jint rate) {
    RetroArchAudioManager::getInstance().setSampleRate(rate);
}

JNIEXPORT jboolean JNICALL Java_com_fceumm_wrapper_audio_RetroArchAudioManager_isAudioEnabledNative(JNIEnv* env, jobject thiz) {
    return RetroArchAudioManager::getInstance().isEnabled();
}

JNIEXPORT jboolean JNICALL Java_com_fceumm_wrapper_audio_RetroArchAudioManager_isAudioMutedNative(JNIEnv* env, jobject thiz) {
    return RetroArchAudioManager::getInstance().isMuted();
}

JNIEXPORT jint JNICALL Java_com_fceumm_wrapper_audio_RetroArchAudioManager_getMasterVolumeNative(JNIEnv* env, jobject thiz) {
    return RetroArchAudioManager::getInstance().getMasterVolume();
}

JNIEXPORT jint JNICALL Java_com_fceumm_wrapper_audio_RetroArchAudioManager_getAudioQualityNative(JNIEnv* env, jobject thiz) {
    return RetroArchAudioManager::getInstance().getAudioQuality();
}

JNIEXPORT jint JNICALL Java_com_fceumm_wrapper_audio_RetroArchAudioManager_getSampleRateNative(JNIEnv* env, jobject thiz) {
    return RetroArchAudioManager::getInstance().getSampleRate();
}

}
