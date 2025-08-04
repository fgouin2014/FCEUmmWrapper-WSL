#!/usr/bin/env python3
import os
from PIL import Image, ImageDraw

def create_very_visible_test():
    """Crée une image de test très visible avec des couleurs vives"""
    output_path = "app/src/main/assets/overlays/gamepads/retropad/very_visible_test.png"
    
    # Créer une image 1080x1920 avec un fond coloré
    size = (1080, 1920)
    img = Image.new('RGBA', size, (0, 0, 0, 0))  # Fond transparent
    draw = ImageDraw.Draw(img)
    
    # Dessiner un rectangle coloré qui couvre tout l'écran
    rect_color = (255, 0, 255, 150)  # Magenta semi-transparent
    
    draw.rectangle([0, 0, size[0], size[1]], fill=rect_color)
    
    # Ajouter des formes très visibles
    # Cercle rouge au centre
    center_x, center_y = size[0] // 2, size[1] // 2
    draw.ellipse([center_x - 200, center_y - 200, center_x + 200, center_y + 200], fill=(255, 0, 0, 255))
    
    # Rectangle bleu en haut à gauche
    draw.rectangle([100, 100, 400, 300], fill=(0, 0, 255, 255))
    
    # Rectangle vert en haut à droite
    draw.rectangle([size[0] - 400, 100, size[0] - 100, 300], fill=(0, 255, 0, 255))
    
    # Rectangle jaune en bas à gauche
    draw.rectangle([100, size[1] - 300, 400, size[1] - 100], fill=(255, 255, 0, 255))
    
    # Rectangle cyan en bas à droite
    draw.rectangle([size[0] - 400, size[1] - 300, size[0] - 100, size[1] - 100], fill=(0, 255, 255, 255))
    
    # Sauvegarder
    img.save(output_path)
    print(f"✅ Image de test très visible créée: {output_path}")
    
    return True

if __name__ == "__main__":
    create_very_visible_test() 