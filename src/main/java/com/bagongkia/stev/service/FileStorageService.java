package com.bagongkia.stev.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jdk.internal.org.jline.utils.Log;

@Service
public class FileStorageService {
	
	@Autowired
	private ResourceLoader resourceLoader;

	public void storeFile(MultipartFile file, String fileName) throws IOException {
		Path path = Paths.get("upload").toAbsolutePath().normalize();
		
		Files.createDirectories(path);
		Files.copy(file.getInputStream(), path.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
	}

	public Resource getFile(String fileName) {
		return resourceLoader.getResource("file:download/" + fileName);
	}
	
	public Map<String, String> getConfig() throws IOException {
		InputStream resource = resourceLoader.getResource("file:report.properties").getInputStream();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource))) {
			return reader.lines()
					.map(str -> str.split("="))
					.filter(str -> str.length > 1)
					.collect(Collectors.toMap(str -> str[0], str -> str[1]));
		}
	}
	
}