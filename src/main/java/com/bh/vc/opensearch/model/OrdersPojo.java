package com.bh.vc.opensearch.model;

import java.io.Serializable;

public class OrdersPojo implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String order_no;
	private String product_line;
	private String product_family;
	private String product_sub_family;
	private String product_line0;
	private String product_number;
	private String line_id;
	private String line_item_unit_gm;
	private String mat_doc_desc;
	private String order_line_status;
	private String pms_code;
	private String product_family_code;
	private String sales_order_line;
	private String schedule_ship_date;
	
	public String getOrder_no() {
		return order_no;
	}
	public void setOrder_no(String order_no) {
		this.order_no = order_no;
	}
	public String getProduct_line() {
		return product_line;
	}
	public void setProduct_line(String product_line) {
		this.product_line = product_line;
	}
	public String getProduct_family() {
		return product_family;
	}
	public void setProduct_family(String product_family) {
		this.product_family = product_family;
	}
	public String getProduct_sub_family() {
		return product_sub_family;
	}
	public void setProduct_sub_family(String product_sub_family) {
		this.product_sub_family = product_sub_family;
	}
	public String getProduct_line0() {
		return product_line0;
	}
	public void setProduct_line0(String product_line0) {
		this.product_line0 = product_line0;
	}
	public String getProduct_number() {
		return product_number;
	}
	public void setProduct_number(String product_number) {
		this.product_number = product_number;
	}
	public String getLine_id() {
		return line_id;
	}
	public void setLine_id(String line_id) {
		this.line_id = line_id;
	}
	public String getLine_item_unit_gm() {
		return line_item_unit_gm;
	}
	public void setLine_item_unit_gm(String line_item_unit_gm) {
		this.line_item_unit_gm = line_item_unit_gm;
	}
	public String getMat_doc_desc() {
		return mat_doc_desc;
	}
	public void setMat_doc_desc(String mat_doc_desc) {
		this.mat_doc_desc = mat_doc_desc;
	}
	public String getOrder_line_status() {
		return order_line_status;
	}
	public void setOrder_line_status(String order_line_status) {
		this.order_line_status = order_line_status;
	}
	public String getPms_code() {
		return pms_code;
	}
	public void setPms_code(String pms_code) {
		this.pms_code = pms_code;
	}
	public String getProduct_family_code() {
		return product_family_code;
	}
	public void setProduct_family_code(String product_family_code) {
		this.product_family_code = product_family_code;
	}
	public String getSales_order_line() {
		return sales_order_line;
	}
	public void setSales_order_line(String sales_order_line) {
		this.sales_order_line = sales_order_line;
	}
	public String getSchedule_ship_date() {
		return schedule_ship_date;
	}
	public void setSchedule_ship_date(String schedule_ship_date) {
		this.schedule_ship_date = schedule_ship_date;
	}
	
	@Override
	public String toString() {
		return "OrdersPojo [order_no=" + order_no + ", product_line=" + product_line + ", product_family="
				+ product_family + ", product_sub_family=" + product_sub_family + ", product_line0=" + product_line0
				+ ", product_number=" + product_number + ", line_id=" + line_id + ", line_item_unit_gm="
				+ line_item_unit_gm + ", mat_doc_desc=" + mat_doc_desc + ", order_line_status=" + order_line_status
				+ ", pms_code=" + pms_code + ", product_family_code=" + product_family_code + ", sales_order_line="
				+ sales_order_line + ", schedule_ship_date=" + schedule_ship_date + "]";
	}
	
	
}
