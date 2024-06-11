from flask import Flask, request, jsonify
import mysql.connector
#import MySQLdb


#from OpenSSL import SSL
#context = SSL.Context(SSL.TLS_server_method())
#context.use_privatekey_file('server.key')
#context.use_certificate_file('server.crt')

app = Flask(__name__)

# Connect to MySQL database




db = mysql.connector.connect(
    host="mikitronti.mysql.eu.pythonanywhere-services.com",
    user="mikitronti",
    password="K0j1m4_98",
    database="mikitronti$default"
)

cursor = db.cursor()


def check_db_connection():
    if not db.is_connected():
        db.reconnect()
        #cursor = db.cursor()



#test Route
@app.route('/')
def hello_world():
    return 'Hello from Flask!'

# Route to insert data into score table
@app.route('/insert_score', methods=['POST'])
def insert_score():
    check_db_connection()
    data = request.get_json()
    name = data['name']

    if not sanity_check(str(name)):
        return jsonify({"message": "Insert a valid input!"})

    score = data['score']

    if not sanity_check(str(score)):
        return jsonify({"message": "Insert a valid input!"})

    sql = "INSERT INTO score (userid, maxscore, date) VALUES (%s, %s, CURDATE())"
    values = (name, score)


    cursor.execute(sql, values)


    db.commit()

    return jsonify({"message": "Score inserted successfully"})

# Route to insert data into users table
@app.route('/insert_user', methods=['POST'])
def insert_user():
    check_db_connection()
    data = request.get_json()
    username = data['username']
    #sanity_ceck(username)
    if not sanity_check(username):
        return jsonify({"message": "Insert a valid input!"})

    sql = "INSERT INTO users (username) VALUES (%s)"

    cursor.execute(sql, (username,))


    db.commit()

    return jsonify({"message": "User inserted successfully"})

#Route to update an highscore
@app.route('/update_max_score', methods=['POST'])
def update_max_score():
    check_db_connection()
    data = request.get_json()
    username = data['username']
    if not sanity_check(username):
        return jsonify({"message": "Insert a valid input!"})
    maxscore = data['maxscore']
    if not sanity_check(str(maxscore)):
        return jsonify({"message": "Insert a valid input!"})
    sql = "UPDATE score SET maxscore = %s WHERE userid = %s"
    cursor.execute(sql, (maxscore, username))
    db.commit()

    return jsonify({"message": "Max score updated successfully"})

# Route to retrieve top 10 scores
@app.route('/top_scores', methods=['GET'])
def top_scores():
    check_db_connection()
    sql = "SELECT * FROM score ORDER BY maxscore DESC LIMIT 10"
    cursor.execute(sql)
    scores = cursor.fetchall()

    top_scores = []
    for score in scores:
        top_scores.append({"username": score[0], "maxscore": score[1]})

    return jsonify(top_scores)

@app.route('/get_maxscore', methods=['GET'])
def get_maxscore():
    check_db_connection()
    #data = request.get_json()
    #username = data['username']
    username = request.args.get('username')
    if not sanity_check(username):
        return jsonify({"message": "Insert a valid input!"})
    sql = "SELECT userid FROM users WHERE username = %s"
    cursor.execute(sql,(username,))
    userid = cursor.fetchone()

    # Check if username exists in users table
    if not userid:
        return jsonify({'error': 'Username not found'})

    # Get maxscore for the user
    sql = "SELECT maxscore FROM score WHERE userid = %s"
    cursor.execute(sql, (userid[0],))
    maxscore = cursor.fetchone()

    return jsonify({'username': username, 'maxscore': maxscore[0]})

#get user userid
@app.route('/get_userid', methods=['GET'])
def get_userid():
    check_db_connection()
    #data = request.get_json()
    #username = data['username']
    username = request.args.get('username')
    if not sanity_check(username):
        return jsonify({"message": "Insert a valid input!"})
    sql = "SELECT userid FROM users WHERE username = %s"
    cursor.execute(sql,(username,))
    userid = cursor.fetchone()
    return jsonify({'userid': userid[0]})


# Route to delete a user by id
@app.route('/delete_user/<userid>', methods=['DELETE'])
def delete_user(userid):
    check_db_connection()
    if not sanity_check(str(userid)):
        return jsonify({"message": "Insert a valid input!"})
    sql = "DELETE FROM users WHERE userid = %s"
    cursor.execute(sql, (userid,))
    db.commit()
    return jsonify({"message": "User deleted successfully"})


#Route to delete a score by user id
@app.route('/delete_score/<userid>', methods=['DELETE'])
def delete_score(userid):
    check_db_connection()
    if not sanity_check(str(userid)):
        return jsonify({"message": "Insert a valid input!"})
    sql = "DELETE FROM score WHERE userid = %s"
    cursor.execute(sql, (userid,))
    db.commit()
    return jsonify({"message": "Score deleted successfully"})


def sanity_check(s):
    if "'" in s or ";" in s or "--" in s:
        return False
    return True


if __name__ == '__main__':
    #app.run( ssl_context=context)
    app.run()
