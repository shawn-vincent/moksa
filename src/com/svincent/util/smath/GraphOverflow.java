
package com.svincent.util.smath;

import java.awt.*;
import javax.swing.*;

import com.svincent.util.*;

public class GraphOverflow extends JFrame {

  public static void main (String[] args)
  {
    GraphOverflow go = new GraphOverflow ();
    go.setSize (800, 600);
    go.setDefaultCloseOperation (WindowConstants.DISPOSE_ON_CLOSE);
    go.getContentPane ().add (new GraphCanvas ());
    go.show ();
  }

}

class GraphCanvas extends Canvas {


  public void paint (Graphics g)
  {
    drawGoodies (g);
  }

  public void drawGoodies (Graphics g)
  {
    int topOffset = 10;
    int leftOffset = 10;

    int max = SMath.INT_MAX;
    int root = (int)Math.sqrt (max);

    int top = 25*root;
    long steps = 600;
    int step = (int)(top/steps);
    Util.out.println ("top == "+top);
    Util.out.println ("step == "+step);
//      Util.out.print ("\t\t");
//      for (int j=0+step; j<top && j>=0; j+=step)
//        {
//  	printfancy (j);
//  	Util.out.print ('\t');
//        }
//      Util.out.println ();
    int x = 0, y = 0;

    for (int i=0; i<top && i>=0; i+=step)
      {
	x = 0;
//  	printfancy (i);
//  	Util.out.print (":\t");
	for (int j=0; j<top && j>=0; j+=step)
	  {
	    try {
	      int p = SMath.multiply (i, j);
	      //int p = i*j; 
	      drawPoint (g, p, x, y);
//  	    } catch (SMathOverflow ex) {
//  	      drawError (g, x, y);
	    } catch (Throwable ex) {
	      drawError (g, x, y);
//  	      ex.printStackTrace ();
	    }
	    x++;
	  }
	//Util.out.println ();
	y++;
      }
  }

  static void printfancy (int num)
  {
    if (num > Math.sqrt (SMath.INT_MAX))
      Util.out.print ('(');
    Util.out.print (Integer.toHexString (num));
    if (num > Math.sqrt (SMath.INT_MAX))
      Util.out.print (')');
  }

  public void drawPoint (Graphics g, int p, int x, int y)
  {
    if (p > 0)
      g.setColor (Color.blue);
    else if (p < 0)
      g.setColor (Color.yellow);
    else if (p == 0)
      g.setColor (Color.black);
    g.drawLine (x, y, x, y);
  }

  public void drawError (Graphics g, int x, int y)
  {
    g.setColor (Color.red);
    g.drawLine (x, y, x, y);
  }


}
