import model.Trajectory;
import model.Vector2D;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TrajectoryTest
{
    Trajectory t1;
    
    @Before
    public void beforePlaneteDoubleVecteur()
    {
        t1 = new Trajectory();
    }
    
    @Test
    public void testSetPosition()
    {
        t1.setPosition(0.0, 0, new Vector2D(14.0, 12.0));
    }
}
