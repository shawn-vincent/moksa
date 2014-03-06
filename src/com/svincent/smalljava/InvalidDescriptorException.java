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
 * InvalidDescriptorException.java
 * 
 */
package com.svincent.smalljava;

import com.svincent.util.NestableException;

/**
 * <p>Thrown when parsing a type descriptor goes badly.  The index of 
 * the character which caused the parsing to fail is given with the
 * instruction.</p>
 *
 * <p>This is a NestableException, which means that it can contain 
 * a subexception, if some other exception was thrown that caused this
 * one.</p>
 *
 * @see NestableException
 * @author <a href="http://www.svincent.com/shawn/">Shawn Vincent</a>
 */
public class InvalidDescriptorException extends SmallJavaBuildingException {
  int pos;

  public InvalidDescriptorException () { super (); }
  public InvalidDescriptorException (String msg, int _pos) 
  { super (msg); pos = _pos; }
  public InvalidDescriptorException (String msg, Throwable ex, int _pos) 
  { super (msg, ex); pos = _pos; }

  public int getErrorOffset () { return pos; }

  public String getMessage ()
  { return super.getMessage () + " (pos "+pos+")"; }
}
