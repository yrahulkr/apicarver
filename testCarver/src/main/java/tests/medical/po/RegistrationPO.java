package tests.medical.po;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

// page_url = http://localhost:3000/login
public class RegistrationPO {
    @FindBy(xpath = "//div[contains(@class, 'MuiGrid-item')][.//label[normalize-space(text()) = 'First Name']]//input")
    public WebElement firstName;

    @FindBy(xpath = "//div[contains(@class, 'MuiGrid-item')][.//label[normalize-space(text()) = 'Last Name']]//input")
    public WebElement lastName;

    @FindBy(xpath = "//input[@value='0']")
    public WebElement age;


    @FindBy(xpath = "//input[@value='female']")
    public WebElement gender;

    @FindBy(xpath = "//div[contains(@class, 'MuiGrid-item')][.//label[normalize-space(text()) = 'Phone Number']]//input")
    public WebElement phone;

    @FindBy(xpath = "//div[contains(@class, 'MuiGrid-item')][.//label[normalize-space(text()) = 'Email Address']]//input")
    public WebElement email;

    @FindBy(xpath = "//div[contains(@class, 'MuiGrid-item')][.//label[normalize-space(text()) = 'Residential Address']]//input")
    public WebElement address;

    @FindBy(xpath = "//div[contains(@class, 'MuiGrid-item')][.//label[normalize-space(text()) = 'Language Spoken']]//input")
    public WebElement language;

//    @FindBy(xpath = "//*[@id='root']/div/div/div/div/div[9]/div/input")
//    public WebElement speciality;

    @FindBy(xpath = "//*[@id=\"root\"]/div/div/div/div/div[9]/div/div")
    public WebElement speciality;

    @FindBy(xpath = "//li[text()='Neurology']")
    public WebElement neurology;

    @FindBy(xpath = "//div[contains(@class, 'MuiGrid-item')][.//label[normalize-space(text()) = 'Certifications']]//input")
    public WebElement certifications;

    @FindBy(xpath = "//div[contains(@class, 'MuiGrid-item')][.//label[normalize-space(text()) = 'Education Details']]//textarea")
    public WebElement educationdetails;

    @FindBy(xpath = "//div[contains(@class, 'MuiGrid-item')][.//label[normalize-space(text()) = 'Awards']]//textarea")
    public WebElement awards;

    @FindBy(xpath = "//button[contains(@class, 'submit')]")
    public WebElement submitButton;

    @FindBy(xpath = "//div[contains(@class, 'MuiGrid-item')][.//label[normalize-space(text()) = 'Health Card Number']]//input")
    public WebElement healthCard;

    public RegistrationPO(WebDriver driver) {
        PageFactory.initElements(driver, this);
    }
}