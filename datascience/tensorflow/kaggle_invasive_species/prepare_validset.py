import os
from shutil import copyfile
from shutil import move
import random

training_dir = 'training_set'
validation_dir = 'validation_set'
classes = ["0", "1"]

for claz in classes:
	val_size = 150
	cdir = training_dir+"/"+claz
	print(cdir)
	vcdiir = validation_dir+"/"+claz
	imgs = os.listdir(cdir)
	random.shuffle(imgs)
	for file in imgs:
		if val_size <= 0:
			break
		val_size = val_size - 1
		source_file = cdir+"/"+file
		dest_file = vcdiir+"/"+file
		print("moving "+source_file+" to "+dest_file)
		if not os.path.exists(os.path.dirname(dest_file)):
			os.makedirs(os.path.dirname(dest_file))
		move(source_file, dest_file)