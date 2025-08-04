#!/usr/bin/env python3
import os
from PIL import Image, ImageDraw

def create_visible_test_background():
    """Crée une image de fond avec des cercles très visibles pour tester"""
    output_path = "app/src/main/assets/overlays/gamepads/retropad/test_background.png"
    
    # Créer une image 1080x1920 avec un fond semi-transparent
    size = (1080, 1920)
    img = Image.new('RGBA', size, (0, 0, 0, 0))  # Fond transparent
    draw = ImageDraw.Draw(img)
    
    # Dessiner un rectangle semi-transparent qui couvre tout l'écran
    rect_color = (100, 100, 100, 100)  # Gris plus visible
    
    draw.rectangle([0, 0, size[0], size[1]], fill=rect_color)
    
    # Ajouter des cercles très visibles aux coins
    circle_color = (255, 0, 0, 200)  # Rouge très visible
    circle_size = 100  # Cercles plus grands
    
    # Coin supérieur gauche
    draw.ellipse([50, 50, 50 + circle_size, 50 + circle_size], fill=circle_color)
    # Coin supérieur droit
    draw.ellipse([size[0] - 150, 50, size[0] - 50, 50 + circle_size], fill=circle_color)
    # Coin inférieur gauche
    draw.ellipse([50, size[1] - 150, 50 + circle_size, size[1] - 50], fill=circle_color)
    # Coin inférieur droit
    draw.ellipse([size[0] - 150, size[1] - 150, size[0] - 50, size[1] - 50], fill=circle_color)
    
    # Ajouter un cercle au centre
    center_x, center_y = size[0] // 2, size[1] // 2
    draw.ellipse([center_x - 50, center_y - 50, center_x + 50, center_y + 50], fill=(0, 255, 0, 200))
    
    # Sauvegarder
    img.save(output_path)
    print(f"✅ Image de test créée: {output_path}")
    
    return True

if __name__ == "__main__":
    create_visible_test_background() 