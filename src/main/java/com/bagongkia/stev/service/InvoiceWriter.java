package com.bagongkia.stev.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.CellStyle;
//import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.bagongkia.stev.model.ExitItem;
import com.bagongkia.stev.model.Invoice;
import com.bagongkia.stev.model.Sale;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class InvoiceWriter {
	
	public void writeUnprintedInvoice(List<Sale> sales, List<ExitItem> exitItems) throws IOException {
		Map<String, Invoice> unprintedInvoiceMap = new LinkedHashMap<>();
		sales.stream()
			.filter(item -> !isTrackingCodeExistsInExitItems(exitItems, item.getTrackingCode()))
			.forEach(item -> {
				Invoice invoice = new Invoice();
				invoice.setOrderNumber(item.getOrderNumber());
				invoice.setShippingName(item.getShippingName());
				invoice.setTrackingCode(item.getTrackingCode());
				
				unprintedInvoiceMap.put(item.getOrderNumber(), invoice);
			});
		log.info("Unprinted Invoice Size: {}", unprintedInvoiceMap.size());
		
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("Sheet 1");
		
//		Font headerFont = workbook.createFont();
//	    headerFont.setBold(true);
//	    
//	    CellStyle headerCellStyle = workbook.createCellStyle();
//	    headerCellStyle.setFont(headerFont);
	    
	    Integer i = 0;
		
		Row row1 = sheet.createRow(i);
		Cell cell0s = row1.createCell(0);
		Cell cell1s = row1.createCell(1);
		Cell cell2s = row1.createCell(2);
		Cell cell3s = row1.createCell(3);
		
		cell0s.setCellValue("No.");
//		cell0s.setCellStyle(headerCellStyle);
		cell1s.setCellValue("Order Number");
//		cell1s.setCellStyle(headerCellStyle);
		cell2s.setCellValue("Shipping Name");
//		cell2s.setCellStyle(headerCellStyle);
		cell3s.setCellValue("Tracking Code");
//		cell3s.setCellStyle(headerCellStyle);
		i++;
		
		for (Map.Entry<String, Invoice> entry : unprintedInvoiceMap.entrySet()) {
			Row row = sheet.createRow(i);
			Cell cell0 = row.createCell(0);
			Cell cell1 = row.createCell(1);
			Cell cell2 = row.createCell(2);
			Cell cell3 = row.createCell(3);
			
			cell0.setCellValue(i);
			cell1.setCellValue(entry.getValue().getOrderNumber());
			cell2.setCellValue(entry.getValue().getShippingName());
			cell3.setCellValue(entry.getValue().getTrackingCode());
			i++;
		}
		
//		sheet.autoSizeColumn(0);
//		sheet.autoSizeColumn(1);
//		sheet.autoSizeColumn(2);
//		sheet.autoSizeColumn(3);
		
		Path path = Paths.get("download").toAbsolutePath().normalize();
		Files.createDirectories(path);
		
		FileOutputStream file = new FileOutputStream("download/unprinted-invoice-report.xlsx");
		workbook.write(file);
		
		file.close();
		workbook.close();
	}

	public void writeMultiplePrintedInvoice(List<Sale> sales, List<ExitItem> exitItems) throws IOException {
		Map<String, Invoice> multiplePrintedInvoiceMap = new LinkedHashMap<>();
		Map<String, ExitItem> exitItemMap = new HashMap<>();
		Map<String, ExitItem> duplicateExitItemMap = new HashMap<>();
		
		exitItems.forEach(item -> {
			if (item.getTrackingCode() != null) {
				String trackingCode = item.getTrackingCode().trim().toUpperCase();
				if (exitItemMap.containsKey(trackingCode)) {
					log.debug("Found Duplicate Item: {}", item);
					duplicateExitItemMap.put(trackingCode, item);
				} else {
					exitItemMap.put(trackingCode, item);
				}
			}
		});
		
		sales.stream()
			.filter(item -> {
				if (item.getTrackingCode() != null) {
					return duplicateExitItemMap.containsKey(item.getTrackingCode().trim().toUpperCase());
				} else {
					return false;
				}
			})
			.forEach(item -> {
				Invoice invoice = new Invoice();
				invoice.setOrderNumber(item.getOrderNumber());
				invoice.setShippingName(item.getShippingName());
				invoice.setTrackingCode(item.getTrackingCode());

				multiplePrintedInvoiceMap.put(invoice.getOrderNumber(), invoice);
			});
		log.info("Multiple Printed Invoice Size: {}", multiplePrintedInvoiceMap.size());
		
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("Sheet 1");
		
//		Font headerFont = workbook.createFont();
//	    headerFont.setBold(true);
	    
//	    CellStyle headerCellStyle = workbook.createCellStyle();
//	    headerCellStyle.setFont(headerFont);
	    
	    Integer i = 0;
		
		Row row1 = sheet.createRow(i);
		Cell cell0s = row1.createCell(0);
		Cell cell1s = row1.createCell(1);
		Cell cell2s = row1.createCell(2);
		Cell cell3s = row1.createCell(3);
		
		cell0s.setCellValue("No.");
//		cell0s.setCellStyle(headerCellStyle);
		cell1s.setCellValue("Order Number");
//		cell1s.setCellStyle(headerCellStyle);
		cell2s.setCellValue("Shipping Name");
//		cell2s.setCellStyle(headerCellStyle);
		cell3s.setCellValue("Tracking Code");
//		cell3s.setCellStyle(headerCellStyle);
		i++;
		
		for (Map.Entry<String, Invoice> entry : multiplePrintedInvoiceMap.entrySet()) {
			Row row = sheet.createRow(i);
			Cell cell0 = row.createCell(0);
			Cell cell1 = row.createCell(1);
			Cell cell2 = row.createCell(2);
			Cell cell3 = row.createCell(3);
			
			cell0.setCellValue(i);
			cell1.setCellValue(entry.getValue().getOrderNumber());
			cell2.setCellValue(entry.getValue().getShippingName());
			cell3.setCellValue(entry.getValue().getTrackingCode());
			i++;
		}
		
//		sheet.autoSizeColumn(0);
//		sheet.autoSizeColumn(1);
//		sheet.autoSizeColumn(2);
//		sheet.autoSizeColumn(3);
		
		Path path = Paths.get("download").toAbsolutePath().normalize();
		Files.createDirectories(path);
		
		FileOutputStream file = new FileOutputStream("download/multiple-printed-invoice-report.xlsx");
		workbook.write(file);
		
		file.close();
		workbook.close();
	}
	
	private Boolean isTrackingCodeExistsInExitItems(List<ExitItem> exitItems, String trackingCode) {
		if (trackingCode == null || trackingCode.isEmpty()) {
			return false;
		}
		return exitItems.stream().anyMatch(item -> item != null && item.getTrackingCode() != null && trackingCode.trim().equalsIgnoreCase(item.getTrackingCode().trim()));
	}
}
