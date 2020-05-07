package automail;

import exceptions.ExcessiveDeliveryException;
import exceptions.ItemTooHeavyException;
import strategies.IMailPool;

import java.util.LinkedList;

public class SpecialRobot extends Robot{

    // Special arm for fragile items
    private MailItem specialItem = null;

    /**
     * Initiates the robot's location at the start to be at the mailroom
     * also set it to be waiting for mail.
     *
     * @param delivery  governs the final delivery
     * @param mailPool  is the source of mail items
     * @param behaviour governs selection of mail items for delivery and behaviour on priority arrivals
     */
    public SpecialRobot(IMailDelivery delivery, IMailPool mailPool) {
        super(delivery, mailPool);
    }

    //SPECIAL BEHAVIOURS
    //WRAPPING AND UNWRAPPING FUNCTION (SEPARATE)

    public MailItem getSpecialItem() {
        return specialItem;
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

    /**
     * Delivers a fragile item, including wrapping it and unwrapping it
     *
     * @param fragileItem the fragile item to be wrapped.
     */
    void deliverFragile(MailItem fragileItem) {

    }

    @Override
    public boolean isEmpty() {
        return (this.getDeliveryItem() == null && this.getTube() == null && specialItem == null);
    }

    @Override
    public boolean handsFull() { return this.getDeliveryItem() != null && specialItem != null; }

    /**
     * Deliver fragile item and handles wrapping and unwrapping
     * Overrided from Robot
     *
     * @throws ExcessiveDeliveryException
     */
    @Override
    public void deliverMail() throws ExcessiveDeliveryException {
        if (this.getDeliveryItem() != null && this.getDestination_floor() == this.getDeliveryItem().destination_floor){
            /** Delivery complete, report this to the simulator! */
            delivery.deliver(this.getDeliveryItem());
            System.out.printf("T: %3d > %9s-> Deliver item %s%n", Clock.Time(), getIdTube(), this.getDeliveryItem().id);
            this.setDeliveryItem(null);
        }
        else if (specialItem != null && this.getDestination_floor() == this.specialItem.destination_floor){
            if (specialItem.getWrapping() == specialItem.WRAPPED){
                specialItem.unwrap();
                return;
            }
            else if (specialItem.getWrapping() == specialItem.UNWRAPPED){
                this.delivery.deliver(specialItem);
                specialItem = null;
            }
        }
        this.incrementDeliveryCounter();
        if(this.getDeliveryCounter() > 3){  // Implies a simulation bug
            throw new ExcessiveDeliveryException();
        }
    }

    @Override
    public void addToHand(MailItem mailItem) throws ItemTooHeavyException{
        if (mailItem.fragile){
            assert(this.specialItem == null);
            if (specialItem != null){
                return;
            }
            specialItem = mailItem;
            if (specialItem.weight > INDIVIDUAL_MAX_WEIGHT) throw new ItemTooHeavyException();
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
     * Generic function that moves the robot towards the destination
     * @param destination the floor towards which the robot is moving
     */
    @Override
    protected void moveTowards(int destination) {
        //Checks if when carrying fragile item, another robot is present on the floor
        if( Math.abs(this.getCurrent_floor() - destination) == 1 && checkFloor(destination) && this.specialItem != null && getTube() == null && getDeliveryItem() == null ){
            System.out.println("check alone <" + this.id+ "| current floor:"+ this.getCurrent_floor()+ "|state: " + this.current_state.toString()+ "> destination: " + destination + " |special item:" + this.specialItem);
            return;
        }

        //Cater to mailroom
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

    /**
     * As a robot carrying fragile item, checks whether there's another robot on the same floor
     * @param destFloor the current floor of the robot
     */
    public boolean checkFloor(int destFloor){
//        LinkedList<Robot> robots = getMailPool().getRobots();
        LinkedList<Robot> robots = RobotManager.getInstance().getRobots();
        for(Robot robot : robots){

            // if you found a robot in the same floor
            if (robot.getCurrent_floor() == destFloor) {
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
                if (specialRobot.getCurrent_floor() == destFloor  && specialRobot.getTube() == null && specialRobot.getDeliveryItem() == null && specialItem != null && specialRobot.id != this.id) {
                    System.out.println("current floor: " + getCurrent_floor() + " specialR: " + specialRobot.id);
                    return true;
                }
            }
        }


        return false;
    }

    @Override
    protected void setRoute(){
        if (this.getDeliveryItem() != null) {
            this.setDestination_floor(this.getDeliveryItem().destination_floor);
        }
        else if (specialItem != null) {
            this.setDestination_floor(specialItem.destination_floor);
        }
    }

    @Override
    //String to print when robot is delivering
    public void printDelivery() {
        super.printDelivery();
        if (specialItem != null && this.getDeliveryItem() == null){
            System.out.printf("T: %3d > %9s-> [%s]%n", Clock.Time(), getIdTube(), specialItem.toString());
        }
    }

    @Override
    //Planning for delivery
    protected void handlePreDelivery(){
        if (specialItem != null) {
            if (specialItem.getWrapping() == specialItem.UNWRAPPED){
                specialItem.startWrapping();
                }
            else if(specialItem.getWrapping() == specialItem.HALF_WRAPPED){
                specialItem.finishWrapping();
                }
        }

        /**Set route if:
            - There is delivery item and either:
                - No special item or
                - The special item is fully wrapped

            Will not set route if:
                - There is no delivery item
                - Special item is not properly wrapped
         */
        if (specialItem != null && specialItem.getWrapping() != specialItem.WRAPPED){
            return;
        }
        if(getDeliveryItem() != null || specialItem != null) {
            setRoute();
            this.current_state = RobotState.DELIVERING;
        }
    }

    @Override
    //Check if the mail item is in hands
    public boolean itemIsInHands(String id) {
        if (this.getDeliveryItem() != null && this.getDeliveryItem().id.equals(id) || this.specialItem != null && specialItem.id.equals(id)){
            return true;
        }
        else {
            return false;
        }
    }
}
