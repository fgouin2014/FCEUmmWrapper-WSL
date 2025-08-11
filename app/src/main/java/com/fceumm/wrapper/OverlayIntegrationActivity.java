package com.fceumm.wrapper;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Button;
import android.graphics.Color;
import com.fceumm.wrapper.overlay.RetroArchOverlaySystem;
import com.fceumm.wrapper.overlay.OverlayRenderView;
import com.fceumm.wrapper.overlay.RetroArchInputTest;

/**
 * Activité de test simple pour forcer l'affichage de l'overlay
 */
public class OverlayIntegrationActivity extends Activity {
    private static final String TAG = "OverlayTest";
    
    private FrameLayout mainContainer;
    private OverlayRenderView overlayRenderView;
    private RetroArchOverlaySystem overlaySystem;
    private Button testButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "[START] OverlayIntegrationActivity.onCreate() - Test d'overlay");
        
        // Créer le layout programmatiquement
        createTestLayout();
        
        // Initialiser le système d'overlay
        initOverlaySystem();
        
        Log.i(TAG, "[OK] OverlayIntegrationActivity initialisée");
    }
    
    private void createTestLayout() {
        // Container principal
        mainContainer = new FrameLayout(this);
        mainContainer.setLayoutParams(new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ));
        mainContainer.setBackgroundColor(Color.BLACK);
        
        // Overlay render view
        overlayRenderView = new OverlayRenderView(this);
        overlayRenderView.setLayoutParams(new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ));
        overlayRenderView.setBackgroundColor(Color.TRANSPARENT);
        
        // Bouton de test
        testButton = new Button(this);
        testButton.setText("🎮 TEST OVERLAY");
        testButton.setTextColor(Color.WHITE);
        testButton.setBackgroundColor(Color.RED);
        testButton.setLayoutParams(new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ));
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "🎮 Bouton test pressé - Forcer l'overlay");
                forceOverlayDisplay();
            }
        });
        
        // Ajouter les vues
        mainContainer.addView(overlayRenderView);
        mainContainer.addView(testButton);
        
        setContentView(mainContainer);
    }
    
    private void initOverlaySystem() {
        Log.i(TAG, "[GAME] Initialisation du système d'overlay");
        
        // Obtenir l'instance du système d'overlay
        overlaySystem = RetroArchOverlaySystem.getInstance(this);
        
        // Configurer l'overlay render view
        overlayRenderView.setOverlaySystem(overlaySystem);
        
        // Forcer l'activation de l'overlay
        overlaySystem.setOverlayEnabled(true);
        
        // Charger un overlay de test
        overlaySystem.loadOverlay("nes.cfg");
        
        // Forcer le redessinage
        overlayRenderView.invalidate();
        
        // **100% RETROARCH NATIF** : Tests de compatibilité
        RetroArchInputTest.runAllTests();
        
        Log.i(TAG, "[OK] Système d'overlay initialisé");
    }
    
    private void forceOverlayDisplay() {
        Log.i(TAG, "[GAME] Force overlay display");
        
        // Forcer l'activation
        overlaySystem.setOverlayEnabled(true);
        
        // Charger un overlay spécifique
        overlaySystem.loadOverlay("nes.cfg");
        
        // Forcer le redessinage
        overlayRenderView.invalidate();
        
        Log.i(TAG, "[OK] Overlay forcé - Vérifiez l'écran");
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "[GAME] OverlayIntegrationActivity.onResume()");
        
        // Forcer l'affichage de l'overlay
        if (overlaySystem != null) {
            overlaySystem.setOverlayEnabled(true);
            overlayRenderView.invalidate();
        }
    }
} 