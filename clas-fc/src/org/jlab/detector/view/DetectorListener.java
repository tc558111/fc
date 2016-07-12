/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.view;

/**
 *
 * @author gavalian
 * @version Modified by lcsmith for use with ECMon
 */
public interface DetectorListener {
    void update(DetectorShape2D shape);
    void processShape(DetectorShape2D shape);
}
