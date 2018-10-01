package gov.nara.opa.api.utils;

import java.io.IOException;

import gov.nara.opa.api.services.moderator.BackgroundImageService;
import gov.nara.opa.architecture.logging.OpaLogger;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LoadDefaultBackgroundImages implements InitializingBean {

	@Value("${defaultBackgroundImages}")
	String defaultBackgroundImages;

	@Autowired
	private BackgroundImageService backgroundImageService;

	OpaLogger logger = OpaLogger.getLogger(LoadDefaultBackgroundImages.class);

	@Override
	public void afterPropertiesSet() throws IOException {
		logger.info("Loading default background images");
		try {
			backgroundImageService
					.loadDefaultBackgroundImages(defaultBackgroundImages);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}
