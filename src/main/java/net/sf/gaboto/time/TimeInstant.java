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

import java.util.Calendar;

/**
 * A Java Bean to work with time instants.
 * 
 * @author Arno Mittelbach
 * @version 0.1
 */
public class TimeInstant extends TimeSpan implements Comparable<TimeInstant> {

  private static final long serialVersionUID = 1460039890377361871L;

  /**
   * Create an unspecified time instant.
   */
  public TimeInstant() {
  }

  /**
   * Create a fully specified time instant.
   * 
   * @param year
   *          The year.
   * @param month
   *          The month.
   * @param day
   *          The day.
   */
  public TimeInstant(Integer year, Integer month, Integer day) {
    setStartYear(year);
    setStartMonth(month);
    setStartDay(day);
  }

  /**
   * @param time YYYY, YYYY-MM or YYYY-MM-DD
   */
  public TimeInstant(String time) {
    String[] bits = time.split("-");
    if (bits.length == 1)
      setStartYear(new Integer(bits[0]));
    else if (bits.length == 2) {
      setStartYear(new Integer(bits[0]));
      setStartMonth(new Integer(bits[1])-1);
    } else if (bits.length == 3) {
      setStartYear(new Integer(bits[0]));
      setStartMonth(new Integer(bits[1])-1);
      setStartDay(new Integer(bits[2]));
    } else throw new IllegalArgumentException(time);
    
  }

  /**
   * Create an instance from a Calendar..
   * 
   * @param calendar
   */
  public static TimeInstant from(Calendar calendar) {
    return new TimeInstant(calendar.get(Calendar.YEAR), 
            calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
  }

  public static TimeInstant oneYearOn(TimeInstant in) { 
    TimeInstant out = new TimeInstant(in.getStartYear() + 1, in.getStartMonth(), in.getStartDay());
    if (out.getStartMonth() == null)
      out.setStartMonth(0);
    if (out.getStartDay() == null)
      out.setStartDay(1);
    else 
      out.setStartDay(out.getStartDay());
    return out;
  }
  /**
   * Creates a time instant representing now.
   * 
   * @return A time instant representing now.
   */
  public static TimeInstant now() {
    return from(Calendar.getInstance());
  }

  @Override
  public Integer getDurationDay() {
    return 0;
  }

  @Override
  public void setDurationDay(Integer durationDay) {
    throw new IllegalStateException("Time instants do not have a duration.");
  }

  @Override
  public Integer getDurationMonth() {
    return 0;
  }

  @Override
  public void setDurationMonth(Integer durationMonth) {
    throw new IllegalStateException("Time instants do not have a duration.");
  }

  @Override
  public Integer getDurationYear() {
    return 0;
  }

  @Override
  public void setDurationYear(Integer durationYear) {
    throw new IllegalStateException("Time instants do not have a duration.");
  }

  @Override
  public boolean hasFixedDuration() {
    throw new IllegalStateException("Time instants do not have a duration.");
  }

  @Override
  public boolean contains(TimeSpan ts) {
    if (ts instanceof TimeInstant) {
      return ts.equals(this);
    }
    return false;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof TimeInstant))
      return false;

    TimeInstant ti = (TimeInstant) obj;

    return (this.getStartYear() == ti.getStartYear())
        && (this.getStartMonth() == ti.getStartMonth())
        && (this.getStartDay() == ti.getStartDay());
  }

  /**
   * With non-zero based months.
   * @see net.sf.gaboto.time.TimeSpan#toString()
   */
  @Override
  public String toString() {
    if (this.equals(TimeSpan.BIG_BANG))
      return "big-bang";
    if (this.equals(TimeSpan.DOOMS_DAY))
      return "dooms-day";

    String s = "";

    s += startYear;
    if (startMonth != null)
      s += "-" + (startMonth + 1);
    if (startDay != null)
      s += "-" + startDay;

    return s;
  }

  /**
   * Tests if an instant is roughly the same .. for example 300-3-18 is
   * roughly the same as 300-null-null
   * 
   * @param ti
   *          The time instant to test this instant against.
   * 
   * @return True, if they are roughly the same.
   */
  public boolean canUnify(TimeInstant ti) {
    if (this.startYear.equals(ti.getStartYear())) {
      if (this.getStartMonth() == null || ti.getStartMonth() == null) {
        return true;
      } else if (this.getStartMonth().equals(ti.getStartMonth())) {
        if (this.getStartDay() == null || ti.getStartDay() == null) { 
          System.err.println("Can unify");
          return true;
        } else if (this.getStartDay().equals(ti.getStartDay())) { 
          System.err.println("Can unify");
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Compares this time instant to another.
   * 
   * @param ti
   *          The other time instant.
   * 
   * @return -1, if this instant is earlier. 1, if it is later. 0 else.
   */
  public int compareTo(TimeInstant other) {
	  if (this.startYear < other.getStartYear()) return -1;
	  else if (this.startYear > other.getStartYear()) return 1;

	  int thisMonth = this.getStartMonth() != null ? this.getStartMonth() : 0;
	  int otherMonth = other.getStartMonth() != null ? other.getStartMonth() : 0;
	  if (thisMonth < otherMonth) return -1;
	  else if (thisMonth > otherMonth) return 1;

	  int thisDay = this.getStartDay() != null ? this.getStartDay() : 0;
	  int otherDay = other.getStartDay() != null ? other.getStartDay() : 0;
	  if (thisDay < otherDay) return -1;
	  else if (thisDay > otherDay) return 1;

	  return 0;
  }

  public int hashCode() {
	  int data[] = {this.startYear, this.startMonth, this.startDay};
	  return data.hashCode();
  }
  
	public TimeInstant clone() {
		return new TimeInstant(
				this.getStartYear(),
				this.getStartMonth(),
				this.getStartDay()
		);
	}

}
