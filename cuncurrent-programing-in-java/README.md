# cuncurrent programing-java
This repository contains projects submitted as a course for the Cuncurrent Programming in Java Specialization.


## 1.1 Java Threads
**Lecture Summary:** In this lecture, we learned the concept of threads as lower-level building blocks for concurrent programs. A unique aspect of Java compared to prior mainstream programming languages is that Java included the notions of threads (as instances of the \mathtt {java.lang.Thread}java.lang.Thread class) in its language definition right from the start.

When an instance of \mathtt {Thread}Thread is created (via a \mathtt {new}new operation), it does not start executing right away; instead, it can only start executing when its \mathtt {start()}start() method is invoked. The statement or computation to be executed by the thread is specified as a parameter to the constructor.

The Thread class also includes a wait operation in the form of a \mathtt {join()}join() method. If thread \mathtt {t0}t0 performs a \mathtt {t1.join()}t1.join() call, thread \mathtt {t0}t0 will be forced to wait until thread \mathtt {t1}t1 completes, after which point it can safely access any values computed by thread \mathtt {t1}t1. Since there is no restriction on which thread can perform a \mathtt {join}join on which other thread, it is possible for a programmer to erroneously create a deadlock cycle with \mathtt {join}join operations. (A deadlock occurs when two threads wait for each other indefinitely, so that neither can make any progress.)

**Further Reading:**
1. Wikipedia article on [Threads](https://en.wikipedia.org/wiki/Thread_(computing))

2. [Tutorial on Java threads](https://docs.oracle.com/javase/tutorial/essential/concurrency/runthread.html)

3. [Documentation on Thread class in Java 8](https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.html)



## 1.2 Structured Locks 
**Lecture Summary:** In this lecture, we learned about structured locks, and how they can be implemented using \mathtt {synchronized}synchronized statements and methods in Java. Structured locks can be used to enforce mutual exclusion and avoid data races, as illustrated by the \mathtt {incr()}incr() method in the \mathtt {A.count}A.count example, and the \mathtt {insert()}insert() and \mathtt {remove()}remove() methods in the the \mathtt {Buffer}Buffer example. A major benefit of structured locks is that their acquire and release operations are implicit, since these operations are automatically performed by the Java runtime environment when entering and exiting the scope of a \mathtt {synchronized}synchronized statement or method, even if an exception is thrown in the middle.

We also learned about \mathtt {wait()}wait() and \mathtt {notify()}notify() operations that can be used to block and resume threads that need to wait for specific conditions. For example, a producer thread performing an \mathtt {insert()}insert() operation on a bounded buffer can call \mathtt {wait()}wait() when the buffer is full, so that it is only unblocked when a consumer thread performing a \mathtt {remove()}remove() operation calls \mathtt {notify()}notify(). Likewise, a consumer thread performing a \mathtt {remove()}remove() operation on a bounded buffer can call \mathtt {wait()}wait() when the buffer is empty, so that it is only unblocked when a producer thread performing an \mathtt {insert()}insert() operation calls \mathtt {notify()}notify(). Structured locks are also referred to as intrinsic locks or monitors.

**Optional Reading:**
1. [Tutorial on Intrinsic Locks and Synchronization in Java](https://docs.oracle.com/javase/tutorial/essential/concurrency/locksync.html)

2. [Tutorial on Guarded Blocks in Java](https://docs.oracle.com/javase/tutorial/essential/concurrency/guardmeth.html)

3. Wikipedia article on [Monitors](https://en.wikipedia.org/wiki/Monitor_(synchronization))


## 1.3 Unstructured Locks 
**Lecture Summary:** In this lecture, we introduced unstructured locks (which can be obtained in Java by creating instances of  \mathtt {ReentrantLock()}ReentrantLock()), and used three examples to demonstrate their generality relative to structured locks. The first example showed how explicit \mathtt {lock()}lock() and \mathtt {unlock()}unlock() operations on unstructured locks can be used to support a hand-over-hand locking pattern that implements a non-nested pairing of lock/unlock operations which cannot be achieved with synchronized statements/methods. The second example showed how the \mathtt {tryLock()}tryLock() operations in unstructured locks can enable a thread to check the availability of a lock, and thereby acquire it if it is available or do something else if it is not. The third example illustrated the value of read-write locks (which can be obtained in Java by creating instances of \mathtt {ReentrantReadWriteLock()}ReentrantReadWriteLock()), whereby multiple threads are permitted to acquire a lock \mathtt {L}L in “read mode”, \mathtt {L.readLock().lock()}L.readLock().lock(), but only one thread is permitted to acquire the lock in “write mode”, \mathtt {L.writeLock().lock()}L.writeLock().lock().

However, it is also important to remember that the generality and power of unstructured locks is accompanied by an extra responsibility on the part of the programmer, e.g., ensuring that calls to \mathtt {unlock()}unlock() are not forgotten, even in the presence of exceptions.

**Optional Reading:**
1. [Tutorial on Lock Objects in Java](https://docs.oracle.com/javase/tutorial/essential/concurrency/newlocks.html)

2. [Documentation on Java’s Lock interfaces](http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/locks/Lock.html)


## 1.4 Liveness and Progress Guarantees 
**Lecture Summary:** In this lecture, we studied three ways in which a parallel program may enter a state in which it stops making forward progress. For sequential programs, an “infinite loop” is a common way for a program to stop making forward progress, but there are other ways to obtain an absence of progress in a parallel program. The first is deadlock, in which all threads are blocked indefinitely, thereby preventing any forward progress. The second is livelock, in which all threads repeatedly perform an interaction that prevents forward progress, e.g., an infinite “loop” of repeating lock acquire/release patterns. The third is starvation, in which at least one thread is prevented from making any forward progress. 

The term “liveness” refers to a progress guarantee. The three progress guarantees that correspond to the absence of the conditions listed above are deadlock freedom, livelock freedom, and starvation freedom. 

Optional Reading: 
1. [Deadlock example with synchronized methods in Java](https://docs.oracle.com/javase/tutorial/essential/concurrency/deadlock.html) 

2. [Starvation and Livelock examples in Java](https://docs.oracle.com/javase/tutorial/essential/concurrency/starvelive.html)

3. Wikipedia article on [Deadlock and Livelock](https://en.wikipedia.org/wiki/Deadlock)



### Deadlock
### Livelock
### Startvation
