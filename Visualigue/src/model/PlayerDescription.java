/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 *
 * @author emile
 */
public class PlayerDescription extends ElementDescription implements java.io.Serializable
{
    public PlayerDescription(String name, Vector2D size, String image) throws ValidationException
    {
        super(name, size, image);
    }
    
    @Override
    public TypeDescription getType()
    {
        return TypeDescription.Player;
    }
}
