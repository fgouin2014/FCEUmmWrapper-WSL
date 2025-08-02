package com.fceumm.wrapper;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.fceumm.wrapper.audio.LowLatencyAudioManager;
import com.fceumm.wrapper.audio.CleanAudioManager;

public class AudioQualityTestActivity extends Activity {
    private static final String TAG = "AudioQualityTest";
    
    private LowLatencyAudioManager lowLatencyAudio;
    private CleanAudioManager cleanAudio;
    private TextView tvAudioInfo;
    private Button btnTestLowLatency;
    private Button btnTestCleanAudio;
    private Button btnCompareAudio;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Mode FULLSCREEN
        getWindow().setFlags(
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        
        setContentView(R.layout.activity_audio_quality_test);
        
        // Initialiser les deux gestionnaires audio
        lowLatencyAudio = new LowLatencyAudioManager(this);
        cleanAudio = new CleanAudioManager(this);
        
        // Initialiser les vues
        initViews();
        setupListeners();
        updateAudioInfo();
        
        Log.i(TAG, "Activité de test audio initialisée");
    }
    
    private void initViews() {
        tvAudioInfo = findViewById(R.id.tv_audio_info);
        btnTestLowLatency = findViewById(R.id.btn_test_low_latency);
        btnTestCleanAudio = findViewById(R.id.btn_test_clean_audio);
        btnCompareAudio = findViewById(R.id.btn_compare_audio);
    }
    
    private void setupListeners() {
        btnTestLowLatency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testLowLatencyAudio();
            }
        });
        
        btnTestCleanAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testCleanAudio();
            }
        });
        
        btnCompareAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compareAudioModes();
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
    
    private void testLowLatencyAudio() {
        Log.i(TAG, "Test audio basse latence");
        
        // Arrêter l'autre mode
        cleanAudio.stopAudio();
        
        // Démarrer le mode basse latence
        lowLatencyAudio.startAudio();
        
        // Générer un signal de test
        byte[] testSignal = generateTestSignal();
        lowLatencyAudio.writeAudioData(testSignal);
        
        Toast.makeText(this, "Test audio basse latence - Écoutez la qualité", Toast.LENGTH_SHORT).show();
        
        // Arrêter après 2 secondes
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                lowLatencyAudio.stopAudio();
                Toast.makeText(AudioQualityTestActivity.this, "Test terminé", Toast.LENGTH_SHORT).show();
            }
        }, 2000);
    }
    
    private void testCleanAudio() {
        Log.i(TAG, "Test audio propre");
        
        // Arrêter l'autre mode
        lowLatencyAudio.stopAudio();
        
        // Démarrer le mode propre
        cleanAudio.startAudio();
        
        // Générer un signal de test
        byte[] testSignal = generateTestSignal();
        cleanAudio.writeAudioData(testSignal);
        
        Toast.makeText(this, "Test audio propre - Écoutez la qualité", Toast.LENGTH_SHORT).show();
        
        // Arrêter après 2 secondes
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                cleanAudio.stopAudio();
                Toast.makeText(AudioQualityTestActivity.this, "Test terminé", Toast.LENGTH_SHORT).show();
            }
        }, 2000);
    }
    
    private void compareAudioModes() {
        Log.i(TAG, "Comparaison des modes audio");
        
        String comparison = String.format(
            "Basse Latence:\n" +
            "- Buffer: %d bytes\n" +
            "- Queue: %d éléments\n" +
            "- Latence: ~20-40ms\n" +
            "- Qualité: Peut avoir du bruit\n\n" +
            "Audio Propre:\n" +
            "- Buffer: %d bytes\n" +
            "- Queue: %d éléments\n" +
            "- Latence: ~50-80ms\n" +
            "- Qualité: Sans bruit",
            lowLatencyAudio.getBufferSize(),
            lowLatencyAudio.getQueueSize(),
            cleanAudio.getBufferSize(),
            cleanAudio.getQueueSize()
        );
        
        tvAudioInfo.setText(comparison);
        Toast.makeText(this, "Comparaison affichée", Toast.LENGTH_SHORT).show();
    }
    
    private byte[] generateTestSignal() {
        // Générer un signal de test simple (440 Hz)
        int sampleRate = 44100;
        int durationMs = 500; // 500ms de signal
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
    
    private void updateAudioInfo() {
        String info = String.format(
            "Audio Basse Latence:\n" +
            "Buffer: %d bytes\n" +
            "Queue: %d éléments\n\n" +
            "Audio Propre:\n" +
            "Buffer: %d bytes\n" +
            "Queue: %d éléments",
            lowLatencyAudio.getBufferSize(),
            lowLatencyAudio.getQueueSize(),
            cleanAudio.getBufferSize(),
            cleanAudio.getQueueSize()
        );
        
        tvAudioInfo.setText(info);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (lowLatencyAudio != null) {
            lowLatencyAudio.release();
        }
        if (cleanAudio != null) {
            cleanAudio.release();
        }
    }
} 