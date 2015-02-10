package ro.nextreports.server.web.core.validation;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.ValidationError;

public class AnalysisNameValidator extends JcrNameValidator {
		
	private static final long serialVersionUID = 1L;

	public AnalysisNameValidator() {
        super();
    }

    public AnalysisNameValidator(String errorMessage) {
        super(errorMessage);
    }
   
	@Override
	public void validate(IValidatable<String> validatable) {
		String value = validatable.getValue();
		if (value.contains(" ")) {			
			ValidationError error = new ValidationError();   
			String errorM = errorMessage;
			if (errorM == null) {
				errorM = "' ' not allowed in name";
			}
            error.setMessage(errorM);
            validatable.error(error);
		} else {
			super.validate(validatable);
		}
	}

}
