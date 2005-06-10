/*
 * @test
 * @synopsis multiple threads using a single lock
 */
/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain. Use, modify, and
 * redistribute this code in any way without acknowledgement.
 */

import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

public final class SimpleWriteLockLoops {
    static final ExecutorService pool = Executors.newCachedThreadPool();
    static final LoopHelpers.SimpleRandom rng = new LoopHelpers.SimpleRandom();
    static boolean print = false;
    static int iters = 10000000;

    public static void main(String[] args) throws Exception {
        int maxThreads = 100;
        if (args.length > 0) 
            maxThreads = Integer.parseInt(args[0]);

        print = true;

        int reps = 2;
        for (int i = 1; i <= maxThreads; i += (i+1) >>> 1) {
            int n = reps;
            if (reps > 1) --reps;
            while (n-- > 0) {
                System.out.print("Threads: " + i);
                new WriteLockLoop(i).test();
                Thread.sleep(100);
            }
        }
        pool.shutdown();
    }

    static final class WriteLockLoop implements Runnable {
        private int v = rng.next();
        private volatile int result = 17;
        private volatile int readBarrier;
        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        private final LoopHelpers.BarrierTimer timer = new LoopHelpers.BarrierTimer();
        private final CyclicBarrier barrier;
        private final int nthreads;
        WriteLockLoop(int nthreads) {
            this.nthreads = nthreads;
            barrier = new CyclicBarrier(nthreads+1, timer);
        }

        final void test() throws Exception {
            for (int i = 0; i < nthreads; ++i) 
                pool.execute(this);
            barrier.await();
            barrier.await();
            if (print) {
                long time = timer.getTime();
                long tpi = time / ((long)iters * nthreads);
                System.out.print("\t" + LoopHelpers.rightJustify(tpi) + " ns per lock");
                double secs = (double)(time) / 1000000000.0;
                System.out.println("\t " + secs + "s run time");
            }

            int r = result;
            if (r == 0) // avoid overoptimization
                System.out.println("useless result: " + r);
        }

        public final void run() {
            final Lock lock = this.lock.writeLock();
            try {
                barrier.await(); 
                int sum = v + 1;
                int x = 0;
                int n = iters;
                while (n-- > 0) {
                    lock.lock();
                    int k = (sum & 3);
                    if (k > 0) {
                        x = v;
                        while (k-- > 0)
                            x = LoopHelpers.compute6(x);
                        v = x;
                    }
                    else x = sum + 1;
                    lock.unlock();
                    if ((x += readBarrier) == 0)
                        ++readBarrier;
                    for (int l = x & 7; l > 0; --l)
                        sum += LoopHelpers.compute6(sum);
                } 
                barrier.await();
                result += sum;
            }
            catch (Exception ie) { 
                return; 
            }
        }
    }

}
