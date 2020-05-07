package automail;
import java.util.LinkedList;

/**
 * Singleton class to manage robots and keep track of their location
 */
public class RobotManager {

    private static RobotManager instance;
    public static RobotManager getInstance() {
        if (instance == null) {
            instance = new RobotManager();
        }
        return instance;
    }

    private LinkedList<Robot> robots;

    private RobotManager(){
        this.robots = new LinkedList<>();
    }

    public void addRobots(Robot[] robots) {
        for (Robot robot : robots) {
            this.robots.add(robot);
        }
    }

    // get all robots
    public LinkedList<Robot> getRobots(){
        return (LinkedList<Robot>) robots.clone();
    }

}
