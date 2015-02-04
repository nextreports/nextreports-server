package ro.nextreports.server.domain;

import java.util.Arrays;
import java.util.List;

import org.jcrom.converter.Converter;

public class JcrExpressionConverter implements Converter<String, String> {

	private final String MUL = "*";
	private final String DIV = "/";
	private final String MUL_JCR = "#MUL#";
	private final String DIV_JCR = "#DIV#";

	private List<String> userSequences = Arrays.asList(MUL, DIV);
	private List<String> jcrSequences  = Arrays.asList(MUL_JCR, DIV_JCR);

	@Override
	public String convertToJcrProperty(String userExpression) {
		for (int i=0, size=userSequences.size(); i<size; i++) {
			userExpression = userExpression.replace(userSequences.get(i), jcrSequences.get(i));
		}		
		return userExpression;
	}

	@Override
	public String convertToEntityAttribute(String jcrExpression) {
		for (int i=0, size=jcrSequences.size(); i<size; i++) {
			jcrExpression = jcrExpression.replace(jcrSequences.get(i), userSequences.get(i));
		}		
		return jcrExpression;
	}

}