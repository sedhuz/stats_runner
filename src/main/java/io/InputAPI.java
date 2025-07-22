package io;

import properties.PropertiesAPI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class InputAPI
{
	public static String getQuery() {
		String filePath = PropertiesAPI.getInputFile();
		try {
			return new String(Files.readAllBytes(Paths.get(filePath))).trim();
		} catch (IOException e) {
			throw new RuntimeException("fatal: failed to read query from file: " + filePath, e);
		}
	}
}