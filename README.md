# spring-boot-books-app

Sample application for users to manage a collection of books via REST API. The application is behind basic authentication
and can be accessed with the following credentials: user1/password.

The application comes preloaded with a few sample records that can be used to quickly test endpoints.

### Running application
This application can be run using either spring boot or docker:
```
mvn clean spring-boot:run
```

Or

```
./mvnw install dockerfile:build
docker run -p 8080:8080 -t books/books
```

### Running tests
```
mvn clean test
```

### Example request
```
curl -u user1:password http://localhost:8080/books/
```

### Swagger UI documentation can be viewed at
http://localhost:8080/swagger-ui.html
