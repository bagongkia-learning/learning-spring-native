package com.bagongkia.stev.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bagongkia.stev.model.ExitItem;
import com.bagongkia.stev.model.LostItem;
import com.bagongkia.stev.model.Payment;
import com.bagongkia.stev.model.ReturnedItem;
import com.bagongkia.stev.model.Sale;

@Service
public class ReportService {
	
	@Autowired
	private FileStorageService fileStorageService;
	
	@Autowired
	private FileReader fileReader;
	
	@Autowired
	private LostItemsFileWriter lostItemsFileWriter;
	
	@Autowired
	private InvoiceWriter invoiceWriter;
	
	public void storeLostItemsFile(MultipartFile file) throws IOException {
		//TODO: VALIDATION
		fileStorageService.storeFile(file, "lost-items-report.xlsx");
	}
	
	public void storeSalesFile(MultipartFile file) throws IOException {
		//TODO: VALIDATION
		fileStorageService.storeFile(file, "sales-report.csv");
	}
	
	public void storePaymentFile(MultipartFile file) throws IOException {
		//TODO: VALIDATION
		fileStorageService.storeFile(file, "payment-report.csv");
	}
	
	public void storeReturnedItemFile(MultipartFile file) throws IOException {
		//TODO: VALIDATION
		fileStorageService.storeFile(file, "returned-items-report.xlsx");
	}
	
	public void storeStockOutFile(MultipartFile file) throws IOException {
		//TODO: VALIDATION
		fileStorageService.storeFile(file, "stock-out-report.xlsx");
	}

	public Resource getUnprintedInvoiceReport() {
		return fileStorageService.getFile("unprinted-invoice-report.xlsx");
	}

	public Resource getMultiplePrintedInvoiceReport() {
		return fileStorageService.getFile("multiple-printed-invoice-report.xlsx");
	}

	public Resource getLostItemsReport() {
		return fileStorageService.getFile("lost-items-report.xlsx");
	}
	
	public void generateLostItemsReport(MultipartFile salesFile, MultipartFile paymentFile, 
			MultipartFile returnedItemsFile, MultipartFile lostItemsFile) throws Exception {
		List<Sale> sales = fileReader.readSalesFile(salesFile.getInputStream());
		List<Payment> payments = fileReader.readPaymentFile(paymentFile.getInputStream());
		List<ReturnedItem> returnedItems = new ArrayList<>();
		if (returnedItemsFile != null) {
			returnedItems = fileReader.readReturnedItemsFile(returnedItemsFile.getInputStream());
		}
		List<LostItem> lostItems = new ArrayList<>();
		if (lostItemsFile != null) {
			lostItems = fileReader.readLostItemsFile(lostItemsFile.getInputStream());
		}
		lostItemsFileWriter.write(lostItems, sales, payments, returnedItems);
	}

	public void generateInvoiceReport(MultipartFile salesFile, MultipartFile exitItemsFile) throws Exception {
		List<Sale> sales = fileReader.readSalesFile(salesFile.getInputStream());
		List<ExitItem> exitItems = fileReader.readExitItemsFile(exitItemsFile.getInputStream());
		invoiceWriter.writeUnprintedInvoice(sales, exitItems);
		invoiceWriter.writeMultiplePrintedInvoice(sales, exitItems);
	}
}
