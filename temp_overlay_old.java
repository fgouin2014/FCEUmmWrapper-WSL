package com.fceumm.wrapper.overlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RetroArchOverlayLoader extends View {
    private static final String TAG = "RetroArchOverlayLoader";
    
    private Context context;
    private String overlayPath;
    private String currentOverlayName; // Nom de l'overlay actuellement chargé
    private Map<String, OverlayButton> buttons;
    private List<OverlayButton> buttonList; // Liste ordonnée des boutons
    private List<OverlayImage> images;
    private Paint paint;
    private OnButtonPressedListener listener;
    
    // Suivi des doigts pour le multitouch
    private Set<Integer> activePointers;
    private Map<String, Long> buttonPressTimes; // Pour suivre quand les boutons ont été pressés
    private static final long BUTTON_HOLD_TIME = 500; // 500ms de tolérance pour maintenir le bouton pressé
    
    public interface OnButtonPressedListener {
        void onButtonPressed(String buttonName, boolean pressed);
    }
    
    public static class OverlayButton {
        public String name;
        public float x, y;
        public float width, height;
        public String type; // "rect", "radial"
        public Bitmap overlay;
        public boolean isPressed = false;
        
        public OverlayButton(String name, float x, float y, float width, float height, String type) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.type = type;
        }
    }
    
    public static class OverlayImage {
        public float x, y;
        public float width, height;
        public Bitmap bitmap;
        
        public OverlayImage(float x, float y, float width, float height, Bitmap bitmap) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.bitmap = bitmap;
        }
    }
    
    public RetroArchOverlayLoader(Context context) {
        super(context);
        init(context);
    }
    
    public RetroArchOverlayLoader(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    public RetroArchOverlayLoader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    
    private void init(Context context) {
        this.context = context;
        this.buttons = new HashMap<>();
        this.buttonList = new ArrayList<>();
        this.images = new ArrayList<>();
        this.activePointers = new HashSet<>();
        this.buttonPressTimes = new HashMap<>();
        this.paint = new Paint();
        paint.setAntiAlias(true);
        
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return handleTouch(event);
            }
        });
    }
    
    public void loadOverlay(String overlayName) {
        loadOverlay(overlayName, -1); // Utiliser l'overlay automatique basé sur l'orientation
    }
    
    public void loadOverlay(String overlayName, int overlayIndex) {
        Log.i(TAG, "Chargement de l'overlay: " + overlayName + " (index: " + overlayIndex + ")");
        
        // Stocker le nom de l'overlay actuel
        this.currentOverlayName = overlayName;
        
        // Vider les boutons existants
        buttons.clear();
        buttonList.clear();
        
        // Construire le chemin vers le fichier de configuration
        String configPath;
        if (overlayName.contains("/")) {
            // Pour les overlays dans des sous-dossiers (ex: flat/nes)
            // Le fichier est directement dans le dossier parent (ex: flat/nes.cfg)
            configPath = "overlays/gamepads/" + overlayName + ".cfg";
        } else {
            // Pour les overlays simples (ex: retropad)
            configPath = "overlays/gamepads/" + overlayName + "/" + overlayName + ".cfg";
        }
        
        try {
            InputStream is = context.getAssets().open(configPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            
            String line;
            String targetOverlayIndex;
            
            if (overlayIndex >= 0) {
                // Utiliser l'index spécifié
                targetOverlayIndex = "overlay" + overlayIndex + "_";
                Log.d(TAG, "Overlay forcé: " + targetOverlayIndex);
            } else {
                // Utiliser l'overlay automatique basé sur l'orientation et le type d'overlay
                boolean isPortrait = isPortraitMode();
                
                // Détecter le bon index selon l'overlay
                if (overlayName.equals("retropad")) {
                    // Retropad utilise overlay1_ pour portrait, overlay0_ pour landscape
                    targetOverlayIndex = isPortrait ? "overlay1_" : "overlay0_";
                } else if (overlayName.contains("snes")) {
                    // SNES utilise overlay2_ pour portrait, overlay0_ pour landscape
                    targetOverlayIndex = isPortrait ? "overlay2_" : "overlay0_";
                } else {
                    // Par défaut: overlay4_ pour portrait, overlay0_ pour landscape (NES, etc.)
                    targetOverlayIndex = isPortrait ? "overlay4_" : "overlay0_";
                }
                
                Log.d(TAG, "Mode: " + (isPortrait ? "PORTRAIT" : "PAYSAGE") + ", Overlay: " + overlayName + ", Index cible: " + targetOverlayIndex);
            }
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Parser les descriptions de boutons pour l'overlay cible
                if (line.startsWith(targetOverlayIndex) && line.contains("_desc") && !line.contains("_overlay")) {
                    parseButtonDescription(line, overlayName);
                } else if (line.startsWith(targetOverlayIndex) && line.contains("_desc") && line.contains("_overlay")) {
                    parseOverlayImage(line, overlayName);
                }
            }
            
            reader.close();
            is.close();
            
            Log.i(TAG, "Overlay chargé avec succès: " + overlayName + " (" + buttons.size() + " boutons) - Index: " + targetOverlayIndex);
            
        } catch (IOException e) {
            Log.e(TAG, "Erreur lors du chargement de l'overlay: " + overlayName, e);
        }
    }
    
    private boolean isPortraitMode() {
        android.content.res.Configuration config = context.getResources().getConfiguration();
        return config.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT;
    }
    
    private void parseButtonDescription(String line, String overlayName) {
        try {
            // Format: overlay0_desc0 = "left,0.04375,0.80208333333,radial,0.0525,0.0875"
            String[] parts = line.split("=");
            if (parts.length == 2) {
                String desc = parts[1].trim().replace("\"", "");
                String[] values = desc.split(",");
                
                if (values.length >= 6) {
                                         String buttonName = values[0];
                     float x = Float.parseFloat(values[1]);
                     float y = Float.parseFloat(values[2]); // Coordonnées Y originales (pas d'inversion)
                    String type = values[3];
                    float width = Float.parseFloat(values[4]);
                    float height = Float.parseFloat(values[5]);
                    
                    // Gérer les boutons combinés et diagonales avec des hitboxes optimisées
                    if (buttonName.contains("|")) {
                        // Bouton combiné (ex: "a|b", "left|up")
                        // Les diagonales ont des dimensions plus petites dans les .cfg
                        // Il faut les agrandir davantage pour compenser
                        // En paysage, les diagonales sont encore plus petites qu'en portrait
                        boolean isPortrait = isPortraitMode();
                        if (isPortrait) {
                            width *= 1.8f; // Portrait - agrandir pour compenser la petite taille
                            height *= 1.8f;
                            Log.d(TAG, "Bouton combiné portrait détecté: " + buttonName + " - Hitbox agrandie x1.8");
                        } else {
                            width *= 2.5f; // Paysage - agrandir encore plus car dimensions plus petites
                            height *= 2.5f;
                            Log.d(TAG, "Bouton combiné paysage détecté: " + buttonName + " - Hitbox agrandie x2.5");
                        }
                    } else if (buttonName.equals("left") || buttonName.equals("right") || 
                               buttonName.equals("up") || buttonName.equals("down")) {
                        // Boutons directionnels - hitbox plus généreuse
                        width *= 1.15f;
                        height *= 1.15f;
                                                 // Log.d(TAG, "Bouton directionnel: " + buttonName + " - Hitbox optimisée");
                    } else if (buttonName.equals("a") || buttonName.equals("b") || 
                               buttonName.equals("x") || buttonName.equals("y")) {
                        // Boutons d'action - hitbox standard mais précise
                        width *= 1.1f;
                        height *= 1.1f;
                                                 // Log.d(TAG, "Bouton d'action: " + buttonName + " - Hitbox standard");
                    }
                    
                    // Charger tous les boutons, même les dupliqués (RetroArch les utilise pour différentes fonctionnalités)
                    // Seulement ignorer si c'est exactement le même bouton avec les mêmes coordonnées
                    if (buttons.containsKey(buttonName)) {
                        OverlayButton existingButton = buttons.get(buttonName);
                        if (existingButton.x == x && existingButton.y == y) {
                            // Log.d(TAG, "Bouton dupliqué exact ignoré: " + buttonName + " à (" + x + ", " + y + ")");
                            return;
                        }
                    }
                    
                    OverlayButton button = new OverlayButton(buttonName, x, y, width, height, type);
                    buttons.put(buttonName, button);
                    buttonList.add(button);
                    
                                         // Log.d(TAG, "Bouton ajouté: " + buttonName + " à (" + x + ", " + y + ") - Type: " + type + ", Taille: " + width + "x" + height);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors du parsing de la description: " + line, e);
        }
    }
    
    private void parseOverlayImage(String line, String overlayName) {
        try {
            // Format: overlay0_desc6_overlay = img/b.png
            String[] parts = line.split("=");
            if (parts.length == 2) {
                String imagePath = parts[1].trim();
                // Chercher l'image dans le même dossier que le fichier .cfg
                String fullPath;
                if (overlayName.contains("/")) {
                    // Pour les overlays dans des sous-dossiers (ex: flat/nes)
                    fullPath = "overlays/gamepads/" + overlayName.split("/")[0] + "/" + imagePath;
                } else {
                    // Pour les overlays simples (ex: retropad)
                    fullPath = "overlays/gamepads/" + overlayName + "/" + imagePath;
                }
                
                // Charger l'image
                InputStream is = null;
                Bitmap bitmap = null;
                
                try {
                    is = context.getAssets().open(fullPath);
                    bitmap = BitmapFactory.decodeStream(is);
                } catch (Exception e) {
                    // Si l'image n'est pas trouvée dans le dossier parent, essayer dans le dossier img
                    if (overlayName.contains("/")) {
                        String fallbackPath = "overlays/gamepads/" + overlayName.split("/")[0] + "/img/" + imagePath;
                        try {
                            if (is != null) is.close();
                            is = context.getAssets().open(fallbackPath);
                            bitmap = BitmapFactory.decodeStream(is);
                                                         // Log.d(TAG, "Image trouvée dans le dossier img: " + fallbackPath);
                        } catch (Exception e2) {
                            Log.w(TAG, "Image non trouvée: " + fullPath + " ou " + fallbackPath);
                        }
                    }
                } finally {
                    if (is != null) is.close();
                }
                
                if (bitmap != null) {
                    // Extraire l'index du bouton (ex: "6" de "overlay0_desc6_overlay")
                    String buttonIndexStr = line.split("_desc")[1].split("_overlay")[0];
                    int buttonIndex = Integer.parseInt(buttonIndexStr);
                    
                    // Associer l'image au bouton correspondant dans la liste
                    if (buttonIndex < buttonList.size()) {
                        OverlayButton button = buttonList.get(buttonIndex);
                        button.overlay = bitmap;
                                                 // Log.d(TAG, "Image chargée pour le bouton " + buttonIndex + " (" + button.name + "): " + imagePath);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors du chargement de l'image: " + line, e);
        }
    }
    
    private boolean handleTouch(MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        
        // Gérer le multitouch avec suivi des doigts
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            float x = event.getX(pointerIndex) / getWidth();
            float y = event.getY(pointerIndex) / getHeight(); // Coordonnées Android normales
            long currentTime = System.currentTimeMillis();
            
                         // Log.d(TAG, "Clic détecté à: (" + x + ", " + y + ") - Pointer: " + pointerIndex);
            
            // Chercher tous les boutons touchés par ce doigt
            for (OverlayButton button : buttons.values()) {
                if (isPointInButton(x, y, button)) {
                    boolean wasPressed = button.isPressed;
                    button.isPressed = true;
                    
                    if (!wasPressed && listener != null) {
                        buttonPressTimes.put(button.name, currentTime);
                        
                        // Gérer les boutons combinés (diagonales, combos)
                        if (button.name.contains("|")) {
                            // Diviser le bouton combiné en boutons individuels
                            String[] individualButtons = button.name.split("\\|");
                            for (String individualButton : individualButtons) {
                                listener.onButtonPressed(individualButton, true);
                                                                 Log.d(TAG, "Bouton combiné pressé: " + button.name + " -> " + individualButton + " activé");
                            }
                        } else {
                            // Bouton normal
                            listener.onButtonPressed(button.name, true);
                                                         // Log.d(TAG, "Bouton pressé: " + button.name + " à (" + button.x + ", " + button.y + ")");
                        }
                    }
                }
            }
            
            invalidate();
            return true;
            
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
            // Relâcher tous les boutons pressés par ce doigt
            for (OverlayButton button : buttons.values()) {
                if (button.isPressed) {
                    button.isPressed = false;
                    buttonPressTimes.remove(button.name);
                    if (listener != null) {
                        // Gérer les boutons combinés (diagonales, combos)
                        if (button.name.contains("|")) {
                            // Diviser le bouton combiné en boutons individuels
                            String[] individualButtons = button.name.split("\\|");
                            for (String individualButton : individualButtons) {
                                listener.onButtonPressed(individualButton, false);
                                                                 // Log.d(TAG, "Bouton combiné relâché: " + button.name + " -> " + individualButton + " désactivé");
                            }
                        } else {
                            // Bouton normal
                            listener.onButtonPressed(button.name, false);
                                                         // Log.d(TAG, "Bouton relâché: " + button.name);
                        }
                    }
                }
            }
            
            invalidate();
            return true;
            
        } else if (action == MotionEvent.ACTION_MOVE) {
            // Gérer le mouvement pour tous les doigts
            int pointerCount = event.getPointerCount();
            Set<String> currentlyPressedButtons = new HashSet<>();
            long currentTime = System.currentTimeMillis();
            
            // Vérifier tous les doigts actuellement sur l'écran
            for (int i = 0; i < pointerCount; i++) {
                                 float x = event.getX(i) / getWidth();
                 float y = event.getY(i) / getHeight(); // Coordonnées Android normales
                
                for (OverlayButton button : buttons.values()) {
                    if (isPointInButton(x, y, button)) {
                        currentlyPressedButtons.add(button.name);
                        
                        if (!button.isPressed && listener != null) {
                            button.isPressed = true;
                            buttonPressTimes.put(button.name, currentTime);
                            
                            // Gérer les boutons combinés (diagonales, combos)
                            if (button.name.contains("|")) {
                                // Diviser le bouton combiné en boutons individuels
                                String[] individualButtons = button.name.split("\\|");
                                for (String individualButton : individualButtons) {
                                    listener.onButtonPressed(individualButton, true);
                                                                         // Log.d(TAG, "Bouton combiné pressé (move): " + button.name + " -> " + individualButton + " activé");
                                }
                            } else {
                                // Bouton normal
                                listener.onButtonPressed(button.name, true);
                                                                 // Log.d(TAG, "Bouton pressé (move): " + button.name);
                            }
                        }
                    }
                }
            }
            
            // Relâcher les boutons qui ne sont plus touchés (avec tolérance de temps)
            for (OverlayButton button : buttons.values()) {
                if (button.isPressed && !currentlyPressedButtons.contains(button.name)) {
                    Long pressTime = buttonPressTimes.get(button.name);
                    if (pressTime != null && (currentTime - pressTime) > BUTTON_HOLD_TIME) {
                        // Le bouton a été pressé assez longtemps, on peut le relâcher
                        button.isPressed = false;
                        buttonPressTimes.remove(button.name);
                        if (listener != null) {
                            // Gérer les boutons combinés (diagonales, combos)
                            if (button.name.contains("|")) {
                                // Diviser le bouton combiné en boutons individuels
                                String[] individualButtons = button.name.split("\\|");
                                for (String individualButton : individualButtons) {
                                    listener.onButtonPressed(individualButton, false);
                                                                         // Log.d(TAG, "Bouton combiné relâché (move): " + button.name + " -> " + individualButton + " désactivé");
                                }
                            } else {
                                // Bouton normal
                                listener.onButtonPressed(button.name, false);
                                                                 // Log.d(TAG, "Bouton relâché (move): " + button.name);
                            }
                        }
                    }
                    // Si le bouton n'a pas été pressé assez longtemps, on le garde pressé
                }
            }
            
            invalidate();
            return true;
        }
        
        return true;
    }
    
    private boolean isPointInButton(float x, float y, OverlayButton button) {
        if (button.type.equals("rect")) {
            // Zone rectangulaire avec hitbox optimisée
            float hitboxWidth = button.width * 1.2f; // 20% plus large pour une meilleure détection
            float hitboxHeight = button.height * 1.2f; // 20% plus haute
            return x >= button.x - hitboxWidth/2 && x <= button.x + hitboxWidth/2 &&
                   y >= button.y - hitboxHeight/2 && y <= button.y + hitboxHeight/2;
        } else if (button.type.equals("radial")) {
            // Zone circulaire avec hitbox optimisée
            float dx = x - button.x;
            float dy = y - button.y;
            float distance = (float) Math.sqrt(dx*dx + dy*dy);
            float hitboxRadius = button.width/2 * 1.3f; // 30% plus grand pour une meilleure détection
            return distance <= hitboxRadius;
        }
        return false;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int width = getWidth();
        int height = getHeight();
        
        // PAS DE FOND - Laisser le jeu visible en arrière-plan
        // paint.setColor(0x33000000);
        // canvas.drawRect(0, 0, width, height, paint);
        
        // Dessiner tous les boutons avec leurs images
        for (OverlayButton button : buttons.values()) {
            if (button.overlay != null) {
                // Facteur d'agrandissement adaptatif selon le type de bouton
                float scaleFactor = 1.0f; // Base
                
                if (button.name.contains("|")) {
                    // Boutons combinés - légèrement plus petits visuellement
                    scaleFactor = 0.9f;
                } else if (button.name.equals("left") || button.name.equals("right") || 
                           button.name.equals("up") || button.name.equals("down")) {
                    // Boutons directionnels - taille standard
                    scaleFactor = 1.0f;
                } else if (button.name.equals("a") || button.name.equals("b") || 
                           button.name.equals("x") || button.name.equals("y")) {
                    // Boutons d'action - taille standard
                    scaleFactor = 1.0f;
                } else {
                    // Autres boutons (start, select, etc.) - taille standard
                    scaleFactor = 1.0f;
                }
                
                float buttonWidth = button.width * scaleFactor;
                float buttonHeight = button.height * scaleFactor;
                
                float left = (button.x - buttonWidth/2) * width;
                float top = (button.y - buttonHeight/2) * height;
                float right = (button.x + buttonWidth/2) * width;
                float bottom = (button.y + buttonHeight/2) * height;
                
                RectF rect = new RectF(left, top, right, bottom);
                
                // Appliquer une transparence pour tous les boutons - VISIBLES MAIS TRANSPARENTS
                if (button.isPressed) {
                    paint.setAlpha(255); // Très visible quand pressé
                } else {
                    paint.setAlpha(180); // Visible par défaut mais transparent
                }
                
                canvas.drawBitmap(button.overlay, null, rect, paint);
            }
        }
        
                 // Log pour déboguer (désactivé pour réduire le spam)
         // if (buttons.size() > 0) {
         //     Log.d(TAG, "onDraw - Boutons visibles: " + buttons.size() + ", Dimensions: " + width + "x" + height);
         // }
    }
    
    public void setOnButtonPressedListener(OnButtonPressedListener listener) {
        this.listener = listener;
    }
    
    public void resetAllButtons() {
        for (OverlayButton button : buttons.values()) {
            if (button.isPressed) {
                button.isPressed = false;
                if (listener != null) {
                    listener.onButtonPressed(button.name, false);
                }
            }
        }
        buttonPressTimes.clear();
        invalidate();
    }
    
    public void reloadOverlayForOrientation(String overlayName) {
        Log.i(TAG, "Rechargement de l'overlay pour l'orientation: " + overlayName);
        
        // Si c'est le même overlay, juste recharger avec l'index automatique
        if (overlayName.equals(currentOverlayName)) {
            loadOverlay(overlayName, -1); // Utiliser l'index automatique basé sur l'orientation
        } else {
            // Nouvel overlay, le charger complètement
            loadOverlay(overlayName);
        }
        
        invalidate();
    }
    
    // Méthodes pour changer d'overlay
    public void loadLandscapeOverlay(String overlayName) {
        loadOverlay(overlayName, 0); // overlay0 = landscape
        invalidate();
    }
    
    public void loadPortraitOverlay(String overlayName) {
        loadOverlay(overlayName, 4); // overlay4 = portrait-A
        invalidate();
    }
    
    public void hideOverlay(String overlayName) {
        loadOverlay(overlayName, 8); // overlay8 = hidden
        invalidate();
    }
    
    /**
     * Met à jour la sensibilité des diagonales (désactivée pour simplifier)
     */
    public void setDiagonalSensitivity(float sensitivity) {
        // Désactivé pour simplifier la logique
        Log.d(TAG, "Sensibilité des diagonales désactivée pour simplifier");
    }
} 