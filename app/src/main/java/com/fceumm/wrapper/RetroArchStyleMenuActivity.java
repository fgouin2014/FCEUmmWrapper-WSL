package com.fceumm.wrapper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.Toast;
import android.content.Intent;

import com.fceumm.wrapper.overlay.RetroArchOverlaySystem;

/**
 * Menu style RetroArch authentique
 * Interface utilisateur 100% compatible avec RetroArch
 */
public class RetroArchStyleMenuActivity extends Activity {
    private static final String TAG = "RetroArchStyleMenu";
    private static final String PREF_NAME = "FCEUmmRetroArchMenu";
    
    private SharedPreferences preferences;
    private RetroArchOverlaySystem overlaySystem;
    
    // Navigation RetroArch
    private String currentMenu = "main";
    private String[] menuHistory = new String[10];
    private int menuHistoryIndex = 0;
    
    // Interface RetroArch
    private ListView menuListView;
    private TextView menuTitle;
    private TextView menuSubtitle;
    private LinearLayout menuContainer;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialiser les préférences
        preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        
        // Initialiser le système d'overlay
        overlaySystem = RetroArchOverlaySystem.getInstance(this);
        
        // Créer l'interface RetroArch authentique
        createRetroArchStyleInterface();
        
        // Afficher le menu principal
        showMainMenu();
        
        Log.i(TAG, "🎮 Menu RetroArch authentique créé");
    }
    
    /**
     * Créer l'interface style RetroArch authentique
     */
    private void createRetroArchStyleInterface() {
        // Layout principal style RetroArch
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.parseColor("#000000")); // Noir RetroArch
        mainLayout.setPadding(0, 0, 0, 0);
        
        // Header RetroArch
        LinearLayout headerLayout = new LinearLayout(this);
        headerLayout.setOrientation(LinearLayout.VERTICAL);
        headerLayout.setBackgroundColor(Color.parseColor("#1a1a1a"));
        headerLayout.setPadding(20, 20, 20, 20);
        
        // Titre principal style RetroArch
        menuTitle = new TextView(this);
        menuTitle.setText("RETROARCH");
        menuTitle.setTextColor(Color.parseColor("#00ff00")); // Vert RetroArch
        menuTitle.setTextSize(28);
        menuTitle.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        menuTitle.setGravity(Gravity.CENTER);
        headerLayout.addView(menuTitle);
        
        // Sous-titre
        menuSubtitle = new TextView(this);
        menuSubtitle.setText("Configuration des effets visuels");
        menuSubtitle.setTextColor(Color.parseColor("#cccccc"));
        menuSubtitle.setTextSize(14);
        menuSubtitle.setGravity(Gravity.CENTER);
        menuSubtitle.setPadding(0, 10, 0, 0);
        headerLayout.addView(menuSubtitle);
        
        mainLayout.addView(headerLayout);
        
        // Container du menu
        menuContainer = new LinearLayout(this);
        menuContainer.setOrientation(LinearLayout.VERTICAL);
        menuContainer.setBackgroundColor(Color.parseColor("#000000"));
        menuContainer.setPadding(20, 20, 20, 20);
        
        // ListView pour le menu style RetroArch
        menuListView = new ListView(this);
        menuListView.setBackgroundColor(Color.parseColor("#000000"));
        menuListView.setDividerHeight(1);
        // Créer un drawable pour le séparateur
        android.graphics.drawable.ColorDrawable dividerDrawable = new android.graphics.drawable.ColorDrawable(Color.parseColor("#333333"));
        menuListView.setDivider(dividerDrawable);
        menuListView.setPadding(0, 0, 0, 0);
        
        // Style des items RetroArch
        menuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                handleMenuSelection(selectedItem);
            }
        });
        
        menuContainer.addView(menuListView);
        mainLayout.addView(menuContainer);
        
        setContentView(mainLayout);
    }
    
    /**
     * Afficher le menu principal RetroArch
     */
    private void showMainMenu() {
        currentMenu = "main";
        addToMenuHistory("main");
        
        menuTitle.setText("RETROARCH");
        menuSubtitle.setText("Menu principal");
        
        String[] mainMenuItems = {
            "📺 Effets visuels",
            "🎮 Configuration overlays", 
            "⚙️ Paramètres généraux",
            "🔧 Outils de debug",
            "ℹ️ À propos",
            "❌ Quitter"
        };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_list_item_1, mainMenuItems) {
            @Override
            public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                android.view.View view = super.getView(position, convertView, parent);
                if (view instanceof TextView) {
                    TextView textView = (TextView) view;
                    textView.setTextColor(Color.parseColor("#00ff00")); // Vert RetroArch
                    textView.setTextSize(18);
                    textView.setPadding(20, 15, 20, 15);
                    textView.setBackgroundColor(Color.parseColor("#000000"));
                }
                return view;
            }
        };
        
        menuListView.setAdapter(adapter);
    }
    
    /**
     * Afficher le menu des effets visuels
     */
    private void showVisualEffectsMenu() {
        currentMenu = "visual_effects";
        addToMenuHistory("visual_effects");
        
        menuTitle.setText("EFFETS VISUELS");
        menuSubtitle.setText("Configuration des effets RetroArch");
        
        String[] effectsMenuItems = {
            "📺 Scanlines",
            "🔲 Patterns", 
            "🖥️ CRT Bezels",
            "💡 Phosphors",
            "🎨 Opacité",
            "✅ Appliquer",
            "🔄 Réinitialiser",
            "⬅️ Retour"
        };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_list_item_1, effectsMenuItems) {
            @Override
            public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                android.view.View view = super.getView(position, convertView, parent);
                if (view instanceof TextView) {
                    TextView textView = (TextView) view;
                    textView.setTextColor(Color.parseColor("#00ff00"));
                    textView.setTextSize(18);
                    textView.setPadding(20, 15, 20, 15);
                    textView.setBackgroundColor(Color.parseColor("#000000"));
                }
                return view;
            }
        };
        
        menuListView.setAdapter(adapter);
    }
    
    /**
     * Afficher le sous-menu scanlines
     */
    private void showScanlinesMenu() {
        currentMenu = "scanlines";
        addToMenuHistory("scanlines");
        
        menuTitle.setText("SCANLINES");
        menuSubtitle.setText("Effets CRT avec lignes horizontales");
        
        String[] scanlinesItems = {
            "✅ mame-phosphors-3x.cfg",
            "✅ aperture-grille-3x.cfg",
            "✅ crt-royale-scanlines-vertical-interlacing.cfg",
            "⬅️ Retour"
        };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_list_item_1, scanlinesItems) {
            @Override
            public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                android.view.View view = super.getView(position, convertView, parent);
                if (view instanceof TextView) {
                    TextView textView = (TextView) view;
                    textView.setTextColor(Color.parseColor("#00ff00"));
                    textView.setTextSize(18);
                    textView.setPadding(20, 15, 20, 15);
                    textView.setBackgroundColor(Color.parseColor("#000000"));
                }
                return view;
            }
        };
        
        menuListView.setAdapter(adapter);
    }
    
    /**
     * Afficher le sous-menu patterns
     */
    private void showPatternsMenu() {
        currentMenu = "patterns";
        addToMenuHistory("patterns");
        
        menuTitle.setText("PATTERNS");
        menuSubtitle.setText("Grilles et motifs visuels");
        
        String[] patternsItems = {
            "✅ checker.cfg (damier)",
            "✅ grid.cfg (grille)",
            "✅ lines.cfg (lignes)",
            "✅ trellis.cfg (treillis)",
            "⬅️ Retour"
        };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_list_item_1, patternsItems) {
            @Override
            public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                android.view.View view = super.getView(position, convertView, parent);
                if (view instanceof TextView) {
                    TextView textView = (TextView) view;
                    textView.setTextColor(Color.parseColor("#00ff00"));
                    textView.setTextSize(18);
                    textView.setPadding(20, 15, 20, 15);
                    textView.setBackgroundColor(Color.parseColor("#000000"));
                }
                return view;
            }
        };
        
        menuListView.setAdapter(adapter);
    }
    
    /**
     * Afficher le sous-menu CRT Bezels
     */
    private void showCRTBezelsMenu() {
        currentMenu = "crt_bezels";
        addToMenuHistory("crt_bezels");
        
        menuTitle.setText("CRT BEZELS");
        menuSubtitle.setText("Bordures d'écran CRT");
        
        String[] bezelsItems = {
            "✅ horizontal.cfg",
            "✅ vertical.cfg",
            "⬅️ Retour"
        };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_list_item_1, bezelsItems) {
            @Override
            public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                android.view.View view = super.getView(position, convertView, parent);
                if (view instanceof TextView) {
                    TextView textView = (TextView) view;
                    textView.setTextColor(Color.parseColor("#00ff00"));
                    textView.setTextSize(18);
                    textView.setPadding(20, 15, 20, 15);
                    textView.setBackgroundColor(Color.parseColor("#000000"));
                }
                return view;
            }
        };
        
        menuListView.setAdapter(adapter);
    }
    
    /**
     * Afficher le sous-menu Phosphors
     */
    private void showPhosphorsMenu() {
        currentMenu = "phosphors";
        addToMenuHistory("phosphors");
        
        menuTitle.setText("PHOSPHORS");
        menuSubtitle.setText("Effet phosphorescent");
        
        String[] phosphorsItems = {
            "✅ phosphors.cfg",
            "✅ mame-phosphors-3x.cfg",
            "⬅️ Retour"
        };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_list_item_1, phosphorsItems) {
            @Override
            public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                android.view.View view = super.getView(position, convertView, parent);
                if (view instanceof TextView) {
                    TextView textView = (TextView) view;
                    textView.setTextColor(Color.parseColor("#00ff00"));
                    textView.setTextSize(18);
                    textView.setPadding(20, 15, 20, 15);
                    textView.setBackgroundColor(Color.parseColor("#000000"));
                }
                return view;
            }
        };
        
        menuListView.setAdapter(adapter);
    }
    
    /**
     * Gérer la sélection dans le menu
     */
    private void handleMenuSelection(String selectedItem) {
        Log.i(TAG, "Sélection menu: " + selectedItem);
        
        switch (currentMenu) {
            case "main":
                handleMainMenuSelection(selectedItem);
                break;
            case "visual_effects":
                handleVisualEffectsSelection(selectedItem);
                break;
            case "scanlines":
                handleScanlinesSelection(selectedItem);
                break;
            case "patterns":
                handlePatternsSelection(selectedItem);
                break;
            case "crt_bezels":
                handleCRTBezelsSelection(selectedItem);
                break;
            case "phosphors":
                handlePhosphorsSelection(selectedItem);
                break;
        }
    }
    
    /**
     * Gérer la sélection du menu principal
     */
    private void handleMainMenuSelection(String selectedItem) {
        switch (selectedItem) {
            case "📺 Effets visuels":
                showVisualEffectsMenu();
                break;
            case "🎮 Configuration overlays":
                // Lancer l'activité d'intégration des overlays
                Intent intent = new Intent(this, OverlayIntegrationActivity.class);
                startActivity(intent);
                break;
            case "⚙️ Paramètres généraux":
                // Lancer l'activité des paramètres
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case "🔧 Outils de debug":
                // Lancer les outils de debug
                overlaySystem.debugVisualEffects();
                Toast.makeText(this, "🔧 Debug activé - Vérifiez les logs", Toast.LENGTH_SHORT).show();
                break;
            case "ℹ️ À propos":
                // Lancer l'activité à propos
                Intent aboutIntent = new Intent(this, AboutActivity.class);
                startActivity(aboutIntent);
                break;
            case "❌ Quitter":
                finish();
                break;
        }
    }
    
    /**
     * Gérer la sélection du menu effets visuels
     */
    private void handleVisualEffectsSelection(String selectedItem) {
        switch (selectedItem) {
            case "📺 Scanlines":
                showScanlinesMenu();
                break;
            case "🔲 Patterns":
                showPatternsMenu();
                break;
            case "🖥️ CRT Bezels":
                showCRTBezelsMenu();
                break;
            case "💡 Phosphors":
                showPhosphorsMenu();
                break;
            case "🎨 Opacité":
                // Afficher un dialogue pour l'opacité
                showOpacityDialog();
                break;
            case "✅ Appliquer":
                applyCurrentSettings();
                break;
            case "🔄 Réinitialiser":
                resetAllSettings();
                break;
            case "⬅️ Retour":
                goBack();
                break;
        }
    }
    
    /**
     * Gérer la sélection des scanlines
     */
    private void handleScanlinesSelection(String selectedItem) {
        if (selectedItem.startsWith("✅ ")) {
            String effectName = selectedItem.substring(3); // Enlever "✅ "
            overlaySystem.enableScanlines(effectName);
            Toast.makeText(this, "📺 Scanlines activées: " + effectName, Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Scanlines activées: " + effectName);
        } else if (selectedItem.equals("⬅️ Retour")) {
            goBack();
        }
    }
    
    /**
     * Gérer la sélection des patterns
     */
    private void handlePatternsSelection(String selectedItem) {
        if (selectedItem.startsWith("✅ ")) {
            String effectName = selectedItem.substring(3, selectedItem.indexOf(" ("));
            overlaySystem.enablePatterns(effectName);
            Toast.makeText(this, "🔲 Patterns activés: " + effectName, Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Patterns activés: " + effectName);
        } else if (selectedItem.equals("⬅️ Retour")) {
            goBack();
        }
    }
    
    /**
     * Gérer la sélection des CRT Bezels
     */
    private void handleCRTBezelsSelection(String selectedItem) {
        if (selectedItem.startsWith("✅ ")) {
            String effectName = selectedItem.substring(3);
            overlaySystem.enableCRTEffect(effectName);
            Toast.makeText(this, "🖥️ CRT Bezels activés: " + effectName, Toast.LENGTH_SHORT).show();
            Log.i(TAG, "CRT Bezels activés: " + effectName);
        } else if (selectedItem.equals("⬅️ Retour")) {
            goBack();
        }
    }
    
    /**
     * Gérer la sélection des phosphors
     */
    private void handlePhosphorsSelection(String selectedItem) {
        if (selectedItem.startsWith("✅ ")) {
            String effectName = selectedItem.substring(3);
            overlaySystem.enablePatterns(effectName); // Phosphors utilisent patterns
            Toast.makeText(this, "💡 Phosphors activés: " + effectName, Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Phosphors activés: " + effectName);
        } else if (selectedItem.equals("⬅️ Retour")) {
            goBack();
        }
    }
    
    /**
     * Afficher le dialogue d'opacité
     */
    private void showOpacityDialog() {
        // Créer un dialogue simple pour l'opacité
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("🎨 Opacité des effets");
        builder.setMessage("Opacité actuelle: 80%\n\nUtilisez les boutons volume pour ajuster");
        
        builder.setPositiveButton("OK", null);
        builder.setNegativeButton("Annuler", null);
        
        builder.show();
        
        // Simuler un ajustement d'opacité
        overlaySystem.setEffectOpacity(0.8f);
        Toast.makeText(this, "🎨 Opacité définie à 80%", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Appliquer les paramètres actuels
     */
    private void applyCurrentSettings() {
        // Sauvegarder les paramètres
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("visual_effects_applied", true);
        editor.apply();
        
        Toast.makeText(this, "✅ Paramètres appliqués", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "Paramètres visuels appliqués");
    }
    
    /**
     * Réinitialiser tous les paramètres
     */
    private void resetAllSettings() {
        // Réinitialiser les préférences
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
        
        // Désactiver tous les effets
        overlaySystem.disableEffects();
        
        Toast.makeText(this, "🔄 Paramètres réinitialisés", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "Paramètres visuels réinitialisés");
    }
    
    /**
     * Ajouter à l'historique de navigation
     */
    private void addToMenuHistory(String menu) {
        if (menuHistoryIndex < menuHistory.length - 1) {
            menuHistoryIndex++;
        } else {
            // Décaler l'historique
            for (int i = 0; i < menuHistory.length - 1; i++) {
                menuHistory[i] = menuHistory[i + 1];
            }
        }
        menuHistory[menuHistoryIndex] = menu;
    }
    
    /**
     * Retourner au menu précédent
     */
    private void goBack() {
        if (menuHistoryIndex > 0) {
            menuHistoryIndex--;
            String previousMenu = menuHistory[menuHistoryIndex];
            
            switch (previousMenu) {
                case "main":
                    showMainMenu();
                    break;
                case "visual_effects":
                    showVisualEffectsMenu();
                    break;
                default:
                    showMainMenu();
                    break;
            }
        } else {
            showMainMenu();
        }
    }
    
    @Override
    public void onBackPressed() {
        if (currentMenu.equals("main")) {
            super.onBackPressed();
        } else {
            goBack();
        }
    }
}
