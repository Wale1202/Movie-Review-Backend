# Movie Review App (Backend)

## Overview
The backend of the Movie Review App is built with Spring Boot and MongoDB. It serves as the API layer for the frontend, providing endpoints to fetch movie data, manage reviews, and handle user authentication (in future updates).

## Features
1. **Movie Endpoints**:
   - Fetch movie details: `/api/v1/movies`
2. **Review Endpoints**:
   - Submit reviews: `/api/v1/reviews`
   - Fetch reviews: `/api/v1/reviews/{movieId}`
3. **Upcoming Features**:
   - User authentication (Login/Register functionality).
   - Watchlist management.

## Tech Stack
- **Backend Framework**: Spring Boot
- **Database**: MongoDB (Atlas)
- **Dependencies**:
  - Lombok
  - Spring Web
  - Spring Data MongoDB
  - Spring Boot DevTools

## Installation
### Prerequisites
- [Java (JDK)](https://www.oracle.com/java/technologies/javase-downloads.html) installed
- [IntelliJ IDEA](https://www.jetbrains.com/idea/) or any other IDE
- [MongoDB Atlas](https://www.mongodb.com/atlas/database) account and a configured cluster

### Steps
1. Clone the repository:
   ```bash
   git clone <backend-repo-link>
   cd <backend-repo-folder>
   ```

2. Open the project in your IDE.

3. Configure MongoDB:
   - Update the connection URI in the `application.properties` file with your MongoDB Atlas cluster URI.
   - Example:
     ```properties
     spring.data.mongodb.uri=mongodb+srv://<username>:<password>@<cluster-url>/moviereview
     ```

4. Run the application:
   - Use the command:
     ```bash
     ./mvnw spring-boot:run
     ```
     or run the `main` method in the IDE.

5. API will be available at `https://2c4d6b77e14c.ngrok.app`.

## API Endpoints
### Movies
- `GET /api/v1/movies`: Fetches movie data.

### Reviews
- `POST /api/v1/reviews`: Submit a review.
- `GET /api/v1/reviews/{movieId}`: Fetch reviews for a specific movie.

## Database Setup
1. The database is hosted on MongoDB Atlas.
2. A JSON file containing TMDB data is attached to the repository to set up initial content.
3. Steps:
   - Create a new database and cluster in MongoDB Atlas.
   - Connect via MongoDB Compass or Mongo Shell.
   - Import the JSON file for initial movie data.

## Known Issues
- Authentication (Login/Register) is not yet implemented.
- Watchlist feature is planned for future updates.

## Contribution
Contributions are welcome! Feel free to fork the repository and submit pull requests to enhance the backend.

## License
This project is licensed under the MIT License.

## Contact
If you have any questions or suggestions, feel free to reach out via [LinkedIn](https://www.linkedin.com/in/adewale-wale-fagbodun).

## Future Enhancements
- Implement authentication and authorization.
- Add support for the watchlist feature.
- Integrate the backend with TMDB’s official API for real-time movie data.
- Add a Docker Compose file for deployment.
- Unit testing for API endpoints.

