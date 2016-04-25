#takes about 90 seconds to run

import os
import json
import sys  

reload(sys)  
sys.setdefaultencoding('utf8') #to prevent unicode issues when writing data


FOLDER = "TVs"
DELIMITER = "\t"
SEP = os.sep


def get_value(my_map, key):
	try:
		value = my_map[key].replace('\n',' ').replace('\t', ' ').replace('\\', '/')
		if value is not None:
			return value
		return "n/a"
	except Exception as e:
		return "n/a"


parent_dir = os.path.dirname(os.getcwd())

reviews = open(parent_dir + SEP + 'res' + SEP + 'vagrant' + SEP + 'reviews.txt', 'a')
products = open(parent_dir + SEP + 'res' + SEP + 'vagrant' + SEP + 'products.txt', 'a')

#convert json data files into data delimited ("\t") text files to be copied into db tables by psql in 'products.sql'
for subdir, dirs, files in os.walk(os.getcwd() + os.sep + FOLDER):

	for file in files:
		#print os.path.join(subdir, file)
		filepath = subdir + os.sep + file

		if filepath.endswith(".json"):
			#print (filepath)

			json_data = open(filepath).read()
			j_map = json.loads(json_data) #get map representation of json data for ease of access


			p_map = j_map["ProductInfo"]
			product_id = p_map["ProductID"]
			products.write(product_id + DELIMITER + get_value(p_map, "Price") + DELIMITER + get_value(p_map, "Features") + DELIMITER + get_value(p_map, "Name") + DELIMITER + get_value(p_map, "ImgURL") + " \n")

			review_list = j_map["Reviews"]
			for review in review_list:
				#print(review)
				reviews.write(get_value(review, "ReviewID") + DELIMITER + product_id + DELIMITER + get_value(review, "Title") + DELIMITER + get_value(review, "Author") + DELIMITER + get_value(review, "Overall")+ DELIMITER + get_value(review, "Date") + DELIMITER + get_value(review, "Content") + " \n")






