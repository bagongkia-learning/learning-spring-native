package com.bagongkia.stev.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

import com.bagongkia.stev.model.LostItem;
import com.bagongkia.stev.model.Payment;
import com.bagongkia.stev.model.ReturnedItem;
import com.bagongkia.stev.model.Sale;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LostItemsFileWriter {

	public void write(List<LostItem> lostItems, List<Sale> sales, List<Payment> payments, List<ReturnedItem> returnedItems) throws IOException {
		Map<String, BigDecimal> lostItemsMap = new LinkedHashMap<>();
		lostItems.stream()
			.filter(item -> !isOrderNumberExistsInPayments(payments, item.getOrderNumber()))
			.filter(item -> !isTrackingCodeExistsInReturnedItems(returnedItems, item.getTrackingCode()))
			.forEach(item -> {
				LostItem lostItem = new LostItem();
				lostItem.setOrderNumber(item.getOrderNumber());
				lostItem.setTrackingCode(item.getTrackingCode());
				lostItem.setPaidAmount(item.getPaidAmount());
				
				if (lostItemsMap.containsKey(lostItem.getOrderNumberAndTrackingCode())) {
					BigDecimal sumPaidAmount = lostItem.getPaidAmount() == null ? BigDecimal.ZERO : lostItem.getPaidAmount();
					BigDecimal currentPaidAmount = lostItemsMap.get(lostItem.getOrderNumberAndTrackingCode());
					lostItemsMap.put(lostItem.getOrderNumberAndTrackingCode(), currentPaidAmount.add(sumPaidAmount));
				} else {
					lostItemsMap.put(lostItem.getOrderNumberAndTrackingCode(), lostItem.getPaidAmount());
				}
			});
		
		sales.stream()
			.filter(item -> !isOrderNumberExistsInPayments(payments, item.getOrderNumber()))
			.filter(item -> !isTrackingCodeExistsInReturnedItems(returnedItems, item.getTrackingCode()))
			.forEach(item -> {
				LostItem lostItem = new LostItem();
				lostItem.setOrderNumber(item.getOrderNumber());
				lostItem.setTrackingCode(item.getTrackingCode());
				lostItem.setPaidAmount(item.getPaidAmount());
			
				if (lostItemsMap.containsKey(lostItem.getOrderNumberAndTrackingCode())) {
					BigDecimal sumPaidAmount = lostItem.getPaidAmount() == null ? BigDecimal.ZERO : lostItem.getPaidAmount();
					BigDecimal currentPaidAmount = lostItemsMap.get(lostItem.getOrderNumberAndTrackingCode());
					lostItemsMap.put(lostItem.getOrderNumberAndTrackingCode(), currentPaidAmount.add(sumPaidAmount));
				} else {
					lostItemsMap.put(lostItem.getOrderNumberAndTrackingCode(), lostItem.getPaidAmount());
				}
			});
	
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
		cell2s.setCellValue("Tracking Code");
//		cell2s.setCellStyle(headerCellStyle);
		cell3s.setCellValue("Total Invoice");
//		cell3s.setCellStyle(headerCellStyle);
		i++;
		
		for (Map.Entry<String, BigDecimal> entry : lostItemsMap.entrySet()) {
			Row row = sheet.createRow(i);
			Cell cell0 = row.createCell(0);
			Cell cell1 = row.createCell(1);
			Cell cell2 = row.createCell(2);
			Cell cell3 = row.createCell(3);
			
			cell0.setCellValue(i);
			String[] str = entry.getKey().split(";");
			if (str.length >= 1) {
				cell1.setCellValue(str[0]);
			}
			if (str.length >= 2) {
				cell2.setCellValue(str[1]);
			}
			cell3.setCellValue(entry.getValue().doubleValue());
			i++;
		}
		
//		sheet.autoSizeColumn(0);
//		sheet.autoSizeColumn(1);
//		sheet.autoSizeColumn(2);
//		sheet.autoSizeColumn(3);
		
		Path path = Paths.get("download").toAbsolutePath().normalize();
		Files.createDirectories(path);
		
		FileOutputStream file = new FileOutputStream("download/lost-items-report.xlsx");
		workbook.write(file);
		
		file.close();
		workbook.close();
	}
	
	private Boolean isOrderNumberExistsInPayments(List<Payment> payments, String orderNumber) {
		if (orderNumber == null || orderNumber.isEmpty()) {
			return false;
		}
		return payments.stream().anyMatch(item -> {
			Boolean exist = item != null && item.getOrderNumber() != null && orderNumber.trim().equalsIgnoreCase(item.getOrderNumber().trim());
			if (exist) {
				log.debug(item.getOrderNumber());
			}
			return exist;
		});
	}
	
	private Boolean isTrackingCodeExistsInReturnedItems(List<ReturnedItem> returnedItems, String trackingCode) {
		if (trackingCode == null || trackingCode.isEmpty()) {
			return false;
		}
		return returnedItems.stream().anyMatch(item -> {
			Boolean exist = item != null && item.getTrackingCode() != null && trackingCode.trim().equalsIgnoreCase(item.getTrackingCode().trim());
			if (exist) {
				log.debug(item.getTrackingCode());
			}
			return exist;
		});
	}
}
