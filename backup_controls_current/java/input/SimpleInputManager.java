package com.fceumm.wrapper.input;

import android.view.MotionEvent;
import android.view.View;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class SimpleInputManager implements View.OnTouchListener {
    private static final String TAG = "SimpleInputManager";
    private SimpleController controller;
    private SimpleOverlay overlay;
    private boolean[] buttonStates = new boolean[8];
    private int[] activeTouches = new int[10]; // Support multi-touch
    @SuppressWarnings("unchecked")
    private List<Integer>[] activeButtonsPerPointer = new List[10]; // Boutons actifs par pointeur
    
    static {
        System.loadLibrary("fceummwrapper");
    }
    
    public SimpleInputManager(SimpleController controller) {
        this.controller = controller;
        // Initialiser les listes pour chaque pointeur
        for (int i = 0; i < 10; i++) {
            activeButtonsPerPointer[i] = new ArrayList<>();
        }
    }
    
    public void setOverlay(SimpleOverlay overlay) {
        this.overlay = overlay;
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                handleTouchDown(event, pointerIndex);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                handleTouchUp(event, pointerIndex);
                break;
            case MotionEvent.ACTION_MOVE:
                handleTouchMove(event);
                break;
            case MotionEvent.ACTION_CANCEL:
                handleTouchCancel();
                break;
        }
        
        return true;
    }
    
    private void handleTouchDown(MotionEvent event, int pointerIndex) {
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);
        int pointerId = event.getPointerId(pointerIndex);
        
        // Vérifier d'abord si c'est un bouton d'action (A, B, START, SELECT)
        int singleButton = controller.getButtonAtPosition(x, y);
        if (singleButton >= 4) { // A, B, START, SELECT
            // Traitement normal pour les boutons d'action
            if (singleButton >= 0 && singleButton < 8) {
                buttonStates[singleButton] = true;
                setButtonState(singleButton, true);
                activeTouches[pointerId] = singleButton;
                activeButtonsPerPointer[pointerId].clear();
                activeButtonsPerPointer[pointerId].add(singleButton);
                
                // Déclencher l'animation
                if (overlay != null) {
                    overlay.animateButtonPress(singleButton);
                }
                
                Log.d(TAG, "Bouton pressé: " + getButtonName(singleButton) + " (Pointer: " + pointerId + ")");
            }
        } else {
            // Pour les boutons de direction, utiliser la détection des diagonales
            int[] buttonIds = controller.getDiagonalButtons(x, y);
            
            if (buttonIds.length > 0) {
                // Nettoyer les boutons précédents pour ce pointeur
                activeButtonsPerPointer[pointerId].clear();
                
                // Activer tous les boutons détectés
                for (int buttonId : buttonIds) {
                    if (buttonId >= 0 && buttonId < 4) { // Seulement les directions (0-3)
                        buttonStates[buttonId] = true;
                        setButtonState(buttonId, true);
                        activeButtonsPerPointer[pointerId].add(buttonId);
                        
                        // Déclencher l'animation
                        if (overlay != null) {
                            overlay.animateButtonPress(buttonId);
                        }
                        
                        Log.d(TAG, "Bouton pressé: " + getButtonName(buttonId) + " (Pointer: " + pointerId + ")");
                    }
                }
                
                // Stocker le premier bouton comme bouton principal pour ce pointeur
                activeTouches[pointerId] = buttonIds[0];
            } else {
                // Fallback vers l'ancienne méthode pour les autres boutons
                if (singleButton >= 0 && singleButton < 8) {
                    buttonStates[singleButton] = true;
                    setButtonState(singleButton, true);
                    activeTouches[pointerId] = singleButton;
                    activeButtonsPerPointer[pointerId].clear();
                    activeButtonsPerPointer[pointerId].add(singleButton);
                    
                    // Déclencher l'animation
                    if (overlay != null) {
                        overlay.animateButtonPress(singleButton);
                    }
                    
                    Log.d(TAG, "Bouton pressé: " + getButtonName(singleButton) + " (Pointer: " + pointerId + ")");
                }
            }
        }
    }
    
    private void handleTouchUp(MotionEvent event, int pointerIndex) {
        int pointerId = event.getPointerId(pointerIndex);
        
        // Relâcher tous les boutons associés à ce pointeur
        List<Integer> buttonsToRelease = activeButtonsPerPointer[pointerId];
        for (int buttonId : buttonsToRelease) {
            if (buttonId >= 0 && buttonId < 8) {
                // Vérifier si ce bouton est encore pressé par d'autres doigts
                boolean stillPressed = false;
                for (int i = 0; i < activeButtonsPerPointer.length; i++) {
                    if (i != pointerId && activeButtonsPerPointer[i].contains(buttonId)) {
                        stillPressed = true;
                        break;
                    }
                }
                
                // Si aucun autre doigt ne presse ce bouton, le relâcher
                if (!stillPressed) {
                    buttonStates[buttonId] = false;
                    setButtonState(buttonId, false);
                    Log.d(TAG, "Bouton relâché: " + getButtonName(buttonId) + " (Pointer: " + pointerId + ")");
                }
            }
        }
        
        // Nettoyer les listes pour ce pointeur
        activeButtonsPerPointer[pointerId].clear();
        activeTouches[pointerId] = -1;
    }
    
    private void handleTouchMove(MotionEvent event) {
        // Vérifier tous les pointeurs actifs
        for (int i = 0; i < event.getPointerCount(); i++) {
            int pointerId = event.getPointerId(i);
            float x = event.getX(i);
            float y = event.getY(i);
            
            // Vérifier d'abord si c'est un bouton d'action (A, B, START, SELECT)
            int singleButton = controller.getButtonAtPosition(x, y);
            if (singleButton >= 4) { // A, B, START, SELECT
                // Traitement normal pour les boutons d'action
                int previousButton = activeTouches[pointerId];
                if (singleButton != previousButton) {
                    // Relâcher l'ancien bouton
                    if (previousButton >= 0 && previousButton < 8) {
                        buttonStates[previousButton] = false;
                        setButtonState(previousButton, false);
                        Log.d(TAG, "Bouton relâché (move): " + getButtonName(previousButton));
                    }
                    
                    // Presser le nouveau bouton
                    if (singleButton >= 0 && singleButton < 8) {
                        buttonStates[singleButton] = true;
                        setButtonState(singleButton, true);
                        activeTouches[pointerId] = singleButton;
                        activeButtonsPerPointer[pointerId].clear();
                        activeButtonsPerPointer[pointerId].add(singleButton);
                        
                        // Déclencher l'animation
                        if (overlay != null) {
                            overlay.animateButtonPress(singleButton);
                        }
                        
                        Log.d(TAG, "Bouton pressé (move): " + getButtonName(singleButton));
                    } else {
                        activeTouches[pointerId] = -1;
                    }
                }
            } else {
                // Pour les boutons de direction, utiliser la détection des diagonales
                int[] currentButtons = controller.getDiagonalButtons(x, y);
                List<Integer> previousButtons = activeButtonsPerPointer[pointerId];
                
                // Vérifier si les boutons ont changé
                boolean buttonsChanged = false;
                if (currentButtons.length != previousButtons.size()) {
                    buttonsChanged = true;
                } else {
                    // Vérifier si les boutons sont les mêmes
                    for (int j = 0; j < currentButtons.length; j++) {
                        if (j >= previousButtons.size() || currentButtons[j] != previousButtons.get(j)) {
                            buttonsChanged = true;
                            break;
                        }
                    }
                }
                
                if (buttonsChanged) {
                    // Relâcher tous les anciens boutons
                    for (int buttonId : previousButtons) {
                        if (buttonId >= 0 && buttonId < 4) { // Seulement les directions (0-3)
                            // Vérifier si ce bouton est encore pressé par d'autres doigts
                            boolean stillPressed = false;
                            for (int j = 0; j < activeButtonsPerPointer.length; j++) {
                                if (j != pointerId && activeButtonsPerPointer[j].contains(buttonId)) {
                                    stillPressed = true;
                                    break;
                                }
                            }
                            
                            if (!stillPressed) {
                                buttonStates[buttonId] = false;
                                setButtonState(buttonId, false);
                                Log.d(TAG, "Bouton relâché (move): " + getButtonName(buttonId));
                            }
                        }
                    }
                    
                    // Nettoyer et ajouter les nouveaux boutons
                    previousButtons.clear();
                    if (currentButtons.length > 0) {
                        for (int buttonId : currentButtons) {
                            if (buttonId >= 0 && buttonId < 4) { // Seulement les directions (0-3)
                                buttonStates[buttonId] = true;
                                setButtonState(buttonId, true);
                                previousButtons.add(buttonId);
                                
                                // Déclencher l'animation
                                if (overlay != null) {
                                    overlay.animateButtonPress(buttonId);
                                }
                                
                                Log.d(TAG, "Bouton pressé (move): " + getButtonName(buttonId));
                            }
                        }
                        activeTouches[pointerId] = currentButtons[0]; // Stocker le premier comme principal
                    } else {
                        activeTouches[pointerId] = -1;
                    }
                }
            }
        }
    }
    
    private void handleTouchCancel() {
        // Relâcher tous les boutons
        for (int i = 0; i < 8; i++) {
            if (buttonStates[i]) {
                buttonStates[i] = false;
                setButtonState(i, false);
                Log.d(TAG, "Bouton relâché (cancel): " + getButtonName(i));
            }
        }
        
        // Réinitialiser les touches actives
        for (int i = 0; i < activeTouches.length; i++) {
            activeTouches[i] = -1;
            activeButtonsPerPointer[i].clear();
        }
    }
    
    public void resetAllButtons() {
        for (int i = 0; i < 8; i++) {
            buttonStates[i] = false;
        }
        
        // Réinitialiser les touches actives
        for (int i = 0; i < activeTouches.length; i++) {
            activeTouches[i] = -1;
            activeButtonsPerPointer[i].clear();
        }
        
        resetAllButtonsNative();
        Log.d(TAG, "Tous les boutons réinitialisés");
    }
    
    private String getButtonName(int buttonId) {
        switch (buttonId) {
            case 0: return "UP";
            case 1: return "DOWN";
            case 2: return "LEFT";
            case 3: return "RIGHT";
            case 4: return "A";
            case 5: return "B";
            case 6: return "START";
            case 7: return "SELECT";
            default: return "UNKNOWN";
        }
    }
    
    // Méthodes natives
    private native void setButtonState(int buttonId, boolean pressed);
    private native void resetAllButtonsNative();
} 