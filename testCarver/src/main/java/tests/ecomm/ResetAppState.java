package tests.ecomm;

import com.apicarv.testCarver.utils.Settings.SUBJECT;
import com.apicarv.testCarver.utils.UtilsDocker;

public class ResetAppState {

	static final long WAIT_AFTER_RESTART = 30000; //30 seconds
	
    public static void reset(String file, boolean restart){
        UtilsDocker.restartDocker(SUBJECT.ecomm, restart, file);
        try {
			Thread.sleep(WAIT_AFTER_RESTART);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
/*
    private static void resetDB(String username, String password, String dbName, int port, String aSQLScriptFilePath){
        SqlConnection sqlConnection = new SqlConnection();
        sqlConnection.deleteAllTables(username, password, dbName, port);
        sqlConnection.resetUsingSqlScript(username, password, dbName, port, aSQLScriptFilePath);
    }*/
}
