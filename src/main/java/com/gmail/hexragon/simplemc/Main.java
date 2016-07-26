package com.gmail.hexragon.simplemc;

import com.gmail.hexragon.simplemc.controller.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Optional;

public class Main extends Application
{
	private Stage stage;
	
	private MainController mainController;
	
	private ProcessHeartbeat heartbeat;
	
	public static void main(String[] args)
	{
		launch(args);
	}
	
	@Override
	public void start(Stage stage) throws Exception
	{
		this.stage = stage;
		
		// loading main stuff
		FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("files/main.fxml"));
		
		
		heartbeat = new ProcessHeartbeat(this);
		mainController = new MainController(this);
		
		
		loader.setController(getMainController());
		
		Parent root = loader.load();
		
		// meta stuff
		stage.setTitle("SimpleMC");
		stage.setScene(new Scene(root));
		stage.getIcons().add(new Image(this.getClass().getClassLoader().getResourceAsStream("images/icon.png")));
		//resetStagePosition();
		
		Platform.setImplicitExit(false);
		stage.setOnCloseRequest(event ->
		{
			if (!handleClose()) event.consume();
		});
		
		getMainController().setup();
		
		stage.show();
	}
	
	public MainController getMainController()
	{
		return mainController;
	}
	
	public Stage getStage()
	{
		return stage;
	}
	
	public ProcessHeartbeat getHeartbeat()
	{
		return heartbeat;
	}
	
	public boolean handleClose()
	{
		if (heartbeat.isRunning())
		{
			//mainController.consoleField.appendText("Process is still running! Stop or cancel it first!\n");
			
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.setTitle("Confirmation");
			alert.setContentText("The process is still running. Kill it?");
			
			Optional<ButtonType> action = alert.showAndWait();
			
			if (!action.isPresent() || action.get() == ButtonType.CANCEL)
			{
				return false;
			}
			else
			{
				heartbeat.kill();
			}
		}
		
		stage.close();
		Platform.exit();
		System.exit(0);
		return true;
	}
}