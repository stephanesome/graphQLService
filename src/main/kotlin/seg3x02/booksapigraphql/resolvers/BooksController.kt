package seg3x02.booksapigraphql.resolvers

import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CrossOrigin
import seg3x02.booksapigraphql.entity.Author
import seg3x02.booksapigraphql.entity.Book
import seg3x02.booksapigraphql.repository.BookRepository
import seg3x02.booksgraphqlapi.resolvers.types.CreateBookInput
import java.util.*

@Controller
@CrossOrigin(origins = ["http://localhost:4200"])
class BooksController(private val bookRepository: BookRepository,
                      private val mongoOperations: MongoOperations
) {

    @QueryMapping
    fun books(): List<Book> {
        val list = bookRepository.findAll()
        for (bk in list) {
            bk.authors = getAuthors(bk.bookNumber)
        }
        return list
    }

    private fun getAuthors(@Argument bookNumber: Int): List<Author> {
        val query = Query()
        query.addCriteria(Criteria.where("bookNumber").`is`(bookNumber))
        return mongoOperations.find(query, Author::class.java)
    }

    @QueryMapping
    fun bookById(@Argument bookId: String): Book? {
        val book = bookRepository.findById(bookId)
        return if (book.isPresent) {
            val bk = book.get()
            bk.authors = getAuthors(bk.bookNumber)
            bk
        } else {
            null
        }
    }

    @QueryMapping
    fun bookByNumber(@Argument bookNumber: Number): Book? {
        val query = Query()
        query.addCriteria(Criteria.where("bookNumber").`is`(bookNumber))
        val result = mongoOperations.find(query, Book::class.java)
        return if (result.isNotEmpty()) {
            val bk = result[0]
            bk.authors = getAuthors(bk.bookNumber)
            bk
        } else {
            null
        }
    }

    @MutationMapping
    fun newBook(@Argument("createBookInput") input: CreateBookInput) : Book {
        if (input.bookNumber != null &&
                input.category != null &&
                input.title != null && input.cost != null) {
            val book = Book(input.bookNumber, input.category, input.title, input.cost, input.year, input.description)
            book.bookId = UUID.randomUUID().toString()
            bookRepository.save(book)
            return book
        } else {
            throw Exception("Invalid input")
        }
    }

    @MutationMapping
    fun deleteBook(@Argument("bookId") id: String) : Boolean {
        bookRepository.deleteById(id)
        return true
    }

    @MutationMapping
    fun updateBook(@Argument bookId: String, @Argument("createBookInput") input: CreateBookInput) : Book {
        val book = bookRepository.findById(bookId)
        book.ifPresent {
            if (input.bookNumber != null) {
                it.bookNumber = input.bookNumber
            }
            if (input.category != null) {
                it.category = input.category
            }
            if (input.title != null) {
                it.title = input.title
            }
            if (input.cost != null) {
                it.cost = input.cost
            }
            if (input.year != null) {
                it.year = input.year
            }
            if (input.description != null) {
                it.description = input.description
            }
            bookRepository.save(it)
        }
        return book.get()
    }
}
