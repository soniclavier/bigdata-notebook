#export PYTHONPATH=/Users/vviswanath/spark-deep-learning/target/scala-2.11/spark-deep-learning-assembly-0.1.0-spark2.1.jar
#bin/pyspark --master spark://vishnu-macbook-pro:7077 --packages databricks:tensorframes:0.2.8-s_2.11

import tensorflow as tf
import tensorframes as tfs
import sparkdl


images = sparkdl.readImages("/Users/vviswanath/mygit/learn/deep_learning/Deep Learning A-Z/Volume 1 - Supervised Deep Learning/Part 2 - Convolutional Neural Networks (CNN)/Section 8 - Building a CNN/dataset/single_prediction")

x = tfs.row(images, "image")

def resize(img, size):
	img = tf.expand_dims(img, 0)
	return tf.image.resize_bilinear(img, size)[0,:,:,:]

resize = tffunc(np.float32, np.int32)(resize)