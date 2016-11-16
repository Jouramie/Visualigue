package controller;

import java.util.ArrayList;
import java.util.List;
import javafx.concurrent.Task;
import model.BallDescription;
import model.Element;
import model.ElementDescription;
import model.PlayerDescription;
import model.Sport;
import model.StaticElementDescription;
import model.Strategy;
import model.Vector2D;

public class GodController
{

    public static final double FPS = 2;

    private List<Sport> sports;
    private Strategy strategy;
    private double time;
    private ElementDescription currentElementDescription;
    private Element selectedElement;

    private PlayerDescription playerDescription;
    private BallDescription ballDescription;
    private StaticElementDescription staticDescription;

    private Updatable window;

    private StrategyPlayer sp;

    public GodController()
    {
        this.sports = new ArrayList<Sport>();
        this.strategy = null;
        this.time = 0.0;
        this.currentElementDescription = null;
        this.selectedElement = null;

        // Tests values
        playerDescription = new PlayerDescription("player", new Vector2D(40, 40), "/res/player.png");
        ballDescription = new BallDescription("ball", new Vector2D(20, 20), "/res/test.png");
        staticDescription = new StaticElementDescription("static", new Vector2D(20, 20), "/res/cone.png");
        this.strategy = new Strategy("Test", null);

        this.sports.add(new Sport("Hockey", "hockey.png", 400, 1000, 5));
    }

    public void createStrategy(Sport sport, String name)
    {
        //this.strategy = new Strategy(name, sport);
    }

    public void saveStrategy()
    {
        // TODO
    }

    public void loadStrategy(String path)
    {

    }

    public Element addElement(Vector2D pos) throws Exception
    {
        Element elem = null;

        if (currentElementDescription != null)
        {
            if (currentElementDescription instanceof StaticElementDescription)
            {
                elem = this.strategy.createStaticElement((StaticElementDescription) currentElementDescription);
            }
            else if (currentElementDescription instanceof BallDescription)
            {
                elem = this.strategy.createBall((BallDescription) currentElementDescription);
            }
            else if (currentElementDescription instanceof PlayerDescription)
            {
                elem = this.strategy.createPlayer((PlayerDescription) currentElementDescription);
            }

            elem.setPosition(time, pos, 0.0);
            elem.setOrientation(time, new Vector2D(), 0.0);
        }
        return elem;
    }

    public void selectElement(Element elem)
    {
        this.selectedElement = elem;
    }

    public void selectElementDescription(String name)
    {
        if (name.equals("Player"))
        {
            this.currentElementDescription = playerDescription;
        }
        else if (name.equals("Ball"))
        {
            this.currentElementDescription = ballDescription;
        }
        else if (name.equals("Static"))
        {
            this.currentElementDescription = staticDescription;
        }
    }

    public void setCurrentElemPosition(Vector2D pos)
    {
        if (this.selectedElement != null)
        {
            this.selectedElement.setPosition(this.time, pos, 0.0);
        }
    }

    public void setCurrentElemOrientation(Vector2D ori)
    {
        if (this.selectedElement != null)
        {
            this.selectedElement.setOrientation(time, ori, 0.0);
        }
    }

    public List<Element> getAllElements()
    {
        if (this.strategy != null)
        {
            return this.strategy.getAllElements();
        }

        return new ArrayList<Element>();
    }

    public void addSport(String name, String courtImage, double courtHeight, double courtWidth, int playerNumber)
    {
        Sport sport = new Sport(name, courtImage, courtHeight, courtWidth, playerNumber);
        sports.add(sport);
    }

    public void saveSport(String oldName, String newName, String courtImage, double courtHeight, double courtWidth, int playerNumber)
    {
        for (Sport sport : sports)
        {
            if (sport.getName().equals(oldName))
            {
                sport.setName(newName);
                sport.setCourtImage(courtImage);
                sport.setCourtSize(new Vector2D(courtHeight, courtWidth));
                sport.setMaxPlayer(playerNumber);
            }
        }
    }

    public List<Sport> getSports()
    {
        return this.sports;
    }

    public double getCurrentTime()
    {
        return this.time;
    }

    public void setCurrentTime(double time)
    {
        if (time >= 0)
        {
            this.time = time;
        }
        else
        {
            this.time = 0;
        }
    }

    public void nextFrame()
    {
        setCurrentTime(time + 1);
    }

    public void prevFrame()
    {
        setCurrentTime(time - 1);
    }

    public double getDuration()
    {
        return strategy.getDuration();
    }

    private class StrategyPlayer extends Task<Void>
    {

        private long start;
        private boolean playing;

        @Override
        protected Void call() throws Exception
        {
            playing = true;
            start = System.currentTimeMillis();
            long previousTimeMillis;

            while (time <= strategy.getDuration())
            {
                previousTimeMillis = System.currentTimeMillis();
                Thread.sleep((long) (1000 / FPS));
                if (playing)
                {
                    time += (double) (System.currentTimeMillis() - previousTimeMillis) / 1000;
                    window.update();
                }
                else
                {
                    break;
                }
            }

            // Arrondissement
            time = ((int) (time * FPS)) / FPS;
            sp = null;
            window.wasLastUpdate();
            return null;
        }

        public void play()
        {
            playing = true;
        }

        public void pause()
        {
            playing = false;
        }
    }

    public void setWindow(Updatable window)
    {
        this.window = window;
    }

    public void playStrategy()
    {
        if (sp == null)
        {
            sp = new StrategyPlayer();
            Thread th = new Thread(sp);
            th.setDaemon(true);
            th.start();
        }
        else
        {
            sp.play();
        }
    }

    public void pauseStrategy()
    {
        if (sp == null)
        {
            throw new IllegalStateException("Something went wrong...");
        }
        else
        {
            sp.pause();
        }
    }
}
