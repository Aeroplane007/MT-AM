import matplotlib.pyplot as plt

# Data for A11
a11_sizes =     [5,    10,   20,   30,     40]
a11_accuracy =  [57.9, 38,   36.5, 36.5,   36.6]
a11_precision = [61.5, 38.7, 37.2, 37.1,   37.2]
a11_recall =    [90.7, 95.5, 95,   95.8,   95.7]

# Data for B10
b10_sizes =     [5,    10,   20,   30,   40]
b10_accuracy =  [55.1, 25.7, 22.4, 21.4, 21.5]
b10_precision = [62.6, 26,   22.7, 21.7, 21.8]
b10_recall =    [82.1, 96.4, 95.6, 94.5, 95.3]

# Plot for A11
plt.figure(figsize=(10, 5))
plt.plot(a11_sizes, a11_accuracy, marker='o', label='Accuracy')
plt.plot(a11_sizes, a11_precision, marker='o', label='Precision')
plt.plot(a11_sizes, a11_recall, marker='o', label='Recall')
plt.title('A11: Performance Metrics vs Neighborhood Size')
plt.xlabel('Neighborhood Size')
plt.ylabel('Percentage')
plt.legend()
plt.grid(True)
plt.show()

# Plot for B10
plt.figure(figsize=(10, 5))
plt.plot(b10_sizes, b10_accuracy, marker='o', label='Accuracy')
plt.plot(b10_sizes, b10_precision, marker='o', label='Precision')
plt.plot(b10_sizes, b10_recall, marker='o', label='Recall')
plt.title('B10: Performance Metrics vs Neighborhood Size')
plt.xlabel('Neighborhood Size')
plt.ylabel('Percentage')
plt.legend()
plt.grid(True)
plt.show()