package automail;

import exceptions.BreakingFragileItemException;
import exceptions.ExcessiveDeliveryException;
import exceptions.ItemTooHeavyException;
import strategies.IMailPool;
import java.util.Map;
import java.util.TreeMap;

/**
 * The robot delivers mail!
 */
public class Robot {
	
    static public final int INDIVIDUAL_MAX_WEIGHT = 2000;

    IMailDelivery delivery;
    protected final String id;
    /** Possible states the robot can be in */
    public enum RobotState { DELIVERING, WAITING, RETURNING }
    public RobotState current_state;
    private int current_floor;
    private int destination_floor;
    private IMailPool mailPool;
    private boolean receivedDispatch;
    
    private MailItem deliveryItem = null;
    private MailItem tube = null;
    
    private int deliveryCounter;
    

    /**
     * Initiates the robot's location at the start to be at the mailroom
     * also set it to be waiting for mail.
     * @param delivery governs the final delivery
     * @param mailPool is the source of mail items
     */
    // PRIOROTISE NON FRAGILE ITEMS SO THEY DONT HAVE TO WAIT FOR THE FRAGILE ITEM TO BE DELIVERED
    public Robot(IMailDelivery delivery, IMailPool mailPool){
    	id = "R" + hashCode();

    	current_state = RobotState.RETURNING;
        current_floor = Building.MAILROOM_LOCATION;
        this.delivery = delivery;
        this.mailPool = mailPool;
        this.receivedDispatch = false;
        this.deliveryCounter = 0;
    }
    
    public void dispatch() {
    	receivedDispatch = true;
    }

    public int getCurrent_floor() {
        return current_floor;
    }

    public void setCurrent_floor(int current_floor) { this.current_floor = current_floor; }

    public MailItem getDeliveryItem(){ return this.deliveryItem; }

    public void setDeliveryItem(MailItem deliveryItem) { this.deliveryItem = deliveryItem; }

    public int getDeliveryCounter(){ return this.deliveryCounter; }

    public void incrementDeliveryCounter(){ this.deliveryCounter++; }

    public int getDestination_floor() { return this.destination_floor; }

    public void setDestination_floor(int destination_floor) { this.destination_floor = destination_floor; }

    public MailItem getTube() { return tube; }

    public boolean isEmpty() { return (deliveryItem == null && tube == null); }

    public boolean handsFull() { return deliveryItem != null; }

    //Check if the mail item is in hands
    public boolean itemIsInHands(String id) {
        if (deliveryItem.getId().equals(id)){
            return true;
        }
        else {
            return false;
        }
    }

    public boolean itemIsInTube(String id) {
        if (tube.getId().equals(id)){
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * This is called on every time step
     * @throws ExcessiveDeliveryException if robot delivers more than the capacity of the tube without refilling
     */
    public void step() throws ExcessiveDeliveryException {    	
    	switch(current_state) {
    		/** This state is triggered when the robot is returning to the mailroom after a delivery */
    		case RETURNING:
    			/** If its current position is at the mailroom, then the robot should change state */
                if(current_floor == Building.MAILROOM_LOCATION){
                    if (tube != null) {
                        mailPool.addToPool(tube);
                        System.out.printf("T: %3d >  +addToPool [%s]%n", Clock.Time(), tube.toString());
                        tube = null;
                    }
        			/** Tell the sorter the robot is ready */
        			mailPool.registerWaiting(this);
                	changeState(RobotState.WAITING);
                } else {
                	/** If the robot is not at the mailroom floor yet, then move towards it! */
                    moveTowards(Building.MAILROOM_LOCATION);
                	break;
                }
    		case WAITING:
                /** If the StorageTube is ready and the Robot is waiting in the mailroom then start the delivery */
                if(!isEmpty() && receivedDispatch){
                    receivedDispatch = false;
                    deliveryCounter = 0; // reset delivery counter
                    handlePreDelivery();
                }
                break;
    		case DELIVERING:
    			if(current_floor == destination_floor){ // If already here drop off either way
                    deliverMail();
                    /** Check if want to return, i.e. if there is no item in the tube*/
                    if(isEmpty()){
                        changeState(RobotState.RETURNING);
                    }
                    else{
                        /** If there is another item, set the robot's route to the location to deliver the item */
                        deliveryItem = tube;
                        tube = null;
                        setRoute();
                        changeState(RobotState.DELIVERING);
                    }
    			} else {
	        		/** The robot is not at the destination yet, move towards it! */
	                moveTowards(destination_floor);
    			}
                break;
    	}
    }

    /**
     * Deliver items
     * Handles everything related to delivery
     *
     * @throws ExcessiveDeliveryException
     */
    protected void deliverMail() throws ExcessiveDeliveryException {
        /** Delivery complete, report this to the simulator! */
        delivery.deliver(deliveryItem);
        deliveryItem = null;
        deliveryCounter++;
        if(deliveryCounter > 2){  // Implies a simulation bug
            throw new ExcessiveDeliveryException();
        }
    }

    /**
     * Planning for delivery
     * Handles things required before a delivery
     */
    protected void handlePreDelivery(){
        setRoute();
        changeState(RobotState.DELIVERING);
    }

    /**
     * Sets the route for the robot
     */
    protected void setRoute() {
        /** Set the destination floor */
        destination_floor = deliveryItem.getDestFloor();
    }

    /**
     * Generic function that moves the robot towards the destination
     * @param destination the floor towards which the robot is moving
     */
    protected void moveTowards(int destination) {
        if(current_floor < destination){
            current_floor++;
        } else {
            current_floor--;
        }
    }
    
    protected String getIdTube() {
    	return String.format("%s(%1d)", id, (tube == null ? 0 : 1));
    }
    
    /**
     * Prints out the change in state
     * @param nextState the state to which the robot is transitioning
     */
    private void changeState(RobotState nextState){
    	assert(!(deliveryItem == null && tube != null));
    	if (current_state != nextState) {
            System.out.printf("T: %3d > %7s changed from %s to %s%n", Clock.Time(), getIdTube(), current_state, nextState);
    	}
    	current_state = nextState;
    	if(nextState == RobotState.DELIVERING){
            printDelivery();
    	}
    }

    /**
     * String to print when robot is delivering
     */
    public void printDelivery() {
        if (this.getDeliveryItem() != null){
            System.out.printf("T: %3d > %9s-> [%s]%n", Clock.Time(), getIdTube(), deliveryItem.toString());
        }
    }
    
	static private int count = 0;
	static private Map<Integer, Integer> hashMap = new TreeMap<Integer, Integer>();

	@Override
	public int hashCode() {
		Integer hash0 = super.hashCode();
		Integer hash = hashMap.get(hash0);
		if (hash == null) { hash = count++; hashMap.put(hash0, hash); }
		return hash;
	}

	public void addToHand(MailItem mailItem) throws ItemTooHeavyException, BreakingFragileItemException {
		assert(deliveryItem == null);
		if(mailItem.getFragile()) throw new BreakingFragileItemException();
		deliveryItem = mailItem;
		if (deliveryItem.getWeight() > INDIVIDUAL_MAX_WEIGHT) throw new ItemTooHeavyException();
	}

	public void addToTube(MailItem mailItem) throws ItemTooHeavyException, BreakingFragileItemException {
		assert(tube == null);
		if(mailItem.getFragile()) throw new BreakingFragileItemException();
		tube = mailItem;
		if (tube.getWeight() > INDIVIDUAL_MAX_WEIGHT) throw new ItemTooHeavyException();
        }
}
