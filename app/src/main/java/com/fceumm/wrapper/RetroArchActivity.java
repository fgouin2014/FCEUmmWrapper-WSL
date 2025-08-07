package com.fceumm.wrapper;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.content.res.Configuration;
import com.fceumm.wrapper.overlay.RetroArchOverlaySystem;
import com.fceumm.wrapper.overlay.OverlayRenderView;

/**
 * Activité principale RetroArch - Structure officielle
 * Reproduit exactement l'architecture de RetroArch Android
 */
public class RetroArchActivity extends Activity {
    private static final String TAG = "RetroArchActivity";
    
    // Composants UI
    private EmulatorView emulatorView;
    private OverlayRenderView overlayRenderView;
    private FrameLayout gameViewport;
    private LinearLayout controlsArea;
    
    // Système d'overlays
    private RetroArchOverlaySystem overlaySystem;
    
    // État de l'application
    private boolean isRunning = false;
    private boolean isPaused = false;
    
    static {
        System.loadLibrary("fceummwrapper");
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "🚀 RetroArchActivity.onCreate() - Démarrage de l'activité RetroArch");
        
        // Configuration plein écran
        setupFullscreen();
        
        // Utiliser le layout RetroArch officiel
        setContentView(R.layout.activity_retroarch);
        
        // Initialiser les composants UI
        initUIComponents();
        
        // Initialiser le système d'overlays
        initOverlaySystem();
        
        // Configurer le layout selon l'orientation
        adjustLayoutForOrientation();
        
        Log.i(TAG, "✅ RetroArchActivity initialisée avec succès");
    }
    
    /**
     * Configuration plein écran comme RetroArch officiel
     */
    private void setupFullscreen() {
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN |
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_FULLSCREEN |
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        
        // Masquer les barres système
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    getWindow().setDecorFitsSystemWindows(false);
                    if (getWindow().getInsetsController() != null) {
                        getWindow().getInsetsController().hide(
                            android.view.WindowInsets.Type.statusBars() | 
                            android.view.WindowInsets.Type.navigationBars() |
                            android.view.WindowInsets.Type.systemBars()
                        );
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
    }
    
    /**
     * Initialiser les composants UI
     */
    private void initUIComponents() {
        Log.d(TAG, "Initialisation des composants UI");
        
        // Récupérer les vues principales
        gameViewport = findViewById(R.id.game_viewport);
        controlsArea = findViewById(R.id.controls_area);
        emulatorView = findViewById(R.id.emulator_view);
        overlayRenderView = findViewById(R.id.overlay_render_view);
        
        if (gameViewport == null) Log.e(TAG, "❌ gameViewport non trouvé");
        if (controlsArea == null) Log.e(TAG, "❌ controlsArea non trouvé");
        if (emulatorView == null) Log.e(TAG, "❌ emulatorView non trouvé");
        if (overlayRenderView == null) Log.e(TAG, "❌ overlayRenderView non trouvé");
        
        Log.d(TAG, "✅ Composants UI initialisés");
    }
    
    /**
     * Initialiser le système d'overlays RetroArch
     */
    private void initOverlaySystem() {
        Log.d(TAG, "Initialisation du système d'overlays RetroArch");
        
        // Obtenir l'instance du système d'overlays
        overlaySystem = RetroArchOverlaySystem.getInstance(this);
        
        // Configurer l'overlay render view
        if (overlayRenderView != null) {
            overlayRenderView.setOverlaySystem(overlaySystem);
        }
        
        // Configurer le listener d'input
        overlaySystem.setInputListener(new RetroArchOverlaySystem.OnOverlayInputListener() {
            @Override
            public void onOverlayInput(int deviceId, boolean pressed) {
                Log.d(TAG, "🎮 Input RetroArch: " + deviceId + " -> " + pressed);
                handleRetroArchInput(deviceId, pressed);
            }
        });
        
        // Charger l'overlay par défaut
        loadDefaultOverlay();
        
        Log.d(TAG, "✅ Système d'overlays RetroArch initialisé");
    }
    
    /**
     * Charger l'overlay par défaut
     */
    private void loadDefaultOverlay() {
        try {
            // Charger l'overlay NES par défaut
            overlaySystem.loadOverlay("nes.cfg");
            overlaySystem.setOverlayEnabled(true);
            
            Log.i(TAG, "✅ Overlay par défaut chargé: nes.cfg");
        } catch (Exception e) {
            Log.e(TAG, "❌ Erreur lors du chargement de l'overlay par défaut", e);
        }
    }
    
    /**
     * Ajuster le layout selon l'orientation
     */
    private void adjustLayoutForOrientation() {
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        
        Log.d(TAG, "🔄 Ajustement du layout - Orientation: " + (isPortrait ? "PORTRAIT" : "PAYSAGE"));
        
        if (isPortrait) {
            // Mode Portrait : Split screen 50/50
            setupPortraitLayout();
        } else {
            // Mode Paysage : Full screen
            setupLandscapeLayout();
        }
    }
    
    /**
     * Configuration du layout portrait
     */
    private void setupPortraitLayout() {
        Log.d(TAG, "📱 Configuration layout portrait (50/50)");
        
        if (controlsArea != null) {
            controlsArea.setVisibility(View.VISIBLE);
        }
        
        if (gameViewport != null) {
            // Ajuster la hauteur du game viewport pour laisser de la place aux contrôles
            android.view.ViewGroup.LayoutParams params = gameViewport.getLayoutParams();
            if (params instanceof android.widget.RelativeLayout.LayoutParams) {
                android.widget.RelativeLayout.LayoutParams relativeParams = (android.widget.RelativeLayout.LayoutParams) params;
                relativeParams.addRule(android.widget.RelativeLayout.ABOVE, R.id.controls_area);
                gameViewport.setLayoutParams(relativeParams);
            }
        }
        
        Log.d(TAG, "✅ Layout portrait configuré (50% jeu, 50% contrôles)");
    }
    
    /**
     * Configuration du layout paysage
     */
    private void setupLandscapeLayout() {
        Log.d(TAG, "🖥️ Configuration layout paysage");
        
        if (controlsArea != null) {
            controlsArea.setVisibility(View.GONE);
        }
        
        if (gameViewport != null) {
            // Game viewport plein écran
            android.view.ViewGroup.LayoutParams params = gameViewport.getLayoutParams();
            if (params instanceof android.widget.RelativeLayout.LayoutParams) {
                android.widget.RelativeLayout.LayoutParams relativeParams = (android.widget.RelativeLayout.LayoutParams) params;
                relativeParams.addRule(android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM);
                gameViewport.setLayoutParams(relativeParams);
            }
        }
        
        Log.d(TAG, "✅ Layout paysage configuré");
    }
    
    /**
     * Gérer les inputs RetroArch
     */
    private void handleRetroArchInput(int deviceId, boolean pressed) {
        // Mapping des device IDs vers les boutons libretro
        switch (deviceId) {
            case 8: // RETRO_DEVICE_ID_JOYPAD_A
                setJoypadButton(8, pressed);
                break;
            case 0: // RETRO_DEVICE_ID_JOYPAD_B
                setJoypadButton(0, pressed);
                break;
            case 9: // RETRO_DEVICE_ID_JOYPAD_X
                setJoypadButton(9, pressed);
                break;
            case 1: // RETRO_DEVICE_ID_JOYPAD_Y
                setJoypadButton(1, pressed);
                break;
            case 4: // RETRO_DEVICE_ID_JOYPAD_UP
                setJoypadButton(4, pressed);
                break;
            case 5: // RETRO_DEVICE_ID_JOYPAD_DOWN
                setJoypadButton(5, pressed);
                break;
            case 6: // RETRO_DEVICE_ID_JOYPAD_LEFT
                setJoypadButton(6, pressed);
                break;
            case 7: // RETRO_DEVICE_ID_JOYPAD_RIGHT
                setJoypadButton(7, pressed);
                break;
            case 3: // RETRO_DEVICE_ID_JOYPAD_START
                setJoypadButton(3, pressed);
                break;
            case 2: // RETRO_DEVICE_ID_JOYPAD_SELECT
                setJoypadButton(2, pressed);
                break;
            default:
                Log.d(TAG, "Input non géré: " + deviceId + " -> " + pressed);
                break;
        }
    }
    
    /**
     * Méthode native pour envoyer les inputs au core libretro
     */
    private native void setJoypadButton(int buttonId, boolean pressed);
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "🔄 Configuration changée - Nouvelle orientation détectée");
        
        // Ajuster le layout pour la nouvelle orientation
        adjustLayoutForOrientation();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "▶️ RetroArchActivity.onResume()");
        
        // Redémarrer l'émulation si nécessaire
        if (isRunning && !isPaused) {
            // Logique de reprise
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "⏸️ RetroArchActivity.onPause()");
        
        // Mettre en pause l'émulation
        isPaused = true;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "🛑 RetroArchActivity.onDestroy()");
        
        // Nettoyer les ressources
        if (overlaySystem != null) {
            overlaySystem.setOverlayEnabled(false);
        }
    }
} 