package ro.editii.scriptorium;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = "ro.editii")
@EnableScheduling
@PropertySource("classpath:version.properties")
public class TextbaseServer {

	public static void main(String[] args) {
		SpringApplication.run(TextbaseServer.class, args);
	}

	private void displayBean(ApplicationContext ctxt, String beanName) {
		System.out.println(String.format("* [%s] : [%s] : [%s]", beanName, ctxt.getBean(beanName),
				ToStringBuilder.reflectionToString(ctxt.getBean(beanName)), ToStringStyle.MULTI_LINE_STYLE));
	}

 	final private static Logger LOG = LoggerFactory.getLogger(TextbaseServer.class);
}
