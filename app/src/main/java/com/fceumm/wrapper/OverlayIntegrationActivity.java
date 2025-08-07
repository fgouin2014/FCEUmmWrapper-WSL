package com.fceumm.wrapper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;

import com.fceumm.wrapper.overlay.RetroArchOverlaySystem;

/**
 * Activité d'intégration du système d'overlays tactiles RetroArch
 * Démonstration complète du système d'overlays
 */
public class OverlayIntegrationActivity extends AppCompatActivity {
    
    private static final String TAG = "OverlayIntegration";
    
    // Système d'overlays
    private RetroArchOverlaySystem overlaySystem;
    
    // Vue de rendu
    private OverlayRenderView renderView;
    
    // Configuration
    private String currentOverlay = "nes.cfg";
    private boolean overlayEnabled = true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Mode plein écran
        getWindow().setFlags(
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        
        Log.d(TAG, "=== DÉBUT OverlayIntegrationActivity.onCreate() ===");
        
        // Initialiser le système d'overlays
        initOverlaySystem();
        
        // Créer la vue de rendu
        renderView = new OverlayRenderView(this);
        
        // Layout principal avec fond noir
        FrameLayout layout = new FrameLayout(this);
        layout.setBackgroundColor(0xFF000000); // Fond noir
        layout.addView(renderView);
        setContentView(layout);
        
        Log.d(TAG, "Layout créé avec succès");
        
        // Mettre à jour les dimensions d'écran et charger l'overlay après que la vue soit créée
        renderView.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Post runnable exécuté");
                updateScreenDimensions();
                loadOverlay(currentOverlay);
                
                // Forcer le premier redessin
                if (renderView != null) {
                    renderView.invalidate();
                    Log.d(TAG, "Premier redessin forcé");
                }
            }
        });
        
        Log.d(TAG, "=== FIN OverlayIntegrationActivity.onCreate() ===");
    }
    
    private void initOverlaySystem() {
        Log.d(TAG, "Initialisation du système d'overlays");
        
        // Initialiser le système principal
        overlaySystem = RetroArchOverlaySystem.getInstance(this);
        overlaySystem.setOverlayEnabled(overlayEnabled);
        overlaySystem.setOverlayOpacity(0.8f);
        
        // Configurer les callbacks
        overlaySystem.setInputListener(new RetroArchOverlaySystem.OnOverlayInputListener() {
            @Override
            public void onOverlayInput(int deviceId, boolean pressed) {
                handleOverlayInput(deviceId, pressed);
            }
        });
        
        Log.d(TAG, "Système d'overlays initialisé avec succès");
    }
    
    private void updateScreenDimensions() {
        // Obtenir les dimensions d'écran réelles
        android.util.DisplayMetrics displayMetrics = new android.util.DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        
        Log.d(TAG, "Dimensions d'écran détectées: " + width + "x" + height);
        
        // Mettre à jour le système d'overlays
        if (overlaySystem != null) {
            overlaySystem.updateScreenDimensions(width, height);
            overlaySystem.forceLayoutUpdate(); // Forcer la mise à jour du layout
            overlaySystem.setOverlayEnabled(true); // S'assurer que l'overlay est activé
            Log.d(TAG, "Dimensions d'écran mises à jour dans le système d'overlays");
        }
        
        // Forcer le redessin
        if (renderView != null) {
            renderView.invalidate();
            Log.d(TAG, "Redessin forcé après mise à jour des dimensions");
        }
    }
    
    private void loadOverlay(String cfgFileName) {
        try {
            Log.d(TAG, "Chargement de l'overlay: " + cfgFileName);
            
            overlaySystem.loadOverlay(cfgFileName);
            currentOverlay = cfgFileName;
            
            // S'assurer que l'overlay est activé après le chargement
            overlaySystem.setOverlayEnabled(true);
            
            Log.d(TAG, "Overlay chargé et activé: " + cfgFileName);
            
            // Forcer le redessin
            if (renderView != null) {
                renderView.invalidate();
                Log.d(TAG, "Redessin forcé après chargement de l'overlay");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors du chargement de l'overlay: " + cfgFileName, e);
        }
    }
    
    private void handleOverlayInput(int deviceId, boolean pressed) {
        // Mapping vers les actions du jeu
        String action = getActionForDeviceId(deviceId);
        
        Log.d(TAG, "Input overlay: " + action + " " + (pressed ? "PRESSED" : "RELEASED"));
        
        // Ici, vous pouvez intégrer avec votre système de jeu
        // Par exemple, envoyer à libretro ou à votre moteur de jeu
        
        // Exemple d'intégration avec libretro
        if (pressed) {
            // handleLibretroInput(deviceId, true);
        } else {
            // handleLibretroInput(deviceId, false);
        }
    }
    
    private String getActionForDeviceId(int deviceId) {
        switch (deviceId) {
            case RetroArchOverlaySystem.RETRO_DEVICE_ID_JOYPAD_A: return "A";
            case RetroArchOverlaySystem.RETRO_DEVICE_ID_JOYPAD_B: return "B";
            case RetroArchOverlaySystem.RETRO_DEVICE_ID_JOYPAD_X: return "X";
            case RetroArchOverlaySystem.RETRO_DEVICE_ID_JOYPAD_Y: return "Y";
            case RetroArchOverlaySystem.RETRO_DEVICE_ID_JOYPAD_UP: return "UP";
            case RetroArchOverlaySystem.RETRO_DEVICE_ID_JOYPAD_DOWN: return "DOWN";
            case RetroArchOverlaySystem.RETRO_DEVICE_ID_JOYPAD_LEFT: return "LEFT";
            case RetroArchOverlaySystem.RETRO_DEVICE_ID_JOYPAD_RIGHT: return "RIGHT";
            case RetroArchOverlaySystem.RETRO_DEVICE_ID_JOYPAD_START: return "START";
            case RetroArchOverlaySystem.RETRO_DEVICE_ID_JOYPAD_SELECT: return "SELECT";
            case RetroArchOverlaySystem.RETRO_DEVICE_ID_JOYPAD_L: return "L";
            case RetroArchOverlaySystem.RETRO_DEVICE_ID_JOYPAD_R: return "R";
            case RetroArchOverlaySystem.RETRO_DEVICE_ID_JOYPAD_L2: return "L2";
            case RetroArchOverlaySystem.RETRO_DEVICE_ID_JOYPAD_R2: return "R2";
            case RetroArchOverlaySystem.RETRO_DEVICE_ID_JOYPAD_L3: return "L3";
            case RetroArchOverlaySystem.RETRO_DEVICE_ID_JOYPAD_R3: return "R3";
            default: return "UNKNOWN";
        }
    }
    
    /**
     * Vue de rendu des overlays
     */
    private class OverlayRenderView extends View {
        
        private Paint backgroundPaint;
        private Paint debugPaint;
        
        public OverlayRenderView(Context context) {
            super(context);
            initPaints();
            Log.d(TAG, "OverlayRenderView créée");
        }
        
        private void initPaints() {
            backgroundPaint = new Paint();
            backgroundPaint.setColor(0xFF000000); // Noir
            
            debugPaint = new Paint();
            debugPaint.setColor(0x80FFFFFF); // Blanc semi-transparent
            debugPaint.setTextSize(24);
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            
            Log.d(TAG, "onDraw appelé - Canvas: " + canvas.getWidth() + "x" + canvas.getHeight());
            
            // Dessiner le fond
            canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);
            
            // Dessiner les overlays
            if (overlaySystem != null && overlaySystem.isOverlayEnabled()) {
                Log.d(TAG, "Rendu des overlays - Enabled: " + overlaySystem.isOverlayEnabled() + 
                      ", ActiveOverlay: " + (overlaySystem.getActiveOverlay() != null));
                overlaySystem.render(canvas);
            } else {
                Log.d(TAG, "Overlays non rendus - System: " + (overlaySystem != null) + 
                      ", Enabled: " + (overlaySystem != null ? overlaySystem.isOverlayEnabled() : "null"));
            }
            
            // Debug: afficher les informations
            drawDebugInfo(canvas);
            
            // Redessiner seulement si nécessaire
            if (overlaySystem != null && overlaySystem.isOverlayEnabled()) {
                postInvalidateDelayed(500); // Redessiner toutes les 500ms
            }
        }
        
        private void drawDebugInfo(Canvas canvas) {
            String info = "Overlay: " + currentOverlay + "\n";
            info += "Enabled: " + overlayEnabled + "\n";
            info += "System Enabled: " + (overlaySystem != null ? overlaySystem.isOverlayEnabled() : "null") + "\n";
            info += "Active Overlay: " + (overlaySystem != null && overlaySystem.getActiveOverlay() != null ? "Yes" : "No") + "\n";
            info += "Touch to test overlays\n";
            info += "Canvas: " + canvas.getWidth() + "x" + canvas.getHeight();
            
            canvas.drawText(info, 50, 100, debugPaint);
        }
        
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            Log.d(TAG, "Touch event: " + event.getAction() + " at " + event.getX() + "," + event.getY());
            
            // Gérer les touches pour les overlays
            if (overlaySystem != null && overlaySystem.isOverlayEnabled()) {
                boolean handled = overlaySystem.handleTouch(event);
                if (handled) {
                    invalidate(); // Redessiner si un overlay a été touché
                    Log.d(TAG, "Touch géré par overlay");
                    return true;
                }
            }
            
            // Toujours redessiner pour s'assurer que les overlays sont visibles
            invalidate();
            Log.d(TAG, "Touch non géré, redessin forcé");
            return super.onTouchEvent(event);
        }
    }
    
    // Méthodes publiques pour la configuration
    
    public void setOverlayEnabled(boolean enabled) {
        this.overlayEnabled = enabled;
        if (overlaySystem != null) {
            overlaySystem.setOverlayEnabled(enabled);
        }
        Log.d(TAG, "Overlay " + (enabled ? "activé" : "désactivé"));
    }
    
    public void setOverlayOpacity(float opacity) {
        if (overlaySystem != null) {
            overlaySystem.setOverlayOpacity(opacity);
        }
    }
    
    public void loadOverlayConfig(String cfgFileName) {
        loadOverlay(cfgFileName);
    }
    
    // Méthodes pour changer d'overlay
    
    public void loadNESOverlay() {
        loadOverlay("nes.cfg");
    }
    
    public void loadRetroPadOverlay() {
        loadOverlay("retropad.cfg");
    }
    
    public void loadSNESOverlay() {
        loadOverlay("snes.cfg");
    }
    
    public void loadGBAOverlay() {
        loadOverlay("gba.cfg");
    }
    
    public void loadGenesisOverlay() {
        loadOverlay("genesis.cfg");
    }
    
    public void loadArcadeOverlay() {
        loadOverlay("arcade.cfg");
    }
    
    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        Log.d(TAG, "Configuration changée - Orientation: " + 
              (newConfig.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE ? "Landscape" : "Portrait"));
        
        // Mettre à jour les dimensions d'écran après le changement d'orientation
        renderView.post(new Runnable() {
            @Override
            public void run() {
                updateScreenDimensions();
                // Recharger l'overlay actuel pour s'assurer qu'il s'affiche correctement
                if (currentOverlay != null) {
                    loadOverlay(currentOverlay);
                }
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Nettoyer les ressources
        if (overlaySystem != null) {
            overlaySystem.setOverlayEnabled(false);
        }
        
        Log.d(TAG, "Activité détruite");
    }
} 