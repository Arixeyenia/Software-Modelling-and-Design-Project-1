package automail;

import exceptions.BreakingFragileItemException;
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



    /**
     * Deliver fragile item and handles wrapping and unwrapping
     * Overrided from Robot
     *
     * @throws ExcessiveDeliveryException
     */
    @Override
    public void deliverMail() throws ExcessiveDeliveryException {
        MailItem deliveryItem = this.getDeliveryItem();
        if(deliveryItem != null){
            /** Delivery complete, report this to the simulator! */
            delivery.deliver(deliveryItem);
            deliveryItem = null;
        }
        else if (specialItem.fragile && deliveryItem == null){
            if (specialItem.getWrapping() == specialItem.WRAPPED){
                specialItem.unwrap();
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
        MailItem deliveryItem = this.getDeliveryItem();
        assert(deliveryItem == null);
        if (mailItem.fragile){
            specialItem = mailItem;
            if (specialItem.weight > INDIVIDUAL_MAX_WEIGHT) throw new ItemTooHeavyException();
        }
        else{
            deliveryItem = mailItem;
            if (deliveryItem.weight > INDIVIDUAL_MAX_WEIGHT) throw new ItemTooHeavyException();
        }
    }

    /**
     * Handles situations if carriers are not empty
     * Override from Robot
     */
    @Override
    public void handleNotEmpty(){
        super.handleNotEmpty();
        if (specialItem != null) {
            if (specialItem.getWrapping() == specialItem.UNWRAPPED){
                specialItem.startWrapping();
            }
            else if(specialItem.getWrapping() == specialItem.HALF_WRAPPED){
                specialItem.finishWrapping();
            }
        }
    }

    /**
     * Generic function that moves the robot towards the destination
     * @param destination the floor towards which the robot is moving
     */
    @Override
    protected void moveTowards(int destination) {
        int current_floor = this.getCurrent_floor();

        //Checks if when carrying fragile item, another robot is present on the floor
        if(Math.abs(current_floor - destination) == 1 && checkFloor(destination) == true && getTube() == null && getDeliveryItem() == null){
            return;
        }

        //Checks if when not carrying fragile item, another robot carrying fragile item is present on the floor
        if(Math.abs(current_floor - destination) == 1 && checkFragileDelivery(destination) == true){
            return;
        }
        if(current_floor < destination){
            current_floor++;
        } else {
            current_floor--;
        }
    }

    /**
     * As a robot carrying fragile item, checks whether there's another robot on the same floor
     * @param destFloor the current floor of the robot
     */
    public boolean checkFloor(int destFloor){
        LinkedList<Robot> robots = getMailPool().getRobots();

        for(Robot robot : robots){
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
        LinkedList<Robot> robots = getMailPool().getRobots();

        for(Robot robot : robots){
            if (robot instanceof  SpecialRobot){
                SpecialRobot specialRobot = (SpecialRobot) robot;
                if (specialRobot.getCurrent_floor() == destFloor && specialRobot.getSpecialItem() != null) {
                    return true;
                }
            }
        }

        return false;
    }
}
