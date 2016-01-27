/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rmi_server_cohort;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sumanaravikrishnan
 */

//To make the server sleep for 10 seconds every 3 minutes
public class Reminder
{
    Timer timer;
    public Reminder(int seconds) 
    {
        timer = new Timer();
        timer.schedule(new RemindTask(), seconds*1000);
        //System.out.println("Scheduled new timer");
    }

    class RemindTask extends TimerTask 
    {
        public void run() 
        {
            System.out.format("Server down for 10 seconds!");
            try 
            {
                Thread.sleep(10000);
            } 
            catch (InterruptedException ex) 
            {
                Logger.getLogger(Reminder.class.getName()).log(Level.SEVERE, null, ex);
                System.out.format("Server is up!");
            }
            timer.cancel();
            new Reminder(180);
        }
    }
}
