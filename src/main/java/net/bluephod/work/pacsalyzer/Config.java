package net.bluephod.work.pacsalyzer;

import java.nio.file.Path;
import java.time.LocalDate;

import lombok.Getter;

@Getter
public class Config {
	private Path csvFile;
	private int hoursPerDay;
	private LocalDate startDate;
	private LocalDate endDate;

	public Config(final Path csvFile, final int hoursPerDay, final LocalDate startDate, final LocalDate endDate) {
		this.csvFile = csvFile;
		this.hoursPerDay = hoursPerDay;
		this.startDate = startDate;
		this.endDate = endDate;
	}
}
