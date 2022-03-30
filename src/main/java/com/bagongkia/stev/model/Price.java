package com.bagongkia.stev.model;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Price {

	private String currency;
	private BigDecimal amount;
	
}
