import nltk
from nltk.corpus import stopwords
from nltk.stem import WordNetLemmatizer
import json
import sys


#we use NLTK to:
#1. Tokenize - splitting the string into tokens, removing puncuation/white space
#2. Remove stop words - get rid of words like 'the' and 'at' that are common but do not give us any informational value
#3. Case fold - make all words lower case so there is no difference between "Puppy" and "puppy" when counting words
#4. Lemmatize - reduce variant forms of the same word to the same base form, so there is no difference between "puppies" and "puppy"
#Lemmatization is chosen over stemming because the words counted will be returned to the user,
#and stemming will return words stems rather than readable words


def clean_field(review):

  t_review = tokenize(review)
  c_review = case_fold(t_review)
  s_review = remove_stop_words(c_review)
  final = lemmatize(s_review)
  return final


#takes a string, returns a list
def tokenize(words):
  tokens = nltk.word_tokenize(words)
  return tokens


#takes a list, returns a list
def case_fold(word_list):
  #make everything lowercase  
  for i in range(len(word_list)):
      word_list[i] = word_list[i].lower()

  return word_list


#takes a list, returns a list
def remove_stop_words(word_list):

  stop_words = stopwords.words('english')
  stop_words = stop_words + ["!", ".", "..", "...", "?", "'s", "n't", "'ve", ",", "'", "$", "(", ")", "''", "~", "~~", "`", "``", "-", "--", "&", "{", "}", ":", ";", "\.*", "'m", "'re"]
  clean_words = [token for token in word_list if token not in stop_words]
  
  return clean_words
  

#takes a list, returns a list  
def lemmatize(word_list):
  lm = WordNetLemmatizer()

  for i in range(len(word_list)):
    word_list[i] = lm.lemmatize(word_list[i])


  return word_list

#return map, key is word, val is its count
def get_count(word_list):

  count_map = {}

  for i in range(len(word_list)):
    if word_list[i] in count_map:
      count_map[word_list[i]] += 1
    else:
      count_map[word_list[i]] = 1

  return count_map


def start():

  #need to uncomment this if running the program for the first time!
  #nltk.download('punkt')
  #nltk.download('stopwords')
  #nltk.download('wordnet')

  review = sys.argv[1]

  clean_list = clean_field(review)
  count_map = get_count(clean_list)

  #get the map into system out
  #json.dumps returns type 'str'
  #sys.stdout.write(json.dumps(count_map))
  print(json.dumps(count_map))

start() 