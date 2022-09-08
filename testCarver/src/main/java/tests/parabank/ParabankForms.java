package tests.parabank;

import com.crawljax.core.configuration.Form;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.Identification.How;
import com.crawljax.forms.FormInput;
import com.crawljax.forms.FormInput.InputType;

public class ParabankForms {
	public static void newOwner(InputSpecification inputPetclinic) {
		Form newUserForm = new Form();

		FormInput firstName = newUserForm.inputField(InputType.TEXT, new Identification(How.id, "customer.firstName"));
		firstName.inputValues("John");

		FormInput lastName = newUserForm.inputField(InputType.TEXT, new Identification(How.id, "customer.lastName"));
		lastName.inputValues("Doe");
		
		FormInput address = newUserForm.inputField(InputType.TEXT, new Identification(How.id, "customer.address.street"));
		address.inputValues("Somewhere on Earth");
		
		FormInput city = newUserForm.inputField(InputType.TEXT, new Identification(How.id, "customer.address.city"));
		city.inputValues("Somecity");
		
		FormInput state = newUserForm.inputField(InputType.TEXT, new Identification(How.id, "customer.address.state"));
		state.inputValues("StateLess");
		
		FormInput zipcode = newUserForm.inputField(InputType.TEXT, new Identification(How.id, "customer.address.zipcode"));
		zipcode.inputValues("123456");
		
		FormInput telephone = newUserForm.inputField(InputType.TEXT, new Identification(How.id, "lastName"));
		telephone.inputValues("1234567890");
		
		FormInput ssn = newUserForm.inputField(InputType.TEXT, new Identification(How.id, "customer.address.street"));
		ssn.inputValues("12312312");
		
		FormInput username = newUserForm.inputField(InputType.TEXT, new Identification(How.id, "customer.username"));
		username.inputValues("admin");
		
		FormInput password = newUserForm.inputField(InputType.TEXT, new Identification(How.id, "customer.password"));
		password.inputValues("password");
		
		FormInput confirm = newUserForm.inputField(InputType.TEXT, new Identification(How.id, "repeatedPassword"));
		confirm.inputValues("password");
		
		inputPetclinic.setValuesInForm(newUserForm).beforeClickElement("input").underXPath("//*[@id=\"customerForm\"]/table/tbody/tr[13]/td[2]/input");
		
	}
	
	public static void billPay(InputSpecification inputParabank) {
		Form billPayform = new Form();

		FormInput firstName = billPayform.inputField(InputType.TEXT, new Identification(How.name, "payee.name"));
		firstName.inputValues("Bill Payee");
	
		FormInput address = billPayform.inputField(InputType.TEXT, new Identification(How.name, "payee.address.street"));
		address.inputValues("Payee Address");
		
		FormInput city = billPayform.inputField(InputType.TEXT, new Identification(How.name, "payee.address.city"));
		city.inputValues("Somecity");
		
		FormInput state = billPayform.inputField(InputType.TEXT, new Identification(How.name, "payee.address.state"));
		state.inputValues("StateLess");
		
		FormInput zipcode = billPayform.inputField(InputType.TEXT, new Identification(How.name, "payee.address.zipCode"));
		zipcode.inputValues("123456");
		
		FormInput telephone = billPayform.inputField(InputType.TEXT, new Identification(How.name, "payee.phoneNumber"));
		telephone.inputValues("1234567890");
		
		
		FormInput account = billPayform.inputField(InputType.TEXT, new Identification(How.name, "payee.accountNumber"));
		account.inputValues("12345");
		
		FormInput accountConfirm = billPayform.inputField(InputType.TEXT, new Identification(How.name, "verifyAccount"));
		accountConfirm.inputValues("12345");
		
		FormInput amount = billPayform.inputField(InputType.TEXT, new Identification(How.name, "amount"));
		amount.inputValues("20");
		
		inputParabank.setValuesInForm(billPayform).beforeClickElement("input").underXPath("//*[@id=\"rightPanel\"]/div/div[1]/form/table/tbody/tr[14]/td[2]/input");
		
	}
	
	public static void login(InputSpecification inputParabank) {
		Form loginForm = new Form();
		FormInput username = loginForm.inputField(InputType.TEXT, new Identification(How.name, "username"));
		username.inputValues("admin");
		
		FormInput password = loginForm.inputField(InputType.TEXT, new Identification(How.name, "password"));
		password.inputValues("password");

		inputParabank.setValuesInForm(loginForm).beforeClickElement("input")
		.underXPath("//*[@id=\"loginPanel\"]/form/div[3]/input");
		
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
