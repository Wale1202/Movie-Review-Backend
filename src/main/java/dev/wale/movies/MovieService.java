package dev.wale.movies;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

import javax.management.RuntimeErrorException;

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
    public List<Movie> getMoviesByUser(String userId){
        return movieRepository.findByAddedBy(userId);
    }
    public void deleteMovie(String movieId, String userId){
        Movie movie = movieRepository.findById(new ObjectId(movieId))
            .orElseThrow(() -> new RuntimeException("Movie not found"));

        if(!userId.equals(movie.getAddedBy())){
            throw new RuntimeException("You are not authorized to delete this movie");
        }
        movieRepository.deleteById(new ObjectId(movieId));
    }
}
