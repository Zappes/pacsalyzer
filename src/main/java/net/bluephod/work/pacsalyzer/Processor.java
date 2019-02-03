package net.bluephod.work.pacsalyzer;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import lombok.Getter;

public class Processor {
	private final Config config;

	public Processor(final Config config) {
		this.config = config;
	}

	public Result process() throws IOException, ParseException {
		HolidayLookup holidayLookup = new HolidayLookup(config);

		CsvMapper mapper = new CsvMapper();
		mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);

		MappingIterator<String[]> it = mapper
			.readerFor(String[].class)
			.readValues(config.getCsvFile().toFile());

		// first we read all bookings and place them in a map that gives us easy access to all the bookings for
		// a given day. We will need that later for summing up stuff.
		Map<LocalDate, List<Booking>> bookings = new HashMap<>();
		while(it.hasNext()) {
			Booking booking = new Booking(it.next());

			bookings.computeIfAbsent(booking.date, date -> new LinkedList<>()).add(booking);
		}

		// farther below, an iteration over this structure is simplified by using computeIfAbsent for every
		// day in the analysis range, so the information how many days had bookings gets harder to access
		// at that point. to keep things simple, we remember how many days there actually were right here.
		int numberOfBookedDays = bookings.size();

		// now we iterate over the entire range of days specified by the start and end date. for every day
		// that has no bookings and that's not a holiday or on a week-end, we increment the number of
		// vacation days.

		List<LocalDate> fullDays = new LinkedList<>();
		List<LocalDate> halfDays = new LinkedList<>();
		List<HolidayLookup.Holiday> holidays = new LinkedList<>();

		// this one is only needed to make the iteration a little easier as there is no beforeOrEqual method
		// on LocalDate
		LocalDate firstDayAfter = config.getEndDate().plusDays(1);
		double totalBookedOverall = 0;

		for(LocalDate date = config.getStartDate(); date.isBefore(firstDayAfter); date = date.plusDays(1)) {

			// it would be great if there was something like Map.getOrDefault(), but computeIfAbsent()
			// will do. keep in mind that after this there will be an entry (albeit with an empty list)
			// for every day including the ones without actual bookings.
			double totalBookedToday = bookings.computeIfAbsent(date, localDate -> Collections.emptyList())
				.stream()
				.mapToDouble(Booking::getHours)
				.sum();

			totalBookedOverall += totalBookedToday;

			if(date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
				// ignore the week-ends.
				continue;
			}

			if(holidayLookup.getHoliday(date).isPresent()) {
				// if this day is a holiday, insert it into the list of ignored holidays and continue with the
				// next day.
				holidays.add(holidayLookup.getHoliday(date).get());
				continue;
			}

			if(totalBookedToday < 0.25) {
				// if less than a quarter of an hour was booked, this is a vacation day.
				fullDays.add(date);
			}
			else if(totalBookedToday <= config.getHoursPerDay() / 2) {
				// if half a day or less was booked, we call it half a vacation day.
				halfDays.add(date);
			}
		}

		return new Result(fullDays.size() + (halfDays.size() * 0.5), numberOfBookedDays, totalBookedOverall, fullDays, halfDays,
			holidays);
	}

	@Getter
	private static class Booking {
		private static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
		private static NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.GERMANY);

		private LocalDate date;
		private double hours;

		public Booking(String[] csvLine) throws ParseException {
			date = LocalDate.parse(csvLine[0], dateFormatter);
			hours = numberFormat.parse(csvLine[6]).doubleValue();
		}
	}
}
