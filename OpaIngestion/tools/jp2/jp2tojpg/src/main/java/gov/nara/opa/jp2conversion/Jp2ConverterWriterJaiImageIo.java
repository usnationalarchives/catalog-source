package gov.nara.opa.jp2conversion;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("writerJaiImageIo")
@Scope("prototype")
public class Jp2ConverterWriterJaiImageIo extends Jp2ConverterWriterBase {

	@Override
	protected void convertFile(String oldFilePath, String newFilePath) throws IOException {
		BufferedImage image = ImageIO.read(new File(oldFilePath));
		ImageIO.write(image, outputFormat, new File(newFilePath));
		
	}

	@Override
	protected String convertGetConvertedFolderName() {
		return "\\Converted-JaiImageIo\\";
	}

}
