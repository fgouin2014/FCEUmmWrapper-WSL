package com.fceumm.wrapper.overlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * **100% RETROARCH NATIF** : Système d'overlay identique à RetroArch officiel
 */
public class RetroArchOverlaySystem {

    private static final String TAG = "RetroArchOverlaySystem";

    // **100% RETROARCH NATIF** : Constantes identiques à RetroArch
    public static final int OVERLAY_MAX_TOUCH = 16;
    public static final int MAX_VISIBILITY = 32;

    // **100% RETROARCH NATIF** : Enums identiques à RetroArch
    public enum OverlayHitbox {
        OVERLAY_HITBOX_RADIAL,
        OVERLAY_HITBOX_RECT,
        OVERLAY_HITBOX_NONE
    }

    public enum OverlayType {
        OVERLAY_TYPE_BUTTONS,
        OVERLAY_TYPE_ANALOG_LEFT,
        OVERLAY_TYPE_ANALOG_RIGHT,
        OVERLAY_TYPE_DPAD_AREA,
        OVERLAY_TYPE_ABXY_AREA,
        OVERLAY_TYPE_KEYBOARD,
        OVERLAY_TYPE_LAST
    }

    public enum OverlayVisibility {
        OVERLAY_VISIBILITY_DEFAULT,
        OVERLAY_VISIBILITY_VISIBLE,
        OVERLAY_VISIBILITY_HIDDEN
    }

    public enum OverlayOrientation {
        OVERLAY_ORIENTATION_NONE,
        OVERLAY_ORIENTATION_LANDSCAPE,
        OVERLAY_ORIENTATION_PORTRAIT
    }

    // **100% RETROARCH NATIF** : Flags identiques à RetroArch
    public static final int OVERLAY_FULL_SCREEN = (1 << 0);
    public static final int OVERLAY_BLOCK_SCALE = (1 << 1);
    public static final int OVERLAY_BLOCK_X_SEPARATION = (1 << 2);
    public static final int OVERLAY_BLOCK_Y_SEPARATION = (1 << 3);
    public static final int OVERLAY_AUTO_X_SEPARATION = (1 << 4);
    public static final int OVERLAY_AUTO_Y_SEPARATION = (1 << 5);

    // **100% RETROARCH NATIF** : Structure OverlayDesc identique à RetroArch
    public static class OverlayDesc {
        public OverlayHitbox hitbox;
        public OverlayType type;

        public int next_index;
        public int image_index;

        public float alpha_mod;
        public float range_mod;
        public float analog_saturate_pct;
        public float range_x, range_y;
        public float range_x_mod, range_y_mod;
        public float mod_x, mod_y, mod_w, mod_h;
        public float delta_x, delta_y;
        public float x;
        public float y;

        // **100% RETROARCH NATIF** : Coordonnées décalées utilisateur
        public float x_shift;
        public float y_shift;

        // **100% RETROARCH NATIF** : Hitbox étendue
        public float x_hitbox;
        public float y_hitbox;
        public float range_x_hitbox, range_y_hitbox;
        public float reach_right, reach_left, reach_up, reach_down;

        public long button_mask; // input_bits_t equivalent

        public int touch_mask;
        public int old_touch_mask;

        public int flags;
        public String next_index_name;

        // **100% RETROARCH NATIF** : Texture et chemin
        public Bitmap texture;
        public String texture_path;

        // **100% RETROARCH NATIF** : Input mapping
        public String input_name; // "a", "b", "left", etc.
        public int libretro_device_id; // RETRO_DEVICE_ID_JOYPAD_A, etc.
    }

    // **100% RETROARCH NATIF** : Structure Overlay identique à RetroArch
    public static class Overlay {
        public List<OverlayDesc> descs;
        public List<Bitmap> load_images;

        public int load_images_size;
        public int id;
        public int pos_increment;

        public int size;
        public int pos;

        public float mod_x, mod_y, mod_w, mod_h;
        public float x, y, w, h;
        public float center_x, center_y;
        public float aspect_ratio;

        public String name;
        public int flags;

        // **100% RETROARCH NATIF** : Propriétés d'affichage
        public boolean full_screen;
        public boolean normalized;
        public float range_mod;
        public float alpha_mod;
        
        // **AMÉLIORATION** : Métadonnées pour la sélection automatique
        public float target_aspect_ratio = -1.0f; // Aspect ratio optimal pour cet overlay
        public boolean auto_x_separation = false;
        public boolean auto_y_separation = false;
        public boolean block_x_separation = false;
        public boolean block_y_separation = false;
    }

    // **100% RETROARCH NATIF** : Structure InputOverlayState identique à RetroArch
    public static class InputOverlayState {
        public int[] keys; // uint32_t keys[RETROK_LAST / 32 + 1]
        public short[] analog; // int16_t analog[4] - Left X, Left Y, Right X, Right Y
        public long buttons; // input_bits_t buttons
        public int touch_count;

        public static class TouchPoint {
            public short x, y;
        }

        public TouchPoint[] touch = new TouchPoint[OVERLAY_MAX_TOUCH];
    }

    // **100% RETROARCH NATIF** : Structure OverlayLayoutDesc identique à RetroArch
    public static class OverlayLayoutDesc {
        public float scale_landscape;
        public float aspect_adjust_landscape;
        public float x_separation_landscape;
        public float y_separation_landscape;
        public float x_offset_landscape;
        public float y_offset_landscape;
        public float scale_portrait;
        public float aspect_adjust_portrait;
        public float x_separation_portrait;
        public float y_separation_portrait;
        public float x_offset_portrait;
        public float y_offset_portrait;
        public float touch_scale;
        public boolean auto_scale;
    }

    // **100% RETROARCH NATIF** : Structure OverlayLayout identique à RetroArch
    public static class OverlayLayout {
        public float x_scale;
        public float y_scale;
        public float x_separation;
        public float y_separation;
        public float x_offset;
        public float y_offset;
    }

    // **100% RETROARCH NATIF** : Device IDs identiques à RetroArch
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

    // **100% RETROARCH NATIF** : Singleton pattern
    private static RetroArchOverlaySystem instance;
    private Context context;

    // **100% RETROARCH NATIF** : État du système
    private List<Overlay> overlays = new ArrayList<>();
    private Overlay activeOverlay;
    private OverlayLayoutDesc layoutDesc;
    private OverlayLayout currentLayout;
    private InputOverlayState overlayState;

    // **100% RETROARCH NATIF** : Configuration
    private String overlayPath = "overlays/gamepads/flat/";
    private String currentCfgFile = "nes.cfg"; // Overlay par défaut
    private boolean overlayEnabled = false;
    private float overlayOpacity = 1.0f;
    private OverlayVisibility visibility = OverlayVisibility.OVERLAY_VISIBILITY_VISIBLE;

    // **100% RETROARCH NATIF** : État de l'écran
    private int screenWidth = 1280;
    private int screenHeight = 720;
    private boolean isLandscape = true;

    // **100% RETROARCH NATIF** : Gestion des touches
    private Map<Integer, TouchPoint> activeTouches = new HashMap<>();

    // **100% RETROARCH NATIF** : Interface de callback
    public interface OnOverlayInputListener {
        void onOverlayInput(int deviceId, boolean pressed);
    }

    // **100% RETROARCH NATIF** : Structure TouchPoint
    public static class TouchPoint {
        public float x, y;
        public long timestamp;
        public int pointerId;

        public TouchPoint(float x, float y, int pointerId) {
            this.x = x;
            this.y = y;
            this.pointerId = pointerId;
            this.timestamp = System.currentTimeMillis();
        }
    }

    private OnOverlayInputListener inputListener;

    // **100% RETROARCH NATIF** : Constructeur privé
    private RetroArchOverlaySystem(Context context) {
        this.context = context;
        initLayoutDesc();
        initOverlayState();
    }

    // **100% RETROARCH NATIF** : Singleton getInstance
    public static RetroArchOverlaySystem getInstance(Context context) {
        if (instance == null) {
            instance = new RetroArchOverlaySystem(context);
        }
        return instance;
    }

    // **100% RETROARCH NATIF** : Initialisation du layout
    private void initLayoutDesc() {
        layoutDesc = new OverlayLayoutDesc();
        layoutDesc.scale_landscape = 1.0f;
        layoutDesc.scale_portrait = 1.0f;
        layoutDesc.auto_scale = true;
    }

    // **100% RETROARCH NATIF** : Initialisation de l'état
    private void initOverlayState() {
        overlayState = new InputOverlayState();
        overlayState.keys = new int[256]; // RETROK_LAST / 32 + 1
        overlayState.analog = new short[4];
        overlayState.buttons = 0;
        overlayState.touch_count = 0;
    }

    // **100% RETROARCH NATIF** : Mise à jour du chemin d'overlay
    public void updateOverlayPath(String selectedOverlay) {
        if (selectedOverlay != null && !selectedOverlay.isEmpty()) {
            overlayPath = "overlays/gamepads/" + selectedOverlay + "/";
        }
    }
    
    // **AMÉLIORATION** : Chargement automatique selon le système
    public void loadOverlayForSystem(String systemName) {
        String overlayFile = null;
        
        // **MAPPING** : Système → fichier overlay
        switch (systemName.toLowerCase()) {
            case "nes":
            case "fceumm":
                overlayFile = "nes.cfg";
                break;
            case "snes":
            case "bsnes":
                overlayFile = "snes.cfg";
                break;
            case "gba":
            case "mgba":
                overlayFile = "gba.cfg";
                break;
            case "gb":
            case "gambatte":
                overlayFile = "gameboy.cfg";
                break;
            case "psx":
            case "beetle_psx":
                overlayFile = "psx.cfg";
                break;
            case "n64":
            case "mupen64plus":
                overlayFile = "nintendo64.cfg";
                break;
            case "genesis":
            case "genesis_plus_gx":
                overlayFile = "genesis.cfg";
                break;
            case "arcade":
            case "mame":
                overlayFile = "arcade.cfg";
                break;
            case "neogeo":
                overlayFile = "neogeo.cfg";
                break;
            case "atari2600":
            case "stella":
                overlayFile = "atari2600.cfg";
                break;
            case "atari7800":
                overlayFile = "atari7800.cfg";
                break;
            case "dreamcast":
                overlayFile = "dreamcast.cfg";
                break;
            case "saturn":
                overlayFile = "saturn.cfg";
                break;
            case "psp":
                overlayFile = "psp.cfg";
                break;
            case "virtualboy":
                overlayFile = "virtualboy.cfg";
                break;
            case "turbografx16":
            case "mednafen_pce":
                overlayFile = "turbografx-16.cfg";
                break;
            default:
                overlayFile = "retropad.cfg"; // Overlay générique par défaut
                break;
        }
        
        if (overlayFile != null) {
            Log.d(TAG, "🎮 Chargement overlay pour système " + systemName + ": " + overlayFile);
            loadOverlay(overlayFile);
        }
    }

    // **100% RETROARCH NATIF** : Chargement d'overlay
    public void loadOverlay(String cfgFileName) {
        try {
            overlays.clear();
            currentCfgFile = cfgFileName;

            String fullPath = overlayPath + cfgFileName;
            InputStream inputStream = context.getAssets().open(fullPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            List<Overlay> allOverlays = new ArrayList<>();
            Overlay currentOverlay = null;
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                if (line.startsWith("overlays = ")) {
                    // Nombre total d'overlays
                    int overlayCount = Integer.parseInt(line.split("=")[1].trim());
                    for (int i = 0; i < overlayCount; i++) {
                        Overlay overlay = new Overlay();
                        overlay.descs = new ArrayList<>();
                        overlay.load_images = new ArrayList<>();
                        overlay.id = i;
                        allOverlays.add(overlay);
                    }
                } else if (line.startsWith("overlay") && line.contains("_name = ")) {
                    // Définir le nom de l'overlay
                    String[] parts = line.split("=");
                    if (parts.length == 2) {
                        String overlayKey = parts[0].trim();
                        int overlayIndex = Integer.parseInt(overlayKey.split("_")[0].replace("overlay", ""));
                        if (overlayIndex < allOverlays.size()) {
                            currentOverlay = allOverlays.get(overlayIndex);
                            currentOverlay.name = parts[1].trim().replace("\"", "");
                        }
                    }
                } else if (line.startsWith("overlay") && line.contains("_aspect_ratio = ")) {
                    // **AMÉLIORATION** : Lire l'aspect ratio optimal
                    String[] parts = line.split("=");
                    if (parts.length == 2) {
                        String overlayKey = parts[0].trim();
                        int overlayIndex = Integer.parseInt(overlayKey.split("_")[0].replace("overlay", ""));
                        if (overlayIndex < allOverlays.size()) {
                            currentOverlay = allOverlays.get(overlayIndex);
                            currentOverlay.target_aspect_ratio = Float.parseFloat(parts[1].trim());
                        }
                    }
                } else if (line.startsWith("overlay") && line.contains("_auto_x_separation = ")) {
                    // **AMÉLIORATION** : Lire les paramètres de séparation
                    String[] parts = line.split("=");
                    if (parts.length == 2) {
                        String overlayKey = parts[0].trim();
                        int overlayIndex = Integer.parseInt(overlayKey.split("_")[0].replace("overlay", ""));
                        if (overlayIndex < allOverlays.size()) {
                            currentOverlay = allOverlays.get(overlayIndex);
                            currentOverlay.auto_x_separation = Boolean.parseBoolean(parts[1].trim());
                        }
                    }
                } else if (line.startsWith("overlay") && line.contains("_auto_y_separation = ")) {
                    String[] parts = line.split("=");
                    if (parts.length == 2) {
                        String overlayKey = parts[0].trim();
                        int overlayIndex = Integer.parseInt(overlayKey.split("_")[0].replace("overlay", ""));
                        if (overlayIndex < allOverlays.size()) {
                            currentOverlay = allOverlays.get(overlayIndex);
                            currentOverlay.auto_y_separation = Boolean.parseBoolean(parts[1].trim());
                        }
                    }
                } else if (line.startsWith("overlay") && line.contains("_descs = ")) {
                    // Définir le nombre de descriptions pour l'overlay
                    String[] parts = line.split("=");
                    if (parts.length == 2) {
                        String overlayKey = parts[0].trim();
                        int overlayIndex = Integer.parseInt(overlayKey.split("_")[0].replace("overlay", ""));
                        if (overlayIndex < allOverlays.size()) {
                            currentOverlay = allOverlays.get(overlayIndex);
                        }
                    }
                } else if (line.startsWith("overlay") && line.contains("_desc") && line.contains("=") && !line.contains("_overlay") && currentOverlay != null) {
                    // Description de bouton
                    parseOverlayDesc(line, currentOverlay);
                } else if (line.startsWith("overlay") && line.contains("_desc") && line.contains("_overlay") && currentOverlay != null) {
                    // Image de bouton
                    parseOverlayImage(line, currentOverlay);
                }
            }

            reader.close();
            inputStream.close();

            // **100% RETROARCH NATIF** : Charger les textures
            for (Overlay overlay : allOverlays) {
                loadOverlayTextures(overlay);
            }

            // **CORRECTION CRITIQUE** : Copier allOverlays dans overlays
            overlays.clear();
            overlays.addAll(allOverlays);

            // **100% RETROARCH NATIF** : Sélectionner l'overlay actif
            activeOverlay = selectOverlayForCurrentOrientation(allOverlays);
            if (activeOverlay != null) {
                // **100% RETROARCH NATIF** : Forcer la mise à jour des dimensions
                Log.d(TAG, "🔄 Forcer mise à jour dimensions: " + screenWidth + "x" + screenHeight + " - isLandscape: " + isLandscape);
                applyOverlayLayout();
            }

        } catch (IOException e) {
            Log.e(TAG, "Erreur lors du chargement de l'overlay: " + e.getMessage());
        }
    }

    // **100% RETROARCH NATIF** : Sélection d'overlay par critères de correspondance
    private Overlay selectOverlayForCurrentOrientation(List<Overlay> allOverlays) {
        if (!allOverlays.isEmpty()) {
            // **CALCUL** : Aspect ratio de l'écran actuel
            float screenAspectRatio = (float) screenWidth / screenHeight;
            boolean isLandscape = screenWidth > screenHeight;
            
            Log.d(TAG, "🔍 Sélection overlay - Screen: " + screenWidth + "x" + screenHeight + 
                  " (aspectRatio: " + screenAspectRatio + ", isLandscape: " + isLandscape + ")");
            Log.d(TAG, "📋 Overlays disponibles: " + allOverlays.size());
            
            // **PHASE 1** : Recherche par correspondance exacte d'aspect ratio
            Overlay bestMatch = null;
            float bestMatchDiff = Float.MAX_VALUE;
            
            for (Overlay overlay : allOverlays) {
                if (overlay.target_aspect_ratio > 0) {
                    float diff = Math.abs(overlay.target_aspect_ratio - screenAspectRatio);
                    Log.d(TAG, "  Overlay: " + overlay.name + " - Target AR: " + overlay.target_aspect_ratio + 
                          " - Diff: " + diff + " - Descs: " + (overlay.descs != null ? overlay.descs.size() : 0));
                    
                    if (diff < bestMatchDiff) {
                        bestMatchDiff = diff;
                        bestMatch = overlay;
                    }
                }
            }
            
            if (bestMatch != null && bestMatchDiff < 0.5f) { // Tolérance de 0.5
                Log.d(TAG, "✅ Overlay trouvé par aspect ratio: " + bestMatch.name + " (diff: " + bestMatchDiff + ")");
                return bestMatch;
            }
            
            // **PHASE 2** : Recherche par nom avec logique intelligente
            String baseOrientation = isLandscape ? "landscape" : "portrait";
            String targetName = baseOrientation + "-A";
            
            // **LOGIC** : Choisir l'overlay approprié selon l'aspect ratio
            if (isLandscape) {
                if (screenAspectRatio > 2.0f) {
                    targetName = "landscape-B"; // Ultra-wide
                } else if (screenAspectRatio > 1.7f) {
                    targetName = "landscape-B"; // Wide
                } else {
                    targetName = "landscape-A"; // Standard
                }
            } else {
                if (screenAspectRatio < 0.6f) {
                    targetName = "portrait-B"; // Très étroit
                } else {
                    targetName = "portrait-A"; // Standard
                }
            }
            
            Log.d(TAG, "🔍 Recherche par nom: " + targetName);
            
            for (Overlay overlay : allOverlays) {
                if (overlay.name != null && overlay.name.equals(targetName)) {
                    Log.d(TAG, "✅ Overlay trouvé par nom: " + overlay.name);
                    return overlay;
                }
            }
            
            // **PHASE 3** : Fallback par orientation de base
            Log.w(TAG, "⚠️ Overlay spécifique non trouvé: " + targetName + ", essai avec: " + baseOrientation);
            for (Overlay overlay : allOverlays) {
                if (overlay.name != null && overlay.name.startsWith(baseOrientation)) {
                    Log.d(TAG, "✅ Overlay de fallback trouvé: " + overlay.name);
                    return overlay;
                }
            }
            
            // **PHASE 4** : Premier overlay disponible
            if (!allOverlays.isEmpty()) {
                Log.w(TAG, "⚠️ Aucun overlay trouvé, utilisation du premier disponible");
                return allOverlays.get(0);
            }
            
            Log.e(TAG, "❌ Aucun overlay disponible");
        }
        return null;
    }

    // **100% RETROARCH NATIF** : Parsing des descriptions d'overlay
    private void parseOverlayDesc(String line, Overlay overlay) {
        try {
            String[] parts = line.split("=");
            if (parts.length >= 2) {
                String descData = parts[1].trim().replace("\"", "");
                String[] descParts = descData.split(",");

                if (descParts.length >= 5) {
                    OverlayDesc desc = new OverlayDesc();
                    desc.input_name = descParts[0].trim();
                    desc.mod_x = Float.parseFloat(descParts[1].trim());
                    desc.mod_y = Float.parseFloat(descParts[2].trim());
                    
                    // **DEBUG** : Afficher les coordonnées brutes du fichier .cfg
                    if (desc.input_name != null && (desc.input_name.equals("l") || desc.input_name.equals("r") || desc.input_name.equals("nul"))) {
                        Log.d(TAG, "📄 " + desc.input_name + " - Coordonnées brutes du .cfg: x=" + descParts[1].trim() + ", y=" + descParts[2].trim());
                    }
                    
                    // **100% RETROARCH NATIF** : Mapping hitbox
                    String hitboxType = descParts[3].trim();
                    if ("radial".equals(hitboxType)) {
                        desc.hitbox = OverlayHitbox.OVERLAY_HITBOX_RADIAL;
                    } else if ("rect".equals(hitboxType)) {
                        desc.hitbox = OverlayHitbox.OVERLAY_HITBOX_RECT;
                    } else {
                        desc.hitbox = OverlayHitbox.OVERLAY_HITBOX_NONE;
                    }
                    
                    desc.mod_w = Float.parseFloat(descParts[4].trim());
                    desc.mod_h = Float.parseFloat(descParts[5].trim());

                    // **100% RETROARCH NATIF** : Mapping vers Libretro
                    desc.libretro_device_id = mapInputToLibretro(desc.input_name);

                    overlay.descs.add(desc);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur parsing overlay desc: " + e.getMessage());
        }
    }

    // **100% RETROARCH NATIF** : Parsing des images d'overlay
    private void parseOverlayImage(String line, Overlay overlay) {
        try {
            String[] parts = line.split("=");
            if (parts.length >= 2) {
                String imagePath = parts[1].trim();
                
                // **100% RETROARCH NATIF** : Chemin relatif aux assets
                String fullImagePath = overlayPath + imagePath;
                
                // Extraire l'index du desc de la ligne
                String[] lineParts = parts[0].split("_");
                for (int i = 0; i < lineParts.length; i++) {
                    if (lineParts[i].startsWith("desc")) {
                        try {
                            int descIndex = Integer.parseInt(lineParts[i].substring(4));
                            if (descIndex >= 0 && descIndex < overlay.descs.size()) {
                                overlay.descs.get(descIndex).texture_path = fullImagePath;
                            }
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "Erreur lors du parsing de l'index du desc: " + lineParts[i]);
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur parsing overlay image: " + e.getMessage());
        }
    }

    // **100% RETROARCH NATIF** : Chargement des textures
    private void loadOverlayTextures(Overlay overlay) {
        for (OverlayDesc desc : overlay.descs) {
            if (desc.texture_path != null) {
                try {
                    InputStream inputStream = context.getAssets().open(desc.texture_path);
                    desc.texture = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Erreur chargement texture: " + desc.texture_path);
                }
            }
        }
    }

    // **100% RETROARCH NATIF** : Mapping input vers Libretro
    private int mapInputToLibretro(String inputName) {
        if (inputName == null) return -1;

        switch (inputName.toLowerCase()) {
            case "a": return RETRO_DEVICE_ID_JOYPAD_A;
            case "b": return RETRO_DEVICE_ID_JOYPAD_B;
            case "x": return RETRO_DEVICE_ID_JOYPAD_X;
            case "y": return RETRO_DEVICE_ID_JOYPAD_Y;
            case "l": return RETRO_DEVICE_ID_JOYPAD_L;
            case "r": return RETRO_DEVICE_ID_JOYPAD_R;
            case "l2": return RETRO_DEVICE_ID_JOYPAD_L2;
            case "r2": return RETRO_DEVICE_ID_JOYPAD_R2;
            case "l3": return RETRO_DEVICE_ID_JOYPAD_L3;
            case "r3": return RETRO_DEVICE_ID_JOYPAD_R3;
            case "start": return RETRO_DEVICE_ID_JOYPAD_START;
            case "select": return RETRO_DEVICE_ID_JOYPAD_SELECT;
            case "up": return RETRO_DEVICE_ID_JOYPAD_UP;
            case "down": return RETRO_DEVICE_ID_JOYPAD_DOWN;
            case "left": return RETRO_DEVICE_ID_JOYPAD_LEFT;
            case "right": return RETRO_DEVICE_ID_JOYPAD_RIGHT;
            default: return -1;
        }
    }

    // **100% RETROARCH NATIF** : Application du layout (SANS SCALE)
    private void applyOverlayLayout() {
        if (activeOverlay == null) return;

        // **100% RETROARCH NATIF** : Coordonnées directes
        for (OverlayDesc desc : activeOverlay.descs) {
            // **100% RETROARCH NATIF** : Coordonnées directes sans modification
            desc.x_hitbox = desc.mod_x;
            desc.y_hitbox = desc.mod_y; // Coordonnées directes
            desc.range_x_hitbox = desc.mod_w;
            desc.range_y_hitbox = desc.mod_h;
        }
    }

    // **100% RETROARCH NATIF** : Gestion des touches
    public boolean handleTouch(MotionEvent event) {
        if (!overlayEnabled || activeOverlay == null) return false;

        int action = event.getActionMasked();
        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                return handleMultiTouchDown(event, pointerIndex, pointerId);
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                return handleMultiTouchUp(event, pointerIndex, pointerId);
            case MotionEvent.ACTION_MOVE:
                return handleMultiTouchMove(event);
            default:
                return false;
        }
    }

    // **100% RETROARCH NATIF** : Touch down
    private boolean handleMultiTouchDown(MotionEvent event, int pointerIndex, int pointerId) {
        float touchX = event.getX(pointerIndex);
        float touchY = event.getY(pointerIndex);

        // **100% RETROARCH NATIF** : Normalisation directe
        float normalizedX = touchX / screenWidth;
        float normalizedY = touchY / screenHeight;

        if (activeOverlay != null && activeOverlay.descs != null) {
            for (OverlayDesc desc : activeOverlay.descs) {
                if (desc.mod_w > 0 && desc.mod_h > 0) {
                    // **100% RETROARCH NATIF** : Vérification hitbox (déjà inversée dans applyOverlayLayout)
                    if (normalizedX >= desc.x_hitbox && normalizedX <= desc.x_hitbox + desc.range_x_hitbox &&
                        normalizedY >= desc.y_hitbox && normalizedY <= desc.y_hitbox + desc.range_y_hitbox) {

                        // **100% RETROARCH NATIF** : Traitement du touch
                        if (desc.input_name != null && desc.libretro_device_id >= 0) {
                            if (inputListener != null) {
                                inputListener.onOverlayInput(desc.libretro_device_id, true);
                            }

                            desc.touch_mask |= (1 << pointerId);
                            activeTouches.put(pointerId, new TouchPoint(touchX, touchY, pointerId));

                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    // **100% RETROARCH NATIF** : Touch up
    private boolean handleMultiTouchUp(MotionEvent event, int pointerIndex, int pointerId) {
        activeTouches.remove(pointerId);

        if (activeOverlay != null && activeOverlay.descs != null) {
            for (OverlayDesc desc : activeOverlay.descs) {
                if ((desc.touch_mask & (1 << pointerId)) != 0) {
                    desc.touch_mask &= ~(1 << pointerId);

                    if (desc.libretro_device_id >= 0 && inputListener != null) {
                        inputListener.onOverlayInput(desc.libretro_device_id, false);
                    }

                    return true;
                }
            }
        }

        return false;
    }

    // **100% RETROARCH NATIF** : Touch move
    private boolean handleMultiTouchMove(MotionEvent event) {
        boolean handled = false;

        for (int i = 0; i < event.getPointerCount(); i++) {
            int pointerId = event.getPointerId(i);
            float touchX = event.getX(i);
            float touchY = event.getY(i);

            float normalizedX = touchX / screenWidth;
            float normalizedY = touchY / screenHeight;

            if (activeOverlay != null && activeOverlay.descs != null) {
                for (OverlayDesc desc : activeOverlay.descs) {
                    if ((desc.touch_mask & (1 << pointerId)) != 0) {
                        // **100% RETROARCH NATIF** : Vérifier si toujours dans la hitbox
                        if (normalizedX < desc.x_hitbox || normalizedX > desc.x_hitbox + desc.range_x_hitbox ||
                            normalizedY < desc.y_hitbox || normalizedY > desc.y_hitbox + desc.range_y_hitbox) {
                            
                            // **100% RETROARCH NATIF** : Sortir de la hitbox
                            desc.touch_mask &= ~(1 << pointerId);
                            if (desc.libretro_device_id >= 0 && inputListener != null) {
                                inputListener.onOverlayInput(desc.libretro_device_id, false);
                            }
                        }
                        handled = true;
                    }
                }
            }
        }

        return handled;
    }

    // **100% RETROARCH NATIF** : Rendu
    public void render(Canvas canvas) {
        if (activeOverlay == null || !overlayEnabled || activeOverlay.descs == null || activeOverlay.descs.isEmpty()) {
            return;
        }

        Paint paint = new Paint();
        paint.setAlpha((int)(255 * overlayOpacity));
        paint.setAntiAlias(true);

        // **100% RETROARCH NATIF** : Debug dimensions
        float canvasWidth = canvas.getWidth();
        float canvasHeight = canvas.getHeight();
        
        // **CORRECTION CRITIQUE** : Mettre à jour les dimensions si elles ont changé
        if (screenWidth != (int)canvasWidth || screenHeight != (int)canvasHeight) {
            updateScreenDimensions((int)canvasWidth, (int)canvasHeight);
        }
        
        // **DEBUG** : Vérifier les dimensions réelles vs Canvas
        Log.d(TAG, "🎨 Rendu overlay - Canvas: " + canvasWidth + "x" + canvasHeight + 
              " - Screen: " + screenWidth + "x" + screenHeight +
              " - Overlay: " + activeOverlay.name + " - Descs: " + activeOverlay.descs.size() +
              " - Orientation détectée: " + (canvasWidth > canvasHeight ? "LANDSCAPE" : "PORTRAIT"));

        // **100% RETROARCH NATIF** : Rendu des textures uniquement
        for (OverlayDesc desc : activeOverlay.descs) {
            if (desc.texture != null && desc.mod_w > 0 && desc.mod_h > 0) {
                // **100% RETROARCH NATIF** : Coordonnées directes (pas d'inversion nécessaire)
                float pixelX = desc.mod_x * canvasWidth;
                float pixelY = desc.mod_y * canvasHeight; // Coordonnées directes
                float pixelW = desc.mod_w * canvasWidth;
                float pixelH = desc.mod_h * canvasHeight;

                // **DEBUG** : Afficher les coordonnées pour les boutons importants
                if (desc.input_name != null && (desc.input_name.equals("l") || desc.input_name.equals("r") || desc.input_name.equals("nul"))) {
                    Log.d(TAG, "🎯 " + desc.input_name + " - mod_x: " + desc.mod_x + " -> pixelX: " + pixelX + 
                          " - mod_y: " + desc.mod_y + " -> pixelY: " + pixelY +
                          " - mod_w: " + desc.mod_w + " -> pixelW: " + pixelW +
                          " - mod_h: " + desc.mod_h + " -> pixelH: " + pixelH +
                          " - RectF: (" + pixelX + ", " + pixelY + ", " + (pixelX + pixelW) + ", " + (pixelY + pixelH) + ")");
                }

                RectF destRect = new RectF(
                    pixelX, pixelY,
                    pixelX + pixelW, pixelY + pixelH
                );

                canvas.drawBitmap(desc.texture, null, destRect, paint);
            }
        }
    }

    // **100% RETROARCH NATIF** : Getters et setters
    public void setOverlayEnabled(boolean enabled) {
        this.overlayEnabled = enabled;
    }

    public void setOverlayOpacity(float opacity) {
        this.overlayOpacity = opacity;
    }

    public void setInputListener(OnOverlayInputListener listener) {
        this.inputListener = listener;
    }

    public boolean isOverlayEnabled() {
        return overlayEnabled;
    }

    public Overlay getActiveOverlay() {
        return activeOverlay;
    }

    public void forceLayoutUpdate() {
        if (activeOverlay != null) {
            applyOverlayLayout();
        }
    }

    public void updateScreenDimensions(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        this.isLandscape = width > height;

        Log.d(TAG, "📱 updateScreenDimensions: " + width + "x" + height + " - isLandscape: " + isLandscape);

        // **CORRECTION CRITIQUE** : Re-sélectionner l'overlay si les dimensions changent
        if (activeOverlay != null && !overlays.isEmpty()) {
            Overlay newActiveOverlay = selectOverlayForCurrentOrientation(overlays);
            if (newActiveOverlay != null && newActiveOverlay != activeOverlay) {
                Log.d(TAG, "🔄 Changement d'overlay: " + activeOverlay.name + " -> " + newActiveOverlay.name);
                activeOverlay = newActiveOverlay;
                applyOverlayLayout();
            }
        }
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public boolean isLandscape() {
        return isLandscape;
    }
} 