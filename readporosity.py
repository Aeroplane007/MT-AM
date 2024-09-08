import math 
import scipy
import csv
import numpy as np

porositymat = scipy.io.loadmat('porosity.mat', simplify_cells=True)
porosity = porositymat['porosity'][1]
layermat = scipy.io.loadmat('layer.mat', simplify_cells=True)
layers = layermat['layer'][1]
minlayer = np.max([np.min(layers[x]) for x in range(len(layers))])
startlayers = np.array([np.min(layers[x]) for x in range(len(layers))], dtype=np.int32) - 1

def getLabelsC(objectIndex, porositythreshold, startLayer, endLayer):
    porosityvalue = getCporosities(objectIndex, startLayer, endLayer)
    if math.isnan(porosityvalue):
        return np.nan
    # Classification
    # elif (porosityvalue*100) >= porositythreshold:
    #     return 1
    # else:
    #     return 0
    # Regression
    else:
        return porosityvalue

def getCporosities(objectIndex, startLayer, endLayer):
    objectIndex -= 1
    layerOffset = startlayers[objectIndex]
    if startLayer < layerOffset:
        return np.nan
    if endLayer < layerOffset:
        endLayer = layerOffset
    porosityValue = np.mean(porosity[objectIndex][startLayer-layerOffset:endLayer-layerOffset+1])
    if math.isnan(porosityValue):
        raise Exception("NaN error in porosity")
    return porosityValue

def savePorosities(output_file, object):
    data = []
    for i in range(1,91):
        data.append([str(getCporosities(object, i, i) * 100)])
    with open(output_file, 'w', newline='') as csv_file:
        writer = csv.writer(csv_file)
        writer.writerow(["porosity"])
        writer.writerows(data)

savePorosities("porosityData.csv", 7)