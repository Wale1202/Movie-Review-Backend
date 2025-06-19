package dev.wale.movies;
import jakarta.validation.constraints.*;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
public class MovieRequest {
    @NotBlank(message = "Title is required")
    private String title;
    @NotBlank(message = "Description is required")
    private String description;
    @NotNull(message = "Year is required")
    @Min(value = 1900, message = "Year must not be before 1900")
    @Max(value = 2025, message = "Year cannot be in the future")
    private Integer year;
    @NotEmpty(message = "At least one genre is required")
    private List<@Pattern(regexp = "Action|Comedy|Drama|Horror|Science Fiction|Adventure|", message = "Genre must be Action, Comedy etc")String > genres;
    @Pattern(regexp = "^(https?|ftp)://.*$", message = "Poster Url must be valid")
    private String poster;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }
}
