package tests.medical.po;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class AdminPO {
    @FindBy(xpath = "//button[contains(@class, 'makeStyles-ApproveButton-131')]")
    public WebElement approveButton;


    @FindBy(xpath = "//span[@title='Approved']")
    public WebElement approveSpan;

    @FindBy(xpath = "//button[@title='Log out']")
    public WebElement logOut;

    public AdminPO(WebDriver driver) {
        PageFactory.initElements(driver, this);
    }

}
