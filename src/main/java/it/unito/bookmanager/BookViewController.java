package it.unito.bookmanager;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class BookViewController {

    private final BookRepository bookRepository;

    public BookViewController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("books", bookRepository.findAll());
        return "index";
    }

    @PostMapping("/add")
    public String addBook(
            @RequestParam String title,
            @RequestParam String author,
            @RequestParam String isbn) {
        bookRepository.save(new Book(null, title, author, isbn));
        return "redirect:/";
    }

    @PostMapping("/update/{id}")
    public String updateBook(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String author,
            @RequestParam String isbn) {

        bookRepository.findById(id).ifPresent(book -> {
            book.setTitle(title);
            book.setAuthor(author);
            book.setIsbn(isbn);
            bookRepository.save(book);
        });
        return "redirect:/";
    }

    @PostMapping("/delete/{id}")
    public String deleteBook(@PathVariable Long id) {
        bookRepository.deleteById(id);
        return "redirect:/";
    }
}
