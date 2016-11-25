package model;

public abstract class ElementDescription implements java.io.Serializable
{
    public enum TypeDescription
    {
        Player,
        Ball,
        Obstacle;
        
        @Override
        public String toString()
        {
            switch(this)
            {
                case Player:
                    return "joueur";
                case Ball:
                    return "balle";
                case Obstacle:
                    return "obstacle";
            }
            
            return "";
        }
    }
    
    private String name;
    private Vector2D size;
    private String image;
    
    public ElementDescription(String name, Vector2D size, String image) throws ValidationException
    {
        setName(name);
        setSize(size);
        setImage(image);
    }
    
    public abstract TypeDescription getType();
    
    public void setName(String name) throws ValidationException
    {
        if(name == null || name.isEmpty())
        {
           throw new ValidationException("Nom invalide") ;
        }
        this.name = name;
    }
    
    public String getName()
    {
        return this.name;
    }
    
    public void setSize(Vector2D size) throws ValidationException
    {
        if(size == null || size.getX() <= 0 || size.getY() <= 0)
        {
           throw new ValidationException("Dimensions invalides") ;
        }
        this.size = size;
    }
    
    public Vector2D getSize()
    {
        return this.size;
    }
    
    public void setImage(String image) throws ValidationException
    {
        if(image == null || image.isEmpty())
        {
           throw new ValidationException("Image invalide") ;
        }
        this.image = image;
    }
    
    public String getImage()
    {
        return this.image;
    }
}
