package com.bagongkia.stev.model;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class Order {

	private Integer rowNumber;
	private String orderNumber;
	private String resiNumber;
	private String paymentDate;
	private String totalProductPrice;
	private String orderStatus;
	
	private String currency;
	private BigDecimal totalIncome;
	
}
