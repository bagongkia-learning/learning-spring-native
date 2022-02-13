package com.bagongkia.stev.model;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString
public class PaymentReport implements Serializable {

	private static final long serialVersionUID = 1L;
	private String orderNumber;
	private String orderItemNumber;
	private BigDecimal amount;
}