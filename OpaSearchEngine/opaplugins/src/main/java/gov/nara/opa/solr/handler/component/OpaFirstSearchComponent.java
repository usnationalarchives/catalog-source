package gov.nara.opa.solr.handler.component;

import java.io.IOException;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;

public class OpaFirstSearchComponent extends SearchComponent {

	@Override
	public void prepare(ResponseBuilder rb) throws IOException {
		String s = "OpaFirstSearchComponent::prepare";
		System.out.println(s);
	}

	@Override
	public void process(ResponseBuilder rb) throws IOException {
		String s = "OpaFirstSearchComponent::process";
		System.out.println(s);
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		String s = "this is the description of OpaFirstSearchComponent";
		return null;
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return super.getVersion();
	}

	@Override
	public NamedList getStatistics() {
		// TODO Auto-generated method stub
		return super.getStatistics();
	}

	@Override
	public String getSource() {
		// TODO Auto-generated method stub
		return null;
	}

}
