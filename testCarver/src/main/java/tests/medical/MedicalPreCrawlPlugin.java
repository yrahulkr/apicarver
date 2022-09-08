package tests.medical;

import com.crawljax.core.CrawlerContext;
import com.crawljax.core.plugin.OnUrlLoadPlugin;

public class MedicalPreCrawlPlugin implements OnUrlLoadPlugin {
    String role;
    String username;
    String password;
    public MedicalPreCrawlPlugin(String role, String username, String password){
       this.role = role;
       this.username = username;
       this.password = password;
    }
    @Override
    public void onUrlLoad(CrawlerContext context) {

        medicalManualPlugin manualPlugin = new medicalManualPlugin();
        manualPlugin.driver = context.getBrowser().getWebDriver();
        if(role.equalsIgnoreCase("patient")){
            try {
                manualPlugin.loginPatient(username, password);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else{
            try {
                manualPlugin.loginDoctor(username, password);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
