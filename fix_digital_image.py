#!/usr/bin/env python3
import os
from PIL import Image

def make_digital_transparent():
    """Rend l'image digital.png transparente"""
    input_path = "app/src/main/assets/overlays/gamepads/retropad/digital.png"
    output_path = "app/src/main/assets/overlays/gamepads/retropad/digital_transparent.png"
    
    if not os.path.exists(input_path):
        print(f"❌ Fichier {input_path} non trouvé")
        return False
    
    try:
        # Ouvrir l'image
        img = Image.open(input_path)
        print(f"📁 Image originale: {img.size}, mode: {img.mode}")
        
        # Convertir en RGBA si nécessaire
        if img.mode != 'RGBA':
            img = img.convert('RGBA')
        
        # Créer une nouvelle image avec fond transparent
        transparent_img = Image.new('RGBA', img.size, (0, 0, 0, 0))
        
        # Copier les pixels non-noirs
        pixels = img.getdata()
        transparent_pixels = []
        
        for pixel in pixels:
            r, g, b, a = pixel
            # Si le pixel est noir ou très sombre, le rendre transparent
            if r < 50 and g < 50 and b < 50:
                transparent_pixels.append((0, 0, 0, 0))
            else:
                # Garder le pixel avec une transparence réduite
                transparent_pixels.append((r, g, b, min(a, 128)))
        
        transparent_img.putdata(transparent_pixels)
        
        # Sauvegarder
        transparent_img.save(output_path)
        print(f"✅ Image transparente créée: {output_path}")
        
        # Remplacer l'original
        os.replace(output_path, input_path)
        print(f"✅ Image originale remplacée par la version transparente")
        
        return True
        
    except Exception as e:
        print(f"❌ Erreur: {e}")
        return False

if __name__ == "__main__":
    make_digital_transparent() 