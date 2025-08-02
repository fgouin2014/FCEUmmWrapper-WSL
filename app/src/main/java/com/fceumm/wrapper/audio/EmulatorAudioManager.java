package com.fceumm.wrapper.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class EmulatorAudioManager {
    private static final String TAG = "EmulatorAudioManager";
    
    private AudioTrack audioTrack;
    private boolean isInitialized = false;
    private int sampleRate = 44100;
    private int channelConfig = AudioFormat.CHANNEL_OUT_STEREO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int bufferSize;
    
    public EmulatorAudioManager(Context context) {
        initAudio();
    }
    
    private void initAudio() {
        try {
            // Calculer la taille du buffer
            bufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat);
            if (bufferSize == AudioTrack.ERROR_BAD_VALUE || bufferSize == AudioTrack.ERROR) {
                Log.e(TAG, "Erreur lors du calcul de la taille du buffer");
                return;
            }
            
            // Créer l'AudioTrack
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
                    .setBufferSizeInBytes(bufferSize * 2) // Buffer plus grand pour éviter les underruns
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build();
            
            isInitialized = true;
            Log.i(TAG, "Audio initialisé: sampleRate=" + sampleRate + ", bufferSize=" + bufferSize);
            
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'initialisation audio: " + e.getMessage());
        }
    }
    
    public void startAudio() {
        if (isInitialized && audioTrack != null) {
            audioTrack.play();
            Log.i(TAG, "Audio démarré");
        }
    }
    
    public void stopAudio() {
        if (audioTrack != null) {
            audioTrack.stop();
            Log.i(TAG, "Audio arrêté");
        }
    }
    
    public void writeAudioData(byte[] audioData) {
        if (isInitialized && audioTrack != null && audioData != null && audioData.length > 0) {
            try {
                int written = audioTrack.write(audioData, 0, audioData.length);
                if (written < 0) {
                    Log.e(TAG, "Erreur lors de l'écriture audio: " + written);
                }
            } catch (Exception e) {
                Log.e(TAG, "Erreur lors de l'écriture audio: " + e.getMessage());
            }
        }
    }
    
    public void release() {
        if (audioTrack != null) {
            audioTrack.release();
            audioTrack = null;
            isInitialized = false;
            Log.i(TAG, "Audio libéré");
        }
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
} 