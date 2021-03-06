package model;

import java.util.ArrayList;
import java.util.List;

public class Sport implements java.io.Serializable {
    private final List<ObstacleDescription> obstacleDescriptions;
    private final List<BallDescription> ballDescriptions;
    private final List<PlayerDescription> playerDescriptions;
    private String name;
    private int maxPlayer;
    private int maxTeam;
    private String courtImage;
    private Vector2D courtSize;

    public Sport(String name, String courtImage, double courtHeight, double courtWidth, int maxPlayer, int maxTeam) throws ValidationException {
        setName(name);
        setCourtImage(courtImage);
        setCourtSize(new Vector2D(courtWidth, courtHeight));
        setMaxPlayer(maxPlayer);
        setMaxTeam(maxTeam);

        this.obstacleDescriptions = new ArrayList<>();
        this.ballDescriptions = new ArrayList<>();
        this.playerDescriptions = new ArrayList<>();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) throws ValidationException {
        if (name == null || name.isEmpty()) {
            throw new ValidationException("Nom invalide");
        }
        this.name = name;
    }

    public String getCourtImage() {
        return this.courtImage;
    }

    public void setCourtImage(String courtImage) throws ValidationException {
        if (courtImage == null || courtImage.isEmpty()) {
            throw new ValidationException("Image invalide");
        }
        this.courtImage = courtImage;
    }

    public Vector2D getCourtSize() {
        return this.courtSize;
    }

    public void setCourtSize(Vector2D courtSize) throws ValidationException {
        if (courtSize == null || courtSize.getX() <= 0 || courtSize.getY() <= 0) {
            throw new ValidationException("Dimensions invalides");
        }
        this.courtSize = courtSize;
    }

    public int getMaxPlayer() {
        return this.maxPlayer;
    }

    public void setMaxPlayer(int maxPlayer) throws ValidationException {
        if (maxPlayer < 1) {
            throw new ValidationException("Nombre de joueurs max invalide");
        }
        this.maxPlayer = maxPlayer;
    }

    public int getMaxTeam() {
        return this.maxTeam;
    }

    public void setMaxTeam(int maxTeam) throws ValidationException {
        if (maxTeam < 1) {
            throw new ValidationException("Nombre d'équipes invalide");
        }
        this.maxTeam = maxTeam;
    }

    public void addBallDescription(BallDescription desc) {
        this.ballDescriptions.add(desc);
    }

    public BallDescription getBallDescription(String name) {
        if (name == null) {
            return null;
        }

        for (BallDescription desc : this.ballDescriptions) {
            if (desc.getName().equals(name)) {
                return desc;
            }
        }
        return null;
    }

    public List<BallDescription> getAllBallDescriptions() {
        return this.ballDescriptions;
    }

    public void addPlayerDescription(PlayerDescription desc) {
        this.playerDescriptions.add(desc);
    }

    public PlayerDescription getPlayerDescription(String name) {
        if (name == null) {
            return null;
        }

        for (PlayerDescription desc : this.playerDescriptions) {
            if (desc.getName().equals(name)) {
                return desc;
            }
        }
        return null;
    }

    public List<PlayerDescription> getAllPlayerDescriptions() {
        return this.playerDescriptions;
    }

    public void addObstacleDescription(ObstacleDescription desc) {
        this.obstacleDescriptions.add(desc);
    }

    public ObstacleDescription getObstacleDescription(String name) {
        if (name == null) {
            return null;
        }

        for (ObstacleDescription desc : this.obstacleDescriptions) {
            if (desc.getName().equals(name)) {
                return desc;
            }
        }
        return null;
    }

    public List<ObstacleDescription> getAllObstacleDescriptions() {
        return this.obstacleDescriptions;
    }

    public void deleteElementDescription(ElementDescription desc) {
        switch (desc.getType()) {
            case Ball:
                this.ballDescriptions.remove(desc);
                break;
            case Player:
                this.playerDescriptions.remove(desc);
                break;
            case Obstacle:
                this.obstacleDescriptions.remove(desc);
                break;
        }
    }
}
