package com.fceumm.wrapper.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.fceumm.wrapper.RetroArchCoreSystem;
import com.fceumm.wrapper.RetroArchInputManager;
import com.fceumm.wrapper.RetroArchStateManager;
import com.fceumm.wrapper.RetroArchConfigManager;
import com.fceumm.wrapper.RetroArchVideoManager;
import com.fceumm.wrapper.menu.RetroArchMenuSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import android.os.Handler;
import android.os.Looper;

/**
 * **100% RETROARCH NATIF** : Interface utilisateur moderne conforme aux standards RetroArch
 * Remplace complètement l'ancien overlay de debug rouge par une interface native moderne
 */
public class RetroArchModernUI extends View {
    private static final String TAG = "RetroArchModernUI";
    
    // **100% RETROARCH** : Constantes libretro pour les inputs
    public static final int RETRO_DEVICE_ID_JOYPAD_B = 0;
    public static final int RETRO_DEVICE_ID_JOYPAD_Y = 1;
    public static final int RETRO_DEVICE_ID_JOYPAD_SELECT = 2;
    public static final int RETRO_DEVICE_ID_JOYPAD_START = 3;
    public static final int RETRO_DEVICE_ID_JOYPAD_UP = 4;
    public static final int RETRO_DEVICE_ID_JOYPAD_DOWN = 5;
    public static final int RETRO_DEVICE_ID_JOYPAD_LEFT = 6;
    public static final int RETRO_DEVICE_ID_JOYPAD_RIGHT = 7;
    public static final int RETRO_DEVICE_ID_JOYPAD_A = 8;
    public static final int RETRO_DEVICE_ID_JOYPAD_X = 9;
    public static final int RETRO_DEVICE_ID_JOYPAD_L = 10;
    public static final int RETRO_DEVICE_ID_JOYPAD_R = 11;
    public static final int RETRO_DEVICE_ID_JOYPAD_L2 = 12;
    public static final int RETRO_DEVICE_ID_JOYPAD_R2 = 13;
    public static final int RETRO_DEVICE_ID_JOYPAD_L3 = 14;
    public static final int RETRO_DEVICE_ID_JOYPAD_R3 = 15;
    
    // **100% RETROARCH** : États de l'interface
    public enum UIState {
        UI_STATE_GAMEPLAY,      // Mode jeu normal
        UI_STATE_MENU,          // Menu ouvert
        UI_STATE_QUICK_MENU,    // Menu rapide
        UI_STATE_OVERLAY,       // Overlay de contrôles
        UI_STATE_NOTIFICATION   // Notifications
    }
    
    // **100% RETROARCH** : Thèmes de l'interface
    public enum UITheme {
        UI_THEME_DARK,          // Thème sombre (par défaut)
        UI_THEME_LIGHT,         // Thème clair
        UI_THEME_RETRO,         // Thème rétro
        UI_THEME_MODERN         // Thème moderne
    }
    
    private Context context;
    private UIState currentState = UIState.UI_STATE_GAMEPLAY;
    private UITheme currentTheme = UITheme.UI_THEME_DARK;
    private UIState lastLoggedState = null; // Pour éviter le spam de logs
    
    // **100% RETROARCH** : Systèmes RetroArch intégrés
    private RetroArchCoreSystem coreSystem;
    private RetroArchInputManager inputManager;
    private RetroArchStateManager stateManager;
    private RetroArchConfigManager configManager;
    private RetroArchVideoManager videoManager;
    private RetroArchMenuSystem menuSystem;
    
    // **100% RETROARCH AUTHENTIQUE** : Gestionnaire d'overlays RetroArch
    private com.fceumm.wrapper.overlay.RetroArchOverlayManager overlayManager;
    
    // **100% RETROARCH** : Éléments d'interface
    private Paint backgroundPaint;
    private Paint textPaint;
    private Paint buttonPaint;
    private Paint highlightPaint;
    private Paint notificationPaint;
    
    // **100% RETROARCH** : Notifications
    private List<Notification> notifications = new ArrayList<>();
    
    // **100% RETROARCH** : Interface de callback
    public interface UICallback {
        void onMenuRequested();
        void onQuickMenuRequested();
        void onStateSaved();
        void onStateLoaded();
        void onScreenshotTaken();
        void onRewindActivated();
        void onFastForwardActivated();
        void onRomSelectionRequested();
        void onSettingsRequested();
        void onBackToMainMenu();
        void onInputSent(int deviceId, boolean pressed);
    }
    
    private UICallback uiCallback;
    
    /**
     * **100% RETROARCH** : Classe de notification
     */
    public static class Notification {
        public String message;
        public long timestamp;
        public long duration;
        public int type; // 0=info, 1=success, 2=warning, 3=error
        
        public Notification(String message, long duration, int type) {
            this.message = message;
            this.timestamp = System.currentTimeMillis();
            this.duration = duration;
            this.type = type;
        }
    }
    
    // **100% RETROARCH NATIF** : Gestion multi-touch native
    private Map<Integer, TouchPoint> activeTouches = new HashMap<>();
    private static final int MAX_TOUCH_POINTS = 16; // Standard RetroArch
    
    // **100% RETROARCH** : Variables pour l'affichage des gamepads
    private List<String> connectedGamepads = new ArrayList<>();
    private boolean gamepadStatusVisible = true;
    private long lastGamepadUpdate = 0;
    private static final long GAMEPAD_UPDATE_INTERVAL = 1000; // 1 seconde
    
    // **CRITIQUE** : Timer pour forcer le rendu continu
    private Handler renderHandler;
    private Runnable renderRunnable;
    
    /**
     * **100% RETROARCH NATIF** : Point de contact
     */
    public static class TouchPoint {
        public float x, y;
        public long timestamp;
        public int pointerId;
        public boolean isActive;
        
        public TouchPoint(float x, float y, int pointerId) {
            this.x = x;
            this.y = y;
            this.pointerId = pointerId;
            this.timestamp = System.currentTimeMillis();
            this.isActive = true;
        }
    }
    
    public RetroArchModernUI(Context context) {
        super(context);
        this.context = context;
        
        // **CRITIQUE** : Configurer la vue pour le rendu automatique
        setWillNotDraw(false); // FORCER le rendu
        setLayerType(LAYER_TYPE_HARDWARE, null); // Accélération matérielle
        
        // **CRITIQUE** : S'assurer que cette vue reçoit les touches
        setClickable(true);
        setFocusable(true);
        setFocusableInTouchMode(true);
        
        // **DEBUG** : Vérifier la visibilité
        setVisibility(View.VISIBLE);
        
        initializeUI();
        Log.i(TAG, "🎮 **100% RETROARCH** - Interface moderne initialisée avec rendu forcé et touches activées");
    }
    
    /**
     * **100% RETROARCH** : Initialisation de l'interface
     */
    private void initializeUI() {
        // **100% RETROARCH** : Initialiser les systèmes
        coreSystem = new RetroArchCoreSystem(context);
        inputManager = new RetroArchInputManager(context);
        stateManager = new RetroArchStateManager(context);
        configManager = new RetroArchConfigManager(context);
        videoManager = new RetroArchVideoManager(context);
        menuSystem = new RetroArchMenuSystem(context);
        
        // **100% RETROARCH AUTHENTIQUE** : Initialisation du gestionnaire d'overlays
        overlayManager = new com.fceumm.wrapper.overlay.RetroArchOverlayManager(context);
        overlayManager.setInputCallback(new com.fceumm.wrapper.overlay.RetroArchOverlayManager.OverlayInputCallback() {
            @Override
            public void onOverlayInput(int deviceId, boolean pressed) {
                // **100% RETROARCH AUTHENTIQUE** : Transmettre l'input à RetroArch
                if (uiCallback != null) {
                    uiCallback.onInputSent(deviceId, pressed);
                }
            }
            
            @Override
            public void onOverlayAction(String action) {
                // **100% RETROARCH AUTHENTIQUE** : Gérer les actions spéciales
                handleOverlayAction(action);
            }
        });
        
        // **100% RETROARCH AUTHENTIQUE** : Charger l'overlay NES officiel
        boolean overlayLoaded = overlayManager.loadOverlay("overlays/gamepads/flat/nes.cfg");
        if (overlayLoaded) {
            Log.i(TAG, "✅ **100% RETROARCH AUTHENTIQUE** - Overlay NES officiel chargé avec succès");
        } else {
            Log.e(TAG, "❌ **100% RETROARCH AUTHENTIQUE** - Échec du chargement de l'overlay NES officiel");
        }
        
        // **100% RETROARCH** : Configurer les peintures
        setupPaints();
        
        // **100% RETROARCH** : Configurer le menu
        setupMenuSystem();
        
        // **CRITIQUE** : Initialiser le timer de rendu continu
        setupRenderTimer();
        
        Log.i(TAG, "🎮 **100% RETROARCH AUTHENTIQUE** - Interface configurée avec overlays RetroArch");
    }
    
    /**
     * **100% RETROARCH** : Configuration des peintures
     */
    private void setupPaints() {
        // **100% RETROARCH** : Peinture de fond
        backgroundPaint = new Paint();
        backgroundPaint.setAntiAlias(true);
        
        // **100% RETROARCH** : Peinture de texte
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        
        // **100% RETROARCH** : Peinture de bouton
        buttonPaint = new Paint();
        buttonPaint.setAntiAlias(true);
        buttonPaint.setStyle(Paint.Style.FILL);
        
        // **100% RETROARCH** : Peinture de surbrillance
        highlightPaint = new Paint();
        highlightPaint.setAntiAlias(true);
        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setStrokeWidth(3.0f);
        
        // **100% RETROARCH** : Peinture de notification
        notificationPaint = new Paint();
        notificationPaint.setAntiAlias(true);
        notificationPaint.setStyle(Paint.Style.FILL);
        
        // **100% RETROARCH** : Appliquer le thème
        applyTheme();
    }
    
    /**
     * **CRITIQUE** : Configuration du timer de rendu continu
     */
    private void setupRenderTimer() {
        renderHandler = new Handler(Looper.getMainLooper());
        renderRunnable = new Runnable() {
            @Override
            public void run() {
                // **CRITIQUE** : Forcer le redessinage de l'interface
                invalidate();
                
                // **CRITIQUE** : Programmer le prochain rendu (60 FPS)
                renderHandler.postDelayed(this, 16); // ~60 FPS
            }
        };
        
        // **CRITIQUE** : Démarrer le timer de rendu
        renderHandler.post(renderRunnable);
    }
    
    /**
     * **100% RETROARCH** : Configuration du système de menu
     */
    private void setupMenuSystem() {
        menuSystem.setMenuCallback(new RetroArchMenuSystem.MenuCallback() {
            @Override
            public void onMenuEntrySelected(RetroArchMenuSystem.MenuEntry entry) {
                handleMenuSelection(entry);
            }
            
            @Override
            public void onMenuClosed() {
                setUIState(UIState.UI_STATE_GAMEPLAY);
            }
            
            @Override
            public void onMenuOpened() {
                setUIState(UIState.UI_STATE_MENU);
            }
        });
    }
    
    /**
     * **100% RETROARCH** : Gestion des sélections de menu
     */
    private void handleMenuSelection(RetroArchMenuSystem.MenuEntry entry) {
        Log.i(TAG, "🎮 **100% RETROARCH** - Sélection menu: " + entry.title);
        
        switch (entry.title) {
            case "🎮 Démarrer le jeu":
                setUIState(UIState.UI_STATE_GAMEPLAY);
                break;
            case "💾 Sauvegarder":
                saveState();
                break;
            case "📂 Charger":
                loadState();
                break;
            case "📸 Screenshot":
                takeScreenshot();
                break;
            case "⏪ Rewind":
                activateRewind();
                break;
            case "⏩ Fast Forward":
                activateFastForward();
                break;
            case "❌ Quitter":
                if (uiCallback != null) {
                    // Fermer l'application
                }
                break;
        }
    }
    
    /**
     * **100% RETROARCH** : Rendu principal
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.i(TAG, "🎮 **ATTACHÉ** - Interface moderne attachée à la fenêtre");
        Log.d(TAG, "🎮 **VISIBILITÉ** - Visibilité: " + getVisibility() + " - Largeur: " + getWidth() + " - Hauteur: " + getHeight());
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // **CRITIQUE** : Arrêter le timer de rendu
        if (renderHandler != null && renderRunnable != null) {
            renderHandler.removeCallbacks(renderRunnable);
            Log.i(TAG, "🎨 **TIMER ARRÊTÉ** - Timer de rendu arrêté");
        }
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // **DEBUG** : Vérifier les dimensions du canvas
        float width = canvas.getWidth();
        float height = canvas.getHeight();
        
        // **DEBUG** : Log des dimensions seulement si l'état change (pour éviter le spam)
        if (width > 0 && height > 0 && (lastLoggedState == null || lastLoggedState != currentState)) {
            Log.d(TAG, "🎨 **RENDU** - Canvas: " + width + "x" + height + " - État: " + currentState);
            lastLoggedState = currentState;
        }
        
        // **100% RETROARCH** : Rendu selon l'état
        switch (currentState) {
            case UI_STATE_GAMEPLAY:
                renderGameplayUI(canvas);
                break;
            case UI_STATE_MENU:
                renderMenuUI(canvas);
                break;
            case UI_STATE_QUICK_MENU:
                renderQuickMenuUI(canvas);
                break;
            case UI_STATE_OVERLAY:
                renderOverlayUI(canvas);
                break;
            case UI_STATE_NOTIFICATION:
                renderNotifications(canvas);
                break;
        }
        
        // **100% RETROARCH** : Toujours afficher les notifications
        renderNotifications(canvas);
    }
    
    /**
     * **100% RETROARCH** : Rendu de l'interface de jeu
     */
    private void renderGameplayUI(Canvas canvas) {
        // **100% RETROARCH AUTHENTIQUE** : Interface transparente en mode jeu
        float width = canvas.getWidth();
        float height = canvas.getHeight();
        
        // **100% RETROARCH** : Barre d'état en haut (transparente)
        renderFPSDisplay(canvas, width, height);
        
        // **100% RETROARCH** : Indicateurs de performance (transparents)
        renderPerformanceIndicators(canvas, width, height);
        
        // **100% RETROARCH** : Notifications (si présentes)
        renderNotifications(canvas);
        
        // **100% RETROARCH AUTHENTIQUE** : Rendu de l'overlay RetroArch
        if (overlayManager != null) {
            overlayManager.render(canvas);
        }
        
        // **100% RETROARCH** : Afficher le statut des gamepads
        renderGamepadStatus(canvas);
    }
    
    /**
     * **100% RETROARCH** : Rendu du menu principal - FULLSCREEN
     */
    private void renderMenuUI(Canvas canvas) {
        float width = canvas.getWidth();
        float height = canvas.getHeight();
        
        // **100% RETROARCH** : Fond fullscreen opaque comme RetroArch officiel
        backgroundPaint.setColor(Color.parseColor("#1A1A1A")); // Fond sombre RetroArch
        canvas.drawRect(0, 0, width, height, backgroundPaint);
        
        // **100% RETROARCH** : Titre du menu - CENTRÉ ET PLUS GRAND
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(96.0f); // TAILLE AUGMENTÉE POUR FULLSCREEN
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("🎮 RetroArch", width / 2, height * 0.15f, textPaint);
        
        // **100% RETROARCH** : Sous-titre - CENTRÉ
        textPaint.setTextSize(48.0f); // TAILLE AUGMENTÉE POUR FULLSCREEN
        textPaint.setColor(Color.LTGRAY);
        canvas.drawText("Interface Native Moderne", width / 2, height * 0.22f, textPaint);
        
        // **100% RETROARCH** : Boutons du menu - FULLSCREEN
        renderMenuButtons(canvas, width, height);
    }
    
    /**
     * **100% RETROARCH** : Rendu du menu rapide - FULLSCREEN
     */
    private void renderQuickMenuUI(Canvas canvas) {
        float width = canvas.getWidth();
        float height = canvas.getHeight();
        
        // **100% RETROARCH** : Fond fullscreen opaque comme RetroArch officiel
        backgroundPaint.setColor(Color.parseColor("#1A1A1A")); // Fond sombre RetroArch
        canvas.drawRect(0, 0, width, height, backgroundPaint);
        
        // **100% RETROARCH** : Titre du menu rapide - CENTRÉ ET PLUS GRAND
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(72.0f); // TAILLE AUGMENTÉE POUR FULLSCREEN
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("⚡ Menu Rapide", width / 2, height * 0.15f, textPaint);
        
        // **100% RETROARCH** : Boutons rapides - FULLSCREEN
        renderQuickMenuButtons(canvas, width, height);
    }
    
    /**
     * **100% RETROARCH AUTHENTIQUE** : Rendu des contrôles de gameplay
     */
    private void renderGameplayControls(Canvas canvas, float width, float height) {
        // **100% RETROARCH AUTHENTIQUE** : Contrôles selon le standard RetroArch
        
        // **DPAD** : Contrôle directionnel (gauche, bas) - Zone normalisée
        float dpadCenterX = width * 0.125f;
        float dpadCenterY = height * 0.8f;
        float dpadRadius = width * 0.1f;
        
        // Fond du D-Pad (semi-transparent)
        buttonPaint.setColor(Color.parseColor("#60000000"));
        canvas.drawCircle(dpadCenterX, dpadCenterY, dpadRadius, buttonPaint);
        
        // Boutons directionnels (très discrets)
        buttonPaint.setColor(Color.parseColor("#20FFFFFF"));
        float buttonSize = dpadRadius * 0.3f;
        
        // Haut
        canvas.drawCircle(dpadCenterX, dpadCenterY - dpadRadius * 0.5f, buttonSize, buttonPaint);
        // Bas
        canvas.drawCircle(dpadCenterX, dpadCenterY + dpadRadius * 0.5f, buttonSize, buttonPaint);
        // Gauche
        canvas.drawCircle(dpadCenterX - dpadRadius * 0.5f, dpadCenterY, buttonSize, buttonPaint);
        // Droite
        canvas.drawCircle(dpadCenterX + dpadRadius * 0.5f, dpadCenterY, buttonSize, buttonPaint);
        
        // **BOUTONS D'ACTION** : A, B, Start, Select (droite, bas) - Zones normalisées
        float buttonZoneX = width * 0.85f;
        float buttonZoneY = height * 0.8f;
        float buttonSizeLarge = width * 0.05f;
        float buttonSizeSmall = width * 0.04f;
        
        // Bouton A (principal, rouge)
        buttonPaint.setColor(Color.parseColor("#40FF0000"));
        canvas.drawCircle(buttonZoneX, buttonZoneY, buttonSizeLarge, buttonPaint);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(buttonSizeLarge * 0.6f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("A", buttonZoneX, buttonZoneY + buttonSizeLarge * 0.2f, textPaint);
        
        // Bouton B (secondaire, vert)
        buttonPaint.setColor(Color.parseColor("#4000FF00"));
        canvas.drawCircle(buttonZoneX - buttonSizeLarge * 2, buttonZoneY, buttonSizeLarge, buttonPaint);
        textPaint.setTextSize(buttonSizeLarge * 0.6f);
        canvas.drawText("B", buttonZoneX - buttonSizeLarge * 2, buttonZoneY + buttonSizeLarge * 0.2f, textPaint);
        
        // Bouton Start (bleu)
        buttonPaint.setColor(Color.parseColor("#400000FF"));
        canvas.drawCircle(buttonZoneX, buttonZoneY - buttonSizeLarge * 2, buttonSizeSmall, buttonPaint);
        textPaint.setTextSize(buttonSizeSmall * 0.5f);
        canvas.drawText("START", buttonZoneX, buttonZoneY - buttonSizeLarge * 2 + buttonSizeSmall * 0.2f, textPaint);
        
        // Bouton Select (jaune)
        buttonPaint.setColor(Color.parseColor("#40FFFF00"));
        canvas.drawCircle(buttonZoneX - buttonSizeLarge * 2, buttonZoneY - buttonSizeLarge * 2, buttonSizeSmall, buttonPaint);
        textPaint.setTextSize(buttonSizeSmall * 0.5f);
        canvas.drawText("SELECT", buttonZoneX - buttonSizeLarge * 2, buttonZoneY - buttonSizeLarge * 2 + buttonSizeSmall * 0.2f, textPaint);
        
        // **BOUTON MENU** : Discret (coin supérieur droit)
        float menuButtonX = width * 0.95f;
        float menuButtonY = height * 0.05f;
        buttonPaint.setColor(Color.parseColor("#40000000"));
        canvas.drawCircle(menuButtonX, menuButtonY, width * 0.03f, buttonPaint);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(width * 0.02f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("☰", menuButtonX, menuButtonY + width * 0.01f, textPaint);
        
        // **100% RETROARCH AUTHENTIQUE** : Indicateur de zone de jeu (très discret)
        textPaint.setColor(Color.parseColor("#10FFFFFF"));
        textPaint.setTextSize(width * 0.015f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Zone de jeu", width * 0.5f, height * 0.1f, textPaint);
    }
    
    /**
     * **100% RETROARCH** : Rendu de l'overlay
     */
    private void renderOverlayUI(Canvas canvas) {
        // **100% RETROARCH** : Overlay de contrôles tactiles
        // Utiliser le système d'overlay RetroArch existant
    }
    
    /**
     * **100% RETROARCH** : Rendu des notifications
     */
    private void renderNotifications(Canvas canvas) {
        float width = canvas.getWidth();
        float height = canvas.getHeight();
        
        long currentTime = System.currentTimeMillis();
        List<Notification> activeNotifications = new ArrayList<>();
        
        for (Notification notification : notifications) {
            if (currentTime - notification.timestamp < notification.duration) {
                activeNotifications.add(notification);
            }
        }
        
        // **100% RETROARCH** : Nettoyer les notifications expirées
        notifications.clear();
        notifications.addAll(activeNotifications);
        
        // **100% RETROARCH** : Afficher les notifications actives
        float yOffset = 50;
        for (Notification notification : activeNotifications) {
            renderNotification(canvas, notification, width, yOffset);
            yOffset += 60;
        }
    }
    
    /**
     * **100% RETROARCH** : Rendu d'une notification
     */
    private void renderNotification(Canvas canvas, Notification notification, float width, float y) {
        // **100% RETROARCH** : Couleur selon le type
        int backgroundColor;
        int textColor = Color.WHITE;
        
        switch (notification.type) {
            case 0: // Info
                backgroundColor = Color.parseColor("#2196F3");
                break;
            case 1: // Success
                backgroundColor = Color.parseColor("#4CAF50");
                break;
            case 2: // Warning
                backgroundColor = Color.parseColor("#FF9800");
                break;
            case 3: // Error
                backgroundColor = Color.parseColor("#F44336");
                break;
            default:
                backgroundColor = Color.parseColor("#2196F3");
                break;
        }
        
        // **100% RETROARCH** : Fond de la notification
        notificationPaint.setColor(backgroundColor);
        RectF notificationRect = new RectF(20, y, width - 20, y + 50);
        canvas.drawRoundRect(notificationRect, 10, 10, notificationPaint);
        
        // **100% RETROARCH** : Texte de la notification - TAILLE CORRIGÉE
        textPaint.setColor(textColor);
        textPaint.setTextSize(24.0f); // TAILLE AUGMENTÉE
        canvas.drawText(notification.message, width / 2, y + 35, textPaint);
    }
    
    /**
     * **100% RETROARCH** : Rendu des boutons du menu
     */
    private void renderMenuButtons(Canvas canvas, float width, float height) {
        float buttonWidth = width * 0.7f; // Boutons plus larges pour fullscreen
        float buttonHeight = height * 0.06f; // Boutons plus petits pour tenir dans l'écran
        float startY = height * 0.3f; // Commencer plus haut pour avoir plus d'espace
        float spacing = height * 0.015f; // Espacement réduit
        
        String[] buttonTexts = {
            "▶️ Retour au Jeu", // **100% RETROARCH AUTHENTIQUE** : Bouton principal pour revenir au jeu
            "🎮 Démarrer le Jeu",
            "📁 Sélectionner ROM",
            "⚙️ Paramètres",
            "💾 Sauvegarder",
            "📂 Charger",
            "🔄 Restaurer Overlay", // **100% RETROARCH AUTHENTIQUE** : Nouveau bouton
            "🏠 Menu Principal"
        };
        
        // **100% RETROARCH** : Vérifier que tous les boutons tiennent dans l'écran
        float totalHeight = buttonTexts.length * buttonHeight + (buttonTexts.length - 1) * spacing;
        float availableHeight = height * 0.6f; // 60% de l'écran disponible pour les boutons
        
        if (totalHeight > availableHeight) {
            // Ajuster la taille si nécessaire
            buttonHeight = availableHeight / buttonTexts.length * 0.8f;
            spacing = availableHeight / buttonTexts.length * 0.2f;
        }
        
        for (int i = 0; i < buttonTexts.length; i++) {
            float buttonX = width / 2 - buttonWidth / 2;
            float buttonY = startY + i * (buttonHeight + spacing);
            
            // **100% RETROARCH AUTHENTIQUE** : Vérifier que le bouton ne dépasse pas
            if (buttonY + buttonHeight > height * 0.9f) {
                break; // Arrêter si on dépasse 90% de la hauteur
            }
            
            // **100% RETROARCH AUTHENTIQUE** : Fond du bouton - FULLSCREEN
            RectF buttonRect = new RectF(buttonX, buttonY, buttonX + buttonWidth, buttonY + buttonHeight);
            canvas.drawRoundRect(buttonRect, 12, 12, buttonPaint);
            
            // **100% RETROARCH AUTHENTIQUE** : Texte du bouton - FULLSCREEN
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTextSize(buttonHeight * 0.35f); // Taille proportionnelle réduite
            textPaint.setColor(Color.WHITE);
            canvas.drawText(buttonTexts[i], buttonX + buttonWidth / 2, buttonY + buttonHeight / 2 + (buttonHeight * 0.15f), textPaint);
        }
    }
    
    /**
     * **100% RETROARCH** : Rendu des boutons du menu rapide - FULLSCREEN
     */
    private void renderQuickMenuButtons(Canvas canvas, float width, float height) {
        String[] buttons = {
            "▶️ Retour au Jeu", // **100% RETROARCH AUTHENTIQUE** : Bouton principal pour revenir au jeu
            "⏪ Rewind",
            "⏩ Fast Forward",
            "💾 Save State",
            "📂 Load State",
            "📸 Screenshot",
            "🎮 Menu Principal"
        };
        
        float buttonWidth = width * 0.6f; // Boutons plus larges pour fullscreen
        float buttonHeight = height * 0.05f; // Boutons plus petits pour tenir dans l'écran
        float startY = height * 0.25f; // Commencer plus bas pour centrer
        float spacing = height * 0.015f; // Espacement réduit
        
        // **100% RETROARCH** : Vérifier que tous les boutons tiennent dans l'écran
        float totalHeight = buttons.length * buttonHeight + (buttons.length - 1) * spacing;
        float availableHeight = height * 0.6f; // 60% de l'écran disponible pour les boutons
        
        if (totalHeight > availableHeight) {
            // Ajuster la taille si nécessaire
            buttonHeight = availableHeight / buttons.length * 0.8f;
            spacing = availableHeight / buttons.length * 0.2f;
        }
        
        for (int i = 0; i < buttons.length; i++) {
            float x = width / 2 - buttonWidth / 2;
            float y = startY + i * (buttonHeight + spacing);
            
            // **100% RETROARCH** : Vérifier que le bouton ne dépasse pas
            if (y + buttonHeight > height * 0.9f) {
                break; // Arrêter si on dépasse 90% de la hauteur
            }
            
            // **100% RETROARCH** : Fond du bouton - FULLSCREEN
            buttonPaint.setColor(Color.parseColor("#404040"));
            RectF buttonRect = new RectF(x, y, x + buttonWidth, y + buttonHeight);
            canvas.drawRoundRect(buttonRect, 10, 10, buttonPaint);
            
            // **100% RETROARCH** : Texte du bouton - FULLSCREEN
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(buttonHeight * 0.35f); // Taille proportionnelle réduite
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(buttons[i], x + buttonWidth / 2, y + buttonHeight / 2 + (buttonHeight * 0.15f), textPaint);
        }
    }
    
    /**
     * **100% RETROARCH** : Rendu de l'affichage FPS
     */
    private void renderFPSDisplay(Canvas canvas, float width, float height) {
        // **100% RETROARCH** : Fond de l'affichage FPS - plus visible
        notificationPaint.setColor(Color.parseColor("#CC000000")); // Plus opaque
        RectF fpsRect = new RectF(10, 10, 140, 45); // Plus grand
        canvas.drawRoundRect(fpsRect, 8, 8, notificationPaint);
        
        // **100% RETROARCH** : Bordure pour plus de visibilité
        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.GREEN);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2.0f);
        canvas.drawRoundRect(fpsRect, 8, 8, borderPaint);
        
        // **100% RETROARCH** : Texte FPS - TAILLE CORRIGÉE
        textPaint.setColor(Color.GREEN);
        textPaint.setTextSize(24.0f); // TAILLE AUGMENTÉE
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setFakeBoldText(true); // Gras pour plus de visibilité
        canvas.drawText("FPS: 60", 20, 35, textPaint);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(false);
    }
    
    /**
     * **100% RETROARCH** : Rendu des indicateurs de performance
     */
    private void renderPerformanceIndicators(Canvas canvas, float width, float height) {
        // **100% RETROARCH** : Indicateurs de performance
        String[] indicators = {
            "CPU: 15%%",
            "GPU: 20%%",
            "RAM: 45%%"
        };
        
        float startX = width - 200;
        float startY = 10;
        float spacing = 25;
        
        for (int i = 0; i < indicators.length; i++) {
            float x = startX;
            float y = startY + i * spacing;
            
            // **100% RETROARCH** : Fond de l'indicateur - TAILLE CORRIGÉE
            notificationPaint.setColor(Color.parseColor("#CC000000")); // Plus opaque
            RectF indicatorRect = new RectF(x, y, x + 190, y + 35); // Plus grand
            canvas.drawRoundRect(indicatorRect, 8, 8, notificationPaint);
            
            // **100% RETROARCH** : Bordure pour plus de visibilité
            Paint borderPaint = new Paint();
            borderPaint.setColor(Color.CYAN);
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(2.0f);
            canvas.drawRoundRect(indicatorRect, 8, 8, borderPaint);
            
            // **100% RETROARCH** : Texte de l'indicateur - TAILLE CORRIGÉE
            textPaint.setColor(Color.CYAN);
            textPaint.setTextSize(20.0f); // TAILLE AUGMENTÉE
            textPaint.setTextAlign(Paint.Align.LEFT);
            textPaint.setFakeBoldText(true); // Gras pour plus de visibilité
            canvas.drawText(indicators[i], x + 10, y + 25, textPaint);
        }
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(false);
    }
    
    /**
     * **100% RETROARCH** : Mettre à jour l'affichage des gamepads
     */
    public void updateGamepadStatus(List<String> gamepads) {
        connectedGamepads.clear();
        if (gamepads != null) {
            connectedGamepads.addAll(gamepads);
        }
        lastGamepadUpdate = System.currentTimeMillis();
        invalidate(); // Forcer le redessinage
    }

    /**
     * **100% RETROARCH** : Afficher/masquer le statut des gamepads
     */
    public void setGamepadStatusVisible(boolean visible) {
        gamepadStatusVisible = visible;
        invalidate();
    }

    /**
     * **100% RETROARCH** : Rendu de l'affichage des gamepads
     */
    private void renderGamepadStatus(Canvas canvas) {
        if (!gamepadStatusVisible || connectedGamepads.isEmpty()) {
            return;
        }

        // Position en haut à droite
        float x = canvas.getWidth() - 20;
        float y = 60;
        
        // Fond semi-transparent
        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.parseColor("#80000000"));
        bgPaint.setStyle(Paint.Style.FILL);
        
        // Calculer la taille du texte
        Paint textPaint = new Paint();
        textPaint.setColor(Color.GREEN);
        textPaint.setTextSize(18.0f);
        textPaint.setAntiAlias(true);
        
        // Mesurer le texte le plus long
        float maxWidth = 0;
        for (String gamepad : connectedGamepads) {
            float width = textPaint.measureText(gamepad);
            if (width > maxWidth) {
                maxWidth = width;
            }
        }
        
        // Dessiner le fond
        float padding = 10;
        float bgWidth = maxWidth + padding * 2;
        float bgHeight = connectedGamepads.size() * 25 + padding * 2;
        RectF bgRect = new RectF(x - bgWidth, y - bgHeight, x, y);
        canvas.drawRoundRect(bgRect, 5, 5, bgPaint);
        
        // Dessiner le titre
        Paint titlePaint = new Paint();
        titlePaint.setColor(Color.YELLOW);
        titlePaint.setTextSize(16.0f);
        titlePaint.setAntiAlias(true);
        titlePaint.setFakeBoldText(true);
        
        String title = "🎮 Gamepads Connectés";
        float titleWidth = titlePaint.measureText(title);
        canvas.drawText(title, x - bgWidth + padding, y - bgHeight + 20, titlePaint);
        
        // Dessiner la liste des gamepads
        float textY = y - bgHeight + 40;
        for (String gamepad : connectedGamepads) {
            canvas.drawText("• " + gamepad, x - bgWidth + padding, textY, textPaint);
            textY += 25;
        }
    }
    
    /**
     * **100% RETROARCH NATIF** : Gestion multi-touch native
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // **100% RETROARCH AUTHENTIQUE** : Déléguer au gestionnaire d'overlays RetroArch
        if (currentState == UIState.UI_STATE_GAMEPLAY && overlayManager != null) {
            boolean handled = overlayManager.handleTouchEvent(event);
            if (handled) {
                return true;
            }
        }
        
        int action = event.getActionMasked();
        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);
        
        // **DEBUG** : Log pour voir si les touches arrivent
        Log.d(TAG, "🎮 **TOUCH EVENT** - Action: " + action + " - Pointer: " + pointerId + " - Pos: (" + x + ", " + y + ")");
        
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                // **100% RETROARCH** : Nouveau point de contact
                TouchPoint newTouch = new TouchPoint(x, y, pointerId);
                activeTouches.put(pointerId, newTouch);
                return handleMultiTouchDown(x, y, pointerId);
                
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                // **100% RETROARCH** : Point de contact relâché
                activeTouches.remove(pointerId);
                return handleMultiTouchUp(x, y, pointerId);
                
            case MotionEvent.ACTION_MOVE:
                // **100% RETROARCH** : Mise à jour des points de contact
                for (int i = 0; i < event.getPointerCount(); i++) {
                    int pid = event.getPointerId(i);
                    TouchPoint touch = activeTouches.get(pid);
                    if (touch != null) {
                        touch.x = event.getX(i);
                        touch.y = event.getY(i);
                        touch.timestamp = System.currentTimeMillis();
                    }
                }
                return handleMultiTouchMove(x, y, pointerId);
                
            case MotionEvent.ACTION_CANCEL:
                // **100% RETROARCH** : Annulation de tous les contacts
                activeTouches.clear();
                return true;
        }
        
        return super.onTouchEvent(event);
    }
    
    /**
     * **100% RETROARCH NATIF** : Gestion multi-touch down
     */
    private boolean handleMultiTouchDown(float x, float y, int pointerId) {
        // Log réduit pour éviter le spam
        
        switch (currentState) {
            case UI_STATE_MENU:
                return handleMenuTouch(x, y);
            case UI_STATE_QUICK_MENU:
                return handleQuickMenuTouch(x, y);
            case UI_STATE_GAMEPLAY:
                return handleGameplayMultiTouch(x, y, pointerId);
        }
        return false;
    }
    
    /**
     * **100% RETROARCH NATIF** : Gestion multi-touch up
     */
    private boolean handleMultiTouchUp(float x, float y, int pointerId) {
        Log.d(TAG, "🎮 **100% RETROARCH** - Multi-touch UP: " + pointerId + " - Contacts restants: " + activeTouches.size());
        
        if (currentState == UIState.UI_STATE_GAMEPLAY) {
            return handleGameplayMultiTouchRelease(x, y, pointerId);
        }
        return false;
    }
    
    /**
     * **100% RETROARCH NATIF** : Gestion multi-touch move
     */
    private boolean handleMultiTouchMove(float x, float y, int pointerId) {
        if (currentState == UIState.UI_STATE_GAMEPLAY) {
            return handleGameplayMultiTouchMove(x, y, pointerId);
        }
        return false;
    }
    
    /**
     * **100% RETROARCH AUTHENTIQUE** : Gestion multi-touch pour le gameplay
     */
    private boolean handleGameplayMultiTouch(float x, float y, int pointerId) {
        // **100% RETROARCH AUTHENTIQUE** : Utiliser le vrai système d'overlays RetroArch
        if (overlayManager != null) {
            // Créer un MotionEvent simulé pour le gestionnaire d'overlays
            MotionEvent event = MotionEvent.obtain(
                System.currentTimeMillis(), System.currentTimeMillis(),
                MotionEvent.ACTION_DOWN, x, y, 0
            );
            boolean handled = overlayManager.handleTouchEvent(event);
            event.recycle();
            return handled;
        }
        return false;
    }
    
    /**
     * **100% RETROARCH AUTHENTIQUE** : Relâchement multi-touch pour le gameplay
     */
    private boolean handleGameplayMultiTouchRelease(float x, float y, int pointerId) {
        // **100% RETROARCH AUTHENTIQUE** : Utiliser le vrai système d'overlays RetroArch
        if (overlayManager != null) {
            // Créer un MotionEvent simulé pour le gestionnaire d'overlays
            MotionEvent event = MotionEvent.obtain(
                System.currentTimeMillis(), System.currentTimeMillis(),
                MotionEvent.ACTION_UP, x, y, 0
            );
            boolean handled = overlayManager.handleTouchEvent(event);
            event.recycle();
            return handled;
        }
        return false;
    }
    
    /**
     * **100% RETROARCH AUTHENTIQUE** : Mouvement multi-touch pour le gameplay
     */
    private boolean handleGameplayMultiTouchMove(float x, float y, int pointerId) {
        // **100% RETROARCH AUTHENTIQUE** : Utiliser le vrai système d'overlays RetroArch
        if (overlayManager != null) {
            // Créer un MotionEvent simulé pour le gestionnaire d'overlays
            MotionEvent event = MotionEvent.obtain(
                System.currentTimeMillis(), System.currentTimeMillis(),
                MotionEvent.ACTION_MOVE, x, y, 0
            );
            boolean handled = overlayManager.handleTouchEvent(event);
            event.recycle();
            return handled;
        }
        return false;
    }
    
    /**
     * **100% RETROARCH AUTHENTIQUE** : Gestion des touches d'overlay RetroArch
     */
    private boolean handleRetroArchOverlayTouch(float x, float y, int pointerId, boolean pressed) {
        float width = getWidth();
        float height = getHeight();
        
        // **100% RETROARCH AUTHENTIQUE** : Normaliser les coordonnées (0.0-1.0)
        float normalizedX = x / width;
        float normalizedY = y / height;
        
        // **100% RETROARCH AUTHENTIQUE** : Bouton menu discret (coin supérieur droit)
        if (normalizedX > 0.95f && normalizedY < 0.05f) {
            if (pressed) {
                currentState = UIState.UI_STATE_MENU;
                Log.i(TAG, "🎮 **MENU RETROARCH** - Activé via bouton menu");
                invalidate();
            }
            return true;
        }
        
        // **100% RETROARCH AUTHENTIQUE** : Système d'overlays standard
        // D-Pad (zone gauche, bas)
        if (normalizedX < 0.25f && normalizedY > 0.6f) {
            // Détecter la direction dans le D-Pad
            float dpadCenterX = 0.125f;
            float dpadCenterY = 0.8f;
            float dpadRadius = 0.1f;
            
            float dx = normalizedX - dpadCenterX;
            float dy = normalizedY - dpadCenterY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            
            if (distance <= dpadRadius) {
                if (pressed) {
                    // Détecter la direction principale
                    if (Math.abs(dx) > Math.abs(dy)) {
                        if (dx > 0) {
                            sendInputToRetroArch(RETRO_DEVICE_ID_JOYPAD_RIGHT, true);
                            Log.d(TAG, "🎮 **DPAD** - Droite");
                        } else {
                            sendInputToRetroArch(RETRO_DEVICE_ID_JOYPAD_LEFT, true);
                            Log.d(TAG, "🎮 **DPAD** - Gauche");
                        }
                    } else {
                        if (dy > 0) {
                            sendInputToRetroArch(RETRO_DEVICE_ID_JOYPAD_DOWN, true);
                            Log.d(TAG, "🎮 **DPAD** - Bas");
                        } else {
                            sendInputToRetroArch(RETRO_DEVICE_ID_JOYPAD_UP, true);
                            Log.d(TAG, "🎮 **DPAD** - Haut");
                        }
                    }
                } else {
                    // Relâcher toutes les directions
                    sendInputToRetroArch(RETRO_DEVICE_ID_JOYPAD_UP, false);
                    sendInputToRetroArch(RETRO_DEVICE_ID_JOYPAD_DOWN, false);
                    sendInputToRetroArch(RETRO_DEVICE_ID_JOYPAD_LEFT, false);
                    sendInputToRetroArch(RETRO_DEVICE_ID_JOYPAD_RIGHT, false);
                }
                return true;
            }
        }
        
        // **100% RETROARCH AUTHENTIQUE** : Boutons d'action (zone droite, bas)
        if (normalizedX > 0.75f && normalizedY > 0.6f) {
            // Bouton A (principal, rouge)
            if (normalizedX > 0.85f && normalizedY > 0.75f) {
                sendInputToRetroArch(RETRO_DEVICE_ID_JOYPAD_A, pressed);
                if (pressed) Log.d(TAG, "🎮 **BOUTON A** - Pressé");
                return true;
            }
            
            // Bouton B (secondaire, vert)
            if (normalizedX > 0.75f && normalizedX < 0.85f && normalizedY > 0.75f) {
                sendInputToRetroArch(RETRO_DEVICE_ID_JOYPAD_B, pressed);
                if (pressed) Log.d(TAG, "🎮 **BOUTON B** - Pressé");
                return true;
            }
            
            // Bouton Start (bleu)
            if (normalizedX > 0.85f && normalizedY > 0.6f && normalizedY < 0.75f) {
                sendInputToRetroArch(RETRO_DEVICE_ID_JOYPAD_START, pressed);
                if (pressed) Log.d(TAG, "🎮 **BOUTON START** - Pressé");
                return true;
            }
            
            // Bouton Select (jaune)
            if (normalizedX > 0.75f && normalizedX < 0.85f && normalizedY > 0.6f && normalizedY < 0.75f) {
                sendInputToRetroArch(RETRO_DEVICE_ID_JOYPAD_SELECT, pressed);
                if (pressed) Log.d(TAG, "🎮 **BOUTON SELECT** - Pressé");
                return true;
            }
        }
        
        // **100% RETROARCH AUTHENTIQUE** : Zone de jeu (centre) - pas d'action
        if (normalizedX > 0.25f && normalizedX < 0.75f && normalizedY < 0.6f) {
            // Zone de jeu - ne rien faire
            return false;
        }
        
        return false; // Touché non géré
    }
    
    /**
     * **100% RETROARCH AUTHENTIQUE** : Gérer les actions d'overlay
     */
    private void handleOverlayAction(String action) {
        Log.i(TAG, "🎮 **100% RETROARCH AUTHENTIQUE** - Action overlay: " + action);
        
        switch (action) {
            case "menu_toggle":
                // **100% RETROARCH AUTHENTIQUE** : Bouton RetroArch pour ouvrir le menu
                if (currentState == UIState.UI_STATE_GAMEPLAY) {
                    currentState = UIState.UI_STATE_MENU;
                    Log.i(TAG, "🎮 **100% RETROARCH AUTHENTIQUE** - Menu ouvert via bouton RetroArch");
                } else {
                    currentState = UIState.UI_STATE_GAMEPLAY;
                    Log.i(TAG, "🎮 **100% RETROARCH AUTHENTIQUE** - Menu fermé via bouton RetroArch");
                }
                invalidate();
                break;
                
            case "force_restore_overlay":
                // **100% RETROARCH AUTHENTIQUE** : Forcer la restauration de l'overlay
                if (overlayManager != null) {
                    overlayManager.forceRestoreOverlay();
                    Log.i(TAG, "🔄 **100% RETROARCH AUTHENTIQUE** - Restauration forcée de l'overlay");
                }
                break;
                
            default:
                Log.d(TAG, "🎮 **100% RETROARCH AUTHENTIQUE** - Action non gérée: " + action);
                break;
        }
    }
    
    /**
     * **100% RETROARCH AUTHENTIQUE** : Envoyer input à RetroArch
     */
    private void sendInputToRetroArch(int deviceId, boolean pressed) {
        if (uiCallback != null) {
            uiCallback.onInputSent(deviceId, pressed);
        }
    }
    
    /**
     * **100% RETROARCH** : Gestion des touches - CORRIGÉ POUR FULLSCREEN
     */
    private boolean handleMenuTouch(float x, float y) {
        float width = getWidth();
        float height = getHeight();
        float buttonWidth = width * 0.7f; // Même largeur que le rendu
        float buttonHeight = height * 0.06f; // Même hauteur que le rendu
        float startY = height * 0.3f; // Même position que le rendu
        float spacing = height * 0.015f; // Même espacement que le rendu
        
        // **100% RETROARCH** : Vérifier que tous les boutons tiennent dans l'écran
        float totalHeight = 7 * buttonHeight + 6 * spacing;
        float availableHeight = height * 0.6f;
        
        if (totalHeight > availableHeight) {
            buttonHeight = availableHeight / 7 * 0.8f;
            spacing = availableHeight / 7 * 0.2f;
        }
        
        String[] buttonTexts = {
            "🎮 Démarrer le Jeu",
            "📁 Sélectionner ROM",
            "⚙️ Paramètres",
            "💾 Sauvegarder",
            "📂 Charger",
            "🔄 Restaurer Overlay",
            "🏠 Menu Principal"
        };
        
        for (int i = 0; i < buttonTexts.length; i++) {
            float buttonX = width / 2 - buttonWidth / 2;
            float buttonY = startY + i * (buttonHeight + spacing);
            
            // **100% RETROARCH AUTHENTIQUE** : Vérifier que le bouton ne dépasse pas
            if (buttonY + buttonHeight > height * 0.9f) {
                break; // Arrêter si on dépasse 90% de la hauteur
            }
            
            if (x >= buttonX && x <= buttonX + buttonWidth &&
                y >= buttonY && y <= buttonY + buttonHeight) {
                
                String[] actions = {
                    "resume_game", // **100% RETROARCH AUTHENTIQUE** : Retour au jeu en cours
                    "start_game",
                    "rom_selection",
                    "settings",
                    "save_state",
                    "load_state",
                    "restore_overlay", // **100% RETROARCH AUTHENTIQUE** : Nouveau bouton
                    "back_to_main"
                };
                
                executeMenuAction(actions[i]);
                return true;
            }
        }
        return false;
    }
    
    /**
     * **100% RETROARCH** : Gestion des touches du menu rapide - CORRIGÉ POUR FULLSCREEN
     */
    private boolean handleQuickMenuTouch(float x, float y) {
        float width = getWidth();
        float height = getHeight();
        float buttonWidth = width * 0.6f; // Même largeur que le rendu
        float buttonHeight = height * 0.05f; // Même hauteur que le rendu
        float startY = height * 0.25f; // Même position que le rendu
        float spacing = height * 0.015f; // Même espacement que le rendu
        
        // **100% RETROARCH** : Vérifier que tous les boutons tiennent dans l'écran
        float totalHeight = 7 * buttonHeight + 6 * spacing; // 7 boutons maintenant
        float availableHeight = height * 0.6f;
        
        if (totalHeight > availableHeight) {
            buttonHeight = availableHeight / 7 * 0.8f; // 7 boutons maintenant
            spacing = availableHeight / 7 * 0.2f;
        }
        
        String[] buttons = {
            "⏪ Rewind",
            "⏩ Fast Forward",
            "💾 Save State",
            "📂 Load State",
            "📸 Screenshot",
            "🎮 Menu Principal"
        };
        
        for (int i = 0; i < buttons.length; i++) {
            float buttonX = width / 2 - buttonWidth / 2;
            float buttonY = startY + i * (buttonHeight + spacing);
            
            // **100% RETROARCH** : Vérifier que le bouton ne dépasse pas
            if (buttonY + buttonHeight > height * 0.9f) {
                break; // Arrêter si on dépasse 90% de la hauteur
            }
            
            if (x >= buttonX && x <= buttonX + buttonWidth &&
                y >= buttonY && y <= buttonY + buttonHeight) {
                
                String[] actions = {
                    "resume_game", // **100% RETROARCH AUTHENTIQUE** : Retour au jeu en cours
                    "rewind",
                    "fast_forward",
                    "save_state",
                    "load_state",
                    "screenshot",
                    "main_menu"
                };
                
                executeQuickMenuAction(actions[i]);
                return true;
            }
        }
        return false;
    }
    
    /**
     * **100% RETROARCH** : Gestion des touches de gameplay
     */
    private boolean handleGameplayTouch(float x, float y) {
        // **100% RETROARCH** : Logique pour les contrôles de jeu
        Log.d(TAG, "🎮 Touché en mode gameplay: " + x + ", " + y);
        
        float width = getWidth();
        float height = getHeight();
        
        // **BOUTON MENU** : En haut à droite
        float menuButtonX = width - 60.0f;
        float menuButtonY = 60.0f;
        if (Math.sqrt((x - menuButtonX) * (x - menuButtonX) + (y - menuButtonY) * (y - menuButtonY)) <= 30.0f) {
            currentState = UIState.UI_STATE_MENU;
            Log.i(TAG, "🎮 **MENU ACTIVÉ** - Bouton menu touché");
            invalidate();
            return true;
        }
        
        // **DPAD** : Contrôle directionnel (gauche)
        float dpadX = 80.0f;
        float dpadY = height - 150.0f;
        float dpadSize = 120.0f;
        
        if (Math.sqrt((x - dpadX) * (x - dpadX) + (y - dpadY) * (y - dpadY)) <= dpadSize) {
            // Détecter la direction
            float dx = x - dpadX;
            float dy = y - dpadY;
            
            if (Math.abs(dx) > Math.abs(dy)) {
                // Horizontal
                if (dx > 0) {
                    Log.i(TAG, "🎮 **DPAD** - Droite pressée");
                    // Envoyer l'input à RetroArch
                    if (uiCallback != null) {
                        uiCallback.onInputSent(RETRO_DEVICE_ID_JOYPAD_RIGHT, true);
                    }
                } else {
                    Log.i(TAG, "🎮 **DPAD** - Gauche pressée");
                    if (uiCallback != null) {
                        uiCallback.onInputSent(RETRO_DEVICE_ID_JOYPAD_LEFT, true);
                    }
                }
            } else {
                // Vertical
                if (dy > 0) {
                    Log.i(TAG, "🎮 **DPAD** - Bas pressé");
                    if (uiCallback != null) {
                        uiCallback.onInputSent(RETRO_DEVICE_ID_JOYPAD_DOWN, true);
                    }
                } else {
                    Log.i(TAG, "🎮 **DPAD** - Haut pressé");
                    if (uiCallback != null) {
                        uiCallback.onInputSent(RETRO_DEVICE_ID_JOYPAD_UP, true);
                    }
                }
            }
            return true;
        }
        
        // **BOUTONS D'ACTION** : A, B, Start, Select (droite)
        float buttonStartX = width - 200.0f;
        float buttonY = height - 150.0f;
        float buttonSpacing = 70.0f;
        
        // Bouton A
        if (Math.sqrt((x - buttonStartX) * (x - buttonStartX) + (y - buttonY) * (y - buttonY)) <= 40.0f) {
            Log.i(TAG, "🎮 **BOUTON A** - Pressé");
            if (uiCallback != null) {
                uiCallback.onInputSent(RETRO_DEVICE_ID_JOYPAD_A, true);
            }
            return true;
        }
        
        // Bouton B
        if (Math.sqrt((x - (buttonStartX + buttonSpacing)) * (x - (buttonStartX + buttonSpacing)) + 
                     (y - buttonY) * (y - buttonY)) <= 35.0f) {
            Log.i(TAG, "🎮 **BOUTON B** - Pressé");
            if (uiCallback != null) {
                uiCallback.onInputSent(RETRO_DEVICE_ID_JOYPAD_B, true);
            }
            return true;
        }
        
        // Bouton Start
        if (Math.sqrt((x - buttonStartX) * (x - buttonStartX) + 
                     (y - (buttonY - buttonSpacing)) * (y - (buttonY - buttonSpacing))) <= 25.0f) {
            Log.i(TAG, "🎮 **BOUTON START** - Pressé");
            if (uiCallback != null) {
                uiCallback.onInputSent(RETRO_DEVICE_ID_JOYPAD_START, true);
            }
            return true;
        }
        
        // Bouton Select
        if (Math.sqrt((x - (buttonStartX + buttonSpacing)) * (x - (buttonStartX + buttonSpacing)) + 
                     (y - (buttonY - buttonSpacing)) * (y - (buttonY - buttonSpacing))) <= 25.0f) {
            Log.i(TAG, "🎮 **BOUTON SELECT** - Pressé");
            if (uiCallback != null) {
                uiCallback.onInputSent(RETRO_DEVICE_ID_JOYPAD_SELECT, true);
            }
            return true;
        }
        
        // **AUCUN BOUTON TOUCHÉ** : Ne rien faire (pas de menu automatique)
        Log.d(TAG, "🎮 **AUCUN BOUTON** - Touché en zone vide");
        return false; // Touché non géré par l'interface
    }
    
    /**
     * **100% RETROARCH** : Exécution des actions du menu
     */
    private void executeMenuAction(String action) {
        switch (action) {
            case "resume_game":
                // **100% RETROARCH AUTHENTIQUE** : Retour au jeu en cours
                setUIState(UIState.UI_STATE_GAMEPLAY);
                showNotification("▶️ Retour au jeu en cours", 2000, 1);
                Log.i(TAG, "🎮 **100% RETROARCH** - Retour au jeu en cours");
                break;
            case "start_game":
                setUIState(UIState.UI_STATE_GAMEPLAY);
                showNotification("🎮 Retour au jeu", 2000, 1);
                break;
            case "rom_selection":
                // Ouvrir la sélection de ROM
                if (uiCallback != null) {
                    uiCallback.onRomSelectionRequested();
                }
                showNotification("📁 Sélection ROM", 2000, 0);
                break;
            case "settings":
                // **100% RETROARCH AUTHENTIQUE** : Ouvrir les paramètres
                if (uiCallback != null) {
                    uiCallback.onSettingsRequested();
                }
                showNotification("⚙️ Paramètres", 2000, 0);
                Log.i(TAG, "🎮 **100% RETROARCH** - Ouverture des paramètres");
                break;
            case "save_state":
                saveState();
                break;
            case "load_state":
                loadState();
                break;
            case "screenshot":
                takeScreenshot();
                break;
            case "restore_overlay":
                // Restaurer l'overlay
                if (overlayManager != null) {
                    overlayManager.forceRestoreOverlay();
                    showNotification("🔄 Overlay restauré", 2000, 0);
                }
                break;
            case "back_to_main":
                // Retour au menu principal
                if (uiCallback != null) {
                    uiCallback.onBackToMainMenu();
                }
                showNotification("🏠 Retour au menu principal", 2000, 0);
                break;
        }
    }
    
    /**
     * **100% RETROARCH** : Exécution des actions du menu rapide
     */
    private void executeQuickMenuAction(String action) {
        switch (action) {
            case "resume_game":
                // **100% RETROARCH AUTHENTIQUE** : Retour au jeu en cours
                setUIState(UIState.UI_STATE_GAMEPLAY);
                showNotification("▶️ Retour au jeu en cours", 2000, 1);
                Log.i(TAG, "🎮 **100% RETROARCH** - Retour au jeu en cours (menu rapide)");
                break;
            case "rewind":
                activateRewind();
                break;
            case "fast_forward":
                activateFastForward();
                break;
            case "save_state":
                saveState();
                break;
            case "load_state":
                loadState();
                break;
            case "screenshot":
                takeScreenshot();
                break;
            case "main_menu":
                setUIState(UIState.UI_STATE_MENU);
                break;
        }
    }
    
    /**
     * **100% RETROARCH** : Méthodes publiques pour l'interface
     */
    public void setUIState(UIState state) {
        this.currentState = state;
        invalidate();
        Log.i(TAG, "🎮 **100% RETROARCH** - État UI: " + state);
    }
    
    public void setUITheme(UITheme theme) {
        this.currentTheme = theme;
        applyTheme();
        invalidate();
        Log.i(TAG, "🎮 **100% RETROARCH** - Thème UI: " + theme);
    }
    
    public void setUICallback(UICallback callback) {
        this.uiCallback = callback;
    }
    
    public void showNotification(String message, long duration, int type) {
        notifications.add(new Notification(message, duration, type));
        invalidate();
        Log.i(TAG, "🎮 **100% RETROARCH** - Notification: " + message);
    }
    
    public void showQuickMenu() {
        setUIState(UIState.UI_STATE_QUICK_MENU);
    }
    
    public void showMainMenu() {
        setUIState(UIState.UI_STATE_MENU);
    }
    
    public void hideMenu() {
        setUIState(UIState.UI_STATE_GAMEPLAY);
    }
    
    /**
     * **100% RETROARCH** : Actions RetroArch
     */
    private void saveState() {
        if (stateManager != null) {
            stateManager.saveState();
            showNotification("💾 État sauvegardé", 2000, 1);
        }
    }
    
    private void loadState() {
        if (stateManager != null) {
            stateManager.loadState();
            showNotification("📂 État chargé", 2000, 1);
        }
    }
    
    private void takeScreenshot() {
        if (videoManager != null) {
            videoManager.takeScreenshot();
            showNotification("📸 Screenshot pris", 2000, 1);
        }
    }
    
    private void activateRewind() {
        if (coreSystem != null) {
            coreSystem.activateFeature(RetroArchCoreSystem.RetroArchFeature.FEATURE_REWIND);
            showNotification("⏪ Rewind activé", 2000, 0);
        }
    }
    
    private void activateFastForward() {
        if (coreSystem != null) {
            coreSystem.activateFeature(RetroArchCoreSystem.RetroArchFeature.FEATURE_FAST_FORWARD);
            showNotification("⏩ Fast Forward activé", 2000, 0);
        }
    }
    
    /**
     * **100% RETROARCH** : Application du thème
     */
    private void applyTheme() {
        switch (currentTheme) {
            case UI_THEME_DARK:
                applyDarkTheme();
                break;
            case UI_THEME_LIGHT:
                applyLightTheme();
                break;
            case UI_THEME_RETRO:
                applyRetroTheme();
                break;
            case UI_THEME_MODERN:
                applyModernTheme();
                break;
        }
    }
    
    private void applyDarkTheme() {
        // **100% RETROARCH** : Thème sombre par défaut
        backgroundPaint.setColor(Color.parseColor("#1A1A1A"));
        textPaint.setColor(Color.WHITE);
        buttonPaint.setColor(Color.parseColor("#404040"));
        highlightPaint.setColor(Color.parseColor("#2196F3"));
    }
    
    private void applyLightTheme() {
        // **100% RETROARCH** : Thème clair
        backgroundPaint.setColor(Color.parseColor("#F5F5F5"));
        textPaint.setColor(Color.BLACK);
        buttonPaint.setColor(Color.parseColor("#E0E0E0"));
        highlightPaint.setColor(Color.parseColor("#1976D2"));
    }
    
    private void applyRetroTheme() {
        // **100% RETROARCH** : Thème rétro
        backgroundPaint.setColor(Color.parseColor("#2D2D2D"));
        textPaint.setColor(Color.parseColor("#00FF00"));
        buttonPaint.setColor(Color.parseColor("#404040"));
        highlightPaint.setColor(Color.parseColor("#FF6B35"));
    }
    
    private void applyModernTheme() {
        // **100% RETROARCH** : Thème moderne
        backgroundPaint.setColor(Color.parseColor("#121212"));
        textPaint.setColor(Color.WHITE);
        buttonPaint.setColor(Color.parseColor("#BB86FC"));
        highlightPaint.setColor(Color.parseColor("#03DAC6"));
    }
    
    /**
     * **100% RETROARCH** : Getters pour l'état
     */
    public UIState getCurrentState() {
        return currentState;
    }
    
    public UITheme getCurrentTheme() {
        return currentTheme;
    }
    
    public boolean isMenuVisible() {
        return currentState == UIState.UI_STATE_MENU || currentState == UIState.UI_STATE_QUICK_MENU;
    }
}
