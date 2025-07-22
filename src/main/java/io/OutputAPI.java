package io;

import properties.PropertiesAPI;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class OutputAPI {

	private static final String OUTPUT_DIR = PropertiesAPI.getOutputDir();
	private static final DateTimeFormatter FILE_DATE_FORMAT = PropertiesAPI.getLogDateTimeFormatterForFile();
	private final Path filePath;

	public OutputAPI() {
		String timestamp = LocalDateTime.now().format(FILE_DATE_FORMAT);
		String fileName = "result_" + timestamp + ".csv";
		try {
			Path outDirPath = Paths.get(OUTPUT_DIR);
			Files.createDirectories(outDirPath);
			this.filePath = outDirPath.resolve(fileName);
		} catch (IOException e) {
			throw new UncheckedIOException("fatal : failed to create output dir", e);
		}
	}

	public synchronized void writeResultToCSV(List<String> headers, List<String[]> newRows) {
		boolean writeHeaders = !Files.exists(filePath);

		try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
			if (writeHeaders) {
				writer.write(String.join(",", headers));
				writer.newLine();
			}
			for (String[] row : newRows) {
				writer.write(String.join(",", escape(row)));
				writer.newLine();
			}
		} catch (IOException e) {
			throw new UncheckedIOException("fatal : failed to append to CSV output", e);
		}
	}

	private List<String> escape(String[] row) {
		List<String> escaped = new ArrayList<>(row.length);
		for (String col : row) {
			if (col == null) col = "";
			if (col.contains(",") || col.contains("\"")) {
				col = "\"" + col.replace("\"", "\"\"") + "\"";
			}
			escaped.add(col);
		}
		return escaped;
	}
}