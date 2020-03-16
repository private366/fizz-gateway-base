package com.wehotel.fizz;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.springframework.boot.test.context.SpringBootTest;
import com.wehotel.fizz.input.Input;
 

@SpringBootTest
class ConfigLoaderTests {
	@Test
	void contextLoads() {
	 
		
	}
	@Test
	void testCreateInput() {
		File file = new File("/Users/francis/wehotel/workspaces/fizz-gateway/json/aggr-demo.json");
		
		ConfigLoader loader = new ConfigLoader();
		Input input = null;
		try {
			input = loader.createInput(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Assertions.assertNotNull(input);
		
	}
	
	@Test
	void testCreatePipeline() {
		File file = new File("/Users/francis/wehotel/workspaces/fizz-gateway/json/aggr-demo.json");
		
		ConfigLoader loader = new ConfigLoader();
		Pipeline pipeline = null;
		try {
			pipeline = loader.createPipeline(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Assertions.assertNotNull(pipeline);
		
	}


}
