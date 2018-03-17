package com.cts.search.controller;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cts.bl.SearchBL;
import com.cts.bo.ExceptionBO;
import com.cts.bo.FileSearchResultBO;
import com.cts.bo.SearchBO;
import com.cts.bo.ValidationBO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/search")
@Api(value = "Search", description = "Operations pertaining to search")
@Scope(value=ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SearchController {

	@Autowired
	@Qualifier("springManagedSearchBL")
	private SearchBL searchBusinessLogic;

	@CrossOrigin
	@ApiOperation(value = "Search words in the files and return the matched files", response = SearchBO.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "List of files (File Name and File Location) matched", response = SearchBO.class),
			@ApiResponse(code = 500, message = "Error Fetching", response = ExceptionBO.class),
			@ApiResponse(code = 400, message = "Validation Failed", response = ValidationBO.class) })
	@RequestMapping(value = "/files", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	public ResponseEntity<?> getFileSearchResults(
			@ApiParam(value = "Please send search words. Multiple words should be placed one after another deliminated by space", required = false) @RequestParam(value = "searchwords", required = false) String searchParameters,
			@ApiParam(value = "Please mention whether you would like to search all-sub directories. Allowed values are Y or y for yes and N or n for No", required = false, defaultValue = "Y", allowableValues = "Y,N") @RequestParam(value = "recursivesearch", required = false) String recursiveSearch,
			@ApiParam(value = "Please mention whether you would like to search case sensitive. Allowed values are Y or y for yes and N or n for No", required = false, defaultValue = "Y", allowableValues = "Y,N") @RequestParam(value = "casesensitive", required = false) String caseSensitiveSearch,
			@ApiParam(value = "Please provide the search root path. If not provided default path provided at the application level will be picked up", required = false) @RequestParam(value = "searchpath", required = false) String searchPath)
			throws Exception {

		if (StringUtils.isBlank(searchParameters))
			return new ResponseEntity<ValidationBO>(
					new ValidationBO("You have to enter atleast one word as 'searchWords' query parameter"),
					HttpStatus.BAD_REQUEST);
		// Path p4 = FileSystems.getDefault().getPath(searchPath);
		if (!StringUtils.isBlank(searchPath)) {
			if (!new File(searchPath).exists())
				return new ResponseEntity<ValidationBO>(
						new ValidationBO(String.format("The searchpath %s does not exist", searchPath)),
						HttpStatus.BAD_REQUEST);
			else if (!new File(searchPath).isDirectory() || Files.list(new File(searchPath).toPath()).count() == 0) {
				return new ResponseEntity<ValidationBO>(
						new ValidationBO(String
								.format("The searchpath %s is not a valid directory or a blank directory", searchPath)),
						HttpStatus.BAD_REQUEST);
			} else
				searchBusinessLogic.setRootDirectory(
						searchPath.indexOf(':') == searchPath.length() - 1 ? searchPath + "/" : searchPath);
		}
		String[] searchWords = searchParameters.replaceAll("\\s{2,}", " ").trim().split(" ");

		// Setting additional preferences for search
		if (!StringUtils.isEmpty(recursiveSearch)
				&& (recursiveSearch.trim().equalsIgnoreCase("Y") || recursiveSearch.trim().equalsIgnoreCase("N")))
			searchBusinessLogic
					.setRecursiveDirectorySearch(recursiveSearch.trim().equalsIgnoreCase("Y") ? true : false);
		if (!StringUtils.isEmpty(caseSensitiveSearch) && (caseSensitiveSearch.trim().equalsIgnoreCase("Y")
				|| caseSensitiveSearch.trim().equalsIgnoreCase("N")))
			searchBusinessLogic.setCaseSensitiveSearch(caseSensitiveSearch.trim().equalsIgnoreCase("Y") ? true : false);

		// searchBusinessLogic.getMatchedFiles(searchWords);
		try {
			SearchBO<FileSearchResultBO> searchBO = searchBusinessLogic.getMatchedFiles(searchWords);
			return new ResponseEntity<SearchBO<FileSearchResultBO>>(searchBO, HttpStatus.OK);
		} catch (Exception ex) {
			return new ResponseEntity<ExceptionBO>(new ExceptionBO("", ex), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

}
