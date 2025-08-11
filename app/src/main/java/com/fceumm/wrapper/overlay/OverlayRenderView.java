package com.fceumm.wrapper.overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.fceumm.wrapper.input.RetroArchInputSystem;
import com.fceumm.wrapper.overlay.RetroArchOverlayRenderer;

public class OverlayRenderView extends View {
    private static final String TAG = "OverlayRenderView";
    
    private RetroArchOverlaySystem overlaySystem;
    private RetroArchInputSystem inputSystem;
    private RetroArchOverlayRenderer overlayRenderer;
    private Paint paint;
    
    // **OPTIMISATION LOGS** : Variables pour réduire la fréquence des logs
    private long lastDebugLogTime = 0;
    private static final long DEBUG_LOG_INTERVAL = 5000; // 5 secondes entre les logs
    private int drawCount = 0;
    
    public OverlayRenderView(Context context) {
        super(context);
        init();
    }
    
    public OverlayRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public OverlayRenderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        Log.d(TAG, "Initialisation de OverlayRenderView");
        paint = new Paint();
        paint.setAntiAlias(true);
        
        // Permettre les événements tactiles
        setClickable(true);
        setFocusable(true);
    }
    
    public void setOverlaySystem(RetroArchOverlaySystem overlaySystem) {
        this.overlaySystem = overlaySystem;
        Log.d(TAG, "Système d'overlay assigné à la vue");
    }
    
    public void setInputSystem(RetroArchInputSystem inputSystem) {
        this.inputSystem = inputSystem;
        this.overlayRenderer = new RetroArchOverlayRenderer(getContext(), inputSystem);
        Log.d(TAG, "Nouveau système d'input assigné à la vue");
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // **OPTIMISATION LOGS** : Log périodique au lieu de 60 FPS
        long currentTime = System.currentTimeMillis();
        drawCount++;
        
        if (currentTime - lastDebugLogTime > DEBUG_LOG_INTERVAL) {
            Log.d(TAG, "🎨 onDraw - View: " + getWidth() + "x" + getHeight() + 
                  " - Canvas: " + canvas.getWidth() + "x" + canvas.getHeight() +
                  " - Parent: " + (getParent() != null ? getParent().getClass().getSimpleName() : "null") +
                  " - Draws depuis dernier log: " + drawCount);
            lastDebugLogTime = currentTime;
            drawCount = 0;
        }
        
        // **NOUVEAU** : Utiliser le nouveau système de rendu
        if (overlayRenderer != null) {
            overlayRenderer.render(canvas);
        } else if (overlaySystem != null) {
            overlaySystem.render(canvas);
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // **NOUVEAU** : Utiliser le nouveau système d'input
        if (inputSystem != null) {
            Log.i(TAG, "🎯 onTouchEvent - action=" + event.getActionMasked() + ", coordonnées locales: (" + event.getX() + ", " + event.getY() + ")");
            
            boolean handled = inputSystem.handleTouchEvent(event);
            if (handled) {
                Log.d(TAG, "✅ Événement tactile géré par le nouveau système d'input");
                invalidate(); // Redessiner après traitement
                return true;
            } else {
                Log.d(TAG, "❌ Événement tactile non géré par le nouveau système d'input");
            }
        } else if (overlaySystem != null) {
            // **FALLBACK** : Ancien système
            Log.i(TAG, "🎯 onTouchEvent - action=" + event.getActionMasked() + ", coordonnées locales: (" + event.getX() + ", " + event.getY() + ")");
            
            boolean handled = overlaySystem.handleTouch(event);
            if (handled) {
                Log.d(TAG, "✅ Événement tactile géré par l'ancien système d'overlay");
                invalidate(); // Redessiner après traitement
                return true;
            } else {
                Log.d(TAG, "❌ Événement tactile non géré par l'ancien système d'overlay");
            }
        }
        
        return super.onTouchEvent(event);
    }
    
    private MotionEvent translateTouchCoordinates(MotionEvent event) {
        // Obtenir la position de cette vue dans les coordonnées de l'écran
        int[] location = new int[2];
        getLocationOnScreen(location);
        
        // Créer un nouvel événement avec les coordonnées traduites
        MotionEvent.PointerProperties[] properties = new MotionEvent.PointerProperties[event.getPointerCount()];
        MotionEvent.PointerCoords[] coords = new MotionEvent.PointerCoords[event.getPointerCount()];
        
        for (int i = 0; i < event.getPointerCount(); i++) {
            properties[i] = new MotionEvent.PointerProperties();
            event.getPointerProperties(i, properties[i]);
            
            coords[i] = new MotionEvent.PointerCoords();
            event.getPointerCoords(i, coords[i]);
            
            // Traduire les coordonnées
            coords[i].x += location[0];
            coords[i].y += location[1];
            
            Log.d(TAG, "Traduction coordonnées pointer " + i + ": local(" + event.getX(i) + "," + event.getY(i) + ") -> absolu(" + coords[i].x + "," + coords[i].y + ")");
        }
        
        return MotionEvent.obtain(
            event.getDownTime(),
            event.getEventTime(),
            event.getAction(),
            event.getPointerCount(),
            properties,
            coords,
            event.getMetaState(),
            event.getButtonState(),
            event.getXPrecision(),
            event.getYPrecision(),
            event.getDeviceId(),
            event.getEdgeFlags(),
            event.getSource(),
            event.getFlags()
        );
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d(TAG, "Taille changée: " + w + "x" + h + " (était: " + oldw + "x" + oldh + ")");
        
        if (overlaySystem != null) {
            overlaySystem.updateScreenDimensions(w, h);
        }
    }
} 