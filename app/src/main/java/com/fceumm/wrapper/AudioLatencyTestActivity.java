package com.fceumm.wrapper;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.fceumm.wrapper.audio.InstantAudioManager;

public class AudioLatencyTestActivity extends Activity {
    private static final String TAG = "AudioLatencyTest";
    
    private InstantAudioManager audioManager;
    private TextView tvLatencyInfo;
    private Button btnTestLatency;
    private Button btnTestCoreAudio;
    private Button btnBack;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Mode FULLSCREEN
        getWindow().setFlags(
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        
        setContentView(R.layout.activity_audio_latency_test);
        
        // Initialiser l'audio
        audioManager = new InstantAudioManager(this);
        
        // Initialiser les vues
        initViews();
        setupListeners();
        updateLatencyInfo();
        
        Log.i(TAG, "Activité de test de latence audio initialisée");
    }
    
    private void initViews() {
        tvLatencyInfo = findViewById(R.id.tv_latency_info);
        btnTestLatency = findViewById(R.id.btn_test_latency);
        btnTestCoreAudio = findViewById(R.id.btn_test_core_audio);
        btnBack = findViewById(R.id.btn_back);
    }
    
    private void setupListeners() {
        btnTestLatency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testDirectLatency();
            }
        });
        
        btnTestCoreAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testCoreAudioLatency();
            }
        });
        
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    
    private void testDirectLatency() {
        Log.i(TAG, "Test de latence directe");
        
        // Démarrer l'audio
        audioManager.startAudio();
        
        // Générer un signal de test immédiat
        byte[] testSignal = generateTestSignal();
        long startTime = System.nanoTime();
        
        // Écrire directement
        audioManager.writeAudioData(testSignal);
        
        long endTime = System.nanoTime();
        long latency = (endTime - startTime) / 1000000; // Convertir en millisecondes
        
        String result = String.format("Latence directe: %d ms", latency);
        tvLatencyInfo.setText(result);
        
        Toast.makeText(this, "Test direct terminé: " + latency + "ms", Toast.LENGTH_SHORT).show();
        
        Log.i(TAG, "Latence directe mesurée: " + latency + "ms");
    }
    
    private void testCoreAudioLatency() {
        Log.i(TAG, "Test de latence du core audio");
        
        // Démarrer l'audio
        audioManager.startAudio();
        
        // Simuler le processus complet
        long startTime = System.nanoTime();
        
        // Générer un signal de test et l'écrire dans l'audio manager
        byte[] testSignal = generateTestSignal();
        if (testSignal != null && testSignal.length > 0) {
            audioManager.writeAudioData(testSignal);
        }
        
        long endTime = System.nanoTime();
        long latency = (endTime - startTime) / 1000000; // Convertir en millisecondes
        
        String result = String.format("Latence core: %d ms", latency);
        tvLatencyInfo.setText(result);
        
        Toast.makeText(this, "Test core terminé: " + latency + "ms", Toast.LENGTH_SHORT).show();
        
        Log.i(TAG, "Latence core mesurée: " + latency + "ms");
    }
    
    private byte[] generateTestSignal() {
        // Générer un signal de test simple (440 Hz)
        int sampleRate = 44100;
        int durationMs = 100; // 100ms de signal
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
    
    private void updateLatencyInfo() {
        String info = String.format(
            "Audio Instantané:\n" +
            "Buffer: %d bytes\n" +
            "Sample Rate: %d Hz\n" +
            "Mode: Basse Latence\n\n" +
            "Instructions:\n" +
            "1. Test Direct: Mesure la latence Java\n" +
            "2. Test Core: Mesure la latence complète\n" +
            "3. Comparez les résultats",
            audioManager.getBufferSize(),
            audioManager.getSampleRate()
        );
        
        tvLatencyInfo.setText(info);
    }
    
    // Pas de méthode native nécessaire pour ce test
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioManager != null) {
            audioManager.release();
        }
    }
} 