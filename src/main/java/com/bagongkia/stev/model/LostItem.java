package com.bagongkia.stev.model;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString
public class LostItem implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String orderNumber;
	private String trackingCode;
	private BigDecimal paidAmount;
	
	public String getOrderNumberAndTrackingCode() {
		return (orderNumber == null ? "" : orderNumber).concat(";").concat(trackingCode == null ? "" : trackingCode).toUpperCase();
	}
}