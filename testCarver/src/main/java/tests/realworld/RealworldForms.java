package tests.realworld;

import com.crawljax.core.configuration.Form;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.Identification.How;
import com.crawljax.forms.FormInput;
import com.crawljax.forms.FormInput.InputType;

public class RealworldForms {
	public static void signup(InputSpecification inputPetclinic) {
		Form newUserForm = new Form();


		
		FormInput username = newUserForm.inputField(InputType.TEXT, new Identification(How.xpath, "/html/body/div[1]/div/div/div/div/form/fieldset/fieldset[1]/input"));
		username.inputValues("admin");
		
		FormInput email = newUserForm.inputField(InputType.TEXT, new Identification(How.xpath, "/html/body/div[1]/div/div/div/div/form/fieldset/fieldset[2]/input"));
		email.inputValues("jdoe@mydomain.net");
		
		FormInput password = newUserForm.inputField(InputType.TEXT, new Identification(How.xpath, "/html/body/div[1]/div/div/div/div/form/fieldset/fieldset[3]/input"));
		password.inputValues("password");
		
		inputPetclinic.setValuesInForm(newUserForm).beforeClickElement("button").underXPath("/html/body/div[1]/div/div/div/div/form/fieldset/button");
		
	}
	
	
	
	public static void login(InputSpecification inputParabank) {
		Form loginForm = new Form();
		FormInput username = loginForm.inputField(InputType.TEXT, new Identification(How.xpath, "/html/body/div[1]/div/div/div/div/form/fieldset/fieldset[1]/input"));
		username.inputValues("jdoe@mydomain.net");
		
		FormInput password = loginForm.inputField(InputType.TEXT, new Identification(How.xpath, "/html/body/div[1]/div/div/div/div/form/fieldset/fieldset[2]/input"));
		password.inputValues("password");

		inputParabank.setValuesInForm(loginForm).beforeClickElement("button")
		.underXPath("/html/body/div[1]/div/div/div/div/form/fieldset/button");
		
	}
	
	

}
