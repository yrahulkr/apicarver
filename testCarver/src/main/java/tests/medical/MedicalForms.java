package tests.medical;

import com.crawljax.core.configuration.Form;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.Identification.How;
import com.crawljax.forms.FormInput;
import com.crawljax.forms.FormInput.InputType;
import com.crawljax.forms.InputValue;

import java.util.HashSet;
import java.util.Set;

public class MedicalForms {
	public static void login(InputSpecification inputBooker, String radio, String user, String pass) {
		Form loginForm = new Form();

		FormInput role = loginForm.inputField(InputType.RADIO, new Identification(How.xpath, radio));
		Set<InputValue> inputValueSet = new HashSet<>();
		inputValueSet.add(new InputValue("1", true));
		role.inputValues(inputValueSet);

		FormInput username = loginForm.inputField(InputType.TEXT, new Identification(How.name, "username"));
		username.inputValues(user);
		
		FormInput password = loginForm.inputField(InputType.TEXT, new Identification(How.name, "password"));
		password.inputValues(pass);

		inputBooker.setValuesInForm(loginForm).beforeClickElement("BUTTON").underXPath("/HTML[1]/BODY[1]/DIV[1]/DIV[1]/DIV[1]/DIV[1]/DIV[1]/DIV[1]/DIV[1]/BUTTON[1]");
	}
	
}
