package com.bh.vc.opensearch.service;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.DefaultRequest;
import com.amazonaws.Request;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.http.ExecutionContext;
import com.amazonaws.http.HttpMethodName;
import com.bh.vc.opensearch.config.ConfigurationInfo;
import com.bh.vc.opensearch.constant.ElasticSearchConstants;
import com.bh.vc.opensearch.exception.IdNotFoundException;
import com.bh.vc.opensearch.handler.AwsResponse;
import com.bh.vc.opensearch.handler.ElasticSearchClientHandler;
import com.bh.vc.opensearch.model.Movie;
import com.bh.vc.opensearch.model.MovieQuery;
import com.bh.vc.opensearch.model.PartsPojo;
import com.bh.vc.opensearch.model.SearchQuery;
import com.bh.vc.opensearch.utils.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

@Named
public class OpenSearchService {

	private static final Logger LOGGER = LoggerFactory.getLogger(OpenSearchService.class);

	private static final AWSCredentials AWS_CREDENTIALS = new DefaultAWSCredentialsProviderChain().getCredentials();

	@Autowired
	private ConfigurationInfo configurationInfo;

	public String createIndex(List<PartsPojo> requestBody) throws JsonProcessingException {
//        final ObjectMapper objectMapper = new ObjectMapper();
//        final List<PartsInventory> partsPojo = objectMapper.readValue(requestBody, PartsInventory.class);
    	String requestData = null;
		String url = null;
		try {
			requestData = convertToStringJson(requestBody);
			url = ElasticSearchConstants.VC_INDEX + "/" + ElasticSearchConstants.PARTS_DOCUMENT_TYPE + "/_bulk";
			LOGGER.info("url:{}", url);

			if (requestData != null) {

				Request request = generateSignedRequest(url, requestData, null, HttpMethodName.PUT);
				AwsResponse response = executeRequest(request);

				// Creating a new document not seen before results in a 201 status, where as
				// overwriting a previous document results in a 200
				if (response != null && (response.getHttpResponse().getStatusCode() == HttpStatus.CREATED.value()
						|| response.getHttpResponse().getStatusCode() == HttpStatus.OK.value())) {
					LOGGER.info("Successfully created parts indexex with requestBody: {}", requestData.toString());
					return "Successfully created parts index ";
				}
			}
			
		}catch (Exception e) {
			LOGGER.error("Error created parts indexex with requestBody: {}", e, requestData.toString());
		}
		return null;
    }

	public String convertToStringJson(List<PartsPojo> requestBody) {
		List<PartsPojo> partsList = new ArrayList<>();
		StringBuilder completeJson = new StringBuilder();
		int idNo = 0;
		
		for (PartsPojo partsInventory : requestBody) {		
			String VC_INDEX = 
					"{ \"index\" : { \"_index\" : \""+ElasticSearchConstants.VC_INDEX
					+"\", \"_type\" : \""+ElasticSearchConstants.PARTS_DOCUMENT_TYPE
					+"\", \"_id\":\""+(idNo+1)+"\" } }";
			completeJson.append(VC_INDEX)
			.append("\n")
			.append("{ \"part_no\" : \""+partsInventory.getPart_no().trim()+"\", \"part_desc\" : \""+partsInventory.getPart_desc().trim()+"\" }")
			.append("\n");
			
			idNo++;
//			partsInventory.setId(partsInventory.getId());
//			partsInventory.setPart_no(partsInventory.getPart_no().trim());
//			partsInventory.setPart_desc(partsInventory.getPart_desc().trim());
//			partsList.add(partsInventory);
		}
//		Gson gson = new Gson();
//		return gson.toJson(partsList);
		return completeJson.toString();
	}

	/**
	 * Sign the request to AWS ElasticSearch using the AWS4Signer
	 *
	 * @param request The Request
	 */
	private void signRequest(Request request) {
		final String region = configurationInfo.getRegion();
		final String serviceName = configurationInfo.getServiceName();

		final AWS4Signer aws4Signer = new AWS4Signer();
		aws4Signer.setRegionName(region);
		aws4Signer.setServiceName(serviceName);
		aws4Signer.sign(request, AWS_CREDENTIALS);
	}

	/**
	 * Build the full URL, create request headers, and build Request object prior to
	 * signing the Request to send to AWS ElasticSearch
	 *
	 * @param url            The URL
	 * @param json           The request body
	 * @param parameters     The request parameters
	 * @param httpMethodName The HTTPMethodName
	 * @return The Request
	 */
	private Request generateSignedRequest(final String url, final String json,
			final Map<String, List<String>> parameters, final HttpMethodName httpMethodName) {

		final String endpoint = configurationInfo.getEndpoint() + "/" + url;
		final Map<String, String> headers = new HashMap<>();
		headers.put("content-type", "application/json");

		final Request request = new DefaultRequest(configurationInfo.getServiceName());
		request.setHeaders(headers);

		// JSON is used for Creating and Updating objects in ElasticSearch
		if (json != null) {
			request.setContent(new ByteArrayInputStream(json.getBytes()));
		}
		// Parameters are used for queries
		if (parameters != null) {
			request.setParameters(parameters);
		}
		request.setEndpoint(URI.create(endpoint));
		request.setHttpMethod(httpMethodName);

		signRequest(request);

		return request;
	}

	/**
	 * Submit the Request to AWS, and return the response
	 *
	 * @param request The Request
	 * @return AwsResponse
	 */
	private AwsResponse executeRequest(Request request) {
		try {
			final ClientConfiguration configuration = new ClientConfiguration();
			final ExecutionContext context = new ExecutionContext(true);
			final ElasticSearchClientHandler client = new ElasticSearchClientHandler(configuration);

			return client.execute(context, request);
		} catch (Exception e) {
			LOGGER.error("Error executing ElasticSearch Request.", e);
		}
		return null;
	}

	/**
	 * Create a new document in ElasticSearch with a given Index, Document Mapping,
	 * Document Body, and Document ID
	 *
	 * @param index The index to create the new document in
	 * @param type  The mapping used by the index
	 * @param json  The document
	 * @param id    The document ID
	 * @return AwsResponse
	 */
    private AwsResponse createDocument(final String index, final String type, final String json, final String id) {
        final String url = index + "/" + type + "/" + id;
        final Request request = generateSignedRequest(url, json, null, HttpMethodName.PUT);

        return executeRequest(request);
    }

	private AwsResponse createPartsDocument(final String index, final String type, final String json, final String id) {
		final String url = index + "/" + type + "/_bulk";
		LOGGER.info("url:{}", url);
		final Request request = generateSignedRequest(url, json, null, HttpMethodName.POST);

		return executeRequest(request);
	}

	/**
	 * Delete a document from ElasticSearch with a given Index, Document Mapping,
	 * and Document ID
	 *
	 * @param index The index to delete the document from
	 * @param type  The mapping use by the index
	 * @param id    The ID of the document to be deleted
	 * @return AwsResponse
	 */
	public AwsResponse deleteDocument(final String index, final String type, final String id) {
		final String url = index + "/" + type + "/" + id;
		// JSON and URL Parameters are not needed when deleting documents from
		// ElasticSearch
		final Request request = generateSignedRequest(url, null, null, HttpMethodName.DELETE);

		return executeRequest(request);
	}

	/**
	 * Create the Movie in ElasticSearch
	 *
	 * @param movie The Movie
	 * @return The response string
	 * @throws JsonProcessingException Throws JsonProcessingException when response
	 *                                 cannot be parsed
	 */
	public String createNewMovie(Movie movie) throws JsonProcessingException {
		final ObjectMapper objectMapper = new ObjectMapper();
		final String json = objectMapper.writeValueAsString(movie);
		if (json != null) {
			AwsResponse response = createDocument(ElasticSearchConstants.VC_INDEX,
					ElasticSearchConstants.ORDERS_DOCUMENT_TYPE, json, movie.getId().toString());
			// Creating a new document not seen before results in a 201 status, where as
			// overwriting a previous document results in a 200
			if (response != null && (response.getHttpResponse().getStatusCode() == HttpStatus.CREATED.value()
					|| response.getHttpResponse().getStatusCode() == HttpStatus.OK.value())) {
				LOGGER.info("Successfully created new movie with ID: {} and title: {}", movie.getId(),
						movie.getTitle());
				return movie.getTitle();
			}
		}

		return null;
	}

	/**
	 * Update the Movie in ElasticSearch
	 *
	 * @param movie The Movie
	 * @param id    The ID of the Movie
	 * @return The response string
	 * @throws JsonProcessingException Throws JsonProcessingException when response
	 *                                 cannot be parsed
	 */
	public String updateMovie(final Long id, Movie movie) throws JsonProcessingException, IdNotFoundException {
		final MovieQuery movieQuery = new MovieQuery();
		movieQuery.setId(id);

		// Search ElasticSearch to make sure that the given ID is valid
		final String movieToUpdate = "";//getMovies(ElasticSearchConstants.ORDERS_INDEX, 0, 100, null, movieQuery);
		if (movieToUpdate == null || movieToUpdate.equals(ElasticSearchConstants.EMPTY_RESPONSE)) {
			throw new IdNotFoundException("Failed to find movie to update with id of " + id);
		}

		// If the ID does exist, then overwrite the existing object with the object
		// provided in the update request
		final ObjectMapper objectMapper = new ObjectMapper();
		final String json = objectMapper.writeValueAsString(movie);
		if (json != null) {
			AwsResponse response = createDocument(ElasticSearchConstants.VC_INDEX,
					ElasticSearchConstants.ORDERS_DOCUMENT_TYPE, json, movie.getId().toString());
			if (response != null && response.getHttpResponse().getStatusCode() == HttpStatus.OK.value()) {
				LOGGER.info("Successfully created new movie with ID: {} and title: {}", movie.getId(),
						movie.getTitle());
				return movie.getTitle();
			}
		}

		return null;
	}

	/**
	 * Generate the request from the API criteria, and return the results from
	 * ElasticSearch
	 *
	 * @param from         Beginning point of the query
	 * @param size         Number of objects to return in the query
	 * @param filterValues Optional list of values to filter the response by
	 * @return Response
	 */
	public String getOpenSearchData(final String index, final int from, final int size, Set<String> filterValues,
			final SearchQuery searchQuery) {
		JSONObject query = new JSONObject();
		JSONObject bool = new JSONObject();
		JSONObject must = new JSONObject();
		JSONArray array = new JSONArray();

		createQuery(searchQuery, array);

		query.put("from", from);
		query.put("size", size);
		must.put("must", array);
		bool.put("bool", must);
		query.put("query", bool);
		if (filterValues != null) {
			query.put("_source", filterValues);
		}

		final Map<String, List<String>> parameters = new HashMap<>();
		parameters.put(ElasticSearchConstants.FILTER_PATH, Collections.singletonList(ElasticSearchConstants.FILTER));

		final String url = index + ElasticSearchConstants.SEARCH_API;
		LOGGER.info("ES Query Details: Url: {}\n Body: {}\n params:{}", url, query, parameters.toString());
		final Request request = generateSignedRequest(url, query.toString(), parameters, HttpMethodName.GET);

		final AwsResponse response = executeRequest(request);

		return response != null ? response.getBody() : "";
	}

	/**
	 * Build a fuzzy search ElasticSearch query
	 *
	 * @param from         Beginning point of the query
	 * @param size         Number of objects to return in the query
	 * @param filterValues Optional list of values to filter the response by
	 * @return Response
	 */
	public String getMoviesFuzzySearch(final String index, final int from, final int size, Set<String> filterValues,
			final MovieQuery movieQuery) {
		JSONObject query = new JSONObject();
		JSONObject fuzzy = new JSONObject();
		JSONObject searchTerm = new JSONObject();

		createMovieQueryFuzzySearch(movieQuery, searchTerm);

		query.put("from", from);
		query.put("size", size);
		fuzzy.put("fuzzy", searchTerm);
		query.put("query", fuzzy);
		if (filterValues != null) {
			query.put("_source", filterValues);
		}

		final Map<String, List<String>> parameters = new HashMap<>();
		parameters.put(ElasticSearchConstants.FILTER_PATH, Collections.singletonList(ElasticSearchConstants.FILTER));

		final String url = index + ElasticSearchConstants.SEARCH_API;
		LOGGER.info("ES Query Body: {}", query);
		final Request request = generateSignedRequest(url, query.toString(), parameters, HttpMethodName.GET);

		final AwsResponse response = executeRequest(request);

		return response != null ? response.getBody() : "";
	}

	/**
	 * Build a query statement from the MovieQuery object
	 *
	 * @param movieQuery The MovieQuery
	 * @param array      The JSONArray
	 */
	private void createQuery(final SearchQuery searchQuery, JSONArray array) {
		if (searchQuery.getId() != null) {
			buildElasticSearchMatchStatement("id", searchQuery.getId(), array);
		}
		if (StringUtils.checkNullOrEmpty(searchQuery.getPart_no())) {
			buildElasticSearchMatchStatement("part_no", searchQuery.getPart_no(), array);
		}
		if (StringUtils.checkNullOrEmpty(searchQuery.getPart_desc())) {
			buildElasticSearchMatchStatement("part_desc", searchQuery.getPart_desc(), array);
		}
	}

	/**
	 * Build an ElasticSearch fuzzy search statement.
	 *
	 * @param movieQuery The MovieQuery
	 * @param searchTerm The term to search for
	 */
	private void createMovieQueryFuzzySearch(final MovieQuery movieQuery, JSONObject searchTerm) {
		if (StringUtils.checkNullOrEmpty(movieQuery.getStoryline())) {
			buildElasticSearchFuzzyStatement("storyline", movieQuery.getStoryline(), searchTerm);
		}
		if (StringUtils.checkNullOrEmpty(movieQuery.getSynopsis())) {
			buildElasticSearchFuzzyStatement("synopsis", movieQuery.getSynopsis(), searchTerm);
		}

	}

	/**
	 * Build an ElasticSearch 'should' statement.
	 *
	 * @param field The field to search in
	 * @param value The value to search for
	 * @param array The JSONArray to append the the query to
	 */
	private void buildElasticSearchShouldStatement(final String field, final Collection value, final JSONArray array) {
		if (value.size() > 1) {
			final JSONObject bool = new JSONObject();
			final JSONObject should = new JSONObject();
			final JSONArray match = new JSONArray();
			for (Object objectValue : value) {
				buildElasticSearchMatchStatement(field, objectValue, match);
			}
			should.put("should", match);
			bool.put("bool", should);
			array.put(bool);
		} else {
			buildElasticSearchMatchStatement(field, value.iterator().next(), array);
		}
	}

	/**
	 * Build an ElasticSearch 'match' statement. This is equivalent to a SQL
	 * 'equals' statement.
	 *
	 * @param field The field to search in
	 * @param value The value to search for
	 * @param array The JSONArray to append the query to
	 */
	private void buildElasticSearchMatchStatement(final String field, final Object value, final JSONArray array) {
		final JSONObject matchItem = new JSONObject();
		final JSONObject matchTerms = new JSONObject();
		matchTerms.put(field, value);
		matchItem.put("match", matchTerms);
		array.put(matchItem);
	}

	/**
	 * Build a fuzzy search clause
	 *
	 * @param field      The field to search in
	 * @param value      The partial value to search for
	 * @param searchTerm The JSONObject
	 */
	private void buildElasticSearchFuzzyStatement(final String field, final Object value, final JSONObject searchTerm) {
		final JSONObject fuzzyBlock = new JSONObject();
		fuzzyBlock.put("value", value);
		fuzzyBlock.put("boost", 1.0);
		fuzzyBlock.put("fuzziness", 50);
		fuzzyBlock.put("prefix_length", 0);
		fuzzyBlock.put("max_expansions", 100);
		searchTerm.put(field, fuzzyBlock);
	}

	/**
	 * Build request to /_stats API in ElasticSearch
	 *
	 * @param index The Index
	 * @return Response
	 */
	public String getIndexStatistics(final String index) {
		final String url = index + ElasticSearchConstants.STATS_API;

		final Request request = generateSignedRequest(url, null, null, HttpMethodName.GET);

		final AwsResponse response = executeRequest(request);

		return response != null ? response.getBody() : "";
	}

}
