/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Logger;

import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author suman
 */
public class Client_Logger 
{
    public static final Logger logger = Logger.getLogger("ClientLog");
    static FileHandler fh = null;
    public Client_Logger()
    {
        try 
        {
            //Setting the properties for the log
            String path = getLogPath();
            createLogFile(path);
            fh = new FileHandler(path,true);  
            
            fh = new FileHandler(path,true);  
            SimpleFormatter formatter = new SimpleFormatter();  
            fh.setFormatter(formatter); 
            logger.setUseParentHandlers(false);
            logger.addHandler(fh);
            
            logger.info("------------------------------------------------------");
        } 
        catch (Exception e) 
        {
            //e.printStackTrace();
        }
    }
    public static void logEntry(String level, String message)
    {
        if(level.equalsIgnoreCase("SEVERE"))
        {
            logger.severe(message);
        }
        else if (level.equalsIgnoreCase("WARNING"))
        {
            logger.warning(message);
        }
        else
        {
            logger.info(message);
        }
    }
    private static void createLogFile(String path)
    {
        try
        {
            File yourFile = new File(path);
            if(!yourFile.exists()) 
            {
                yourFile.createNewFile();
            }
        }
        catch(Exception e)
        {
            //e.printStackTrace();
        }
    }

    private static String getLogPath()
    {
        String path = System.getProperty("user.dir");
        int flag = 0;
        while (flag < 2)
        {
            if (null != path && path.length() > 0 )
            {
                int endIndex = path.lastIndexOf("\\");
                if (endIndex != -1)  
                {
                    path = path.substring(0, endIndex); // not forgot to put check if(endIndex != -1)
                }
            }
			flag++;
        }
        path = path + "/log/ClientLog.log";
        path = path.replace("\\", "/");
        return path;
    }
    
    public static void closeFileHandler()
    {
        fh.close();
    }
}
