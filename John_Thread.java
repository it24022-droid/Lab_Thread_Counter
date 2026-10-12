import java.util.concurrent.atomic.AtomicLong;

class CounterTask implements Runnable {
    private final int increments;
    private final boolean threadSafe;
    
    private long nonStaticCounter = 0;
    
    private static AtomicLong atomicStaticCounter = new AtomicLong(0);
    private static long unsafeStaticCounter = 0;

    public CounterTask(int increments, boolean threadSafe) {
        this.increments = increments;
        this.threadSafe = threadSafe;
    }

    @Override
    public void run() {
        for (int i = 0; i < increments; i++) {
            nonStaticCounter++;

            if (threadSafe) {
                atomicStaticCounter.incrementAndGet();
            } else {
                unsafeStaticCounter++;
            }
        }
    }

    public long getNonStaticCounter() {
        return nonStaticCounter;
    }

    public static long getStaticCounter(boolean threadSafe) {
        if (threadSafe) {
            return atomicStaticCounter.get();
        } else {
            return unsafeStaticCounter;
        }
    }

    public static void resetCounters() {
        atomicStaticCounter.set(0);
        unsafeStaticCounter = 0;
    }
}

public class John_Thread {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java John_Thread <threads> <increments_per_thread> <true/false>");
            return;
        }

        int numThreads = Integer.parseInt(args[0]);
        int incrementsPerThread = Integer.parseInt(args[1]);
        boolean threadSafe = Boolean.parseBoolean(args[2]);

        CounterTask.resetCounters();

        Thread[] threads = new Thread[numThreads];
        CounterTask[] tasks = new CounterTask[numThreads];

        for (int i = 0; i < numThreads; i++) {
            tasks[i] = new CounterTask(incrementsPerThread, threadSafe);
            threads[i] = new Thread(tasks[i]);
            threads[i].start();
        }

        for (int i = 0; i < numThreads; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long totalNonStatic = 0;
        for (int i = 0; i < numThreads; i++) {
            totalNonStatic += tasks[i].getNonStaticCounter();
        }

        long finalStatic = CounterTask.getStaticCounter(threadSafe);
        long expected = (long) numThreads * incrementsPerThread;
        long absoluteDiff = Math.abs(finalStatic - totalNonStatic);
        double percentageDiff = (totalNonStatic == 0) ? 0.0 : ((double) absoluteDiff / totalNonStatic) * 100.0;

        System.out.println("--- Experiment Results ---");
        System.out.println("Threads (N): " + numThreads);
        System.out.println("Increments per thread (K): " + incrementsPerThread);
        System.out.println("Mode (Thread-safe/Atomic): " + threadSafe);
        System.out.println("Expected Count: " + expected);
        System.out.println("Static Count (S): " + finalStatic);
        System.out.println("Non-Static Total (I): " + totalNonStatic);
        System.out.println("Absolute Difference (D): " + absoluteDiff);
        System.out.println("Percentage Difference (P): " + percentageDiff + "%");
    }
}
