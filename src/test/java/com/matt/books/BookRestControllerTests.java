package com.matt.books;

import com.matt.books.models.Account;
import com.matt.books.models.Book;
import com.matt.books.repositories.AccountRepository;
import com.matt.books.repositories.BookRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = BooksApplication.class)
@WebAppConfiguration
@ContextConfiguration
public class BookRestControllerTests {

    private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
                MediaType.APPLICATION_JSON.getSubtype(),
                Charset.forName("utf8"));

    private MockMvc mockMvc;

    private static final String usernameOne = "testUserOne";
    private static final String usernameTwo = "testUserTwo";
    private static final String password = "password";
    private static final String endpoint = "/books/";

    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    private Account accountOne;
    private Account accountTwo;

    private List<Book> bookListOne = new ArrayList<>();
    private List<Book> bookListTwo = new ArrayList<>();

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream()
                .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
                .findAny()
                .orElse(null);

        assertNotNull("the JSON message converter must not be null",
                this.mappingJackson2HttpMessageConverter);
    }

    @Before
    public void setup() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext).apply(springSecurity()).build();

        this.bookRepository.deleteAll();
        this.accountRepository.deleteAll();

        // Create account + book collection for first user
        this.accountOne = accountRepository.save(new Account(usernameOne, password, new String[]{"ROLE_USER"}));
        this.bookListOne.add(bookRepository.save(new Book(accountOne, "One", "First book")));
        this.bookListOne.add(bookRepository.save(new Book(accountOne, "Two", "Second book")));

        // Create account + book collection for second user
        this.accountTwo = accountRepository.save(new Account(usernameTwo, password, new String[]{"ROLE_USER"}));
        this.bookListTwo.add(bookRepository.save(new Book(accountTwo, "Three", "Third book")));
        this.bookListTwo.add(bookRepository.save(new Book(accountTwo, "Four", "Fourth book")));
    }

    /**
     * Ensure that a user can retrieve all of their books.
     */
    @Test
    @WithMockUser(username = usernameOne, roles = "USER")
    public void getBooks() throws Exception {
        mockMvc.perform(get(endpoint))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(this.bookListOne.get(0).getId().intValue())))
                .andExpect(jsonPath("$[0].title", is("One")))
                .andExpect(jsonPath("$[0].description", is("First book")))
                .andExpect(jsonPath("$[1].id", is(this.bookListOne.get(1).getId().intValue())))
                .andExpect(jsonPath("$[1].title", is("Two")))
                .andExpect(jsonPath("$[1].description", is("Second book")));
    }

    /**
     * Ensure that a user can retrieve one of their books.
     */
    @Test
    @WithMockUser(username = usernameOne, roles = "USER")
    public void getSingleBook() throws Exception {
        mockMvc.perform(get(endpoint
                + this.bookListOne.get(0).getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(this.bookListOne.get(0).getId().intValue())))
                .andExpect(jsonPath("$.title", is("One")))
                .andExpect(jsonPath("$.description", is("First book")));
    }

    /**
     * Ensure that a user can add a book to their collection.
     */
    @Test
    @WithMockUser(username = usernameOne, roles = "USER")
    public void addBook() throws Exception {
        String bookJson = json(new Book(
                this.accountOne, "Another Book", "New Book"));

        this.mockMvc.perform(post(endpoint)
                .contentType(contentType)
                .content(bookJson))
                .andExpect(status().isCreated());
    }

    /**
     * Ensure that a user can update a book in their collection.
     */
    @Test
    @WithMockUser(username = usernameOne, roles = "USER")
    public void updateBook() throws Exception {
        String bookJson = json(new Book(
                this.accountOne, "Updated Title", "Updated Book"));

        this.mockMvc.perform(get(endpoint
                + this.bookListOne.get(0).getId())
                .contentType(contentType)
                .content(bookJson))
                .andExpect(status().isOk());
    }

    /**
     * Ensure that one user can not retrieve a book from another user's collection.
     */
    @Test
    @WithMockUser(username = usernameTwo, roles = "USER")
    public void getWrongUserBook() throws Exception {
        this.mockMvc.perform(get(endpoint
                + this.bookListOne.get(0).getId()))
                .andExpect(status().isNotFound());
    }

    /**
     * Ensure that a user can not update a book in another user's collection.
     */
    @Test
    @WithMockUser(username = usernameTwo, roles = "USER")
    public void updateWrongUserBook() throws Exception {
        String bookJson = json(new Book(
                this.accountOne, "Updated Title", "Updated Book"));

        this.mockMvc.perform(get(endpoint
                + this.bookListOne.get(0).getId())
                .contentType(contentType)
                .content(bookJson))
                .andExpect(status().isNotFound());
    }

    /**
     * Ensure that one user can delete a book from their collection.
     */
    @Test
    @WithMockUser(username = usernameOne, roles = "USER")
    public void deleteBook() throws Exception {
        this.mockMvc.perform(delete(endpoint
                + this.bookListOne.get(0).getId()))
                .andExpect(status().isAccepted());
    }

    /**
     * Ensure that one user can not delete a book from another user's collection.
     */
    @Test
    @WithMockUser(username = usernameTwo, roles = "USER")
    public void deleteWrongUserBook() throws Exception {
        this.mockMvc.perform(delete(endpoint
                + this.bookListOne.get(0).getId()))
                .andExpect(status().isNotFound());
    }

    /**
     *  Convert a POJO to a json representation.
     *
     * @param o - POJO
     * @return json string of supplied object
     * @throws IOException
     */
    protected String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
}
