package istic.m2miage.possicat;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class MainPOSSICAT extends Application {
	
	@Override
    public void start(Stage primaryStage) throws Exception{
    	Planning p = new Planning(primaryStage);
    	
    	FXMLLoader loader = new FXMLLoader();
    	
    	loader.setLocation(getClass().getResource("/Window.fxml"));
    	
    	loader.setController(p);
    	Parent root = loader.<Parent>load();
    	
    	primaryStage.setTitle("PossiJar");
        primaryStage.setScene(new Scene(root, 800, 500));
        primaryStage.show();
	}
	
	public static void main(String[] args) {
        launch(args);
    }
}