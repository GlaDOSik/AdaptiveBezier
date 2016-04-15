/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bezierrast;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**

 @author Ludek
 */
public class BezierRast extends Application {

    @Override
    public void start(Stage primaryStage){

        FXMLLoader loader = new FXMLLoader();
        try{
            Parent root = loader.load(getClass().getResource("MainWindow.fxml"));
            Scene scene = new Scene(root, 800, 525);
            primaryStage.setTitle("Rasterizace Beziérovy křivky");
            primaryStage.setScene(scene);
            primaryStage.setWidth(820);
            primaryStage.setHeight(545);
            primaryStage.setResizable(false);
            primaryStage.show();
        }
        catch (IOException ex){
            Logger.getLogger(BezierRast.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     @param args the command line arguments
     */
    public static void main(String[] args){
        launch(args);
    }

}
