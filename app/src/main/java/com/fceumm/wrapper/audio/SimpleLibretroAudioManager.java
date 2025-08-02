package com.fceumm.wrapper.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.util.Log;

public class SimpleLibretroAudioManager {
    private static final String TAG = "SimpleLibretroAudio";
    
    private AudioTrack audioTrack;
    private boolean isInitialized = false;
    private int sampleRate = 48000; // Standard libretro - haute qualité
    private int channelConfig = AudioFormat.CHANNEL_OUT_STEREO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int bufferSize;
    
    public SimpleLibretroAudioManager(Context context) {
        initAudio();
    }
    
    private void initAudio() {
        try {
            // Calculer la taille du buffer optimale
            int minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat);
            if (minBufferSize == AudioTrack.ERROR_BAD_VALUE || minBufferSize == AudioTrack.ERROR) {
                Log.e(TAG, "Erreur lors du calcul de la taille du buffer");
                return;
            }
            
            // Buffer minimal pour la réactivité
            bufferSize = minBufferSize;
            
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
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                    .build();
            
            isInitialized = true;
            Log.i(TAG, "Audio simple initialisé: sampleRate=" + sampleRate + 
                      ", bufferSize=" + bufferSize + ", minBufferSize=" + minBufferSize);
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'initialisation audio: " + e.getMessage());
        }
    }
    
    public void startAudio() {
        if (!isInitialized || audioTrack == null) {
            return;
        }
        audioTrack.play();
        Log.i(TAG, "Audio simple démarré");
    }
    
    public void stopAudio() {
        if (audioTrack != null) {
            audioTrack.stop();
        }
        Log.i(TAG, "Audio simple arrêté");
    }
    
    public void writeAudioData(byte[] audioData) {
        if (!isInitialized || audioData == null || audioData.length == 0) {
            return;
        }
        
        try {
            int written = audioTrack.write(audioData, 0, audioData.length);
            if (written < 0) {
                Log.e(TAG, "Erreur lors de l'écriture audio: " + written);
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
        Log.i(TAG, "Audio simple libéré");
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