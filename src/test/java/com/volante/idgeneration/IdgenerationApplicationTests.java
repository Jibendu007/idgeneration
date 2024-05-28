//package com.volante.idgeneration;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//
//@SpringBootTest
//class IdgenerationApplicationTests {
//
//	@Test
//	void contextLoads() {
//	}
//
//}
package com.volante.idgeneration;

import com.volante.idgeneration.service.IdGenerationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class IdgenerationApplicationTests {

	@Autowired
	private IdGenerationService idGenerationService;

	@Test
	void contextLoads() {
	}

	@Test
	void generatedIdsShouldBeUnique() throws IOException {
		Set<String> ids = new HashSet<>();
		int numIds = 10; // Number of IDs to generate and check
		String payloadJson = "{ \"type\": \"interface\", \"name\": \"Credittransfertrace\" }";
		for (int i = 0; i < numIds; i++) {
			String id = idGenerationService.generateId(payloadJson);
			ids.add(id);
		}

		// Assert that the number of unique IDs generated matches the number of IDs requested
		assertEquals(numIds, ids.size());
	}
}

