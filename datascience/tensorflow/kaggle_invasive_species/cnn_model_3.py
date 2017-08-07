#nvidia-smi
#~/.keras/keras.json

#import keras
#print keras.__version__
#1.2.2
#https://faroit.github.io/keras-docs/1.2.2/


from keras.models import Sequential
from keras.layers.pooling import MaxPooling2D
from keras.layers.core import Dense
from keras.layers.core import Flatten
from keras.layers.core import Dropout
from keras.layers.convolutional import Conv2D
from keras.layers.pooling import MaxPooling2D
from keras.models import model_from_json
from keras.layers.normalization import BatchNormalization

from keras.preprocessing.image import ImageDataGenerator

#check 
#from tensorflow.python.client import device_lib
#print(device_lib.list_local_devices())

#load previous model
#json_file = open('cnn_model_3.json', 'r')
#loaded_model_json = json_file.read()
#json_file.close()
#classification = model_from_json(loaded_model_json)

classification = Sequential()
classification.add(Conv2D(512, 3,3, input_shape=(128, 128, 3), activation = 'relu'))
classification.add(MaxPooling2D())

classification.add(Conv2D(512, 3,3, activation = 'relu'))
classification.add(MaxPooling2D())

classification.add(Conv2D(256, 3,3, activation = 'relu'))
classification.add(MaxPooling2D())

classification.add(Conv2D(128, 3,3, activation = 'relu'))
classification.add(MaxPooling2D())

classification.add(Conv2D(64, 3,3, activation = 'relu'))
classification.add(MaxPooling2D())


classification.add(Flatten())

classification.add(Dense(1024, activation = 'relu'))
classification.add(BatchNormalization())

classification.add(Dense(512, activation = 'relu'))
classification.add(BatchNormalization())

classification.add(Dense(256, activation = 'relu'))
classification.add(BatchNormalization())

classification.add(Dense(1, activation = 'sigmoid'))



for layer in classification.layers:
    print(str(layer.name)+" "+str(layer.input_shape)+" -> "+str(layer.output_shape))


classification.compile(optimizer='Adam', loss='binary_crossentropy', metrics = ['accuracy'])

train_data_gen = ImageDataGenerator(rotation_range=20, rescale=1./255, shear_range=0.4, zoom_range=0.1, horizontal_flip=True)
valid_data_gen = ImageDataGenerator(rotation_range=20, rescale=1./255, shear_range=0.4, zoom_range=0.1, horizontal_flip=True)

train_gen = train_data_gen.flow_from_directory('training_set', target_size=(128, 128), batch_size=25, class_mode='binary')
valid_gen = valid_data_gen.flow_from_directory('validation_set', target_size=(128, 128), batch_size=25, class_mode='binary')

#classification.load_weights("cnn_model_3.h5")
classification.fit_generator(train_gen, samples_per_epoch=2145, validation_data=valid_gen, nb_epoch=10, nb_val_samples=150)

classification_json = classification.to_json()
with open("cnn_model_3.json", "w") as json_file:
    json_file.write(classification_json)

classification.save_weights("cnn_model_3.h5")
#89.3 val accuracy


#mv test/*.jpg test/unknown/
test_data_gen = ImageDataGenerator(rescale=1./255)
test_gen = test_data_gen.flow_from_directory('test', target_size=(128, 128), batch_size=25, class_mode='binary')

prediction = classification.predict_generator(test_gen, 1531)

result = []
filenames = test_gen.filenames
for i in range(len(filenames)):
	result.append((int(filenames[i].split("/")[1].split(".")[0]), prediction[i][0]))

result.sort(key=lambda tup: tup[0])

with open("submission3.csv", "w") as output:
	output.write("name,invasive\n")
	for i in range(0, len(result)):
		output.write(str(result[i][0])+","+str(result[i][1])+"\n")

