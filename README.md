# Soundtrack Recommender

Soundtrack Recommender is a Java console application that recommends movie soundtrack tracks based on a selected mood using the Spotify Web API.

## Description

Soundtrack Recommender is designed to help users discover soundtrack-style music based on mood categories such as action, romantic, epic, drama, and mystery.

The application connects to the Spotify Web API, searches for soundtrack-related tracks, applies a rule-based scoring algorithm, and displays the most relevant recommendations in the console.

The project demonstrates API integration, environment-based configuration, HTTP requests, JSON parsing, Java collections, and basic recommendation logic.

## Tech Stack

- Java 22
- Maven
- Spotify Web API
- Gson
- Java HTTP Client
- Java Collections
- Rule-based recommendation logic

## Features

- Mood-based soundtrack recommendations
- Spotify Web API integration
- Console-based user interaction
- Rule-based track scoring
- Search by soundtrack-related keywords
- Spotify track links in recommendation results
- Environment variable based API configuration
- Maven-based project structure

## Project Structure

```text
soundtrack-recommender/
│
├── README.md
├── LICENSE
├── .gitignore
├── pom.xml
│
└── src/
    └── main/
        └── java/
            ├── sra.java
            │
            └── org/
                └── example/
                    └── Main.java
```

## Main Files

### `sra.java`

Contains the main recommendation logic of the application.

Main responsibilities:

- Reads Spotify API credentials from environment variables
- Displays mood options in the console
- Accepts user input
- Searches tracks using the Spotify Web API
- Scores results based on mood profiles
- Displays recommended soundtrack tracks

### `org/example/Main.java`

Contains a basic starter class generated with the Maven project structure.

The main recommendation flow is implemented in `sra.java`.

## Setup Instructions

### 1. Clone the repository

```bash
git clone https://github.com/Seyidli06/soundtrack-recommender.git
```

### 2. Open the project folder

```bash
cd soundtrack-recommender
```

### 3. Compile the project

```bash
mvn clean compile
```

### 4. Configure Spotify API credentials

The application requires Spotify API credentials to access the Spotify Web API.

Required environment variables:

```env
SPOTIFY_CLIENT_ID=your_spotify_client_id
SPOTIFY_CLIENT_SECRET=your_spotify_client_secret
```

Real Spotify credentials should be configured locally and should not be committed to the repository.

### 5. Run the application

The project can be run from IntelliJ IDEA by starting:

```text
sra.java
```

It can also be run from the terminal with Maven:

```bash
mvn exec:java -Dexec.mainClass="sra"
```

## Environment Variables

Sensitive configuration values are provided through environment variables.

```env
SPOTIFY_CLIENT_ID=your_spotify_client_id
SPOTIFY_CLIENT_SECRET=your_spotify_client_secret
```

These values are required for Spotify Web API authentication.

Real API credentials should not be written directly in the source code, README file, or committed to GitHub.

## Usage

After running the application, the console displays available mood categories.

Example mood selection flow:

```text
=== Soundtrack Recommender (Block-Safe) ===
Choose a mood:
[1] ACTION
[2] ROMANTIC
[3] EPIC
[4] DRAMA
[5] MYSTERY
Selection:
```

After selecting a mood, the application asks how many recommendations should be returned:

```text
How many?
```

The application then searches Spotify and displays matching soundtrack recommendations.

Example output format:

```text
Recommendations:
1) Track Name — Artist Name | album: Album Name | pop: 75 | Spotify URL
2) Track Name — Artist Name | album: Album Name | pop: 68 | Spotify URL
```

## Available Moods

```text
ACTION
ROMANTIC
EPIC
DRAMA
MYSTERY
```

## Recommendation Logic

The recommendation system uses a rule-based scoring approach.

The application searches Spotify using soundtrack-related keywords and mood-specific terms. Each result receives a score based on:

- Mood-related keywords
- Soundtrack-related words such as score, theme, OST, cinematic, and orchestral
- Mood-specific seed artists
- Track popularity
- Negative keyword filtering

The highest-scoring tracks are displayed as recommendations.

## External API Integration

This project uses the Spotify Web API.

Main external API usage:

```text
Spotify Accounts API
Spotify Search API
```

Authentication is handled using Spotify client credentials.

Required credentials:

```text
SPOTIFY_CLIENT_ID
SPOTIFY_CLIENT_SECRET
```

## Screenshots

This is a console-based Java application. Screenshots can be added later to demonstrate the application startup, mood selection, and recommendation output.

Recommended screenshot examples:

```text
application-start.png
mood-selection.png
recommendation-results.png
```

Screenshots should not include Spotify client IDs, client secrets, access tokens, or other sensitive information.

## Project Status

This project is a demo application and is currently under development.

Planned improvements:

- Refactor source code into separate packages and classes
- Improve error handling
- Add more mood categories
- Improve recommendation scoring
- Add clearer console output formatting
- Add unit tests
- Add screenshots and example outputs

## Repository Standards

This repository includes:

- `README.md`
- Clear folder structure
- Meaningful commit history
- MIT License
- `.gitignore`

## Commit Message Examples

```text
feat: add Spotify API client
feat: add mood-based recommendation logic
feat: add console input flow
fix: handle invalid mood selection
refactor: separate Spotify client and recommendation service
docs: update README documentation
chore: update gitignore rules
```

## Author

Adil Seyidli

## License

This project is licensed under the MIT License.
