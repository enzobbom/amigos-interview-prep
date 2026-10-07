package com.amigoscode.interview.book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    // if all filters are null, will intentionally result in the same as findAll()
    @Query("""
            SELECT b
            FROM Book b
            WHERE (:title IS NULL OR LOWER(b.title) = LOWER(:title))
              AND (:genre IS NULL OR LOWER(b.genre) = LOWER(:genre))
              AND (:authorName IS NULL OR LOWER(b.author.name) LIKE LOWER(CONCAT('%', :authorName, '%')))
            """)
    List<Book> findBooksByFilters(
            @Param("title") String title,
            @Param("genre") String genre,
            @Param("authorName") String authorName
    );
}
