package automail;

import strategies.IMailPool;

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

    /**
     * Delivers a fragile item, including wrapping it and unwrapping it
     *
     * @param fragileItem the fragile item to be wrapped.
     */
    void deliverFragile(MailItem fragileItem) {

    }
}
