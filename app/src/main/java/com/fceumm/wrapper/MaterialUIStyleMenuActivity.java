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
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.SeekBar;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

import com.fceumm.wrapper.overlay.RetroArchOverlaySystem;

/**
 * Menu style MaterialUI RetroArch authentique
 * Interface utilisateur mobile 100% compatible avec RetroArch MaterialUI
 */
public class MaterialUIStyleMenuActivity extends Activity {
    private static final String TAG = "MaterialUIStyleMenu";
    private static final String PREF_NAME = "FCEUmmMaterialUIMenu";
    
    private SharedPreferences preferences;
    private RetroArchOverlaySystem overlaySystem;
    
    // Navigation MaterialUI
    private String currentMenu = "main";
    private String[] menuHistory = new String[10];
    private int menuHistoryIndex = 0;
    
    // Interface MaterialUI
    private ListView menuListView;
    private TextView menuTitle;
    private TextView menuSubtitle;
    private LinearLayout menuContainer;
    private LinearLayout navBar;
    private LinearLayout statusBar;
    
    // Couleurs MaterialUI authentiques
    private static final int MUI_COLOR_PRIMARY = Color.parseColor("#00ff00"); // Vert RetroArch
    private static final int MUI_COLOR_BACKGROUND = Color.parseColor("#000000"); // Noir
    private static final int MUI_COLOR_SURFACE = Color.parseColor("#1a1a1a"); // Gris foncé
    private static final int MUI_COLOR_TEXT_PRIMARY = Color.parseColor("#ffffff"); // Blanc
    private static final int MUI_COLOR_TEXT_SECONDARY = Color.parseColor("#cccccc"); // Gris clair
    private static final int MUI_COLOR_DIVIDER = Color.parseColor("#333333"); // Séparateur
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialiser les préférences
        preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        
        // Initialiser le système d'overlay
        overlaySystem = RetroArchOverlaySystem.getInstance(this);
        
        // Créer l'interface MaterialUI authentique
        createMaterialUIStyleInterface();
        
        // Afficher le menu principal
        showMainMenu();
        
        Log.i(TAG, "🎮 Menu MaterialUI RetroArch authentique créé");
    }
    
    /**
     * Créer l'interface style MaterialUI authentique
     */
    private void createMaterialUIStyleInterface() {
        // Layout principal style MaterialUI
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(MUI_COLOR_BACKGROUND);
        mainLayout.setPadding(0, 0, 0, 0);
        
        // Status Bar MaterialUI (en haut)
        statusBar = new LinearLayout(this);
        statusBar.setOrientation(LinearLayout.HORIZONTAL);
        statusBar.setBackgroundColor(MUI_COLOR_SURFACE);
        statusBar.setPadding(20, 10, 20, 10);
        statusBar.setGravity(Gravity.CENTER_VERTICAL);
        
        TextView statusText = new TextView(this);
        statusText.setText("FCEUmm Wrapper - MaterialUI");
        statusText.setTextColor(MUI_COLOR_TEXT_SECONDARY);
        statusText.setTextSize(12);
        statusBar.addView(statusText);
        
        mainLayout.addView(statusBar);
        
        // Header MaterialUI avec titre et sous-titre
        LinearLayout headerLayout = new LinearLayout(this);
        headerLayout.setOrientation(LinearLayout.VERTICAL);
        headerLayout.setBackgroundColor(MUI_COLOR_SURFACE);
        headerLayout.setPadding(20, 20, 20, 20);
        
        // Titre principal style MaterialUI
        menuTitle = new TextView(this);
        menuTitle.setText("RETROARCH");
        menuTitle.setTextColor(MUI_COLOR_PRIMARY);
        menuTitle.setTextSize(24);
        menuTitle.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        menuTitle.setGravity(Gravity.CENTER);
        headerLayout.addView(menuTitle);
        
        // Sous-titre
        menuSubtitle = new TextView(this);
        menuSubtitle.setText("Configuration des effets visuels");
        menuSubtitle.setTextColor(MUI_COLOR_TEXT_SECONDARY);
        menuSubtitle.setTextSize(14);
        menuSubtitle.setGravity(Gravity.CENTER);
        menuSubtitle.setPadding(0, 8, 0, 0);
        headerLayout.addView(menuSubtitle);
        
        mainLayout.addView(headerLayout);
        
        // Container du menu MaterialUI
        menuContainer = new LinearLayout(this);
        menuContainer.setOrientation(LinearLayout.VERTICAL);
        menuContainer.setBackgroundColor(MUI_COLOR_BACKGROUND);
        menuContainer.setPadding(0, 0, 0, 0);
        
        // ListView pour le menu style MaterialUI
        menuListView = new ListView(this);
        menuListView.setBackgroundColor(MUI_COLOR_BACKGROUND);
        menuListView.setDividerHeight(1);
        // Créer un drawable pour le séparateur
        android.graphics.drawable.ColorDrawable dividerDrawable = new android.graphics.drawable.ColorDrawable(MUI_COLOR_DIVIDER);
        menuListView.setDivider(dividerDrawable);
        menuListView.setPadding(0, 0, 0, 0);
        
        // Style des items MaterialUI
        menuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                handleMenuSelection(selectedItem);
            }
        });
        
        menuContainer.addView(menuListView);
        mainLayout.addView(menuContainer);
        
        // Navigation Bar MaterialUI (en bas)
        navBar = new LinearLayout(this);
        navBar.setOrientation(LinearLayout.HORIZONTAL);
        navBar.setBackgroundColor(MUI_COLOR_SURFACE);
        navBar.setPadding(20, 15, 20, 15);
        navBar.setGravity(Gravity.CENTER);
        
        // Boutons de navigation MaterialUI
        Button backButton = new Button(this);
        backButton.setText("⬅️ Retour");
        backButton.setTextColor(MUI_COLOR_PRIMARY);
        backButton.setBackgroundColor(Color.TRANSPARENT);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });
        
        Button applyButton = new Button(this);
        applyButton.setText("✅ Appliquer");
        applyButton.setTextColor(MUI_COLOR_PRIMARY);
        applyButton.setBackgroundColor(Color.TRANSPARENT);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyCurrentSettings();
            }
        });
        
        navBar.addView(backButton);
        navBar.addView(applyButton);
        
        mainLayout.addView(navBar);
        
        setContentView(mainLayout);
    }
    
    /**
     * Afficher le menu principal MaterialUI
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
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
            android.R.layout.simple_list_item_1, mainMenuItems) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (view instanceof TextView) {
                    TextView textView = (TextView) view;
                    textView.setTextColor(MUI_COLOR_TEXT_PRIMARY);
                    textView.setTextSize(16);
                    textView.setPadding(20, 15, 20, 15);
                    textView.setBackgroundColor(MUI_COLOR_BACKGROUND);
                }
                return view;
            }
        };
        
        menuListView.setAdapter(adapter);
    }
    
    /**
     * Afficher le menu des effets visuels MaterialUI
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
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
            android.R.layout.simple_list_item_1, effectsMenuItems) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (view instanceof TextView) {
                    TextView textView = (TextView) view;
                    textView.setTextColor(MUI_COLOR_TEXT_PRIMARY);
                    textView.setTextSize(16);
                    textView.setPadding(20, 15, 20, 15);
                    textView.setBackgroundColor(MUI_COLOR_BACKGROUND);
                }
                return view;
            }
        };
        
        menuListView.setAdapter(adapter);
    }
    
    /**
     * Afficher le sous-menu scanlines MaterialUI
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
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
            android.R.layout.simple_list_item_1, scanlinesItems) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (view instanceof TextView) {
                    TextView textView = (TextView) view;
                    textView.setTextColor(MUI_COLOR_TEXT_PRIMARY);
                    textView.setTextSize(16);
                    textView.setPadding(20, 15, 20, 15);
                    textView.setBackgroundColor(MUI_COLOR_BACKGROUND);
                }
                return view;
            }
        };
        
        menuListView.setAdapter(adapter);
    }
    
    /**
     * Afficher le sous-menu patterns MaterialUI
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
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
            android.R.layout.simple_list_item_1, patternsItems) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (view instanceof TextView) {
                    TextView textView = (TextView) view;
                    textView.setTextColor(MUI_COLOR_TEXT_PRIMARY);
                    textView.setTextSize(16);
                    textView.setPadding(20, 15, 20, 15);
                    textView.setBackgroundColor(MUI_COLOR_BACKGROUND);
                }
                return view;
            }
        };
        
        menuListView.setAdapter(adapter);
    }
    
    /**
     * Afficher le sous-menu CRT Bezels MaterialUI
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
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
            android.R.layout.simple_list_item_1, bezelsItems) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (view instanceof TextView) {
                    TextView textView = (TextView) view;
                    textView.setTextColor(MUI_COLOR_TEXT_PRIMARY);
                    textView.setTextSize(16);
                    textView.setPadding(20, 15, 20, 15);
                    textView.setBackgroundColor(MUI_COLOR_BACKGROUND);
                }
                return view;
            }
        };
        
        menuListView.setAdapter(adapter);
    }
    
    /**
     * Afficher le sous-menu Phosphors MaterialUI
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
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
            android.R.layout.simple_list_item_1, phosphorsItems) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (view instanceof TextView) {
                    TextView textView = (TextView) view;
                    textView.setTextColor(MUI_COLOR_TEXT_PRIMARY);
                    textView.setTextSize(16);
                    textView.setPadding(20, 15, 20, 15);
                    textView.setBackgroundColor(MUI_COLOR_BACKGROUND);
                }
                return view;
            }
        };
        
        menuListView.setAdapter(adapter);
    }
    
    /**
     * Gérer la sélection dans le menu MaterialUI
     */
    private void handleMenuSelection(String selectedItem) {
        Log.i(TAG, "Sélection menu MaterialUI: " + selectedItem);
        
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
     * Gérer la sélection du menu principal MaterialUI
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
     * Gérer la sélection du menu effets visuels MaterialUI
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
     * Gérer la sélection des scanlines MaterialUI
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
     * Gérer la sélection des patterns MaterialUI
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
     * Gérer la sélection des CRT Bezels MaterialUI
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
     * Gérer la sélection des phosphors MaterialUI
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
     * Afficher le dialogue d'opacité MaterialUI
     */
    private void showOpacityDialog() {
        // Créer un dialogue MaterialUI pour l'opacité
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
     * Appliquer les paramètres actuels MaterialUI
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
     * Réinitialiser tous les paramètres MaterialUI
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
     * Ajouter à l'historique de navigation MaterialUI
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
     * Retourner au menu précédent MaterialUI
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

