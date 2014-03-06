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
 * PrologLoader.java
 * XXX maybe rename to PrologInit or PrologFile or something....
 */
package com.svincent.moksa;

import com.svincent.util.*;

/**
 * <p>Superclass of the Loader generated for each prolog file compiled
 * by MoksaProlog. </p>
 *
 * <p>Subclasses are responsible for defining a no-arg constructor
 * which defines the source prolog file name, plus a <code>load</code>
 * method which installs all the rule definitions and executes all the
 * queries defined by this Prolog file. </p>
 **/
public abstract class PrologLoader extends WamObject {
  /** The filename of the compiled Prolog file. */
  String prologFileName = null;

  /** Creates a new Loader object. */
//    public PrologLoader (String _prologFileName)
//    { prologFileName = _prologFileName; }

  /**
   * Loads all the compiled Rule classes defined by this Loader, and
   * executes all the appropriate queries, also.
   **/
  public void load (PrologEngine engine) throws PrologException
  {
    Util.assertTrue (false, 
                 "Internal Error: Compiled Prolog file "+
                 getClass ().getName ()+
                 " does not override 'load'");
  }
}
