package com.matt.books.repositories;

import com.matt.books.models.Book;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.Optional;

public interface BookRepository extends CrudRepository<Book, Long> {
    Collection<Book> findByAccountUsername(String username);
    Optional<Book> findByAccountUsernameAndId(String username, Long id);
}
