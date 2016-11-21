package controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import model.BallDescription;
import model.Element;
import model.ElementDescription;
import model.ObstacleDescription;
import model.PlayerDescription;
import model.Sport;
import model.Strategy;
import model.ValidationException;
import model.Vector2D;

public class GodController
{

    public static final double FPS = 2;

    private Map<String, Sport> sports;
    private Strategy strategy;
    private double time;
    private ElementDescription currentElementDescription;
    private Element selectedElement;

    private PlayerDescription playerDescription;
    private BallDescription ballDescription;
    private ObstacleDescription obstacleDescription;

    private Updatable window;
    
    private StrategyPlayer sp;

    public GodController()
    {
        this.sports = new TreeMap();
        this.strategy = null;
        this.time = 0.0;
        this.currentElementDescription = null;
        this.selectedElement = null;

        // Tests values
        playerDescription = new PlayerDescription("player", new Vector2D(40, 40), "/res/player.png");
        ballDescription = new BallDescription("ball", new Vector2D(20, 20), "/res/test.png");
        obstacleDescription = new ObstacleDescription("obstacle", new Vector2D(20, 20), "/res/cone.png");
        this.strategy = new Strategy("Test", null);

        try 
        {
            saveSport(null, "Hockey", "hockey.png", 400, 1000, 5, 2);
        }
        catch (ValidationException ex)
        {
            Logger.getLogger(GodController.class.getName()).log(Level.SEVERE, null, ex);
        }
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
            if (currentElementDescription instanceof ObstacleDescription)
            {
                elem = this.strategy.createObstacle((ObstacleDescription) currentElementDescription);
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
        } else if (name.equals("Ball"))
        {
            this.currentElementDescription = ballDescription;
        } else if (name.equals("Static"))
        {
            this.currentElementDescription = obstacleDescription;
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

    public void saveSport(String oldName, String newName, String courtImage, double courtHeight, double courtWidth, int playerNumber, int numTeams) throws ValidationException
    {
        Sport sport = null;
        if(oldName != null)
        {
            sport = sports.get(oldName);
        }
        
        if(sport != null)
        {
            sport.setName(newName);
            sport.setCourtImage(courtImage);
            sport.setCourtSize(new Vector2D(courtWidth, courtHeight));
            sport.setMaxPlayer(playerNumber);
            sport.setMaxTeam(numTeams);
        }
        
        else
        {
            sport = new Sport(newName, courtImage, courtHeight, courtWidth, playerNumber, numTeams);
            sports.put(newName, sport);
        }
    }
    
    public Sport getSport(String name)
    {
        if(name == null)
        {
            return null;
        }
        
        return sports.get(name);
    }

    public List<Sport> getSports()
    {
        return new ArrayList<Sport>(sports.values());
    }
    
    public void deleteSport(String sportName)
    {
        if(sportName != null)
        {
            sports.remove(sportName);
        }
    }
    
    public void saveBallDescription(String sportName, String oldName, String newName, String image, double height, double width) throws ValidationException
    {
        Sport sport = getSport(sportName);
        if(sport != null)
        {
            BallDescription desc = null;
            
            if(oldName != null)
            {
                desc = sport.getBallDescription(oldName);
            }

            if(desc != null)
            {
                desc.setName(newName);
                desc.setImage(image);
                desc.setSize(new Vector2D(width, height));
            }

            else
            {
                desc = new BallDescription(newName, new Vector2D(width, height), image);
                sport.addBallDescription(desc);
            }
        }
    }
    
    public BallDescription getBallDescription(String sportName, String name)
    {
        BallDescription desc = null;
        Sport sport = getSport(sportName);
        if(sport != null)
        {         
            if(name != null)
            {
                desc = sport.getBallDescription(name);
            }
        }
        
        return desc;
    }
    
    public void deleteBallDescription(String sportName, String name)
    {
        BallDescription desc = null;
        Sport sport = getSport(sportName);
        if(sport != null)
        {         
            if(name != null)
            {
                desc = sport.getBallDescription(name);
                sport.deleteElementDescription(desc);
            }
        }
    }
    
    public void savePlayerDescription(String sportName, String oldName, String newName, String image, double height, double width) throws ValidationException
    {
        Sport sport = getSport(sportName);
        if(sport != null)
        {
            PlayerDescription desc = null;
            
            if(oldName != null)
            {
                desc = sport.getPlayerDescription(oldName);
            }

            if(desc != null)
            {
                desc.setName(newName);
                desc.setImage(image);
                desc.setSize(new Vector2D(width, height));
            }

            else
            {
                desc = new PlayerDescription(newName, new Vector2D(width, height), image);
                sport.addPlayerDescription(desc);
            }
        }
    }
    
    public PlayerDescription getPlayerDescription(String sportName, String name)
    {
        PlayerDescription desc = null;
        Sport sport = getSport(sportName);
        if(sport != null)
        {         
            if(name != null)
            {
                desc = sport.getPlayerDescription(name);
            }
        }
        
        return desc;
    }
    
    public void deletePlayerDescription(String sportName, String name)
    {
        PlayerDescription desc = null;
        Sport sport = getSport(sportName);
        if(sport != null)
        {         
            if(name != null)
            {
                desc = sport.getPlayerDescription(name);
                sport.deleteElementDescription(desc);
            }
        }
    }
    
    public void saveObstacleDescription(String sportName, String oldName, String newName, String image, double height, double width) throws ValidationException
    {
        Sport sport = getSport(sportName);
        if(sport != null)
        {
            ObstacleDescription desc = null;
            
            if(oldName != null)
            {
                desc = sport.getObstacleDescription(oldName);
            }

            if(desc != null)
            {
                desc.setName(newName);
                desc.setImage(image);
                desc.setSize(new Vector2D(width, height));
            }

            else
            {
                desc = new ObstacleDescription(newName, new Vector2D(width, height), image);
                sport.addObstacleDescription(desc);
            }
        }
    }
    
    public ObstacleDescription getObstacleDescription(String sportName, String name)
    {
        ObstacleDescription desc = null;
        Sport sport = getSport(sportName);
        if(sport != null)
        {         
            if(name != null)
            {
                desc = sport.getObstacleDescription(name);
            }
        }
        
        return desc;
    }
    
    public void deleteObstacleDescription(String sportName, String name)
    {
        ObstacleDescription desc = null;
        Sport sport = getSport(sportName);
        if(sport != null)
        {         
            if(name != null)
            {
                desc = sport.getObstacleDescription(name);
                sport.deleteElementDescription(desc);
            }
        }
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
        } else
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

        @Override
        protected Void call() throws Exception
        {
            start = System.currentTimeMillis();
            long currentTime;

            do
            {
                currentTime = System.currentTimeMillis();
                time = (double)(currentTime - start) / 1000;
                window.update();
                Thread.sleep((long)(1000 / FPS));
            } while (time <= strategy.getDuration());

            time = ((int)(time*FPS))/FPS;
            return null;
        }
    }

    public void setWindow(Updatable window)
    {
        this.window = window;
    }
    
    public void playStrategy()
    {
        sp = new StrategyPlayer();
        Thread th = new Thread(sp);
        th.setDaemon(true);
        th.start();
    }
}
