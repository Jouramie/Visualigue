package controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import model.BallDescription;
import model.Element;
import model.ElementDescription;
import model.MobileElement;
import model.ObstacleDescription;
import model.ObstacleElement;
import model.Player;
import model.PlayerDescription;
import model.Sport;
import model.Strategy;
import model.ValidationException;
import model.Vector2D;

public class GodController implements java.io.Serializable
{

    public static final double FPS_EDIT = 2;
    public static final double FPS_PLAY = 10;

    private Map<String, Sport> sports;
    private Map<String, Strategy> strategies;
    private Strategy strategy;
    private transient double time;
    private ElementDescription currentElementDescription;
    private int currentTeam;
    private Element selectedElement;

    private boolean respectMaxNbOfPlayers;
    private transient Recorder recorder;
    private transient Updatable window;
    private transient StrategyPlayer sp;
    private static transient GodController instance;
    private static transient ArrayList<ByteArrayOutputStream> stateList = new ArrayList();
    private static transient int currentState = -1;

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

    public static GodController getInstance()
    {
        if (instance == null)
        {
            instance = new GodController();
            GodController.addState();
        }

        return instance;
    }

    public static void load(String path)
    {
        GodController result = null;

        File f = new File(path);
        if (f.exists() && !f.isDirectory())
        {
            try
            {
                FileInputStream fileIn = new FileInputStream(path);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                result = (GodController) in.readObject();
                in.close();
                fileIn.close();
            } catch (IOException | ClassNotFoundException ex)
            {
                ex.printStackTrace();
            }
        }

        if (result != null)
        {
            result.setWindow(getInstance().window);
            GodController.instance = result;
            instance.time = 0;

            stateList.clear();
            currentState = -1;
            addState();
        }
    }

    public static void save(String path)
    {
        try
        {
            if (path == null || path.isEmpty())
            {
                path = "visualigue.ser";
            }

            FileOutputStream fileOut = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(getInstance());
            out.close();
            fileOut.close();
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    public static void addState()
    {
        if (GodController.instance == null)
        {
            return;
        }

        try
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(GodController.getInstance());
            out.close();
            bos.close();

            stateList = new ArrayList(stateList.subList(0, currentState + 1));
            stateList.add(bos);
            currentState = stateList.size() - 1;
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    public static boolean canRedo()
    {
        return currentState < stateList.size() - 1;
    }

    public static void redo()
    {
        if (canRedo())
        {
            currentState++;
            GodController result = null;

            try
            {
                ByteArrayInputStream bis = new ByteArrayInputStream(stateList.get(currentState).toByteArray());
                ObjectInputStream in = new ObjectInputStream(bis);
                result = (GodController) in.readObject();
                in.close();
                bis.close();
            } catch (IOException | ClassNotFoundException ex)
            {
                ex.printStackTrace();
            }

            if (result != null)
            {
                result.setWindow(getInstance().window);
                GodController.instance = result;
                result.window.update();
            }
        }
    }

    public static boolean canUndo()
    {
        return currentState > 0;
    }

    public static void undo()
    {
        if (canUndo())
        {
            currentState--;
            GodController result = null;

            try
            {
                ByteArrayInputStream bis = new ByteArrayInputStream(stateList.get(currentState).toByteArray());
                ObjectInputStream in = new ObjectInputStream(bis);
                result = (GodController) in.readObject();
                in.close();
                bis.close();
            } catch (IOException | ClassNotFoundException ex)
            {
                ex.printStackTrace();
            }

            if (result != null)
            {
                result.setWindow(getInstance().window);
                GodController.instance = result;
                result.window.update();
            }
        }
    }
    
    public Strategy getCurrentStrategy()
    {
        return this.strategy;
    }

    public Element addElement(Vector2D pos) throws Exception
    {
        Element elem = null;

        if (currentElementDescription != null && isValidCoord(currentElementDescription, pos))
        {
            if (currentElementDescription instanceof ObstacleDescription)
            {
                if (isObstacleNotInstersectingTrajectory((ObstacleDescription) currentElementDescription, pos))
                {
                    elem = this.strategy.createObstacle((ObstacleDescription) currentElementDescription);
                    elem.setPosition(time, pos, 0.0);
                    elem.setOrientation(time, new Vector2D(1, 0), 0.0);
                }
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

            GodController.addState();
            window.update();
        }
        return elem;
    }
    
    public void beginRecording(MobileElement mobile)
    {
        try
        {
            recorder = new Recorder(mobile);
            Thread th = new Thread(recorder);
            th.setDaemon(true);
            th.start();
        }
        catch(Exception e)
        {
        }
    }
    
    public void stopRecording()
    {
        recorder.stopRecording();
        recorder = null;
    }
    
    public boolean getRespectMaxNbOfPlayers()
    {
        return respectMaxNbOfPlayers;
    }

    public void setRespectMaxNbOfPlayers(boolean isRespected)
    {
        respectMaxNbOfPlayers = isRespected;
        GodController.addState();
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

            GodController.addState();
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
        if (this.selectedElement != null && isValidCoord(selectedElement.getElementDescription(), pos))
        {
            if (selectedElement instanceof MobileElement)
            {
                if (!isInterpolationValid(pos))
                {
                    window.update();
                    return;
                }
            }
            else
            {
                if (!isObstacleNotInstersectingTrajectory((ObstacleDescription) selectedElement.getElementDescription(), pos))
                {
                    window.update();
                    return;
                }
            }

            this.selectedElement.setPosition(this.time, pos, 1.0/FPS_EDIT);

            GodController.addState();
            window.update();
        }
        window.update();
    }

    public boolean isInterpolationValid(Vector2D newPos)
    {
        if (selectedElement != null && selectedElement instanceof MobileElement)
        {
            for (Element obstacle : getAllElements())
            {
                if (obstacle != selectedElement && obstacle instanceof ObstacleElement)
                {
                    final int NB_OF_VERIFICATIONS = 30;
                    MobileElement elem = (MobileElement) selectedElement;
                    Vector2D elemSize = elem.getElementDescription().getSize();
                    Vector2D obstaclePosition = obstacle.getPosition(time);
                    Vector2D obstacleSize = obstacle.getElementDescription().getSize();
                    Vector2D pos;
                    Vector2D dl;
                    java.awt.geom.Rectangle2D.Double rect1 = new java.awt.geom.Rectangle2D.Double(obstaclePosition.getX() - obstacleSize.getX() / 2, obstaclePosition.getY() - obstacleSize.getY() / 2, obstacleSize.getX(), obstacleSize.getY());
                    java.awt.geom.Rectangle2D.Double rect2 = new java.awt.geom.Rectangle2D.Double();
                    
                    double previousTime = elem.getPreviousKeyFrame(time);
                    if(previousTime != time)
                    {
                        pos = elem.getPosition(previousTime);
                        dl = newPos.substract(pos);
                        dl.setLength(dl.getLength() / NB_OF_VERIFICATIONS);

                        for (int i = 0; i < NB_OF_VERIFICATIONS; i++)
                        {
                            rect2.setRect(pos.getX() - elemSize.getX() / 2, pos.getY() - elemSize.getY() / 2, elemSize.getX(), elemSize.getY());

                            if (rect1.intersects(rect2))
                            {
                                return false;
                            }

                            pos = pos.add(dl);
                        }
                    }

                    double nextTime = elem.getNextKeyFrame(time);
                    if(nextTime != time)
                    {
                        pos = elem.getPosition(nextTime);
                        dl = newPos.substract(pos);
                        dl.setLength(dl.getLength() / NB_OF_VERIFICATIONS);

                        for (int i = 0; i < NB_OF_VERIFICATIONS; i++)
                        {
                            rect2.setRect(pos.getX() - elemSize.getX() / 2, pos.getY() - elemSize.getY() / 2, elemSize.getX(), elemSize.getY());

                            if (rect1.intersects(rect2))
                            {
                                return false;
                            }

                            pos = pos.add(dl);
                        }
                    }
                }
            }
        }

        return true;
    }

    public boolean isObstacleNotInstersectingTrajectory(ObstacleDescription obstacleDescription, Vector2D newPos)
    {
        for (Element element : getAllElements())
        {
            if (element instanceof MobileElement)
            {
                final double NB_OF_VERIFICATIONS_BY_SECOND = 15;
                MobileElement mobile = (MobileElement) element;
                Vector2D mobileSize = mobile.getElementDescription().getSize();
                Vector2D obstacleSize = obstacleDescription.getSize();

                double verificationTime = 0;
                double maxVerfificationTime = (int) (strategy.getDuration() * NB_OF_VERIFICATIONS_BY_SECOND);

                java.awt.geom.Rectangle2D.Double rect1 = new java.awt.geom.Rectangle2D.Double(newPos.getX() - obstacleSize.getX() / 2, newPos.getY() - obstacleSize.getY() / 2, obstacleSize.getX(), obstacleSize.getY());
                java.awt.geom.Rectangle2D.Double rect2 = new java.awt.geom.Rectangle2D.Double();

                while (verificationTime <= maxVerfificationTime)
                {
                    rect2.setRect(mobile.getPosition(verificationTime).getX() - mobileSize.getX() / 2, mobile.getPosition(verificationTime).getY() - mobileSize.getY() / 2, mobileSize.getX(), mobileSize.getY());

                    if (rect1.intersects(rect2))
                    {
                        return false;
                    }

                    verificationTime += (1.0 / NB_OF_VERIFICATIONS_BY_SECOND);
                }
            }
        }

        return true;
    }

    public boolean isValidCoord(ElementDescription elementDescription, Vector2D pos)
    {
        Vector2D elementSize = elementDescription.getSize();

        for (Element e : getAllElements())
        {
            if (e != selectedElement && e instanceof ObstacleElement)
            {
                Vector2D obstacleSize = e.getElementDescription().getSize();

                if (pos.getX() + elementSize.getX() / 2 >= e.getPosition(time).getX() - obstacleSize.getX() / 2 && pos.getX() - elementSize.getX() / 2 <= e.getPosition(time).getX() + obstacleSize.getX() / 2)
                {
                    if (pos.getY() + elementSize.getY() / 2 >= e.getPosition(time).getY() - obstacleSize.getY() / 2 && pos.getY() - elementSize.getY() / 2 <= e.getPosition(time).getY() + obstacleSize.getY() / 2)
                    {
                        return false;
                    }
                }
            }
        }

        Vector2D courtSize = this.strategy.getSport().getCourtSize();

        if (pos.getX() + elementSize.getX() / 2 > courtSize.getX())
        {
            return false;
        }
        if (pos.getX() - elementSize.getX() / 2 < 0)
        {
            return false;
        }
        if (pos.getY() + elementSize.getY() / 2 > courtSize.getY())
        {
            return false;
        }
        if (pos.getY() - elementSize.getY() / 2 < 0)
        {
            return false;
        }

        return true;
    }

    public void setCurrentElemOrientation(Vector2D ori)
    {
        if (this.selectedElement != null)
        {
            this.selectedElement.setOrientation(time, ori, 0.0);

            GodController.addState();
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

        GodController.addState();

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
        return new ArrayList<>(sports.values());
    }

    public void deleteSport(String sportName)
    {
        if (sportName != null)
        {
            sports.remove(sportName);
            GodController.addState();
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
        return new ArrayList<>(strategies.values());
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

            GodController.addState();
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
        BallDescription desc;
        Sport sport = getSport(sportName);
        if (sport != null)
        {
            if (name != null)
            {
                desc = sport.getBallDescription(name);
                sport.deleteElementDescription(desc);

                GodController.addState();
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

            GodController.addState();
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
        PlayerDescription desc;
        Sport sport = getSport(sportName);
        if (sport != null)
        {
            if (name != null)
            {
                desc = sport.getPlayerDescription(name);
                sport.deleteElementDescription(desc);

                GodController.addState();
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

            GodController.addState();
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
        ObstacleDescription desc;
        Sport sport = getSport(sportName);
        if (sport != null)
        {
            if (name != null)
            {
                desc = sport.getObstacleDescription(name);
                sport.deleteElementDescription(desc);

                GodController.addState();
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

        GodController.addState();
        if (window != null)
        {
            window.update();
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
                window.update();
                GodController.addState();
            }
        }
    }

    public void setSelectedPlayerName(String name)
    {
        if (selectedElement instanceof Player)
        {
            ((Player) selectedElement).setName(name);

            GodController.addState();
            window.update();
        }
    }

    public void setSelectedPlayerTeam(int team)
    {
        if (selectedElement instanceof Player)
        {
            ((Player) selectedElement).setTeam(team);
            GodController.addState();
            window.update();
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
            long previousTimeMillis;

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
                Platform.runLater(() -> {
                    window.update();
                });
            }

            // Arrondissement
            time = Math.round(((int) (time * FPS_PLAY)) / FPS_PLAY);
            sp = null;
            window.lastUpdate();
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
    
    private class Recorder extends Task<Void>
    {
            private long previousTime;
            private long currentTime;
            private MobileElement mobile;
            private boolean running;
            
            Recorder(MobileElement mobile)
            {
                this.mobile = mobile;
            }

            @Override
            protected Void call() throws Exception
            {
                currentTime = System.currentTimeMillis();
                this.running = true;

                while(this.running)
                {
                    previousTime = currentTime;
                    currentTime = System.currentTimeMillis();
                    double dt = (currentTime - previousTime)/1000.0;

                    Platform.runLater(() -> {
                        time += dt;
                        Vector2D pos = window.updateOnRecord(this.mobile);
                        if(isValidCoord(currentElementDescription, pos))
                        {
                            selectedElement.setPosition(time, pos, dt);
                        }
                    });

                    Thread.sleep((long)(1000.0 / FPS_PLAY));
                }
                
                GodController.addState();
                return null;
            }
            
            public void stopRecording()
            {
                this.running = false;
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
