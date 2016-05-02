import shelve

loaded_doc_data = shelve.open("src/product_corpus.db")

for key in loaded_doc_data.keys():

	print key
	print loaded_doc_data[key]
