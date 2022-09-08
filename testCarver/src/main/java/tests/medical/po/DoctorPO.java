package tests.medical.po;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class DoctorPO {
    @FindBy(xpath = "//button[@aria-label='open drawer']")
    public WebElement menu;

    @FindBy(xpath = "//div[@class='MuiListItemText-root'][.//span[text()='Pending requests']]")
    public WebElement pendingRequests;

    @FindBy(xpath = "//div[@class='MuiListItemText-root'][.//span[text()='Request history']]")
    public WebElement history;

    @FindBy(xpath = "//div[@class='MuiListItemText-root'][.//span[text()='Forum']]")
    public WebElement forum;
    @FindBy(xpath = "//*[@id='panel1bh-header']/div[2]")
    public WebElement expandDoctor;

    @FindBy(xpath = "//*[@id=\"outlined-required\"]")
    public WebElement medication;

    @FindBy(xpath = "//*[@id=\"panel1bh-content\"]/div/p/div/div[2]/div/div/textarea")
    public WebElement advice;

    @FindBy(xpath = "//*[@id=\"panel1bh-content\"]/div/p/button")
    public WebElement submitRequest;

    @FindBy(xpath = "//div[@class='MuiListItemText-root'][.//span[text()='Logout']]")
    public WebElement logout;

    public DoctorPO(WebDriver driver) {
        PageFactory.initElements(driver, this);
    }
}
