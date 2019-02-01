package net.bluephod.work.pacsalyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class Pacsalyzer {
	public static void main(String... args) throws IOException, ParseException {
		if(CollectionUtils.size(args) != 5) {
			usage(null);
		}

		int arg = 0;

		Path csvFile = Paths.get(args[arg++]);

		if(!Files.exists(csvFile)) {
			usage(String.format("Booking file %s doesn't exist.", csvFile));
		}

		Path holidayFile = Paths.get(args[arg++]);

		if(!Files.exists(csvFile)) {
			usage(String.format("Holiday file %s doesn't exist.", holidayFile));
		}

		int hoursPerDay = 0;

		try {
			hoursPerDay = Integer.parseInt(args[arg++]);
		}
		finally {
			if(hoursPerDay < 1 || hoursPerDay > 24) {
				usage("Hours per day must be an integer between 1 and 24");
			}
		}

		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");

		LocalDate startDate = null;
		LocalDate endDate = null;
		try {
			startDate = LocalDate.parse(args[arg++], dateFormat);
			endDate = LocalDate.parse(args[arg++], dateFormat);
		}
		finally {
			if(startDate == null || endDate == null) {
				usage("Wrong date format for start or end date.");
			}
		}

		System.out.printf("Analyzing %s for date range %s-%s...%n", csvFile, args[2], args[3]);
		printResult(new Processor(new Config(csvFile, holidayFile, hoursPerDay, startDate, endDate)).process());
	}

	private static void printResult(final Result result) {
		System.out.printf("Days with bookings: %d%n", result.getBookedDays());
		System.out.println();
		System.out.printf("Vacation days:      %s%n", result.getVacationDays());
		System.out.printf("  thereof full:     %d%n", result.getFullDays().size());
		System.out.printf("  thereof half:     %d%n", result.getHalfDays().size());

		if(!result.getFullDays().isEmpty()) {
			System.out.println("\nList of full vacation days");
			result.getFullDays().forEach(date -> System.out.printf("   %s%n", date));
		}

		if(!result.getHalfDays().isEmpty()) {
			System.out.println("\nList of half vacation days");
			result.getHalfDays().forEach(date -> System.out.printf("   %s%n", date));
		}

		if(!result.getHolidays().isEmpty()) {
			System.out.println("\nList of ignored holidays in date range");
			result.getHolidays().forEach(holiday -> System.out.printf("   %s (%s)%n", holiday.getDate(), holiday.getName()));
		}
	}

	private static void usage(String error) {
		System.err.println("Usage: pacsalyzer <path to bookings csv> <path to holidays csv> <hours per day> <start date> <end date>");

		if(StringUtils.isNotBlank(error)) {
			System.err.println("\n" + error);
		}
		System.exit(1);
	}
}
