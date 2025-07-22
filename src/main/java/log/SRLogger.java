package log;

import properties.PropertiesAPI;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SRLogger {

	private static final String LOG_DIR = PropertiesAPI.getLogDir();
	private static final DateTimeFormatter DATE_TIME_FORMAT = PropertiesAPI.getLogDateTimeFormatter();
	private static final DateTimeFormatter DATE_TIME_FORMAT_FOR_FILE = PropertiesAPI.getLogDateTimeFormatterForFile();

	private BufferedWriter writer;  // not final anymore, we recreate per query
	private final List<String> resultLines = new ArrayList<>();

	private LocalDateTime startTime;
	private String currentQuery;
	private long totalDurationMs = 0;
	private long totalRows = 0;

	public SRLogger() {}

	public synchronized void init(String query) {
		writeQueryToLog();
		closeWriter();
		this.currentQuery = query;
		this.startTime = LocalDateTime.now();
		this.resultLines.clear();
		this.totalDurationMs = 0;
		this.totalRows = 0;

		String timestamp = startTime.format(DATE_TIME_FORMAT_FOR_FILE);
		String logFileName = timestamp + ".log";

		try {
			Path logDirPath = Paths.get(LOG_DIR);
			if (!Files.exists(logDirPath)) {
				Files.createDirectories(logDirPath);
			}
			Path logFilePath = logDirPath.resolve(logFileName);
			this.writer = new BufferedWriter(new FileWriter(logFilePath.toFile(), true));
		} catch (IOException e) {
			throw new RuntimeException("fatal: failed to open log file '" + LOG_DIR + "/" + logFileName + "'", e);
		}
	}

	public synchronized void log(String host, String db, boolean success, int rows, String errorMsg, long durationMs) {
		totalDurationMs += durationMs;
		totalRows += rows;

		String status = formatStatus(success, rows, errorMsg);
		String line = String.format("- [%s] [%s] %s [time: %d ms]", host, db, status, durationMs);

		resultLines.add(line);
	}

	public synchronized void close() {
		writeQueryToLog();
		closeWriter();
	}

	// ------------ Private Helper Methods -------------

	private void writeQueryToLog() {
		if (currentQuery == null || writer == null) return;

		try {
			writer.write(String.format("Query Executed At: %s\n", startTime.format(DATE_TIME_FORMAT)));
			writer.write(String.format("Query: '%s'\n", currentQuery));

			for (String line : resultLines) {
				writer.write(line + "\n");
			}

			writer.write(String.format("Total Rows: %d rows\n", totalRows));
			writer.write(String.format("Total Time: %d ms\n", totalDurationMs));
			writer.flush();
		} catch (IOException e) {
			System.err.println("Failed to write query log: " + e.getMessage());
		} finally {
			currentQuery = null;
		}
	}

	private String formatStatus(boolean success, int rows, String errorMsg) {
		if (success) {
			return String.format("success (%d rows)", rows);
		}
		return (errorMsg != null) ? String.format("failed (%s)", errorMsg) : "failed";
	}

	private void closeWriter() {
		if (writer != null) {
			try {
				writer.close();
			} catch (IOException e) {
				System.err.println("Failed to close log file: " + e.getMessage());
			} finally {
				writer = null;
			}
		}
	}
}