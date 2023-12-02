import os
import sqlite3
import pandas as pd
import csv
import re
from os.path import dirname, join
from android.content import Context
import io

custom_stopwords = ["the", "and", "to", "in", "of", "a", "is", "it"]

def word_tokenize(text):
    try:
        # Remove special characters within words
        text = re.sub(r'[^\w\s]', '', text)
    except Exception as e:
        print("Error in re.sub:", e)
        print("Input text:", repr(text))  # This will print the representation of text
        text = ''  # Assign a default value if there is an error

    # Simple tokenization based on whitespace
    words = text.lower().split()
    return [word for word in words if word]



from os.path import dirname, join
from android.content import Context
import pandas as pd
import sqlite3
import io

def read_csv_content(context):
    try:
        # Construct the full path to the CSV file
        csv_file_path = join(dirname(__file__), "ShortStories.csv")
        print("______________", csv_file_path)

        # Open the CSV file using Pandas and read only the first 500 rows
        df = pd.read_csv(csv_file_path, encoding='utf-8', nrows=500)

        # Access DataFrame content (for example, print the first few rows)
        print(df.head())

        return df
    except Exception as e:
        print("Error reading CSV file:", e)
        return None

# Modify the connect_to_database function
def connect_to_database(context, content_df):
    db_path = os.path.join(str(context.getFilesDir()), "corpus.db")
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()

    # Create a table
    cursor.execute('''CREATE TABLE IF NOT EXISTS documents
                       (id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT,
                        content TEXT)''')

    # Insert data into the table
    for index, row in content_df.iterrows():
        cursor.execute('INSERT INTO documents (name, content) VALUES (?, ?)', (row['title'], row['text']))

    # Commit changes and close connection
    conn.commit()
    return conn




def create_index(conn, index_path):
    print("Index path:", index_path)

    if os.path.exists(index_path):
        print("Index file already exists.")
    else:
        c = conn.cursor()

        # Fetch all words from the documents
        c.execute('SELECT id, content FROM documents')
        data = c.fetchall()

        # Create a CSV file to store tokenized words along with doc IDs
        with open(index_path, 'w', newline='', encoding='utf-8') as csvfile:
            csv_writer = csv.writer(csvfile)
            csv_writer.writerow(['word', 'doc_id'])

            # Tokenize and store words in the CSV file
            for doc_id, content in data:
                words = word_tokenize(content)
                words = [word.lower() for word in words if word.isalpha()]

                if words:
                    for word in set(words):
                        csv_writer.writerow([word, doc_id])
                else:
                    print(f"No valid words for {doc_id}")

        print("Index file created successfully.")

import pandas as pd
from os.path import join, dirname

import pandas as pd
import sqlite3
from os.path import join, dirname

def search_documents(index_path, query, context):
    # Tokenize the query
    words = word_tokenize(query)

    # Open the CSV file using Pandas
    df = pd.read_csv(index_path)

    # Filter rows containing the words in the query
    query_data = df[df['word'].isin(words)]

    # Get unique doc_ids
    doc_ids = query_data['doc_id'].unique()
    print("Query Data:", query_data)  # Print query data for debugging

    db_path = "/data/user/0/com.midterm.storysearch/files/corpus.db"
    titles = []

    with sqlite3.connect(db_path) as conn:
        cursor = conn.cursor()

        # Fetch names (titles) from the documents table based on matching doc_ids
        sql_query = 'SELECT id, name FROM documents WHERE id IN ({})'.format(','.join(map(str, doc_ids)))
        cursor.execute(sql_query)
        result = cursor.fetchall()

        # Create a dictionary to map doc_ids to titles
        id_to_title = dict(result)

        for doc_id in doc_ids:
            print("Processing doc_id:", doc_id)

            # Retrieve the title using the dictionary
            title = id_to_title.get(doc_id)

            if title:
                titles.append(title)
            else:
                print(f"No result for doc_id {doc_id}")

    # Print the final list of DocTitles for debugging
    print("Doc Titles:", titles)

    # Return the list of DocTitles
    return titles

