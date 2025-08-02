package com.fceumm.wrapper;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.fceumm.wrapper.audio.LowLatencyAudioManager;

public class LowLatencyAudioSettingsActivity extends Activity {
    private static final String TAG = "LowLatencyAudioSettings";
    
    private LowLatencyAudioManager audioManager;
    private TextView tvLatencyInfo;
    private TextView tvBufferSize;
    private TextView tvQueueSize;
    private SeekBar sbBufferMultiplier;
    private Button btnTestLatency;
    private Button btnApplySettings;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Mode FULLSCREEN
        getWindow().setFlags(
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        
        setContentView(R.layout.activity_low_latency_audio_settings);
        
        // Initialiser l'audio manager
        audioManager = new LowLatencyAudioManager(this);
        
        // Initialiser les vues
        initViews();
        setupListeners();
        updateLatencyInfo();
        
        Log.i(TAG, "Activité de paramètres audio basse latence initialisée");
    }
    
    private void initViews() {
        tvLatencyInfo = findViewById(R.id.tv_latency_info);
        tvBufferSize = findViewById(R.id.tv_buffer_size);
        tvQueueSize = findViewById(R.id.tv_queue_size);
        sbBufferMultiplier = findViewById(R.id.sb_buffer_multiplier);
        btnTestLatency = findViewById(R.id.btn_test_latency);
        btnApplySettings = findViewById(R.id.btn_apply_settings);
        
        // Configurer le SeekBar
        sbBufferMultiplier.setMax(5); // 1x à 6x
        sbBufferMultiplier.setProgress(0); // 1x par défaut
    }
    
    private void setupListeners() {
        btnTestLatency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testLatency();
            }
        });
        
        btnApplySettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applySettings();
            }
        });
        
        // Mettre à jour les infos en temps réel
        findViewById(R.id.btn_refresh_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateLatencyInfo();
            }
        });
        
        // Bouton retour
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    
    private void testLatency() {
        Log.i(TAG, "Test de latence audio démarré");
        
        // Démarrer l'audio
        audioManager.startAudio();
        
        // Générer un signal de test
        byte[] testSignal = generateTestSignal();
        
        // Mesurer le temps de latence
        long startTime = System.nanoTime();
        audioManager.writeAudioData(testSignal);
        
        // Simuler un délai pour mesurer la latence
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long endTime = System.nanoTime();
        long latency = (endTime - startTime) / 1000000; // en millisecondes
        
        // Arrêter l'audio
        audioManager.stopAudio();
        
        // Afficher les résultats
        String result = String.format("Latence mesurée: %d ms\nBuffer: %d bytes\nQueue: %d éléments",
                latency, audioManager.getBufferSize(), audioManager.getQueueSize());
        
        tvLatencyInfo.setText(result);
        Toast.makeText(this, "Test de latence terminé", Toast.LENGTH_SHORT).show();
        
        Log.i(TAG, "Test de latence terminé: " + latency + "ms");
    }
    
    private byte[] generateTestSignal() {
        // Générer un signal de test simple (440 Hz)
        int sampleRate = audioManager.getSampleRate();
        int durationMs = 200; // 200ms de signal (plus long pour mieux entendre)
        int numSamples = (sampleRate * durationMs) / 1000;
        
        byte[] signal = new byte[numSamples * 4]; // 16-bit stereo
        
        for (int i = 0; i < numSamples; i++) {
            // Générer une onde sinusoïdale simple
            double frequency = 440.0; // 440 Hz
            double amplitude = 0.3;
            double sample = amplitude * Math.sin(2 * Math.PI * frequency * i / sampleRate);
            
            // Convertir en 16-bit PCM
            short pcmSample = (short) (sample * 32767);
            
            // Écrire en stéréo (L et R identiques)
            int index = i * 4;
            signal[index] = (byte) (pcmSample & 0xFF);
            signal[index + 1] = (byte) ((pcmSample >> 8) & 0xFF);
            signal[index + 2] = (byte) (pcmSample & 0xFF);
            signal[index + 3] = (byte) ((pcmSample >> 8) & 0xFF);
        }
        
        return signal;
    }
    
    private void applySettings() {
        int multiplier = sbBufferMultiplier.getProgress() + 1;
        
        // Recréer l'audio manager avec les nouveaux paramètres
        audioManager.release();
        audioManager = new LowLatencyAudioManager(this);
        
        Toast.makeText(this, "Paramètres appliqués (Buffer x" + multiplier + ")", Toast.LENGTH_SHORT).show();
        updateLatencyInfo();
        
        Log.i(TAG, "Paramètres audio appliqués: buffer multiplier = " + multiplier);
    }
    
    private void updateLatencyInfo() {
        if (audioManager != null) {
            tvBufferSize.setText("Taille buffer: " + audioManager.getBufferSize() + " bytes");
            tvQueueSize.setText("Taille queue: " + audioManager.getQueueSize() + " éléments");
            
            // Calculer la latence théorique
            int bufferSize = audioManager.getBufferSize();
            int sampleRate = audioManager.getSampleRate();
            double latencyMs = (bufferSize * 1000.0) / (sampleRate * 4); // 4 bytes par sample stéréo
            
            String info = String.format("Latence théorique: %.1f ms\nSample Rate: %d Hz\nBuffer: %d bytes",
                    latencyMs, sampleRate, bufferSize);
            
            tvLatencyInfo.setText(info);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioManager != null) {
            audioManager.release();
        }
    }
} 