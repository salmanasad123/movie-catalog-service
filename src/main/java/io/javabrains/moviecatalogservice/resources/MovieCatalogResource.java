package io.javabrains.moviecatalogservice.resources;

import io.javabrains.moviecatalogservice.models.CatalogItem;
import io.javabrains.moviecatalogservice.models.Rating;
import io.javabrains.moviecatalogservice.models.UserRating;
import io.javabrains.moviecatalogservice.services.MovieInfo;
import io.javabrains.moviecatalogservice.services.UserRatingInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/catalog")
public class MovieCatalogResource {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private MovieInfo movieInfo;
    @Autowired
    private UserRatingInfo userRatingInfo;


    // when the circuit breaks calls fallback method don't call getCatalog method, add hystrixCommand to the method
    // that needs circuit breaking
    @GetMapping("/{userId}")
    public List<CatalogItem> getCatalog(@PathVariable String userId) {

        // get all rated movie ids
        UserRating userRating = userRatingInfo.getUserRating(userId);

        return userRating.getUserRatings().stream()
                .map((Rating rating) -> {
                    // for each movie id call movie info service and get details
                    return movieInfo.getCatalogItem(rating);
                })
                .collect(Collectors.toList());
    }


    // you don't need to make another api call in fallback because that api call might fail and
    // you need another fallback method for that, so return a cached response or a hard-coded response
    // not used
    public List<CatalogItem> getFallbackCatalog(@PathVariable String userId) {
        // return hard coded list of catalog items
        return Arrays.asList(new CatalogItem("No movie", "", 0));
    }
}
