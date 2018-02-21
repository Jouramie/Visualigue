package model;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class BallTrajectory extends Trajectory {
    private final TreeMap<Double, Player> owners;
    private final Vector2D ballSize;

    public BallTrajectory(Vector2D ballSize) {
        this.owners = new TreeMap<>();
        this.ballSize = ballSize;
    }

    public void giveToOwner(double time, Player player) {
        if (this.owners.isEmpty()) //This could be removed... I think
        {
            this.owners.put(0d, player);
            return;
        }

        if (this.owners.floorEntry(time) != null && this.owners.floorEntry(time).getValue() != player) {
            this.owners.put(time, player);
        }

        if (this.positions.get(time) != null) {
            this.positions.remove(time);
        }
    }

    public void takeFromOwner(double time) {
        if (this.owners.floorEntry(time) != null && this.owners.floorEntry(time).getValue() != null) {
            this.positions.put(time, getBallPositionFromPlayer(time));
        }
        this.owners.put(time, null);
    }

    public void takeFromLastOwner(double time) {
        if (this.owners.floorEntry(time) != null && this.owners.floorEntry(time).getValue() != null) {
            this.positions.put(time, getBallPositionFromPlayer(time));
        }
        this.owners.put(time, null);
    }

    @Override
    public Vector2D getPosition(double time) {
        Vector2D pos;

        if (this.owners.floorEntry(time) != null && this.owners.floorEntry(time).getValue() != null) {
            if (this.owners.ceilingEntry(time) != null && this.owners.ceilingEntry(time).getValue() != null) {
                Vector2D lastPos = getBallPositionFromPlayer(this.owners.floorKey(time));
                Vector2D nextPos = getBallPositionFromPlayer(this.owners.ceilingKey(time));
                double delta = (time - this.owners.floorKey(time)) / (this.owners.ceilingKey(time) - this.owners.floorKey(time));
                pos = interpolate(lastPos, nextPos, delta);
            } else {
                pos = getBallPositionFromPlayer(time);
            }
        } else {
            Vector2D lastPos = new Vector2D();
            Vector2D nextPos = new Vector2D();
            double delta = 0;

            double lastOwnerTime = -Double.MAX_VALUE; //The last time the ball belonged to a player
            if (time >= this.owners.firstKey()) {
                lastOwnerTime = this.owners.floorKey(time);
            }
            while (this.owners.get(lastOwnerTime) == null) {
                if (lastOwnerTime <= this.owners.firstKey()) {
                    lastOwnerTime = -Double.MAX_VALUE;
                    break;
                }

                lastOwnerTime = this.owners.lowerKey(lastOwnerTime);
            }
            if (lastOwnerTime != -Double.MAX_VALUE) {
                lastOwnerTime = this.owners.higherKey(lastOwnerTime);
            }

            double nextOwnerTime = Double.MAX_VALUE; //The next time the ball will belong to a player
            if (time <= this.owners.lastKey()) {
                nextOwnerTime = this.owners.ceilingKey(time);
            }
            while (this.owners.get(nextOwnerTime) == null) {
                if (nextOwnerTime >= this.owners.lastKey()) {
                    nextOwnerTime = Double.MAX_VALUE;
                    break;
                }

                nextOwnerTime = this.owners.higherKey(nextOwnerTime);
            }

            double lastPositionTime = -Double.MAX_VALUE; //The last time the position was recorded
            if (time >= this.positions.firstKey()) {
                lastPositionTime = this.positions.floorKey(time);
            }

            double nextPositionTime = Double.MAX_VALUE; //The next time the position will be recorded
            if (time <= this.positions.lastKey()) {
                nextPositionTime = this.positions.ceilingKey(time);
            }

            if (nextPositionTime <= nextOwnerTime) //Interpolation to ball position
            {
                if (lastPositionTime >= lastOwnerTime) //Interpolation from ball position
                {
                    lastPos = this.positions.get(lastPositionTime);
                    nextPos = this.positions.get(nextPositionTime);
                    delta = (time - lastPositionTime) / (nextPositionTime - lastPositionTime);
                } else //Interpolation from player
                {
                    lastPos = getBallPositionFromPlayer(lastOwnerTime);
                    nextPos = this.positions.get(nextPositionTime);
                    delta = (time - lastOwnerTime) / (nextPositionTime - lastOwnerTime);
                }
            } else //Interpolation to player
            {
                if (lastPositionTime >= lastOwnerTime) //Interpolation from ball position
                {
                    lastPos = this.positions.get(lastPositionTime);
                    nextPos = getBallPositionFromPlayer(nextOwnerTime);
                    delta = (time - lastPositionTime) / (nextOwnerTime - lastPositionTime);
                } else //Interpolation from player
                {
                    lastPos = getBallPositionFromPlayer(lastOwnerTime);
                    nextPos = getBallPositionFromPlayer(nextOwnerTime);
                    delta = (time - lastOwnerTime) / (nextOwnerTime - lastOwnerTime);
                }
            }

            pos = interpolate(lastPos, nextPos, delta);
        }

        return pos;
    }

    @Override
    public double getDuration() {
        double duration = 0;
        if (!this.positions.isEmpty() && !this.orientations.isEmpty() && !this.owners.isEmpty()) {
            duration = Math.max(this.positions.lastKey(), this.orientations.lastKey());
            duration = Math.max(duration, this.owners.lastKey());
        }
        return duration;
    }

    private Vector2D getBallPositionFromPlayer(double time) {
        Player player = this.owners.floorEntry(time).getValue();
        Vector2D playerPos = player.getPosition(time);
        Vector2D playerOri = player.getOrientation(time);
        Vector2D size = player.getElementDescription().getSize();

        double x = playerPos.getX() + Math.cos(playerOri.getAngle()) * ((size.getX() + this.ballSize.getX()) / 2);
        double y = playerPos.getY() + Math.sin(playerOri.getAngle()) * ((size.getY() + this.ballSize.getY()) / 2);

        return new Vector2D(x, y);
    }

    private Vector2D interpolate(Vector2D lastPos, Vector2D nextPos, double delta) {
        Vector2D result;
        if (lastPos != null && nextPos != null) {
            Vector2D pos = lastPos.clone();

            Vector2D diff = nextPos.substract(lastPos);
            if (diff.equals(new Vector2D())) {
                return pos;
            }
            diff = diff.multiply(delta);
            result = pos.add(diff);
        } else {
            if (lastPos != null) {
                result = lastPos;
            } else if (nextPos != null) {
                result = nextPos;
            } else {
                result = new Vector2D();
            }
        }

        return result;
    }

    public void deletePlayer(Player player) {
        for (Iterator<Map.Entry<Double, Player>> it = this.owners.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Double, Player> entry = it.next();
            if (entry.getValue() == player) {
                it.remove();
            }
        }
    }
}
