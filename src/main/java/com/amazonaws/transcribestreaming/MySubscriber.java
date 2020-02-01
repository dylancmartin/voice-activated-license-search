package com.amazonaws.transcribestreaming;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.transcribestreaming.model.AudioEvent;
import software.amazon.awssdk.services.transcribestreaming.model.AudioStream;


public class MySubscriber implements Subscription
{
    private final Subscriber<? super AudioStream> subscriber;
    private final InputStream inputStream;
    private ExecutorService executor = Executors.newFixedThreadPool(1);
    private AtomicLong demand = new AtomicLong(0);


    public MySubscriber(Subscriber<? super AudioStream> subscriber, InputStream inputStream)
    {
        this.subscriber = subscriber;
        this.inputStream = inputStream;
    }

    @Override
    public void request(long n)
    {
        if (n <= 0)
        {
            subscriber.onError(new IllegalArgumentException("Demand must be positive"));
        }
        demand.getAndAdd(n);

        executor.submit(() -> {
            try
            {
                do
                {
                    ByteBuffer audioBuffer = getNextEvent();
                    if (audioBuffer.remaining() > 0)
                    {
                        AudioEvent audioEvent = audioEventFromBuffer(audioBuffer);
                        subscriber.onNext(audioEvent);
                    }
                    else
                    {
                        subscriber.onComplete();
                        break;
                    }
                } while (demand.decrementAndGet() > 0);
            } catch (Exception e1)
            {
                subscriber.onError(e1);
            }
        });
    }

    @Override
    public void cancel()
    {
        executor.shutdown();
    }

    private ByteBuffer getNextEvent()
    {
        ByteBuffer audioBuffer = null;
        byte[] audioBytes = new byte[1024];

        int len = 0;
        try
        {
            len = inputStream.read(audioBytes);

            if (len <= 0)
            {
            	audioBuffer = ByteBuffer.allocate(0);
            }
            else
            {
                audioBuffer = ByteBuffer.wrap(audioBytes, 0, len);
            }
        } catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
        return audioBuffer;
    }

    private AudioEvent audioEventFromBuffer(ByteBuffer bb)
    {
        return AudioEvent.builder()
                .audioChunk(SdkBytes.fromByteBuffer(bb))
                .build();
    }
}