/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bezierrast;

import javafx.geometry.Point2D;

/**
 *
 * @author Ludek
 */
public class TreeNode {

    public TreeNode(Point2D cp1, Point2D cp2, Point2D cp3, Point2D cp4, MainWindowController controller, TreeNode parent, int adaptiveValue) {
        //mám kontrolní body, potřebuju je rozdělit?
        //ano, vypočítám bod rozdělení a získám left a right
        //musím analyzovat left a right (v konstruktoru)        
        this.controller = controller;
        this.parent = parent;
        this.cp1 = cp1;
        this.cp2 = cp2;
        this.cp3 = cp3;
        this.cp4 = cp4;
        this.adaptiveValue = adaptiveValue;
    }

    public void analyzeSegmentComputeLeftRight() {
        if (!controller.evaluateCPs(cp1, cp2, cp3, cp4, adaptiveValue)) {
            controller.computePointOnBez(cp1, cp2, cp3, cp4, 0.5, true, this);         
        }        
    }

    MainWindowController controller;
    private Point2D cp1;
    private Point2D cp2;
    private Point2D cp3;
    private Point2D cp4;
    private int adaptiveValue;

    public TreeNode parent;
    public TreeNode left;
    public TreeNode right;
    public Point2D halfP;
    
}
