package model;

public abstract class ElementDescription
{
    private String name;
    private Vector2D size;
    private String image;
    
    public ElementDescription()
    {
        this.name = "";
        this.size = new Vector2D();
        this.image = "";
    }
    
    public ElementDescription(String name, Vector2D size, String image)
    {
        this.name = name;
        this.size = size;
        this.image = image;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public String getName()
    {
        return this.name;
    }
    
    public void setSize(Vector2D size)
    {
        this.size = size;
    }
    
    public Vector2D getSize()
    {
        return this.size;
    }
    
    public void setImage(String image)
    {
        this.image = image;
    }
    
    public String getImage()
    {
        return this.image;
    }
}
