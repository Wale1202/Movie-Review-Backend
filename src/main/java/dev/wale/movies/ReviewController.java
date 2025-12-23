package dev.wale.movies;

import org.apache.catalina.util.ErrorPageSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Map;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {
    @Autowired
    private ReviewService reviewService;


    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        // Delegate to ReviewService
        List<Review> reviews = reviewService.getAllReviews();
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }
    @PostMapping
    public ResponseEntity<Review> createReview(@RequestBody Map<String, String> payload, Jwt jwt){
        String userId = jwt.getSubject();
        return new ResponseEntity<Review>(reviewService.createReview(payload.get("reviewBody"), payload.get("imdbId"), userId), HttpStatus.CREATED);
    }
}
