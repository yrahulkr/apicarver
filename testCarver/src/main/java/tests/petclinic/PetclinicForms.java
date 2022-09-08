package tests.petclinic;

import com.crawljax.core.configuration.Form;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.Identification.How;
import com.crawljax.forms.FormInput;
import com.crawljax.forms.FormInput.InputType;

public class PetclinicForms {
	public static void newOwner(InputSpecification inputPetclinic) {
		Form newUserForm = new Form();

		FormInput firstName = newUserForm.inputField(InputType.TEXT, new Identification(How.id, "firstName"));
		firstName.inputValues("John");

		FormInput lastName = newUserForm.inputField(InputType.TEXT, new Identification(How.id, "lastName"));
		lastName.inputValues("Doe");

		FormInput telephone = newUserForm.inputField(InputType.TEXT, new Identification(How.id, "lastName"));
		telephone.inputValues("1234567890");
		
		inputPetclinic.setValuesInForm(newUserForm).beforeClickElement("button").withText("Add Owner");
		
	}
	
	public static void findOwner(InputSpecification inputPetclinic) {
		Form findOwnerForm = new Form();

		FormInput lastName = findOwnerForm.inputField(InputType.TEXT, new Identification(How.id, "lastName"));
		lastName.inputValues("", "Doe", "Davis");

		inputPetclinic.setValuesInForm(findOwnerForm).beforeClickElement("button").withText("Find Owner");
		
	}
	
	
}
