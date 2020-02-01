package com.amazonaws.transcribestreaming;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.transcribestreaming.TranscribeStreamingAsyncClient;
import software.amazon.awssdk.services.transcribestreaming.model.LanguageCode;
import software.amazon.awssdk.services.transcribestreaming.model.MediaEncoding;
import software.amazon.awssdk.services.transcribestreaming.model.Result;
import software.amazon.awssdk.services.transcribestreaming.model.StartStreamTranscriptionRequest;
import software.amazon.awssdk.services.transcribestreaming.model.StartStreamTranscriptionResponseHandler;
import software.amazon.awssdk.services.transcribestreaming.model.TranscriptEvent;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;


public class Transcriber
{
    private static TranscribeStreamingAsyncClient client;
    private static String partial = "";
    private static String tempPartial = "";
    private static String transcription = "";
    private static String finalTranscription;
    private static TargetDataLine line;
    private static InputStream stream;

    
    public void initiateTranscription() throws InterruptedException, ExecutionException, LineUnavailableException
    {
    	client = TranscribeStreamingAsyncClient.builder()
                .region(Region.US_WEST_2)
              //.credentialsProvider(getCredentials())
                .build();
    	StreamPublisher.subscription = new MySubscriber(null, stream);
    	CompletableFuture<Void> result = client.startStreamTranscription(
    			getRequest(16000), new StreamPublisher(stream),
    			getResponseHandler());
    	result.get();
    	client.close();	
    }
    
    
    public void initiateRecording()
    {
    	stream = getStreamFromMic();
    }
    
    
    private InputStream getStreamFromMic()
    {
        AudioFormat format = setFormat();
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        if (!AudioSystem.isLineSupported(info))
        {
            System.out.println("Line not supported");
            System.exit(1);
        }
        try
        {
			line = (TargetDataLine) AudioSystem.getLine(info);
			line.open(format);
			line.start();
		} catch (LineUnavailableException e)
        {
			System.out.println("No available audio lines could be found");
			e.printStackTrace();
			System.exit(1);
		}
        AudioInputStream inputStream = new AudioInputStream(line);
        return inputStream;
    }
    
    
    public void closeMicLine()
    {
    	line.stop();
    	line.close();
    }
    
    
    private StartStreamTranscriptionRequest getRequest(int sampleRateHertz)
    {
        return StartStreamTranscriptionRequest.builder()
                .languageCode(LanguageCode.EN_US)
                .mediaEncoding(MediaEncoding.PCM)
                .mediaSampleRateHertz(sampleRateHertz)
                .vocabularyName("AlphaNumericAndStates")
                .build();
    }
    
    
    private StartStreamTranscriptionResponseHandler getResponseHandler()
    {
        return StartStreamTranscriptionResponseHandler.builder()
                .onResponse(r -> {
                	Main.writeToLog("Execution " + Integer.toString(Main.counter) + ":\n");
                })
                .onError(e -> {
                    System.out.println("Response Error Occurred: ");
                    Main.writeToLog("An error occurred in processing the audio: \n" + e.getMessage().toString() + "\n\n");
                })
                .onComplete(() -> {
                	/*This conditional appends remaining transcription to the final transcription if Amazon Transcribe
                	 *ends a transcription job without indication the last portion as not 'isPartial'
                	 */
                	if (tempPartial.length() > 0)
                	{
                		transcription += " " + tempPartial;
                	}
                	finalTranscription = transcription.strip();
                	try
                	{
                		String query = Main.interpreter.format(finalTranscription);
						myGUI.setOutputMessage(query);
						//This resets the debug text for each successful iteration
						if(!myGUI.getDebugInfo().getText().contentEquals(" "))
						{
							myGUI.setDebugText(" ");
						}
					} catch (Exception e1)
                	{
						myGUI.setOutputMessage("Transcribe couldn't handle the following command:");
						myGUI.setDebugText(finalTranscription + " ");
					} finally
                	{
						transcription = "";
						Main.writeToLog("final transcription: " + finalTranscription + "\n\n");
						Main.writeToCountFile(++Main.counter);
					}
                })
                .subscriber(event -> {
                    List<Result> results = ((TranscriptEvent) event).transcript().results();
                    if (results.size() > 0 && !results.get(0).alternatives().get(0).transcript().isEmpty())
                    {
                    	partial = results.get(0).alternatives().get(0).transcript();
                    	tempPartial = partial;
                    	Main.writeToLog(String.format("partial: %s\n", partial));
                    	
                    	if (!results.get(0).isPartial())
                    	{
                    		transcription += " " + partial;
                    		tempPartial = "";
                    	}
                    }
                })
                .build();
    }

    
    private AudioFormat setFormat()
    {
    	int sampleRate = 16000;
        int bitSampleSize = 16;
        int channels = 1;
        boolean signed = true;
        boolean smallEndian = false;
        return new AudioFormat(sampleRate, bitSampleSize, channels, signed, smallEndian);
    }
}