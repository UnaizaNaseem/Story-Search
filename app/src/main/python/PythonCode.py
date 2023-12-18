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
from fuzzywuzzy import process
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
        df = pd.read_csv(csv_file_path, encoding='utf-8', nrows=3100, usecols=['title', 'text'])
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


def search_documents(index_path, query, context, max_distance=15, min_similarity=70):
    ranked_doc_ids = rank_documents(index_path, query, context, max_distance, min_similarity)
    return ranked_doc_ids

def rank_documents(index_path, query, context, max_distance=15, min_similarity=70, max_results=20):
    words = word_tokenize(query)
    df = pd.read_csv(index_path)
    document_scores = {}

    for word in words:
        if word in df['word'].values:
            doc_ids = ast.literal_eval(df[df['word'] == word]['doc_ids'].iloc[0])
            for doc_id in doc_ids:
                document_scores[doc_id] = document_scores.get(doc_id, 0) + 1

    # If no exact matches, try fuzzy matching
    if not document_scores:
        fuzzy_matches = find_fuzzy_matches(words, df['word'].values, min_similarity)
        for match, similarity in fuzzy_matches:
            doc_ids = ast.literal_eval(df[df['word'] == match]['doc_ids'].iloc[0])
            for doc_id in doc_ids:
                document_scores[doc_id] = document_scores.get(doc_id, 0) + similarity

    sorted_documents = sorted(document_scores.items(), key=lambda x: x[1], reverse=True)
    ranked_doc_ids = [doc_id for doc_id, _ in sorted_documents]

    # Return only the first 20 results if there are more than 20
    return ranked_doc_ids[:max_results]


def find_fuzzy_matches(words, candidates, min_similarity):
    matches = []

    for word in words:
        match, similarity = process.extractOne(word, candidates)
        if similarity >= min_similarity:
            matches.append((match, similarity))

    return matches


def get_document_titles(context, doc_ids):
    db_path = os.path.join(str(context.getFilesDir()), "corpus.db")
    titles = []

    with sqlite3.connect(db_path) as conn:
        cursor = conn.cursor()

        sql_query = f'SELECT id, name FROM documents WHERE id IN {tuple(doc_ids)}'
        cursor.execute(sql_query)
        result = cursor.fetchall()

        id_to_title = dict(result)

        for doc_id in doc_ids:
            title = id_to_title.get(doc_id)
            if title:
                titles.append(title)

    return titles

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
