package com.fceumm.wrapper.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class UltraLowLatencyAudioManager {
    private static final String TAG = "UltraLowLatencyAudio";
    
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
    
    // Paramètres ultra-réactifs
    private static final int BUFFER_SIZE_MULTIPLIER = 1; // Buffer minimal absolu
    private static final int MAX_QUEUE_SIZE = 1; // Queue de 1 seul élément
    
    public UltraLowLatencyAudioManager(Context context) {
        audioQueue = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);
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
            
            // Utiliser un buffer minimal absolu pour la réactivité maximale
            bufferSize = minBufferSize * BUFFER_SIZE_MULTIPLIER;
            
            // Créer l'AudioTrack avec des paramètres ultra-réactifs
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
            Log.i(TAG, "Audio ultra-réactif initialisé: sampleRate=" + sampleRate + 
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
        
        // Démarrer le thread audio dédié avec priorité maximale
        audioThread = new Thread(() -> {
            Log.i(TAG, "Thread audio ultra-réactif démarré");
            while (isRunning.get()) {
                try {
                    // Traitement immédiat sans timeout
                    byte[] audioData = audioQueue.poll();
                    if (audioData != null && audioData.length > 0) {
                        int written = audioTrack.write(audioData, 0, audioData.length);
                        if (written < 0) {
                            Log.e(TAG, "Erreur lors de l'écriture audio: " + written);
                        }
                    } else {
                        // Pas de données, pause minimale
                        Thread.sleep(1);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Erreur dans le thread audio: " + e.getMessage());
                }
            }
            Log.i(TAG, "Thread audio ultra-réactif terminé");
        });
        
        audioThread.setPriority(Thread.MAX_PRIORITY); // Priorité maximale
        audioThread.start();
        
        Log.i(TAG, "Audio ultra-réactif démarré");
    }
    
    public void stopAudio() {
        isRunning.set(false);
        
        if (audioThread != null) {
            try {
                audioThread.join(500); // Attendre max 500ms
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
        
        Log.i(TAG, "Audio ultra-réactif arrêté");
    }
    
    public void writeAudioData(byte[] audioData) {
        if (!isInitialized || audioData == null || audioData.length == 0) {
            return;
        }
        
        try {
            // Supprimer immédiatement les anciennes données pour la réactivité maximale
            if (audioQueue.size() >= MAX_QUEUE_SIZE) {
                audioQueue.poll(); // Retirer l'ancienne donnée immédiatement
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
        Log.i(TAG, "Audio ultra-réactif libéré");
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