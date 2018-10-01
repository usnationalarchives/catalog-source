package gov.nara.opa.common.services.docstransforms.impl;

public class MaxNonBulkTimeExceededException extends Exception{
	public MaxNonBulkTimeExceededException(String msg){
		super(msg);
	}
}