package com.fceumm.wrapper.input;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.animation.ValueAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.util.Log;
import androidx.core.content.ContextCompat;

public class SimpleOverlay extends View {
    private static final String TAG = "SimpleOverlay";
    private SimpleController controller;
    private SimpleInputManager inputManager;
    private Paint paint;
    private Paint textPaint;
    
    // Drawables pour les boutons
    private Drawable dPadUpDrawable;
    private Drawable dPadDownDrawable;
    private Drawable dPadLeftDrawable;
    private Drawable dPadRightDrawable;
    private Drawable buttonADrawable;
    private Drawable buttonBDrawable;
    private Drawable startButtonDrawable;
    private Drawable selectButtonDrawable;
    
    // États des boutons pour les animations
    private boolean[] buttonPressed = new boolean[8];
    private ValueAnimator[] buttonAnimators = new ValueAnimator[8];
    
    public SimpleOverlay(Context context) {
        super(context);
        init();
    }
    
    public SimpleOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        controller = new SimpleController(getContext());
        inputManager = new SimpleInputManager(controller);
        inputManager.setOverlay(this);
        setOnTouchListener(inputManager);
        
        // Paint principal pour les boutons
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        
        // Paint pour le texte
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(20);
        
        // Charger les drawables pour les boutons
        loadButtonDrawables();
        
        // Initialiser les animations
        for (int i = 0; i < 8; i++) {
            final int buttonIndex = i;
            buttonAnimators[i] = ValueAnimator.ofFloat(0f, 1f);
            buttonAnimators[i].setDuration(150);
            buttonAnimators[i].setInterpolator(new AccelerateDecelerateInterpolator());
            buttonAnimators[i].addUpdateListener(animation -> {
                buttonPressed[buttonIndex] = animation.getAnimatedFraction() > 0.5f;
                invalidate();
            });
        }
    }
    
    private void loadButtonDrawables() {
        try {
            // Charger les drawables pour la croix directionnelle
            dPadUpDrawable = ContextCompat.getDrawable(getContext(), getResources().getIdentifier("btn_dpad_up_selector", "drawable", getContext().getPackageName()));
            dPadDownDrawable = ContextCompat.getDrawable(getContext(), getResources().getIdentifier("btn_dpad_down_selector", "drawable", getContext().getPackageName()));
            dPadLeftDrawable = ContextCompat.getDrawable(getContext(), getResources().getIdentifier("btn_dpad_left_selector", "drawable", getContext().getPackageName()));
            dPadRightDrawable = ContextCompat.getDrawable(getContext(), getResources().getIdentifier("btn_dpad_right_selector", "drawable", getContext().getPackageName()));
            
            // Charger les drawables pour les boutons d'action
            buttonADrawable = ContextCompat.getDrawable(getContext(), getResources().getIdentifier("nintendo_button_a", "drawable", getContext().getPackageName()));
            buttonBDrawable = ContextCompat.getDrawable(getContext(), getResources().getIdentifier("btn_round_b_selector", "drawable", getContext().getPackageName()));
            
            // Charger les drawables pour les boutons système
            startButtonDrawable = ContextCompat.getDrawable(getContext(), getResources().getIdentifier("blank_button", "drawable", getContext().getPackageName()));
            selectButtonDrawable = ContextCompat.getDrawable(getContext(), getResources().getIdentifier("blank_button", "drawable", getContext().getPackageName()));
        } catch (Exception e) {
            // En cas d'erreur, on garde les drawables null et on utilisera le dessin par défaut
            Log.w(TAG, "Erreur lors du chargement des drawables: " + e.getMessage());
        }
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        controller.updateLayout(w, h);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Dessiner D-Pad avec les drawables
        drawButtonWithDrawable(canvas, dPadUpDrawable, controller.getDPadUp());
        drawButtonWithDrawable(canvas, dPadDownDrawable, controller.getDPadDown());
        drawButtonWithDrawable(canvas, dPadLeftDrawable, controller.getDPadLeft());
        drawButtonWithDrawable(canvas, dPadRightDrawable, controller.getDPadRight());
        
        // Dessiner boutons A et B avec les drawables
        drawButtonWithDrawable(canvas, buttonADrawable, controller.getButtonA());
        drawButtonWithDrawable(canvas, buttonBDrawable, controller.getButtonB());
        
        // Dessiner Start et Select avec les drawables
        drawButtonWithDrawable(canvas, startButtonDrawable, controller.getButtonStart());
        drawButtonWithDrawable(canvas, selectButtonDrawable, controller.getButtonSelect());
        
        // Dessiner les labels pour Start et Select
        textPaint.setTextSize(16);
        textPaint.setColor(Color.WHITE);
        textPaint.setStyle(Paint.Style.FILL);
        
        canvas.drawText("START", controller.getButtonStart().centerX(), controller.getButtonStart().centerY() + 5, textPaint);
        canvas.drawText("SELECT", controller.getButtonSelect().centerX(), controller.getButtonSelect().centerY() + 5, textPaint);
    }
    
    private void drawButtonWithDrawable(Canvas canvas, Drawable drawable, RectF bounds) {
        if (drawable != null) {
            drawable.setBounds((int)bounds.left, (int)bounds.top, (int)bounds.right, (int)bounds.bottom);
            drawable.draw(canvas);
        } else {
            // Fallback : dessiner un rectangle simple si le drawable n'est pas disponible
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(150, 100, 100, 100));
            canvas.drawRect(bounds, paint);
            
            // Bordure
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            paint.setColor(Color.WHITE);
            canvas.drawRect(bounds, paint);
        }
    }
    
    // Méthode pour déclencher l'animation d'un bouton
    public void animateButtonPress(int buttonIndex) {
        if (buttonIndex >= 0 && buttonIndex < 8) {
            if (buttonAnimators[buttonIndex].isRunning()) {
                buttonAnimators[buttonIndex].cancel();
            }
            buttonAnimators[buttonIndex].start();
        }
    }
    
    public SimpleInputManager getInputManager() {
        return inputManager;
    }
} 