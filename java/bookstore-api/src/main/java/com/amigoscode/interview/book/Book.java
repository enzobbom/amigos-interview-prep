package com.amigoscode.interview.book;

import com.amigoscode.interview.author.Author;
import com.amigoscode.interview.review.Review;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    @NotBlank
    private String isbn;

    private String genre;

    @NotNull
    @Positive
    private BigDecimal price;

    @NotNull
    private LocalDate publishedDate;

    @ManyToOne
    @JoinColumn(name = "author_id")
    @JsonIgnoreProperties({"books"})
    private Author author;

    @OneToMany(
            mappedBy = "book",
            cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"book"})
    private List<Review> reviews = new ArrayList<>();

}
