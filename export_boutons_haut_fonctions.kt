// EXPORT DES FONCTIONS KOTLIN DES BOUTONS DU HAUT DE LA SURFACE DE JEU
// Fichier: export_boutons_haut_fonctions.kt
// Date: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")

// ==========================================
// IMPORTS NÉCESSAIRES
// ==========================================
import android.widget.Button
import android.widget.Toast
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Switch
import android.widget.ArrayAdapter
import android.widget.AdapterView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory

// ==========================================
// VARIABLES GLOBALES POUR LES BOUTONS
// ==========================================

// Variables pour le système de rewind
private val rewindBuffer = ArrayDeque<ByteArray>()
private var rewindEnabled = true
private var rewindBufferMaxDurationSec = 5
private val rewindIntervalMs = 500
private val rewindBufferMaxSize = (rewindBufferMaxDurationSec * 1000) / rewindIntervalMs
private var rewindJob: Job? = null
private var rewindProgressiveJob: Job? = null

// Variables pour l'état du dialog d'aide
private var isHelpDialogOpen = false
private var lastHelpTabIndex = 0

// ==========================================
// FONCTIONS D'INITIALISATION DES BOUTONS
// ==========================================

/**
 * Initialise tous les boutons du haut de la surface de jeu
 */
fun initializeTopButtons() {
    initializeSaveButton()
    initializeScreenshotButton()
    initializeLoadButton()
    initializeHelpButton()
    initializeRewindButton()
    initializeMenuButton()
}

// ==========================================
// BOUTON SAUVEGARDE
// ==========================================

/**
 * Initialise le bouton de sauvegarde
 */
fun initializeSaveButton() {
    val btnSave = findViewById<Button>(R.id.btnSave)
    
    // Clic simple : quicksave
    btnSave.setOnClickListener {
        Log.d("GameActivity", "Clic sur bouton SAUVEGARDE principal")
        if (!::retroView.isInitialized) {
            Log.w("GameActivity", "Impossible de sauvegarder : retroView non initialisé")
            Toast.makeText(this, "Erreur : émulateur non initialisé", Toast.LENGTH_SHORT).show()
            return@setOnClickListener
        }
        if (!com.swordfish.libretrodroid.LibretroDroid.isCoreInitialized()) {
            Log.w("GameActivity", "Impossible de sauvegarder : core non initialisé")
            Toast.makeText(this, "Erreur : core non initialisé", Toast.LENGTH_SHORT).show()
            return@setOnClickListener
        }
        try {
            val saveData = retroView.serializeState()
            val file = File(saveDir, "save_slot_0.sav")
            file.writeBytes(saveData)
            
            // Capture d'écran pour le quicksave avec traitement d'image
            retroView.captureScreenshotSafe { screenshotBytes ->
                if (screenshotBytes != null && screenshotBytes.isNotEmpty()) {
                    val fullBitmap = BitmapFactory.decodeByteArray(screenshotBytes, 0, screenshotBytes.size)
                    if (fullBitmap != null) {
                        val cropWidth = 1080
                        val cropHeight = 828
                        val cropLeft = 0
                        val cropTop = (fullBitmap.height - cropHeight) / 2
                        val croppedBitmap = Bitmap.createBitmap(fullBitmap, cropLeft, cropTop, cropWidth, cropHeight)
                        val screenshotFile = File(saveDir, "save_slot_0.png")
                        FileOutputStream(screenshotFile).use { out ->
                            croppedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        }
                    }
                }
                Toast.makeText(this, "Quicksave effectué !", Toast.LENGTH_SHORT).show()
                Log.d("GameActivity", "Quicksave effectué dans save_slot_0.sav (${saveData.size} octets)")
            }
        } catch (e: Exception) {
            Log.e("GameActivity", "Erreur lors de la sauvegarde du quicksave: ", e)
            Toast.makeText(this, "Erreur lors de la sauvegarde du quicksave", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Appui long : menu de sauvegarde
    btnSave.setOnLongClickListener {
        showSaveDialog()
        true
    }
}

// ==========================================
// BOUTON CAPTURE D'ÉCRAN
// ==========================================

/**
 * Initialise le bouton de capture d'écran
 */
fun initializeScreenshotButton() {
    val btnScreenshot = findViewById<Button>(R.id.btnScreenshot)
    btnScreenshot.visibility = View.VISIBLE
    btnScreenshot.isEnabled = true
    
    btnScreenshot.setOnClickListener {
        // Générer un nom de fichier unique avec timestamp
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        retroView.captureScreenshotSafe { screenshotBytes ->
            if (screenshotBytes != null && screenshotBytes.isNotEmpty()) {
                val fullBitmap = BitmapFactory.decodeByteArray(screenshotBytes, 0, screenshotBytes.size)
                if (fullBitmap != null) {
                    // Crop central 1080x828 (toujours, comme pour les sauvegardes)
                    val cropWidth = 1080
                    val cropHeight = 828
                    val cropLeft = 0
                    val cropTop = (fullBitmap.height - cropHeight) / 2
                    val croppedBitmap = Bitmap.createBitmap(fullBitmap, cropLeft, cropTop, cropWidth, cropHeight)
                    val screenshotFile = File(saveDir, "screenshot_${timestamp}.png")
                    FileOutputStream(screenshotFile).use { out ->
                        croppedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    showScreenshotPreviewDialog(screenshotFile)
                } else {
                    showScreenshotPreviewDialog(null)
                }
            } else {
                showScreenshotPreviewDialog(null)
            }
        }
    }
}

// ==========================================
// BOUTON CHARGER
// ==========================================

/**
 * Initialise le bouton de chargement
 */
fun initializeLoadButton() {
    val btnLoad = findViewById<Button>(R.id.btnLoad)
    
    // Clic simple : chargement du quicksave
    btnLoad.setOnClickListener {
        if (!::retroView.isInitialized) return@setOnClickListener
        val quickFile = File(saveDir, "save_slot_0.sav")
        if (quickFile.exists()) {
            try {
                val data = quickFile.readBytes()
                val ok = retroView.unserializeState(data)
                if (ok) {
                    Toast.makeText(this, "Quicksave chargé !", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Erreur lors du chargement du quicksave", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("GameActivity", "Erreur lors du chargement du quicksave: ", e)
                Toast.makeText(this, "Erreur lors du chargement du quicksave", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Chercher la dernière sauvegarde parmi les slots 1 à 5
            val slots = (1..5).map { File(saveDir, "save_slot_${it}.sav") }
            val lastFile = slots.filter { it.exists() }.maxByOrNull { it.lastModified() }
            if (lastFile != null) {
                try {
                    val data = lastFile.readBytes()
                    val ok = retroView.unserializeState(data)
                    if (ok) {
                        Toast.makeText(this, "Dernière sauvegarde chargée (slot ${lastFile.name.filter { it.isDigit() }}) !", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Erreur lors du chargement de la sauvegarde", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("GameActivity", "Erreur lors du chargement de la sauvegarde: ", e)
                    Toast.makeText(this, "Erreur lors du chargement de la sauvegarde", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Aucune sauvegarde trouvée.\nAppuyez sur le bouton de gauche pour créer un quicksave, ou faites un appui long pour accéder aux sauvegardes manuelles.", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    // Appui long : menu de chargement
    btnLoad.setOnLongClickListener {
        showLoadDialog()
        true
    }
}

// ==========================================
// BOUTON AIDE
// ==========================================

/**
 * Initialise le bouton d'aide
 */
fun initializeHelpButton() {
    val btnHelp = findViewById<Button>(R.id.btnHelp)
    btnHelp.setOnClickListener {
        showHelpDialog()
    }
    
    // Restauration de l'état du dialog d'aide
    if (savedInstanceState != null) {
        isHelpDialogOpen = savedInstanceState.getBoolean("isHelpDialogOpen", false)
        lastHelpTabIndex = savedInstanceState.getInt("lastHelpTabIndex", 0)
        if (isHelpDialogOpen) {
            showHelpDialog(lastHelpTabIndex)
        }
    }
}

// ==========================================
// BOUTON REWIND
// ==========================================

/**
 * Initialise le bouton de rewind
 */
fun initializeRewindButton() {
    val btnRewind = findViewById<Button>(R.id.rewind_button)
    val switchRewindEnable = findViewById<Switch>(R.id.switchRewindEnable)
    val rewindSpinner = findViewById<ImageView>(R.id.rewind_spinner)
    
    // Configuration du switch d'activation
    switchRewindEnable?.isChecked = rewindEnabled
    btnRewind?.visibility = if (rewindEnabled) View.VISIBLE else View.GONE
    
    switchRewindEnable?.setOnCheckedChangeListener { _, isChecked ->
        rewindEnabled = isChecked
        btnRewind?.visibility = if (isChecked) View.VISIBLE else View.GONE
        if (!rewindEnabled) {
            rewindBuffer.clear()
        }
    }
    
    // Configuration du spinner de durée
    val spinnerRewindDuration = findViewById<Spinner>(R.id.spinnerRewindDuration)
    val durations = listOf(5, 10, 20)
    spinnerRewindDuration?.setSelection(durations.indexOf(rewindBufferMaxDurationSec))
    spinnerRewindDuration?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
            rewindBufferMaxDurationSec = durations[position]
        }
        override fun onNothingSelected(parent: AdapterView<*>) {}
    }
    
    // Animation du spinner
    val rotateAnim = android.view.animation.RotateAnimation(
        0f, -360f,
        android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
        android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f
    ).apply {
        duration = 700
        repeatCount = android.view.animation.Animation.INFINITE
        interpolator = android.view.animation.LinearInterpolator()
    }
    
    // Gestion du touch pour le rewind progressif
    btnRewind?.setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (rewindEnabled) {
                    showDarkToast("Rewind en cours...")
                    rewindSpinner?.visibility = View.VISIBLE
                    rewindSpinner?.startAnimation(rotateAnim)
                    rewindProgressiveJob = mainScope.launch {
                        while (rewindBuffer.isNotEmpty() && isActive) {
                            val state = rewindBuffer.removeLast()
                            retroView.unserializeState(state)
                            delay(100)
                        }
                        rewindSpinner?.clearAnimation()
                        rewindSpinner?.visibility = View.GONE
                        showDarkToast("Limite de rewind atteinte")
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                rewindProgressiveJob?.cancel()
                rewindSpinner?.clearAnimation()
                rewindSpinner?.visibility = View.GONE
            }
        }
        true
    }
    
    // Lancement de la sauvegarde périodique pour le rewind
    rewindJob = mainScope.launch {
        while (isActive) {
            if (::retroView.isInitialized) {
                if (::retroView.isInitialized && com.swordfish.libretrodroid.LibretroDroid.isCoreInitialized()) {
                    try {
                        val state = retroView.serializeState()
                        rewindBuffer.addLast(state)
                        if (rewindBuffer.size > rewindBufferMaxSize) {
                            rewindBuffer.removeFirst()
                        }
                    } catch (e: Exception) {
                        Log.e("GameActivity", "Erreur lors de la sauvegarde périodique (core non prêt) : ", e)
                    }
                }
            }
            kotlinx.coroutines.delay(rewindIntervalMs.toLong())
        }
    }
}

// ==========================================
// BOUTON MENU
// ==========================================

/**
 * Initialise le bouton menu
 */
fun initializeMenuButton() {
    val btnMenuIcon = findViewById<Button>(R.id.btnMenuIcon)
    val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
    
    // Appui long : ouvre le drawer
    btnMenuIcon.setOnLongClickListener {
        drawerLayout.openDrawer(GravityCompat.END)
        true
    }
    
    // Clic simple : action vide (pour éviter les clics accidentels)
    btnMenuIcon.setOnClickListener { }
    
    // Configuration du listener du drawer
    drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
        override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
        override fun onDrawerOpened(drawerView: View) {
            Log.d("GameActivity", "Drawer ouvert (onDrawerOpened)")
            if (::retroView.isInitialized) {
                Log.d("GameActivity", "Drawer ouvert : pause core (forcé)")
                retroView.onPause()
            }
        }
        override fun onDrawerClosed(drawerView: View) {
            Log.d("GameActivity", "Drawer fermé (onDrawerClosed)")
            if (coreRestartRequired) {
                Log.w("GameActivity", "Drawer fermé : restart demandé, relance GameActivity")
                coreRestartRequired = false
                finish()
                startActivity(intent)
            } else if (::retroView.isInitialized && com.swordfish.libretrodroid.LibretroDroid.isCoreInitialized()) {
                Log.d("GameActivity", "Drawer fermé : reprise core")
                retroView.onResume()
            } else {
                Log.w("GameActivity", "Drawer fermé : core non initialisé, mais pas de restart demandé. On ne fait rien.")
            }
        }
        override fun onDrawerStateChanged(newState: Int) {}
    })
}

// ==========================================
// FONCTIONS UTILITAIRES
// ==========================================

/**
 * Affiche un toast avec un style sombre
 */
fun showDarkToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

/**
 * Affiche le dialog de sauvegarde
 */
fun showSaveDialog() {
    // Capturer l'écran AVANT d'ouvrir le dialog
    retroView.captureScreenshotSafe { screenshotBytes ->
        if (screenshotBytes != null && screenshotBytes.isNotEmpty()) {
            // Sauvegarder la capture dans un fichier temporaire
            val tempScreenshotFile = File(filesDir, "temp_screenshot.png")
            tempScreenshotFile.writeBytes(screenshotBytes)
        }
        
        // Maintenant ouvrir le dialog
        showSaveDialogInternal()
    }
}

/**
 * Affiche le dialog de chargement
 */
fun showLoadDialog() {
    // Implémentation du dialog de chargement
    // Cette fonction doit être implémentée selon vos besoins
}

/**
 * Affiche le dialog d'aide
 */
fun showHelpDialog(tabIndex: Int = 0) {
    // Implémentation du dialog d'aide
    // Cette fonction doit être implémentée selon vos besoins
}

/**
 * Affiche le preview de la capture d'écran
 */
fun showScreenshotPreviewDialog(screenshotFile: File?) {
    // Implémentation du preview de capture d'écran
    // Cette fonction doit être implémentée selon vos besoins
}

// ==========================================
// NOTES D'IMPLÉMENTATION
// ==========================================
/*
1. Toutes les fonctions d'initialisation doivent être appelées dans onCreate()
2. Les variables globales (rewindBuffer, etc.) doivent être déclarées au niveau de la classe
3. Les fonctions utilitaires (showSaveDialog, showLoadDialog, etc.) doivent être implémentées
4. Les imports nécessaires doivent être ajoutés au début du fichier
5. Les références aux ressources (R.id.*) doivent être correctement importées
6. Les variables comme retroView, saveDir, mainScope doivent être disponibles dans le contexte
7. Les fonctions de callback pour les dialogs doivent être implémentées selon vos besoins
*/ 