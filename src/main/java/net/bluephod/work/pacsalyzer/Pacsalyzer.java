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
	public static void main(String ... args) throws IOException, ParseException {
		if(CollectionUtils.size(args) != 4) {
			usage(null);
		}

		Path csvFile = Paths.get(args[0]);

		if(!Files.exists(csvFile)) {
			usage(String.format("File %s doesn't exist.", csvFile));
		}

		int hoursPerDay = 0;

		try {
			hoursPerDay = Integer.parseInt(args[1]);
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
			startDate = LocalDate.parse(args[2], dateFormat);
			endDate = LocalDate.parse(args[3], dateFormat);
		}
		finally {
			if(startDate == null || endDate == null)
				usage("Wrong date format for start or end date.");
		}

		System.out.printf("Analyzing %s for date range %s-%s...%n", csvFile, args[2], args[3]);
		printResult(new Processor(new Config(csvFile, hoursPerDay, startDate, endDate)).process());
	}

	private static void printResult(final Result result) {
		System.out.println("Days with bookings: " + result.getBookedDays());
		System.out.println("Vacation days:      " + result.getVacationDays());
		System.out.println("  thereof full:     " + result.getFullDays().size());
		System.out.println("  thereof half:     " + result.getHalfDays().size());
		if(!result.getFullDays().isEmpty()) {
			System.out.println("List of full vacation days");
			result.getFullDays().stream().forEach(date -> {
				System.out.println("   " + date);
			});
		}
		if(!result.getHalfDays().isEmpty()) {
			System.out.println("List of half vacation days");
			result.getHalfDays().stream().forEach(date -> {
				System.out.println("   " + date);
			});
		}
	}

	private static void usage(String error) {
		System.err.println("Usage: pacsalyzer <path to csv> <hours per day> <start date> <end date>");

		if(StringUtils.isNotBlank(error)) {
			System.err.println("\n" + error);
		}
		System.exit(1);
	}
}
