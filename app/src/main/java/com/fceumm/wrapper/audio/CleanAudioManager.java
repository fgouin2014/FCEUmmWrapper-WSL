package com.fceumm.wrapper.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class CleanAudioManager {
    private static final String TAG = "CleanAudioManager";
    
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
    
    // Paramètres optimisés pour la réactivité
    private static final int BUFFER_SIZE_MULTIPLIER = 1; // Buffer minimal pour la réactivité
    private static final int MAX_QUEUE_SIZE = 2; // Queue minimale pour éviter la latence
    
    public CleanAudioManager(Context context) {
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
            
            // Utiliser un buffer minimal pour la réactivité
            bufferSize = minBufferSize * BUFFER_SIZE_MULTIPLIER;
            
            // Créer l'AudioTrack avec des paramètres optimisés pour la qualité
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
                    .setBufferSizeInBytes(bufferSize) // Buffer minimal pour la réactivité
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY) // Mode basse latence
                    .build();
            
            isInitialized = true;
            Log.i(TAG, "Audio propre initialisé: sampleRate=" + sampleRate + 
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
            Log.i(TAG, "Thread audio propre démarré");
            while (isRunning.get()) {
                try {
                    // Attendre les données audio avec timeout plus long
                    byte[] audioData = audioQueue.poll();
                    if (audioData != null && audioData.length > 0) {
                        int written = audioTrack.write(audioData, 0, audioData.length);
                        if (written < 0) {
                            Log.e(TAG, "Erreur lors de l'écriture audio: " + written);
                        }
                    } else {
                        // Pas de données, pause minimale pour la réactivité
                        Thread.sleep(1);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Erreur dans le thread audio: " + e.getMessage());
                }
            }
            Log.i(TAG, "Thread audio propre terminé");
        });
        
        audioThread.setPriority(Thread.MAX_PRIORITY); // Priorité maximale pour la réactivité
        audioThread.start();
        
        Log.i(TAG, "Audio propre démarré");
    }
    
    public void stopAudio() {
        isRunning.set(false);
        
        if (audioThread != null) {
            try {
                audioThread.join(2000); // Attendre max 2 secondes
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
        
        Log.i(TAG, "Audio propre arrêté");
    }
    
    public void writeAudioData(byte[] audioData) {
        if (!isInitialized || audioData == null || audioData.length == 0) {
            return;
        }
        
        try {
            // Si la queue est pleine, supprimer les anciennes données pour la réactivité
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
        Log.i(TAG, "Audio propre libéré");
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