package controller;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
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
import model.Player;
import model.PlayerDescription;
import model.Sport;
import model.Strategy;
import model.ValidationException;
import model.Vector2D;

public class GodController implements java.io.Serializable
{

    public static final double FPS = 2;
    public static final double FPS_PLAY = 10;
    
    private Map<String, Sport> sports;
    private Map<String, Strategy> strategies;
    private Strategy strategy;
    private double time;
    private ElementDescription currentElementDescription;
    private int currentTeam;
    private Element selectedElement;
    
    private PlayerDescription playerDescription;
    private BallDescription ballDescription;
    private ObstacleDescription obstacleDescription;
    private boolean respectMaxNbOfPlayers;
    
    private transient Updatable window;
    
    private transient StrategyPlayer sp;
    
    public GodController()
    {
        this.sports = new TreeMap();
        this.strategies = new TreeMap();
        this.strategy = null;
        this.time = 0.0;
        this.currentElementDescription = null;
        this.currentTeam = 0;
        this.selectedElement = null;
        this.respectMaxNbOfPlayers = false;
        
        try
        {
            Sport sport = saveSport(null, "Hockey", "/res/hockey.png", 400, 1000, 5, 2);
            sport.addPlayerDescription(new PlayerDescription("Joueur", new Vector2D(60, 60), "/res/player.png"));
            sport.addBallDescription(new BallDescription("Balle", new Vector2D(20, 20), "/res/test.png"));
            sport.addObstacleDescription(new ObstacleDescription("Obstacle", new Vector2D(20, 20), "/res/cone.png"));
            
            createStrategy("Test", "Hockey");
        } catch (ValidationException ex)
        {
            Logger.getLogger(GodController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void save()
    {
        try
        {
            FileOutputStream fileOut = new FileOutputStream("visualigue.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(this);
            out.close();
            fileOut.close();
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
    
    public Element addElement(Vector2D pos) throws Exception
    {
        Element elem = null;
        
        if (currentElementDescription != null)
        {
            if (currentElementDescription instanceof ObstacleDescription)
            {
                elem = this.strategy.createObstacle((ObstacleDescription) currentElementDescription);
                elem.setPosition(time, pos, 0.0);
                elem.setOrientation(time, new Vector2D(1, 0), 0.0);
            }
            else if (currentElementDescription instanceof BallDescription)
            {
                elem = this.strategy.createBall((BallDescription) currentElementDescription);
                elem.setPosition(time, pos, 0.0);
                elem.setOrientation(time, new Vector2D(1, 0), 0.0);
            }
            else if (currentElementDescription instanceof PlayerDescription && (!respectMaxNbOfPlayers || getNbOfPlayersInTeam(currentTeam) < getMaxNbOfPlayers()))
            {
                elem = this.strategy.createPlayer((PlayerDescription) currentElementDescription, currentTeam);
                elem.setPosition(time, pos, 0.0);
                elem.setOrientation(time, new Vector2D(1, 0), 0.0);
            }
            
            window.update();
        }
        return elem;
    }
    
    public boolean getRespectMaxNbOfPlayers()
    {
        return respectMaxNbOfPlayers;
    }
    
    public void setRespectMaxNbOfPlayers(boolean isRespected)
    {
        respectMaxNbOfPlayers = isRespected;
    }
    
    public int getNbOfPlayersInTeam(int team)
    {
        int result = 0;
        
        for (Element e : strategy.getAllElements())
        {
            if (e instanceof Player && ((Player) e).getTeam() == team)
            {
                result++;
            }
        }
        
        return result;
    }
    
    public int[] getTeams()
    {
        int[] result = new int[strategy.getSport().getMaxTeam()];
        
        for (int i = 0; i < result.length; i++)
        {
            result[i] = i + 1;
        }
        
        return result;
    }
    
    public int getMaxNbOfPlayers()
    {
        return strategy.getSport().getMaxPlayer();
    }
    
    public void deleteCurrentElement()
    {
        if (selectedElement != null)
        {
            strategy.deleteElement(selectedElement);
            selectedElement = null;
            window.update();
        }
    }
    
    public void selectElement(Element elem)
    {
        this.selectedElement = elem;
    }
    
    public void selectElementDescription(ElementDescription.TypeDescription type, String name)
    {
        switch (type)
        {
            case Player:
                this.currentElementDescription = getPlayerDescription(strategy.getSport().getName(), name);
                break;
            case Ball:
                this.currentElementDescription = getBallDescription(strategy.getSport().getName(), name);
                currentTeam = 0;
                break;
            case Obstacle:
                this.currentElementDescription = getObstacleDescription(strategy.getSport().getName(), name);
                currentTeam = 0;
                break;
        }
    }
    
    public void selectTeam(int team)
    {
        currentTeam = team;
    }
    
    public void setCurrentElemPosition(Vector2D pos)
    {
        if (this.selectedElement != null)
        {
            this.selectedElement.setPosition(this.time, pos, 0.0);
            window.update();
        }
    }
    
    public void setCurrentElemOrientation(Vector2D ori)
    {
        if (this.selectedElement != null)
        {
            this.selectedElement.setOrientation(time, ori, 0.0);
            window.update();
        }
    }
    
    public List<Element> getAllElements()
    {
        if (this.strategy != null)
        {
            return this.strategy.getAllElements();
        }
        
        return new ArrayList<>();
    }
    
    public Sport saveSport(String oldName, String newName, String courtImage, double courtHeight, double courtWidth, int playerNumber, int numTeams) throws ValidationException
    {
        Sport sport = null;
        if (oldName != null)
        {
            sport = sports.get(oldName);
        }
        
        if (sport != null)
        {
            sports.remove(oldName);
            
            sport.setName(newName);
            sport.setCourtImage(courtImage);
            sport.setCourtSize(new Vector2D(courtWidth, courtHeight));
            sport.setMaxPlayer(playerNumber);
            sport.setMaxTeam(numTeams);
            
            sports.put(newName, sport);
        }
        
        else
        {
            sport = new Sport(newName, courtImage, courtHeight, courtWidth, playerNumber, numTeams);
            sports.put(newName, sport);
        }
        
        return sport;
    }
    
    public Sport getSport(String name)
    {
        if (name == null)
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
        if (sportName != null)
        {
            sports.remove(sportName);
        }
    }
    
    public void createStrategy(String name, String sport) throws ValidationException
    {
        Sport s = getSport(sport);
        if (s == null)
        {
            throw new ValidationException("Sport invalide.");
        }
        
        Strategy strat = new Strategy(name, s);
        this.strategies.put(name, strat);
        loadStrategy(name);
    }
    
    public void loadStrategy(String name)
    {
        Strategy strat = getStrategy(name);
        
        if (strat != null)
        {
            this.strategy = strat;
        }
    }
    
    public Strategy getStrategy(String name)
    {
        if (name == null)
        {
            return null;
        }
        
        return strategies.get(name);
    }
    
    public List<Strategy> getStrategies()
    {
        return new ArrayList<Strategy>(strategies.values());
    }
    
    public void saveBallDescription(String sportName, String oldName, String newName, String image, double height, double width) throws ValidationException
    {
        Sport sport = getSport(sportName);
        if (sport != null)
        {
            BallDescription desc = null;
            
            if (oldName != null)
            {
                desc = sport.getBallDescription(oldName);
            }
            
            if (desc != null)
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
        if (sport != null)
        {
            if (name != null)
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
        if (sport != null)
        {
            if (name != null)
            {
                desc = sport.getBallDescription(name);
                sport.deleteElementDescription(desc);
            }
        }
    }
    
    public void savePlayerDescription(String sportName, String oldName, String newName, String image, double height, double width) throws ValidationException
    {
        Sport sport = getSport(sportName);
        if (sport != null)
        {
            PlayerDescription desc = null;
            
            if (oldName != null)
            {
                desc = sport.getPlayerDescription(oldName);
            }
            
            if (desc != null)
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
        if (sport != null)
        {
            if (name != null)
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
        if (sport != null)
        {
            if (name != null)
            {
                desc = sport.getPlayerDescription(name);
                sport.deleteElementDescription(desc);
            }
        }
    }
    
    public void saveObstacleDescription(String sportName, String oldName, String newName, String image, double height, double width) throws ValidationException
    {
        Sport sport = getSport(sportName);
        if (sport != null)
        {
            ObstacleDescription desc = null;
            
            if (oldName != null)
            {
                desc = sport.getObstacleDescription(oldName);
            }
            
            if (desc != null)
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
        if (sport != null)
        {
            if (name != null)
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
        if (sport != null)
        {
            if (name != null)
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
        }
        else
        {
            this.time = 0;
        }
        window.update();
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
    
    public List<ObstacleDescription> getAllObstacleDescriptions()
    {
        return this.strategy.getSport().getAllObstacleDescriptions();
    }
    
    public List<BallDescription> getAllBallDescriptions()
    {
        return this.strategy.getSport().getAllBallDescriptions();
    }
    
    public List<PlayerDescription> getAllPlayerDescriptions()
    {
        return this.strategy.getSport().getAllPlayerDescriptions();
    }
    
    public void setSelectedPlayerRole(String newElementDescription)
    {
        if (selectedElement instanceof Player)
        {
            PlayerDescription description = null;
            
            for (PlayerDescription playerDesc : this.strategy.getSport().getAllPlayerDescriptions())
            {
                if (playerDesc.getName().equals(newElementDescription))
                {
                    description = playerDesc;
                }
            }
            
            if (description != null)
            {
                ((Player) selectedElement).setPlayerDescription(description);
            }
        }
    }
    
    public void setSelectedPlayerName(String name)
    {
        if (selectedElement instanceof Player)
        {
            ((Player) selectedElement).setName(name);
            window.update();
        }
    }
    
    public void setSelectedPlayerTeam(int team)
    {
        if (selectedElement instanceof Player)
        {
            ((Player) selectedElement).setTeam(team);
        }
    }
    
    private class StrategyPlayer extends Task<Void>
    {
        private boolean playing;
        private double speed;
        
        public StrategyPlayer(double speed)
        {
            this.speed = speed;
        }
        
        @Override
        protected Void call() throws Exception
        {
            playing = true;
            long previousTimeMillis = System.currentTimeMillis();
            
            while (0 <= time && time <= strategy.getDuration())
            {
                previousTimeMillis = System.currentTimeMillis();
                Thread.sleep((long) (1000 / FPS_PLAY));
                if (!playing)
                {
                    break;
                }
                time += (double) (System.currentTimeMillis() - previousTimeMillis) / 1000 * speed;
                if (time > strategy.getDuration() || time < 0)
                {
                    break;
                }
                window.update();
                System.out.println(time);
            }

            // Arrondissement
            
            time = Math.round(((int) (time * FPS_PLAY)) / FPS_PLAY);
            sp = null;
            window.lastUpdate();
            System.out.println(time);
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
    
    public String getCourtImage()
    {
        return this.strategy.getSport().getCourtImage();
    }
    
    public Vector2D getCourtDimensions()
    {
        return this.strategy.getSport().getCourtSize();
    }
    
    public void setWindow(Updatable window)
    {
        this.window = window;
    }
    
    public void playStrategy(double speed)
    {
        if (sp == null)
        {
            sp = new StrategyPlayer(speed);
            Thread th = new Thread(sp);
            th.setDaemon(true);
            th.start();
        }
        else
        {
            sp.speed = speed;
            sp.play();
        }
    }
    
    public void pauseStrategy()
    {
        if (sp != null)
        {
            sp.pause();
        }
    }
    
    public int getMaxTeam()
    {
        return strategy.getSport().getMaxTeam();
    }
}
