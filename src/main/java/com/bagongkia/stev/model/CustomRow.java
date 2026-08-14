package com.bagongkia.stev.model;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRow;

public class CustomRow extends XSSFRow {

	public CustomRow(CTRow row, XSSFSheet sheet) {
		super(row, sheet);
	}

}
