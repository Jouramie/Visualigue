package controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import model.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GodController implements java.io.Serializable {

    public static final double FPS_EDIT = 2;
    public static final double FPS_PLAY = 10;
    private static transient GodController instance;
    private static transient ArrayList<ByteArrayOutputStream> stateList = new ArrayList();
    private static transient int currentState = -1;
    private final Map<String, Sport> sports;
    private final Map<String, Strategy> strategies;
    private Strategy strategy;
    private double time;
    private ElementDescription currentElementDescription;
    private int currentTeam;
    private Element selectedElement;
    private boolean respectMaxNbOfPlayers;
    private transient Recorder recorder;
    private transient Updatable window;
    private transient StrategyPlayer sp;

    public GodController() {
        this.sports = new TreeMap<>();
        this.strategies = new TreeMap<>();
        this.strategy = null;
        this.time = 0.0;
        this.currentElementDescription = null;
        this.currentTeam = 0;
        this.selectedElement = null;
        this.respectMaxNbOfPlayers = false;

        try {
            Sport sport = saveSport(null, "Hockey", "/res/hockey.png", 400, 1000, 5, 2);
            sport.addPlayerDescription(new PlayerDescription("Joueur", new Vector2D(60, 60), "/res/player.png"));
            sport.addBallDescription(new BallDescription("Balle", new Vector2D(20, 20), "/res/test.png"));
            sport.addObstacleDescription(new ObstacleDescription("Obstacle", new Vector2D(20, 20), "/res/cone.png"));

            createStrategy("Test", "Hockey");
        } catch (ValidationException ex) {
            Logger.getLogger(GodController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static GodController getInstance() {
        if (instance == null) {
            instance = new GodController();
            GodController.addState();
        }

        return instance;
    }

    public static void load(String path) {
        GodController result = null;

        File f = new File(path);
        if (f.exists() && !f.isDirectory()) {
            try {
                FileInputStream fileIn = new FileInputStream(path);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                result = (GodController) in.readObject();
                in.close();
                fileIn.close();
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }

        if (result != null) {
            result.setWindow(instance == null ? null : instance.window);
            GodController.instance = result;
            instance.time = 0;

            stateList.clear();
            currentState = -1;
            addState();
        }
    }

    public static void save(String path) {
        try {
            if (path == null || path.isEmpty()) {
                path = "visualigue.ser";
            }

            FileOutputStream fileOut = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(getInstance());
            out.close();
            fileOut.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void addState() {
        if (GodController.instance == null) {
            return;
        }

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(GodController.getInstance());
            out.close();
            bos.close();

            stateList = new ArrayList<>(stateList.subList(0, currentState + 1));
            stateList.add(bos);
            currentState = stateList.size() - 1;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static boolean canRedo() {
        return currentState < stateList.size() - 1;
    }

    public static void redo() {
        if (canRedo()) {
            currentState++;
            GodController result = null;

            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(stateList.get(currentState).toByteArray());
                ObjectInputStream in = new ObjectInputStream(bis);
                result = (GodController) in.readObject();
                in.close();
                bis.close();
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }

            if (result != null) {
                result.setWindow(getInstance().window);
                GodController.instance = result;
                result.window.update();
            }
        }
    }

    public static boolean canUndo() {
        return currentState > 0;
    }

    public static void undo() {
        if (canUndo()) {
            currentState--;
            GodController result = null;

            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(stateList.get(currentState).toByteArray());
                ObjectInputStream in = new ObjectInputStream(bis);
                result = (GodController) in.readObject();
                in.close();
                bis.close();
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }

            if (result != null) {
                result.setWindow(getInstance().window);
                GodController.instance = result;
                result.window.update();
            }
        }
    }

    public Strategy getCurrentStrategy() {
        return this.strategy;
    }

    public Element addElement(Vector2D pos) throws Exception {
        Element elem = null;

        if (this.currentElementDescription != null && isValidCoord(this.currentElementDescription, pos)) {
            if (this.currentElementDescription instanceof ObstacleDescription) {
                if (isObstacleNotInstersectingTrajectory((ObstacleDescription) this.currentElementDescription, pos)) {
                    elem = this.strategy.createObstacle((ObstacleDescription) this.currentElementDescription);
                    elem.setPosition(this.time, pos, 0.0);
                    elem.setOrientation(this.time, new Vector2D(1, 0), 0.0);
                }
            } else if (this.currentElementDescription instanceof BallDescription) {
                elem = this.strategy.createBall((BallDescription) this.currentElementDescription);
                elem.setPosition(this.time, pos, 0.0);
                elem.setOrientation(this.time, new Vector2D(1, 0), 0.0);
                ((Ball) elem).takeFromOwner(0);

                Player player = touchingPlayer((BallDescription) this.currentElementDescription, pos);
                if (player != null) {
                    player.giveBall(this.time, (Ball) elem);
                }
            } else if (this.currentElementDescription instanceof PlayerDescription && (!this.respectMaxNbOfPlayers || getNbOfPlayersInTeam(this.currentTeam) < getMaxNbOfPlayers())) {
                elem = this.strategy.createPlayer((PlayerDescription) this.currentElementDescription, this.currentTeam);
                elem.setPosition(this.time, pos, 0.0);
                elem.setOrientation(this.time, new Vector2D(1, 0), 0.0);
            }

            GodController.addState();
            this.window.update();
        }
        return elem;
    }

    public void beginRecording(MobileElement mobile) {
        try {
            this.recorder = new Recorder(mobile);
            Thread th = new Thread(this.recorder);
            th.setDaemon(true);
            th.start();
        } catch (Exception ignored) {
        }
    }

    public void stopRecording() {
        this.recorder.stopRecording();
        this.recorder = null;
        //time = Math.ceil(time * FPS_EDIT) / FPS_EDIT;
        //selectedElement.setPosition(time, selectedElement.getPosition(time), 1.0/FPS_PLAY);
        this.window.update();
    }

    public boolean getRespectMaxNbOfPlayers() {
        return this.respectMaxNbOfPlayers;
    }

    public void setRespectMaxNbOfPlayers(boolean isRespected) {
        this.respectMaxNbOfPlayers = isRespected;
        GodController.addState();
    }

    public int getNbOfPlayersInTeam(int team) {
        int result = 0;

        for (Element e : this.strategy.getAllElements()) {
            if (e instanceof Player && ((Player) e).getTeam() == team) {
                result++;
            }
        }

        return result;
    }

    public int[] getTeams() {
        int[] result = new int[this.strategy.getSport().getMaxTeam()];

        for (int i = 0; i < result.length; i++) {
            result[i] = i + 1;
        }

        return result;
    }

    public int getMaxNbOfPlayers() {
        return this.strategy.getSport().getMaxPlayer();
    }

    public void deleteCurrentElement() {
        if (this.selectedElement != null) {
            if (this.selectedElement instanceof Player) {
                deletePlayer((Player) this.selectedElement);
            } else if (this.selectedElement instanceof Ball) {
                deleteBall((Ball) this.selectedElement);
            }

            this.strategy.deleteElement(this.selectedElement);
            this.selectedElement = null;

            GodController.addState();
            this.window.update();
        }
    }

    private void deletePlayer(Player player) {
        for (Element elem : this.strategy.getAllElements()) {
            if (elem instanceof Ball) {
                ((Ball) elem).deletePlayer(player);
            }
        }
    }

    private void deleteBall(Ball ball) {
        for (Element elem : this.strategy.getAllElements()) {
            if (elem instanceof Player) {
                ((Player) elem).deleteBall(ball);
            }
        }
    }

    public void selectElement(Element elem) {
        this.selectedElement = elem;
        this.window.update();
    }

    public Element getSelectedElement() {
        return this.selectedElement;
    }

    public void selectElementDescription(ElementDescription.TypeDescription type, String name) {
        switch (type) {
            case Player:
                this.currentElementDescription = getPlayerDescription(this.strategy.getSport().getName(), name);
                break;
            case Ball:
                this.currentElementDescription = getBallDescription(this.strategy.getSport().getName(), name);
                this.currentTeam = 0;
                break;
            case Obstacle:
                this.currentElementDescription = getObstacleDescription(this.strategy.getSport().getName(), name);
                this.currentTeam = 0;
                break;
        }
    }

    public void selectTeam(int team) {
        this.currentTeam = team;
    }

    public void setCurrentElemPosition(Vector2D pos) {
        if (this.selectedElement instanceof Ball) {
            for (Element elem : this.strategy.getAllElements()) {
                if (elem instanceof Player && ((Player) elem).getBall(this.time) == this.selectedElement) {
                    Player player = (Player) elem;
                    player.giveBall(this.time - 1.0 / FPS_EDIT, null);
                }
            }

            Player player = touchingPlayer((BallDescription) this.selectedElement.getElementDescription(), pos);
            if (player != null) {
                player.giveBall(this.time, (Ball) this.selectedElement);
                this.window.update();
                return;
            }

        }

        if (this.selectedElement != null && isValidCoord(this.selectedElement.getElementDescription(), pos)) {
            if (this.selectedElement instanceof MobileElement) {
                if (!isInterpolationValid(pos)) {
                    this.window.update();
                    return;
                }
            } else {
                if (!isObstacleNotInstersectingTrajectory((ObstacleDescription) this.selectedElement.getElementDescription(), pos)) {
                    this.window.update();
                    return;
                }
            }

            this.selectedElement.setPosition(this.time, pos, 1.0 / FPS_EDIT);

            GodController.addState();
        }
        this.window.update();
    }

    public boolean isInterpolationValid(Vector2D newPos) {
        if (this.selectedElement != null && this.selectedElement instanceof MobileElement) {
            for (Element obstacle : getAllElements()) {
                if (obstacle != this.selectedElement && obstacle instanceof ObstacleElement) {
                    int NB_OF_VERIFICATIONS = 30;
                    MobileElement elem = (MobileElement) this.selectedElement;
                    Vector2D elemSize = elem.getElementDescription().getSize();
                    Vector2D obstaclePosition = obstacle.getPosition(this.time);
                    Vector2D obstacleSize = obstacle.getElementDescription().getSize();
                    Vector2D pos;
                    Vector2D dl;
                    java.awt.geom.Rectangle2D.Double rect1 = new java.awt.geom.Rectangle2D.Double(obstaclePosition.getX() - obstacleSize.getX() / 2, obstaclePosition.getY() - obstacleSize.getY() / 2, obstacleSize.getX(), obstacleSize.getY());
                    java.awt.geom.Rectangle2D.Double rect2 = new java.awt.geom.Rectangle2D.Double();

                    double previousTime = elem.getPreviousKeyFrame(this.time);
                    if (previousTime != this.time) {
                        pos = elem.getPosition(previousTime);
                        dl = newPos.substract(pos);
                        dl.setLength(dl.getLength() / NB_OF_VERIFICATIONS);

                        for (int i = 0; i < NB_OF_VERIFICATIONS; i++) {
                            rect2.setRect(pos.getX() - elemSize.getX() / 2, pos.getY() - elemSize.getY() / 2, elemSize.getX(), elemSize.getY());

                            if (rect1.intersects(rect2)) {
                                return false;
                            }

                            pos = pos.add(dl);
                        }
                    }

                    double nextTime = elem.getNextKeyFrame(this.time);
                    if (nextTime != this.time) {
                        pos = elem.getPosition(nextTime);
                        dl = newPos.substract(pos);
                        dl.setLength(dl.getLength() / NB_OF_VERIFICATIONS);

                        for (int i = 0; i < NB_OF_VERIFICATIONS; i++) {
                            rect2.setRect(pos.getX() - elemSize.getX() / 2, pos.getY() - elemSize.getY() / 2, elemSize.getX(), elemSize.getY());

                            if (rect1.intersects(rect2)) {
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

    private Player touchingPlayer(BallDescription ballDescription, Vector2D pos) {
        Vector2D ballSize = ballDescription.getSize();
        Player player = null;

        for (Element elem : this.strategy.getAllElements()) {
            if (elem instanceof Player) {
                Vector2D elemSize = elem.getElementDescription().getSize();

                if (pos.getX() + ballSize.getX() / 2 >= elem.getPosition(this.time).getX() - elemSize.getX() / 2 && pos.getX() - ballSize.getX() / 2 <= elem.getPosition(this.time).getX() + elemSize.getX() / 2) {
                    if (pos.getY() + ballSize.getY() / 2 >= elem.getPosition(this.time).getY() - elemSize.getY() / 2 && pos.getY() - ballSize.getY() / 2 <= elem.getPosition(this.time).getY() + elemSize.getY() / 2) {
                        player = (Player) elem;
                    }
                }
            }
        }
        return player;
    }

    public boolean isObstacleNotInstersectingTrajectory(ObstacleDescription obstacleDescription, Vector2D newPos) {
        for (Element element : getAllElements()) {
            if (element instanceof MobileElement) {
                double NB_OF_VERIFICATIONS_BY_SECOND = 15;
                MobileElement mobile = (MobileElement) element;
                Vector2D mobileSize = mobile.getElementDescription().getSize();
                Vector2D obstacleSize = obstacleDescription.getSize();

                double verificationTime = 0;
                double maxVerfificationTime = (int) (this.strategy.getDuration() * NB_OF_VERIFICATIONS_BY_SECOND);

                java.awt.geom.Rectangle2D.Double rect1 = new java.awt.geom.Rectangle2D.Double(newPos.getX() - obstacleSize.getX() / 2, newPos.getY() - obstacleSize.getY() / 2, obstacleSize.getX(), obstacleSize.getY());
                java.awt.geom.Rectangle2D.Double rect2 = new java.awt.geom.Rectangle2D.Double();

                while (verificationTime <= maxVerfificationTime) {
                    rect2.setRect(mobile.getPosition(verificationTime).getX() - mobileSize.getX() / 2, mobile.getPosition(verificationTime).getY() - mobileSize.getY() / 2, mobileSize.getX(), mobileSize.getY());

                    if (rect1.intersects(rect2)) {
                        return false;
                    }

                    verificationTime += (1.0 / NB_OF_VERIFICATIONS_BY_SECOND);
                }
            }
        }

        return true;
    }

    public boolean isValidCoord(ElementDescription elementDescription, Vector2D pos) {
        Vector2D elementSize = elementDescription.getSize();

        for (Element e : getAllElements()) {
            if (e != this.selectedElement && e instanceof ObstacleElement) {
                Vector2D obstacleSize = e.getElementDescription().getSize();

                if (pos.getX() + elementSize.getX() / 2 >= e.getPosition(this.time).getX() - obstacleSize.getX() / 2 && pos.getX() - elementSize.getX() / 2 <= e.getPosition(this.time).getX() + obstacleSize.getX() / 2) {
                    if (pos.getY() + elementSize.getY() / 2 >= e.getPosition(this.time).getY() - obstacleSize.getY() / 2 && pos.getY() - elementSize.getY() / 2 <= e.getPosition(this.time).getY() + obstacleSize.getY() / 2) {
                        return false;
                    }
                }
            }
        }

        Vector2D courtSize = this.strategy.getSport().getCourtSize();

        if (pos.getX() + elementSize.getX() / 2 > courtSize.getX()) {
            return false;
        } else if (pos.getX() - elementSize.getX() / 2 < 0) {
            return false;
        } else if (pos.getY() + elementSize.getY() / 2 > courtSize.getY()) {
            return false;
        } else if (pos.getY() - elementSize.getY() / 2 < 0) {
            return false;
        }

        return true;
    }

    public void setCurrentElemOrientation(Vector2D ori) {
        if (this.selectedElement != null) {
            this.selectedElement.setOrientation(this.time, ori, 0.0);

            GodController.addState();
            this.window.update();
        }
    }

    public List<Element> getAllElements() {
        if (this.strategy != null) {
            return this.strategy.getAllElements();
        }

        return new ArrayList<>();
    }

    public Sport saveSport(String oldName, String newName, String courtImage, double courtHeight, double courtWidth, int playerNumber, int numTeams) throws ValidationException {
        Sport sport = null;
        if (oldName != null) {
            sport = this.sports.get(oldName);
        }

        if (sport != null) {
            this.sports.remove(oldName);

            sport.setName(newName);
            sport.setCourtImage(courtImage);
            sport.setCourtSize(new Vector2D(courtWidth, courtHeight));
            sport.setMaxPlayer(playerNumber);
            sport.setMaxTeam(numTeams);

            this.sports.put(newName, sport);
        } else {
            sport = new Sport(newName, courtImage, courtHeight, courtWidth, playerNumber, numTeams);
            this.sports.put(newName, sport);
        }

        GodController.addState();

        return sport;
    }

    public Sport getSport(String name) {
        if (name == null) {
            return null;
        }

        return this.sports.get(name);
    }

    public List<Sport> getSports() {
        return new ArrayList<>(this.sports.values());
    }

    public void deleteSport(String sportName) {
        if (sportName != null) {
            this.sports.remove(sportName);
            GodController.addState();
        }
    }

    public void createStrategy(String name, String sport) throws ValidationException {
        Sport s = getSport(sport);
        if (s == null) {
            throw new ValidationException("Sport invalide.");
        }

        Strategy strat = new Strategy(name, s);
        this.strategies.put(name, strat);
        loadStrategy(name);
    }

    public void loadStrategy(String name) {
        Strategy strat = getStrategy(name);

        if (strat != null) {
            this.strategy = strat;
        }
        this.time = 0;
        this.selectedElement = null;
    }

    public Strategy getStrategy(String name) {
        if (name == null) {
            return null;
        }

        return this.strategies.get(name);
    }

    public void deleteStrategy(String name) {
        if (name == null) {
            return;
        }

        Strategy s = this.strategies.get(name);
        if (s != null && this.strategies.size() > 1) {
            this.strategies.remove(name);
        }
    }

    public List<Strategy> getStrategies() {
        return new ArrayList<>(this.strategies.values());
    }

    public void saveBallDescription(String sportName, String oldName, String newName, String image, double height, double width) throws ValidationException {
        Sport sport = getSport(sportName);
        if (sport != null) {
            BallDescription desc = null;

            if (oldName != null) {
                desc = sport.getBallDescription(oldName);
            }

            if (desc != null) {
                desc.setName(newName);
                desc.setImage(image);
                desc.setSize(new Vector2D(width, height));
            } else {
                desc = new BallDescription(newName, new Vector2D(width, height), image);
                sport.addBallDescription(desc);
            }

            GodController.addState();
        }
    }

    public BallDescription getBallDescription(String sportName, String name) {
        BallDescription desc = null;
        Sport sport = getSport(sportName);
        if (sport != null) {
            if (name != null) {
                desc = sport.getBallDescription(name);
            }
        }

        return desc;
    }

    public void deleteBallDescription(String sportName, String name) {
        BallDescription desc;
        Sport sport = getSport(sportName);
        if (sport != null) {
            if (name != null) {
                desc = sport.getBallDescription(name);
                sport.deleteElementDescription(desc);

                GodController.addState();
            }
        }
    }

    public void savePlayerDescription(String sportName, String oldName, String newName, String image, double height, double width) throws ValidationException {
        Sport sport = getSport(sportName);
        if (sport != null) {
            PlayerDescription desc = null;

            if (oldName != null) {
                desc = sport.getPlayerDescription(oldName);
            }

            if (desc != null) {
                desc.setName(newName);
                desc.setImage(image);
                desc.setSize(new Vector2D(width, height));
            } else {
                desc = new PlayerDescription(newName, new Vector2D(width, height), image);
                sport.addPlayerDescription(desc);
            }

            GodController.addState();
        }
    }

    public PlayerDescription getPlayerDescription(String sportName, String name) {
        PlayerDescription desc = null;
        Sport sport = getSport(sportName);
        if (sport != null) {
            if (name != null) {
                desc = sport.getPlayerDescription(name);
            }
        }

        return desc;
    }

    public void deletePlayerDescription(String sportName, String name) {
        PlayerDescription desc;
        Sport sport = getSport(sportName);
        if (sport != null) {
            if (name != null) {
                desc = sport.getPlayerDescription(name);
                sport.deleteElementDescription(desc);

                GodController.addState();
            }
        }
    }

    public void saveObstacleDescription(String sportName, String oldName, String newName, String image, double height, double width) throws ValidationException {
        Sport sport = getSport(sportName);
        if (sport != null) {
            ObstacleDescription desc = null;

            if (oldName != null) {
                desc = sport.getObstacleDescription(oldName);
            }

            if (desc != null) {
                desc.setName(newName);
                desc.setImage(image);
                desc.setSize(new Vector2D(width, height));
            } else {
                desc = new ObstacleDescription(newName, new Vector2D(width, height), image);
                sport.addObstacleDescription(desc);
            }

            GodController.addState();
        }
    }

    public ObstacleDescription getObstacleDescription(String sportName, String name) {
        ObstacleDescription desc = null;
        Sport sport = getSport(sportName);
        if (sport != null) {
            if (name != null) {
                desc = sport.getObstacleDescription(name);
            }
        }

        return desc;
    }

    public void deleteObstacleDescription(String sportName, String name) {
        ObstacleDescription desc;
        Sport sport = getSport(sportName);
        if (sport != null) {
            if (name != null) {
                desc = sport.getObstacleDescription(name);
                sport.deleteElementDescription(desc);

                GodController.addState();
            }
        }
    }

    public double getCurrentTime() {
        return this.time;
    }

    public void setCurrentTime(double time) {
        if (time >= 0) {
            this.time = time;
        } else {
            this.time = 0;
        }

        GodController.addState();
        if (this.window != null) {
            this.window.update();
        }
    }

    public void nextFrame() {
        setCurrentTime(this.time + 1);
    }

    public void prevFrame() {
        setCurrentTime(this.time - 1);
    }

    public double getDuration() {
        return this.strategy.getDuration();
    }

    public List<ObstacleDescription> getAllObstacleDescriptions() {
        return this.strategy.getSport().getAllObstacleDescriptions();
    }

    public List<BallDescription> getAllBallDescriptions() {
        return this.strategy.getSport().getAllBallDescriptions();
    }

    public List<PlayerDescription> getAllPlayerDescriptions() {
        return this.strategy.getSport().getAllPlayerDescriptions();
    }

    public void setSelectedPlayerRole(String newElementDescription) {
        if (this.selectedElement instanceof Player) {
            PlayerDescription description = null;

            for (PlayerDescription playerDesc : this.strategy.getSport().getAllPlayerDescriptions()) {
                if (playerDesc.getName().equals(newElementDescription)) {
                    description = playerDesc;
                }
            }

            if (description != null) {
                ((Player) this.selectedElement).setPlayerDescription(description);
                this.window.update();
                GodController.addState();
            }
        }
    }

    public void setSelectedPlayerName(String name) {
        if (this.selectedElement instanceof Player) {
            ((Player) this.selectedElement).setName(name);

            GodController.addState();
            this.window.update();
        }
    }

    public void setSelectedPlayerTeam(int team) {
        if (this.selectedElement instanceof Player) {
            ((Player) this.selectedElement).setTeam(team);
            GodController.addState();
            this.window.update();
        }
    }

    public String getCourtImage() {
        return this.strategy.getSport().getCourtImage();
    }

    public Vector2D getCourtDimensions() {
        return this.strategy.getSport().getCourtSize();
    }

    public void setWindow(Updatable window) {
        this.window = window;
    }

    public void playStrategy(double speed) {
        if (this.sp == null) {
            this.sp = new StrategyPlayer(speed);
            Thread th = new Thread(this.sp);
            th.setDaemon(true);
            th.start();
        } else {
            this.sp.speed = speed;
            this.sp.play();
        }
    }

    public void pauseStrategy() {
        if (this.sp != null) {
            this.sp.pause();
        }
    }

    public int getMaxTeam() {
        return this.strategy.getSport().getMaxTeam();
    }

    private class StrategyPlayer extends Task<Void> {

        private boolean playing;
        private double speed;

        public StrategyPlayer(double speed) {
            this.speed = speed;
        }

        @Override
        protected Void call() throws Exception {
            this.playing = true;
            long previousTimeMillis;

            while (0 <= GodController.this.time && GodController.this.time <= GodController.this.strategy.getDuration()) {
                previousTimeMillis = System.currentTimeMillis();
                Thread.sleep((long) (1000 / FPS_PLAY));
                if (!this.playing) {
                    break;
                }
                GodController.this.time += (double) (System.currentTimeMillis() - previousTimeMillis) / 1000 * this.speed;
                if (GodController.this.time > GodController.this.strategy.getDuration() || GodController.this.time < 0) {
                    break;
                }
                Platform.runLater(() -> GodController.this.window.update());
            }

            // Arrondissement
            GodController.this.time = Math.round(GodController.this.time * FPS_EDIT) / FPS_EDIT;
            GodController.this.sp = null;
            Platform.runLater(() ->
            {
                GodController.this.window.lastUpdate();
            });
            return null;
        }

        public void play() {
            this.playing = true;
        }

        public void pause() {
            this.playing = false;
        }
    }

    private class Recorder extends Task<Void> {

        private final MobileElement mobile;
        private long previousTime;
        private long currentTime;
        private boolean running;

        Recorder(MobileElement mobile) {
            this.mobile = mobile;
        }

        @Override
        protected Void call() throws Exception {
            this.currentTime = System.currentTimeMillis();
            this.running = true;
            boolean firstTime = true;

            while (this.running) {
                this.previousTime = this.currentTime;
                this.currentTime = System.currentTimeMillis();
                double dt = (this.currentTime - this.previousTime) / 1000.0;

                if (!firstTime) {
                    Platform.runLater(() ->
                    {
                        GodController.this.time += dt;
                        Vector2D pos = GodController.this.window.updateOnRecord(this.mobile);

                        if (isValidCoord(GodController.this.currentElementDescription, pos)) {
                            GodController.this.selectedElement.setOrientation(GodController.this.time - dt, pos.substract(GodController.this.selectedElement.getPosition(GodController.this.time)).normaliser(), dt);
                            GodController.this.selectedElement.setPosition(GodController.this.time, pos, dt);
                        }
                    });
                } else {
                    Platform.runLater(() ->
                    {
                        GodController.this.time += dt;
                        Vector2D pos = GodController.this.window.updateOnRecord(this.mobile);

                        if (isValidCoord(GodController.this.currentElementDescription, pos)) {
                            GodController.this.selectedElement.setPosition(GodController.this.time, pos, dt);
                        }
                    });
                }

                Thread.sleep((long) (1000.0 / FPS_PLAY));
                firstTime = false;
            }

            Platform.runLater(() ->
                    GodController.this.selectedElement.setPosition(GodController.this.time, GodController.this.selectedElement.getPosition(GodController.this.time), 1.0 / FPS_EDIT));

            GodController.addState();
            return null;
        }

        public void stopRecording() {
            this.running = false;
        }
    }
}
