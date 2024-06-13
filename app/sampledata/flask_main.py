
# A very simple Flask Hello World app for you to get started with...

from flask import Flask, request, jsonify
import mysql.connector

app = Flask(__name__)

@app.route('/')
def hello_world():
    return 'Hello from Flask!'

# Connect to MySQL database
db = mysql.connector.connect(
  host="maxbranca.mysql.eu.pythonanywhere-services.com",
  user="maxbranca",
  password="K0j1m4_98",
  database="maxbranca$default"
)

cursor = db.cursor()

def check_db_connection():
    if not db.is_connected():
        db.reconnect()

@app.route('/add_user', methods=['POST'])
def add_user():
    check_db_connection()
    data = request.get_json()
    username = data['username']

    sql = "INSERT INTO scores (username, score, date_of_score) VALUES (%s, %s, CURDATE())"
    values = (username, 0)

    cursor.execute(sql, values)
    db.commit()

    return jsonify({'message': 'User added successfully'}), 201

@app.route('/update_score', methods=['PUT'])
def update_score():
    check_db_connection()
    data = request.get_json()
    username = data['username']
    score = data['score']

    sql = "UPDATE scores SET score = %s, date_of_score = CURDATE() WHERE username = %s"
    values = (score, username)

    cursor.execute(sql, values)
    db.commit()

    return jsonify({'message': 'Score updated successfully'}), 200

@app.route('/get_score', methods=['GET'])
def get_score():
    check_db_connection()
    username = request.args.get('username')

    sql = "SELECT score, date_of_score FROM scores WHERE username = %s"

    cursor.execute(sql, (username,))

    result = cursor.fetchone()

    if result:
        score, date_of_score = result
        return jsonify({'username': username, 'score': score, 'date_of_score': str(date_of_score)}), 200
    else:
        return jsonify({'message': 'User not found'}), 404

@app.route('/get_all_score', methods=['GET'])
def get_all_score():
    check_db_connection()

    sql = "SELECT * FROM scores"

    cursor.execute(sql)

    result = cursor.fetchall()

    top_scores = []
    for r in result:
        top_scores.append({"username": r[0], "maxscore": r[1], "date": r[2]})

    return jsonify(top_scores),200


if __name__ == '__main__':
    app.run()
