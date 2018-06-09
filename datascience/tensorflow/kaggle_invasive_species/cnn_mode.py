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

from keras.preprocessing.image import ImageDataGenerator

#check 
#from tensorflow.python.client import device_lib
#print(device_lib.list_local_devices())

classification = Sequential()
classification.add(Conv2D(50, 3,3, input_shape=(128, 128, 3), activation = 'relu'))
classification.add(MaxPooling2D())

classification.add(Conv2D(25, 3,3, activation = 'relu'))
classification.add(MaxPooling2D())

classification.add(Flatten())

classification.add(Dense(200, activation = 'relu'))
classification.add(Dropout(0.5))

classification.add(Dense(1, activation = 'sigmoid'))

for layer in classification.layers:
    print(str(layer.name)+" "+str(layer.input_shape)+" -> "+str(layer.output_shape))


classification.compile(optimizer='Adam', loss='binary_crossentropy', metrics = ['accuracy'])

train_data_gen = ImageDataGenerator(rescale=1./255, shear_range=0.2, zoom_range=0.2, horizontal_flip=True)
valid_data_gen = ImageDataGenerator(rescale=1./255)

train_gen = train_data_gen.flow_from_directory('training_set', target_size=(128, 128), batch_size=25, class_mode='binary')
valid_gen = valid_data_gen.flow_from_directory('validation_set', target_size=(128, 128), batch_size=25, class_mode='binary')

#classification.load_weights("classification_model.h5")
classification.fit_generator(train_gen, samples_per_epoch=2145, validation_data=valid_gen, nb_epoch=25, nb_val_samples=150)

classification_json = classification.to_json()
with open("cnn_model_1.json", "w") as json_file:
    json_file.write(classification_json)
classification.save_weights("cnn_model_1.h5")
#91.3 val accuracy


#mv test/*.jpg test/unknown/
test_data_gen = ImageDataGenerator(rescale=1./255)
test_gen = test_data_gen.flow_from_directory('test', target_size=(128, 128), batch_size=25, class_mode='binary')

prediction = classification.predict_generator(test_gen, 1531)

filenames = []
for f in test_gen.filenames:
	filenames.append(int(f.split("/")[1].split(".")[0]))

filenames.sort()


