package com.fceumm.wrapper;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import com.fceumm.wrapper.overlay.RetroArchOverlaySystem;
// TEMPORAIREMENT DÉSACTIVÉ - import com.fceumm.wrapper.overlay.RetroArchOverlayLoader;
import android.content.Intent;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "🚀 MainActivity.onCreate() - Lancement de l'émulation via EmulationActivity");
        
        // Récupérer la ROM sélectionnée depuis l'Intent
        String selectedRom = getIntent().getStringExtra("selected_rom");
        Log.i(TAG, "ROM sélectionnée: " + (selectedRom != null ? selectedRom : "ROM par défaut (marioduckhunt.nes)"));
        
        // Lancer EmulationActivity avec la ROM sélectionnée
        Intent emulationIntent = new Intent(this, EmulationActivity.class);
        if (selectedRom != null) {
            emulationIntent.putExtra("selected_rom", selectedRom);
        }
        startActivity(emulationIntent);
        finish(); // Fermer MainActivity
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "MainActivity détruite");
    }
} 