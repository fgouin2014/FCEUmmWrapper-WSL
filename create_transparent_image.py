#!/usr/bin/env python3
import os
from PIL import Image

def create_transparent_image():
    """Crée une image 1x1 complètement transparente"""
    output_path = "app/src/main/assets/overlays/gamepads/retropad/transparent_pixel.png"
    
    # Créer une image 1x1 complètement transparente
    img = Image.new('RGBA', (1, 1), (0, 0, 0, 0))
    
    # Sauvegarder
    img.save(output_path)
    print(f"✅ Image transparente créée: {output_path}")
    
    return True

if __name__ == "__main__":
    create_transparent_image() 