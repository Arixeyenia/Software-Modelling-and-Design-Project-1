package automail;

import java.util.Map;
import java.util.TreeMap;

// import java.util.UUID;

/**
 * Represents a mail item
 */
public class MailItem{
	
    /** Represents the destination floor to which the mail is intended to go */
    protected final int destination_floor;
    /** The mail identifier */
    protected final String id;
    /** The time the mail item arrived */
    protected final int arrival_time;
    /** The weight in grams of the mail item */
    protected final int weight;
    /** The fragile property */
    protected final boolean fragile;

    public static final int UNWRAPPED = 0;
    public static final int HALF_WRAPPED = 1;
    public static final int WRAPPED = 2;
    public static final int TOTAL_WRAPPING_TIME = 3;
    private int wrapping = UNWRAPPED;

    // Retrieves/Changes the wrapping status
    public int getWrapping() {
        return wrapping;
    }

    public void wrap() {
        if (fragile && wrapping != WRAPPED) {
            this.wrapping++;
        }
    }

    public void unwrap() {
        if (fragile && wrapping == WRAPPED) {
            this.wrapping = UNWRAPPED;
        }
    }

    /**
     * Constructor for a MailItem
     * @param dest_floor the destination floor intended for this mail item
     * @param arrival_time the time that the mail arrived
     * @param weight the weight of this mail item
     */
    public MailItem(int dest_floor, int arrival_time, int weight, boolean isFragile){
        this.destination_floor = dest_floor;
        this.id = String.valueOf(hashCode());
        this.arrival_time = arrival_time;
        this.weight = weight;
        this.fragile = isFragile;
    }

    @Override
    public String toString(){
        return String.format("Mail Item:: ID: %6s | Arrival: %4d | Destination: %2d | Weight: %4d | %7s", id, arrival_time, destination_floor, weight, (fragile ? "fragile" : "normal"));
    }

    /**
     *
     * @return the destination floor of the mail item
     */
    public int getDestFloor() {
        return destination_floor;
    }
    
    /**
     *
     * @return the ID of the mail item
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @return the arrival time of the mail item
     */
    public int getArrivalTime(){
        return arrival_time;
    }

    /**
    *
    * @return the weight of the mail item
    */
   public int getWeight(){
       return weight;
   }

    public boolean getFragile() { return fragile; }
   
	static private int count = 0;
	static private Map<Integer, Integer> hashMap = new TreeMap<Integer, Integer>();

	@Override
	public int hashCode() {
		Integer hash0 = super.hashCode();
		Integer hash = hashMap.get(hash0);
		if (hash == null) { hash = count++; hashMap.put(hash0, hash); }
		return hash;
	}
}
