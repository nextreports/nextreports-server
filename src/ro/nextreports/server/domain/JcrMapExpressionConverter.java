package ro.nextreports.server.domain;

import java.util.HashMap;
import java.util.Map;

import org.jcrom.converter.Converter;

public class JcrMapExpressionConverter implements Converter<Map<String, String>, Map<String, String>> {	

	private JcrExpressionConverter jc = new JcrExpressionConverter();	
	
	@Override
	public Map<String, String> convertToJcrProperty(Map<String, String> expressionsMap) {
		if (expressionsMap == null) {
			return null;
		}
		Map<String, String> result = new HashMap<String, String>();
		for (String key : expressionsMap.keySet()) {			
			result.put(jc.convertToJcrProperty(key), jc.convertToJcrProperty(expressionsMap.get(key)));
		}		
		return result;
	}

	@Override
	public Map<String, String> convertToEntityAttribute(Map<String, String> jcrExpressionsList) {
		if (jcrExpressionsList == null) {
			return null;
		}		
		Map<String, String> result = new HashMap<String, String>();
		for (String key : jcrExpressionsList.keySet()) {
			result.put(jc.convertToEntityAttribute(key), jc.convertToEntityAttribute(jcrExpressionsList.get(key)));
		}		
		return result;
	}

}