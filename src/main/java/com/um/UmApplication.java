package com.um;

import com.um.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
public class UmApplication implements CommandLineRunner {

	final FileService fileService;

	@Autowired
	public UmApplication(FileService fileService) {
		this.fileService = fileService;
	}

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(UmApplication.class);
		app.setBannerMode(Banner.Mode.OFF);
		app.run(args);
	}

	/**
	 * Для каждого файла, переданного в качестве параметра,
	 * создается отдельный поток в котором вызывается сервисный
	 * метод, обрабатывающий данный файл.
	 */
	@Override
	public void run(String... args) {
		ExecutorService es = null;
		try{
			es = Executors.newCachedThreadPool();
			if (args.length > 0) {
				for(String item : args){
					String extension = fileService.fileExtension(item.toLowerCase());
					switch (extension){
						case "csv":
							es.submit(() -> {
								try {
									fileService.processCSV(item);
								} catch (FileNotFoundException e) {
									e.getMessage();
								}
							});
							break;
						case "json":
							es.submit(() -> {
								try {
									fileService.processJSON(item);
								} catch (FileNotFoundException e) {
									e.getMessage();
								}
							});
							break;
						case "xlsx":
							es.submit(() -> fileService.processXLSX(item));
							break;
						default:
							System.out.println(item + " is wrong file name");
					}
				}
			} else {
				System.out.println("File names should have been provided on start");
			}
		} finally {
			if(es != null){
				es.shutdown();
			}
		}
	}
}