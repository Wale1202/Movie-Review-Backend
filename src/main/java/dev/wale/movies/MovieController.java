package dev.wale.movies;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/movies")
public class MovieController {
    @Autowired
    private MovieService movieService;
    @GetMapping
    public ResponseEntity<List<Movie>> getAllMovies(){
        return new ResponseEntity<List<Movie>>(movieService.allMovies(), HttpStatus.OK);
    }

    @GetMapping("/{imdbId}")
    public ResponseEntity<Optional<Movie>> getSingleMovie(@PathVariable String imdbId){
        return new ResponseEntity<Optional<Movie>>(movieService.singleMovie(imdbId), HttpStatus.OK);
    }


    @PostMapping
    public ResponseEntity<Object> createMovie(@RequestBody Movie movie) {
        try {
            Movie savedMovie = movieService.addMovie(movie);
            return new ResponseEntity<>(savedMovie, HttpStatus.CREATED);
        }catch(ResponseStatusException e){
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(errorResponse);

        }
        //return new ResponseEntity<>(movieService.addMovie(movie), HttpStatus.CREATED);
    }

}
