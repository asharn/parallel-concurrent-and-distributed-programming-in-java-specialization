package edu.coursera.concurrent;

import edu.rice.pcdp.Actor;

import java.util.ArrayList;
import java.util.List;

import static edu.rice.pcdp.PCDP.finish;

/**
 * An actor-based implementation of the Sieve of Eratosthenes.
 *
 * TODO Fill in the empty SieveActorActor actor class below and use it from
 * countPrimes to determin the number of primes <= limit.
 */
public final class SieveActor extends Sieve {
    /**
     * {@inheritDoc}
     *
     * TODO Use the SieveActorActor class to calculate the number of primes <=
     * limit in parallel. You might consider how you can model the Sieve of
     * Eratosthenes as a pipeline of actors, each corresponding to a single
     * prime number.
     */
    @Override
    public int countPrimes(final int limit) {
        final SieveActorActor sieveActor = new SieveActorActor(2);
        finish(() -> {
            for (int i = 3; i<= limit; i+=2) {
                sieveActor.send(i);
            }
            sieveActor.send(0);
        });

        int numPrimes = 0;
        SieveActorActor loopActor = sieveActor;
        while (loopActor != null) {
            numPrimes += loopActor.numPrimes;
            loopActor = loopActor.nextActor;
        }
        return numPrimes;
    }

    /**
     * An actor class that helps implement the Sieve of Eratosthenes in
     * parallel.
     */
    public static final class SieveActorActor extends Actor {
        private static final int MAX_LOCAL_PRIMES = 1000;
        private List<Integer> primes;
        private int numPrimes;
        private SieveActorActor nextActor;

        public SieveActorActor(final int localPrime) {
            primes = new ArrayList<>();
            primes.add(localPrime);
            this.nextActor = null;
            this.numPrimes = 1;
        }

        /**
         * Process a single message sent to this actor.
         *
         * TODO complete this method.
         *
         * @param msg Received message
         */
        @Override
        public void process(final Object msg) {
            final int candidate = (Integer) msg;
            if (candidate <= 0) {
            }
            else {
                final boolean locallyPrime = isLocallyPrime(candidate);
                if (locallyPrime) {
                    if (primes.size() <= MAX_LOCAL_PRIMES) {
                        primes.add(candidate);
                        numPrimes++;
                    } else if (nextActor == null) {
                        nextActor = new SieveActorActor(candidate);
                    } else {
                        nextActor.send(msg);
                    }
                }
            }
        }

        private boolean isLocallyPrime(final Integer candidate) {
            return primes
                    .parallelStream()
                    .noneMatch(prime -> candidate % prime == 0);
        }
    }
}
