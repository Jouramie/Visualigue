package vue;

import model.Element;
import model.MobileElement;

public class UIGhostElement extends UIGeneralElement {

    public UIGhostElement(Element element) {
        super(element);
    }

    @Override
    public void refreshNode() {
        super.refreshNode();
        this.globalGroup.setOpacity(0.5);
    }

    @Override
    public void update(double time) {
        double previousTime = time;
        if (this.element instanceof MobileElement) {
            previousTime = ((MobileElement) this.element).getPreviousKeyFrame(time);
        }
        super.update(previousTime);
    }
}
