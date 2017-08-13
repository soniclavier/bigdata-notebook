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
from keras.optimizers import Adam
from keras.preprocessing.image import ImageDataGenerator
from sklearn.metrics import roc_auc_score
#check 
#from tensorflow.python.client import device_lib
#print(device_lib.list_local_devices())

#load previous model
#json_file = open('cnn_model_3.json', 'r')
#loaded_model_json = json_file.read()
#json_file.close()
#classification = model_from_json(loaded_model_json)

classification = Sequential()
classification.add(Conv2D(16, 3,3, input_shape=(128, 128, 3), activation = 'relu'))
classification.add(MaxPooling2D())
classification.add(Conv2D(64, 3,3, activation = 'relu'))
classification.add(MaxPooling2D())
classification.add(Conv2D(128, 3,3, activation = 'relu'))
classification.add(MaxPooling2D())
classification.add(Conv2D(256, 3,3, activation = 'relu'))
classification.add(MaxPooling2D())
classification.add(Conv2D(512, 3,3, activation = 'relu'))
classification.add(MaxPooling2D())
classification.add(Dropout(0.25))
classification.add(Flatten())
classification.add(Dense(1024, activation = 'relu'))
classification.add(BatchNormalization())
classification.add(Dense(1, activation = 'sigmoid'))
adam_optimizer = Adam(lr=0.0001, decay=1e-6)
classification.compile(optimizer=adam_optimizer, loss='binary_crossentropy', metrics = ['accuracy'])


for layer in classification.layers:
    print(str(layer.name)+" "+str(layer.input_shape)+" -> "+str(layer.output_shape))

#train on all training set

train_all_data_gen = ImageDataGenerator(rotation_range=30, shear_range=0.2, zoom_range=0.2, horizontal_flip=True, vertical_flip=True, fill_mode='nearest', width_shift_range = 0.2,
            height_shift_range = 0.2)
train_all_gen = train_all_data_gen.flow_from_directory('training_set_all', target_size=(128, 128), batch_size=25, class_mode='binary')
classification.fit_generator(train_all_gen, samples_per_epoch=2295, nb_epoch=20)
#score 0.964

classification.fit_generator(train_all_gen, samples_per_epoch=2295, nb_epoch=10)
#score 0.978

#mv test/*.jpg test/unknown/
test_data_gen = ImageDataGenerator()
test_gen = test_data_gen.flow_from_directory('test', target_size=(128, 128), batch_size=25, class_mode='binary', shuffle=False)


prediction = classification.predict_generator(test_gen, 1531)


result = []
filenames = test_gen.filenames
for i in range(len(filenames)):
	result.append((int(filenames[i].split("/")[1].split(".")[0]), prediction[i][0]))

result.sort(key=lambda tup: tup[0])

with open("submission7_all_4.csv", "w") as output:
	output.write("name,invasive\n")
	for i in range(0, len(result)):
		output.write(str(result[i][0])+","+str(result[i][1])+"\n")


