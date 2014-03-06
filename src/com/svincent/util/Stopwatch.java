/*
 * Stopwatch.java
 *
 */

package com.svincent.util;

import java.util.*;
import java.text.*;

/**
 * A timer class, useful for timing code.
 * <p>
 * I got tired of trying to figure out the Date class again and again, 
 * and having unpleasant output, because I was too lazy to process the
 * output.  But never again!  Thanks to the wonders of Object Oriented
 * Computing, I've written a Stopwatch class, which does all the work
 * for me!
 * <p>
 * Using the Stopwatch is <i>very</i> easy.  There are only two 
 * interesting methods: <code>reset</code> and <code>getMillis</code>.
 * <p>
 * <code>reset</code> resets the stopwatch to zero.
 * <i>Note that a newly constructed Stopwatch is implicitly reset.</i>
 * <p>
 * <code>getMillis</code> returns the number of milliseconds that passed
 * since the last call to <code>reset</code>.
 * <i>Note that printing a Stopwatch prints a human-readable representation
 * of the milliseconds which have passed (in hh:mm:ss.mmm format).</i>
 * <p>
 * The following code block illustrates use of the Stopwatch.
 * <code>
 * <ul>
 *   Stopwatch stopwatch = <b>new</b> Stopwatch();<br>
 *   func(); <em> Takes a while to run </em><br>
 *   Util.out.println(stopwatch);<br>
 * </ul>   
 * </code>
 *
 * @author <a href="http://www.svincent.com/shawn/">Shawn Vincent</a>
 */
public class Stopwatch {
  /**
   * The timestamp of the last call to <code>reset</code>
   * <p>
   * This initializer resets the timestamp to the current time 
   * on instantiation.
   */
  Date startTime = new Date ();

  /**
   * Resets the time to 0.
   * <p>
   * (happens implicitly upon construction)
   */
  public void reset () { startTime = new Date (); }
  
  /**
   * Returns a string representation of the current time.
   * <p>
   * This representation is in <b>hh:mm:ss.mmm</b> format, where
   * hh == hours, mm == minutes, ss == seconds, and mmm=milliseconds.
   * The <code>hours</code> field can be longer than two digits, if the
   * Stopwatch has run for longer than 99 hours. (This is unlikely to 
   * be the case, however)
   * 
   * @return The string representation of this object.
   */
  public String tag () 
  {
    long totalmillis = getMillis ();
    long millis = totalmillis % 1000;
    totalmillis /= 1000;
    long seconds = totalmillis % 60;
    totalmillis /= 60;
    long minutes = totalmillis % 60;
    totalmillis /= 60;
    long hours = totalmillis;
    return ""+(hours < 10?"0":"")+hours+
      ":"+(minutes < 10?"0":"")+minutes+
      ":"+(seconds < 10?"0":"")+seconds+
      "."+(millis<100?"0":"")+(millis < 10?"0":"")+millis;
  }

  public String toString () { return tag (); }

  /**
   * Return the number of milliseconds which have passed since we
   * reset the stopwatch.
   *
   * @return The number of milliseconds since the last call to 
   *         <code>reset</code>
   */
  public long getMillis () 
  {
    Date currentTime = new Date ();
    long start = startTime.getTime ();
    long current = currentTime.getTime ();
    return current - start;
  }

  /**
   * Some test code.
   * <p>
   * <ol>
   * <li><code>argv[0]</code> is parsed into an <code>int</code>.</li>
   * <li>The current thread is asked to wait this long, in milliseconds.</li>
   * <li>This waiting is timed by a Stopwatch instance, and the timing is
   *     printed to <code>Util.out</code>.</li>
   * </ol>
   * 
   * @param argv The command line arguments.
   */
  public static void main (String[] argv) throws Exception 
  {
    if (argv.length < 1) 
      {
	Util.out.println ("Usage: ");
	Util.out.println ("  java "+Stopwatch.class.getName()+
			  " <milliseconds>");
	Util.out.println ("Stopwatch demonstrates the use of the Stopwatch ");
	Util.out.println ("class, which does timings and formatting of times");
	return;
      }
    Thread myThread = Thread.currentThread ();
    int timeToSleep = Integer.parseInt (argv[0]);
    Stopwatch stopwatch = new Stopwatch ();
    myThread.sleep (timeToSleep);
    Util.out.println (stopwatch);
  }
}
