from sklearn.linear_model import LinearRegression
import csv
from PIL import Image
import numpy as np

X = []
y = []

def getGroundTruthData(input_file):
    with open(input_file, 'r', newline='') as csv_file:
        csv_reader = csv.reader(csv_file)
        for i in range(1,8):
            next(csv_reader)
        for row in csv_reader:
            y.append(float(row[0]))

def getTrainingData(image_folder):
    for i in range(7,91):
        img = Image.open(image_folder + "/object_8_layer_" + str(i) + ".png")
        img_array = np.array(img) / 0xFFFF
        X.append(img_array.flatten())


getGroundTruthData("porosityData.csv")
getTrainingData("thermalImages")
print(len(y))
print(len(X))
reg = LinearRegression().fit(X, y)
print(reg.score(X, y))
print(reg.predict([X[0]]))

