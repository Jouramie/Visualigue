/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import model.MobileElement;
import model.Vector2D;

/**
 *
 * @author JEBOL42
 */
public interface Updatable
{
    public void update();
    public Vector2D updateOnRecord(MobileElement mobile);
    public void lastUpdate();
}
