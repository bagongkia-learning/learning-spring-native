package com.bagongkia.stev.controller;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bagongkia.stev.ReportException;
import com.bagongkia.stev.service.ReportService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping(path = "/report")
public class ReportController {
	
	@Autowired
	private ReportService reportService;
	
	@PostMapping(path = "/download-lost-items-report")
	public String lostItemReport(
			@RequestParam("salesFile") MultipartFile salesFile,
			@RequestParam("paymentFile") MultipartFile paymentFile,
			@RequestParam(name = "returnedItemsFile", required = false) MultipartFile returnedItemsFile,
			@RequestParam(name = "lostItemsFile", required = false) MultipartFile lostItemsFile) {
		try {
			reportService.generateLostItemsReport(salesFile, paymentFile, returnedItemsFile, lostItemsFile);
		} catch(ReportException e) {
			log.error("Generate Lost Items Report Failure: {}", e);
			return "FAILED - " + e.getMessage();
		} catch(Exception e) {
			log.error("Generate Lost Items Report Failure: {}", e);
			return "FAILED";
		}
		return "COMPLETED";
	}
	
//	@PostMapping(path = "/download-lost-items-report")
//	public String losingItemReport(@RequestParam("salesFile") MultipartFile salesFile,
//			@RequestParam("paymentFile") MultipartFile paymentFile,
//			@RequestParam("returnedItemsFile") MultipartFile returnedItemsFile) throws Exception {
//		if (!jobExplorer.findRunningJobExecutions("lostItemReportJob").isEmpty()) {
//			log.info("Job is still runnning");
//			return "FAILED";
//		} else {
//			if (salesFile == null) {
//				return "Laporan Penjualan belum dipilih";
//			}
//			if (paymentFile == null) {
//				return "Laporan Uang Masuk belum dipilih";
//			}
//			if (returnedItemsFile == null) {
//				return "Laporan Barang Retur belum dipilih";
//			}
//			reportService.storeSalesFile(salesFile);
//			reportService.storePaymentFile(paymentFile);
//			reportService.storeReturnedItemFile(returnedItemsFile);
//			return reportService.runLostItemReport();
//		}
//	}
	
//	@PostMapping(path = "/download-lost-items-report")
//	public String losingItemReport(@RequestParam(name = "lostItemsFile", required = false) MultipartFile lostItemsFile,
//			@RequestParam("salesFile") MultipartFile salesFile,
//			@RequestParam("paymentFile") MultipartFile paymentFile,
//			@RequestParam("returnedItemsFile") MultipartFile returnedItemsFile) throws Exception {
//		if (!jobExplorer.findRunningJobExecutions("lostItemReportJob").isEmpty()) {
//			log.info("Job is still runnning");
//			return "FAILED";
//		} else {
//			if (salesFile == null) {
//				return "Laporan Penjualan belum dipilih";
//			}
//			if (paymentFile == null) {
//				return "Laporan Uang Masuk belum dipilih";
//			}
//			if (returnedItemsFile == null) {
//				return "Laporan Barang Retur belum dipilih";
//			}
//			reportService.storeSalesFile(salesFile);
//			reportService.storePaymentFile(paymentFile);
//			reportService.storeReturnedItemFile(returnedItemsFile);
//			if (lostItemsFile != null) {
//				reportService.storeLostItemsFile(lostItemsFile);
//				return reportService.runLostItemReport2();
//			}
//			return reportService.runLostItemReport();
//		}
//	}
	
	@GetMapping(path = "/download-lost-items-report", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Resource> downloadLostItemsReport() throws Exception {
		SimpleDateFormat fmt = new SimpleDateFormat("dd-MMM-yyyy");
		
		HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Laporan Barang Hilang " + fmt.format(new Date()) + ".xlsx");
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        
        Resource resource = reportService.getLostItemsReport();
        
		return ResponseEntity.ok()
				.headers(headers)
				.contentLength(resource.contentLength())
				.contentType(MediaType.MULTIPART_FORM_DATA)
				.body(resource);
	}
	
	@GetMapping(path = "/download-unprinted-invoice-report", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Resource> downloadUnprintedInvoiceReport() throws Exception {
		SimpleDateFormat fmt = new SimpleDateFormat("dd-MMM-yyyy");
		
		HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Laporan Invoice Belum Cetak " + fmt.format(new Date()) + ".xlsx");
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        
        Resource resource = reportService.getUnprintedInvoiceReport();
        
		return ResponseEntity.ok()
				.headers(headers)
				.contentLength(resource.contentLength())
				.contentType(MediaType.MULTIPART_FORM_DATA)
				.body(resource);
	}
	
	@GetMapping(path = "/download-multiple-printed-invoice-report", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Resource> downloadMultiplePrintedInvoiceReport() throws Exception {
		SimpleDateFormat fmt = new SimpleDateFormat("dd-MMM-yyyy");
		
		HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Laporan Invoice Double Cetak " + fmt.format(new Date()) + ".xlsx");
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        
        Resource resource = reportService.getMultiplePrintedInvoiceReport();
        
		return ResponseEntity.ok()
				.headers(headers)
				.contentLength(resource.contentLength())
				.contentType(MediaType.MULTIPART_FORM_DATA)
				.body(resource);
	}
	
//	@PostMapping(path = "/download-invoice-report")
//	public String invoiceReport(@RequestParam("salesFile") MultipartFile salesFile,
//			@RequestParam("stockOutFile") MultipartFile stockOutFile) throws Exception {
//		reportService.storeSalesFile(salesFile);
//		reportService.storeStockOutFile(stockOutFile);
//		return reportService.runInvoiceReport();
//	}
	
	@PostMapping(path = "/download-invoice-report")
	public String invoiceReport(@RequestParam("salesFile") MultipartFile salesFile,
			@RequestParam("stockOutFile") MultipartFile stockOutFile) throws Exception {
		try {
			reportService.generateInvoiceReport(salesFile, stockOutFile);
		} catch(ReportException e) {
			log.error("Generate Invoice Report Failure: {}", e);
			return "FAILED - " + e.getMessage();
		} catch(Exception e) {
			log.error("Generate Invoice Report Failure: {}", e);
			return "FAILED";
		}
		return "COMPLETED";
	}
	
	@GetMapping(path = "/test-download")
	public String testDownload() throws IOException {
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
		
		return lostItemReport(salesFile, paymentFile, returnedItemsFile, lostItemsFile);
	}
	
}