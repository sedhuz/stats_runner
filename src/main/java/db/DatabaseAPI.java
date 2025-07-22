package db;

import io.OutputAPI;
import log.SRLogger;
import log.SRLOGSTATUS;
import properties.PropertiesAPI;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;

public class DatabaseAPI {

	private static final String USERNAME = PropertiesAPI.getDBUsername();
	private static final String PASSWORD = PropertiesAPI.getDBPassword();
	private static final Pattern SKIP_DB_PATTERN = Pattern.compile(PropertiesAPI.getSkippedDBsRegex());
	private static final int MAX_THREADS = PropertiesAPI.getMaxThreads();

	// --- Hosts Execution ---

	public static void execute(String query) {
		SRLogger logger = new SRLogger();
		OutputAPI output = new OutputAPI();
		logger.init(query);

		for (String host : PropertiesAPI.getDBHosts()) {
			executeForHost(host, query, logger, output);
		}

		logger.close();
	}

	public static void executeParallely(String query) {
		SRLogger logger = new SRLogger();
		OutputAPI output = new OutputAPI();
		logger.init(query);

		ExecutorService hostExecutor = Executors.newFixedThreadPool(MAX_THREADS);
		for (String host : PropertiesAPI.getDBHosts()) {
			hostExecutor.submit(() -> executeForHostParallel(host, query, logger, output));
		}
		runWithTimeout(hostExecutor);
		logger.close();
	}

	// --- Host Execution ---

	private static void executeForHost(String host, String query, SRLogger logger, OutputAPI output) {
		for (String db : getDatabasesFromHost(host)) {
			if (shouldSkip(db, logger, host)) continue;
			executeForDB(host, db, query, logger, output);
		}
	}

	private static void executeForHostParallel(String host, String query, SRLogger logger, OutputAPI output) {
		ExecutorService dbExecutor = Executors.newFixedThreadPool(MAX_THREADS);
		for (String db : getDatabasesFromHost(host)) {
			if (shouldSkip(db, logger, host)) continue;
			dbExecutor.submit(() -> executeForDB(host, db, query, logger, output));
		}
		runWithTimeout(dbExecutor);
	}

	// --- DB Execution ---

	private static void executeForDB(String host, String db, String query, SRLogger logger, OutputAPI output) {
		String url = String.format("jdbc:mysql://%s/%s?useSSL=false&allowPublicKeyRetrieval=true", host, db);
		long start = System.currentTimeMillis();

		try (Connection conn = DriverManager.getConnection(url, USERNAME, PASSWORD);
			 Statement stmt = conn.createStatement()) {

			boolean hasResultSet = stmt.execute(query);
			long duration = System.currentTimeMillis() - start;

			if (hasResultSet) {
				try (ResultSet rs = stmt.getResultSet()) {
					List<String> headers = extractHeaders(rs);
					List<String[]> rows = extractRows(rs);
					output.writeResultToCSV(headers, rows);
					logger.log(host, db, SRLOGSTATUS.SUCCESS, rows.size(), null, duration);
				}
			} else {
				int updateCount = stmt.getUpdateCount();
				logger.log(host, db, SRLOGSTATUS.SUCCESS, updateCount, null, duration);
			}
		} catch (Exception e) {
			long duration = System.currentTimeMillis() - start;
			logger.log(host, db, SRLOGSTATUS.FAILURE, 0, e.getMessage(), duration);
		}
	}

	// --- Helpers ---

	private static boolean shouldSkip(String db, SRLogger logger, String host) {
		if (SKIP_DB_PATTERN.matcher(db).matches()) {
			logger.log(host, db, SRLOGSTATUS.SKIPPED, 0, "System DB", 0);
			return true;
		}
		return false;
	}

	private static List<String> getDatabasesFromHost(String host) {
		List<String> databases = new ArrayList<>();
		String url = String.format("jdbc:mysql://%s/?useSSL=false&allowPublicKeyRetrieval=true", host);

		try (Connection conn = DriverManager.getConnection(url, USERNAME, PASSWORD);
			 Statement stmt = conn.createStatement();
			 ResultSet rs = stmt.executeQuery("SHOW DATABASES")) {

			while (rs.next()) {
				databases.add(rs.getString(1));
			}
		} catch (Exception e) {
			System.err.println("fatal : failed to fetch DBs from host: " + host + " => " + e.getMessage());
		}
		return databases;
	}

	private static List<String> extractHeaders(ResultSet rs) throws SQLException {
		ResultSetMetaData meta = rs.getMetaData();
		List<String> headers = new ArrayList<>();
		for (int i = 1; i <= meta.getColumnCount(); i++) {
			headers.add(meta.getColumnLabel(i));
		}
		return headers;
	}

	private static List<String[]> extractRows(ResultSet rs) throws SQLException {
		List<String[]> rows = new ArrayList<>();
		int colCount = rs.getMetaData().getColumnCount();
		while (rs.next()) {
			String[] row = new String[colCount];
			for (int i = 1; i <= colCount; i++) {
				row[i - 1] = rs.getString(i);
			}
			rows.add(row);
		}
		return rows;
	}

	private static void runWithTimeout(ExecutorService executor) {
		executor.shutdown();
		try {
			if (!executor.awaitTermination(10, TimeUnit.MINUTES)) {
				executor.shutdownNow();
			}
		} catch (InterruptedException e) {
			executor.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}
}