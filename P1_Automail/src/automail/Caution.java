package automail;

public class Caution {

    /**
     * Checks whether there's another robot on the same floor
     * @param robotList a list of all active robots
     * @param destFloor the current floor of the robot
     */
    static boolean checkFloor(SpecialRobot[] robotList, int destFloor){

        for(int i =0; i<robotList.length; i++){
            if (robotList[i].getCurrent_floor() == destFloor) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks the floor to see whether another robot with fragile mail is going there
     * @param robotList a list of all active robots
     * @param destFloor the current floor of the robot
     */
    static boolean checkFragile(SpecialRobot[] robotList, int destFloor){

        for(int i =0; i<robotList.length; i++){
            if (robotList[i].getCurrent_floor() == destFloor && robotList[i].getSpecialItem() != null) {
                return true;
            }
        }

        return false;
    }
}
