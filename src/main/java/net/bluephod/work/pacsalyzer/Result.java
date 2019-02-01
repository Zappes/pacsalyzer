package net.bluephod.work.pacsalyzer;

import java.time.LocalDate;
import java.util.List;

import lombok.Getter;

@Getter
public class Result {
	private final double vacationDays;
	private final int bookedDays;
	private final List<LocalDate> fullDays;
	private final List<LocalDate> halfDays;

	public Result(final double vacationDays, final int bookedDays, final List<LocalDate> fullDays,
		final List<LocalDate> halfDays) {
		this.vacationDays = vacationDays;
		this.bookedDays = bookedDays;
		this.fullDays = fullDays;
		this.halfDays = halfDays;
	}
}
