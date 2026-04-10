package dev.wale.movies;

import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.bson,types.ObjectId;
import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.SelectionOperators.First.first;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }
    public Review createReview(String reviewBody, String imdbId, String userId){
        Review review = new Review(reviewBody);
        review.setUserId(userId);
        review = reviewRepository.insert(review);

        mongoTemplate.update(Movie.class)
                .matching(Criteria.where("imdbId").is(imdbId))
                .apply(new Update().push("reviewIds").value(review))
                .first();
    
        return review;
    }
    public void deleteReview(String reviewId, String imdbId, String userId){
        reviewRepository.deleteById(new ObjectId(reviewId));

        mongoTemplate.update(Movie.class)
                .matching(Criteria.where("imdbId").is(imdbId))
                .apply(new Update().pull("reviewIds", Criteria.where("id").is(new ObjectId(reviewId))))
                .first();
    }
}
