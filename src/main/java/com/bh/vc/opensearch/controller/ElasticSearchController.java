package com.bh.vc.opensearch.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.bh.vc.opensearch.constant.ElasticSearchConstants;
import com.bh.vc.opensearch.exception.IdNotFoundException;
import com.bh.vc.opensearch.handler.AwsResponse;
import com.bh.vc.opensearch.model.Movie;
import com.bh.vc.opensearch.model.MovieQuery;
import com.bh.vc.opensearch.model.PartsPojo;
import com.bh.vc.opensearch.service.ElasticSearchService;
import com.fasterxml.jackson.core.JsonProcessingException;

@RestController
@RequestMapping("/elastic-search")
public class ElasticSearchController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchController.class);

    @Autowired
    private ElasticSearchService elasticSearchService;

    @PostMapping(value = "/createPartsIndex", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public ResponseEntity<String> createPartsIndex(@RequestBody final String parts) {
        String data = null;
        try {
            data = elasticSearchService.createPartsIndexes(parts);
            if (data != null) {
                return ResponseEntity.status(HttpStatus.OK).body("Successfully created " + data);
            }
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to create Movie.", e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create  " + parts);
    }
    
    
    /**
     * Get a Set of Movies that match your query criteria
     *
     * @param movieQuery The query
     * @return Set of Movies
     */
    @PostMapping(value = "/search", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public ResponseEntity<String> getFromElasticSearch(@RequestBody final MovieQuery movieQuery) {
        return ResponseEntity.status(HttpStatus.OK).body(
                elasticSearchService.getMovies(ElasticSearchConstants.VC_INDEX, 0, 100, null, movieQuery));
    }

    /**
     * Fuzzy search the Movies index with a partial word, or one word in a sentence.
     *
     * @param movieQuery The query
     * @return Set of Movies
     */
    @PostMapping(value = "/fuzzySearch", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public ResponseEntity<String> getFromElasticSearchFuzzySearch(@RequestBody final MovieQuery movieQuery) {
        return ResponseEntity.status(HttpStatus.OK).body(
                elasticSearchService.getMoviesFuzzySearch(ElasticSearchConstants.VC_INDEX, 0, 100, null, movieQuery));
    }

    /**
     * Create a new Movie in ElasticSearch
     *
     * @param movie The Movie object
     * @return Response Entity
     */
    @PostMapping(value = "/create", produces = {MediaType.TEXT_PLAIN_VALUE})
    @ResponseBody
    public ResponseEntity<String> createElasticSearchObject(@RequestBody final Movie movie) {
        String title = null;
        try {
            title = elasticSearchService.createNewMovie(movie);
            if (title != null) {
                return ResponseEntity.status(HttpStatus.OK).body("Successfully created " + title);
            }
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to create Movie.", e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create  " + movie.getTitle());
    }

    /**
     * Update a Movie object in ElasticSearch
     *
     * @param movie The Movie object
     * @return Response Entity
     */
    @PutMapping(value = "/update", produces = {MediaType.TEXT_PLAIN_VALUE})
    @ResponseBody
    public ResponseEntity<String> updateElasticSearchObject(@RequestBody final Movie movie,
                                                            @RequestParam(value = "id", required = true) final Long id) {
        String title = null;
        try {
            title = elasticSearchService.updateMovie(id, movie);
            if (title != null) {
                return ResponseEntity.status(HttpStatus.OK).body("Successfully updated " + title);
            }
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to update Movie.", e);
        } catch (IdNotFoundException inf) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(inf.getMessage());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update  " + movie.getTitle());
    }

    /**
     * Delete a Movie object in ElasticSearch
     *
     * @param index The targeted index
     * @param type  The document type
     * @param id    The document ID
     * @return Response Entity
     */
    @DeleteMapping(value = "/delete", produces = {MediaType.TEXT_PLAIN_VALUE})
    @ResponseBody
    public ResponseEntity<String> deleteFromElasticSearch(@RequestParam("index") final String index,
                                                          @RequestParam("type") final String type,
                                                          @RequestParam("id") final String id) {
        AwsResponse response = elasticSearchService.deleteDocument(index, type, id);
        if (response != null && response.getHttpResponse().getStatusCode() == 200) {
            return ResponseEntity.status(HttpStatus.OK).body("Successfully deleted movie with ID of " + id);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting ElasticSearch document");
        }
    }

    /**
     * Get statistics about an ElasticSearch Index
     *
     * @param index The targeted index
     * @return Response Entity
     */
    @GetMapping(value = "/statistics", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public ResponseEntity<String> indexStatistics(@RequestParam("index") final String index) {
        String response = elasticSearchService.getIndexStatistics(index);
        if (response != null) {
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching statistics for index");
        }
    }
}
