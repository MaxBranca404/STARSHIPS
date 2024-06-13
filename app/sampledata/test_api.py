import requests

# Define the base URL of the REST API
base_url = 'https://maxbranca.eu.pythonanywhere.com'

# Function to add a user
def test_add_user():
    endpoint = '/add_user'
    params = {'username': 'test_user'}
    response = requests.post(base_url + endpoint, json=params)
    print(f'Add User Status Code: {response.status_code}')
    if response.status_code == 201:
        print('User added successfully')
    else:
        print('Error adding user')

# Function to update the user's score
def test_update_score():
    endpoint = '/update_score'
    params = {'username': 'test_user', 'score': 150}
    response = requests.put(base_url + endpoint, json=params)
    print(f'Update Score Status Code: {response.status_code}')
    if response.status_code == 200:
        print('Score updated successfully')
    else:
        print('Error updating score')

# Function to get the user's score
def test_get_score():
    endpoint = '/get_score'
    params = {'username': 'test_user'}
    response = requests.get(base_url + endpoint, params = params)
    print(f'Get Score Status Code: {response.status_code}')
    if response.status_code == 200:
        data = response.json()
        print('User score retrieved:', data)
    else:
        print('Error retrieving score')

# Function to handle non-existent user
def test_user_not_found():
    endpoint = '/get_score'
    params = {'username': 'non_test_user'}
    response = requests.get(base_url + endpoint, params = params)
    print(f'Get Non-existent User Status Code: {response.status_code}')
    if response.status_code == 404:
        print('User not found as expected')
    else:
        print('Error handling non-existent user')

# Function to get all the user's score
def test_all_user():
    endpoint = "/get_all_score"
    response = requests.get(base_url + endpoint)
    data = response.json()
    print('User score retrieved:', data)

# Run all tests
def run_tests():
    test_add_user()
    test_update_score()
    test_get_score()
    test_user_not_found()
    test_all_user()

if __name__ == '__main__':
    run_tests()
