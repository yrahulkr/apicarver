package tests.medical;

import com.apicarv.testCarver.utils.DriverProvider;
import org.junit.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import tests.medical.po.*;

import java.time.Duration;

public class MedicalTests {
    static WebDriver driver;
    @BeforeClass
    public static void Setup(){
        driver = DriverProvider.getInstance().getDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
    }

    @Test
    public void registerDoctor() throws Exception {
        driver.navigate().to(MedicalCrawlingRules.URL);
        HomePO homePo = new HomePO(driver);
        homePo.registerTab.click();
        homePo.doctorRole.click();
        homePo.usernameInput.sendKeys("doctor1");
        homePo.passwordInput.sendKeys("doctor1");
        homePo.confirmPasswordInput.sendKeys("doctor1");
        homePo.submitButton.click();
//        Scanner scanner = new Scanner(System.in);
//        scanner.nextLine();

        RegistrationPO regPo = new RegistrationPO(driver);
        regPo.firstName.sendKeys("doctor1");
        regPo.lastName.sendKeys("lastName");
        regPo.age.sendKeys("25");
        regPo.gender.click();
        regPo.phone.sendKeys("9876543210");
        regPo.email.sendKeys("dummy@email.com");
        regPo.address.sendKeys("somewhere on earth");
        regPo.language.sendKeys("english");
        regPo.speciality.click();
        regPo.neurology.click();
        regPo.certifications.sendKeys("neurology certificates");
        regPo.educationdetails.sendKeys("studied in some university in some country on earth");
        regPo.awards.sendKeys("Got some awards during college and then some after");
        regPo.submitButton.click();

        Thread.sleep(5000);
    }

    @Test
    public void approveDoctor() throws Exception{
        driver.navigate().to(MedicalCrawlingRules.URL);
        HomePO homePo = new HomePO(driver);
        homePo.adminRole.click();
        homePo.usernameInput.sendKeys("admin");
        homePo.passwordInput.sendKeys("password");
        homePo.submitButton.click();

        AdminPO adminPO = new AdminPO(driver);

        adminPO.approveSpan.click();
        adminPO.logOut.click();

        Thread.sleep(5000);
    }


    @Test
    public void registerPatient() throws Exception{
        driver.navigate().to(MedicalCrawlingRules.URL);
        HomePO homePo = new HomePO(driver);
        homePo.registerTab.click();
        homePo.patientRole.click();
        homePo.usernameInput.sendKeys("patient1");
        homePo.passwordInput.sendKeys("patient1");
        homePo.confirmPasswordInput.sendKeys("patient1");
        homePo.submitButton.click();
//        Scanner scanner = new Scanner(System.in);
//        scanner.nextLine();

        RegistrationPO regPo = new RegistrationPO(driver);
        regPo.firstName.sendKeys("patient1");
        regPo.lastName.sendKeys("lastName");
        regPo.age.sendKeys("25");
        regPo.gender.click();
        regPo.phone.sendKeys("9876543210");
        regPo.email.sendKeys("dummy1@email.com");
        regPo.address.sendKeys("somewhere on earth");
        regPo.language.sendKeys("english");
        regPo.healthCard.sendKeys("1234567890");
        regPo.submitButton.click();

        Thread.sleep(5000);
    }


    public void loginPatient() throws Exception{
        driver.navigate().to(MedicalCrawlingRules.URL);
        HomePO homePo = new HomePO(driver);
        homePo.patientRole.click();
        homePo.usernameInput.sendKeys("patient");
        homePo.passwordInput.sendKeys("patient");
        homePo.submitButton.click();
    }

    public void loginDoctor() throws Exception{
        driver.navigate().to(MedicalCrawlingRules.URL);
        HomePO homePo = new HomePO(driver);
        homePo.doctorRole.click();
        homePo.usernameInput.sendKeys("doctor");
        homePo.passwordInput.sendKeys("doctor");
        homePo.submitButton.click();
    }



    @Test
    public void createRequest() throws Exception{
        loginPatient();

        PatientPO patientPO = new PatientPO(driver);
        patientPO.menu.click();
        patientPO.createRequest.click();
        patientPO.expandDoctor.get(1).click();
//        elementToBeClickable(patientPO.submitRequest, 30);
//        Thread.sleep(100);
        patientPO.requestBody.get(1).sendKeys("I have a health issue for which I need your help");
        patientPO.submitRequest.get(1).click();
        Thread.sleep(5000);
    }

    @Test
    public void treatPatient() throws Exception{
        loginDoctor();
        DoctorPO doctorPO = new DoctorPO(driver);
        doctorPO.menu.click();
        doctorPO.pendingRequests.click();
        doctorPO.expandDoctor.click();
        doctorPO.medication.sendKeys("prescribed medicines");
        doctorPO.advice.sendKeys("Treatment advice for patient");
        doctorPO.submitRequest.click();

    }

    @Test
    public void checkPrescription() throws Exception{
        loginPatient();
        PatientPO patientPO = new PatientPO(driver);
        patientPO.menu.click();
        patientPO.pastRequests.click();
        Thread.sleep(5000);
    }

    @Test
    public void checkDoctorHistory() throws Exception{
        loginDoctor();
        DoctorPO doctorPO = new DoctorPO(driver);
        doctorPO.menu.click();
        doctorPO.history.click();
        Thread.sleep(5000);
    }

    private Wait<WebDriver> getWebDriverFluentWait(int timeout) {
        return new FluentWait<WebDriver>(driver)
                .withTimeout(Duration.ofSeconds(timeout))
                .pollingEvery(Duration.ofSeconds(1))
                .ignoring(NoSuchElementException.class);
    }

    public WebElement elementToBeClickable(WebElement webElement, int timeout) {
        try {
            return getWebDriverFluentWait(timeout)
                    .until(ExpectedConditions.elementToBeClickable(webElement));
        } catch (Exception e) {
            return null;
        }
    }

    @Test
    public void postInForum() throws Exception{
        loginPatient();
        PatientPO patientPO = new PatientPO(driver);
        patientPO.menu.click();
        patientPO.forum.click();
        ForumPO forumPO = new ForumPO(driver);
        forumPO.newPost.click();
        forumPO.inputTopic.sendKeys("New topic by patient " + "patient");
        forumPO.postButton.click();
        Thread.sleep(2000);
        forumPO.postedItem.get(0).click();
        forumPO.writeComment.sendKeys("new comment");
        forumPO.submitComment.click();
        Thread.sleep(2000);
    }




    @AfterClass
    public static void cleanup(){
        driver.close();
    }
}

