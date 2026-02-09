package dev.wale.movies;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@Service
public class MovieService {
    @Autowired
    private MovieRepository movieRepository;
    public List<Movie> allMovies(){
        return movieRepository.findAll();
    }
   public Optional<Movie> singleMovie(String imdbId){
        return movieRepository.findMovieByImdbId(imdbId);
   }
    public Movie addMovie(Movie movie){
        Optional<Movie> exists = movieRepository.findMovieByTitle( movie.getTitle());

        if(exists.isPresent()){
            throw new Error("Invalid title");
        }

        return movieRepository.save(movie);
    }
}
