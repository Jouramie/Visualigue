package model;

public class Vector2D implements Cloneable, java.io.Serializable
{
    private double x;
    private double y;

    public Vector2D()
    {
        this.x = 0;
        this.y = 0;
    }

    public Vector2D(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    public void setX(double x)
    {
        this.x = x;
    }

    public void setY(double y)
    {
        this.y = y;
    }

    public double getX()
    {
        return this.x;
    }

    public double getY()
    {
        return this.y;
    }

    public double getAngle()
    {
        double resultat;

        if(getX() != 0)
        {
            resultat = Math.atan(getY() / getX());
        }

        else if(getY() == 0)
        {
            resultat = 0;
        }

        else if(getY() > 0)
        {
            resultat = Math.PI / 2;
        }

        else
        {
            resultat = 3 * Math.PI / 2;
        }

        if(x < 0.0 && y < 0.0)
        {
            resultat += Math.PI;
        }
        else if(y < 0.0 && x > 0.0)
        {
            resultat += Math.PI * 2;
        }
        else if(x < 0.0)
        {
            resultat += Math.PI;
        }
        while(resultat > 2 * Math.PI)
        {
            resultat -= Math.PI;
        }

        return resultat;
    }

    public void setAngle(double a)
    {
        double norme = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        x = Math.cos(a) * norme;
        y = Math.sin(a) * norme;
    }

    public double getLength()
    {
        return Math.sqrt(Math.pow(getX(), 2) + Math.pow(getY(), 2));
    }

    public void setLength(double pGrandeur)
    {
        double angle = getAngle();
        setAngle(0);
        setX(pGrandeur);
        setAngle(angle);
    }

    public Vector2D add(Vector2D v)
    {
        Vector2D resultat = new Vector2D();

        resultat.setX(getX() + v.getX());
        resultat.setY(getY() + v.getY());

        return resultat;
    }

    public Vector2D substract(Vector2D v)
    {
        Vector2D resultat = new Vector2D();

        resultat.setX(getX() - v.getX());
        resultat.setY(getY() - v.getY());

        return resultat;
    }

    public Vector2D multiply(double s)
    {
        Vector2D resultat = new Vector2D();

        resultat.setX(this.getX() * s);
        resultat.setY(this.getY() * s);

        return resultat;
    }

    public double multiply(Vector2D v)
    {
        return this.getX() * v.getX() + this.getY() * v.getY();
    }

    public Vector2D normaliser()
    {
        Vector2D resultat = new Vector2D();

        if(this.getLength() != 0)
        {
            resultat.setX(this.getX() / this.getLength());
            resultat.setY(this.getY() / this.getLength());
        }

        return resultat;
    }
    
    public Vector2D clone()
    {
        return new Vector2D(x, y);

    }
    
    public boolean equals(Vector2D v){
        return x == v.x && y == v.y;
    }
    
    @Override
    public String toString(){
        return "[" + x + ", " + y + "]";
    }
}
