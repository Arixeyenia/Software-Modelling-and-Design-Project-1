package automail;

import exceptions.ItemTooHeavyException;

public interface ISpecialRobot {

    /**
     * Handle wrapping of fragile items when first received
     */
    void handleWrapping();

    /**
     * Handle unwrapping of fragile items before delivering
     */
    void handleUnwrapping();

    /**
     * As a robot carrying fragile item, checks whether there's another robot on the same floor
     * @param destFloor the current floor of the robot
     */
    boolean checkFloor(int destFloor);

    /**
     * As a robot not carrying fragile item, checks the floor to see whether another robot with fragile mail is going there
     * @param destFloor the current floor of the robot
     */
    boolean checkFragileDelivery(int destFloor);

    /**
     * Add an item to special hands
     * @param mailItem The mail item to add to special hands
     * @return If the item has been successfully added to the special hands
     * @throws ItemTooHeavyException
     */
    boolean addToSpecialHands(MailItem mailItem) throws ItemTooHeavyException;
}
