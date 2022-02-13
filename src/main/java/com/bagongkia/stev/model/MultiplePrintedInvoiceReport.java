package com.bagongkia.stev.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString
public class MultiplePrintedInvoiceReport {

	private String orderNumber;
	private String trackingCode;
	private String shippingName;
}
