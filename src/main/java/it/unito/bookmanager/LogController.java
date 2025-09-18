package it.unito.bookmanager;

import org.springframework.web.bind.annotation.*;
import java.nio.file.*;
import java.io.IOException;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3001") // consenti al frontend React di leggere
public class LogController {

    @GetMapping("/logs")
    public List<String> getLogs() throws IOException {
        Path logFile = Paths.get("logs/spring.log"); // posizione file log
        if (!Files.exists(logFile)) {
            return List.of("Nessun log disponibile");
        }
        return Files.readAllLines(logFile);
    }
}
