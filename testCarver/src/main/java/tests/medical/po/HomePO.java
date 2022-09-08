package tests.medical.po;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import java.util.List;

// page_url = http://localhost:3000/login
public class HomePO {
    @FindBy(xpath = "//div[@role='radiogroup']")
    public WebElement muiformgroupRootDiv;

    @FindBy(name = "username")
    public WebElement usernameInput;

    @FindBy(name = "password")
    public WebElement passwordInput;

    @FindBy(xpath = "//span[normalize-space(text()) = 'LOG IN']")
    public WebElement muibuttonLabelSpan;
    

    @FindBy(xpath = "//*[text() = 'REGISTER']")
    public WebElement registerTab;

    @FindBy(xpath = "//*[text() = 'LOGIN']")
    public WebElement loginTab;

    @FindBy(xpath = "//div[@role='radiogroup']")
    public List<WebElement> muiformgroupRootDiv2;

    @FindBy(name = "role")
    public WebElement roleInput;

    @FindBy(xpath = "//input[@value='Doctor']")
    public WebElement doctorRole;


    @FindBy(xpath = "//input[@value='Patient']")
    public WebElement patientRole;

    @FindBy(xpath = "//input[@value='Admin']")
    public WebElement adminRole;

    @FindBy(name = "confirm password")
    public WebElement confirmPasswordInput;

    @FindBy(xpath = "//button[contains(@class, 'submit')]")
    public WebElement submitButton;


    
    
    

    // No page elements added

    public HomePO(WebDriver driver) {
        PageFactory.initElements(driver, this);
    }
}