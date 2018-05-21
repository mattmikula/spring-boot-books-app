# spring-boot-books-app

Sample application for users to manage a collection of books via REST API. The application is behind basic authentication
and can be accessed with the following credentials: user1/password.

The application comes preloaded with a few sample records that can be used to quickly test endpoints.

### Running application
```
mvn clean spring-boot:run
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
