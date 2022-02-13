package com.bagongkia.stev;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.bagongkia.stev.controller.ReportController;

@SpringBootTest
public class ReportControllerTests {
	
	@Autowired
	private ReportController reportController;

	@Test
	void test() throws IOException {
		final Resource salesResource = new ClassPathResource("Sales.csv");
		final InputStream salesInput = salesResource.getInputStream();
		final MultipartFile salesFile = new MockMultipartFile(salesResource.getFilename(), salesInput);
		
		final Resource paymentResource = new ClassPathResource("Payment.csv");
		final InputStream paymentInput = paymentResource.getInputStream();
		final MultipartFile paymentFile = new MockMultipartFile(paymentResource.getFilename(), paymentInput);
		
		final Resource returnedItemsResource = new ClassPathResource("Return.xlsx");
		final InputStream returnedItemsInput = returnedItemsResource.getInputStream();
		final MultipartFile returnedItemsFile = new MockMultipartFile(returnedItemsResource.getFilename(), returnedItemsInput);
		
		final Resource lostItemsResource = new ClassPathResource("Lost.xlsx");
		final InputStream lostItemsInput = lostItemsResource.getInputStream();
		final MultipartFile lostItemsFile = new MockMultipartFile(lostItemsResource.getFilename(), lostItemsInput);
		
		reportController.lostItemReport(salesFile, paymentFile, returnedItemsFile, lostItemsFile);
	}
	
}
