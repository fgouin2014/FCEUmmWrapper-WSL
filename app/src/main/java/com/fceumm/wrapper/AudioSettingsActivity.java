package com.fceumm.wrapper;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.ArrayAdapter;
import android.util.Log;

public class AudioSettingsActivity extends Activity {
    private static final String TAG = "AudioSettingsActivity";
    
    static {
        System.loadLibrary("fceummwrapper");
    }
    
    private SeekBar volumeSeekBar;
    private TextView volumeText;
    private ToggleButton muteButton;
    private Spinner qualitySpinner;
    private Spinner sampleRateSpinner;
    private ToggleButton rfFilterButton;
    private Spinner stereoDelaySpinner;
    private ToggleButton swapDutyButton;
    private Button closeButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_settings);
        
        // Initialiser les vues
        volumeSeekBar = findViewById(R.id.volume_seekbar);
        volumeText = findViewById(R.id.volume_text);
        muteButton = findViewById(R.id.mute_button);
        qualitySpinner = findViewById(R.id.quality_spinner);
        sampleRateSpinner = findViewById(R.id.sample_rate_spinner);
        rfFilterButton = findViewById(R.id.rf_filter_button);
        stereoDelaySpinner = findViewById(R.id.stereo_delay_spinner);
        swapDutyButton = findViewById(R.id.swap_duty_button);
        closeButton = findViewById(R.id.close_button);
        
        // Configurer les spinners
        setupSpinners();
        
        // Configurer les listeners
        setupListeners();
        
        // S'assurer que libretro est initialisé
        ensureLibretroInitialized();
        
        // Charger les valeurs actuelles
        loadCurrentSettings();
    }
    
    private void setupSpinners() {
        // Qualité audio
        String[] qualities = {"Faible", "Élevée", "Maximum"};
        ArrayAdapter<String> qualityAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, qualities);
        qualityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        qualitySpinner.setAdapter(qualityAdapter);
        
        // Taux d'échantillonnage
        String[] sampleRates = {"22050 Hz", "44100 Hz", "48000 Hz"};
        ArrayAdapter<String> sampleRateAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, sampleRates);
        sampleRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sampleRateSpinner.setAdapter(sampleRateAdapter);
        
        // Effet stéréo
        String[] stereoDelays = {"Désactivé", "1ms", "2ms", "3ms", "4ms", "5ms", "6ms", "7ms", "8ms", "9ms", "10ms", "15ms"};
        ArrayAdapter<String> stereoDelayAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, stereoDelays);
        stereoDelayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stereoDelaySpinner.setAdapter(stereoDelayAdapter);
    }
    
    private void setupListeners() {
        // Volume
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    volumeText.setText("Volume: " + progress + "%");
                    setMasterVolume(progress);
                    forceApplyAudioSettings(); // Forcer l'application
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // Mute
        muteButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setAudioMuted(!isChecked);
            forceApplyAudioSettings(); // Forcer l'application
        });
        
        // Qualité
        qualitySpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                setAudioQuality(position);
                forceApplyAudioSettings(); // Forcer l'application
            }
            
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        
        // Taux d'échantillonnage
        sampleRateSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                int[] sampleRates = {22050, 44100, 48000};
                setSampleRate(sampleRates[position]);
                forceApplyAudioSettings(); // Forcer l'application
            }
            
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        
        // Filtre RF
        rfFilterButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setRfFilter(isChecked);
            forceApplyAudioSettings(); // Forcer l'application
        });
        
        // Effet stéréo
        stereoDelaySpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                int[] delays = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15};
                setStereoDelay(delays[position]);
                forceApplyAudioSettings(); // Forcer l'application
            }
            
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        
        // Échange cycles de devoir
        swapDutyButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setSwapDuty(isChecked);
            forceApplyAudioSettings(); // Forcer l'application
        });
        
        // Fermer
        closeButton.setOnClickListener(v -> finish());
    }
    
    private void loadCurrentSettings() {
        try {
            // Charger les valeurs depuis les variables globales natives
            int volume = getMasterVolume();
            boolean muted = getAudioMuted();
            int quality = getAudioQuality();
            int sampleRate = getSampleRate();
            boolean rfFilter = getRfFilter();
            int stereoDelay = getStereoDelay();
            boolean swapDuty = getSwapDuty();
            
            // Appliquer les valeurs aux contrôles UI
            volumeSeekBar.setProgress(volume);
            volumeText.setText("Volume: " + volume + "%");
            muteButton.setChecked(muted);
            qualitySpinner.setSelection(quality);
            
            // Convertir le sample rate en index
            int sampleRateIndex = 1; // 44100 par défaut
            switch (sampleRate) {
                case 22050: sampleRateIndex = 0; break;
                case 44100: sampleRateIndex = 1; break;
                case 48000: sampleRateIndex = 2; break;
            }
            sampleRateSpinner.setSelection(sampleRateIndex);
            
            rfFilterButton.setChecked(rfFilter);
            
            // Convertir le délai stéréo en index
            int stereoDelayIndex = 0; // Désactivé par défaut
            if (stereoDelay > 0 && stereoDelay <= 10) {
                stereoDelayIndex = stereoDelay;
            } else if (stereoDelay == 15) {
                stereoDelayIndex = 11;
            }
            stereoDelaySpinner.setSelection(stereoDelayIndex);
            
            swapDutyButton.setChecked(swapDuty);
            
            Log.d(TAG, "Paramètres audio chargés: volume=" + volume + 
                      ", muted=" + muted + ", quality=" + quality + 
                      ", sampleRate=" + sampleRate + ", rfFilter=" + rfFilter + 
                      ", stereoDelay=" + stereoDelay + ", swapDuty=" + swapDuty);
            
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors du chargement des paramètres audio", e);
            // Utiliser les valeurs par défaut en cas d'erreur
            volumeSeekBar.setProgress(100);
            volumeText.setText("Volume: 100%");
            muteButton.setChecked(false);
            qualitySpinner.setSelection(1);
            sampleRateSpinner.setSelection(1);
            rfFilterButton.setChecked(false);
            stereoDelaySpinner.setSelection(0);
            swapDutyButton.setChecked(false);
        }
    }
    
    // Méthodes natives - utiliser les mêmes noms que MainActivity
    private native void setMasterVolume(int volume);
    private native void setAudioMuted(boolean muted);
    private native void setAudioQuality(int quality);
    private native void setSampleRate(int sampleRate);
    
    // Nouvelles méthodes natives pour les paramètres avancés
    private native void setRfFilter(boolean enabled);
    private native void setStereoDelay(int delay_ms);
    private native void setSwapDuty(boolean enabled);
    private native void ensureLibretroInitialized();
    
    // Fonctions natives pour récupérer les valeurs actuelles
    private native int getMasterVolume();
    private native int getAudioQuality();
    private native int getSampleRate();
    private native boolean getRfFilter();
    private native int getStereoDelay();
    private native boolean getSwapDuty();
    private native boolean getAudioMuted();
    
    // Fonction pour forcer l'application des paramètres
    private native void forceApplyAudioSettings();
    
    // Fonction pour forcer la réinitialisation audio complète
    private native void forceAudioReinit();
    
    // Fonction pour optimiser l'audio pour Duck Hunt
    private native void optimizeForDuckHunt();
    
    // Fonction pour basculer le mode de latence audio
    private native void toggleLowLatencyMode();
} 