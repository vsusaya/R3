#Search method to be used by the exploration window
#takes in a list of search terms
#performs *conjunctive* search (AND)
#returns a list of product IDs

import sys
import shelve
from LanguageProcessor import clean_field

def search(query):

  loaded_dict = shelve.open("src/product_index.db") #need to add src/ since the java program is running this script
  	
  #clean the words in the same way the index was created
  normalized_query = clean_field(" ".join(query))

  clean_query = []
  for i in range(len(normalized_query)):
    clean_query.append(str(normalized_query[i].encode('utf-8')))	
	
  results_list = []	
  for term in clean_query:
    if term in loaded_dict:
      #append a list containing the list of doc IDs, and length of that list
      results_list.append([loaded_dict[term], len(loaded_dict[term])])
	

  #optimization: now sort results_list based on the length of the doc ID list	
  results_list.sort(key=lambda x: x[1])
  
  #now get the intersection of the lists
  final_list = []
  for i in range(len(results_list)):
    
    if i != len(results_list) - 1:
      final_list = [val for val in results_list[i][0] if val in results_list[i+1][0]]
    elif len(results_list) == 1:
      final_list = results_list[i][0]
      break
         
  #now we have the list of matching doc ids
  #so look up these doc ids in the doc data shelve
  loaded_doc_data = shelve.open("src/product_corpus.db")
  return_map = {}
  for key in final_list:
    doc_map = loaded_doc_data[str(key)]
    try:
      title = str(doc_map["name"]) + ": "
      #if len(title) > 40:
      #  title = title[:40] + "..."
    except Exception as e:
      title = "Name Not Found: "
    try:
      desc = str(doc_map["features"])
      #if len(desc) > 40:
      #  desc = desc[:40] + "..."
    except Exception as e:
      desc = "..."
    return_map[key] = {"name": title, "features": desc}
      
  if len(return_map.keys()) > 0:
    print return_map
  else:
    print 'Could not find the query '+" ".join(query)+'. Sorry!'


terms = sys.argv[1:]
search(terms)    