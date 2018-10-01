package gov.nara.opa.jp2conversion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("writerImageMagick")
@Scope("prototype")
public class Jp2ConverterWriterImageMagick extends Jp2ConverterWriterBase {

	private static final Log log = LogFactory
			.getLog(Jp2ConverterWriterImageMagick.class);

	@Override
	protected void convertFile(String oldFilePath, String newFilePath)
			throws IOException, InterruptedException {
		StringBuffer output = new StringBuffer();
		String command = "C:\\Program Files (x86)\\ImageMagick-6.3.9-Q16\\convert.exe -quality 100 \"" + oldFilePath + "\" \""
				+ newFilePath + "\"";
		java.lang.ProcessBuilder pb = new java.lang.ProcessBuilder(command);
		Process p = pb.start();
		p.waitFor();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				p.getErrorStream()));

		String line = "";
		while ((line = reader.readLine()) != null) {
			output.append(line + "\n");
		}
		if (output.length() > 0){
			log.error(output);
		}
	}

	/*
	 * @Override protected void convertFile(String oldFilePath, String
	 * newFilePath) throws IOException, MagickException { //oldFilePath=
	 * "C:\\Dev\\Data\\NARA\\JP2 Files\\ConvTest\\Expanded\\Folder1\\0016.jp2";
	 * try { ImageInfo imageInfo = new ImageInfo(oldFilePath); MagickImage
	 * magickConverter = new MagickImage(imageInfo);
	 * magickConverter.setFileName(newFilePath);
	 * magickConverter.writeImage(imageInfo); } catch (MagickException ex){ if
	 * (ex.getMessage().equals("No image to set file name")){
	 * log.info("Could not convert file: " + oldFilePath); } else { throw ex; }
	 * }
	 * 
	 * }
	 */

	@Override
	protected String convertGetConvertedFolderName() {
		return "\\Converted-ImageMagick\\";
	}

}
