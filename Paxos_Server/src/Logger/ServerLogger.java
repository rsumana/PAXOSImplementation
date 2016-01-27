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
public class ServerLogger 
{
    public static final Logger logger = Logger.getLogger("ServerLog");
    public ServerLogger()
    {
        try 
        {
            String path = getLogPath();
            createLogFile(path);
            //String classPath = System.getProperty("user.dir");
            FileHandler fh = new FileHandler(path,true);  
            fh.close();
            fh = new FileHandler(path,true);  
            SimpleFormatter formatter = new SimpleFormatter();  
            fh.setFormatter(formatter); 
            
            //Remove Console Handler and add File Handler
            logger.setUseParentHandlers(false);
            logger.addHandler(fh);
            
            //Denote the start of a session
            logger.info("------------------------------------------------------");
        } 
        catch (Exception e) 
        {
            logger.severe("Exception while creating log - "+e);
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
            //System cannot create the log file. Logs will be printed on console.
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
        path = path + "/log/ServerLog.log";
        path = path.replace("\\", "/");
        return path;
    }    
}
