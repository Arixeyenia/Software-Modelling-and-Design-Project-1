package automail;

import exceptions.*;
import strategies.Automail;
import strategies.IMailPool;
import strategies.MailPool;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

/**
 * This class simulates the behaviour of AutoMail
 */
public class Simulation {

    /** Constant for the mail generator */
    private static int MAIL_TO_CREATE;
    private static int MAIL_MAX_WEIGHT;
    
    private static boolean CAUTION_ENABLED;
    private static boolean FRAGILE_ENABLED;
    private static boolean STATISTICS_ENABLED;
    
    private static ArrayList<MailItem> MAIL_DELIVERED;
    private static double total_score = 0;

    // Variables to be printed
	private static int normal_packs = 0;
	private static int caution_packs = 0;
	private static int normal_weight = 0;
	private static int caution_weight = 0;
	private static int wrapping_time = 0;

	public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
    	Properties automailProperties = new Properties();
		// Default properties
    	automailProperties.setProperty("Robots", "Standard");
    	automailProperties.setProperty("MailPool", "strategies.SimpleMailPool");
    	automailProperties.setProperty("Floors", "10");
    	automailProperties.setProperty("Mail_to_Create", "80");
    	automailProperties.setProperty("Last_Delivery_Time", "100");
    	automailProperties.setProperty("Caution", "true");
    	automailProperties.setProperty("Fragile", "false");
    	automailProperties.setProperty("Statistics", "false");

    	// Read properties
		FileReader inStream = null;
		try {
			inStream = new FileReader("automail.properties");
			automailProperties.load(inStream);
		} finally {
			 if (inStream != null) {
	                inStream.close();
	            }
		}

		//Seed
		String seedProp = automailProperties.getProperty("Seed");
		// Floors
		Building.FLOORS = Integer.parseInt(automailProperties.getProperty("Floors"));
        System.out.println("Floors: " + Building.FLOORS);
		// Mail_to_Create
		MAIL_TO_CREATE = Integer.parseInt(automailProperties.getProperty("Mail_to_Create"));
        System.out.println("Mail_to_Create: " + MAIL_TO_CREATE);
        // Mail_to_Create
     	MAIL_MAX_WEIGHT = Integer.parseInt(automailProperties.getProperty("Mail_Max_Weight"));
        System.out.println("Mail_Max_Weight: " + MAIL_MAX_WEIGHT);
		// Last_Delivery_Time
		Clock.LAST_DELIVERY_TIME = Integer.parseInt(automailProperties.getProperty("Last_Delivery_Time"));
        System.out.println("Last_Delivery_Time: " + Clock.LAST_DELIVERY_TIME);
        // Caution ability
        CAUTION_ENABLED = Boolean.parseBoolean(automailProperties.getProperty("Caution"));
        System.out.println("Caution enabled: " + CAUTION_ENABLED);
        // Fragile mail generation
        FRAGILE_ENABLED = Boolean.parseBoolean(automailProperties.getProperty("Fragile"));
        System.out.println("Fragile enabled: " + FRAGILE_ENABLED);
        // Statistics tracking
        STATISTICS_ENABLED = Boolean.parseBoolean(automailProperties.getProperty("Statistics"));
        System.out.println("Statistics enabled: " + STATISTICS_ENABLED);
		// Robots
		int robots = Integer.parseInt(automailProperties.getProperty("Robots"));
		System.out.print("Robots: "); System.out.println(robots);
		assert(robots > 0);
		// MailPool
		IMailPool mailPool = new MailPool(robots);

		// End properties
		
        MAIL_DELIVERED = new ArrayList<MailItem>();
                
        /** Used to see whether a seed is initialized or not */
        HashMap<Boolean, Integer> seedMap = new HashMap<>();
        
        /** Read the first argument and save it as a seed if it exists */
        if (args.length == 0 ) { // No arg
        	if (seedProp == null) { // and no property
        		seedMap.put(false, 0); // so randomise
        	} else { // Use property seed
        		seedMap.put(true, Integer.parseInt(seedProp));
        	}
        } else { // Use arg seed - overrides property
        	seedMap.put(true, Integer.parseInt(args[0]));
        }
        Integer seed = seedMap.get(true);
        System.out.println("Seed: " + (seed == null ? "null" : seed.toString()));
        Automail automail = new Automail(mailPool, new ReportDelivery(), robots, CAUTION_ENABLED);
        MailGenerator mailGenerator = new MailGenerator(MAIL_TO_CREATE, MAIL_MAX_WEIGHT, automail.mailPool, seedMap);
        
        /** Initiate all the mail */
        mailGenerator.generateAllMail(FRAGILE_ENABLED);
        while(MAIL_DELIVERED.size() != mailGenerator.MAIL_TO_CREATE) {
            mailGenerator.step();
            try {
                automail.mailPool.step();
				for (Robot robot: RobotManager.getInstance().getRobots()) {
					robot.step();
				}
			} catch (ExcessiveDeliveryException|ItemTooHeavyException|BreakingFragileItemException e) {
				e.printStackTrace();
				System.out.println("Simulation unable to complete.");
				System.exit(0);
			}
            Clock.Tick();
        }
        printResults();
    }
    
    static class ReportDelivery implements IMailDelivery {
    	
    	/** Confirm the delivery and calculate the total score */
    	public void deliver(MailItem deliveryItem){
    		if(!MAIL_DELIVERED.contains(deliveryItem)){
    			MAIL_DELIVERED.add(deliveryItem);
                System.out.printf("T: %3d > Deliv(%4d) [%s]%n", Clock.Time(), MAIL_DELIVERED.size(), deliveryItem.toString());
    			// Calculate delivery score
    			total_score += calculateDeliveryScore(deliveryItem);

    			if (STATISTICS_ENABLED) {
					if (!deliveryItem.getFragile()) {
						// Increment the number of normal packages delivered
						normal_packs++;

						// Increment the weight of normal packages delivered
						normal_weight += deliveryItem.getWeight();
					}
					else if (deliveryItem.getFragile()){
						// Increment the number of caution packages delivered
						caution_packs++;

						// Increment the weight of normal packages delivered
						caution_weight += deliveryItem.getWeight();

						wrapping_time += MailItem.TOTAL_WRAPPING_TIME;
					}
				}
    		}
    		else{
    			try {
    				throw new MailAlreadyDeliveredException();
    			} catch (MailAlreadyDeliveredException e) {
    				e.printStackTrace();
    			}
    		}


    	}

    }
    
    private static double calculateDeliveryScore(MailItem deliveryItem) {
    	// Penalty for longer delivery times
    	final double penalty = 1.2;
    	double priority_weight = 0;
        return Math.pow(Clock.Time() - deliveryItem.getArrivalTime(),penalty)*(1+Math.sqrt(priority_weight));
    }

    private static void printResults(){
        System.out.println("T: "+Clock.Time()+" | Simulation complete!");
        System.out.println("Final Delivery time: "+Clock.Time());
        System.out.printf("Final Score: %.2f%n", total_score);

        if (STATISTICS_ENABLED) {
			// Stats required to print
			System.out.printf("Number of packages delivered normally: %d%n", normal_packs);
			System.out.printf("Number of packages delivered using caution: %d%n", caution_packs);
			System.out.printf("Total weight of packages delivered normally: %d%n", normal_weight);
			System.out.printf("Total weight of packages delivered using caution: %d%n", caution_weight);
			System.out.printf("Total time spent wrapping and unwrapping: %d%n", wrapping_time);
		}
    }
}
