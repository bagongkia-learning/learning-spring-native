package com.bagongkia.stev.model;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString
public class Payment {

	private String orderNumber;
	private String orderItemNumber;
	private BigDecimal amount;
}
