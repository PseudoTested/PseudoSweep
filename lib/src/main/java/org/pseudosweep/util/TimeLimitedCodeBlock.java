package org.pseudosweep.util;

// Code taken from http://stackoverflow.com/questions/5715235/java-set-timeout-on-a-certain-block-of-code

import org.pseudosweep.PseudoSweepException;

import java.util.concurrent.*;

public class TimeLimitedCodeBlock {

    public static <T> T runWithTimeout(Callable<T> callable, long timeout, TimeUnit timeUnit) throws TimeoutException { 
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Future<T> future = executor.submit(callable);
        executor.shutdown(); // This does not cancel the already-scheduled task.
        try {
            return future.get(timeout, timeUnit);
        }
        catch (TimeoutException e) {
            future.cancel(true);
            throw e;
        } catch (InterruptedException | ExecutionException e) {
            throw new PseudoSweepException(e);
        }
        
        
        
        
        
        
        
        
        
        
        
    }
}
