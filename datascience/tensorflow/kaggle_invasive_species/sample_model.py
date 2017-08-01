from keras.models import Sequential
from keras.layers.pooling import MaxPooling2D
from keras.layers.core import Dense
from keras.layers.core import Flatten
from keras.layers.core import Dropout
from keras.layers.convolutional import Conv2D
from keras.layers.pooling import MaxPooling2D

from keras.preprocessing.image import ImageDataGenerator
classification = Sequential()
classification.add(Conv2D(10, (3,3), input_shape=(64, 64, 3), activation = 'relu'))

classification.add(Conv2D(10, (3,3), activation = 'relu'))
classification.add(MaxPooling2D())

classification.add(Conv2D(5, (3,3), activation = 'relu'))
classification.add(MaxPooling2D())

classification.add(Flatten())

classification.add(Dense(50, activation = 'relu'))
classification.add(Dropout(0.5))
classification.add(Dense(1, activation = 'sigmoid'))

for layer in classification.layers:
    print(str(layer.name)+" "+str(layer.input_shape)+" -> "+str(layer.output_shape))


classification.compile(optimizer='Adam', loss='binary_crossentropy', metrics = ['accuracy'])

train_data_gen = ImageDataGenerator(rescale=1./255, shear_range=0.2, zoom_range=0.2, horizontal_flip=True)
valid_data_gen = ImageDataGenerator(rescale=1./255)

train_gen = train_data_gen.flow_from_directory('sample/training_set', target_size=(64, 64), batch_size=5, class_mode='binary')
valid_gen = valid_data_gen.flow_from_directory('sample/validation_set', target_size=(64, 64), batch_size=5, class_mode='binary')

#classification.load_weights("sample_model.h5")
classification.fit_generator(train_gen, steps_per_epoch=36, validation_data=valid_gen, epochs=10, validation_steps=36)

#save the weights
classification.save_weights("sample_model.h5")



