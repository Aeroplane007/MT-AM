import os
import re


# Define the regex pattern to match the filenames
pattern = r'SI266220230709123549_(\d+)_\d+_\d+_Max_32F\.tif'

# Iterate over each file in the directory

directory = 'Layer_Images_Claudia/'
    # Check if the filename matches the pattern
for filename in os.listdir(directory):
    match = re.match(pattern, filename)

    if match:
            # Construct the new filename
        new_filename = f"Layer_{match.group(1)}.tif"
        # Rename the file
        os.rename(os.path.join(directory, filename), os.path.join(directory, new_filename))
        print(f"Renamed {filename} to {new_filename}")


