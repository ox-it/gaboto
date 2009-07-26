/**
 * Copyright 2009 University of Oxford
 *
 * Written by Arno Mittelbach for the Erewhon Project
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution.
 *  - Neither the name of the University of Oxford nor the names of its 
 *    contributors may be used to endorse or promote products derived from this 
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.sf.gaboto.time;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;

import net.sf.gaboto.model.Gaboto;
import net.sf.gaboto.model.NoTimeIndexSetException;
import net.sf.gaboto.util.GabotoPredefinedQueries;


import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * A Java Bean implementing time spans (up to resolution of days).
 * 
 * <p>
 * Be careful, this class returns Integer objects instead of int primitive
 * types. ts1.getStartYear() == t2.getStartYear() will therefore not work!
 * </p>
 * 
 * @author Arno Mittelbach
 * @version 0.1
 * 
 */
public class TimeSpan implements Serializable {

  /**
	 * 
	 */
  private static final long serialVersionUID = 1808387080829575007L;

  public static final int START_UNIT_YEAR = 1;
  public static final int START_UNIT_MONTH = 2;
  public static final int START_UNIT_DAY = 3;

  /* 1 - 31 */
  protected Integer startDay;
  /* 0 - 11 */
  protected Integer startMonth;
  protected Integer startYear;

  private Integer durationDay;
  private Integer durationMonth;
  private Integer durationYear;

  /**
   * You could also refer to this as the beginning of time.
   * 
   * Integer.MIN_INT does not work since the duration is also represented as an integer.
   * This might be changed at some point to use longs instead of int.
   */
  public static final TimeInstant BIG_BANG = new TimeInstant(-100000000, 0, 1);

  /**
   * You could also refer to this as the end of time or the big crunch ..
   * 
   * Integer.MAXINT does not work since the duration is also represented as an integer.
   * This might be changed at some point to use longs instead of integers.
   */
  public static final TimeInstant DOOMS_DAY = new TimeInstant(100000000, 11, 31);

  /**
   * Describes the time span from BIG_BANG to DOOMS_DAY.
   * 
   * @see TimeSpan#BIG_BANG
   * @see TimeSpan#DOOMS_DAY
   */
  public static final TimeSpan EXISTENCE = createFromInstants(TimeSpan.BIG_BANG, TimeSpan.DOOMS_DAY);

  /**
   * Creates an empty time span
   */
  public TimeSpan() {
  }

  /**
   * Creates a unbound time span with a specified start date.
   * 
   * @param startYear
   *          The start year.
   * @param startMonth
   *          The start month.
   * @param startDay
   *          The start day.
   */
  public TimeSpan(Integer startYear, Integer startMonth, Integer startDay) {
    setStartYear(startYear);
    setStartMonth(startMonth);
    setStartDay(startDay);
  }

  /**
   * Creates a fully specified time span.
   * 
   * @param startYear
   *          The start year.
   * @param startMonth
   *          The start month.
   * @param startDay
   *          The start day.
   * @param durationYear
   *          The duration (in years).
   * @param durationMonth
   *          The duration (in months).
   * @param durationDay
   *          The duration (in days).
   */
  public TimeSpan(Integer startYear, Integer startMonth, Integer startDay,
      Integer durationYear, Integer durationMonth, Integer durationDay) {
    setStartYear(startYear);
    setStartMonth(startMonth);
    setStartDay(startDay);
    setDurationYear(durationYear);
    setDurationMonth(durationMonth);
    setDurationDay(durationDay);
  }

  /**
   * Creates a time span from a graph name.
   * 
   * @param graphName
   *          The graph's name
   * @param gaboto
   *          The Gaboto system that contains the graph
   * 
   * @return The new TimeSpan
   */
  public static TimeSpan createFromGraphName(String graphName, Gaboto gaboto) {
    if (graphName.equals(gaboto.getGlobalKnowledgeGraph().getGraphName().getURI()))
      return TimeSpan.EXISTENCE;

    try {
      return gaboto.getTimeDimensionIndexer().getTimeSpanFor(graphName);
    } catch (NoTimeIndexSetException e) {

      String query = GabotoPredefinedQueries.getTimeInformationQuery(graphName);

      QueryExecution qe = QueryExecutionFactory.create(query, gaboto
          .getContextDescriptionGraph());
      ResultSet rs = qe.execSelect();

      if (rs.hasNext()) {
        QuerySolution qs = rs.nextSolution();

        // get variables
        RDFNode startYearNode = qs.get("beginDescYear");
        RDFNode startMonthNode = qs.get("beginDescMonth");
        RDFNode startDayNode = qs.get("beginDescDay");
        RDFNode durationYearNode = qs.get("durationYears");
        RDFNode durationMonthNode = qs.get("durationMonths");
        RDFNode durationDayNode = qs.get("durationDays");

        // extract timespan
        TimeSpan ts = new TimeSpan();
        ts.setStartYear(((Literal) startYearNode).getInt());
        if (startMonthNode != null)
          ts.setStartMonth(((Literal) startMonthNode).getInt());
        if (startDayNode != null)
          ts.setStartDay(((Literal) startDayNode).getInt());

        if (durationYearNode != null)
          ts.setDurationYear(((Literal) durationYearNode).getInt());
        if (durationMonthNode != null)
          ts.setDurationMonth(((Literal) durationMonthNode).getInt());
        if (durationDayNode != null)
          ts.setDurationDay(((Literal) durationDayNode).getInt());

        return ts;
      }

      return null;
    }
  }

  /**
   * Creates a time span from two time instants.
   * 
   * @param begin
   *          The beginning.
   * @param end
   *          The end.
   * 
   * @return The time span.
   */
  public static TimeSpan createFromInstants(TimeInstant begin, TimeInstant end) {
    if (begin.canUnify(end)) 
      // FIXME WTF looks like a hack
      return createFromInstants(begin, TimeInstant.oneYearOn(begin));
    else if (begin.compareTo(end) != -1)  
      throw new IllegalArgumentException(
          "Begin has to be earlier than end. begin: " + begin + ", end: " + end  );

    if (begin.startMonth == null)
      end.setStartMonth(null);  // WTF ???
    if (begin.startMonth != null && end.startMonth == null)
      end.setStartMonth(1);
    if (begin.startDay == null)
      end.setStartDay(null);
    if (begin.startDay != null && end.startDay == null)
      end.setStartDay(1);

    // everything seems ok

    TimeSpan ts = new TimeSpan(begin.startYear, begin.startMonth, begin.startDay);

    // if latest == big crunch, it is easy
    if (end.equals(TimeSpan.DOOMS_DAY))
      return ts;

    // calculate duration

    Integer durationDays = null, durationMonths = null, durationYears = 0;

    boolean dayOverflow = false, monthOverflow = false;

    // years

    if (end.startDay != null) {
      if (end.startDay < begin.startDay) {
        int daysInMonth = 0;
        if (end.startMonth > 1)
          daysInMonth = getDaysInMonth(end.startYear, end.startMonth - 1);
        else
          daysInMonth = getDaysInMonth(end.startYear - 1, 11);

        durationDays = (daysInMonth - begin.startDay + end.startDay) % daysInMonth;

        if (durationDays >= 31)
          throw new RuntimeException("Bug in time arithetic");

        dayOverflow = true;
      } else {
        durationDays = end.startDay - begin.startDay;
      }
    }

    if (end.startMonth != null) {
      int endStartMonth = end.startMonth;
      if (dayOverflow)
        endStartMonth--;
      if (endStartMonth < begin.startMonth) {
        durationMonths = (12 - begin.startMonth + endStartMonth);
        monthOverflow = true;
      } else
        durationMonths = endStartMonth - begin.startMonth;
    }

    // years
    durationYears = end.startYear - begin.startYear - (monthOverflow ? 1 : 0);

    ts.setDurationDay(durationDays);
    ts.setDurationMonth(durationMonths);
    ts.setDurationYear(durationYears);

    return ts;
  }

  /**
   * Returns a canonicalized version of this time span.
   * 
   * <p>
   * Due to overlaps different looking time spans might actually represent the
   * same time span. This method returns a canonicalized representation of these
   * time spans.
   * </p>
   * 
   * @return The canonicalized version of this time span.
   */
  public TimeSpan canonicalize() {
    if (hasFixedDuration())
      return TimeSpan.createFromInstants(getBegin(), getEnd());
    return this;
  }

  /**
   * Returns the starting point (day).
   * 
   * @return The starting point (day).
   */
  public Integer getStartDay() {
    return startDay;
  }

  /**
   * Sets the starting point (day).
   * 
   * @param startDay
   *          The start day.
   */
  public void setStartDay(Integer startDay) {
    if (startDay !=  null && startDay > 31)
      throw new IllegalArgumentException(
          "There is no month with more than 31 days, argument was " + startDay);
    if (startDay !=  null && startDay < 1)
      throw new IllegalArgumentException(
          "Days start with day 1 : " + startDay);
    this.startDay = startDay;
  }

  /**
   * Returns the starting point (month).
   * 
   * @return The starting point (month).
   */
  public Integer getStartMonth() {
    return startMonth;
  }

  /**
   * Sets the starting point (month).
   * 
   * @param startMonth
   *          The start month.
   */
  public void setStartMonth(Integer startMonth) {
    if (startMonth != null && startMonth.intValue() > 11)
      throw new IllegalArgumentException(
          "There is no year with more than 12 months, zero based, argument was "
              + startMonth + ".");
    if (startMonth != null && startMonth.intValue() < 0)
      throw new IllegalArgumentException(
          "Month number: "
              + startMonth);
    this.startMonth = startMonth;
  }

  /**
   * Returns the starting point (year).
   * 
   * @return The starting point (year).
   */
  public Integer getStartYear() {
    return startYear;
  }

  /**
   * Sets the starting point (year).
   * 
   * @param startYear
   *          The start year.
   */
  public void setStartYear(Integer startYear) {
    this.startYear = startYear;
  }

  /**
   * Returns the resolution of this time span.
   * 
   * @return The resolution.
   */
  public int getStartUnit() {
    if (startDay != null)
      return START_UNIT_DAY;
    else if (startMonth != null)
      return START_UNIT_MONTH;

    return START_UNIT_YEAR;
  }

  /**
   * Returns the duration of days.
   * 
   * @return The number of days.
   */
  public Integer getDurationDay() {
    return durationDay == null ? 0 : durationDay;
  }

  /**
   * Sets the duration of days.
   * 
   * @param durationDay
   *          The number of days.
   */
  public void setDurationDay(Integer durationDay) {
    if (durationDay  != null && durationDay >= 31)
      throw new IllegalArgumentException(
          "There is no month on earth that has more than 31 days. You might just want to add an extra month");

    if (durationDay == null)
      this.durationDay = null;
    else 
      this.durationDay = durationDay == 0 ? null : durationDay;
  }

  /**
   * Returns the duration of months.
   * 
   * @return The number of months.
   */
  public Integer getDurationMonth() {
    return durationMonth == null ? 0 : durationMonth;
  }

  /**
   * Sets the duration of months
   * 
   * @param durationMonth
   *          The number of months.
   */
  public void setDurationMonth(Integer durationMonth) {
    if (durationMonth != null && durationMonth >= 12)
      throw new IllegalArgumentException(
          "There is no year on earth that has more than 12 months. You might just want to add an extra year.");

    if (durationMonth == null)
      this.durationMonth = null;
    else
      this.durationMonth = durationMonth == 0 ? null : durationMonth;
  }

  /**
   * Returns the duration of years.
   * 
   * @return The duration of years.
   */
  public Integer getDurationYear() {
    return durationYear == null ? 0 : durationYear;
  }

  /**
   * Sets the duration of years.
   * 
   * @param durationYear
   */
  public void setDurationYear(Integer durationYear) {
    if (durationYear == null)
      this.durationYear = null;
    else
      this.durationYear = durationYear == 0 ? null : durationYear;
  }

  /**
   * Creates a time instant that corresponds to the beginning of this time span.
   * 
   * @return The corresponding time instant to the begin of this time span.
   */
  public TimeInstant getBegin() {
    return new TimeInstant(getStartYear(), getStartMonth(), getStartDay());
  }

  /**
   * Creates a time instant that corresponds to the end of this time span.
   * 
   * @return The corresponding time instant to the end of this time span.
   */
  public TimeInstant getEnd() {
    if (!hasFixedDuration())
      return TimeSpan.DOOMS_DAY;

    TimeInstant end = new TimeInstant();

    Integer newYear = getStartYear() + getDurationYear();
    Integer newMonth = null;
    Integer newDay = null;

    if (getStartMonth() != null) {
      newMonth = getStartMonth() + getDurationMonth();

      if (newMonth > 11) {
        newMonth = newMonth % 12;
        newYear++;
      }

      if (getStartDay() != null) {
        if (isMonthOverflow(newYear, newMonth, getStartDay(), getDurationDay())) {
          newMonth++;
          if (newMonth > 11) {
            newMonth = newMonth % 12;
            newYear++;
          }
          if (newMonth == Calendar.JANUARY)
            newDay = newDay(getStartDay(), getDurationDay(), getDaysInMonth(newYear, Calendar.DECEMBER));
          else
            newDay = newDay(getStartDay(), getDurationDay(), getDaysInMonth(newYear, newMonth - 1));
        } else {
          newDay = newDay(getStartDay(), getDurationDay(), getDaysInMonth(newYear, newMonth));
        }
      }
    }

    end.setStartYear(newYear);
    end.setStartMonth(newMonth);
    end.setStartDay(newDay);

    return end;
  }

  int newDay(int start, int duration, int daysInMonth) { 
    if (((start + duration) % daysInMonth) == 0)
      return daysInMonth;
    else 
      return (start + duration) % daysInMonth;
  }
  /**
   * Tests whether a given point in time falls into this time span.
   * 
   * @param ti
   *          The time instant to test.
   * @return Whether the instant falls into this time span.
   */
  public boolean contains(TimeInstant ti) {
    // test if beginning is later than this beginning
    TimeInstant beginning = this.getBegin();
    int compB = ti.compareTo(beginning);
    if (compB == 0 || ti.canUnify(beginning))
      return true;
    else if (compB < 0)
      return false;

    TimeInstant end = this.getEnd();
    int compE = ti.compareTo(end);
    if (compE <= 0 || ti.canUnify(end))
      return true;

    return false;
  }

  /**
   * Returns the number of days in a given month and year
   * 
   * @param year
   *          The year (we need that for leap years)
   * @param month
   *          (the month)
   * @return the number of days in a month and year
   */
  private static int getDaysInMonth(int year, int month) {
    int daysInMonth = 0;
    switch (month) {
    case Calendar.JANUARY:
      daysInMonth = 31;
      break;
    case Calendar.FEBRUARY:
      GregorianCalendar cal = new GregorianCalendar();
      daysInMonth = cal.isLeapYear(year) ? 29 : 28;
      break;
    case Calendar.MARCH:
      daysInMonth = 31;
      break;
    case Calendar.APRIL: 
      daysInMonth = 30;
      break;
    case Calendar.MAY:
      daysInMonth = 31;
      break;
    case Calendar.JUNE: 
      daysInMonth = 30;
      break;
    case Calendar.JULY:
      daysInMonth = 31;
      break;
    case Calendar.AUGUST:
      daysInMonth = 31;
      break;
    case Calendar.SEPTEMBER:
      daysInMonth = 30;
      break;
    case Calendar.OCTOBER: 
      daysInMonth = 31;
      break;
    case Calendar.NOVEMBER:
      daysInMonth = 30;
      break;
    case Calendar.DECEMBER:
      daysInMonth = 31;
      break;
    default:
      throw new IllegalArgumentException(month + " is not a month.");
    }

    return daysInMonth;
  }

  /**
   * Tests if there is an overflow in the months because of the days spanning
   * over the end of a month.
   * 
   * @param year
   *          The year we are talking about
   * @param month
   *          The month we are talking about
   * @param day
   *          The start day
   * @param durationDays
   *          How many days are we talking about
   * 
   * @return Whether or not we end up in the next month
   */
  private boolean isMonthOverflow(Integer year, Integer month, Integer day, Integer durationDays) {
    
    if (year == null || month == null || day == null || durationDays == null)
      return false;

    int daysInMonth = getDaysInMonth(year, month);

    if (day + durationDays >= daysInMonth)
      System.err.println(day +"+"+ durationDays +">="+ daysInMonth);
    else
      System.err.println(day +"+"+ durationDays +"<"+ daysInMonth);
    return day + durationDays >= daysInMonth;
  }

  /**
   * Test whether a time span falls into this time span.
   * 
   * @param ts
   *          The time span to be tested.
   * @return True, if the passed time span is contained by this time span.
   * 
   * @see #contains(TimeInstant)
   */
  public boolean contains(TimeSpan ts) {
    TimeInstant tsBeginning = ts.getBegin();
    TimeInstant tsEnd = ts.getEnd();

    return contains(tsBeginning) && contains(tsEnd);
  }

  /**
   * Tests whether two time spans overlap.
   * 
   * @param ts
   *          The time span to test the overlap.
   * 
   * @return True if the two time spans overlap.
   */
  public boolean overlaps(TimeSpan ts) {
    TimeInstant tsBeginning = ts.getBegin();
    TimeInstant tsEnd = ts.getEnd();

    return (!this.getBegin().equals(tsBeginning) && contains(tsBeginning))
        || (!this.getEnd().equals(tsEnd) && contains(tsEnd));
  }

  /**
   * Tests whether this time span has an upper bound.
   * 
   * @return True if it has an upper bound.
   */
  public boolean hasFixedDuration() {
    return null != durationDay || null != durationMonth || null != durationYear;
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof TimeSpan))
      return false;

    TimeSpan ts = (TimeSpan) obj;

    return this.getBegin().equals(ts.getBegin())
        && this.getEnd().equals(ts.getEnd());
  }

  @Override
  public String toString() {
    if (this.equals(TimeSpan.EXISTENCE))
      return "existence";
    String s = "";

    s += getBegin().toString();

    if (!hasFixedDuration())
      s += "~dooms-day";
    else {
      s += "~" + (durationYear == null  ? "0" : durationYear);
      s += "-" + (durationMonth == null ? "0" : durationMonth);
      s += "-" + (durationDay == null   ? "0" : durationDay);
    }

    return s;
  }
}
