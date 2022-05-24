package fr.catlean.delivery.processor.bootstrap;

import fr.catlean.delivery.processor.bootstrap.configuration.DomainConfiguration;
import fr.catlean.delivery.processor.domain.service.DeliveryCommand;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableConfigurationProperties
@Import(value = {DomainConfiguration.class})
public class CatleanDeliveryProcessorApplication implements CommandLineRunner {

  private final DeliveryCommand deliveryCommand;

  public CatleanDeliveryProcessorApplication(DeliveryCommand deliveryCommand) {
    this.deliveryCommand = deliveryCommand;
  }

  public static void main(String[] args) {
    SpringApplication.run(CatleanDeliveryProcessorApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    deliveryCommand.collectRepositoriesForOrganisation("armis-paris");
    System.exit(0);
  }
}
