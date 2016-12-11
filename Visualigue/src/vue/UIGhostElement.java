package vue;

import model.Element;
import model.MobileElement;

/**
 * @author Jérémie Bolduc
 */
public class UIGhostElement extends UIGeneralElement
{

    public UIGhostElement(Element element)
    {
        super(element);
    }

    @Override
    public void refreshNode()
    {
        super.refreshNode();
        node.setOpacity(0.5);
    }

    @Override
    public void update(double time)
    {
        double previousTime = time;
        if (element instanceof MobileElement)
        {
            MobileElement e = (MobileElement) element;
            previousTime = ((MobileElement) element).getPreviousKeyFrame(time);
        }
        super.update(previousTime);
    }
}
