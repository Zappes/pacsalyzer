package net.bluephod.work.pacsalyzer;

import java.nio.file.Path;
import java.time.LocalDate;

import lombok.Getter;

@Getter
public class Config {
	private Path csvFile;
	private Path holidayFile;
	private int hoursPerDay;
	private LocalDate startDate;
	private LocalDate endDate;

	public Config(final Path csvFile, final Path holidayFile, final int hoursPerDay, final LocalDate startDate,
		final LocalDate endDate) {
		this.csvFile = csvFile;
		this.holidayFile = holidayFile;
		this.hoursPerDay = hoursPerDay;
		this.startDate = startDate;
		this.endDate = endDate;
	}
}
