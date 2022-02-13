package com.bagongkia.stev.model;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString
public class Sale {

	private String orderNumber;
	private String shippingName;
	private String trackingCode;
	private BigDecimal paidAmount;
}
