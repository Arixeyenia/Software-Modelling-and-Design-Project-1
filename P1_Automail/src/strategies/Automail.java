package strategies;

import automail.*;

public class Automail {
	      
    public Robot[] robots;
    public IMailPool mailPool;
    
    public Automail(IMailPool mailPool, IMailDelivery delivery, int numRobots, boolean caution) {
    	// Swap between simple provided strategies and your strategies here
    	    	
    	/** Initialize the MailPool */
    	
    	this.mailPool = mailPool;
    	
    	/** Initialize robots */
    	robots = new Robot[numRobots];
    	if (!caution) {
    	    for (int i = 0; i < numRobots; i++) robots[i] = new Robot(delivery, mailPool);
        } else {
            for (int i = 0; i < numRobots; i++) robots[i] = new SpecialRobot(delivery, mailPool);
        }
    }


    
}
