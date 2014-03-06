
package com.svincent.util;

/**
 * An assertion.
 * @author <a href="http://www.svincent.com/shawn/">Shawn Vincent</a>
 */
public class AssertionException extends NestableRuntimeException
{
  public AssertionException () { super (); }
  public AssertionException (String msg) { super (msg); }
  public AssertionException (String msg, Throwable ex) { super (msg, ex); }
}
