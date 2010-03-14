package net.sf.gaboto.time;

public class ImmutableTimeInstant extends TimeInstant {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3583249724989158992L;

	public ImmutableTimeInstant(Integer year, Integer month, Integer day) {
		super.setStartYear(year);
		super.setStartMonth(month);
		super.setStartDay(day);
	}
	
	public ImmutableTimeInstant(String time) {
		if (time.equals("dooms-day")) {
			super.setStartYear(TimeSpan.DOOMS_DAY.startYear);
			super.setStartMonth(TimeSpan.DOOMS_DAY.startMonth);
			super.setStartDay(TimeSpan.DOOMS_DAY.startDay);
		} else if (time.equals("big-bang")) {
			super.setStartYear(TimeSpan.BIG_BANG.startYear);
			super.setStartMonth(TimeSpan.BIG_BANG.startMonth);
			super.setStartDay(TimeSpan.BIG_BANG.startDay);
		} else {
			String[] bits = time.split("-");
			if (bits.length == 1)
				super.setStartYear(new Integer(bits[0]));
			else if (bits.length == 2) {
				super.setStartYear(new Integer(bits[0]));
				super.setStartMonth(new Integer(bits[1])-1);
			} else if (bits.length == 3) {
				super.setStartYear(new Integer(bits[0]));
				super.setStartMonth(new Integer(bits[1])-1);
				super.setStartDay(new Integer(bits[2]));
			} else throw new IllegalArgumentException(time);
		}
		
		if (time.equals("2010-03-02"))
			System.out.println("Bar");
	}

	@Override
	public void setStartYear(Integer startYear) {
		throw new IllegalStateException("You can't modify an immutable object");
	}
	@Override
	public void setStartMonth(Integer startMonth) {
		throw new IllegalStateException("You can't modify an immutable object");
	}
	@Override
	public void setStartDay(Integer startDay) {
		throw new IllegalStateException("You can't modify an immutable object");
	}
	

	
}
