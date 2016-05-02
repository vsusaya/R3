#take products.txt that was created (columns delimited by '\t' and rows by '\n')
#want columns 1 (product id) , 3 (features), and 4 (name) (total of 5 columns) (not 0 indexed)
#Creates a shelve to store all keys (documents = products) and their values (features, name)
#this is needed to create an inverted index in ProductIndex.py

import os
import shelve

SEP = os.sep

parent_dir = os.path.dirname(os.getcwd())

products_file = open(parent_dir + SEP + 'res' + SEP + 'vagrant' + SEP + 'products.txt', 'r')


product_corpus = {}

for line in products_file:
	fields = line.split("\t")
	product_corpus[fields[0]] = {"name": fields[2], "features": fields[3]}

product_shelve = shelve.open("product_corpus.db")
product_shelve.update(product_corpus)