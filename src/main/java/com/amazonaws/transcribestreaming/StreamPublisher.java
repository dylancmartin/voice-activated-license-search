package com.amazonaws.transcribestreaming;

import java.io.InputStream;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import software.amazon.awssdk.services.transcribestreaming.model.AudioStream;

public class StreamPublisher implements Publisher<AudioStream>
{
    private final InputStream inputStream;
	public static MySubscriber subscription;


    public StreamPublisher(InputStream inputStream)
    {
        this.inputStream = inputStream;
    }

    
    @Override
    public void subscribe(Subscriber<? super AudioStream> s)
    {
    	if ( s != null)
    	{
    		subscription.cancel();
    	}
        subscription = new MySubscriber(s, inputStream);
        s.onSubscribe(subscription);
    }
}