package com.fceumm.wrapper.input;

import android.graphics.RectF;
import android.content.Context;
import android.util.DisplayMetrics;

public class SimpleController {
    private RectF dPadUp;
    private RectF dPadDown;
    private RectF dPadLeft;
    private RectF dPadRight;
    private RectF buttonA;
    private RectF buttonB;
    private RectF buttonStart;
    private RectF buttonSelect;
    
    private int screenWidth;
    private int screenHeight;
    private float screenDensity;
    private boolean isLandscape;
    
    public SimpleController(Context context) {
        dPadUp = new RectF();
        dPadDown = new RectF();
        dPadLeft = new RectF();
        dPadRight = new RectF();
        buttonA = new RectF();
        buttonB = new RectF();
        buttonStart = new RectF();
        buttonSelect = new RectF();
        
        // Obtenir la densité d'écran pour l'adaptation
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        screenDensity = metrics.density;
    }
    
    public void updateLayout(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        this.isLandscape = width > height;
        
        // Utiliser les coordonnées originales mais avec des tailles adaptatives
        // Taille modérée de la croix directionnelle pour rester dans les limites de l'écran
        int dPadSize = (int)(140 * screenDensity); // Taille équilibrée (entre 120 et 180)
        int buttonSize = (int)(60 * screenDensity);
        
        if (isLandscape) {
            // Layout paysage - positions originales (pas de changement)
            int dPadX = 50;
            int dPadY = height - dPadSize - 50; // Position originale
            
            // Calcul des positions pour la croix directionnelle classique (zones séparées)
            int centerX = dPadX + dPadSize / 2;
            int centerY = dPadY + dPadSize / 2;
            int buttonWidth = dPadSize / 3; // Largeur de chaque bouton de direction
            
            // Positionnement classique avec zones séparées (forme de croix)
            dPadUp.set(centerX - buttonWidth/2, dPadY, centerX + buttonWidth/2, centerY - buttonWidth/2);
            dPadDown.set(centerX - buttonWidth/2, centerY + buttonWidth/2, centerX + buttonWidth/2, dPadY + dPadSize);
            dPadLeft.set(dPadX, centerY - buttonWidth/2, centerX - buttonWidth/2, centerY + buttonWidth/2);
            dPadRight.set(centerX + buttonWidth/2, centerY - buttonWidth/2, dPadX + dPadSize, centerY + buttonWidth/2);
            
            // Boutons A et B en diagonale - à droite de l'écran, basés sur la position de la croix
            int buttonAX = width - buttonSize - 50; // Position X du bouton A (à droite de l'écran)
            int buttonAY = dPadY; // Position Y du bouton A (même niveau que la croix)
            int buttonBX = buttonAX - buttonSize - 20; // Position X du bouton B (à gauche du bouton A)
            int buttonBY = buttonAY + buttonSize; // Position Y du bouton B (plus bas de 1x la taille)
            
            buttonA.set(buttonAX, buttonAY, buttonAX + buttonSize, buttonAY + buttonSize);
            buttonB.set(buttonBX, buttonBY, buttonBX + buttonSize, buttonBY + buttonSize);
            
            // Start et Select en haut - grossis de moitié et ordre inversé
            int startX = width / 2 - 120;
            int startY = 50;
            int startButtonWidth = 120; // Largeur augmentée de moitié (80 -> 120)
            int startButtonHeight = 60; // Hauteur augmentée de moitié (40 -> 60)
            
            buttonSelect.set(startX, startY, startX + startButtonWidth, startY + startButtonHeight);
            buttonStart.set(startX + startButtonWidth + 20, startY, startX + startButtonWidth + 20 + startButtonWidth, startY + startButtonHeight);
        } else {
            // Layout portrait - positions originales
            int dPadX = 50;
            int dPadY = height - dPadSize - 150; // Déplacé plus haut (était -50)
            
            // Calcul des positions pour la croix directionnelle classique (zones séparées)
            int centerX = dPadX + dPadSize / 2;
            int centerY = dPadY + dPadSize / 2;
            int buttonWidth = dPadSize / 3; // Largeur de chaque bouton de direction
            
            // Positionnement classique avec zones séparées (forme de croix)
            dPadUp.set(centerX - buttonWidth/2, dPadY, centerX + buttonWidth/2, centerY - buttonWidth/2);
            dPadDown.set(centerX - buttonWidth/2, centerY + buttonWidth/2, centerX + buttonWidth/2, dPadY + dPadSize);
            dPadLeft.set(dPadX, centerY - buttonWidth/2, centerX - buttonWidth/2, centerY + buttonWidth/2);
            dPadRight.set(centerX + buttonWidth/2, centerY - buttonWidth/2, dPadX + dPadSize, centerY + buttonWidth/2);
            
            // Boutons A et B en diagonale - à droite de l'écran, basés sur la position de la croix
            int buttonAX = width - buttonSize - 50; // Position X du bouton A (à droite de l'écran)
            int buttonAY = dPadY; // Position Y du bouton A (même niveau que la croix)
            int buttonBX = buttonAX - buttonSize - 20; // Position X du bouton B (à gauche du bouton A)
            int buttonBY = buttonAY + buttonSize; // Position Y du bouton B (plus bas de 1x la taille)
            
            buttonA.set(buttonAX, buttonAY, buttonAX + buttonSize, buttonAY + buttonSize);
            buttonB.set(buttonBX, buttonBY, buttonBX + buttonSize, buttonBY + buttonSize);
            
            // Start et Select en bas - grossis de moitié et ordre inversé
            int startX = width / 2 - 120;
            int startY = height - 80;
            int startButtonWidth = 120; // Largeur augmentée de moitié (80 -> 120)
            int startButtonHeight = 60; // Hauteur augmentée de moitié (40 -> 60)
            
            buttonSelect.set(startX, startY, startX + startButtonWidth, startY + startButtonHeight);
            buttonStart.set(startX + startButtonWidth + 20, startY, startX + startButtonWidth + 20 + startButtonWidth, startY + startButtonHeight);
        }
    }
    
    public int getButtonAtPosition(float x, float y) {
        // Tolérance étendue UNIQUEMENT pour les directions
        float directionTolerance = 20 * screenDensity; // Pour les directions (0-3)
        float actionTolerance = 10 * screenDensity; // Pour A, B, START, SELECT (4-7)
        
        // Vérifier d'abord les boutons de direction avec tolérance étendue
        if (dPadUp.contains(x, y) || isNearRect(dPadUp, x, y, directionTolerance)) return 0; // UP
        if (dPadDown.contains(x, y) || isNearRect(dPadDown, x, y, directionTolerance)) return 1; // DOWN
        if (dPadLeft.contains(x, y) || isNearRect(dPadLeft, x, y, directionTolerance)) return 2; // LEFT
        if (dPadRight.contains(x, y) || isNearRect(dPadRight, x, y, directionTolerance)) return 3; // RIGHT
        
        // Vérifier les boutons d'action avec tolérance normale
        if (buttonA.contains(x, y) || isNearRect(buttonA, x, y, actionTolerance)) return 4; // A
        if (buttonB.contains(x, y) || isNearRect(buttonB, x, y, actionTolerance)) return 5; // B
        if (buttonStart.contains(x, y) || isNearRect(buttonStart, x, y, actionTolerance)) return 6; // START
        if (buttonSelect.contains(x, y) || isNearRect(buttonSelect, x, y, actionTolerance)) return 7; // SELECT
        
        // Si aucun bouton n'est détecté, vérifier la zone centrale du D-pad pour les directions
        float centerX = dPadLeft.right; // Centre horizontal
        float centerY = dPadUp.bottom; // Centre vertical
        float dPadRadius = (dPadRight.left - dPadLeft.right) / 2; // Rayon du D-pad
        
        // Si le toucher est dans la zone centrale du D-pad
        if (Math.abs(x - centerX) < dPadRadius && Math.abs(y - centerY) < dPadRadius) {
            // Déterminer la direction dominante basée sur la position relative
            boolean upPressed = y < centerY;
            boolean downPressed = y > centerY;
            boolean leftPressed = x < centerX;
            boolean rightPressed = x > centerX;
            
            // Retourner la direction dominante
            if (upPressed && rightPressed) return 0; // UP (priorité)
            if (upPressed && leftPressed) return 0; // UP (priorité)
            if (downPressed && rightPressed) return 1; // DOWN (priorité)
            if (downPressed && leftPressed) return 1; // DOWN (priorité)
            if (upPressed) return 0; // UP
            if (downPressed) return 1; // DOWN
            if (leftPressed) return 2; // LEFT
            if (rightPressed) return 3; // RIGHT
        }
        
        return -1;
    }
    
    // Méthode optimisée pour détecter les diagonales du D-pad
    private int detectDPadDiagonal(float x, float y) {
        // Avec le nouveau positionnement, les boutons se chevauchent naturellement
        // Pas besoin de calculs complexes, la détection normale suffit
        float tolerance = 10 * screenDensity;
        
        // Vérifier les boutons individuels avec tolérance
        if (dPadUp.contains(x, y) || isNearRect(dPadUp, x, y, tolerance)) return 0; // UP
        if (dPadDown.contains(x, y) || isNearRect(dPadDown, x, y, tolerance)) return 1; // DOWN
        if (dPadLeft.contains(x, y) || isNearRect(dPadLeft, x, y, tolerance)) return 2; // LEFT
        if (dPadRight.contains(x, y) || isNearRect(dPadRight, x, y, tolerance)) return 3; // RIGHT
        
        return -1;
    }
    
    // Méthode optimisée pour détecter les diagonales avec support multi-touch
    // UNIQUEMENT pour les boutons de direction (0-3), pas A et B (4-5)
    public int[] getDiagonalButtons(float x, float y) {
        // Augmenter la tolérance pour des transitions plus fluides
        float tolerance = 20 * screenDensity; // Doublé de 10 à 20
        
        // Utiliser un tableau pré-alloué au lieu d'une liste
        int[] tempButtons = new int[4]; // Maximum 4 boutons (UP, DOWN, LEFT, RIGHT)
        int buttonCount = 0;
        
        // Vérifier UNIQUEMENT les boutons de direction (0-3) avec tolérance étendue
        if (dPadUp.contains(x, y) || isNearRect(dPadUp, x, y, tolerance)) {
            tempButtons[buttonCount++] = 0; // UP
        }
        if (dPadDown.contains(x, y) || isNearRect(dPadDown, x, y, tolerance)) {
            tempButtons[buttonCount++] = 1; // DOWN
        }
        if (dPadLeft.contains(x, y) || isNearRect(dPadLeft, x, y, tolerance)) {
            tempButtons[buttonCount++] = 2; // LEFT
        }
        if (dPadRight.contains(x, y) || isNearRect(dPadRight, x, y, tolerance)) {
            tempButtons[buttonCount++] = 3; // RIGHT
        }
        
        // Si aucun bouton n'est détecté, vérifier la zone centrale du D-pad
        if (buttonCount == 0) {
            // Calculer le centre du D-pad
            float centerX = dPadLeft.right; // Centre horizontal
            float centerY = dPadUp.bottom; // Centre vertical
            float dPadRadius = (dPadRight.left - dPadLeft.right) / 2; // Rayon du D-pad
            
            // Si le toucher est dans la zone centrale, détecter la direction dominante
            if (Math.abs(x - centerX) < dPadRadius && Math.abs(y - centerY) < dPadRadius) {
                // Déterminer la direction basée sur la position relative
                boolean upPressed = y < centerY;
                boolean downPressed = y > centerY;
                boolean leftPressed = x < centerX;
                boolean rightPressed = x > centerX;
                
                // Ajouter les directions détectées
                if (upPressed) tempButtons[buttonCount++] = 0; // UP
                if (downPressed) tempButtons[buttonCount++] = 1; // DOWN
                if (leftPressed) tempButtons[buttonCount++] = 2; // LEFT
                if (rightPressed) tempButtons[buttonCount++] = 3; // RIGHT
            }
        }
        
        // Créer le tableau final avec la taille exacte
        int[] result = new int[buttonCount];
        System.arraycopy(tempButtons, 0, result, 0, buttonCount);
        return result;
    }
    
    // Nouvelle méthode pour détecter les boutons A et B normalement
    public int[] getActionButtons(float x, float y) {
        float tolerance = 10 * screenDensity; // Tolérance normale pour A et B
        
        // Utiliser un tableau pré-alloué pour A et B
        int[] tempButtons = new int[2]; // Maximum 2 boutons (A, B)
        int buttonCount = 0;
        
        // Vérifier UNIQUEMENT les boutons d'action (4-5) avec tolérance normale
        if (buttonA.contains(x, y) || isNearRect(buttonA, x, y, tolerance)) {
            tempButtons[buttonCount++] = 4; // A
        }
        if (buttonB.contains(x, y) || isNearRect(buttonB, x, y, tolerance)) {
            tempButtons[buttonCount++] = 5; // B
        }
        
        // Créer le tableau final avec la taille exacte
        int[] result = new int[buttonCount];
        System.arraycopy(tempButtons, 0, result, 0, buttonCount);
        return result;
    }
    
    private boolean isNearRect(RectF rect, float x, float y, float tolerance) {
        return x >= rect.left - tolerance && x <= rect.right + tolerance &&
               y >= rect.top - tolerance && y <= rect.bottom + tolerance;
    }
    
    public RectF getDPadUp() { return dPadUp; }
    public RectF getDPadDown() { return dPadDown; }
    public RectF getDPadLeft() { return dPadLeft; }
    public RectF getDPadRight() { return dPadRight; }
    public RectF getButtonA() { return buttonA; }
    public RectF getButtonB() { return buttonB; }
    public RectF getButtonStart() { return buttonStart; }
    public RectF getButtonSelect() { return buttonSelect; }
    
    public boolean isLandscape() { return isLandscape; }
    public float getScreenDensity() { return screenDensity; }
} 