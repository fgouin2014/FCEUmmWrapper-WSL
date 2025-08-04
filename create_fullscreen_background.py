#!/usr/bin/env python3
import os
from PIL import Image, ImageDraw

def create_fullscreen_background():
    """Crée une image de fond pleine taille semi-transparente"""
    output_path = "app/src/main/assets/overlays/gamepads/retropad/fullscreen_background.png"
    
    # Créer une image 1080x1920 (taille d'écran typique)
    size = (1080, 1920)
    img = Image.new('RGBA', size, (0, 0, 0, 0))  # Fond transparent
    draw = ImageDraw.Draw(img)
    
    # Dessiner un rectangle semi-transparent qui couvre tout l'écran
    rect_color = (50, 50, 50, 30)  # Gris très transparent
    
    draw.rectangle([0, 0, size[0], size[1]], fill=rect_color)
    
    # Ajouter quelques éléments visuels pour confirmer que l'image est chargée
    # Dessiner des cercles aux coins
    circle_color = (100, 100, 100, 80)
    circle_size = 20
    
    # Coin supérieur gauche
    draw.ellipse([10, 10, 10 + circle_size, 10 + circle_size], fill=circle_color)
    # Coin supérieur droit
    draw.ellipse([size[0] - 30, 10, size[0] - 10, 10 + circle_size], fill=circle_color)
    # Coin inférieur gauche
    draw.ellipse([10, size[1] - 30, 10 + circle_size, size[1] - 10], fill=circle_color)
    # Coin inférieur droit
    draw.ellipse([size[0] - 30, size[1] - 30, size[0] - 10, size[1] - 10], fill=circle_color)
    
    # Sauvegarder
    img.save(output_path)
    print(f"✅ Image de fond pleine taille créée: {output_path}")
    
    return True

if __name__ == "__main__":
    create_fullscreen_background() 