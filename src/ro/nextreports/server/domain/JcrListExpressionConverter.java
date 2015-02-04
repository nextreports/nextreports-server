package ro.nextreports.server.domain;

import java.util.ArrayList;
import java.util.List;

import org.jcrom.converter.Converter;

public class JcrListExpressionConverter implements Converter<List<String>, List<String>> {	

	private JcrExpressionConverter jc = new JcrExpressionConverter();	
	
	@Override
	public List<String> convertToJcrProperty(List<String> expressionsList) {
		if (expressionsList == null) {
			return null;
		}
		List<String> result = new ArrayList<String>();
		for (String exp : expressionsList) {			
			result.add(jc.convertToJcrProperty(exp));
		}		
		return result;
	}

	@Override
	public List<String> convertToEntityAttribute(List<String> jcrExpressionsList) {
		if (jcrExpressionsList == null) {
			return null;
		}		
		List<String> result = new ArrayList<String>();
		for (String jcrExp : jcrExpressionsList) {
			result.add(jc.convertToEntityAttribute(jcrExp));
		}		
		return result;
	}

}