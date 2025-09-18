package it.unito.bookmanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/books")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class BookController {
    private static final Logger log = LoggerFactory.getLogger(BookController.class);

    @Autowired
    private BookRepository repository;

    // GET /books - lista completa
    @GetMapping
    public List<Book> getAllBooks() {
        log.info("GET /books");
        return repository.findAll();
    }

    // GET /books/{id} - dettaglio
    @GetMapping("/{id}")
    public ResponseEntity<Book> getById(@PathVariable Long id) {
        log.info("GET /books/{}", id);
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /books - crea
    @PostMapping
    public ResponseEntity<Book> createBook(@RequestBody Book book) {
        log.info("POST /books - payload: {}", book);
        Book saved = repository.save(book);
        return ResponseEntity
                .created(URI.create("/books/" + saved.getId()))
                .body(saved);
    }

    // PUT /books/{id} - aggiorna
    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody Book book) {
        log.info("PUT /books/{} - payload: {}", id, book);
        return repository.findById(id).map(b -> {
            b.setTitle(book.getTitle());
            b.setAuthor(book.getAuthor());
            b.setIsbn(book.getIsbn());
            
            Book updated = repository.save(b);
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    // DELETE /books/{id} - elimina
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        log.warn("DELETE /books/{}", id);
        return repository.findById(id).map(b -> {
            repository.deleteById(id);
            
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}