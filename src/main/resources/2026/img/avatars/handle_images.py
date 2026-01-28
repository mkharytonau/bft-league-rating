import os
from PIL import Image

def process_images(raw_directory, target_directory):
    for filename in os.listdir(raw_directory):
        file_path = os.path.join(raw_directory, filename)
        
        # Skip if it's not a file
        if not os.path.isfile(file_path):
            continue
        
        # Remove trailing whitespace from the filename
        name, ext = os.path.splitext(filename)
        new_name = name.rstrip()  # Remove trailing whitespace
        if new_name != name:
            new_filename = new_name + ext
            new_file_path = os.path.join(raw_directory, new_filename)
            os.rename(file_path, new_file_path)
            file_path = new_file_path
            filename = new_filename
        
        # Rename .jpeg to .jpg
        if filename.lower().endswith('.jpeg'):
            new_filename = filename[:-5] + '.jpg'
            new_file_path = os.path.join(raw_directory, new_filename)
            os.rename(file_path, new_file_path)
            file_path = new_file_path
            filename = new_filename
        
        # Rename .JPG to .jpg
        if filename.endswith('.JPG'):
            new_filename = filename[:-4] + '.jpg'
            new_file_path = os.path.join(raw_directory, new_filename)
            os.rename(file_path, new_file_path)
            file_path = new_file_path
            filename = new_filename
            
        if os.path.basename(__file__) == filename:
            print(f"Skipping script file: {filename}")
            continue
        
        # Open the image
        with Image.open(file_path) as img:
            # Convert non-JPEG files to JPEG
            if not filename.lower().endswith('.jpg'):
                new_filename = os.path.splitext(filename)[0] + '.jpg'
                new_file_path = os.path.join(raw_directory, new_filename)
                img = img.convert('RGB')  # Ensure compatibility with JPEG
                img.save(new_file_path, 'JPEG')
                os.remove(file_path)  # Remove the original file
                file_path = new_file_path
                filename = new_filename
            
            # Resize the image to ensure the largest dimension is 64 pixels
            max_dimension = 64
            
            # Resize the image while preserving aspect ratio, ensuring the smallest dimension is max_dimension
            img_ratio = img.width / img.height
            if img_ratio > 1:  # Wider than tall
                new_width = int(img_ratio * max_dimension)
                new_height = max_dimension
            else:  # Taller than wide or square
                new_width = max_dimension
                new_height = int(max_dimension / img_ratio)
            
            img = img.resize((new_width, new_height), Image.Resampling.LANCZOS)
            
            # Calculate the cropping box to take a square from the center
            left = (new_width - max_dimension) // 2
            top = (new_height - max_dimension) // 2
            right = left + max_dimension
            bottom = top + max_dimension
            
            # Crop the image to the square
            new_img = img.crop((left, top, right, bottom))
            
            # Replace img with the new image
            img = new_img
            target_file_path = os.path.join(target_directory, filename)
            img.save(target_file_path, 'JPEG')
            print(f"Processed and saved: {target_file_path}")

process_images('./raw', './thumbnails')