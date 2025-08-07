package com.fceumm.wrapper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class AudioSettingsActivity extends Activity {
    private static final String TAG = "AudioSettingsActivity";
    private static final String PREF_NAME = "FCEUmmSettings";
    
    private SharedPreferences preferences;
    private SeekBar volumeSeekBar;
    private TextView volumeText;
    private Switch muteSwitch;
    private Switch lowLatencySwitch;
    private Switch rfFilterSwitch;
    private Switch swapDutySwitch;
    private SeekBar stereoDelaySeekBar;
    private TextView stereoDelayText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Mode FULLSCREEN complet
        getWindow().setFlags(
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        
        setContentView(R.layout.activity_audio_settings);
        
        // Masquer la barre de navigation (Android 4.4+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    getWindow().setDecorFitsSystemWindows(false);
                    if (getWindow().getInsetsController() != null) {
                        getWindow().getInsetsController().hide(android.view.WindowInsets.Type.statusBars() | android.view.WindowInsets.Type.navigationBars());
                        getWindow().getInsetsController().setSystemBarsBehavior(android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                    }
                } else {
                    if (getWindow().getDecorView() != null) {
                        getWindow().getDecorView().setSystemUiVisibility(
                            android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            android.view.View.SYSTEM_UI_FLAG_FULLSCREEN |
                            android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        );
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Erreur lors de la configuration du fullscreen: " + e.getMessage());
            }
        }
        
        // Initialiser les préférences
        preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        
        Log.i(TAG, "Activité des paramètres audio initialisée");
        
        // Initialiser les vues
        setupViews();
        
        // Charger les paramètres actuels
        loadCurrentSettings();
        
        // Configurer les listeners
        setupListeners();
    }
    
    private void setupViews() {
        volumeSeekBar = findViewById(R.id.seekbar_volume);
        volumeText = findViewById(R.id.text_volume);
        muteSwitch = findViewById(R.id.switch_mute);
        lowLatencySwitch = findViewById(R.id.switch_low_latency);
        rfFilterSwitch = findViewById(R.id.switch_rf_filter);
        swapDutySwitch = findViewById(R.id.switch_swap_duty);
        stereoDelaySeekBar = findViewById(R.id.seekbar_stereo_delay);
        stereoDelayText = findViewById(R.id.text_stereo_delay);
    }
    
    private void loadCurrentSettings() {
        // Volume (0-100)
        int volume = preferences.getInt("master_volume", 100);
        volumeSeekBar.setProgress(volume);
        volumeText.setText("Volume: " + volume + "%");
        
        // Mute
        boolean muted = preferences.getBoolean("audio_muted", false);
        muteSwitch.setChecked(muted);
        
        // Qualité audio
        int quality = preferences.getInt("audio_quality", 2);
        updateQualityButtons(quality);
        
        // Sample rate
        int sampleRate = preferences.getInt("sample_rate", 48000);
        updateSampleRateButtons(sampleRate);
        
        // RF Filter
        boolean rfFilter = preferences.getBoolean("audio_rf_filter", false);
        rfFilterSwitch.setChecked(rfFilter);
        
        // Stereo Delay (0-50ms)
        int stereoDelay = preferences.getInt("audio_stereo_delay", 0);
        stereoDelaySeekBar.setProgress(stereoDelay);
        stereoDelayText.setText("Délai Stéréo: " + stereoDelay + "ms");
        
        // Swap Duty
        boolean swapDuty = preferences.getBoolean("audio_swap_duty", false);
        swapDutySwitch.setChecked(swapDuty);
        
        // Low Latency
        boolean lowLatency = preferences.getBoolean("audio_low_latency", true);
        lowLatencySwitch.setChecked(lowLatency);
        
        Log.i(TAG, "Paramètres audio chargés - Volume: " + volume + "%, Mute: " + muted);
    }
    
    private void setupListeners() {
        // Volume SeekBar
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    volumeText.setText("Volume: " + progress + "%");
                    saveVolume(progress);
                    applyVolume(progress);
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // Mute Switch
        muteSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveMute(isChecked);
            applyMute(isChecked);
        });
        
        // RF Filter Switch
        rfFilterSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveRfFilter(isChecked);
            applyRfFilter(isChecked);
        });
        
        // Swap Duty Switch
        swapDutySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSwapDuty(isChecked);
            applySwapDuty(isChecked);
        });
        
        // Low Latency Switch
        lowLatencySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveLowLatency(isChecked);
            applyLowLatency(isChecked);
        });
        
        // Stereo Delay SeekBar
        stereoDelaySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    stereoDelayText.setText("Délai Stéréo: " + progress + "ms");
                    saveStereoDelay(progress);
                    applyStereoDelay(progress);
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // Boutons de qualité
        setupQualityButtons();
        
        // Boutons de sample rate
        setupSampleRateButtons();
        
        // Boutons spéciaux
        setupSpecialButtons();
    }
    
    private void setupQualityButtons() {
        Button btnQualityLow = findViewById(R.id.btn_quality_low);
        Button btnQualityHigh = findViewById(R.id.btn_quality_high);
        Button btnQualityMax = findViewById(R.id.btn_quality_max);
        
        btnQualityLow.setOnClickListener(v -> {
            updateQualityButtons(0);
            saveQuality(0);
            applyQuality(0);
        });
        
        btnQualityHigh.setOnClickListener(v -> {
            updateQualityButtons(1);
            saveQuality(1);
            applyQuality(1);
        });
        
        btnQualityMax.setOnClickListener(v -> {
            updateQualityButtons(2);
            saveQuality(2);
            applyQuality(2);
        });
    }
    
    private void setupSampleRateButtons() {
        Button btnSampleRate22 = findViewById(R.id.btn_sample_rate_22);
        Button btnSampleRate44 = findViewById(R.id.btn_sample_rate_44);
        Button btnSampleRate48 = findViewById(R.id.btn_sample_rate_48);
        
        btnSampleRate22.setOnClickListener(v -> {
            updateSampleRateButtons(22050);
            saveSampleRate(22050);
            applySampleRate(22050);
        });
        
        btnSampleRate44.setOnClickListener(v -> {
            updateSampleRateButtons(44100);
            saveSampleRate(44100);
            applySampleRate(44100);
        });
        
        btnSampleRate48.setOnClickListener(v -> {
            updateSampleRateButtons(48000);
            saveSampleRate(48000);
            applySampleRate(48000);
        });
    }
    
    private void setupSpecialButtons() {
        Button btnOptimizeDuckHunt = findViewById(R.id.btn_optimize_duck_hunt);
        Button btnForceApply = findViewById(R.id.btn_force_apply);
        Button btnResetDefaults = findViewById(R.id.btn_reset_defaults);
        Button btnBack = findViewById(R.id.btn_back);
        
        btnOptimizeDuckHunt.setOnClickListener(v -> {
            optimizeForDuckHunt();
        });
        
        btnForceApply.setOnClickListener(v -> {
            forceApplySettings();
        });
        
        btnResetDefaults.setOnClickListener(v -> {
            resetToDefaults();
        });
        
        btnBack.setOnClickListener(v -> {
            finish();
        });
    }
    
    private void updateQualityButtons(int quality) {
        Button btnQualityLow = findViewById(R.id.btn_quality_low);
        Button btnQualityHigh = findViewById(R.id.btn_quality_high);
        Button btnQualityMax = findViewById(R.id.btn_quality_max);
        
        btnQualityLow.setBackgroundResource(quality == 0 ? android.R.color.holo_green_dark : android.R.color.darker_gray);
        btnQualityHigh.setBackgroundResource(quality == 1 ? android.R.color.holo_green_dark : android.R.color.darker_gray);
        btnQualityMax.setBackgroundResource(quality == 2 ? android.R.color.holo_green_dark : android.R.color.darker_gray);
    }
    
    private void updateSampleRateButtons(int sampleRate) {
        Button btnSampleRate22 = findViewById(R.id.btn_sample_rate_22);
        Button btnSampleRate44 = findViewById(R.id.btn_sample_rate_44);
        Button btnSampleRate48 = findViewById(R.id.btn_sample_rate_48);
        
        btnSampleRate22.setBackgroundResource(sampleRate == 22050 ? android.R.color.holo_blue_dark : android.R.color.darker_gray);
        btnSampleRate44.setBackgroundResource(sampleRate == 44100 ? android.R.color.holo_blue_dark : android.R.color.darker_gray);
        btnSampleRate48.setBackgroundResource(sampleRate == 48000 ? android.R.color.holo_blue_dark : android.R.color.darker_gray);
    }
    
    // Méthodes de sauvegarde
    private void saveVolume(int volume) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("master_volume", volume);
        editor.apply();
        Log.i(TAG, "Volume sauvegardé: " + volume + "%");
    }
    
    private void saveMute(boolean muted) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("audio_muted", muted);
        editor.apply();
        Log.i(TAG, "Mute sauvegardé: " + muted);
    }
    
    private void saveQuality(int quality) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("audio_quality", quality);
        editor.apply();
        Log.i(TAG, "Qualité sauvegardée: " + quality);
    }
    
    private void saveSampleRate(int sampleRate) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("sample_rate", sampleRate);
        editor.apply();
        Log.i(TAG, "Sample rate sauvegardé: " + sampleRate + " Hz");
    }
    
    private void saveRfFilter(boolean enabled) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("audio_rf_filter", enabled);
        editor.apply();
        Log.i(TAG, "RF Filter sauvegardé: " + enabled);
    }
    
    private void saveStereoDelay(int delay) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("audio_stereo_delay", delay);
        editor.apply();
        Log.i(TAG, "Stereo delay sauvegardé: " + delay + "ms");
    }
    
    private void saveSwapDuty(boolean enabled) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("audio_swap_duty", enabled);
        editor.apply();
        Log.i(TAG, "Swap duty sauvegardé: " + enabled);
    }
    
    private void saveLowLatency(boolean enabled) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("audio_low_latency", enabled);
        editor.apply();
        Log.i(TAG, "Low latency sauvegardé: " + enabled);
    }
    
    // Méthodes d'application natives
    private void applyVolume(int volume) {
        try {
            setMasterVolume(volume);
            Toast.makeText(this, "🔊 Volume: " + volume + "%", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'application du volume: " + e.getMessage());
        }
    }
    
    private void applyMute(boolean muted) {
        try {
            setAudioMuted(muted);
            Toast.makeText(this, "🔇 Audio " + (muted ? "muet" : "activé"), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'application du mute: " + e.getMessage());
        }
    }
    
    private void applyQuality(int quality) {
        try {
            setAudioQuality(quality);
            String qualityName = quality == 0 ? "Faible" : quality == 1 ? "Élevée" : "Maximum";
            Toast.makeText(this, "🎵 Qualité: " + qualityName, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'application de la qualité: " + e.getMessage());
        }
    }
    
    private void applySampleRate(int sampleRate) {
        try {
            setSampleRate(sampleRate);
            Toast.makeText(this, "🎼 Sample Rate: " + sampleRate + " Hz", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'application du sample rate: " + e.getMessage());
        }
    }
    
    private void applyRfFilter(boolean enabled) {
        try {
            // Cette fonctionnalité sera implémentée dans le code natif
            Toast.makeText(this, "🔧 RF Filter: " + (enabled ? "Activé" : "Désactivé"), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'application du RF filter: " + e.getMessage());
        }
    }
    
    private void applyStereoDelay(int delay) {
        try {
            // Cette fonctionnalité sera implémentée dans le code natif
            Toast.makeText(this, "🎧 Délai Stéréo: " + delay + "ms", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'application du délai stéréo: " + e.getMessage());
        }
    }
    
    private void applySwapDuty(boolean enabled) {
        try {
            // Cette fonctionnalité sera implémentée dans le code natif
            Toast.makeText(this, "🔄 Swap Duty: " + (enabled ? "Activé" : "Désactivé"), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'application du swap duty: " + e.getMessage());
        }
    }
    
    private void applyLowLatency(boolean enabled) {
        try {
            // Cette fonctionnalité sera implémentée dans le code natif
            Toast.makeText(this, "⚡ Low Latency: " + (enabled ? "Activé" : "Désactivé"), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'application du low latency: " + e.getMessage());
        }
    }
    
    // Méthodes spéciales
    private void optimizeForDuckHunt() {
        try {
            // Paramètres optimaux pour Duck Hunt
            saveVolume(100);
            saveQuality(2);
            saveSampleRate(48000);
            saveRfFilter(false);
            saveStereoDelay(0);
            saveSwapDuty(false);
            saveLowLatency(true);
            
            // Appliquer les paramètres
            applyVolume(100);
            applyQuality(2);
            applySampleRate(48000);
            applyRfFilter(false);
            applyStereoDelay(0);
            applySwapDuty(false);
            applyLowLatency(true);
            
            // Mettre à jour l'interface
            loadCurrentSettings();
            
            Toast.makeText(this, "🎯 Optimisé pour Duck Hunt!", Toast.LENGTH_LONG).show();
            Log.i(TAG, "Optimisation Duck Hunt appliquée");
            
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'optimisation Duck Hunt: " + e.getMessage());
        }
    }
    
    private void forceApplySettings() {
        try {
            // Forcer l'application de tous les paramètres
            int volume = preferences.getInt("master_volume", 100);
            boolean muted = preferences.getBoolean("audio_muted", false);
            int quality = preferences.getInt("audio_quality", 2);
            int sampleRate = preferences.getInt("sample_rate", 48000);
            
            applyVolume(volume);
            applyMute(muted);
            applyQuality(quality);
            applySampleRate(sampleRate);
            
            Toast.makeText(this, "✅ Paramètres forcés!", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Application forcée des paramètres audio");
            
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'application forcée: " + e.getMessage());
        }
    }
    
    private void resetToDefaults() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🔄 Réinitialisation");
        builder.setMessage("Voulez-vous réinitialiser tous les paramètres audio aux valeurs par défaut ?");
        builder.setPositiveButton("✅ Oui", (dialog, which) -> {
            try {
                // Valeurs par défaut
                saveVolume(100);
                saveQuality(2);
                saveSampleRate(48000);
                saveMute(false);
                saveRfFilter(false);
                saveStereoDelay(0);
                saveSwapDuty(false);
                saveLowLatency(true);
                
                // Appliquer
                applyVolume(100);
                applyQuality(2);
                applySampleRate(48000);
                applyMute(false);
                applyRfFilter(false);
                applyStereoDelay(0);
                applySwapDuty(false);
                applyLowLatency(true);
                
                // Mettre à jour l'interface
                loadCurrentSettings();
                
                Toast.makeText(this, "🔄 Paramètres réinitialisés!", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Paramètres audio réinitialisés aux valeurs par défaut");
                
            } catch (Exception e) {
                Log.e(TAG, "Erreur lors de la réinitialisation: " + e.getMessage());
            }
        });
        builder.setNegativeButton("❌ Non", null);
        builder.show();
    }
    
    // Méthodes natives
    private native void setMasterVolume(int volume);
    private native void setAudioMuted(boolean muted);
    private native void setAudioQuality(int quality);
    private native void setSampleRate(int sampleRate);
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "Activité des paramètres audio reprise");
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "Activité des paramètres audio mise en pause");
    }
} 