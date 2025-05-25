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
            # Create a new image with white background and the desired dimensions
            new_img = Image.new('RGB', (max_dimension, max_dimension), (255, 255, 255))
            
            # Resize the image while preserving aspect ratio
            img.thumbnail((max_dimension, max_dimension), Image.Resampling.LANCZOS)
            
            # Center the resized image on the white background
            offset_x = (max_dimension - img.width) // 2
            offset_y = (max_dimension - img.height) // 2
            new_img.paste(img, (offset_x, offset_y))
            
            # Replace img with the new image
            img = new_img
            target_file_path = os.path.join(target_directory, filename)
            img.save(target_file_path, 'JPEG')
            print(f"Processed and saved: {target_file_path}")

process_images('./raw', './thumbnails')