package model;

import java.util.ArrayList;
import java.util.List;

public class Strategy implements java.io.Serializable {
    private final String name;
    private final List<Element> elements;
    private final Sport sport;
    private int nbPlayer;

    public Strategy(String name, Sport sport) throws ValidationException {
        if (name == null || name.isEmpty()) {
            throw new ValidationException("Nom invalide.");
        }
        if (sport == null) {
            throw new ValidationException("Sport invalide.");
        }

        this.name = name;
        this.sport = sport;
        this.elements = new ArrayList<>();
        this.nbPlayer = 0;
    }

    public ObstacleElement createObstacle(ObstacleDescription desc) {
        ObstacleElement elem = new ObstacleElement(desc);
        this.elements.add(elem);
        return elem;
    }

    public Ball createBall(BallDescription desc) {
        Ball elem = new Ball(desc);
        this.elements.add(elem);
        return elem;
    }

    public Player createPlayer(PlayerDescription desc) {
        return createPlayer(desc, 0);
    }

    public Player createPlayer(PlayerDescription desc, int team) {
        Player elem = new Player(desc, team);
        this.elements.add(elem);
        this.nbPlayer++;
        return elem;

    }

    public void deleteElement(Element elem) {
        for (int i = 0; i < this.elements.size(); i++) {
            Element e = this.elements.get(i);
            if (e == elem) {
                if (e instanceof Player) {
                    this.nbPlayer--;
                }

                this.elements.remove(i);

                return;
            }
        }
    }

    public String getName() {
        return this.name;
    }

    public double getDuration() {
        double result = 0.0;

        for (Element elem : this.elements) {
            double duration = elem.getTrajectoryDuration();
            if (duration > result) {
                result = duration;
            }
        }

        return result;
    }

    public List<Element> getAllElements() {
        return this.elements;
    }

    public int getNbPlayer() {
        int result = 0;

        for (Element elem : this.elements) {
            if (elem instanceof Player) {
                result++;
            }
        }

        return result;
    }

    public Sport getSport() {
        return this.sport;
    }
}
