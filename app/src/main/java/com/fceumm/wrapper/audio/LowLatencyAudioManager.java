package com.fceumm.wrapper.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class LowLatencyAudioManager {
    private static final String TAG = "LowLatencyAudioManager";
    
    private AudioTrack audioTrack;
    private boolean isInitialized = false;
    private int sampleRate = 44100;
    private int channelConfig = AudioFormat.CHANNEL_OUT_STEREO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int bufferSize;
    
    // Thread dédié pour l'audio
    private Thread audioThread;
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private LinkedBlockingQueue<byte[]> audioQueue;
    
    // Optimisations pour la latence avec réduction du bruit
    private static final int MIN_BUFFER_SIZE_MULTIPLIER = 2; // Buffer légèrement plus grand pour réduire le bruit
    private static final int MAX_QUEUE_SIZE = 5; // Queue un peu plus grande pour éviter les interruptions
    
    public LowLatencyAudioManager(Context context) {
        audioQueue = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);
        initAudio();
    }
    
    private void initAudio() {
        try {
            // Calculer la taille minimale du buffer
            int minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat);
            if (minBufferSize == AudioTrack.ERROR_BAD_VALUE || minBufferSize == AudioTrack.ERROR) {
                Log.e(TAG, "Erreur lors du calcul de la taille du buffer");
                return;
            }
            
            // Utiliser un buffer équilibré pour réduire le bruit tout en gardant une latence acceptable
            bufferSize = minBufferSize * MIN_BUFFER_SIZE_MULTIPLIER;
            
            // Créer l'AudioTrack avec des paramètres optimisés pour la latence
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
                    .setBufferSizeInBytes(bufferSize) // Buffer équilibré pour réduire le bruit
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                    .build();
            
            isInitialized = true;
            Log.i(TAG, "Audio basse latence initialisé: sampleRate=" + sampleRate + 
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
        
        // Démarrer le thread audio dédié
        audioThread = new Thread(() -> {
            Log.i(TAG, "Thread audio démarré");
            while (isRunning.get()) {
                try {
                    // Attendre les données audio avec timeout court
                    byte[] audioData = audioQueue.poll();
                    if (audioData != null && audioData.length > 0) {
                        int written = audioTrack.write(audioData, 0, audioData.length);
                        if (written < 0) {
                            Log.e(TAG, "Erreur lors de l'écriture audio: " + written);
                        }
                    } else {
                        // Pas de données, faire une petite pause
                        Thread.sleep(1);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Erreur dans le thread audio: " + e.getMessage());
                }
            }
            Log.i(TAG, "Thread audio terminé");
        });
        
        audioThread.setPriority(Thread.MAX_PRIORITY);
        audioThread.start();
        
        Log.i(TAG, "Audio basse latence démarré");
    }
    
    public void stopAudio() {
        isRunning.set(false);
        
        if (audioThread != null) {
            try {
                audioThread.join(1000); // Attendre max 1 seconde
            } catch (InterruptedException e) {
                Log.w(TAG, "Interruption lors de l'arrêt du thread audio");
            }
            audioThread = null;
        }
        
        if (audioTrack != null) {
            audioTrack.stop();
        }
        
        // Vider la queue
        audioQueue.clear();
        
        Log.i(TAG, "Audio basse latence arrêté");
    }
    
    public void writeAudioData(byte[] audioData) {
        if (!isInitialized || audioData == null || audioData.length == 0) {
            return;
        }
        
        try {
            // Si la queue est pleine, vider les anciennes données pour éviter la latence
            if (audioQueue.size() >= MAX_QUEUE_SIZE) {
                audioQueue.poll(); // Retirer l'ancienne donnée
            }
            
            // Ajouter les nouvelles données
            audioQueue.offer(audioData);
            
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'ajout des données audio: " + e.getMessage());
        }
    }
    
    public void release() {
        stopAudio();
        
        if (audioTrack != null) {
            audioTrack.release();
            audioTrack = null;
        }
        
        isInitialized = false;
        Log.i(TAG, "Audio basse latence libéré");
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
    
    public int getQueueSize() {
        return audioQueue.size();
    }
    
    public void setSampleRate(int newSampleRate) {
        if (newSampleRate > 0 && newSampleRate != sampleRate) {
            sampleRate = newSampleRate;
            Log.i(TAG, "Sample rate changé à: " + sampleRate);
        }
    }
} 