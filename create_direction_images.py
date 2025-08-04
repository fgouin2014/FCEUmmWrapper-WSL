#!/usr/bin/env python3
import os
from PIL import Image, ImageDraw

def create_direction_image(filename, direction, size=(50, 50)):
    """Crée une image de direction simple"""
    img = Image.new('RGBA', size, (0, 0, 0, 0))  # Fond transparent
    draw = ImageDraw.Draw(img)
    
    # Couleur du bouton
    color = (100, 100, 100, 180)  # Gris semi-transparent
    
    # Dessiner le bouton selon la direction
    if direction == "left":
        # Flèche vers la gauche
        points = [(size[0]-10, 10), (size[0]-10, size[1]-10), (10, size[1]//2)]
    elif direction == "right":
        # Flèche vers la droite
        points = [(10, 10), (10, size[1]-10), (size[0]-10, size[1]//2)]
    elif direction == "up":
        # Flèche vers le haut
        points = [(10, size[1]-10), (size[0]-10, size[1]-10), (size[0]//2, 10)]
    elif direction == "down":
        # Flèche vers le bas
        points = [(10, 10), (size[0]-10, 10), (size[0]//2, size[1]-10)]
    elif direction == "left-down":
        # Flèche diagonale gauche-bas
        points = [(size[0]-10, 10), (10, size[1]-10), (size[0]-10, size[1]-10)]
    elif direction == "right-up":
        # Flèche diagonale droite-haut
        points = [(10, size[1]-10), (size[0]-10, 10), (10, 10)]
    elif direction == "up-left":
        # Flèche diagonale haut-gauche
        points = [(size[0]-10, size[1]-10), (10, 10), (size[0]-10, 10)]
    elif direction == "down-right":
        # Flèche diagonale bas-droite
        points = [(10, 10), (size[0]-10, size[1]-10), (10, size[1]-10)]
    
    # Dessiner le polygone
    draw.polygon(points, fill=color)
    
    # Sauvegarder l'image
    img.save(filename, 'PNG')
    print(f"Créé: {filename}")

def main():
    # Créer le répertoire img s'il n'existe pas
    img_dir = "app/src/main/assets/overlays/gamepads/retropad/img"
    os.makedirs(img_dir, exist_ok=True)
    
    # Créer les images de direction
    directions = [
        ("left.png", "left"),
        ("right.png", "right"),
        ("up.png", "up"),
        ("down.png", "down"),
        ("left-down.png", "left-down"),
        ("right-up.png", "right-up"),
        ("up-left.png", "up-left"),
        ("down-right.png", "down-right")
    ]
    
    for filename, direction in directions:
        filepath = os.path.join(img_dir, filename)
        create_direction_image(filepath, direction)

if __name__ == "__main__":
    main() 