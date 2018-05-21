package com.matt.books;

import com.matt.books.models.Account;
import com.matt.books.models.Book;
import com.matt.books.repositories.AccountRepository;
import com.matt.books.repositories.BookRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;


@SpringBootApplication
public class BooksApplication {

	public static void main(String[] args) {
		SpringApplication.run(BooksApplication.class, args);
	}


    /**
     * Create some sample data to use when interacting with the application.
     */
	@Bean
	CommandLineRunner init(AccountRepository accountRepository,
						   BookRepository bookRepository) {
		return (evt) -> Arrays.asList(
			"user1,user2,user3".split(","))
			.forEach(
				a -> {
					Account account = accountRepository.save(new Account(a, "password", new String[]{"ROLE_USER"}));
					bookRepository.save(new Book(account, "Pragmatic Programmer", "A description"));
					bookRepository.save(new Book(account, "Building Microservices", "A description"));
				});
	}

}