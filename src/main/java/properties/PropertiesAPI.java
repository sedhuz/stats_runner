package properties;

import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class PropertiesAPI {

	private static final String PROPERTIES_FILE = PROPERTIES.PROPERTIES_FILE;

	private static final Properties properties = new Properties();

	static {
		try (InputStream input = PropertiesAPI.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
			if (input == null) {
				throw new RuntimeException("Unable to find properties file: " + PROPERTIES_FILE);
			}
			properties.load(input);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load properties file: " + PROPERTIES_FILE, e);
		}
	}

	// ------------ Public Getter Methods ------------

	public static String getProperty(String key) {
		return properties.getProperty(key);
	}

	public static String getProperty(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}

	// ------------- Convenience Methods -------------

	public static String getDBUsername() {
		return getProperty(PROPERTIES.DB_USERNAME);
	}

	public static String getDBPassword() {
		return getProperty(PROPERTIES.DB_PASSWORD);
	}

	public static String[] getDBHosts() {
		return getProperty(PROPERTIES.DB_HOSTS).split(",");
	}

	public static String getSkippedDBsRegex() {
		return getProperty(PROPERTIES.DB_SKIPPED_DBS_REGEX);
	}

	public static Integer getMaxThreads() {
		return Integer.parseInt(getProperty(PROPERTIES.APP_THREADS, "6"));
	}

	public static Boolean isParallelEnabled() {
		return Boolean.parseBoolean(getProperty(PROPERTIES.APP_PARALLEL_ENABLED, "false"));
	}

	public static String getLogDir() {
		return getProperty(PROPERTIES.LOG_DIR);
	}

	public static DateTimeFormatter getLogDateTimeFormatter() {
		String pattern = getProperty(PROPERTIES.LOG_DATE_TIME_FORMAT, "yyyy-MM-dd HH:mm:ss");
		return DateTimeFormatter.ofPattern(pattern);
	}

	public static DateTimeFormatter getLogDateTimeFormatterForFile() {
		String pattern = getProperty(PROPERTIES.LOG_DATE_TIME_FORMAT_FOR_FILE, "yyyy-MM-dd HH:mm:ss");
		return DateTimeFormatter.ofPattern(pattern);
	}

	public static String getInputFile() {
		return getProperty(PROPERTIES.INPUT_FILE);
	}

	public static String getOutputDir() {
		return getProperty(PROPERTIES.OUTPUT_DIR);
	}

	public static DateTimeFormatter getOutputDateTimeFormatterForFile() {
		String pattern = getProperty(PROPERTIES.OUTPUT_DATE_TIME_FORMAT_FOR_FILE, "yyyy-MM-dd HH:mm:ss");
		return DateTimeFormatter.ofPattern(pattern);
	}
}