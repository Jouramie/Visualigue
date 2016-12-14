package vue;

import java.util.HashMap;
import javafx.scene.image.Image;

/**
 * @author Jérémie Bolduc
 */
public class ImageLoader
{
    static private HashMap<String, Image> images = new HashMap();

    static public Image getImage(String image)
    {
        Image result = images.get(image);
        if (result == null)
        {
            result = new Image(image);
            images.put(image, result);
        }

        return result;
    }
}
