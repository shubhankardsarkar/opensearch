package com.bh.vc.opensearch.model;

import java.io.Serializable;

public class PartsPojo implements Serializable {
	private static final long serialVersionUID = 1L;
	
//	private Long id;
	private String part_no;
	private String part_desc;

	public PartsPojo() {

	}

//	public Long getId() {
//		return id;
//	}
//
//	public void setId(Long id) {
//		this.id = id;
//	}

	public String getPart_no() {
		return part_no;
	}

	public void setPart_no(String part_no) {
		this.part_no = part_no;
	}

	public String getPart_desc() {
		return part_desc;
	}

	public void setPart_desc(String part_desc) {
		this.part_desc = part_desc;
	}

	@Override
	public String toString() {
		return "PartsInventory [part_no=" + part_no + ", part_desc=" + part_desc + "]";
	}

}
