package model;

import java.util.ArrayList;
import java.util.List;

public class Strategy
{
    private int  nbPlayer;
    private final String name;
    private List<Element> elements;
    private Sport sport;
    
    public Strategy(String name, Sport sport)
    {
        this.name = name;
        this.sport = sport;
        this.elements = new ArrayList<>();
        this.nbPlayer = 0;
    }
    
    public ObstacleElement createObstacle(ObstacleDescription desc)
    {
        ObstacleElement elem = new ObstacleElement(desc);
        elements.add(elem);
        return elem;
    }
    
    public Ball createBall(BallDescription desc)
    {
        Ball elem = new Ball(desc);
        elements.add(elem);
        return elem;
    }
     
    public Player createPlayer(PlayerDescription desc) throws Exception
    {
        /*if(this.nbPlayer >= this.sport.getMaxPlayer())
        {
            throw new Exception("Votre sport ne peut pas contenir plus de " + this.sport.getMaxPlayer() + " joueurs.");
        }*/
        
        Player elem = new Player(desc);
        elements.add(elem);
        this.nbPlayer++;
        return elem;
    }
    
    public void deleteElement(Element elem)
    {
        for(int i = 0; i < this.elements.size() ; i++)
        {
            Element e = this.elements.get(i);
            if(e == elem)
            {
                if(e instanceof Player)
                {
                    this.nbPlayer--;
                }
                
                this.elements.remove(i);
                
                return;
            }
        }
    }
    
    public double getDuration()
    {
        double result = 0.0;
        
        for(Element elem : this.elements)
        {
            double duration = elem.getTrajectoryDuration();
            if(duration > result)
            {
                result = duration;
            }
        }
        
        return result;
    }
    
    public List<Element> getAllElements()
    {
        return this.elements;
    }
    
    /*public Sport getSport()
    {
        return this.sport;
    }*/
    
    public int getNbPlayer()
    {
        int result = 0;
        
        for(Element elem : this.elements)
        {
            if(elem instanceof Player)
            {
                result++;
            }
        }
        
        return result;
    }
    
    public Sport getSport()
    {
        return this.sport;
    }
}
