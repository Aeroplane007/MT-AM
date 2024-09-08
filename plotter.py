import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
import os
from sklearn.metrics import confusion_matrix

object = "B10"
percent = "15"
plot_filename = "confusionMatrix"+object+"_"+percent+"_OC.png"

# Example numbers for true positives, false positives, true negatives, and false negatives
TP = 262
FP = 940
TN = 0
FN = 13

# Creating the confusion matrix manually
conf_matrix = np.array([[TP, FP],
                        [FN, TN]])

sns.set(context='talk', font_scale=1.3)


print(plot_filename)
print("Accuracy: " , (TP+TN)/(TP+FP+TN+FN))
print("Precision: " , (TP)/(TP+FP))
print("Recall: " , (TP)/(TP+FN))

# Plotting the confusion matrix using seaborn's heatmap for better visualization
plt.figure(figsize=(6, 6))
sns.heatmap(conf_matrix, annot=True, fmt='d', cmap='Blues', xticklabels=['1', '0'], yticklabels=['1', '0'], cbar=False)
plt.ylabel('Predicted')
plt.xlabel('Actual')
plt.title('Confusion Matrix')

plt.tight_layout()

# Ensure the directory exists
output_dir = "../Images"
if not os.path.exists(output_dir):
    os.makedirs(output_dir)

# Save the plot with the given name
plt.savefig(os.path.join(output_dir, plot_filename))


plt.show()
