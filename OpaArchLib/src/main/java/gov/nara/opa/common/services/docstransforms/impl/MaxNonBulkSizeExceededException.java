package gov.nara.opa.common.services.docstransforms.impl;

public class MaxNonBulkSizeExceededException extends Exception{
	public MaxNonBulkSizeExceededException(String msg){
		super(msg);
	}
}