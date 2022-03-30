package com.bagongkia.stev.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.CellStyle;
//import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.bagongkia.stev.model.Income;
import com.bagongkia.stev.model.Order;
import com.bagongkia.stev.model.Price;
import com.bagongkia.stev.model.ReturnedItem;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LostOrdersFileWriter {

	public void write(List<Order> lostOrders, List<Order> orders, List<Income> incomes, List<ReturnedItem> returnedItems) throws IOException {
		Map<String, Order> lostOrdersMap = new LinkedHashMap<>();
		lostOrders.stream()
			.filter(item -> !isOrderNumberExistsInIncomes(incomes, item.getOrderNumber()))
			.filter(item -> !isTrackingCodeExistsInReturnedItems(returnedItems, item.getResiNumber()))
			.forEach(item -> {
				Order order = new Order();
				order.setOrderNumber(item.getOrderNumber());
				order.setResiNumber(item.getResiNumber());
				order.setPaymentDate(item.getPaymentDate());
				order.setTotalProductPrice(item.getTotalProductPrice());
				order.setOrderStatus(item.getOrderStatus());
				
				Price price = extractPrice(order.getTotalProductPrice());
				String key = order.getOrderNumber() + ";" + price.getCurrency();
				if (lostOrdersMap.containsKey(key)) {
					BigDecimal sumPrice =  price.getAmount() == null ? BigDecimal.ZERO : price.getAmount();
					BigDecimal currentPrice = lostOrdersMap.get(key).getSumProductPrice();
					order.setCurrency(price.getCurrency());
					order.setSumProductPrice(currentPrice.add(sumPrice));
					lostOrdersMap.put(key, order);
				} else {
					order.setCurrency(price.getCurrency());
					order.setSumProductPrice(price.getAmount());
					lostOrdersMap.put(key, order);
				}
			});
		
		orders.stream()
			.filter(item -> !isOrderNumberExistsInIncomes(incomes, item.getOrderNumber()))
			.filter(item -> !isTrackingCodeExistsInReturnedItems(returnedItems, item.getResiNumber()))
			.forEach(item -> {
				Order order = new Order();
				order.setOrderNumber(item.getOrderNumber());
				order.setResiNumber(item.getResiNumber());
				order.setPaymentDate(item.getPaymentDate());
				order.setTotalProductPrice(item.getTotalProductPrice());
				order.setOrderStatus(item.getOrderStatus());
				
				Price price = extractPrice(order.getTotalProductPrice());
				String key = order.getOrderNumber() + ";" + price.getCurrency();
				if (lostOrdersMap.containsKey(key)) {
					BigDecimal sumPrice =  price.getAmount() == null ? BigDecimal.ZERO : price.getAmount();
					BigDecimal currentPrice = lostOrdersMap.get(key).getSumProductPrice();
					order.setCurrency(price.getCurrency());
					order.setSumProductPrice(currentPrice.add(sumPrice));
					lostOrdersMap.put(key, order);
				} else {
					order.setCurrency(price.getCurrency());
					order.setSumProductPrice(price.getAmount());
					lostOrdersMap.put(key, order);
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
		Cell cell4s = row1.createCell(4);
		Cell cell5s = row1.createCell(5);
		
		cell0s.setCellValue("No.");
//		cell0s.setCellStyle(headerCellStyle);
		cell1s.setCellValue("Nomor Pesanan");
//		cell1s.setCellStyle(headerCellStyle);
		cell2s.setCellValue("Nomor Resi");
//		cell2s.setCellStyle(headerCellStyle);
		cell3s.setCellValue("Waktu Pembayaran Dilakukan");
//		cell3s.setCellStyle(headerCellStyle);
		cell4s.setCellValue("Total Harga Produk");
//		cell4s.setCellStyle(headerCellStyle);
		cell5s.setCellValue("Status Pesanan");
//		cell5s.setCellStyle(headerCellStyle);
		i++;
		
		DecimalFormat df = new DecimalFormat("###,##0");

        DecimalFormatSymbols customSymbol = new DecimalFormatSymbols();
        customSymbol.setGroupingSeparator('.');
        df.setDecimalFormatSymbols(customSymbol);

		for (Map.Entry<String, Order> entry : lostOrdersMap.entrySet()) {
			Order order = entry.getValue();
			Row row = sheet.createRow(i);
			Cell cell0 = row.createCell(0);
			Cell cell1 = row.createCell(1);
			Cell cell2 = row.createCell(2);
			Cell cell3 = row.createCell(3);
			Cell cell4 = row.createCell(4);
			Cell cell5 = row.createCell(5);
			
			cell0.setCellValue(i);
			cell1.setCellValue(order.getOrderNumber());
			cell2.setCellValue(order.getResiNumber());
			cell3.setCellValue(order.getPaymentDate());
			cell4.setCellValue(order.getCurrency() + df.format(order.getSumProductPrice().longValue()));
			cell5.setCellValue(order.getOrderStatus());
			i++;
		}
		
//		sheet.autoSizeColumn(0);
//		sheet.autoSizeColumn(1);
//		sheet.autoSizeColumn(2);
//		sheet.autoSizeColumn(3);
		
		Path path = Paths.get("download").toAbsolutePath().normalize();
		Files.createDirectories(path);
		
		FileOutputStream file = new FileOutputStream("download/lost-orders-report.xlsx");
		workbook.write(file);
		
		file.close();
		workbook.close();
	}
	
	private Price extractPrice(String priceStr) {
		Price price = new Price();
		
		Pattern p1 = Pattern.compile("[\\d,.]+");
		Matcher m1 = p1.matcher(priceStr);
		if (m1.find()) {
			try {
				DecimalFormatSymbols symbols = new DecimalFormatSymbols();
				symbols.setGroupingSeparator('.');
				symbols.setDecimalSeparator(',');
				String pattern = "#,##0.0#";
				DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
				decimalFormat.setParseBigDecimal(true);
				
				price.setAmount((BigDecimal) decimalFormat.parse(m1.group()));
			} catch (ParseException e) {
				DecimalFormatSymbols symbols = new DecimalFormatSymbols();
				symbols.setGroupingSeparator(',');
				symbols.setDecimalSeparator('.');
				String pattern = "#,##0.0#";
				DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
				decimalFormat.setParseBigDecimal(true);
				
				try {
					price.setAmount((BigDecimal) decimalFormat.parse(m1.group()));
				} catch (ParseException e1) {
					price.setAmount(BigDecimal.ZERO);
				}
			}
		}

		Pattern p2 = Pattern.compile("[a-zA-Z ]+");
		Matcher m2 = p2.matcher(priceStr);
		if (m2.find()) {
		  price.setCurrency(m2.group());
		}
		return price;
	}
	
	private Boolean isOrderNumberExistsInIncomes(List<Income> incomes, String orderNumber) {
		if (orderNumber == null || orderNumber.isEmpty()) {
			return false;
		}
		return incomes.stream().anyMatch(item -> {
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