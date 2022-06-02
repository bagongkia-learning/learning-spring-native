package com.bagongkia.stev.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;

import com.bagongkia.stev.ReportException;
import com.bagongkia.stev.model.ExitItem;
import com.bagongkia.stev.model.Income;
import com.bagongkia.stev.model.LostItem;
import com.bagongkia.stev.model.Order;
import com.bagongkia.stev.model.Payment;
import com.bagongkia.stev.model.ReturnedItem;
import com.bagongkia.stev.model.Sale;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FileReader {

	public List<Payment> readPaymentFile(InputStream inputStream) throws ReportException, IOException {
		List<Payment> payments = new ArrayList<>();
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		int row = 0;
		try (CSVReader br = new CSVReader(inputStreamReader)) {
			try {
				String[] line;
				while ((line = br.readNext()) != null) {
					row++;
					if (row == 1) {
						continue; 
					}
					Payment payment = new Payment();
					payment.setOrderNumber(line[13]);
					payments.add(payment);
				}
			} catch (Exception e) {
				throw new ReportException("PLEASE RECHECK ROW " + row, e);
			} finally {
				br.close();
			}
			log.info("Payment Records size: {}", payments.size());
		} finally {
			inputStreamReader.close();
		}
		return payments;
	}
	
	public List<Sale> readSalesFile(InputStream inputStream) throws ReportException, IOException {
		List<Sale> sales = new ArrayList<>();
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		int row = 0;
		CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
		try (CSVReader br = new CSVReaderBuilder(inputStreamReader).withCSVParser(parser).build()) {
			String[] line;
			try {
				while ((line = br.readNext()) != null) {
					row++;
					if (row == 1) {
						continue; 
					}
					
					Sale sale = new Sale();
					sale.setOrderNumber(line[9]);
					sale.setShippingName(line[14]);
					
					if (line[37] != null) {
						sale.setPaidAmount(new BigDecimal(line[38]));
					}
					sale.setTrackingCode(line[49]);
					sales.add(sale);
				}
				log.info("Sales Records size: {}", sales.size());
			} catch(Exception e) {
				throw new ReportException("PLEASE RECHECK ROW " + row, e);
			} finally {
				br.close();
			}
		} finally {
			inputStreamReader.close();
		}
		return sales;
	}	

	public List<ReturnedItem> readReturnedItemsFile(InputStream inputStream) throws EncryptedDocumentException, IOException, ReportException {
		List<ReturnedItem> returnedItems = new ArrayList<>();
		Workbook workbook = WorkbookFactory.create(inputStream);
		try {
			Iterator<Sheet> sheetIterator = workbook.iterator();
			while (sheetIterator.hasNext()) {
				Sheet sheet = sheetIterator.next();
				Iterator<Row> rowIterator = sheet.iterator();
			    while (rowIterator.hasNext()) {
			    	Row row = rowIterator.next();
			        Iterator<Cell> cellIterator = row.cellIterator();
			        while (cellIterator.hasNext()) {
			        	Cell cell = cellIterator.next();
			            ReturnedItem item = new ReturnedItem();
			            try {
			            	try {
			            		item.setTrackingCode(cell.getStringCellValue());
			            	} catch (IllegalStateException e) {
			            		BigDecimal trackingCode = new BigDecimal(cell.getNumericCellValue());
				            	if (trackingCode.compareTo(BigDecimal.ZERO) > 0) {
				            		item.setTrackingCode(trackingCode.toPlainString());
				            	}
			            	}
			            } catch (IllegalStateException e) {
			            	throw new ReportException("PLEASE RECHECK SHEET (" + sheet.getSheetName() + ") ON CELL " + cell.getAddress(), e);
			            }
			            returnedItems.add(item);
			        }
		        }
			}
		} finally {
			workbook.close();
		}
		log.info("Returned Items Records size: {}", returnedItems.size());
		return returnedItems;
	}
	
	public List<LostItem> readLostItemsFile(InputStream inputStream) throws EncryptedDocumentException, IOException, ReportException {
		List<LostItem> lostItems = new ArrayList<>();
		Workbook workbook = WorkbookFactory.create(inputStream);
		try {
			Iterator<Sheet> sheetIterator = workbook.iterator();
			while (sheetIterator.hasNext()) {
				Sheet sheet = sheetIterator.next();
				Iterator<Row> rowIterator = sheet.iterator();
				rowIterator.next();
		        while (rowIterator.hasNext()) {
		            Row row = rowIterator.next();
		            
		            try {
			            LostItem item = new LostItem();
			            Cell cell1 = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
			            Cell cell2 = row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
			            try {
			            	item.setOrderNumber(cell1.getStringCellValue());
			            } catch (IllegalStateException e) {
			            	BigDecimal orderNumber = new BigDecimal(cell1.getNumericCellValue());
			            	if (orderNumber.compareTo(BigDecimal.ZERO) > 0) {
			            		item.setOrderNumber(orderNumber.toPlainString());
			            	}
			            }
			            try {
			            	item.setTrackingCode(cell2.getStringCellValue());
			            } catch (IllegalStateException e) {
			            	BigDecimal trackingCode = new BigDecimal(cell2.getNumericCellValue());
			            	if (trackingCode.compareTo(BigDecimal.ZERO) > 0) {
			            		item.setTrackingCode(trackingCode.toPlainString());
			            	}
			            }
			            
			            Cell cell3 = row.getCell(3, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
			            item.setPaidAmount(new BigDecimal(cell3.getNumericCellValue()));
			            if (item.getOrderNumber() != null && !item.getOrderNumber().isEmpty()) {
			            	lostItems.add(item);
			            }
		            } catch (IllegalStateException e) {
		            	throw new ReportException("PLEASE RECHECK SHEET (" + sheet.getSheetName() + ") ON ROW " + row.getRowNum(), e);		            	
		            }
		        }
			}
		} finally {
			workbook.close();
		}
		log.info("Lost Items Records size: {}", lostItems.size());
		return lostItems;
	}

	public List<ExitItem> readExitItemsFile(InputStream inputStream) throws EncryptedDocumentException, IOException, ReportException {
		List<ExitItem> exitItems = new ArrayList<>();
		Workbook workbook = WorkbookFactory.create(inputStream);
		try {
			Iterator<Sheet> sheetIterator = workbook.iterator();
			while (sheetIterator.hasNext()) {
				Sheet sheet = sheetIterator.next();
				Iterator<Row> rowIterator = sheet.iterator();
			    while (rowIterator.hasNext()) {
			    	Row row = rowIterator.next();
			        Iterator<Cell> cellIterator = row.cellIterator();
			        while (cellIterator.hasNext()) {
			        	Cell cell = cellIterator.next();
			            ExitItem item = new ExitItem();
			            try {
			            	try {
			            		item.setTrackingCode(cell.getStringCellValue());
			            	} catch (IllegalStateException e) {
			            		BigDecimal trackingCode = new BigDecimal(cell.getNumericCellValue());
				            	if (trackingCode.compareTo(BigDecimal.ZERO) > 0) {
				            		item.setTrackingCode(trackingCode.toPlainString());
				            	}
			            	}
			            } catch (IllegalStateException e) {
			            	throw new ReportException("PLEASE RECHECK SHEET (" + sheet.getSheetName() + ") ON CELL " + cell.getAddress(), e);
			            }
			            exitItems.add(item);
			        }
		        }
			}
		} finally {
			workbook.close();
		}
		log.info("Exit Items Records size: {}", exitItems.size());
		return exitItems;
	}
	
	public List<Order> readOrderFile(InputStream inputStream) throws IOException, ReportException {
		List<Order> orderList = new ArrayList<>();
		Workbook workbook = WorkbookFactory.create(inputStream);
		try {
			Iterator<Sheet> sheetIterator = workbook.iterator();
			int i = 0;
			if (sheetIterator.hasNext()) {
				Sheet sheet = sheetIterator.next();
				Iterator<Row> rowIterator = sheet.iterator();
				int rowNum = 0;
			    while (rowIterator.hasNext()) {
			    	Row row = rowIterator.next();
			        Iterator<Cell> cellIterator = row.cellIterator();
			        i = 0;
			        if (++rowNum > 1) {
				        Order order = new Order();
				        while (cellIterator.hasNext()) {
				        	i++;
				        	Cell cell = cellIterator.next();
				            try {
	//			            	try {
				            		if (i == 1) {
				            			order.setOrderNumber(cell.getStringCellValue());
				            		} else if (i == 2) {
				            			order.setOrderStatus(cell.getStringCellValue());
				            		} else if (i == 4) {
				            			order.setResiNumber(cell.getStringCellValue());
				            		} else if (i == 10) {
					            		order.setPaymentDate(cell.getStringCellValue());
				            		} else if (i == 18) {
				            			order.setTotalProductPrice(cell.getStringCellValue());
				            		}
	//			            	} catch (IllegalStateException e) {
	//			            		BigDecimal trackingCode = new BigDecimal(cell.getNumericCellValue());
	//				            	if (trackingCode.compareTo(BigDecimal.ZERO) > 0) {
	//				            		item.setTrackingCode(trackingCode.toPlainString());
	//				            	}
	//			            	}
				            } catch (IllegalStateException e) {
				            	throw new ReportException("PLEASE RECHECK SHEET (" + sheet.getSheetName() + ") ON CELL " + cell.getAddress(), e);
				            }
				        }
				        orderList.add(order);
			        }
		        }
			}
		} finally {
			workbook.close();
		}
		log.info("Order List Records size: {}", orderList.size());
		return orderList;
	}
	
	public List<Income> readIncomeFile(InputStream inputStream) throws IOException, ReportException {
		List<Income> incomeList = new ArrayList<>();
		Workbook workbook = WorkbookFactory.create(inputStream);
		try {
			Iterator<Sheet> sheetIterator = workbook.iterator();
			int i = 0;
			if (sheetIterator.hasNext()) {
				Sheet sheet = sheetIterator.next();
				Iterator<Row> rowIterator = sheet.iterator();
				int rowNum = 0;
			    while (rowIterator.hasNext()) {
			    	Row row = rowIterator.next();
			        Iterator<Cell> cellIterator = row.cellIterator();
			        i = 0;
			        if (++rowNum > 6) {
				        Income income = new Income();
				        while (cellIterator.hasNext()) {
				        	i++;
				        	Cell cell = cellIterator.next();
				            try {
	//			            	try {
				            		if (i == 2) {
				            			income.setOrderNumber(cell.getStringCellValue());
				            		} else if (i == 22) {
				            			Pattern p1 = Pattern.compile("[\\d]+");
				            			Matcher m1 = p1.matcher(cell.getStringCellValue());
				            			
				            			if (m1.find()) {
				            				income.setAmount(new BigDecimal(m1.group()));
				            			} else {
				            				income.setAmount(BigDecimal.ZERO);
				            			}
				            		}
	//			            	} catch (IllegalStateException e) {
	//			            		BigDecimal trackingCode = new BigDecimal(cell.getNumericCellValue());
	//				            	if (trackingCode.compareTo(BigDecimal.ZERO) > 0) {
	//				            		item.setTrackingCode(trackingCode.toPlainString());
	//				            	}
	//			            	}
				            } catch (IllegalStateException e) {
				            	throw new ReportException("PLEASE RECHECK SHEET (" + sheet.getSheetName() + ") ON CELL " + cell.getAddress(), e);
				            }
				        }
				        incomeList.add(income);
			        }
		        }
			}
		} finally {
			workbook.close();
		}
		log.info("Income List Records size: {}", incomeList.size());
		return incomeList;
	}
	
	public List<Order> readLostOrdersFile(InputStream inputStream) throws IOException, ReportException {
		List<Order> orderList = new ArrayList<>();
		Workbook workbook = WorkbookFactory.create(inputStream);
		try {
			Iterator<Sheet> sheetIterator = workbook.iterator();
			int i = 0;
			if (sheetIterator.hasNext()) {
				Sheet sheet = sheetIterator.next();
				Iterator<Row> rowIterator = sheet.iterator();
				int rowNum = 0;
			    while (rowIterator.hasNext()) {
			    	Row row = rowIterator.next();
			    	if (++rowNum > 1) {
				        Iterator<Cell> cellIterator = row.cellIterator();
				        i = 0;
				        Order order = new Order();
				        while (cellIterator.hasNext()) {
				        	i++;
				        	Cell cell = cellIterator.next();
				            try {
	//			            	try {
				            		if (i == 2) {
				            			order.setOrderNumber(cell.getStringCellValue());
				            		} else if (i == 3) {
				            			order.setResiNumber(cell.getStringCellValue());
				            		} else if (i == 4) {
					            		order.setPaymentDate(cell.getStringCellValue());
				            		} else if (i == 5) {
				            			try {
				            				order.setTotalProductPrice(cell.getStringCellValue());
				            			} catch (IllegalStateException e) {
							            	order.setTotalProductPrice(String.valueOf(cell.getNumericCellValue()));
						            	}
				            		} else if (i == 6) {
				            			order.setOrderStatus(cell.getStringCellValue());
				            		}
	//			            	} catch (IllegalStateException e) {
	//			            		BigDecimal trackingCode = new BigDecimal(cell.getNumericCellValue());
	//				            	if (trackingCode.compareTo(BigDecimal.ZERO) > 0) {
	//				            		item.setTrackingCode(trackingCode.toPlainString());
	//				            	}
	//			            	}
				            } catch (IllegalStateException e) {
				            	throw new ReportException("PLEASE RECHECK SHEET (" + sheet.getSheetName() + ") ON CELL " + cell.getAddress(), e);
				            }
				        }
				        orderList.add(order);
			    	}
		        }
			}
		} finally {
			workbook.close();
		}
		log.info("Order List Records size: {}", orderList.size());
		return orderList;
	}
}
