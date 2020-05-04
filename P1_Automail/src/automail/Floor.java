package automail;

import java.util.ArrayList;

public class Floor {
    public static int floor;
    private ArrayList<Robot> robotsOnFloor;

    public Floor(int floor){
        this.floor = floor;
        this.robotsOnFloor = new ArrayList<>();
    }

    public ArrayList<Robot> getRobotsOnFloor(){
        return (ArrayList<Robot>) this.robotsOnFloor.clone();
    }

    public void addToFloor(Robot robot){
        this.robotsOnFloor.add(robot);
    }

    public void removeFromFloor(Robot robot){
        for (Robot ourRobot:robotsOnFloor) {
            if (robot.id == ourRobot.id){
                this.robotsOnFloor.remove(robot);
                return;
            }
        }
    }
}
