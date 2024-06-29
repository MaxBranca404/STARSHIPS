# Starship Game App

## Overview

Starship is a space shooting game setted in a 3D environment. The game features a user registration system, highscore leaderboard, and a dynamic VR environment. Scores are stored online to maintain a global leaderboard.

## Features

- **User Registration**: Register and log in with a unique username.
- **Highscore Leaderboard**: View and compete on a global leaderboard.
- **VR Environment**: Navigate and interact in a virtual space environment.
- **Sound Effects**: Engaging soundtrack and sound effects for a better gaming experience.

## Main Components

### `MainActivity`

The main entry point of the app. Handles user registration, navigation between screens, and media playback.

- **User Registration**: Prompts for username if not registered.
- **Menu Navigation**: Provides navigation to game start, highscore, and environment selection screens.
- **Media Playback**: Manages background soundtrack and button click sounds.

### `UserRegistrationScreen`

Composable function to display a registration dialog for new users.

### `MenuScreen`

Composable function to display the main menu with options to start the game, view highscore, and select environment.

### `HighscoreScreen`

Composable function to display the top 10 highscores. Fetches data from the server and sorts scores in descending order.

### `SelectionScreen`

Composable function to allow users to select a VR environment before starting the game.

### `VRScreen`

Composable function for the main gameplay. Handles 3D rendering, shooting mechanics, and collision detection.

### Models and Data Classes

- **Bullet**: Holds bullet node and its direction.
- **Explosion**: Holds explosion node and frame count.
- **ScoreEntry**: Holds username, score, and date for highscores.

## Key Functions

### `checkUser`

Checks if a user is already registered. If not, it registers the user.

### `registerUser`

Registers a new user with the server.

### `getAllScore`

Fetches all scores from the server.

### `getScore`

Fetches the score of a specific user.

### `updateScore`

Updates the score of a user on the server.

## Installation and Setup

1. **Clone the Repository**:
    ```sh
    https://github.com/MaxBranca404/MACCProj.git
    ```

2. **Open in Android Studio**:
    Open the cloned repository in Android Studio.

3. **Build the Project**:
    Build the project to resolve dependencies.

4. **Run the App**:
    Connect an Android device or start an emulator and run the app from Android Studio.

## Dependencies

Ensure to include necessary dependencies in your `build.gradle` file:
- **Retrofit** for network operations.
- **Jetpack Compose** for UI components.
- **Filament** for 3D rendering.
- **MediaPlayer** for sound effects.

## Server Setup

The app interacts with an online server to store and retrieve user data and scores. Ensure the server is set up and running with appropriate endpoints for user registration and score handling.
