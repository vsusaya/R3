#This creates an inverted index, where the key is a term, and the values are a list of product IDs

from LanguageProcessor import clean_field
import sys
import shelve

#takes a list of list of field values, and returns a single list without duplicates among those sub lists
def consolidate_list(word_list):
  
  final_list = []
  flag = True
  for sublist in word_list:    
    
    if flag:
      my_set = set(sublist)
      final_list = final_list + list(my_set)
      flag = False
    else:
      final_list = final_list + sublist

  return final_list


#this returns a one-dimensional list containing all words  
def single_level(word_list):

  final_list = []
  
  for i in range(len(word_list)):
    final_list.append(word_list[i])
      
  my_list = list(set(final_list))

  return final_list 


#Call this to create the shelved dictionary
def start():

  #need to uncomment this if running the program for the first time!
  #nltk.download('punkt')


  map = shelve.open("product_corpus.db")


  #map = load_json("2015_movies.json")
	
  #the inverted index
  shelved_dict = {}
  
  #loop over every key and get the value (dict)
  #for every product id
  for key in map.keys():
    dict = map[key]
    
    temp_list = []
    #for the fields in the document (name, features)
    for field in dict.keys():

      value = dict[field]
      final = clean_field(value)
      temp_list.append(final)
      
    temp_list = consolidate_list(temp_list)
    
    #convert temp_list into a one-dimensional list (since temp_list contains two lists)
    last_list = single_level(temp_list)
         

    #loop through list and add this doc id to the inverted index
    for word in last_list:
      #if the word is not yet in the inverted index, add the key, with the value being a list
      clean_key = str(key).replace(":","")
      
      if str(word.encode('utf-8')) in shelved_dict:
        #checks to make sure the same doc number is not in postings twice      
        if not (clean_key in shelved_dict[str(word.encode('utf-8'))]):
          shelved_dict[str(word.encode('utf-8'))].append(clean_key) 
          
      else:
        #if the word isn't in the index, just append the doc id to the value for this key
        shelved_dict[str(word.encode('utf-8'))] = [clean_key] 
    
      
  #after having created the index, loop through dict keys and sort the doc IDs for optimization when performing conjunctive queries
  all_keys = shelved_dict.keys()
  for i in range(len(all_keys)):
    shelved_dict[all_keys[i]].sort()
  
  actual_shelved = shelve.open("product_index.db")
  actual_shelved.update(shelved_dict)
  
reload(sys)  
sys.setdefaultencoding('utf8')  
start()
