package log;

import properties.PropertiesAPI;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SRLogger {

	private static final String LOG_DIR = PropertiesAPI.getLogDir();
	private static final DateTimeFormatter DATE_TIME_FORMAT = PropertiesAPI.getLogDateTimeFormatter();
	private static final DateTimeFormatter DATE_TIME_FORMAT_FOR_FILE = PropertiesAPI.getLogDateTimeFormatterForFile();

	private BufferedWriter writer;
	private LocalDateTime startTime;
	private String currentQuery;
	private long totalDurationMs = 0;
	private long totalRows = 0;

	public SRLogger() {}

	public synchronized void init(String query) {
		closeWriter();  // close previous log if any

		this.currentQuery = query;
		this.startTime = LocalDateTime.now();
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

			// Write query header
			writer.write(String.format("query execution started at: %s\n", startTime.format(DATE_TIME_FORMAT)));
			writer.write(String.format("query:\n%s\n", currentQuery));
			writer.flush();

		} catch (IOException e) {
			throw new RuntimeException("fatal: failed to open log file '" + LOG_DIR + "/" + logFileName + "'", e);
		}
	}

	public synchronized void log(String host, String db, SRLOGSTATUS status, int rows, String message, long durationMs) {
		if (status == SRLOGSTATUS.SUCCESS) {
			totalRows += rows;
			totalDurationMs += durationMs;
		}

		String line = String.format(
			"- host:%s db:%s status:%s [time:%d ms]%s\n",
			host,
			db,
			formatStatus(status, rows),
			durationMs,
			(status == SRLOGSTATUS.SKIPPED && message != null) ? " [" + message + "]" : ""
		);

		try {
			writer.write(line);
			writer.flush();
		} catch (IOException e) {
			System.err.println("fatal: failed to write log: " + e.getMessage());
		}
	}

	public synchronized void close() {
		if (writer != null) {
			try {
				writer.write(String.format("total rows: %d rows\n", totalRows));
				writer.write(String.format("total time: %d ms\n", totalDurationMs));
				writer.flush();
			} catch (IOException e) {
				System.err.println("failed to write summary to log: " + e.getMessage());
			}
		}
		closeWriter();
	}

	// ------------ Private Helpers -------------

	private String formatStatus(SRLOGSTATUS status, int rows) {
		return switch (status) {
			case SUCCESS -> String.format("success [%d rows]", rows);
			case FAILURE -> "failure";
			case SKIPPED -> "skipped";
		};
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