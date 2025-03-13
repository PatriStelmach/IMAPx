package Patri.Stelmach.demo;

import Patri.Stelmach.demo.Frontend.EmailClientApp;
import javafx.application.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties()
public class ImaPxApplication {

	public static void main(String[] args)
	{
		ConfigurableApplicationContext context = new SpringApplicationBuilder(ImaPxApplication.class)
				.headless(false)
				.run(args);


		Application.launch(EmailClientApp.class, args);
	}


}
