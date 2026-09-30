package com.amigoscode.interview.book;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {
    @Mock
    BookRepository bookRepository;

    @InjectMocks
    BookService underTest;

    @Test
    void getAllBooks() {
        // given
        Book book1 = mock(Book.class);
        Book book2 = mock(Book.class);

        when(bookRepository.findAll()).thenReturn(List.of(book1, book2));

        // when
        List<Book> expected = underTest.getAllBooks();

        // then
        assertThat(expected.getFirst()).isEqualTo(book1);
        assertThat(expected.getLast()).isEqualTo(book2);

        verify(bookRepository).findAll();
        verify(book1).getAuthor();
        verify(book2).getAuthor();
    }

    @Test
    void createBook() {
        // given
        Book book = mock(Book.class);

        when(bookRepository.save(book)).thenReturn(book);

        // when
        Book expected = underTest.createBook(book);

        // then
        assertThat(expected).isSameAs(book);
    }

    @Test
    void deleteBook_shouldDeleteBook() {
        // given
        Long bookId = 1L;

        // when
        underTest.deleteBook(bookId);

        // then
        verify(bookRepository).deleteById(bookId);
    }

    @Test
    void getBookById_shouldGetBookWhenIdExists() {
        // given
        Long bookId = 1L;
        Book book = new Book(1L, "Book name", "isbn something number", "Horror", BigDecimal.TEN, LocalDate.now(), null, null);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        // when
        Optional<Book> expected = underTest.getBookById(bookId);

        // then
        assertThat(expected.get()).isEqualTo(book);
    }

    @Test
    void getBookById_shouldReturnNullWhenIdDoesNotExist() {
        // given
        Long bookId = 1L;

        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // when
        Optional<Book> expected = underTest.getBookById(bookId);

        // then
        assertThat(expected.isEmpty()).isTrue();
    }
}