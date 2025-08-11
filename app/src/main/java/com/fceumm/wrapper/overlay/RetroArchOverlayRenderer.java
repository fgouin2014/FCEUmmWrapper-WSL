package com.fceumm.wrapper.overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.util.Log;
import com.fceumm.wrapper.input.RetroArchInputSystem;
import java.util.Map;

/**
 * **100% RETROARCH NATIF** : Rendu d'overlay authentique conforme aux standards RetroArch
 * Reproduit fidèlement l'apparence visuelle de RetroArch
 */
public class RetroArchOverlayRenderer {
    private static final String TAG = "RetroArchOverlayRenderer";
    
    private Context context;
    private RetroArchInputSystem inputSystem;
    private Paint paint;
    private Paint shadowPaint;
    private Paint textPaint;
    private boolean debugMode = true;
    private float opacity = 0.85f;
    
    // **100% RETROARCH** : Couleurs authentiques RetroArch
    private static final int COLOR_BACKGROUND = Color.argb(200, 20, 20, 20);
    private static final int COLOR_DPAD_BASE = Color.argb(255, 60, 60, 60);
    private static final int COLOR_DPAD_HIGHLIGHT = Color.argb(255, 100, 100, 100);
    private static final int COLOR_BUTTON_A = Color.argb(255, 220, 50, 50);
    private static final int COLOR_BUTTON_B = Color.argb(255, 50, 50, 220);
    private static final int COLOR_START = Color.argb(255, 50, 220, 50);
    private static final int COLOR_SELECT = Color.argb(255, 220, 220, 50);
    private static final int COLOR_PRESSED = Color.argb(255, 255, 255, 255);
    private static final int COLOR_SHADOW = Color.argb(100, 0, 0, 0);
    private static final int COLOR_TEXT = Color.argb(255, 255, 255, 255);
    private static final int COLOR_TEXT_SHADOW = Color.argb(150, 0, 0, 0);
    
    public RetroArchOverlayRenderer(Context context, RetroArchInputSystem inputSystem) {
        this.context = context;
        this.inputSystem = inputSystem;
        initPaints();
        Log.i(TAG, "🎨 Rendu d'overlay RetroArch authentique initialisé");
    }
    
    private void initPaints() {
        // **PEINTURE PRINCIPALE**
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha((int)(255 * opacity));
        
        // **PEINTURE OMBRE**
        shadowPaint = new Paint();
        shadowPaint.setAntiAlias(true);
        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setColor(COLOR_SHADOW);
        shadowPaint.setAlpha(100);
        
        // **PEINTURE TEXTE**
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(COLOR_TEXT);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setShadowLayer(2.0f, 1.0f, 1.0f, COLOR_TEXT_SHADOW);
    }
    
    /**
     * **100% RETROARCH** : Rendre l'overlay authentique
     */
    public void render(Canvas canvas) {
        if (canvas == null || inputSystem == null) {
            Log.w(TAG, "⚠️ Rendu ignoré - Canvas ou inputSystem null");
            return;
        }
        
        // **VALIDATION** : Dimensions du canvas
        float canvasWidth = canvas.getWidth();
        float canvasHeight = canvas.getHeight();
        
        if (canvasWidth <= 0 || canvasHeight <= 0) {
            Log.e(TAG, "❌ Dimensions canvas invalides: " + canvasWidth + "x" + canvasHeight);
            return;
        }
        
        Log.d(TAG, "🎨 Rendu overlay authentique - Canvas: " + canvasWidth + "x" + canvasHeight);
        
        // **RENDU DES ZONES TACTILES AUTHENTIQUES**
        renderAuthenticTouchZones(canvas);
        
        // **DEBUG** : Afficher les informations de debug
        if (debugMode) {
            renderDebugInfo(canvas);
        }
    }
    
    /**
     * **100% RETROARCH** : Rendre les zones tactiles authentiques
     */
    private void renderAuthenticTouchZones(Canvas canvas) {
        Map<String, RetroArchInputSystem.TouchZone> zones = inputSystem.getTouchZones();
        
        for (RetroArchInputSystem.TouchZone zone : zones.values()) {
            if (zone.name.startsWith("dpad_")) {
                renderAuthenticDPadDirection(canvas, zone);
            } else {
                renderAuthenticButton(canvas, zone);
            }
        }
    }
    
    /**
     * **100% RETROARCH** : Rendre une direction du D-Pad authentique RetroArch
     */
    private void renderAuthenticDPadDirection(Canvas canvas, RetroArchInputSystem.TouchZone zone) {
        float centerX = (zone.bounds.left + zone.bounds.right) * canvas.getWidth() / 2;
        float centerY = (zone.bounds.top + zone.bounds.bottom) * canvas.getHeight() / 2;
        float radius = Math.min(zone.bounds.width(), zone.bounds.height()) * canvas.getWidth() / 2;
        
        // **OMBRE DE LA DIRECTION**
        shadowPaint.setAlpha(60);
        canvas.drawCircle(centerX + 2, centerY + 2, radius, shadowPaint);
        
        // **COULEUR DE BASE**
        paint.setColor(zone.isPressed ? COLOR_PRESSED : COLOR_DPAD_BASE);
        
        // **DIRECTION PRINCIPALE** avec gradient
        RadialGradient gradient = new RadialGradient(
            centerX, centerY, radius,
            new int[]{zone.isPressed ? COLOR_PRESSED : COLOR_DPAD_BASE, 
                     zone.isPressed ? COLOR_PRESSED : darkenColor(COLOR_DPAD_BASE, 0.7f)},
            new float[]{0.0f, 1.0f},
            Shader.TileMode.CLAMP
        );
        paint.setShader(gradient);
        canvas.drawCircle(centerX, centerY, radius, paint);
        paint.setShader(null);
        
        // **HIGHLIGHT** (effet 3D)
        if (!zone.isPressed) {
            paint.setColor(COLOR_DPAD_HIGHLIGHT);
            canvas.drawCircle(centerX - radius * 0.3f, centerY - radius * 0.3f, radius * 0.4f, paint);
        }
        
        // **BORDURE**
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f);
        paint.setColor(darkenColor(COLOR_DPAD_BASE, 0.5f));
        canvas.drawCircle(centerX, centerY, radius, paint);
        paint.setStyle(Paint.Style.FILL);
        
        // **TEXTE DE LA DIRECTION**
        if (debugMode) {
            textPaint.setTextSize(radius * 0.6f);
            String direction = getDPadDirectionText(zone.name);
            float textY = centerY + radius * 0.2f;
            canvas.drawText(direction, centerX, textY, textPaint);
        }
    }
    
    /**
     * **100% RETROARCH** : Obtenir le texte d'une direction du D-Pad
     */
    private String getDPadDirectionText(String directionName) {
        switch (directionName) {
            case "dpad_up": return "↑";
            case "dpad_down": return "↓";
            case "dpad_left": return "←";
            case "dpad_right": return "→";
            default: return "?";
        }
    }
    
    /**
     * **100% RETROARCH** : Rendre un bouton authentique RetroArch
     */
    private void renderAuthenticButton(Canvas canvas, RetroArchInputSystem.TouchZone zone) {
        float x = zone.bounds.left * canvas.getWidth();
        float y = zone.bounds.top * canvas.getHeight();
        float width = zone.bounds.width() * canvas.getWidth();
        float height = zone.bounds.height() * canvas.getHeight();
        float centerX = x + width / 2;
        float centerY = y + height / 2;
        float radius = Math.min(width, height) / 2;
        
        // **OMBRE DU BOUTON**
        shadowPaint.setAlpha(100);
        canvas.drawCircle(centerX + 3, centerY + 3, radius, shadowPaint);
        
        // **COULEUR DE BASE**
        int baseColor = getAuthenticButtonColor(zone.name);
        paint.setColor(baseColor);
        
        // **BOUTON PRINCIPAL** avec gradient
        RadialGradient gradient = new RadialGradient(
            centerX, centerY, radius,
            new int[]{baseColor, darkenColor(baseColor, 0.7f)},
            new float[]{0.0f, 1.0f},
            Shader.TileMode.CLAMP
        );
        paint.setShader(gradient);
        canvas.drawCircle(centerX, centerY, radius, paint);
        paint.setShader(null);
        
        // **HIGHLIGHT** (effet 3D)
        if (!zone.isPressed) {
            paint.setColor(lightenColor(baseColor, 0.3f));
            canvas.drawCircle(centerX - radius * 0.3f, centerY - radius * 0.3f, radius * 0.4f, paint);
        }
        
        // **ÉTAT PRESSÉ**
        if (zone.isPressed) {
            paint.setColor(COLOR_PRESSED);
            canvas.drawCircle(centerX, centerY, radius * 0.8f, paint);
        }
        
        // **BORDURE**
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.0f);
        paint.setColor(darkenColor(baseColor, 0.5f));
        canvas.drawCircle(centerX, centerY, radius, paint);
        paint.setStyle(Paint.Style.FILL);
        
        // **TEXTE DU BOUTON**
        if (debugMode) {
            textPaint.setTextSize(radius * 0.5f);
            String text = getAuthenticButtonText(zone.name);
            float textY = centerY + radius * 0.15f;
            canvas.drawText(text, centerX, textY, textPaint);
        }
    }
    
    /**
     * **100% RETROARCH** : Obtenir la couleur authentique d'un bouton
     */
    private int getAuthenticButtonColor(String buttonName) {
        switch (buttonName) {
            case "a": return COLOR_BUTTON_A;
            case "b": return COLOR_BUTTON_B;
            case "start": return COLOR_START;
            case "select": return COLOR_SELECT;
            case "menu_toggle": return Color.argb(255, 255, 165, 0); // Orange
            case "quick_menu": return Color.argb(255, 128, 0, 128); // Violet
            default: return Color.argb(255, 128, 128, 128); // Gris
        }
    }
    
    /**
     * **100% RETROARCH** : Obtenir le texte authentique d'un bouton
     */
    private String getAuthenticButtonText(String buttonName) {
        switch (buttonName) {
            case "a": return "A";
            case "b": return "B";
            case "start": return "START";
            case "select": return "SELECT";
            case "menu_toggle": return "MENU";
            case "quick_menu": return "QUICK";
            default: return buttonName.toUpperCase();
        }
    }
    
    /**
     * **100% RETROARCH** : Assombrir une couleur
     */
    private int darkenColor(int color, float factor) {
        int red = (int)(Color.red(color) * factor);
        int green = (int)(Color.green(color) * factor);
        int blue = (int)(Color.blue(color) * factor);
        return Color.argb(Color.alpha(color), red, green, blue);
    }
    
    /**
     * **100% RETROARCH** : Éclaircir une couleur
     */
    private int lightenColor(int color, float factor) {
        int red = Math.min(255, (int)(Color.red(color) + (255 - Color.red(color)) * factor));
        int green = Math.min(255, (int)(Color.green(color) + (255 - Color.green(color)) * factor));
        int blue = Math.min(255, (int)(Color.blue(color) + (255 - Color.blue(color)) * factor));
        return Color.argb(Color.alpha(color), red, green, blue);
    }
    
    /**
     * **100% RETROARCH** : Rendre les informations de debug stylisées
     */
    private void renderDebugInfo(Canvas canvas) {
        // **FOND SEMI-TRANSPARENT**
        paint.setColor(COLOR_BACKGROUND);
        paint.setAlpha(180);
        canvas.drawRect(10, 10, 400, 200, paint);
        
        // **BORDURE**
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.0f);
        paint.setColor(Color.argb(255, 100, 100, 100));
        canvas.drawRect(10, 10, 400, 200, paint);
        paint.setStyle(Paint.Style.FILL);
        
        // **TEXTE DE DEBUG**
        textPaint.setTextSize(24);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setShadowLayer(1.0f, 1.0f, 1.0f, COLOR_TEXT_SHADOW);
        
        float y = 35;
        int lineHeight = 28;
        
        // **TITRE**
        textPaint.setTextSize(26);
        textPaint.setColor(Color.argb(255, 255, 255, 255));
        canvas.drawText("🎮 RetroArch Overlay", 20, y, textPaint);
        y += lineHeight + 5;
        
        // **INFORMATIONS**
        textPaint.setTextSize(20);
        textPaint.setColor(Color.argb(255, 200, 200, 200));
        canvas.drawText("📱 Canvas: " + canvas.getWidth() + "x" + canvas.getHeight(), 20, y, textPaint);
        y += lineHeight;
        
        // **ÉTAT DES BOUTONS**
        canvas.drawText("🎯 Boutons actifs:", 20, y, textPaint);
        y += lineHeight;
        
        Map<String, RetroArchInputSystem.TouchZone> zones = inputSystem.getTouchZones();
        for (RetroArchInputSystem.TouchZone zone : zones.values()) {
            if (zone.isPressed) {
                textPaint.setColor(Color.argb(255, 100, 255, 100));
                canvas.drawText("  ✅ " + zone.name + " (ID: " + zone.deviceId + ")", 40, y, textPaint);
                y += lineHeight;
            }
        }
        
        // **INSTRUCTIONS**
        y += 5;
        textPaint.setColor(Color.argb(255, 255, 255, 100));
        canvas.drawText("💡 Start + Select = Menu RetroArch", 20, y, textPaint);
        y += lineHeight;
        canvas.drawText("💡 Touchez les zones pour tester", 20, y, textPaint);
    }
    
    /**
     * **100% RETROARCH** : Définir le mode debug
     */
    public void setDebugMode(boolean enabled) {
        this.debugMode = enabled;
        Log.i(TAG, "🎮 Mode debug " + (enabled ? "activé" : "désactivé"));
    }
    
    /**
     * **100% RETROARCH** : Définir l'opacité
     */
    public void setOpacity(float opacity) {
        this.opacity = Math.max(0.0f, Math.min(1.0f, opacity));
        paint.setAlpha((int)(255 * this.opacity));
        Log.i(TAG, "🎮 Opacité définie à: " + this.opacity);
    }
    
    /**
     * **100% RETROARCH** : Obtenir l'opacité
     */
    public float getOpacity() {
        return opacity;
    }
    
    /**
     * **100% RETROARCH** : Debug des zones
     */
    public void debugZones() {
        Map<String, RetroArchInputSystem.TouchZone> zones = inputSystem.getTouchZones();
        Log.i(TAG, "🎮 === DEBUG ZONES OVERLAY AUTHENTIQUE ===");
        for (RetroArchInputSystem.TouchZone zone : zones.values()) {
            Log.i(TAG, "🎮 Zone: " + zone.name + 
                      " - Bounds: " + zone.bounds.toString() + 
                      " - Pressed: " + zone.isPressed + 
                      " - DeviceID: " + zone.deviceId);
        }
        Log.i(TAG, "🎮 === FIN DEBUG ===");
    }
}
