package com.bagongkia.stev.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
}