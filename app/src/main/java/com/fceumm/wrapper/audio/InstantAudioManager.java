package com.fceumm.wrapper.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;
import java.util.concurrent.atomic.AtomicBoolean;

public class InstantAudioManager {
    private static final String TAG = "InstantAudio";
    
    private AudioTrack audioTrack;
    private boolean isInitialized = false;
    private int sampleRate = 44100;
    private int channelConfig = AudioFormat.CHANNEL_OUT_STEREO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int bufferSize;
    
    // Pas de queue - traitement direct
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    
    public InstantAudioManager(Context context) {
        initAudio();
    }
    
    private void initAudio() {
        try {
            // Calculer la taille du buffer
            int minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat);
            if (minBufferSize == AudioTrack.ERROR_BAD_VALUE || minBufferSize == AudioTrack.ERROR) {
                Log.e(TAG, "Erreur lors du calcul de la taille du buffer");
                return;
            }
            
            // Buffer minimal absolu pour la latence zéro
            bufferSize = minBufferSize;
            
            // Créer l'AudioTrack avec des paramètres ultra-agressifs
            audioTrack = new AudioTrack.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build())
                    .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(audioFormat)
                            .setSampleRate(sampleRate)
                            .setChannelMask(channelConfig)
                            .build())
                    .setBufferSizeInBytes(bufferSize) // Buffer minimal absolu
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY) // Mode basse latence
                    .build();
            
            isInitialized = true;
            Log.i(TAG, "Audio instantané initialisé: sampleRate=" + sampleRate + 
                      ", bufferSize=" + bufferSize + ", minBufferSize=" + minBufferSize);
            
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'initialisation audio: " + e.getMessage());
        }
    }
    
    public void startAudio() {
        if (!isInitialized || audioTrack == null) {
            Log.e(TAG, "Audio non initialisé");
            return;
        }
        
        if (isRunning.get()) {
            Log.w(TAG, "Audio déjà en cours");
            return;
        }
        
        isRunning.set(true);
        audioTrack.play();
        
        Log.i(TAG, "Audio instantané démarré");
    }
    
    public void stopAudio() {
        isRunning.set(false);
        
        if (audioTrack != null) {
            audioTrack.stop();
        }
        
        Log.i(TAG, "Audio instantané arrêté");
    }
    
    public void writeAudioData(byte[] audioData) {
        if (!isInitialized || audioData == null || audioData.length == 0) {
            return;
        }
        
        try {
            // Écriture directe sans queue pour latence zéro
            int written = audioTrack.write(audioData, 0, audioData.length);
            if (written < 0) {
                Log.e(TAG, "Erreur lors de l'écriture audio: " + written);
            } else {
                // Log pour debug (simplifié)
                Log.d(TAG, "Audio écrit directement: " + written + " bytes");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'écriture audio: " + e.getMessage());
        }
    }
    
    public void release() {
        stopAudio();
        
        if (audioTrack != null) {
            audioTrack.release();
            audioTrack = null;
        }
        
        isInitialized = false;
        Log.i(TAG, "Audio instantané libéré");
    }
    
    public boolean isInitialized() {
        return isInitialized;
    }
    
    public int getSampleRate() {
        return sampleRate;
    }
    
    public int getBufferSize() {
        return bufferSize;
    }
    
    public void setSampleRate(int newSampleRate) {
        if (newSampleRate > 0 && newSampleRate != sampleRate) {
            sampleRate = newSampleRate;
            Log.i(TAG, "Sample rate changé à: " + sampleRate);
        }
    }
} 