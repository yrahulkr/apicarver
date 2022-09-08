package tests.booker;

import com.crawljax.core.configuration.Form;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.Identification.How;
import com.crawljax.forms.FormInput;
import com.crawljax.forms.FormInput.InputType;

public class BookerForms {
	public static void detailsPage(InputSpecification inputBooker) {
		Form newUserForm = new Form();

		FormInput name = newUserForm.inputField(InputType.TEXT, new Identification(How.id, "name"));
		name.inputValues("new Meadows");
		FormInput contactName = newUserForm.inputField(InputType.TEXT, new Identification(How.id, "contactName"));
		contactName.inputValues("new Meadows");
		FormInput contactAddress = newUserForm.inputField(InputType.TEXT, new Identification(How.id, "contactAddress"));
		contactAddress.inputValues("The Old Farmhouse, Shady Street, Newfordburyshire, NE1 410S");
		FormInput logo= newUserForm.inputField(InputType.TEXT, new Identification(How.id, "logoUrl"));
		logo.inputValues("https://www.mwtestconsultancy.co.uk/img/rbp-logo.png");
		FormInput desc= newUserForm.inputField(InputType.TEXTAREA, new Identification(How.id, "description"));
		desc.inputValues("Welcome to New Meadows, a delightful Bed & Breakfast nestled in the hills on Newingtonfordburyshire. A place so beautiful you will never want to leave. All our rooms have comfortable beds and we provide breakfast from the locally sourced supermarket. It is a delightful place.");
		FormInput latitude= newUserForm.inputField(InputType.TEXT, new Identification(How.id, "latitude"));
		latitude.inputValues("52.6351204");
		FormInput longitude= newUserForm.inputField(InputType.TEXT, new Identification(How.id, "longitude"));
		longitude.inputValues("1.2733774");

		FormInput telephone = newUserForm.inputField(InputType.TEXT, new Identification(How.id, "contactPhone"));
		telephone.inputValues("012345678901");
		FormInput email = newUserForm.inputField(InputType.TEXT, new Identification(How.id, "contactEmail"));
		telephone.inputValues("jdoe@mydomain.net");
		
		inputBooker.setValuesInForm(newUserForm).beforeClickElement("button")
		.underXPath("//*[@id=\"updateBranding\"]");
		
	}
	
	
	
	public static void login(InputSpecification inputBooker) {
		Form loginForm = new Form();
		FormInput username = loginForm.inputField(InputType.TEXT, new Identification(How.id, "username"));
		username.inputValues("admin");
		
		FormInput password = loginForm.inputField(InputType.TEXT, new Identification(How.id, "password"));
		password.inputValues("password");

		inputBooker.setValuesInForm(loginForm).beforeClickElement("button").underXPath("//*[@id=\"doLogin\"]");
	}
	
	}
