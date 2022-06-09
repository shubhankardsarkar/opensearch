package com.bh.vc.opensearch.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.bh.vc.opensearch.constant.ElasticSearchConstants;
import com.bh.vc.opensearch.model.OrdersPojo;
import com.bh.vc.opensearch.model.PartsPojo;
import com.bh.vc.opensearch.model.SearchQuery;
import com.bh.vc.opensearch.service.OpenSearchService;
import com.fasterxml.jackson.core.JsonProcessingException;

@RestController
@RequestMapping("/api/v1")
public class OpenSearchController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchController.class);

	@Autowired
	private OpenSearchService openSearchService;

	@PostMapping(value = "/createPartsIndex", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> createPartsIndex(@NonNull @RequestBody List<PartsPojo> requestBody) {
		String responseData = null;

		try {
			LOGGER.info("requestBody:: {}", requestBody.toString());
			responseData = openSearchService.createIndex(requestBody);

			if (responseData != null) {
				return ResponseEntity.status(HttpStatus.OK).body("Successfully created " + responseData);
			}
		} catch (JsonProcessingException e) {
			LOGGER.error("Failed to create ValvCentral Indexes.", e);
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("Failed to create  " + requestBody.toString());
	}

	/*
	 * @PostMapping(value = "/createOrdersIndex", produces = {
	 * MediaType.APPLICATION_JSON_VALUE }) public ResponseEntity<String>
	 * createIndex(@NonNull @RequestBody List<OrdersPojo> requestBody) { String
	 * responseData = null;
	 * 
	 * try { LOGGER.info("requestBody:: {}", requestBody.toString()); responseData =
	 * "";//openSearchService.createIndex(requestBody);
	 * 
	 * if (responseData != null) { return
	 * ResponseEntity.status(HttpStatus.OK).body("Successfully created " +
	 * responseData); } } catch (JsonProcessingException e) {
	 * LOGGER.error("Failed to create ValvCentral Indexes.", e); } return
	 * ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	 * .body("Failed to create  " + requestBody.toString()); }
	 */
	
    @PostMapping(value = "/search", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public ResponseEntity<String> getOpenSearchData(@RequestBody SearchQuery searchQuery) {
        return ResponseEntity.status(HttpStatus.OK).body(
        		openSearchService.getOpenSearchData(ElasticSearchConstants.VC_INDEX, 0, 100, null, searchQuery));
    }
}
