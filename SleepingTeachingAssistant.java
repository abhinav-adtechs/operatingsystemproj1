package adtechs.jordiie.sleepingteachingassistant;

import android.nfc.Tag;
import android.util.Log;

import java.util.concurrent.Semaphore;
import java.util.Random;

public class SleepingTeachingAssistant {

    public static String TAG = "Teaching assistant Logger" ;

    public static void main(String[] args) {
        int numStudents = 5;

        SignalSemaphore wakeup = new SignalSemaphore();
        Semaphore chairs = new Semaphore(3);
        Semaphore available = new Semaphore(1);


        Random studentWait = new Random();

        for (int i = 0; i < numStudents; i++)
        {
            Thread student = new Thread(new Student(studentWait.nextInt(20), wakeup, chairs, available, i+1));
            student.start();
        }

        Thread ta = new Thread(new TeachingAssistant(wakeup, chairs, available));
        ta.start();
    }
}

/**
 * This semaphore wakes up the Teaching Assistant
 */
class SignalSemaphore {
    private boolean signal = false;

    // Used to send the signal.
    public synchronized void take() {
        this.signal = true;
        this.notify();
    }

    // Will wait until it receives a signal before continuing.
    public synchronized void release() throws InterruptedException{
        while(!this.signal) wait();
        this.signal = false;
    }
}

/**
 * Student thread.
 */
class Student implements Runnable
{
    private int programTime;

    private int studentNum;

    private SignalSemaphore wakeup;

    private Semaphore chairs;

    private Semaphore available;

    private Thread t;

    public Student(int program, SignalSemaphore w, Semaphore c, Semaphore a, int num)
    {
        programTime = program;
        wakeup = w;
        chairs = c;
        available = a;
        studentNum = num;
        t = Thread.currentThread();
    }

    /**
     * Infinite loop until Interrupt
     */

    public void run()
    {
        // Infinite loop.
        while(true)
        {
            try
            {
                System.out.println("Student " + studentNum + " has started programming for " + programTime + " seconds.");
                t.sleep(programTime * 1000);

                System.out.println("Student " + studentNum + " is checking to see if TA is available.");
                if (available.tryAcquire())
                {
                    try
                    {
                        wakeup.take();
                        System.out.println("Student " + studentNum + " has woke up the TA.");
                        System.out.println("Student " + studentNum + " has started working with the TA.");
                        t.sleep(5000);
                        System.out.println("Student " + studentNum + " has stopped working with the TA.");
                    }
                    catch (InterruptedException e)
                    {
                        Log.e(SleepingTeachingAssistant.TAG, "Inside run func") ;
                    }
                    finally
                    {
                        available.release();
                    }
                }
                else
                {
                    System.out.println("Student " + studentNum + " could not see the TA.  Checking for available chairs.");
                    if (chairs.tryAcquire())
                    {
                        try
                        {
                            System.out.println("Student " + studentNum + " is sitting outside the office.  "
                                    + "He is #" + ((3 - chairs.availablePermits())) + " in line.");
                            available.acquire();
                            System.out.println("Student " + studentNum + " has started working with the TA.");
                            t.sleep(5000);
                            System.out.println("Student " + studentNum + " has stopped working with the TA.");
                            available.release();
                        }
                        catch (InterruptedException e)
                        {
                            Log.e(SleepingTeachingAssistant.TAG, " Inside run func of Student" ) ;
                        }
                    }
                    else
                    {
                        System.out.println("Student " + studentNum + " could not see the TA and all chairs were taken.  Back to programming!");
                    }
                }
            }
            catch (InterruptedException e)
            {
                break;
            }
        }
    }
}

/**
 * Teaching Assistant Thread
 */

class TeachingAssistant implements Runnable
{
    private SignalSemaphore wakeup;
    private Semaphore chairs;
    private Semaphore available;
    private Thread t;

    public TeachingAssistant(SignalSemaphore w, Semaphore c, Semaphore a)
    {
        t = Thread.currentThread();
        wakeup = w;
        chairs = c;
        available = a;
    }



    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                System.out.println("No students left.  The TA is going to nap.");
                wakeup.release();
                System.out.println("The TA was awoke by a student.");

                t.sleep(5000);

                // If there are other students waiting.
                if (chairs.availablePermits() != 3)
                {
                    do
                    {
                        t.sleep(5000);
                        chairs.release();
                    }
                    while (chairs.availablePermits() != 3);
                }
            }
            catch (InterruptedException e)
            {
             Log.e(SleepingTeachingAssistant.TAG, "Inside run() of Teaching assistant") ;
            }
        }
    }
}


