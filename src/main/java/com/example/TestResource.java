package com.example;

import static java.time.Month.JUNE;
import static java.time.Month.MAY;
import static org.hibernate.reactive.mutiny.Mutiny.fetch;

import java.time.LocalDate;
import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.resteasy.reactive.RestPath;

import io.smallrye.mutiny.Uni;

/**
 * Demonstrates the use of Hibernate Reactive with the
 * {@link io.smallrye.mutiny.Uni Mutiny}-based API.
 */
@ApplicationScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/test")
public class TestResource {

	@Inject
	Mutiny.Session session;

	@POST
	@Path("/failing")
	public Uni<Author> createAuthor() {
		final Author author = new Author("Neal Stephenson");
		final Book book1 = new Book("0-380-97346-4", "Cryptonomicon", author, LocalDate.of(1999, MAY, 1));
		final Book book2 = new Book("0-553-08853-X", "Snow Crash", author, LocalDate.of(1992, JUNE, 1));
		author.getBooks().add(book1);
		author.getBooks().add(book2);

		return session.withTransaction(
				// persist the Authors with their Books in a transaction
				tx -> session.persist( author )
		).chain(ignore -> session.find( Author.class, author.getId() ));
	}

	@GET
	@Path("/failing/{authorId}")
	public Uni<Collection<Book>> getBooks(@RestPath
	final Integer authorId) {
		return session.find( Author.class, authorId )
				// lazily fetch their books
				.chain( author -> fetch(author.getBooks()));
	}

	@POST
	@Path("/working")
	public Uni<Collection<Book>> createAuthorAndGetBooks() {
		final Author author = new Author("Neal Stephenson");
		final Book book1 = new Book("0-380-97346-4", "Cryptonomicon", author, LocalDate.of(1999, MAY, 1));
		final Book book2 = new Book("0-553-08853-X", "Snow Crash", author, LocalDate.of(1992, JUNE, 1));
		author.getBooks().add(book1);
		author.getBooks().add(book2);

		return session.withTransaction(
				// persist the Authors with their Books in a transaction
				tx -> session.persist( author )
		).chain(ignore -> session.find( Author.class, author.getId() ))
				// lazily fetch their books
				.chain( a -> fetch(a.getBooks()));
	}
}
