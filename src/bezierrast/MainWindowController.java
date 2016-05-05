/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bezierrast;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

/**
 * FXML Controller class
 *
 * @author Ludek
 */
public class MainWindowController implements Initializable {

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        canvas1.getGraphicsContext2D().setLineWidth(2);

        canvas2.getGraphicsContext2D().setLineWidth(2);
        redrawCanvas();
    }

    boolean arePointsVisible = true;
    int pointCount = 0;
    int selectedPointIndex = 3;
    boolean zadavaciRezim = true;
    int segments = 7;
    int adaptiveValue = 100;
    int adaptivePoints = 0;
    float controlPointSize = 14;
    Color pointColor = Color.BLUEVIOLET;
    Point2D[] controlPoints = new Point2D[4];
    //ArrayList<Point2D> queue = new ArrayList<>();

    boolean r = true;

    double[] tempPointsX = new double[5];
    double[] tempPointsY = new double[5];
    double[] bezierPointsX;
    double[] bezierPointsY;

    @FXML
    private Canvas canvas1;
    @FXML
    private Canvas canvas2;
    
    @FXML
    private Label lblNumberOfSegments;
    @FXML
    private Slider sliderSegment;
    @FXML
    private Slider sliderAdaptive;
    @FXML
    private Label lblVisiblePoints;
    @FXML
    private Label lblAdaptiveValue;
    @FXML
    private Label lblNumberOfPointsAdaptive;

    @FXML
    private void canvasPress(MouseEvent event) {
        if (zadavaciRezim) {
            controlPoints[pointCount] = new Point2D(event.getX(), event.getY());
            pointCount++;
            if (pointCount == 4) {
                zadavaciRezim = false;
            }
            redrawCanvas();
        }
        else {
            //detekce vybraneho bodu
            for (int i = 0; i < pointCount; i++) {
                if (controlPoints[i].distance(event.getX(), event.getY()) < 30.0) {
                    selectedPointIndex = i;
                    break;
                }
                else {
                    selectedPointIndex = -1;
                }
            }
        }
    }

    @FXML
    private void canvasDragged(MouseEvent event) {
        if (!zadavaciRezim) {
            if (selectedPointIndex != -1) {
                controlPoints[selectedPointIndex] = new Point2D(event.getX(), event.getY());
                redrawCanvas();
            }
        }
    }

    @FXML
    private void cleanPoints() {
        pointCount = 0;
        selectedPointIndex = -1;
        zadavaciRezim = true;
        redrawCanvas();
    }

    @FXML
    private void sliderSegmentChange() {
        int numberOfPointsOnBez = (int) sliderSegment.getValue()+1;
        lblNumberOfSegments.setText("Počet bodů: " + numberOfPointsOnBez);
        segments = (int) sliderSegment.getValue();
        redrawCanvas();
    }
    
    @FXML
    private void sliderAdaptiveValueChange(){
        adaptiveValue = (int)sliderAdaptive.getValue();
        lblAdaptiveValue.setText("Práh: " + adaptiveValue);
        redrawCanvas();
    }

    private void redrawCanvas() {
        canvas1.getGraphicsContext2D().setFill(Color.WHITE);
        canvas1.getGraphicsContext2D().fillRect(0, 0, canvas1.getWidth(), canvas1.getHeight());
        canvas2.getGraphicsContext2D().setFill(Color.WHITE);
        canvas2.getGraphicsContext2D().fillRect(0, 0, canvas2.getWidth(), canvas2.getHeight());

        canvas1.getGraphicsContext2D().setFill(pointColor);
        canvas2.getGraphicsContext2D().setFill(pointColor);

        for (int i = 0; i < pointCount; i++) {
            canvas1.getGraphicsContext2D().fillOval(controlPoints[i].getX() - (controlPointSize / 2), controlPoints[i].getY() - (controlPointSize / 2), controlPointSize, controlPointSize);
            canvas2.getGraphicsContext2D().fillOval(controlPoints[i].getX() - (controlPointSize / 2), controlPoints[i].getY() - (controlPointSize / 2), controlPointSize, controlPointSize);
        }

        if (!zadavaciRezim) {
            canvas1.getGraphicsContext2D().setStroke(Color.DARKTURQUOISE);
            canvas2.getGraphicsContext2D().setStroke(Color.DARKTURQUOISE);
            canvas1.getGraphicsContext2D().strokeLine(controlPoints[0].getX(), controlPoints[0].getY(), controlPoints[1].getX(), controlPoints[1].getY());
            canvas1.getGraphicsContext2D().strokeLine(controlPoints[1].getX(), controlPoints[1].getY(), controlPoints[2].getX(), controlPoints[2].getY());
            canvas1.getGraphicsContext2D().strokeLine(controlPoints[2].getX(), controlPoints[2].getY(), controlPoints[3].getX(), controlPoints[3].getY());
            canvas2.getGraphicsContext2D().strokeLine(controlPoints[0].getX(), controlPoints[0].getY(), controlPoints[1].getX(), controlPoints[1].getY());
            canvas2.getGraphicsContext2D().strokeLine(controlPoints[1].getX(), controlPoints[1].getY(), controlPoints[2].getX(), controlPoints[2].getY());
            canvas2.getGraphicsContext2D().strokeLine(controlPoints[2].getX(), controlPoints[2].getY(), controlPoints[3].getX(), controlPoints[3].getY());

            double step = 0;
            bezierPointsX = new double[segments + 1];
            bezierPointsY = new double[segments + 1];
            canvas1.getGraphicsContext2D().setStroke(Color.BLUE);
            for (int i = 0; i < segments + 1; i++) {

                Point2D p = computePointOnBez(controlPoints[0], controlPoints[1], controlPoints[2], controlPoints[3], step, false, null);
                bezierPointsX[i] = p.getX();
                bezierPointsY[i] = p.getY();
                step += 1 / (float) segments;
                if (i != 0) {
                    canvas1.getGraphicsContext2D().strokeLine(bezierPointsX[i - 1], bezierPointsY[i - 1], bezierPointsX[i], bezierPointsY[i]);
                }
            }

            TreeNode queue = new TreeNode(controlPoints[0], controlPoints[1], controlPoints[2], controlPoints[3], this, null, adaptiveValue);

            //vytváření bodů - procházení stromu do hloubky
            depth(queue);

            TreeNode currentNode = queue;
            Point2D lastPoint = controlPoints[0];
            adaptivePoints = 0;
            canvas2.getGraphicsContext2D().setStroke(Color.RED);
            while (true) {
                if (currentNode.left.halfP != null) {
                    //zkontroluj levé rameno, pokud je hodnota k vykreslení, skoč na uzel
                    currentNode = currentNode.left;
                }
                else {
                    //je bod k vykresleni?, vykresli bod, null
                    if (currentNode.halfP != null) {
                        canvas2.getGraphicsContext2D().strokeLine(lastPoint.getX(), lastPoint.getY(), currentNode.halfP.getX(), currentNode.halfP.getY());
                        lastPoint = currentNode.halfP;
                        adaptivePoints++;
                        currentNode.halfP = null;
                    }
                    else {
                        break;
                    }
                    //zkontroluj pravé rameno, pokud je hodnota k vyresleni skoč, opakuj smycku
                    if (currentNode.right.halfP != null) {
                        currentNode = currentNode.right;
                    }
                    else {
                        //pokud ne, hledej rodice tak dlouho, dokud nema hodnotu k vykresleni
                        //pokud není žádní nalezena, je vykrelseno jak hlavni leve, tak i prave rameno, konec
                        while (currentNode.parent != null) {
                            currentNode = currentNode.parent;
                            if (currentNode.halfP != null) {
                                break;
                            }
                        }
                    }

                }
            }
            adaptivePoints += 2;
            canvas2.getGraphicsContext2D().strokeLine(lastPoint.getX(), lastPoint.getY(), controlPoints[3].getX(), controlPoints[3].getY());
            lblNumberOfPointsAdaptive.setText("Počet bodů: " + adaptivePoints);
        }
    }

    private double lerp(double a, double b, double f) {
        return a + f * (b - a);
    }

    //ohodnocení kontrolních bodů (zda je nutné segment rozdělit na dvě části)
    public boolean evaluateCPs(Point2D cp1, Point2D cp2, Point2D cp3, Point2D cp4, double threshold) {
        Point2D u = cp4.subtract(cp1);
        Point2D n = new Point2D(u.getY(), -u.getX());
        double c = -(cp1.getX() * n.getX() + cp1.getY() * n.getY());
        double cpl1 = cp2.getX() * n.getX() + cp2.getY() * n.getY() + c;
        double cpl2 = cp3.getX() * n.getX() + cp3.getY() * n.getY() + c;
        if (cpl1 > threshold || cpl1 < -threshold) {
            //pokud je první vně okolí
            return false;
        }
        else if (cpl2 > threshold || cpl2 < -threshold) {
            //pokud je první uvnitř a druhý vně
            return false;
        }
        else {
            //pokud je první a druhý uvnitř
            return true;
        }
    }

    //vypočítá bod na beziérově křivce - funkci používají oba algoritmy
    public Point2D computePointOnBez(Point2D cp1, Point2D cp2, Point2D cp3, Point2D cp4, double step, boolean adaptive, TreeNode node) {
        //double d = tempPoints[0].getX();
        tempPointsX[0] = lerp(cp1.getX(), cp2.getX(), step);
        tempPointsY[0] = lerp(cp1.getY(), cp2.getY(), step);
        tempPointsX[1] = lerp(cp3.getX(), cp4.getX(), step);
        tempPointsY[1] = lerp(cp3.getY(), cp4.getY(), step);
        tempPointsX[2] = lerp(cp2.getX(), cp3.getX(), step);
        tempPointsY[2] = lerp(cp2.getY(), cp3.getY(), step);

        tempPointsX[3] = lerp(tempPointsX[0], tempPointsX[2], step);
        tempPointsY[3] = lerp(tempPointsY[0], tempPointsY[2], step);
        tempPointsX[4] = lerp(tempPointsX[2], tempPointsX[1], step);
        tempPointsY[4] = lerp(tempPointsY[2], tempPointsY[1], step);

        Point2D finP = new Point2D(lerp(tempPointsX[3], tempPointsX[4], step), lerp(tempPointsY[3], tempPointsY[4], step));

        if (adaptive) {
            if (arePointsVisible) {
                canvas2.getGraphicsContext2D().setStroke(Color.RED);
                canvas2.getGraphicsContext2D().strokeOval(finP.getX() - 4, finP.getY() - 4, 8, 8);
            }

            node.left = new TreeNode(cp1, new Point2D(tempPointsX[0], tempPointsY[0]), new Point2D(tempPointsX[3], tempPointsY[3]), finP, this, node, adaptiveValue);
            node.right = new TreeNode(finP, new Point2D(tempPointsX[4], tempPointsY[4]), new Point2D(tempPointsX[1], tempPointsY[1]), cp4, this, node, adaptiveValue);
            node.halfP = finP;

            return null;
        }
        else {
            if (arePointsVisible) {
                canvas1.getGraphicsContext2D().strokeOval(finP.getX() - 4, finP.getY() - 4, 8, 8);
            }
            return finP;
        }
    }

    //prohledávání do hloubky
    public void depth(TreeNode root) {
        if (root == null) {
            return;
        }
        else {
            root.analyzeSegmentComputeLeftRight();
        }
        depth(root.left);
        depth(root.right);
    }

    @FXML
    private void showPoints() {
        if (arePointsVisible) {
            arePointsVisible = false;
            lblVisiblePoints.setText("NE");
            redrawCanvas();
        }
        else {
            arePointsVisible = true;
            lblVisiblePoints.setText("ANO");
            redrawCanvas();
        }
    }

}
