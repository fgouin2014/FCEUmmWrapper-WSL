#!/usr/bin/env python3
import os
from PIL import Image, ImageDraw

def create_visible_background():
    """Crée une image de fond semi-transparente visible"""
    output_path = "app/src/main/assets/overlays/gamepads/retropad/visible_background.png"
    
    # Créer une image 200x200 avec un fond semi-transparent
    size = (200, 200)
    img = Image.new('RGBA', size, (0, 0, 0, 0))  # Fond transparent
    draw = ImageDraw.Draw(img)
    
    # Dessiner un rectangle semi-transparent au centre
    center_x, center_y = size[0] // 2, size[1] // 2
    rect_size = 50
    rect_color = (100, 100, 100, 50)  # Gris très transparent
    
    draw.rectangle([
        center_x - rect_size//2, 
        center_y - rect_size//2,
        center_x + rect_size//2, 
        center_y + rect_size//2
    ], fill=rect_color)
    
    # Sauvegarder
    img.save(output_path)
    print(f"✅ Image de fond visible créée: {output_path}")
    
    return True

if __name__ == "__main__":
    create_visible_background() 