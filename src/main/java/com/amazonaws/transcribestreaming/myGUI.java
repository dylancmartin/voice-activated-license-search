package com.amazonaws.transcribestreaming;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.ExecutionException;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * @author chasepeak
 * @version 2.1
 * @implNote This class forms a graphical user interface to facilitate audio-recording and to display RecordSearch results.
 * A KeyListener manages the transcription process through static TranscriptionExecution methods and multi-threading.
 */

@SuppressWarnings("serial")
public class myGUI extends JFrame
{
	private JFrame frame;
	private Thread transcriberThread;
	private static JLabel debugInfo;
	private static JLabel outputLabel;
	private JLabel recordingStatus;
	private boolean isPressed = false;
	private final Dimension windowSize = new Dimension(475, 175);
	private final GridBagConstraints constraints = new GridBagConstraints();
	private Transcriber transcriber = new Transcriber();
	
	
	public myGUI()
	{
		super("Voice-Enabled RecordSearch");
		buildWindow();
		setupKeyListener();
	}
	
	
	private void buildWindow()
	{
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setMinimumSize(windowSize);
		setLayout(new GridBagLayout());
		
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		
		JLabel header = new JLabel("Press + hold [enter] to capture audio");
		JLabel context0 = new JLabel("Utterance format:");
		JLabel context1 = new JLabel("\"run {state} {license plate/drivers license} {record information}.\"");
		debugInfo = new JLabel(" ");
		outputLabel = new JLabel(" ");
		recordingStatus = new JLabel("recording status: off");
		
		add(recordingStatus, constraints);
		add(header, constraints);
		add(context0, constraints);
		add(context1, constraints);
		add(outputLabel, constraints);
		add(debugInfo, constraints);
	}
	
	
	private void setupKeyListener()
	{
		addKeyListener(new KeyListener()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				//to modify which key to press + hold, change KeyEvent.[KeyCode]
				if (e.getKeyCode() == KeyEvent.VK_ENTER && !isPressed)
				{
					isPressed = !isPressed;
					resetTexts();
					setRecordingStatus();
					transcriber.initiateRecording();
					
					transcriberThread = new Thread(new Runnable() {
			            public void run()
			            {
							try
							{
								transcriber.initiateTranscription();
							} catch (InterruptedException | ExecutionException | LineUnavailableException e)
							{
								System.out.println("The transcription process could not be executed");
								e.printStackTrace();
								System.exit(1);
							}
			            }
					});
					transcriberThread.start();
				}
			}
			@Override
			public void keyReleased(KeyEvent e)
			{
				//to modify which key is pressed, held, and released, change KeyEvent.[KeyCode]
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					isPressed = !isPressed;
					try
					{
						/*This slows down the ending of the transcription to capture the following 0.75s of audio
						 *in order to improve the accuracy of the transcription
						 */
						Thread.sleep(750);
					} catch (InterruptedException e1)
					{
						e1.printStackTrace();
					}
					transcriber.closeMicLine();
					setRecordingStatus();
				}
			}
			@Override
			public void keyTyped(KeyEvent e) {}
		});
	}
	
	
	private void setRecordingStatus()
	{
		String message = (isPressed) ? "recording status: on" : "recording status: off";
		Color color = (isPressed) ? Color.red : Color.black;
		recordingStatus.setText(message);
		recordingStatus.setForeground(color);
	}
	
	
	public static void setOutputMessage(String s)
	{
		outputLabel.setText(s);
	}
	
	
	public static void setDebugText(String s)
	{
		debugInfo.setText(s);
	}
	
	
	public static JLabel getDebugInfo()
	{
		return debugInfo;
	}

	
	private void resetTexts()
	{
		outputLabel.setText(" ");
		debugInfo.setText(" ");
	}

	
	public void render()
	{
		frame = new myGUI();
		frame.pack();
		frame.setVisible(true);
	}
}