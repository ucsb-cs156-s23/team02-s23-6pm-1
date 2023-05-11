package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Book;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.BookRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


@Api(description = "Book")
@RequestMapping("/api/book")
@RestController
@Slf4j
public class BookController extends ApiController {

    @Autowired
    BookRepository bookRepository;

    @ApiOperation(value = "List all books")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Book> allBooks() {
        Iterable<Book> books = bookRepository.findAll();
        return books;
    }

    @ApiOperation(value = "Get a single book")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Book getById(
            @ApiParam("id") @RequestParam Long id) {
        Book books = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Book.class, id));

        return books;
    }

    @ApiOperation(value = "Create a new book")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Book postBooks(
        @ApiParam("name") @RequestParam String name,
        @ApiParam("author") @RequestParam String author,
        @ApiParam("genre") @RequestParam String genre
        )
        {

        Book books = new Book();
        books.setName(name);
        books.setAuthor(author);
        books.setGenre(genre);
        Book savedBook = bookRepository.save(books);

        return savedBook;
    }
    @ApiOperation(value = "Update a single book")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Book updateBooks(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid Book incoming) {

        Book books = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Book.class, id));


        books.setName(incoming.getName());  
        books.setAuthor(incoming.getAuthor());
        books.setGenre(incoming.getGenre());

        bookRepository.save(books);

        return books;
    }

    @ApiOperation(value = "Delete a book")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteBook(
            @ApiParam("code") @RequestParam Long id) {
        Book books = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Book.class, id));

        bookRepository.delete(books);
        return genericMessage("Book with id %s deleted".formatted(id));
    }
}
