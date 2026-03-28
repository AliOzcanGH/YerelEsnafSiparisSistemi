package com.esnaf.yerel_esnaf_siparis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = "com.esnaf.yerel_esnaf_siparis.repository")
@SpringBootApplication
public class YerelEsnafSiparisApplication {

	public static void main(String[] args) {
		SpringApplication.run(YerelEsnafSiparisApplication.class, args);
	}

}
