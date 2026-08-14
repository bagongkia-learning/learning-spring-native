package com.bagongkia.stev.service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRow;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetData;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bagongkia.stev.ReportException;
import com.bagongkia.stev.model.CustomRow;
import com.bagongkia.stev.model.Income;
import com.bagongkia.stev.model.Order;
import com.bagongkia.stev.model.ReturnedItem;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FileReaderV3 {
	
	@Autowired
	private FileStorageService fileStorageService;
	
	private int convertColumnToIndex(String column) {
		int n = 0;
		for (int i = 1; i <= column.length(); i++) {
			char c =  column.toUpperCase().charAt(column.length() - i);
		    n += (c - 'A' + 1) * Math.pow(26, i - 1);
		}

		return n - 1;
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
		
	public List<Order> readOrderFile(InputStream inputStream) throws IOException, ReportException {
		List<Order> orderList = new ArrayList<>();
		Map<String, String> configMap = fileStorageService.getConfig();
		int orderNoIndex = convertColumnToIndex(configMap.get("laporan.order-v3.no-pesanan"));
        int orderStatusIndex = convertColumnToIndex(configMap.get("laporan.order-v3.status-pesanan"));
        int orderResiIndex = convertColumnToIndex(configMap.get("laporan.order-v3.no-resi"));
        int orderPaymentDateIndex = convertColumnToIndex(configMap.get("laporan.order-v3.waktu-pembayaran"));
        int orderTotalPriceIndex = convertColumnToIndex(configMap.get("laporan.order-v3.total-harga-produk"));
        int rtsTimeIndex = convertColumnToIndex(configMap.get("laporan.order-v3.rts-time"));
        int lastColumnIndex = convertColumnToIndex(configMap.get("laporan.order-v3.last-column"));
		
		Workbook workbook = new XSSFWorkbook(inputStream);
		try {
			Iterator<Sheet> sheetIterator = workbook.iterator();
			if (sheetIterator.hasNext()) {
				Sheet sheet = sheetIterator.next();
				CTWorksheet ctSheet = ((XSSFSheet) sheet).getCTWorksheet();
				CTSheetData ctData = ctSheet.getSheetData();
				List<CTRow> rowList = ctData.getRowList();
				int len = lastColumnIndex + 1;
				int idx = 0;
				int j = -1;
				boolean manual = false;
				for (CTRow row : rowList) {
					if (idx >= len * 2) {
						try {
							CustomRow customRow = new CustomRow(row, ((XSSFSheet) sheet));
							Cell cell1 = customRow.getCell(orderNoIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
							Cell cell2 = customRow.getCell(orderStatusIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
							Cell cell3 = customRow.getCell(orderPaymentDateIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
							Cell cell4 = customRow.getCell(orderTotalPriceIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
							manual = !getStringValueFromCell(cell1).isEmpty()
									&& getStringValueFromCell(cell2).isEmpty()
									&& getStringValueFromCell(cell3).isEmpty()
									&& getStringValueFromCell(cell4).isEmpty();
							break;
						} catch (IllegalStateException e) {
			            	throw new ReportException("PLEASE RECHECK SHEET (" + sheet.getSheetName() + ") ON CELL " + idx % len, e);
			            }
					}
					idx++;
				}

				if (manual) {
					idx = 0;
					for (CTRow row : rowList) {
						if (idx >= len * 2) {
							if (idx % len == 0) {
								j++;
								orderList.add(new Order());
							}
							Order order = orderList.get(j);
							try {
								CustomRow customRow = new CustomRow(row, ((XSSFSheet) sheet));
								if (idx % len == orderNoIndex) {
									Cell cell = customRow.getCell(orderNoIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
									order.setOrderNumber(getStringValueFromCell(cell));
								} else if (idx % len == orderStatusIndex) {
									Cell cell = customRow.getCell(orderStatusIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
									order.setOrderStatus(getStringValueFromCell(cell));
								} else if (idx % len == orderResiIndex) {
									Cell cell = customRow.getCell(orderResiIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
									order.setResiNumber(getStringValueFromCell(cell));
								} else if (idx % len == orderPaymentDateIndex) {
									Cell cell = customRow.getCell(orderPaymentDateIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
									order.setPaymentDate(getStringValueFromCell(cell));
								} else if (idx % len == orderTotalPriceIndex) {
									Cell cell = customRow.getCell(orderTotalPriceIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
									order.setTotalProductPrice(getStringValueFromCell(cell));
								} else if (idx % len == rtsTimeIndex) {
									Cell cell = customRow.getCell(rtsTimeIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
									order.setRtsTime(getStringValueFromCell(cell));
								}
							} catch (IllegalStateException e) {
				            	throw new ReportException("PLEASE RECHECK SHEET (" + sheet.getSheetName() + ") ON CELL " + idx % len, e);
				            }
						}
						idx++;
					}
				} else {
					Iterator<Row> rowIterator = sheet.iterator();
					int rowNum = 1;
				    while (rowIterator.hasNext()) {
				    	Row row = rowIterator.next();
				        int i = 0;
				        if (++rowNum > 3) {
					        Order order = new Order();
					        while (i < row.getLastCellNum()) {
					        	Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
					            try {
				            		if (i == orderNoIndex) {
				            			order.setOrderNumber(getStringValueFromCell(cell));
				            		} else if (i == orderStatusIndex) {
				            			order.setOrderStatus(getStringValueFromCell(cell));
				            		} else if (i == orderResiIndex) {
				            			order.setResiNumber(getStringValueFromCell(cell));
				            		} else if (i == orderPaymentDateIndex) {
					            		order.setPaymentDate(getStringValueFromCell(cell));
				            		} else if (i == orderTotalPriceIndex) {
				            			order.setTotalProductPrice(getStringValueFromCell(cell));
				            		} else if (i == rtsTimeIndex) {
				            			order.setRtsTime(getStringValueFromCell(cell));
				            		}
					            } catch (IllegalStateException e) {
					            	throw new ReportException("PLEASE RECHECK SHEET (" + sheet.getSheetName() + ") ON CELL " + cell.getAddress(), e);
					            }
					            i++;
					        }
	 				        if (order.getOrderNumber() != null && !order.getOrderNumber().isEmpty()) {
					        	orderList.add(order);
					        }
				        }
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
		Map<String, String> configMap = fileStorageService.getConfig();
		int orderNoIndex = convertColumnToIndex(configMap.get("laporan.income-v3.no-pesanan"));
        int amountIndex = convertColumnToIndex(configMap.get("laporan.income-v3.total-penghasilan"));
        int sheetIndex = Integer.valueOf(configMap.get("laporan.income-v3.sheet-index"));
		Workbook workbook = WorkbookFactory.create(inputStream);
		try {
			Sheet sheet = workbook.getSheetAt(sheetIndex - 1);
			Iterator<Row> rowIterator = sheet.iterator();
			int rowNum = 0;
		    while (rowIterator.hasNext()) {
		    	Row row = rowIterator.next();
		        int i = 0;
		        if (++rowNum > 1) {
			        Income income = new Income();
			        while (i < row.getLastCellNum()) {
			        	Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
			            try {
		            		if (i == orderNoIndex) {
		            			income.setOrderNumber(getStringValueFromCell(cell));
		            		} else if (i == amountIndex) {
		            			try {
		            				Pattern p1 = Pattern.compile("[\\d]+");
			            			Matcher m1 = p1.matcher(cell.getStringCellValue());
			            			if (m1.find()) {
			            				income.setAmount(parseBigDecimal(m1.group()));
			            			} else {
			            				income.setAmount(BigDecimal.ZERO);
			            			}
		            			} catch (IllegalStateException e) {
		            				BigDecimal amount = new BigDecimal(cell.getNumericCellValue());
		            				income.setAmount(amount);
		            			}
		            		}
			            } catch (IllegalStateException e) {
			            	throw new ReportException("PLEASE RECHECK SHEET (" + sheet.getSheetName() + ") ON CELL " + cell.getAddress(), e);
			            }
			            i++;
			        }
			        incomeList.add(income);
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
		
		Map<String, String> configMap = fileStorageService.getConfig();
		int orderNoIndex = convertColumnToIndex(configMap.get("laporan.barang-hilang-v3.no-pesanan"));
		int resiNoIndex = convertColumnToIndex(configMap.get("laporan.barang-hilang-v3.no-resi"));
		int paymentDateIndex = convertColumnToIndex(configMap.get("laporan.barang-hilang-v3.waktu-pembayaran"));
		int totalPriceIndex = convertColumnToIndex(configMap.get("laporan.barang-hilang-v3.total-harga"));
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
				        	Cell cell = cellIterator.next();
				            try {
			            		if (i == orderNoIndex) {
			            			order.setOrderNumber(getStringValueFromCell(cell));
			            		} else if (i == resiNoIndex) {
			            			order.setResiNumber(getStringValueFromCell(cell));
			            		} else if (i == paymentDateIndex) {
				            		order.setPaymentDate(getStringValueFromCell(cell));
			            		} else if (i == totalPriceIndex) {
			            			order.setTotalProductPrice(getStringValueFromCell(cell));
			            		}
				            } catch (IllegalStateException e) {
				            	throw new ReportException("PLEASE RECHECK SHEET (" + sheet.getSheetName() + ") ON CELL " + cell.getAddress(), e);
				            }
				            i++;
				        } 
				        if (order.getOrderNumber() != null && !order.getOrderNumber().isEmpty()) {
				        	orderList.add(order);
				        }
			    	}
		        }
			}
		} finally {
			workbook.close();
		}
		log.info("Lost Order List Records size: {}", orderList.size());
		return orderList;
	}
	
	private String getStringCellValue(Cell cell) {
		try {
			return cell.getStringCellValue();
		} catch (IllegalStateException e) {
			return String.valueOf(cell.getNumericCellValue());
		}
	}
	
	private String getStringValueFromCell(Cell cell) {
		final String val = getStringCellValue(cell);
		return val == null ? val : val.trim();
	}
	
	private BigDecimal parseBigDecimal(String value) {
		try {
			final DecimalFormatSymbols symbols = new DecimalFormatSymbols();
			symbols.setGroupingSeparator('.');
			symbols.setDecimalSeparator(',');
			String pattern = "#.##0,0#";
			
			final DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
			decimalFormat.setParseBigDecimal(true);
			
			return (BigDecimal) decimalFormat.parse(value);
		} catch (ParseException |IllegalArgumentException e) {
			try {
				final DecimalFormatSymbols symbols = new DecimalFormatSymbols();
				symbols.setGroupingSeparator(',');
				symbols.setDecimalSeparator('.');
				String pattern = "#,##0.0#";
				
				final DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
				decimalFormat.setParseBigDecimal(true);
				
				return (BigDecimal) decimalFormat.parse(value);
			} catch (ParseException |IllegalArgumentException e1) {
				return new BigDecimal(value);
			}
		}
	}
}
