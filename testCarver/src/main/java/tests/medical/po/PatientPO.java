package tests.medical.po;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.List;

public class PatientPO {
    @FindBy(xpath = "//button[@aria-label='open drawer']")
    public WebElement menu;

    @FindBy(xpath = "//div[@class='MuiListItemText-root'][.//span[text()='Create request']]")
    public WebElement createRequest;

    @FindBy(xpath = "//div[@class='MuiListItemText-root'][.//span[text()='Past requests']]")
    public WebElement pastRequests;

    @FindBy(xpath = "//div[@class='MuiListItemText-root'][.//span[text()='Forum']]")
    public WebElement forum;

    @FindBy(xpath = "//div[@class='MuiListItemText-root'][.//span[text()='Logout']]")
    public WebElement logout;

    @FindBy(xpath = "//*[@id='panel1bh-header']/div[2]")
    public List<WebElement> expandDoctor;

    @FindBy(xpath = "//*[@id=\"outlined-multiline-static\"]")
    public List<WebElement> requestBody;

    @FindBy(xpath = "//*[@id=\"panel1bh-content\"]/div/p/div[2]/button")
    public List<WebElement> submitRequest;

    public PatientPO(WebDriver driver) {
        PageFactory.initElements(driver, this);
    }
}
