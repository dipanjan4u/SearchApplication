package com.cts.search;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.cts.search.controller.SearchController;

@RunWith(SpringRunner.class)
@WebMvcTest(SearchController.class)
public class SearchServiceApplicationTests {

	@Autowired
	private MockMvc searchTest;

	@Test
	public void singleWordSearchCaseSensitive() throws Exception {
		searchTest
				.perform(get("/search/files").param("recursivesearch", "Y").param("casesensitive", "Y")
						.param("searchwords", "rahul").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.TOTAL", is(3)));

	}
	
	@Test
	public void singleWordSearchCaseInSensitive() throws Exception {
		searchTest.perform(get("/search/files").param("recursivesearch", "Y").param("casesensitive", "N")
				.param("searchwords", "Rahul").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.TOTAL", is(3)));
	}
	
	@Test
	public void singleWordSearchNonRecursive() throws Exception {
		searchTest.perform(get("/search/files").param("recursivesearch", "N").param("casesensitive", "Y")
				.param("searchwords", "rahul").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.TOTAL", is(1)));
	}	
	
	@Test
	public void multiWordSearchCaseSensitive() throws Exception {
		searchTest.perform(get("/search/files").param("recursivesearch", "Y").param("casesensitive", "Y")
				.param("searchwords", "rahul good").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.TOTAL", is(1)));
	}
	
	@Test
	public void multiWordSearchCaseInSensitive() throws Exception {
		searchTest.perform(get("/search/files").param("recursivesearch", "Y").param("casesensitive", "N")
				.param("searchwords", "RAHUL GOOD").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.TOTAL", is(1)));
	}
	
	@Test
	public void multiWordSearchNonRecursive() throws Exception {
		searchTest.perform(get("/search/files").param("recursivesearch", "N").param("casesensitive", "Y")
				.param("searchwords", "rahul cognizant").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.TOTAL", is(1)));
	}		

}
