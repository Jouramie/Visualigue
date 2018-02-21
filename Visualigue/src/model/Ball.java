package model;

public class Ball extends MobileElement implements java.io.Serializable {
    public Ball(BallDescription desc) {
        super(desc);
        this.trajectory = new BallTrajectory(this.description.getSize());
    }

    public void giveToOwner(double time, Player player) {
        ((BallTrajectory) this.trajectory).giveToOwner(time, player);
    }

    public void takeFromOwner(double time) {
        ((BallTrajectory) this.trajectory).takeFromOwner(time);
    }

    public void takeFromLastOwner(double time) {
        ((BallTrajectory) this.trajectory).takeFromLastOwner(time);
    }

    public void deletePlayer(Player player) {
        ((BallTrajectory) this.trajectory).deletePlayer(player);
    }
}
