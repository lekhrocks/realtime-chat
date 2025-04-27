# Real-Time Chat Application

A real-time chat application built with **Java**, **Spring Boot**, **GraphQL**, **WebSockets**, and **AWS AppSync**. This app allows users to authenticate, send and receive messages in real-time, and use a secure JWT-based authentication system.

## Tech Stack

- **Backend**:
    - **Spring Boot** 3.4.5 (Java 21)
    - **Spring Security** for authentication and authorization
    - **JWT (JSON Web Tokens)** for secure authentication
    - **GraphQL** for flexible data fetching (queries, mutations, and subscriptions)
    - **WebSockets** for real-time communication
    - **AWS AppSync** for managed GraphQL API with real-time updates
- **Frontend** (not included in this repository):
    - WebSocket connection handling
    - GraphQL client to interact with the API

## Features

- **User Authentication**: Users can log in with a username and password. Upon successful login, they receive a **JWT token** for authentication in subsequent requests.
- **Real-Time Messaging**: Messages are sent and received in real-time using **WebSockets**. Users can see messages instantly without refreshing the page.
- **GraphQL API**: The app uses GraphQL for querying data, sending messages (mutations), and receiving real-time message updates (subscriptions).
- **Secure Communication**: JWT tokens ensure that users are authenticated before accessing any private resources or sending messages.

## Architecture Overview

1. **Authentication**:
    - **JWT Authentication** is used for secure login. Upon successful login, a JWT token is generated and returned to the client. This token is sent with every request to authenticate the user.
2. **Real-Time Communication**:
    - The application uses **WebSockets** and **GraphQL Subscriptions** to deliver messages to connected users in real-time.
3. **GraphQL**:
    - **Queries** allow users to fetch their information.
    - **Mutations** are used to send messages.
    - **Subscriptions** provide real-time updates to users when new messages are sent.

## API Endpoints

### 1. **Authentication**
- **POST /login**: Authenticates a user and returns a JWT token.

### 2. **GraphQL Queries**
- **Query me**: Returns the current authenticated user's details (e.g., username).

  Example:
  ```graphql
  query {
    me
  }

### 3. **GraphQL Mutations**
- **Mutation message**: Sends a message to another user.

  Example:
  ```graphql
  mutation {
    message(body: "Hello, World!", to: "userB") {
        from
        to
        body
        sentAt
    }
  }
  ```

### 4. **GraphQL Subscriptions**
- **Subscription inbox**: Listens for incoming messages for a particular user.

    Example:
    ```graphql
    subscription {
        inbox(to: "userA") {
            from
            to
            body
            sentAt
        }
    }
    ```
