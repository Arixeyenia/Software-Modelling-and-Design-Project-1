package automail;

import exceptions.ExcessiveDeliveryException;
import exceptions.ItemTooHeavyException;
import strategies.IMailPool;

import java.util.LinkedList;

public class SpecialRobot extends Robot implements ISpecialRobot{

    // Special arm for fragile items
    private MailItem specialItem = null;


    /**
     * Initiates the robot's location at the start to be at the mailroom
     * also set it to be waiting for mail.
     *
     * @param delivery  governs the final delivery
     * @param mailPool  is the source of mail items
     */
    public SpecialRobot(IMailDelivery delivery, IMailPool mailPool) {
        super(delivery, mailPool);
    }

    @Override
    public int getCurrent_floor() {
        int current_floor = super.getCurrent_floor();
        return current_floor;
    }

    @Override
    public MailItem getDeliveryItem(){
        MailItem deliveryItem = super.getDeliveryItem();
        return deliveryItem;
    }

    @Override
    public int getDeliveryCounter(){
        int deliveryCounter = super.getDeliveryCounter();
        return deliveryCounter;
    }

    @Override
    public boolean isEmpty() { return (this.getDeliveryItem() == null && this.getTube() == null && specialItem == null); }

    @Override
    public boolean handsFull() { return this.getDeliveryItem() != null && specialItem != null; }

    @Override
    /**
     * Check if the mail item is in hands
     * @param id Id of the item to check if it is in robot's hands
     */
    public boolean itemIsInHands(String id) {
        if (this.getDeliveryItem() != null && this.getDeliveryItem().id.equals(id)
                || this.specialItem != null && specialItem.id.equals(id)){
            return true;
        }
        else {
            return false;
        }
    }

    private boolean onlySpecial() {
        return (getTube() == null && getDeliveryItem() == null && specialItem != null) ;
    }

    /**
     * Deliver fragile item and handles wrapping and unwrapping
     * Overrided from Robot
     *
     * @throws ExcessiveDeliveryException
     */
    @Override
    protected void deliverMail() throws ExcessiveDeliveryException {
        if (this.getDeliveryItem() != null && this.getDestination_floor() == this.getDeliveryItem().destination_floor){
            /** Delivery complete, report this to the simulator! */
            delivery.deliver(this.getDeliveryItem());
            System.out.printf("T: %3d > %9s-> Deliver item %s%n", Clock.Time(), getIdTube(), this.getDeliveryItem().id);
            this.setDeliveryItem(null);
        }
        // If item to be delivered is fragile
        else if (specialItem != null && this.getDestination_floor() == this.specialItem.destination_floor){

            // If item is already unwrapped and ready to deliver, deliver and end delivery
            if (specialItem.getWrapping() == specialItem.UNWRAPPED){
                this.delivery.deliver(specialItem);
                specialItem = null;
                return;
            }

            // Hits this is not ready to be delivered, in which case, the item has to be unwrapped
            handleUnwrapping();
        }

        this.incrementDeliveryCounter();
        if(this.getDeliveryCounter() > 3){  // Implies a simulation bug
            throw new ExcessiveDeliveryException();
        }
    }

    public void handleWrapping(){
        if (this.getCurrent_floor() == Building.MAILROOM_LOCATION && this.specialItem != null) {
            if (specialItem.getWrapping() == MailItem.UNWRAPPED){
                specialItem.startWrapping();
            }
            else if (specialItem.getWrapping() == MailItem.HALF_WRAPPED) {
                specialItem.finishWrapping();
            }
            this.dispatch();
        }
    }

    public void handleUnwrapping() {
        if (this.getCurrent_floor() == this.getDestination_floor() && this.specialItem != null){
            if (specialItem.getWrapping() == MailItem.WRAPPED) {
                specialItem.unwrap();
            }
        }
    }

    @Override
    /**
     * Planning for delivery
     * Handles things required before a delivery
     */
    protected void handlePreDelivery(){

        // Only deliver if you either have at least delivery item or special item
        // But if there is a special item, do not delivery if it is not fully wrapped
        if (getDeliveryItem() != null && specialItem == null || specialItem != null && specialItem.getWrapping() == MailItem.WRAPPED) {
            setRoute();
            this.current_state = RobotState.DELIVERING;
            return;
        }

        // Only hits here is special item is present but unwrapped/partially wrapped, in which the wrapping will be handled
        handleWrapping();
    }

    @Override
    /**
     * Sets the route for the robot
     */
    protected void setRoute(){
        if (this.getDeliveryItem() != null) {
            this.setDestination_floor(this.getDeliveryItem().destination_floor);
        }
        else if (specialItem != null) {
            this.setDestination_floor(specialItem.destination_floor);
        }
    }

    /**
     * Generic function that moves the robot towards the destination
     * @param destination the floor towards which the robot is moving
     */
    @Override
    protected void moveTowards(int destination) {
        if( Math.abs(this.getCurrent_floor() - destination) == 1 && checkFloor(destination)
                && this.specialItem != null && getTube() == null && getDeliveryItem() == null){
            return;
        }

        //Checks if when not carrying fragile item, another robot carrying fragile item is present on the floor
        if(Math.abs(this.getCurrent_floor() - destination) == 1 && checkFragileDelivery(destination) == true){
            return;
        }

        if(this.getCurrent_floor() < destination){
            this.setCurrent_floor(this.getCurrent_floor()+1);
        } else {
            this.setCurrent_floor(this.getCurrent_floor()-1);
        }
    }

    @Override
    /**
     * String to print when robot is delivering
     */
    public void printDelivery() {
        super.printDelivery();
        if (specialItem != null && this.getDeliveryItem() == null){
            System.out.printf("T: %3d > %9s-> [%s]%n", Clock.Time(), getIdTube(), specialItem.toString());
        }
    }

    @Override
    /**
     * Adds item to special hands or normal hands depending on mailItem
     * @param mailItem Item mailPool gives the robot
     */
    public void addToHand(MailItem mailItem) throws ItemTooHeavyException{
        if (mailItem.fragile){
            boolean success = addToSpecialHands(mailItem);
            if (!success){
                return;
            }

        }
        else{
            assert(this.getDeliveryItem() == null);
            if (this.getDeliveryItem() != null){
                return;
            }
            this.setDeliveryItem(mailItem);
            if (this.getDeliveryItem().weight > INDIVIDUAL_MAX_WEIGHT) throw new ItemTooHeavyException();
        }
    }

    /**
     * Add an item to special hands
     * @param mailItem The mail item to add to special hands
     * @return If the item has been successfully added to the special hands
     * @throws ItemTooHeavyException
     */
    public boolean addToSpecialHands(MailItem mailItem) throws ItemTooHeavyException {
        assert(this.specialItem == null);
        if (specialItem != null){
            return false;
        }
        specialItem = mailItem;
        if (specialItem.weight > INDIVIDUAL_MAX_WEIGHT) throw new ItemTooHeavyException();
        return true;
    }

    /**
     * As a robot carrying fragile item, checks whether there's another robot on the same floor
     * @param destFloor the current floor of the robot
     */
    public boolean checkFloor(int destFloor){
        LinkedList<Robot> robots = RobotManager.getInstance().getRobots();
        for(Robot robot : robots){

            // if you found a robot in the same floor
            if (robot.getCurrent_floor() == destFloor && robot.getDestination_floor() == destFloor) {
                return true;
            }
        }
        return false;
    }

    /**
     * As a robot not carrying fragile item, checks the floor to see whether another robot with fragile mail is going there
     * @param destFloor the current floor of the robot
     */
    public boolean checkFragileDelivery(int destFloor){
        LinkedList<Robot> robots = RobotManager.getInstance().getRobots();
        for(Robot robot : robots){
            if (robot instanceof SpecialRobot){
                SpecialRobot specialRobot = (SpecialRobot) robot;

                // If a robot is delivering fragile item on floor we're trying to move to
                if (specialRobot.getCurrent_floor() == destFloor && specialRobot.onlySpecial()
                        && specialRobot.getDestination_floor() == destFloor) {
                    return true;
                }
            }
        }
        return false;
    }
}
