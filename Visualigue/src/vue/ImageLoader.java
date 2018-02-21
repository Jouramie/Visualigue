package vue;

import javafx.scene.image.Image;

import java.util.HashMap;

public class ImageLoader {
    static private final HashMap<String, Image> images = new HashMap<>();

    static public Image getImage(String image) {
        Image result = images.get(image);
        if (result == null) {
            result = new Image(image);
            images.put(image, result);
        }

        return result;
    }
}
