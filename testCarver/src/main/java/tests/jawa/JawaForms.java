package tests.jawa;

import com.crawljax.core.configuration.Form;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.Identification.How;
import com.crawljax.forms.FormInput;
import com.crawljax.forms.FormInput.InputType;

public class JawaForms {
	public static void newUser(InputSpecification inputPetclinic) {
		Form newUserForm = new Form();

		FormInput firstName = newUserForm.inputField(InputType.TEXT, new Identification(How.name, "firstname"));
		firstName.inputValues("John1", "John2", "John3", "John4", "John5", "John6");

		FormInput lastName = newUserForm.inputField(InputType.TEXT, new Identification(How.name, "lastname"));
		lastName.inputValues("Doe", "doe2", "Doe3", "Doe4", "Doe5");
		
		FormInput address = newUserForm.inputField(InputType.EMAIL, new Identification(How.name, "email"));
		address.inputValues("email@email.com", "email2@email.com", "email1@email.com", "Email3@email.com");
				
		FormInput username = newUserForm.inputField(InputType.INPUT, new Identification(How.name, "username"));
		username.inputValues("jdoe", "lmse", "asdf", "qwerty", "WLEKW");
		
		FormInput id = newUserForm.inputField(InputType.INPUT, new Identification(How.name, "id"));
		id.inputValues("ID0", "ID1", "ID2", "ID4", "ID5", "ID6");
		
		
		inputPetclinic.setValuesInForm(newUserForm).beforeClickElement("input").withText("CREATE USER");
//		.underXPath("/html/body/app-root/app-admin-layout/div/div[2]/app-user-profile/div/div/div/div[1]/div/div[2]/form/button");
		
	}
	
	
	public static void search(InputSpecification inputParabank) {
		Form searchForm = new Form();
		FormInput username = searchForm.inputField(InputType.TEXT, new Identification(How.xpath, "//*[@id=\"navigation\"]/form/div/input"));
		username.inputValues("john", "doe", "jdoe", "hello");
		

		inputParabank.setValuesInForm(searchForm).beforeClickElement("button")
		.underXPath("//*[@id=\"navigation\"]/form/div/button");
		
	}
	
	
	public static void findByDate(InputSpecification inputParabank) {
		Form findTransactionForm = new Form();
		FormInput lastName = findTransactionForm.inputField(InputType.TEXT, new Identification(How.id, "criteria.onDate"));
		lastName.inputValues("04-25-2022", "04-26-2022");

		inputParabank.setValuesInForm(findTransactionForm).beforeClickElement("button")
		.underXPath("//*[@id=\"rightPanel\"]/div/div/form/div[5]/button");
	}
	public static void findBetweenDates(InputSpecification inputParabank) {
		Form findTransactionForm = new Form();
		FormInput start = findTransactionForm.inputField(InputType.TEXT, new Identification(How.id, "criteria.fromDate"));
		start.inputValues("04-20-2022", "04-22-2022");
		FormInput end = findTransactionForm.inputField(InputType.TEXT, new Identification(How.id, "criteria.toDate"));
		end.inputValues("05-31-2022", "05-30-2022");

		inputParabank.setValuesInForm(findTransactionForm).beforeClickElement("button")
		.underXPath("//*[@id=\"rightPanel\"]/div/div/form/div[7]/button");
	}
	
	public static void findByAmount(InputSpecification inputParabank) {
		Form findTransactionForm = new Form();
		FormInput lastName = findTransactionForm.inputField(InputType.TEXT, new Identification(How.id, "criteria.amount"));
		lastName.inputValues("20");

		inputParabank.setValuesInForm(findTransactionForm).beforeClickElement("button")
		.underXPath("//*[@id=\"rightPanel\"]/div/div/form/div[9]/button");

	}
}
