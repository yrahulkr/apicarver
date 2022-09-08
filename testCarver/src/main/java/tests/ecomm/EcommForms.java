package tests.ecomm;

import com.crawljax.core.configuration.Form;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.Identification.How;
import com.crawljax.forms.FormInput;
import com.crawljax.forms.FormInput.InputType;

public class EcommForms {
	
	
	public static void login(InputSpecification inputEcomm) {
		Form loginForm = new Form();
		FormInput username = loginForm.inputField(InputType.TEXT, new Identification(How.xpath, "//*[@id=\"signin-div\"]/form/div[1]/input"));
		username.inputValues("admin@gmail.com");
		
		FormInput password = loginForm.inputField(InputType.TEXT, new Identification(How.xpath, "//*[@id=\"signin-div\"]/form/div[2]/input"));
		password.inputValues("admin");

		inputEcomm.setValuesInForm(loginForm).beforeClickElement("button").underXPath("//*[@id=\"signin-div\"]/form/button");
	}
	
}
