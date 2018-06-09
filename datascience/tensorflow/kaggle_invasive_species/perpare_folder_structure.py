from shutil import copyfile
import os

with open("train_labels.csv") as labels:
	train_labels = labels.read().splitlines()

def copy(base, img, dest, claz):
	dest_file = dest+"/"+claz+"/"+img
	source_file = base+"/"+img
	if not os.path.exists(os.path.dirname(dest_file)):
		os.makedirs(os.path.dirname(dest_file))	
	copyfile(source_file, dest_file)

for i in train_labels[1:]:
	parts = i.split(",")
	img = parts[0]+".jpg"
	claz = parts[1]
	print("copying "+img+" to class "+claz)
	copy("train", img, "training_set", claz)



#copy("train", "1.jpg", "train_new", "0")

