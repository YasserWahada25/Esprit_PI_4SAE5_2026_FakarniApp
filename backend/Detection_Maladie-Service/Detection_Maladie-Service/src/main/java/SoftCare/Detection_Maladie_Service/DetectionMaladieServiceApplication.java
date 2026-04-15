package SoftCare.Detection_Maladie_Service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class DetectionMaladieServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DetectionMaladieServiceApplication.class, args);
	}
}