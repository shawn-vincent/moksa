/*
 * 
 * Copyright (C) 1999  Shawn P. Vincent (svincent@svincent.com)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * -------------------------------------------------------------------------
 *
 * SmallJavaValidationError.java
 * 
 */
package com.svincent.smalljava;

import com.svincent.util.NestableException;


/**
 * Thrown if errors occur while finalizing a SmallJava data structure.
 *
 * <p>This is a NestableException, which means that it can contain 
 * a subexception, if some other exception was thrown that caused this
 * one.</p>
 *
 * @author <a href="http://www.svincent.com/shawn/">Shawn Vincent</a>
 * @see NestableException
 */
public class SmallJavaValidationException extends NestableException {
  public SmallJavaValidationException () { super (); }
  public SmallJavaValidationException (String msg) { super (msg); }
  public SmallJavaValidationException (String msg, Throwable ex) 
  { super (msg, ex); }
}
