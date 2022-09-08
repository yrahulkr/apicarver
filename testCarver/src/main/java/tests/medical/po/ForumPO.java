package tests.medical.po;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.List;

public class ForumPO {

    @FindBy(xpath = "//*[text() = 'New Post']")
    public WebElement newPost;

    @FindBy(id = "standard-basic")
    public WebElement inputTopic;

    @FindBy(xpath = "//*[text() = 'Post']")
    public WebElement postButton;

    @FindBy(xpath = "//div[@class='rce-container-citem']")
    public List<WebElement> postedItem;


    @FindBy(id = "standard-basic")
    public WebElement writeComment;


    @FindBy(xpath = "//*[text() = 'Submit']")
    public WebElement submitComment;

    public ForumPO(WebDriver driver) {
        PageFactory.initElements(driver, this);
    }
}