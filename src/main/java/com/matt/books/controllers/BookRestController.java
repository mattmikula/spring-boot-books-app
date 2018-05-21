package com.matt.books.controllers;

import com.matt.books.exceptions.BookNotFoundException;
import com.matt.books.models.Book;
import com.matt.books.repositories.AccountRepository;
import com.matt.books.repositories.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collection;


@RestController
@RequestMapping("/books")
public class BookRestController {

    private final BookRepository bookRepository;
    private final AccountRepository accountRepository;

    @Autowired
    BookRestController(BookRepository bookRepository,
                       AccountRepository accountRepository) {
        this.bookRepository = bookRepository;
        this.accountRepository = accountRepository;
    }

    /**
     * Return a list of books for the authorized user.
     *
     * @param principal - authorized user
     */
    @RequestMapping(method = RequestMethod.GET)
    public Collection<Book> getBooks(Principal principal) {
        return this.bookRepository.findByAccountUsername(principal.getName());
    }

    /**
     * Accept JSON data from authorized user to create a book and add to their collection
     *
     * @param principal - authorized user
     * @param input - JSON data used to create a book instance
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> addBook(Principal principal, @RequestBody Book input) {
        return this.accountRepository
                .findByUsername(principal.getName())
                .map(account -> {
                    this.bookRepository.save(new Book(account, input.getTitle(), input.getDescription()));
                    return new ResponseEntity<>("Successfully created resource", HttpStatus.CREATED);
                })
                .orElse(new ResponseEntity<>("Error creating resource", HttpStatus.BAD_REQUEST));
    }

    /**
     * Accept JSON data from authorized user to update a book in their collection
     *
     * Users can only update books in their own collection
     *
     * @param principal - authorized user
     * @param input - JSON data used to update a book instance
     * @param bookId - id of book to update
     */
    @RequestMapping(method = RequestMethod.PUT, value="/{bookId}")
    public ResponseEntity<?> updateBook(Principal principal, @RequestBody Book input, @PathVariable Long bookId) {
        return this.bookRepository.findByAccountUsernameAndId(principal.getName(), bookId)
                .map(book -> {
                    book.setTitle(input.getTitle());
                    book.setDescription(input.getDescription());
                    this.bookRepository.save(book);
                    return new ResponseEntity<>("Resource updated successfully", HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>("Error updating resource", HttpStatus.BAD_REQUEST));
    }

    /**
     * Return a book from a user's collection
     *
     * Users can only retrieve books in their own collection
     *
     * @param principal - authorized user
     * @param bookId - id of book to retrieve
     */
    @RequestMapping(method = RequestMethod.GET, value = "/{bookId}")
    public Book getBook(Principal principal, @PathVariable Long bookId) {
        return this.bookRepository.findByAccountUsernameAndId(principal.getName(), bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId.toString()));
    }

    /**
     * Delete a book from a user's collection
     *
     * Users can only delete books in their own collection
     *
     * @param principal - authorized user
     * @param bookId - id of book to delete
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/{bookId}")
    ResponseEntity<?> deleteBook(Principal principal, @PathVariable Long bookId) {
        return this.bookRepository.findByAccountUsernameAndId(principal.getName(), bookId)
                .map(book -> {
                    this.bookRepository.delete(book);
                    return new ResponseEntity<>("Resource deleted successfully", HttpStatus.ACCEPTED);
                }).orElseThrow(() -> new BookNotFoundException(bookId.toString()));
    }
}