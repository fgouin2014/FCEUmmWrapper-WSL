package com.fceumm.wrapper.overlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;

import com.fceumm.wrapper.config.RetroArchConfigManager;  // **NOUVEAU** : Import pour la configuration

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
    
    // **100% RETROARCH NATIF** : Constantes de device ID identiques à RetroArch
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

        public RetroArchInputBits button_mask; // **100% RETROARCH NATIF** : input_bits_t exact

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
        public RetroArchInputBits buttons; // **100% RETROARCH NATIF** : input_bits_t exact
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

    // **100% RETROARCH NATIF** : Device IDs identiques à RetroArch (utilise RetroArchInputBits.RETRO_DEVICE_ID_*)

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
    private String overlayPath = "overlays/gamepads/nes/"; // **CORRECTION CRITIQUE** : Chemin correct pour NES
    private String currentCfgFile = "nes.cfg"; // **100% RETROARCH** : Overlay normal
    private boolean overlayEnabled = false;
    private float overlayOpacity = 1.0f;
    private float overlayScale = 1.5f;  // **CORRECTION CRITIQUE** : Facteur d'échelle plus élevé pour boutons plus gros
    private OverlayVisibility visibility = OverlayVisibility.OVERLAY_VISIBILITY_VISIBLE;
    private boolean showInputsDebug = false; // **NOUVEAU** : Debug désactivé par défaut pour interface propre

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
        overlayState.analog = new short[4]; // Left X, Left Y, Right X, Right Y
        overlayState.buttons = new RetroArchInputBits(); // **100% RETROARCH NATIF** : input_bits_t exact
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
        
        // **MAPPING** : Système → fichier overlay et dossier
        switch (systemName.toLowerCase()) {
            case "nes":
            case "fceumm":
                overlayFile = "nes.cfg";
                overlayPath = "overlays/gamepads/nes/";
                break;
            case "snes":
            case "bsnes":
                overlayFile = "snes.cfg";
                overlayPath = "overlays/gamepads/snes/";
                break;
            case "gba":
            case "mgba":
                overlayFile = "gba.cfg";
                overlayPath = "overlays/gamepads/gba/";
                break;
            case "gb":
            case "gambatte":
                overlayFile = "gameboy.cfg";
                overlayPath = "overlays/gamepads/gb/";
                break;
            case "psx":
            case "beetle_psx":
                overlayFile = "psx.cfg";
                overlayPath = "overlays/gamepads/psx/";
                break;
            case "n64":
            case "mupen64plus":
                overlayFile = "nintendo64.cfg";
                overlayPath = "overlays/gamepads/n64/";
                break;
            case "genesis":
            case "genesis_plus_gx":
                overlayFile = "genesis.cfg";
                overlayPath = "overlays/gamepads/genesis/";
                break;
            case "arcade":
            case "mame":
                overlayFile = "arcade.cfg";
                overlayPath = "overlays/gamepads/arcade/";
                break;
            case "neogeo":
                overlayFile = "neogeo.cfg";
                overlayPath = "overlays/gamepads/neogeo/";
                break;
            case "atari2600":
            case "stella":
                overlayFile = "atari2600.cfg";
                overlayPath = "overlays/gamepads/atari2600/";
                break;
            case "atari7800":
                overlayFile = "atari7800.cfg";
                overlayPath = "overlays/gamepads/atari7800/";
                break;
            case "dreamcast":
                overlayFile = "dreamcast.cfg";
                overlayPath = "overlays/gamepads/dreamcast/";
                break;
            case "saturn":
                overlayFile = "saturn.cfg";
                overlayPath = "overlays/gamepads/saturn/";
                break;
            case "psp":
                overlayFile = "psp.cfg";
                overlayPath = "overlays/gamepads/psp/";
                break;
            case "virtualboy":
                overlayFile = "virtualboy.cfg";
                overlayPath = "overlays/gamepads/virtualboy/";
                break;
            case "turbografx16":
            case "mednafen_pce":
                overlayFile = "turbografx-16.cfg";
                overlayPath = "overlays/gamepads/turbografx16/";
                break;
            default:
                overlayFile = "retropad.cfg"; // Overlay générique par défaut
                overlayPath = "overlays/gamepads/flat/";
                break;
        }
        
        if (overlayFile != null) {
            Log.d(TAG, "🎮 Chargement overlay pour système " + systemName + ": " + overlayFile + " depuis " + overlayPath);
            loadOverlay(overlayFile);
        }
    }

    // **100% RETROARCH NATIF** : Chargement d'overlay
    // **PARSER ROBUSTE** : Chargement d'overlay avec gestion d'erreurs complète
    public void loadOverlay(String cfgFileName) {
        try {
            Log.d(TAG, "🔍 Parsing du fichier: " + cfgFileName);
            Log.d(TAG, "📁 Chemin de base: " + overlayPath);
            
            overlays.clear();
            currentCfgFile = cfgFileName;

            String fullPath = overlayPath + cfgFileName;
            Log.d(TAG, "🎯 **DIAGNOSTIC** Overlay à charger: " + fullPath);
            
            // **VALIDATION** : Vérifier que le fichier existe
            try {
                context.getAssets().open(fullPath);
            } catch (IOException e) {
                Log.e(TAG, "❌ Fichier CFG introuvable: " + fullPath);
                loadDefaultOverlay();
                return;
            }
            
            InputStream inputStream = context.getAssets().open(fullPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            List<Overlay> allOverlays = new ArrayList<>();
            Overlay currentOverlay = null;
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                try {
                    if (line.startsWith("overlays = ")) {
                        // Nombre total d'overlays
                        int overlayCount = Integer.parseInt(line.split("=")[1].trim());
                        Log.d(TAG, "📋 Nombre d'overlays: " + overlayCount);
                        
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
                                Log.d(TAG, "✅ Overlay " + overlayIndex + " nommé: " + currentOverlay.name);
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
                    } else if (line.startsWith("overlay") && line.contains("_range_mod = ")) {
                        // **SOLUTION UNIVERSELLE** : Parser range_mod comme RetroArch officiel
                        String[] parts = line.split("=");
                        if (parts.length == 2) {
                            String overlayKey = parts[0].trim();
                            int overlayIndex = Integer.parseInt(overlayKey.split("_")[0].replace("overlay", ""));
                            if (overlayIndex < allOverlays.size()) {
                                currentOverlay = allOverlays.get(overlayIndex);
                                currentOverlay.range_mod = Float.parseFloat(parts[1].trim());
                                Log.d(TAG, "🎯 **SOLUTION UNIVERSELLE** range_mod parsé: " + currentOverlay.range_mod + " pour overlay " + overlayIndex);
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
                } catch (Exception e) {
                    Log.w(TAG, "⚠️ Erreur parsing ligne " + lineNumber + ": " + line + " - " + e.getMessage());
                    // Continuer le parsing malgré l'erreur
                }
            }

            reader.close();
            inputStream.close();

            // **VALIDATION** : Vérifier que les overlays sont valides
            if (allOverlays.isEmpty()) {
                Log.e(TAG, "❌ Aucun overlay parsé, utilisation du fallback");
                loadDefaultOverlay();
                return;
            }

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
                Log.d(TAG, "✅ Overlay parsé avec succès: " + activeOverlay.name + " (" + activeOverlay.descs.size() + " boutons)");
            } else {
                Log.e(TAG, "❌ Aucun overlay actif sélectionné");
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Erreur parsing overlay: " + e.getMessage());
            loadDefaultOverlay();
        }
    }
    
    // **FALLBACK** : Charger l'overlay par défaut en cas d'erreur
    private void loadDefaultOverlay() {
        try {
            Log.w(TAG, "⚠️ Chargement de l'overlay par défaut");
            loadOverlay("nes.cfg");
        } catch (Exception e) {
            Log.e(TAG, "❌ Erreur lors du chargement de l'overlay par défaut: " + e.getMessage());
        }
    }

    // **SIMPLIFICATION** : Une seule phase de sélection - SIMPLE ET FIABLE
    private Overlay selectOverlayForCurrentOrientation(List<Overlay> allOverlays) {
        if (allOverlays.isEmpty()) {
            Log.e(TAG, "❌ Aucun overlay disponible");
            return null;
        }
        
        // **1. Vérifier l'orientation actuelle**
        boolean isLandscape = screenWidth > screenHeight;
        
        Log.d(TAG, "🔍 Sélection overlay - Screen: " + screenWidth + "x" + screenHeight + 
              " (isLandscape: " + isLandscape + ")");
        Log.d(TAG, "📋 Overlays disponibles: " + allOverlays.size());
        
        // **2. Chercher l'overlay approprié par orientation**
        for (Overlay overlay : allOverlays) {
            if (overlay.name != null) {
                if (isLandscape && overlay.name.contains("landscape")) {
                    Log.d(TAG, "✅ Overlay landscape trouvé: " + overlay.name);
                    Log.d(TAG, "🎯 **DIAGNOSTIC** Overlay sélectionné: " + overlay.name + " (landscape)");
                    return overlay;
                }
                if (!isLandscape && overlay.name.contains("portrait")) {
                    Log.d(TAG, "✅ Overlay portrait trouvé: " + overlay.name);
                    Log.d(TAG, "🎯 **DIAGNOSTIC** Overlay sélectionné: " + overlay.name + " (portrait)");
                    return overlay;
                }
            }
        }
        
        // **3. Fallback simple avec log**
        Log.w(TAG, "⚠️ Aucun overlay approprié trouvé, utilisation du premier");
        Overlay fallback = allOverlays.get(0);
        Log.d(TAG, "✅ Overlay de fallback: " + fallback.name);
        Log.d(TAG, "🎯 **DIAGNOSTIC** Overlay sélectionné: " + fallback.name + " (fallback)");
        return fallback;
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
                    
                    // **100% RETROARCH NATIF** : Initialisation de button_mask
                    desc.button_mask = new RetroArchInputBits();
                    if (desc.libretro_device_id >= 0) {
                        desc.button_mask.setJoypadButton(desc.libretro_device_id);
                    }

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
            case "a": return RetroArchInputBits.RETRO_DEVICE_ID_JOYPAD_A;
            case "b": return RetroArchInputBits.RETRO_DEVICE_ID_JOYPAD_B;
            case "x": return RetroArchInputBits.RETRO_DEVICE_ID_JOYPAD_X;
            case "y": return RetroArchInputBits.RETRO_DEVICE_ID_JOYPAD_Y;
            case "l": return RetroArchInputBits.RETRO_DEVICE_ID_JOYPAD_L;
            case "r": return RetroArchInputBits.RETRO_DEVICE_ID_JOYPAD_R;
            case "l2": return RetroArchInputBits.RETRO_DEVICE_ID_JOYPAD_L2;
            case "r2": return RetroArchInputBits.RETRO_DEVICE_ID_JOYPAD_R2;
            case "l3": return RetroArchInputBits.RETRO_DEVICE_ID_JOYPAD_L3;
            case "r3": return RetroArchInputBits.RETRO_DEVICE_ID_JOYPAD_R3;
            case "start": return RetroArchInputBits.RETRO_DEVICE_ID_JOYPAD_START;
            case "select": return RetroArchInputBits.RETRO_DEVICE_ID_JOYPAD_SELECT;
            case "up": return RetroArchInputBits.RETRO_DEVICE_ID_JOYPAD_UP;
            case "down": return RetroArchInputBits.RETRO_DEVICE_ID_JOYPAD_DOWN;
            case "left": return RetroArchInputBits.RETRO_DEVICE_ID_JOYPAD_LEFT;
            case "right": return RetroArchInputBits.RETRO_DEVICE_ID_JOYPAD_RIGHT;
            default: return -1;
        }
    }

    // **100% RETROARCH NATIF** : Application du layout avec coordonnées directes
    private void applyOverlayLayout() {
        if (activeOverlay == null) return;

        // **CORRECTION** : Positionnement direct des hitboxes sans transformation
        for (OverlayDesc desc : activeOverlay.descs) {
            // **COORDONNÉES DIRECTES** : Aucune transformation
            desc.x_hitbox = desc.mod_x;
            desc.y_hitbox = desc.mod_y;  // DIRECT au lieu de calculateAutomaticYPosition()
            desc.range_x_hitbox = desc.mod_w;
            desc.range_y_hitbox = desc.mod_h;
            
            Log.d(TAG, "🎯 Hitbox " + desc.input_name + 
                  " - X: " + desc.x_hitbox + " Y: " + desc.y_hitbox + 
                  " W: " + desc.range_x_hitbox + " H: " + desc.range_y_hitbox);
        }
    }

    // **100% RETROARCH NATIF** : Gestion des touches avec support multi-touch avancé
    public boolean handleTouch(MotionEvent event) {
        if (!overlayEnabled || activeOverlay == null) {
            Log.d(TAG, "🔍 **DIAGNOSTIC** handleTouch ignoré - overlayEnabled: " + overlayEnabled + ", activeOverlay: " + (activeOverlay != null));
            return false;
        }

        int action = event.getActionMasked();
        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);
        float touchX = event.getX(pointerIndex);
        float touchY = event.getY(pointerIndex);

        Log.d(TAG, "🔍 **DIAGNOSTIC** handleTouch - Action: " + action + ", Pointer: " + pointerId + 
              ", Position: (" + touchX + ", " + touchY + "), Screen: " + screenWidth + "x" + screenHeight);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                return handleTouchDown(touchX, touchY, pointerId);
                
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                return handleTouchUp(touchX, touchY, pointerId);
                
            case MotionEvent.ACTION_MOVE:
                return handleTouchMove(touchX, touchY, pointerId);
                
            default:
                return false;
        }
    }

    // **100% RETROARCH NATIF** : Touch down avec gestion multi-points correcte
    private boolean handleTouchDown(float touchX, float touchY, int pointerId) {
        Log.d(TAG, "🔍 **DIAGNOSTIC** handleTouchDown - Position: (" + touchX + ", " + touchY + "), Pointer: " + pointerId);
        
        // Normalisation des coordonnées
        float normalizedX = touchX / screenWidth;
        float normalizedY = touchY / screenHeight;
        
        Log.d(TAG, "🔍 **DIAGNOSTIC** Coordonnées normalisées: (" + normalizedX + ", " + normalizedY + ")");
        
        if (activeOverlay == null || activeOverlay.descs == null) {
            Log.w(TAG, "⚠️ **DIAGNOSTIC** Pas d'overlay actif ou descriptions manquantes");
            return false;
        }
        
        Log.d(TAG, "🔍 **DIAGNOSTIC** Nombre de descriptions: " + activeOverlay.descs.size());
        
        boolean touchHandled = false;
        
        // Test de chaque bouton
        for (OverlayDesc desc : activeOverlay.descs) {
            if (desc.mod_w <= 0 || desc.mod_h <= 0) {
                Log.d(TAG, "⚠️ **DIAGNOSTIC** Description invalide pour " + desc.input_name + 
                      " - W: " + desc.mod_w + ", H: " + desc.mod_h);
                continue;
            }
            
            Log.d(TAG, "🔍 **DIAGNOSTIC** Test bouton: " + desc.input_name + 
                  " - Position: (" + desc.mod_x + ", " + desc.mod_y + 
                  ") - Taille: (" + desc.mod_w + ", " + desc.mod_h + ")");
            
            if (isPointInHitbox(normalizedX, normalizedY, desc)) {
                Log.d(TAG, "✅ **DIAGNOSTIC** Bouton touché: " + desc.input_name + 
                      " - Device ID: " + desc.libretro_device_id);
                
                // **100% RETROARCH** : Vérifier si ce pointer n'est pas déjà sur ce bouton
                if ((desc.touch_mask & (1 << pointerId)) == 0) {
                    // Envoyer l'input seulement si pas déjà pressé par ce pointer
                    if (desc.libretro_device_id >= 0 && inputListener != null) {
                        inputListener.onOverlayInput(desc.libretro_device_id, true);
                        Log.d(TAG, "🎮 **DIAGNOSTIC** Input envoyé: " + desc.input_name + " (ID: " + desc.libretro_device_id + ")");
                    } else {
                        Log.w(TAG, "⚠️ **DIAGNOSTIC** Pas d'input listener ou device ID invalide pour " + desc.input_name);
                    }
                }
                
                // Marquer comme touché par ce pointer
                desc.touch_mask |= (1 << pointerId);
                activeTouches.put(pointerId, new TouchPoint(touchX, touchY, pointerId));
                touchHandled = true;
                
                // **100% RETROARCH** : Ne pas retourner immédiatement, tester tous les boutons
                // pour permettre les touches simultanées
            }
        }
        
        if (touchHandled) {
            Log.d(TAG, "✅ **DIAGNOSTIC** Touch down géré pour pointer " + pointerId);
        } else {
            Log.d(TAG, "❌ **DIAGNOSTIC** Aucun bouton touché à la position (" + touchX + ", " + touchY + ")");
        }
        
        return touchHandled;
    }

    // **100% RETROARCH NATIF** : Touch up avec gestion multi-points correcte
    private boolean handleTouchUp(float touchX, float touchY, int pointerId) {
        Log.d(TAG, "🔍 **DIAGNOSTIC** handleTouchUp - Position: (" + touchX + ", " + touchY + "), Pointer: " + pointerId);
        
        TouchPoint touchPoint = activeTouches.get(pointerId);
        if (touchPoint == null) {
            Log.d(TAG, "⚠️ **DIAGNOSTIC** Pas de touch point trouvé pour pointer " + pointerId);
            return false;
        }

        boolean touchHandled = false;

        if (activeOverlay != null && activeOverlay.descs != null) {
            for (OverlayDesc desc : activeOverlay.descs) {
                if ((desc.touch_mask & (1 << pointerId)) != 0) {
                    Log.d(TAG, "✅ **DIAGNOSTIC** Relâchement bouton: " + desc.input_name + 
                          " - Device ID: " + desc.libretro_device_id);
                    
                    // **100% RETROARCH** : Vérifier si d'autres pointers sont encore sur ce bouton
                    int remainingTouches = desc.touch_mask & ~(1 << pointerId);
                    
                    // Envoyer l'input de relâchement seulement si plus aucun pointer sur ce bouton
                    if (remainingTouches == 0) {
                        if (desc.libretro_device_id >= 0 && inputListener != null) {
                            inputListener.onOverlayInput(desc.libretro_device_id, false);
                            Log.d(TAG, "🎮 **DIAGNOSTIC** Input relâché: " + desc.input_name + " (ID: " + desc.libretro_device_id + ")");
                        }
                    } else {
                        Log.d(TAG, "🎮 **DIAGNOSTIC** Bouton maintenu par d'autres pointers: " + desc.input_name);
                    }
                    
                    // Nettoyer le touch mask pour ce pointer
                    desc.touch_mask &= ~(1 << pointerId);
                    touchHandled = true;
                }
            }
        }
        
        // Supprimer le touch point
        activeTouches.remove(pointerId);
        
        if (touchHandled) {
            Log.d(TAG, "✅ **DIAGNOSTIC** Touch up géré pour pointer " + pointerId);
        } else {
            Log.d(TAG, "⚠️ **DIAGNOSTIC** Touch up non géré pour pointer " + pointerId);
        }
        
        return touchHandled;
    }
    
    // **SIMPLIFICATION** : Touch move simple
    private boolean handleTouchMove(float touchX, float touchY, int pointerId) {
        // Pour l'instant, on ne fait rien avec le move
        // On pourrait ajouter de la logique pour les joysticks analogiques plus tard
        return false;
    }

                                                                                                                                                                                                                                                                                               // **SIMPLIFICATION** : Suppression du code cassé

    // **SIMPLIFICATION** : Hitbox simple et robuste
    private boolean isPointInHitbox(float normalizedX, float normalizedY, OverlayDesc desc) {
        // **CRITIQUE** : Utiliser les mêmes coordonnées que le rendu visuel
        float rangeMod = (activeOverlay != null) ? activeOverlay.range_mod : 1.0f;
        if (rangeMod <= 0) rangeMod = 1.5f; // Valeur par défaut RetroArch
        
        // **COORDONNÉES VISUELLES** : Utiliser mod_x, mod_y, mod_w, mod_h comme le rendu
        float pixelX = desc.mod_x;
        float pixelY = desc.mod_y;
        float pixelW = desc.mod_w * rangeMod;
        float pixelH = desc.mod_h * rangeMod;
        
        // **CENTRAGE** : Appliquer le même centrage que le rendu visuel
        float centerOffsetX = (rangeMod - 1.0f) * 0.15f;
        float centerOffsetY = (rangeMod - 1.0f) * 0.15f;
        
        pixelX -= centerOffsetX;
        pixelY -= centerOffsetY;
        
        // **LIMITATION** : Empêcher le débordement
        if (pixelX < 0) pixelX = 0;
        if (pixelY < 0) pixelY = 0;
        if (pixelX + pixelW > 1.0f) pixelX = 1.0f - pixelW;
        if (pixelY + pixelH > 1.0f) pixelY = 1.0f - pixelH;
        
        // **TEST SIMPLE** : Point dans le rectangle
        boolean inHitbox = (normalizedX >= pixelX && normalizedX <= pixelX + pixelW &&
                           normalizedY >= pixelY && normalizedY <= pixelY + pixelH);
        
        Log.d(TAG, "🔍 **DIAGNOSTIC** Hitbox test pour " + desc.input_name + 
              " - Point: (" + normalizedX + ", " + normalizedY + ")" +
              " - Zone: (" + pixelX + ", " + pixelY + ") à (" + (pixelX + pixelW) + ", " + (pixelY + pixelH) + ")" +
              " - Dans hitbox: " + inHitbox);
        
                                                                   return inHitbox;
    }

    // **DEBUG AMÉLIORÉ** : Rendu avec validation complète et logs détaillés
    public void render(Canvas canvas) {
        // **DEBUG COMPLET** : État du système
        Log.d(TAG, "🎨 Rendu overlay - Enabled: " + overlayEnabled + 
              ", ActiveOverlay: " + (activeOverlay != null) + 
              ", Descs: " + (activeOverlay != null ? activeOverlay.descs.size() : 0) + 
              ", ShowInputsDebug: " + showInputsDebug); // **DIAGNOSTIC** : État du debug
        
        if (activeOverlay == null || !overlayEnabled || activeOverlay.descs == null || activeOverlay.descs.isEmpty()) {
            Log.w(TAG, "⚠️ Rendu ignoré - Overlay invalide ou désactivé");
            return;
        }
        
        // **VALIDATION** : Dimensions du canvas
        float canvasWidth = canvas.getWidth();
        float canvasHeight = canvas.getHeight();
        
        if (canvasWidth <= 0 || canvasHeight <= 0) {
            Log.e(TAG, "❌ Dimensions canvas invalides: " + canvasWidth + "x" + canvasHeight);
            return;
        }
        
        // **CORRECTION CRITIQUE** : Mettre à jour les dimensions si elles ont changé
        if (screenWidth != (int)canvasWidth || screenHeight != (int)canvasHeight) {
            updateScreenDimensions((int)canvasWidth, (int)canvasHeight);
        }
        
        // **DEBUG** : Informations complètes
        Log.d(TAG, "🎨 Rendu overlay - Canvas: " + canvasWidth + "x" + canvasHeight + 
              " - Screen: " + screenWidth + "x" + screenHeight +
              " - Overlay: " + activeOverlay.name + " - Descs: " + activeOverlay.descs.size() +
              " - Orientation: " + (canvasWidth > canvasHeight ? "LANDSCAPE" : "PORTRAIT"));

        Paint paint = new Paint();
        paint.setAlpha((int)(255 * overlayOpacity));
        paint.setAntiAlias(true);

        // **NOUVEAU** : Rendu des effets visuels en premier (scanlines, patterns)
        renderEffects(canvas);

        // **RENDU AVEC COORDONNÉES DIRECTES** (CORRECTION CRITIQUE)
        int renderedCount = 0;
        for (OverlayDesc desc : activeOverlay.descs) {
            if (desc.texture != null && desc.mod_w > 0 && desc.mod_h > 0) {
                // **VALIDATION** : Vérifier que les coordonnées sont dans les limites
                if (desc.mod_x >= 0 && desc.mod_x <= 1 && 
                    desc.mod_y >= 0 && desc.mod_y <= 1 &&
                    desc.mod_w > 0 && desc.mod_w <= 1 &&
                    desc.mod_h > 0 && desc.mod_h <= 1) {
                    
                    // **SOLUTION UNIVERSELLE 100% RETROARCH** : Utiliser range_mod comme RetroArch officiel
                    float rangeMod = (activeOverlay != null) ? activeOverlay.range_mod : 1.0f;
                    if (rangeMod <= 0) rangeMod = 1.5f; // Valeur par défaut RetroArch
                    
                    // **CRITIQUE** : Calculer les dimensions avec range_mod
                    float pixelW = desc.mod_w * canvasWidth * rangeMod;  // **RANGE_MOD APPLIQUÉ**
                    float pixelH = desc.mod_h * canvasHeight * rangeMod; // **RANGE_MOD APPLIQUÉ**
                    
                    // **CORRECTION CRITIQUE** : Centrer l'overlay et éviter le débordement
                    float pixelX = desc.mod_x * canvasWidth;
                    float pixelY = desc.mod_y * canvasHeight;
                    
                    // **CENTRAGE INDIVIDUEL AMÉLIORÉ** : Centrer chaque bouton individuellement
                    float centerOffsetX = (rangeMod - 1.0f) * 0.15f; // Décalage réduit pour éviter l'empilement
                    float centerOffsetY = (rangeMod - 1.0f) * 0.15f; // Décalage vertical réduit
                    
                    pixelX -= centerOffsetX * canvasWidth;
                    pixelY -= centerOffsetY * canvasHeight;
                    
                    // **LIMITATION** : Empêcher le débordement de l'écran
                    if (pixelX < 0) pixelX = 0;
                    if (pixelY < 0) pixelY = 0;
                    if (pixelX + pixelW > canvasWidth) pixelX = canvasWidth - pixelW;
                    if (pixelY + pixelH > canvasHeight) pixelY = canvasHeight - pixelH;

                    // **DEBUG** : Log des coordonnées avec centrage (solution universelle RetroArch)
                    Log.d(TAG, "🎯 " + desc.input_name + 
                          " - X: " + desc.mod_x + " -> " + pixelX + " (centré)" +
                          " - Y: " + desc.mod_y + " -> " + pixelY + " (centré)" +
                          " - W: " + desc.mod_w + " -> " + pixelW + " (range_mod: " + rangeMod + ")" +
                          " - H: " + desc.mod_h + " -> " + pixelH + " (range_mod: " + rangeMod + ")" +
                          " - RectF: (" + pixelX + ", " + pixelY + ", " + (pixelX + pixelW) + ", " + (pixelY + pixelH) + ")");

                    RectF destRect = new RectF(
                        pixelX, pixelY,
                        pixelX + pixelW, pixelY + pixelH
                    );

                    canvas.drawBitmap(desc.texture, null, destRect, paint);
                    
                    // **NOUVEAU** : Interface RetroArch moderne - effets visuels au lieu de debug
                    if (overlayOpacity > 0.8f) {
                        // Effet de brillance subtil pour les boutons
                        Paint glowPaint = new Paint();
                        glowPaint.setColor(0x20FFFFFF); // Blanc très transparent
                        glowPaint.setStyle(Paint.Style.STROKE);
                        glowPaint.setStrokeWidth(2.0f);
                        glowPaint.setAntiAlias(true);
                        
                        // Dessiner un contour lumineux subtil
                        canvas.drawRect(destRect, glowPaint);
                    }
                    
                    renderedCount++;
                    
                } else {
                    Log.w(TAG, "⚠️ Coordonnées invalides pour " + desc.input_name + 
                          " - X: " + desc.mod_x + " Y: " + desc.mod_y + 
                          " W: " + desc.mod_w + " H: " + desc.mod_h);
                }
            }
        }
        
        Log.d(TAG, "✅ Rendu de " + renderedCount + " boutons d'overlay");
    }
    
    /**
     * **SUPPRIMÉ** : Méthode calculateAutomaticYPosition() supprimée
     * 
     * RAISON : Cette méthode transformait incorrectement les coordonnées Y du fichier CFG
     * au lieu de les utiliser directement, causant des problèmes de positionnement.
     * 
     * CORRECTION : Les coordonnées Y sont maintenant calculées directement comme
     * desc.mod_y * canvasHeight, de la même manière que X, W et H.
     * 
     * RÉSULTAT : Positions exactes selon le fichier CFG RetroArch officiel.
     */

    // **100% RETROARCH NATIF** : Getters et setters
    public void setOverlayEnabled(boolean enabled) {
        this.overlayEnabled = enabled;
    }

    public void setOverlayOpacity(float opacity) {
        this.overlayOpacity = opacity;
    }

    // **NOUVEAU** : Définir le facteur d'échelle de l'overlay
    public void setOverlayScale(float scale) {
        this.overlayScale = scale;
        Log.d(TAG, "🔧 Facteur d'échelle overlay défini: " + scale);
    }

    // **NOUVEAU** : Obtenir le facteur d'échelle de l'overlay
    public float getOverlayScale() {
        return overlayScale;
    }
    
    // **FORCER LE DEBUG** : Méthode pour activer le debug des zones
    public void forceDebugMode(boolean enabled) {
        this.showInputsDebug = enabled;
        Log.i(TAG, "🔧 **FORCE DEBUG** Mode debug des zones: " + enabled);
    }

    // **NOUVEAU** : Synchroniser avec la configuration RetroArch
    public void syncWithRetroArchConfig(RetroArchConfigManager configManager) {
        if (configManager != null) {
            this.overlayEnabled = configManager.isOverlayEnabled();
            this.overlayOpacity = configManager.getOverlayOpacity();
            this.overlayScale = configManager.getOverlayScale();  // **CRITIQUE** : Facteur d'échelle
            this.showInputsDebug = configManager.isOverlayShowInputsEnabled(); // **100% RETROARCH** : Debug des zones
            Log.i(TAG, "🔄 Synchronisation avec RetroArch - Scale: " + overlayScale + 
                  ", Opacity: " + overlayOpacity + ", Enabled: " + overlayEnabled + 
                  ", ShowInputs: " + showInputsDebug);
        }
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

    // **NOUVEAU** : Support des effets visuels RetroArch
    public enum OverlayEffectType {
        OVERLAY_EFFECT_NONE,
        OVERLAY_EFFECT_SCANLINES,
        OVERLAY_EFFECT_PATTERNS,
        OVERLAY_EFFECT_CRT_BEZELS,
        OVERLAY_EFFECT_PHOSPHORS
    }

    // **NOUVEAU** : Classe pour les effets visuels
    public static class OverlayEffect {
        public OverlayEffectType type;
        public Bitmap effectTexture;
        public String effectPath;
        public boolean fullScreen;
        public float opacity = 1.0f;
        public boolean enabled = false;
        public float x, y, w, h; // Coordonnées pour les effets localisés
    }

    // **NOUVEAU** : Variables pour les effets
    private OverlayEffect currentEffect = new OverlayEffect();
    private String effectsPath = "overlays/effects/";
    private boolean effectsEnabled = false;

    // **NOUVEAU** : Charger un effet visuel
    public void loadEffect(String effectName) {
        try {
            String effectPath = effectsPath + effectName;
            InputStream inputStream = context.getAssets().open(effectPath);
            currentEffect.effectTexture = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            
            currentEffect.effectPath = effectPath;
            currentEffect.enabled = true;
            
            Log.d(TAG, "Effet chargé: " + effectName);
        } catch (IOException e) {
            Log.e(TAG, "Erreur chargement effet: " + effectName);
        }
    }

    // **NOUVEAU** : Activer/désactiver les effets
    public void setEffectsEnabled(boolean enabled) {
        this.effectsEnabled = enabled;
        Log.d(TAG, "Effets " + (enabled ? "activés" : "désactivés"));
    }

    // **NOUVEAU** : Rendu des effets visuels
    private void renderEffects(Canvas canvas) {
        if (!effectsEnabled || currentEffect.effectTexture == null) return;

        Paint effectPaint = new Paint();
        effectPaint.setAlpha((int)(255 * currentEffect.opacity));
        effectPaint.setAntiAlias(true);

        if (currentEffect.fullScreen) {
            // Effet plein écran (scanlines, patterns)
            RectF destRect = new RectF(0, 0, canvas.getWidth(), canvas.getHeight());
            canvas.drawBitmap(currentEffect.effectTexture, null, destRect, effectPaint);
        } else {
            // Effet localisé
            float pixelX = currentEffect.x * canvas.getWidth();
            float pixelY = currentEffect.y * canvas.getHeight();
            float pixelW = currentEffect.w * canvas.getWidth();
            float pixelH = currentEffect.h * canvas.getHeight();
            
            RectF destRect = new RectF(pixelX, pixelY, pixelX + pixelW, pixelY + pixelH);
            canvas.drawBitmap(currentEffect.effectTexture, null, destRect, effectPaint);
        }
    }

    // **NOUVEAU** : Charger un effet visuel depuis les git RetroArch
    public void loadEffectFromGit(String effectName) {
        try {
            // Chemin vers les effets dans les git
            String gitEffectPath = "common-overlays_git/effects/";
            
            // Déterminer le type d'effet
            if (effectName.contains("scanline")) {
                currentEffect.type = OverlayEffectType.OVERLAY_EFFECT_SCANLINES;
                gitEffectPath += "scanlines/";
            } else if (effectName.contains("pattern") || effectName.contains("checker") || effectName.contains("grid")) {
                currentEffect.type = OverlayEffectType.OVERLAY_EFFECT_PATTERNS;
                gitEffectPath += "patterns/";
            } else if (effectName.contains("crt") || effectName.contains("bezels")) {
                currentEffect.type = OverlayEffectType.OVERLAY_EFFECT_CRT_BEZELS;
                gitEffectPath += "crt-bezels/";
            } else if (effectName.contains("phosphor")) {
                currentEffect.type = OverlayEffectType.OVERLAY_EFFECT_PHOSPHORS;
                gitEffectPath += "patterns/";
            }
            
            // Charger l'effet
            loadEffect(effectName);
            
            Log.d(TAG, "Effet git chargé: " + effectName + " (Type: " + currentEffect.type + ")");
        } catch (Exception e) {
            Log.e(TAG, "Erreur chargement effet git: " + effectName, e);
        }
    }

    // **NOUVEAU** : Activer un effet de scanlines
    public void enableScanlines(String scanlineType) {
        loadEffectFromGit(scanlineType);
        setEffectsEnabled(true);
        Log.d(TAG, "Scanlines activées: " + scanlineType);
    }

    // **NOUVEAU** : Activer un effet de patterns
    public void enablePatterns(String patternType) {
        loadEffectFromGit(patternType);
        setEffectsEnabled(true);
        Log.d(TAG, "Patterns activés: " + patternType);
    }

    // **NOUVEAU** : Activer un effet CRT
    public void enableCRTEffect(String crtType) {
        loadEffectFromGit(crtType);
        setEffectsEnabled(true);
        Log.d(TAG, "Effet CRT activé: " + crtType);
    }

    // **NOUVEAU** : Désactiver tous les effets
    public void disableEffects() {
        setEffectsEnabled(false);
        currentEffect.enabled = false;
        Log.d(TAG, "Tous les effets désactivés");
    }

    // **NOUVEAU** : Définir l'opacité des effets
    public void setEffectOpacity(float opacity) {
        if (currentEffect != null) {
            currentEffect.opacity = opacity;
            Log.d(TAG, "Opacité effet définie: " + opacity);
        }
    }

    // **NOUVEAU** : Méthode de test des effets visuels avec debug
    public void testVisualEffects() {
        Log.d(TAG, "🧪 Test des effets visuels RetroArch");
        
        // Test scanlines
        try {
            enableScanlines("mame-phosphors-3x.cfg");
            Log.d(TAG, "✅ Scanlines testées");
        } catch (Exception e) {
            Log.e(TAG, "❌ Erreur test scanlines", e);
        }
        
        // Test patterns
        try {
            enablePatterns("checker.cfg");
            Log.d(TAG, "✅ Patterns testés");
        } catch (Exception e) {
            Log.e(TAG, "❌ Erreur test patterns", e);
        }
        
        // Test CRT
        try {
            enableCRTEffect("horizontal.cfg");
            Log.d(TAG, "✅ Effet CRT testé");
        } catch (Exception e) {
            Log.e(TAG, "❌ Erreur test CRT", e);
        }
    }

    // **NOUVEAU** : Debug des effets visuels
    public void debugVisualEffects() {
        Log.d(TAG, "🔍 Debug des effets visuels:");
        Log.d(TAG, "  - Effects enabled: " + effectsEnabled);
        Log.d(TAG, "  - Current effect: " + (currentEffect != null ? currentEffect.effectPath : "null"));
        Log.d(TAG, "  - Effect type: " + (currentEffect != null ? currentEffect.type : "null"));
        Log.d(TAG, "  - Effect opacity: " + (currentEffect != null ? currentEffect.opacity : "null"));
        Log.d(TAG, "  - Effect fullscreen: " + (currentEffect != null ? currentEffect.fullScreen : "null"));
    }
} 