package dev.wale.movies;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReviewControllerAuthTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getReviews_shouldBePublic() throws Exception {
        mockMvc.perform(get("/api/v1/reviews"))
                .andExpect(status().isOk());
    }

    @Test
    void postReview_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reviewBody\":\"Great!\",\"imdbId\":\"tt123\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postReview_withValidJwt_shouldSucceed() throws Exception {
        mockMvc.perform(post("/api/v1/reviews")
                        .with(jwt().jwt(jwt -> jwt.tokenValue("test-token").subject("user-123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reviewBody\":\"Great!\",\"imdbId\":\"tt123\"}"))
                .andExpect(status().is2xxSuccessful());
    }
}
