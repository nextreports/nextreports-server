package ro.nextreports.server.web.common.util;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

public class ZeroRangeValidator implements IValidator<Integer> {
	 
	private int min;
	private int max;
 
	public ZeroRangeValidator(int min, int max) {
		this.min = min;
		this.max = max;
	}
 
	@Override
	public void validate(IValidatable<Integer> validatable) {
 
		final Integer number = validatable.getValue();
 
		if (!number.equals(0) && ((number < min) || (number > max)) ) {
			error(validatable, "ZeroRangeValidator");
		}
 
	}
 
	private void error(IValidatable<Integer> validatable, String errorKey) {
		ValidationError error = new ValidationError();
		error.addKey(errorKey);
		error.setVariable("minimum", min);
		error.setVariable("maximum", max);		
		validatable.error(error);
	}
 
}
