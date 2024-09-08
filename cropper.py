from PIL import Image

#PADDING = 3
#DISTANCE_X = 267+2*PADDING
#DISTANCE_Y = 273+2*PADDING
#WIDTH = 94-(2*PADDING)
#HEIGHT = 94-(2*PADDING)
#START_OF_FIRST_OBJECT_X = 237+PADDING
#START_OF_FIRST_OBJECT_Y = 249+PADDING







#best accuracy when padding = 19

def Crop():



        for column in range(2,3):
            for row in range(1,2):
                for layer in range(0,90):
                    im = Image.open("Layer_Images/Layer_" + str(layer) + ".png")


                    left = START_OF_FIRST_OBJECT_X + ((DISTANCE_X+WIDTH) * column)
                    top = START_OF_FIRST_OBJECT_Y + ((DISTANCE_Y+HEIGHT) * row)
                    right = START_OF_FIRST_OBJECT_X + WIDTH + ((DISTANCE_X+WIDTH) * column)
                    bottom = START_OF_FIRST_OBJECT_Y + HEIGHT + ((DISTANCE_Y+HEIGHT) * row)

                    im1 = im.crop((left, top, right, bottom))
                    image_name = "object_" + str(column + 1 + row * 5) + "_layer_" + str(layer+1) +".png"
                    im1.save("thermalImages/"+ image_name)
                    f = open("imageNames.txt", "a")
                    f.write(image_name + "\n")
                    f.close



#claudias data set A11
PADDING = 19
WIDTH = 88-(2*PADDING)
HEIGHT = 88-(2*PADDING)
START_OF_FIRST_OBJECT_X = 444+PADDING
START_OF_FIRST_OBJECT_Y = 437+PADDING

def CropClaudiaA11():




                    for layer in range(290,541):
                        #im = Image.open("Layer_Images_Claudia/Layer_" + str(layer) + ".png")


                        left = START_OF_FIRST_OBJECT_X
                        top = START_OF_FIRST_OBJECT_Y
                        right = START_OF_FIRST_OBJECT_X + WIDTH
                        bottom = START_OF_FIRST_OBJECT_Y + HEIGHT

                        #im1 = im.crop((left, top, right, bottom))
                        image_name = "object_A11_layer_" + str(layer) +".png"
                        #im1.save("thermalImagesClaudiaA11/"+ image_name)
                        f = open("imageNamesClaudiaA11.txt", "a")
                        f.write(image_name + "\n")
                        f.close


#claudias data set C4
PADDING = 19
WIDTH = 88-(2*PADDING)
HEIGHT = 88-(2*PADDING)
START_OF_FIRST_OBJECT_X = 1670+PADDING
START_OF_FIRST_OBJECT_Y = 1357+PADDING


def CropClaudiaC4():

                    for layer in range(290,541):
                        im = Image.open("Layer_Images_Claudia/Layer_" + str(layer) + ".png")


                        left = START_OF_FIRST_OBJECT_X
                        top = START_OF_FIRST_OBJECT_Y
                        right = START_OF_FIRST_OBJECT_X + WIDTH
                        bottom = START_OF_FIRST_OBJECT_Y + HEIGHT
                        im1 = im.crop((left, top, right, bottom))
                        image_name = "object_C4_layer_" + str(layer) +".png"
                        im1.save("thermalImagesClaudiaC4/"+ image_name)
                        f = open("imageNamesClaudiaC4.txt", "a")
                        f.write(image_name + "\n")
                        f.close



#claudias data set B10
PADDING = 19
WIDTH = 88-(2*PADDING)
HEIGHT = 88-(2*PADDING)
START_OF_FIRST_OBJECT_X = 901+PADDING
START_OF_FIRST_OBJECT_Y = 1008+PADDING


def CropClaudiaB10():

                    for layer in range(290,541):
                        im = Image.open("Layer_Images_Claudia/Layer_" + str(layer) + ".png")


                        left = START_OF_FIRST_OBJECT_X
                        top = START_OF_FIRST_OBJECT_Y
                        right = START_OF_FIRST_OBJECT_X + WIDTH
                        bottom = START_OF_FIRST_OBJECT_Y + HEIGHT
                        im1 = im.crop((left, top, right, bottom))
                        image_name = "object_B10_layer_" + str(layer) +".png"
                        im1.save("thermalImagesClaudiaB10/"+ image_name)
                        f = open("imageNamesClaudiaB10.txt", "a")
                        f.write(image_name + "\n")
                        f.close
     
CropClaudiaA11()
