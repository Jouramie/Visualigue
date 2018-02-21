package model;

import java.util.Map.Entry;
import java.util.TreeMap;

public class Trajectory implements java.io.Serializable {

    protected TreeMap<Double, Vector2D> positions;
    protected TreeMap<Double, Vector2D> orientations;

    public Trajectory() {
        this.positions = new TreeMap<>();
        this.orientations = new TreeMap<>();
    }

    public void setPosition(double time, double interpolation, Vector2D position) {
        if (this.positions.isEmpty()) {
            this.positions.put(0d, position);
            this.positions.put(time, position);
            return;
        }
        double beginTime = Math.max(0, time - interpolation);
        double endTime = Math.min(getDuration(), time + interpolation);

        Vector2D beginPos = getPosition(beginTime);
        Vector2D endPos = getPosition(endTime);

        flushPositions(beginTime, endTime);

        this.positions.put(beginTime, beginPos);
        this.positions.put(endTime, endPos);
        this.positions.put(time, position);
    }

    public void setOrientation(double time, double interpolation, Vector2D orientation) {
        if (this.orientations.isEmpty()) {
            this.orientations.put(0d, orientation);
            this.orientations.put(time, orientation);
            return;
        }
        double beginTime = Math.max(0, time - interpolation);
        double endTime = Math.min(0, time - interpolation);

        Vector2D beginOrientation = getOrientation(beginTime);
        Vector2D endOrientation = getOrientation(endTime);

        flushOrientations(beginTime, endTime);

        this.orientations.put(beginTime, beginOrientation);
        this.orientations.put(endTime, endOrientation);
        this.orientations.put(time, orientation);
    }

    public Vector2D getPosition(double time) {
        if (this.positions.isEmpty()) {
            return new Vector2D();
        }

        Vector2D pos = this.positions.get(time);
        if (pos != null) {
            return pos;
        }

        Entry<Double, Vector2D> floorEntry = this.positions.floorEntry(time);
        Entry<Double, Vector2D> ceilingEntry = this.positions.ceilingEntry(time);
        if (floorEntry == null) {
            floorEntry = this.positions.firstEntry();
        }
        if (ceilingEntry == null) {
            ceilingEntry = this.positions.lastEntry();
        }

        Vector2D result = floorEntry.getValue().clone();
        double delta = (time - floorEntry.getKey()) / (ceilingEntry.getKey() - floorEntry.getKey());
        Vector2D diff = ceilingEntry.getValue().substract(floorEntry.getValue());
        if (diff.equals(new Vector2D())) {
            return result;
        }
        diff = diff.multiply(delta);
        result = result.add(diff);
        return result;
    }

    public Vector2D getOrientation(double time) {
        // TODO: changer pour une meilleure interpolation.
        if (this.orientations.isEmpty()) {
            return new Vector2D();
        }

        Vector2D pos = this.orientations.get(time);
        if (pos != null) {
            return pos;
        }

        Entry<Double, Vector2D> floorEntry = this.orientations.floorEntry(time);
        Entry<Double, Vector2D> ceilingEntry = this.orientations.ceilingEntry(time);
        if (floorEntry == null) {
            floorEntry = this.orientations.firstEntry();
        }
        if (ceilingEntry == null) {
            ceilingEntry = this.orientations.lastEntry();
        }

        Vector2D result = floorEntry.getValue().clone();
        double delta = (time - floorEntry.getKey()) / (ceilingEntry.getKey() - floorEntry.getKey());
        Vector2D diff = ceilingEntry.getValue().substract(floorEntry.getValue());
        if (diff.equals(new Vector2D())) {
            return result;
        }
        diff = diff.multiply(delta);
        result = result.add(diff);
        return result;
    }

    public double getDuration() {
        double duration = 0;
        if (!this.positions.isEmpty() && !this.orientations.isEmpty()) {
            duration = Math.max(this.positions.lastKey(), this.orientations.lastKey());
        }
        return duration;
    }

    public void flushPositions(double begin, double end) {
        if (begin < end) {
            TreeMap<Double, Vector2D> temp = new TreeMap<>(this.positions.subMap(begin, end));
            if (this.positions.containsKey(end)) {
                temp.put(end, new Vector2D());
            }

            for (Double keys : temp.keySet()) {
                this.positions.remove(keys);
            }
        }
    }

    public void flushOrientations(double begin, double end) {
        if (begin < end) {
            TreeMap<Double, Vector2D> temp = new TreeMap<>(this.orientations.subMap(begin, end));
            if (this.orientations.containsKey(end)) {
                temp.put(end, new Vector2D());
            }

            for (Double keys : temp.keySet()) {
                this.orientations.remove(keys);
            }
        }
    }

    public double getPreviousKeyFrame(double currentTime) {
        double time = 0.0;
        if (this.positions != null && currentTime > 0) {
            time = this.positions.lowerKey(currentTime);
        }
        return time;
    }

    public double getNextKeyFrame(double currentTime) {
        double time = currentTime;
        if (this.positions != null && currentTime < this.positions.lastKey()) {
            time = this.positions.higherKey(currentTime);
        }
        return time;
    }
}
