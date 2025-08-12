package dev.wale.movies;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;

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
        System.out.println(movieService.allMovies().size());
        return new ResponseEntity<List<Movie>>(movieService.allMovies(), HttpStatus.OK);
    }

    @GetMapping("/{imdbId}")
    public ResponseEntity<Optional<Movie>> getSingleMovie(@PathVariable String imdbId){
        return new ResponseEntity<Optional<Movie>>(movieService.singleMovie(imdbId), HttpStatus.OK);
    }


    @PostMapping
    public ResponseEntity<Movie> createMovie(@Valid @RequestBody MovieRequest movieRequest) {
            Movie movie = new Movie();
            movie.setTitle(movieRequest.getTitle());
            movie.setDescription(movieRequest.getDescription());
            movie.setYear(movieRequest.getYear());
            movie.setGenres(movieRequest.getGenres());
            movie.setPoster(movieRequest.getPoster());

            Movie savedMovie = movieService.addMovie(movie);
            return new ResponseEntity<>(savedMovie, HttpStatus.CREATED);

        }
        //return new ResponseEntity<>(movieService.addMovie(movie), HttpStatus.CREATED);
    }


