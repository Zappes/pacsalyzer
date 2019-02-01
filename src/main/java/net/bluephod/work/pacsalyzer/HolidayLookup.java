package net.bluephod.work.pacsalyzer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.Getter;

/**
 * Lookup for holidays to be excluded.
 * <p>
 * This class is specifically crafted for parsing the CSV export format you get at feiertage.net -
 * adapting it to other formats should be an easy thing to do.
 */
public class HolidayLookup {
	private static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

	private final Config config;
	private final Map<LocalDate, Holiday> holidays = new HashMap<>();

	/**
	 * Creates an instance and initializes the holiday list.
	 *
	 * @param config The configuration that contains the path to the holidays file.
	 *
	 * @throws IOException
	 */
	public HolidayLookup(final Config config) throws IOException {
		this.config = config;

		CsvMapper mapper = new CsvMapper();
		mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
		CsvSchema schema = CsvSchema.builder()
			.setColumnSeparator(';')
			.setSkipFirstDataRow(true)
			.build();

		MappingIterator<String[]> it = mapper
			.readerFor(String[].class)
			.with(schema)
			.readValues(config.getHolidayFile().toFile());

		while(it.hasNext()) {
			String[] line = it.next();

			Holiday holiday = new Holiday(LocalDate.parse(line[0], dateFormatter), line[1].trim());

			holidays.put(holiday.getDate(), holiday);
		}
	}

	public Optional<Holiday> getHoliday(LocalDate date) {
		return Optional.ofNullable(holidays.get(date));
	}

	@Getter
	public static class Holiday {
		private final LocalDate date;
		private final String name;

		public Holiday(final LocalDate date, final String name) {
			this.date = date;
			this.name = name;
		}
	}
}
