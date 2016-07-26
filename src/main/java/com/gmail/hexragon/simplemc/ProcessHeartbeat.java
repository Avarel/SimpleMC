package com.gmail.hexragon.simplemc;

import com.gmail.hexragon.simplemc.controller.MainController;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.util.Duration;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ProcessHeartbeat
{
	private Main app;
	
	private Process process;
	
	private boolean running = false;
	
	private ProcessBuilder processBuilder;
	
	private BufferedReader reader;
	private BufferedWriter writer;
	
	private String ip = getPublicIP();
	
	private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	
	public ProcessHeartbeat(Main app)
	{
		this.app = app;
	}
	
	public void start()
	{
		final MainController mainController = app.getMainController();
		
		String targetJar = app.getMainController().jarSelector.getValue();
		if (!targetJar.endsWith(".jar"))
		{
			Platform.runLater(() -> mainController.consoleField.appendText("[Process] Invalid server jar.\n"));
			return;
		}
		
		processBuilder = new ProcessBuilder("java","-client", "-Xmx1G", "-Xms512M", "-jar", targetJar,  "--nojline", "nogui");
		processBuilder.redirectErrorStream(true);
		
		Thread thread = new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					init();
					
					reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
					writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
					
					String line;
					while ((line = reader.readLine()) != null)
					{
						final String fLine = line;
						Platform.runLater(() -> mainController.consoleField.appendText(fLine+"\n"));
					}//reached end of file when process exits
					reader.close();
					writer.close();
					finish();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			
			void init() throws IOException
			{
				Platform.runLater(() ->
				{
					mainController.statusText.setText("IP: "+ip);
					mainController.statusText.setDisable(false);
					mainController.statusText.setOnAction(event ->
					{
						StringSelection stringSelection = new StringSelection(ip);
						Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
						clpbrd.setContents(stringSelection, null);
					});
				});
				
				mainController.startStopButton.setDisable(true);
				mainController.killButton.setDisable(true);
				scheduler.schedule(()->Platform.runLater(()->
				{
					mainController.startStopButton.setDisable(false);
					mainController.startStopButton.setText("Stop Server");
					mainController.killButton.setDisable(false);
				}), 1, TimeUnit.SECONDS);
				
				
				setMoon(false);
				mainController.consoleField.clear();
				
				Platform.runLater(() -> mainController.consoleField.appendText("[Process] Server starting.\n"));
				process = processBuilder.start();
				running = true;
			}
			
			void finish()
			{
				Platform.runLater(() ->
				{
					mainController.statusText.setText("IP: OFFLINE");
					mainController.statusText.setDisable(true);
					mainController.statusText.setOnAction(null);
				});
				
				mainController.startStopButton.setDisable(true);
				mainController.killButton.setDisable(true);
				scheduler.schedule(()->Platform.runLater(()->
				{
					mainController.startStopButton.setDisable(false);
					mainController.startStopButton.setText("Start Server");
					mainController.killButton.setDisable(true);
				}), 1, TimeUnit.SECONDS);
				
				setMoon(true);
				
				Platform.runLater(() -> mainController.consoleField.appendText("[Process] Server successfully closed.\n"));
				process = null;
				running = false;
			}
			
			void setMoon(boolean bool)
			{
				TranslateTransition tt = new TranslateTransition(Duration.millis(500), mainController.statusImg);
				tt.setByX(100);
				tt.setAutoReverse(true);
				
				TranslateTransition tf0 = new TranslateTransition(Duration.millis(500), mainController.statusImg);
				tf0.setByX(-100);
				tf0.setAutoReverse(true);
				
				if (bool)
				{
					tt.play();
					scheduler.schedule(() -> Platform.runLater(() -> {
						mainController.statusImg.setImage(new Image(getClass().getClassLoader().getResourceAsStream("images/moon.png")));
						tf0.play();
					}), 500, TimeUnit.MILLISECONDS);
				}
				else
				{
					tt.play();
					scheduler.schedule(() -> Platform.runLater(() -> {
						mainController.statusImg.setImage(new Image(getClass().getClassLoader().getResourceAsStream("images/sun.png")));
						tf0.play();
					}), 500, TimeUnit.MILLISECONDS);
				}
			}
		};
		thread.start();
	}
	
	public void writeIn(String input)
	{
		if (process != null)
		{
			try
			{
				writer.write(input + "\n");
				writer.flush();
				
				Platform.runLater(() -> app.getMainController().consoleField.appendText("► "+input+"\n"));
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void stop()
	{
		writeIn("stop");
	}
	
	public void kill()
	{
		if (process != null)
		{
			running = false;
			process.destroyForcibly();
		}
	}
	
	public Process getProcess()
	{
		return process;
	}
	
	public boolean isRunning()
	{
		return running;
	}
	
	public String getPublicIP()
	{
		try
		{
			URL whatismyip = new URL("http://checkip.amazonaws.com");
			BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
			
			String ip = in.readLine(); //you get the IP as a String
			in.close();
			return ip;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return "Error";
	}
}
