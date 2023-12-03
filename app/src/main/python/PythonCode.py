import ast
import os
import sqlite3
import pandas as pd
import csv
import re
import nltk
from nltk.corpus import words
from nltk.stem import PorterStemmer
from os.path import dirname, join
from fuzzywuzzy import fuzz
import string

nltk.download('words')
nltk.download('punkt')
english_words = set(words.words())
porter = PorterStemmer()

def preprocess_text(text):
    try:
        text = re.sub(r'[^\w\s]', '', text)
        text = text.translate(str.maketrans('', '', string.punctuation))
    except Exception as e:
        print("Error in re.sub:", e)
        print("Input text:", repr(text))
        text = ''

    return text.lower()

def word_tokenize(text):
    text = preprocess_text(text)
    words = nltk.word_tokenize(text)
    stemmed_words = [porter.stem(word) for word in words]

    return [word for word in stemmed_words if word.isalpha() and word in english_words]

def read_csv_content(context):
    try:
        csv_file_path = join(dirname(__file__), "ShortStories.csv")
        df = pd.read_csv(csv_file_path, encoding='utf-8', nrows=500, usecols=['title', 'text'])
        print(df.head())
        return df
    except Exception as e:
        print("Error reading CSV file:", e)
        return None

def connect_to_database(context, content_df):
    db_path = os.path.join(str(context.getFilesDir()), "corpus.db")
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()

    cursor.execute('''CREATE TABLE IF NOT EXISTS documents
                       (id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT,
                        content TEXT)''')

    for index, row in content_df.iterrows():
        cursor.execute('INSERT INTO documents (name, content) VALUES (?, ?)', (row['title'], row['text']))

    conn.commit()
    return conn

def create_index(conn, index_path):
    print("Index path:", index_path)

    if os.path.exists(index_path):
        print("Index file already exists.")
    else:
        c = conn.cursor()

        c.execute('SELECT id, content FROM documents')
        data = c.fetchall()

        with open(index_path, 'w', newline='', encoding='utf-8') as csvfile:
            csv_writer = csv.writer(csvfile)
            csv_writer.writerow(['word', 'doc_ids'])

            index_data = {}

            for doc_id, content in data:
                words = word_tokenize(content)

                if words:
                    for word in set(words):
                        if word not in index_data:
                            index_data[word] = []
                        index_data[word].append(doc_id)
                else:
                    print(f"No valid words for {doc_id}")

            for word in sorted(index_data.keys()):
                csv_writer.writerow([word, index_data[word]])

        print("Index file created successfully.")

def search_documents(index_path, query, context, max_distance=15):
    words = word_tokenize(query)
    df = pd.read_csv(index_path)
    closest_matches = {}

    for word in words:
        if word in df['word'].values:
            doc_ids = ast.literal_eval(df[df['word'] == word]['doc_ids'].iloc[0])
            closest_matches[word] = doc_ids
        else:
            word_distances = [(existing_word, fuzz.token_sort_ratio(word, existing_word)) for existing_word in df['word'].values]
            closest_word, min_distance = max(word_distances, key=lambda x: x[1])

            if min_distance >= max_distance:
                doc_ids = ast.literal_eval(df[df['word'] == closest_word]['doc_ids'].iloc[0])
                closest_matches[word] = doc_ids

    doc_ids_list = [doc_id for doc_ids in closest_matches.values() for doc_id in doc_ids]
    doc_ids_list = list(set(doc_ids_list))
    doc_ids_list = [int(doc_id) for doc_id in doc_ids_list]

    db_path = "/data/user/0/com.midterm.storysearch/files/corpus.db"
    titles = []

    with sqlite3.connect(db_path) as conn:
        cursor = conn.cursor()

        sql_query = f'SELECT id, name FROM documents WHERE id IN {tuple(doc_ids_list)}'
        cursor.execute(sql_query)
        result = cursor.fetchall()

        id_to_title = dict(result)

        for doc_id in doc_ids_list:
            print("Processing doc_id:", doc_id)

            title = id_to_title.get(doc_id)

            if title:
                titles.append(title)
            else:
                print(f"No result for doc_id {doc_id}")

    print("Doc Titles:", titles)
    return doc_ids_list

def remove_story_from_database(story_name):
    conn = sqlite3.connect("/data/user/0/com.midterm.storysearch/files/corpus.db")
    cursor = conn.cursor()

    cursor.execute('SELECT id FROM documents WHERE name = ?', (story_name,))
    doc_id = cursor.fetchone()

    if doc_id:
        doc_id = doc_id[0]
        cursor.execute('DELETE FROM documents WHERE id = ?', (doc_id,))
        conn.commit()
        print(f"Story removed: {story_name}")

        index_path = "/data/user/0/com.midterm.storysearch/files/index.csv"
        create_index(conn, index_path)

        print("Index reconstructed.")
    else:
        print(f"Story not found: {story_name}")

    conn.close()
