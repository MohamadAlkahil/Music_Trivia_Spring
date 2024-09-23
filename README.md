# Music Trivia Project

## Overview

This project is a real-time multiplayer Music Trivia game built with a Spring Boot backend and a React frontend. 

## Video Demo
https://www.youtube.com/watch?v=RmIhncpdcmc

## Key Features

1. **Real-time Multiplayer**: Users can create or join game sessions and play together in real-time.
2. **JWT Authentication**: Secure authentication using JSON Web Tokens.
3. **WebSocket Communication**: Real-time updates and game state management using WebSockets.
4. **Responsive Design**: The frontend is built with React and features a responsive design for various screen sizes.
5. **RESTful API**: The backend provides a RESTful API for session management and game operations.
6. **External API Integration**: Trivia questions are fetched from an external API (OpenTrivia DB).

## Architecture
![GitHub Logo](https://github.com/MohamadAlkahil/Music_Trivia_Spring/blob/main/architecture%20diagram.png)

## Technologies Used

- Backend: Java, Spring Boot, WebSocket, JWT
- Frontend: JavaScript, React, WebSocket (SockJS & STOMP)
- Database: In-memory storage 
- Communication: RESTful API, WebSocket



### Backend (Spring Boot)

- **Controllers**: Handle HTTP requests and WebSocket messages
  - `SessionController`: Manages session creation and joining
  - `WebSocketController`: Handles real-time game events

- **Services**: Implement business logic
  - `SessionService`: Manages game sessions
  - `TriviaService`: Handles trivia game logic
  - `JwtService`: Manages JWT authentication

- **Models**: Represent data structures
  - `Session`: Represents a game session
  - `User`: Represents a player
  - `TriviaQuestion`: Represents a trivia question



### Frontend (React)

- **Components**: UI components for different game stages
  - `Home`: Landing page
  - `CreateSession`: Session creation form
  - `JoinSession`: Session joining form
  - `Lobby`: Pre-game lobby
  - `TriviaGame`: Main game component

- **WebSocket Manager**: Handles real-time communication with the server
