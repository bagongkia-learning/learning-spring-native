package com.bagongkia.stev.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString
public class StockOutReport implements Serializable {

	private static final long serialVersionUID = 1L;
	private String trackingCode;
}