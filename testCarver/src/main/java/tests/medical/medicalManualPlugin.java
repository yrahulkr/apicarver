package tests.medical;

import com.crawljax.core.CrawlerContext;
import com.crawljax.core.plugin.OnUrlFirstLoadPlugin;
import com.apicarv.testCarver.utils.UtilsSelenium;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tests.medical.po.*;

import java.time.Duration;

public class medicalManualPlugin implements OnUrlFirstLoadPlugin {
	private static final Logger LOGGER = LoggerFactory.getLogger(medicalManualPlugin.class.getName());

	static WebDriver driver;

	@Override
	public void onUrlFirstLoad(CrawlerContext context) {
		driver = context.getBrowser().getWebDriver();
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

		runManualTest(driver);
	}

	public static void runManualTest(WebDriver driver1) {
		if(driver == null){
			// If this function is called from outside crawljax
			driver = driver1;
		}
		try {
			testRegisterDoctor("doctor1", "doctor1", "doctor1", "lastName1", "30",
					"1234567890", "doctor1@mail.com", "somewhere on earth in doctor1 house", "english",
					"neurology certificates", "studied in some university1 in some country on earth",
					"Got some awards during college and then some after");
			testApproveDoctor();
			testRegisterPatient("patient", "patient", "patient", "lastName", "25",
					"1234567890", "patient1@mail.com", "somewhere on earth in patient1 house", "english", "1234567890");
			testCreateRequest("patient", "patient", 0, "I have a health issue for which I need your help");
			testTreatPatient("doctor1", "doctor1", "prescribed medicines", false, "Treatment advice for patient");
			testCheckPrescription("patient", "patient");
			testCheckDoctorHistory("doctor1", "doctor1");
			testPatientForumPost("patient", "patient", "patient1topic");
			testDoctorForumComment("doctor1", "doctor1", 0, "doctor1 comment on patient1 topic");

			testRegisterDoctor("doctor2", "doctor2", "doctor2", "lastName2", "31",
					"2234567890", "doctor2@mail.com", "somewhere on earth in doctor2 house", "english",
					"neurology2 certificates", "studied in some university2 in some country on earth",
					"Got more awards during college and then some after");
			testApproveDoctor();
			testRegisterPatient("patient2", "patient2", "patient2", "lastName2", "26",
					"2234567890", "patient2@mail.com", "somewhere on earth in patient2 house", "english", "2234567890");
			testCreateRequest("patient2", "patient2", 1, "I have a serious health issue and require help");
			testTreatPatient("doctor2", "doctor2", "prescribed medicines", true, "Treatment advice for patient");
			testCheckPrescription("patient2", "patient2");
			testCheckDoctorHistory("doctor2", "doctor2");
			testPatientForumPost("patient2", "patient2", "patient2topic");
			testDoctorForumComment("doctor2", "doctor2", 1, "doctor2 comment on patient2 topic");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	/*
	public void dummy()throws Exception{

		driver = DriverProvider.getInstance().getDriver();
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

		try {
			testRegisterDoctor("doctor1", "doctor1", "doctor1", "lastName1", "30",
					"1234567890", "doctor1@mail.com", "somewhere on earth in doctor1 house", "english",
					"neurology certificates", "studied in some university1 in some country on earth",
					"Got some awards during college and then some after");
			testApproveDoctor();
			testRegisterPatient("patient", "patient", "patient", "lastName", "25",
					"1234567890", "patient1@mail.com", "somewhere on earth in patient1 house", "english", "1234567890");
			testCreateRequest("patient", "patient", 0, "I have a health issue for which I need your help");
			testTreatPatient("doctor1", "doctor1", "prescribed medicines", false, "Treatment advice for patient");
			testCheckPrescription("patient", "patient");
			testCheckDoctorHistory("doctor1", "doctor1");
			testPatientForumPost("patient", "patient", "patient1topic");
			testDoctorForumComment("doctor1", "doctor1", 0, "doctor1 comment on patient1 topic");

			testRegisterDoctor("doctor2", "doctor2", "doctor2", "lastName2", "31",
					"2234567890", "doctor2@mail.com", "somewhere on earth in doctor2 house", "english",
					"neurology2 certificates", "studied in some university2 in some country on earth",
					"Got more awards during college and then some after");
			testApproveDoctor();
			testRegisterPatient("patient2", "patient2", "patient2", "lastName2", "26",
					"2234567890", "patient2@mail.com", "somewhere on earth in patient2 house", "english", "2234567890");
			testCreateRequest("patient2", "patient2", 1, "I have a serious health issue and require help");
			testTreatPatient("doctor2", "doctor2", "prescribed medicines", true, "Treatment advice for patient");
			testCheckPrescription("patient2", "patient2");
			testCheckDoctorHistory("doctor2", "doctor2");
			testPatientForumPost("patient2", "patient2", "patient2topic");
			testDoctorForumComment("doctor2", "doctor2", 1, "doctor2 comment on patient2 topic");

		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			driver.close();
		}
	}

	*/

	public static void testRegisterDoctor(String username, String password, String firstName, String lastName,
										  String age, String phone, String email, String address,
										  String language, String certifications, String education, String awards) throws Exception {
		driver.navigate().to(MedicalCrawlingRules.URL);
		HomePO homePo = new HomePO(driver);
		UtilsSelenium.click(driver,  homePo.registerTab);
		UtilsSelenium.click(driver,  homePo.doctorRole);
		homePo.usernameInput.sendKeys(username);
		homePo.passwordInput.sendKeys(password);
		homePo.confirmPasswordInput.sendKeys(password);
		UtilsSelenium.click(driver,  homePo.submitButton);
//        Scanner scanner = new Scanner(System.in);
//        scanner.nextLine();

		RegistrationPO regPo = new RegistrationPO(driver);
		regPo.firstName.sendKeys(firstName);
		regPo.lastName.sendKeys(lastName);
		regPo.age.sendKeys(age);
		UtilsSelenium.click(driver,  regPo.gender);
		regPo.phone.sendKeys(phone);
		regPo.email.sendKeys(email);
		regPo.address.sendKeys(address);
		regPo.language.sendKeys(language);
		UtilsSelenium.click(driver,  regPo.speciality);
		UtilsSelenium.click(driver,  regPo.neurology);
		regPo.certifications.sendKeys(certifications);
		regPo.educationdetails.sendKeys(education);
		regPo.awards.sendKeys(awards);
		UtilsSelenium.click(driver,  regPo.submitButton);

		Thread.sleep(5000);
	}

	public static void testApproveDoctor() throws Exception{

		driver.navigate().to(MedicalCrawlingRules.URL);
		HomePO homePo = new HomePO(driver);
		UtilsSelenium.click(driver,  homePo.adminRole);
		homePo.usernameInput.sendKeys("admin");
		homePo.passwordInput.sendKeys("password");
		UtilsSelenium.click(driver,  homePo.submitButton);

		AdminPO adminPO = new AdminPO(driver);

		UtilsSelenium.click(driver,  adminPO.approveSpan);
		UtilsSelenium.click(driver,  adminPO.logOut);

		Thread.sleep(5000);
	}


	public static void testRegisterPatient(String username, String password, String firstName, String lastName, String age, String phone, String email, String address,
										   String language, String healthCard) throws Exception{
		driver.navigate().to(MedicalCrawlingRules.URL);
		HomePO homePo = new HomePO(driver);
		UtilsSelenium.click(driver,  homePo.registerTab);
		UtilsSelenium.click(driver,  homePo.patientRole);
		homePo.usernameInput.sendKeys(username);
		homePo.passwordInput.sendKeys(password);
		homePo.confirmPasswordInput.sendKeys(password);
		UtilsSelenium.click(driver,  homePo.submitButton);
//        Scanner scanner = new Scanner(System.in);
//        scanner.nextLine();

		RegistrationPO regPo = new RegistrationPO(driver);
		regPo.firstName.sendKeys(firstName);
		regPo.lastName.sendKeys(lastName);
		regPo.age.sendKeys(age);
		UtilsSelenium.click(driver,  regPo.gender);
		regPo.phone.sendKeys(phone);
		regPo.email.sendKeys(email);
		regPo.address.sendKeys(address);
		regPo.language.sendKeys(language);
		regPo.healthCard.sendKeys(healthCard);
		UtilsSelenium.click(driver,  regPo.submitButton);
		Thread.sleep(2000);
		logoutPatient();
	}

	public static void loginPatient(String username, String password) throws Exception{
		driver.navigate().to(MedicalCrawlingRules.URL);
		HomePO homePo = new HomePO(driver);
		UtilsSelenium.click(driver,  homePo.patientRole);
		homePo.usernameInput.sendKeys(username);
		homePo.passwordInput.sendKeys(password);
		UtilsSelenium.click(driver,  homePo.submitButton);
	}


	public static void loginDoctor(String username, String password) throws Exception{
		driver.navigate().to(MedicalCrawlingRules.URL);
		HomePO homePo = new HomePO(driver);
		UtilsSelenium.click(driver,  homePo.doctorRole);
		homePo.usernameInput.sendKeys(username);
		homePo.passwordInput.sendKeys(password);
		UtilsSelenium.click(driver,  homePo.submitButton);
	}

	public static void testTreatPatient(String docUser, String docPassword, String prescription, boolean needAppointment, String advice) throws Exception{
		loginDoctor(docUser, docPassword);
		DoctorPO doctorPO = new DoctorPO(driver);
		UtilsSelenium.click(driver,  doctorPO.menu);
		UtilsSelenium.click(driver,  doctorPO.pendingRequests);
		UtilsSelenium.click(driver,  doctorPO.expandDoctor);
		doctorPO.medication.sendKeys(prescription);
		doctorPO.advice.sendKeys(advice);
		UtilsSelenium.click(driver,  doctorPO.submitRequest);
		Thread.sleep(1000);
		logoutDoctor();
	}
	public static void testCreateRequest(String patientUser, String patientPassword, int doctorIndex, String requestBody) throws Exception{
		loginPatient(patientUser, patientPassword);

		PatientPO patientPO = new PatientPO(driver);
		UtilsSelenium.click(driver,  patientPO.menu);
		UtilsSelenium.click(driver,  patientPO.createRequest);
		UtilsSelenium.click(driver,  patientPO.expandDoctor.get(doctorIndex));
//        elementToBeClickable(patientPO.submitRequest, 30);
//        Thread.sleep(100);
		patientPO.requestBody.get(doctorIndex).sendKeys(requestBody);
		UtilsSelenium.click(driver,  patientPO.submitRequest.get(doctorIndex));
		Thread.sleep(2000);
		logoutPatient();
	}

	public static void testCheckPrescription(String patientUser, String patientPass) throws Exception{
		loginPatient(patientUser, patientPass);
		PatientPO patientPO = new PatientPO(driver);
		UtilsSelenium.click(driver,  patientPO.menu);
		UtilsSelenium.click(driver,  patientPO.pastRequests);
		Thread.sleep(2000);
		logoutPatient();
	}

	public static void testCheckDoctorHistory(String docUser, String docPass) throws Exception{
		loginDoctor(docUser, docPass);
		DoctorPO doctorPO = new DoctorPO(driver);
		UtilsSelenium.click(driver,  doctorPO.menu);
		UtilsSelenium.click(driver,  doctorPO.history);
		Thread.sleep(2000);
		logoutDoctor();
	}




	public static void addCommentInForum(String comment, int postIndex) throws Exception{
		ForumPO forumPO = new ForumPO(driver);
		UtilsSelenium.click(driver,  forumPO.postedItem.get(postIndex));
		forumPO.writeComment.sendKeys("new comment");
		UtilsSelenium.click(driver,  forumPO.submitComment);
		Thread.sleep(2000);
	}
	public static void createForumPost(String inputTopic) throws Exception{
		ForumPO forumPO = new ForumPO(driver);
		UtilsSelenium.click(driver,  forumPO.newPost);
		forumPO.inputTopic.sendKeys(inputTopic);
		UtilsSelenium.click(driver,  forumPO.postButton);
		Thread.sleep(2000);
	}

	public static void goToPatientForum(String username, String password) throws Exception{
		loginPatient(username, password);
		PatientPO patientPO = new PatientPO(driver);
		UtilsSelenium.click(driver,  patientPO.menu);
		UtilsSelenium.click(driver,  patientPO.forum);
		Thread.sleep(2000);
	}

	public static void goToDoctorForum(String username, String password) throws Exception{
		loginDoctor(username, password);
		DoctorPO doctorPO = new DoctorPO(driver);
		UtilsSelenium.click(driver,  doctorPO.menu);
		UtilsSelenium.click(driver,  doctorPO.forum);
		Thread.sleep(2000);
	}

	public static void testPatientForumPost(String username, String password, String inputTopic) throws Exception {
		goToPatientForum(username, password);
		createForumPost(inputTopic);
		logoutPatient();
	}

	public static void testDoctorForumComment(String username, String password, int postIndex, String comment) throws Exception{
		goToDoctorForum(username, password);
		addCommentInForum(comment, postIndex);
		logoutDoctor();
	}


	public static void logoutPatient(){
		PatientPO patientPO = new PatientPO(driver);
		UtilsSelenium.click(driver,  patientPO.menu);
		UtilsSelenium.click(driver,  patientPO.logout);
	}

	public static void logoutDoctor(){
		DoctorPO doctorPO = new DoctorPO(driver);
		UtilsSelenium.click(driver,  doctorPO.menu);
		UtilsSelenium.click(driver,  doctorPO.logout);
	}

}

