
package com.svincent.calendar;

import java.text.*;
import java.util.*;

import com.svincent.util.*;

public class CalendarUtils extends BaseObject {

  private CalendarUtils () {}

  public static final int Rat=4, Ox=5, Tiger=6, Rabbit=7, Dragon=8,
    Snake=9, Horse=10, Goat=11, Monkey=0, Rooster=1, Dog=2, Pig=3;

  public static int chineseCalendarYear (Date date)
  {
    Calendar c = Calendar.getInstance ();
    c.setTime (date);
    int year = c.get (Calendar.YEAR);

    return year % 12;
  }

  public static String chineseCalendarYearName (int ccyNumber)
  {
    switch (ccyNumber)
      {
      case Rat: return "rat";
      case Ox: return "ox";
      case Tiger: return "tiger";
      case Rabbit: return "rabbit";
      case Dragon: return "dragon";
      case Snake: return "snake";
      case Horse: return "horse";
      case Goat: return "goat";
      case Monkey: return "monkey";
      case Rooster: return "rooster";
      case Dog: return "dog";
      case Pig: return "pig";
      }
    return null;
  }

  public static String modernBirthstone (Date date)
  {
    Calendar c = Calendar.getInstance (); c.setTime (date);
    int month = c.get (Calendar.MONTH);

    switch (month)
      {
      case Calendar.JANUARY:   return "garnet";
      case Calendar.FEBRUARY:  return "amethyst";
      case Calendar.MARCH:     return "aquamarine";
      case Calendar.APRIL:     return "diamond";
      case Calendar.MAY:       return "emerald";
      case Calendar.JUNE:      return "pearl";
      case Calendar.JULY:      return "ruby";
      case Calendar.AUGUST:    return "peridot";
      case Calendar.SEPTEMBER: return "sapphire";
      case Calendar.OCTOBER:   return "opal";
      case Calendar.NOVEMBER:  return "yellow topaz";
      case Calendar.DECEMBER:  return "turquoise";
      }
    return null;
  }

  public static String traditionalBirthstone (Date date)
  {
    Calendar c = Calendar.getInstance (); c.setTime (date);
    int month = c.get (Calendar.MONTH);

    switch (month)
      {
      case Calendar.JANUARY:   return "garnet";
      case Calendar.FEBRUARY:  return "amethyst";
      case Calendar.MARCH:     return "bloodstone";
      case Calendar.APRIL:     return "diamond";
      case Calendar.MAY:       return "emerald";
      case Calendar.JUNE:      return "alexandrite";
      case Calendar.JULY:      return "ruby";
      case Calendar.AUGUST:    return "sardonyx";
      case Calendar.SEPTEMBER: return "sapphire";
      case Calendar.OCTOBER:   return "tourmaline";
      case Calendar.NOVEMBER:  return "citrine";
      case Calendar.DECEMBER:  return "zircon";
      }
    return null;
  }
  public static String mysticalBirthstone (Date date)
  {
    Calendar c = Calendar.getInstance (); c.setTime (date);
    int month = c.get (Calendar.MONTH);

    switch (month)
      {
      case Calendar.JANUARY:   return "emerald";
      case Calendar.FEBRUARY:  return "bloodstone";
      case Calendar.MARCH:     return "jade";
      case Calendar.APRIL:     return "opal";
      case Calendar.MAY:       return "sapphire";
      case Calendar.JUNE:      return "moonstone";
      case Calendar.JULY:      return "ruby";
      case Calendar.AUGUST:    return "diamond";
      case Calendar.SEPTEMBER: return "agate";
      case Calendar.OCTOBER:   return "jasper";
      case Calendar.NOVEMBER:  return "pearl";
      case Calendar.DECEMBER:  return "onyx";
      }
    return null;
  }
  public static String ayurvedicBirthstone (Date date)
  {
    Calendar c = Calendar.getInstance (); c.setTime (date);
    int month = c.get (Calendar.MONTH);

    switch (month)
      {
      case Calendar.JANUARY:   return "garnet";
      case Calendar.FEBRUARY:  return "amethyst";
      case Calendar.MARCH:     return "bloodstone";
      case Calendar.APRIL:     return "diamond";
      case Calendar.MAY:       return "agate";
      case Calendar.JUNE:      return "pearl";
      case Calendar.JULY:      return "ruby";
      case Calendar.AUGUST:    return "sapphire";
      case Calendar.SEPTEMBER: return "moonstone";
      case Calendar.OCTOBER:   return "opal";
      case Calendar.NOVEMBER:  return "topaz";
      case Calendar.DECEMBER:  return "ruby";
      }
    return null;
  }

  public static void main (String[] args) throws Exception
  {
    DateFormat df = DateFormat.getDateInstance (DateFormat.LONG);
    Date d = df.parse (args[0]);

    Util.out.println (chineseCalendarYearName (chineseCalendarYear (d)));
  }
}
