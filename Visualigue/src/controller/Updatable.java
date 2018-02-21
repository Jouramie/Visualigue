/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import model.MobileElement;
import model.Vector2D;

public interface Updatable {
    void update();

    Vector2D updateOnRecord(MobileElement mobile);

    void lastUpdate();
}
