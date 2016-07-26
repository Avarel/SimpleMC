package com.gmail.hexragon.simplemc.controller;

import com.gmail.hexragon.simplemc.Main;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.scene.image.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MainController
{
	private final Main app;
	public JFXButton startStopButton;
	public JFXButton killButton;
	public JFXComboBox<String> jarSelector;
	public JFXTextArea consoleField;
	public JFXTextField inputField;
	public JFXButton statusText;
	public ImageView statusImg;
	
	private List<String> cmdHistory;
	private int cmdHistoryPos = -1;
	
	public MainController(Main app)
	{
		this.app = app;
		cmdHistory = new ArrayList<>();
	}

	public void setup()
	{
		inputField.setOnKeyPressed(key ->
		{
			switch (key.getCode())
			{
				case ENTER:
					app.getHeartbeat().writeIn(inputField.getText());
					cmdHistory.add(inputField.getText());
					Platform.runLater(() -> inputField.clear());
					cmdHistoryPos = -1;
					break;
				case UP:
					if (!cmdHistory.isEmpty())
					{
						if (cmdHistoryPos != -1) cmdHistoryPos -= 1;
						else cmdHistoryPos = cmdHistory.size() - 1;
						if (cmdHistoryPos < 0) cmdHistoryPos = 0;
						Platform.runLater(() -> inputField.setText(cmdHistory.get(cmdHistoryPos)));
					}
					break;
				case DOWN:
					if (!cmdHistory.isEmpty() && cmdHistoryPos != -1)
					{
						cmdHistoryPos += 1;
						if (cmdHistoryPos >= cmdHistory.size())
						{
							cmdHistoryPos = -1;
							Platform.runLater(() -> inputField.clear());
							return;
						}
						Platform.runLater(() -> inputField.setText(cmdHistory.get(cmdHistoryPos)));
					}
			}
		});
		
		startStopButton.setOnMouseClicked(event ->
		{
			if (app.getHeartbeat().isRunning())
			{
				app.getHeartbeat().stop();
			}
			else
			{
				app.getHeartbeat().start();
			}
		});
		
		killButton.setOnAction(event -> app.getHeartbeat().kill());
		
		checkForJars();
		
		jarSelector.setOnAction(event ->
		{
			try
			{
				if (jarSelector.getValue().equals("Recheck..."))
				{
					Platform.runLater(this::checkForJars);
				}
			}
			
			catch (NullPointerException ignored) {}
		});
	}
	
	private void checkForJars()
	{
		jarSelector.getItems().clear();
		
		@SuppressWarnings("ConstantConditions")
		List<String> names = Arrays.stream(new File(".").listFiles((dir, name) -> name.endsWith(".jar") && !name.contains("mcWrapper"))).map(File::getName).collect(Collectors.toList());
		
		jarSelector.getItems().addAll(names);
		jarSelector.getItems().add("Recheck...");
		
		if (jarSelector.getItems().size() > 1)
		{
			jarSelector.setValue(jarSelector.getItems().get(0));
			startStopButton.setDisable(false);
		}
		else
		{
			jarSelector.getItems().clear();
			jarSelector.getItems().add("No jar-file found...");
			jarSelector.getItems().add("Recheck...");
			jarSelector.setValue(jarSelector.getItems().get(0));
			startStopButton.setDisable(true);
		}
	}
	

}
