package gov.nara.opa.ingestion.analysis;

import com.google.common.collect.ImmutableMap;
import com.searchtechnologies.aspire.framework.utilities.StringUtilities;
import com.searchtechnologies.aspire.framework.utilities.XMLUtilities;
import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;
import com.searchtechnologies.aspire.services.Component;
import com.searchtechnologies.aspire.services.Job;
import com.searchtechnologies.aspire.services.logging.ALogger;
import java.io.File;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;

import gov.nara.opa.ingestion.*;
import org.apache.tika.metadata.Metadata;

/**
 * Technical metadata is obtained through Tika.
 * Then, it is updated in the following places:
 * 1. The digital object being processed
 * 2. The objects collection of the parent description
 * 3. The file objects.xml in opa storage
 * @author caraya
 */
public class LegacyTechnicalMetadataUpdater {
    private final Job job;
    private final Component component;
    private final ALogger logger;
    private final JobInfo jobInfo;
    private final String objectId;
    private Metadata fileMetadata;
    private File file;
    private AspireObject metadataObject;
    private String mimeType;

    private static final ImmutableMap<String,String> BMP_FIELDS;
    private static final ImmutableMap<String,String> GIF_FIELDS;
    private static final ImmutableMap<String,String> JPEG_FIELDS;
    private static final ImmutableMap<String,String> JPEG2000_FIELDS;
    private static final ImmutableMap<String,String> TIFF_FIELDS;
    private static final ImmutableMap<String,String> VIDEO_FIELDS;
    private static final ImmutableMap<String,String> MP4_FIELDS;
    private static final ImmutableMap<String,String> WMV_FIELDS;
    private static final ImmutableMap<String,String> MP3_FIELDS;
    private static final ImmutableMap<String,String> WAV_FIELDS;
    private static final ImmutableMap<String,String> PDF_FIELDS;
    private static final ImmutableMap<String,String> PLAIN_TEXT_FIELDS;
    private static final ImmutableMap<String,String> HTML_FIELDS;
    private static final ImmutableMap<String,String> POWERPOINT_FIELDS;
    private static final ImmutableMap<String,String> WORD_FIELDS;
    private static final ImmutableMap<String,String> EXCEL_FIELDS;

    private static final String SIZE_FIELD = "size";
    private static final String MIME_FIELD = "mime";
    private static final String CREATE_DATE_FIELD = "createDate";
    private static final String MODIFY_DATE_FIELD = "modifyDate";

    static{
        BMP_FIELDS = ImmutableMap.<String, String>builder()
                .put("Content-Type", MIME_FIELD)
                .put("Data BitsPerSample", "Data_BitsPerSample")
                .put("height", "height")
                .put("width", "width")
                .put("Compression CompressionTypeName", "Compression_CompressionTypeName")
                .build();

        GIF_FIELDS = ImmutableMap.<String, String>builder()
                .put("Content-Type", MIME_FIELD)
                .put("Chroma BlackIsZero", "Chroma_BlackIsZero")
                .put("Chroma ColorSpaceType", "Chroma_ColorSpaceType")
                .put("Chroma NumChannels", "Chroma_NumChannels")
                .put("Compression CompressionTypeName", "Compression_CompressionTypeName")
                .put("Compression Lossless", "Compression_Lossless")
                .put("Compression NumProgressiveScans", "Compression_NumProgressiveScans")
                .put("height", "height")
                .put("width", "width")
                .build();

        JPEG_FIELDS = ImmutableMap.<String, String>builder()
                .put("Content-Type", MIME_FIELD)
                .put("CreateDate", CREATE_DATE_FIELD)
                .put("ModifyDate", MODIFY_DATE_FIELD)
                .put("metadataDate", "metadataDate")
                .put("Image Height", "height")
                .put("Image Width", "width")
                .put("BitsPerSample", "bitsPerSample")
                .put("PhotometricInterpretation", "photometricInterpretation")
                .put("Orientation", "orientation")
                .put("SamplesPerPixel", "samplesPerPixel")
                .put("PlanarConfiguration", "planarConfiguration")
                .put("ColorSpace", "colorSpace")
                .put("Compression", "compression")
                .put("Software", "software")
                .build();

        JPEG2000_FIELDS = ImmutableMap.<String, String>builder()
                .put("Content-Type", MIME_FIELD)
                .put("CreateDate", CREATE_DATE_FIELD)
                .put("ModifyDate", MODIFY_DATE_FIELD)
                .put("metadataDate", "metadataDate")
                .put("Image Height", "height")
                .put("Image Width", "width")
                .put("BitsPerSample", "bitsPerSample")
                .put("Exposure Mode", "Exposure_Mode")
                .put("Color Space", "Color_Space")
                .put("Compression Type", "Compression_Type")
                .put("Number of Components", "Number_of_Components")
                .put("Component 1", "Component_1")
                .put("Component 2", "Component_2")
                .put("Component 3", "Component_3")
                .build();

        TIFF_FIELDS = ImmutableMap.<String, String>builder()
                .put("Content-Type", MIME_FIELD)
                .put("Creation-Date", CREATE_DATE_FIELD)
                .put("Last-Modified", MODIFY_DATE_FIELD)
                .put("Image Height", "height")
                .put("Image Width", "width")
                .put("BitsPerSample", "bitsPerSample")
                .put("Compression", "compression")
                .put("PlanarConfiguration", "planarConfiguration")
                .put("SamplesPerPixel", "samplesPerPixel")
                .put("PhotometricInterpretation", "photometricInterpretation")
                .build();

        VIDEO_FIELDS = ImmutableMap.<String, String>builder()
                .put("Content-Type", MIME_FIELD)
                .put("Creation-Date", CREATE_DATE_FIELD)
                .put("Last-Modified", MODIFY_DATE_FIELD)
                .build();

        MP4_FIELDS = ImmutableMap.<String, String>builder()
                .put("Content-Type", MIME_FIELD)
                .put("Creation-Date", CREATE_DATE_FIELD)
                .put("Last-Modified", MODIFY_DATE_FIELD)
                .put("General Format", "General_Format")
                .put("General Format profile", "General_Format_profile")
                .put("General Codec ID", "General_Codec_ID")
                .put("General Duration", "General_Duration")
                .put("General Overall bit rate mode", "General_Overall_bit_rate_mode")
                .put("General Overall bit rate", "General_Overall_bit_rate")
                .put("Video Format", "Video_Format")
                .put("Video Format Info", "Video_Format_Info")
                .put("Video Format profile", "Video_Format_profile")
                .put("Video Format settings  CABAC", "Video_Format_settings__CABAC")
                .put("Video Format settings  ReFrames", "Video_Format_settings__ReFrames")
                .put("Video Codec ID", "Video_Codec_ID")
                .put("Video Codec ID Info", "Video_Codec_ID_Info")
                .put("Video Duration", "Video_Duration")
                .put("Video Bit rate mode", "Video_Bit_rate_mode")
                .put("Video Bit rate", "Video_Bit_rate")
                .put("Video Maximum bit rate", "Video_Maximum_bit_rate")
                .put("Video Width", "Video_Width")
                .put("Video Height", "Video_Height")
                .put("Video Display aspect ratio", "Video_Display_aspect_ratio")
                .put("Video Frame rate mode", "Video_Frame_rate_mode")
                .put("Video Frame rate", "Video_Frame_rate")
                .put("Video Standard", "Video_Standard")
                .put("Video Color space", "Video_Color_space")
                .put("Video Chroma subsampling", "Video_Chroma_subsampling")
                .put("Video Bit depth", "Video_Bit_depth")
                .put("Video Scan type", "Video_Scan_type")
                .put("Video Stream size", "Video_Stream_size")
                .put("Video Language", "Video_Language")
                .put("Audio Format", "Audio_Format")
                .put("Audio Format Info", "Audio_Format_Info")
                .put("Audio Format profile", "Audio_Format_profile")
                .put("Audio Codec ID", "Audio_Codec_ID")
                .put("Audio Duration", "Audio_Duration")
                .put("Audio Bit rate mode", "Audio_Bit_rate_mode")
                .put("Audio Bit rate", "Audio_Bit_rate")
                .put("Audio Maximum bit rate", "Audio_Maximum_bit_rate")
                .put("Audio Channel s ", "Audio_Channel_s_")
                .put("Audio Channel positions", "Audio_Channel_positions")
                .put("Audio Sampling rate", "Audio_Sampling_rate")
                .put("Audio Compression mode", "Audio_Compression_mode")
                .put("Audio Stream size", "Audio_Stream_size")
                .put("Audio Language", "Audio_Language")
                .build();

        WMV_FIELDS = ImmutableMap.<String, String>builder()
                .put("Content-Type", MIME_FIELD)
                .put("General Duration String", "General_Duration_String")
                .put("General OverallBitRate Mode String", "General_OverallBitRate_Mode_String")
                .put("General OverallBitRate String", "General_OverallBitRate_String")
                .put("General OverallBitRate Maximum String", "General_OverallBitRate_Maximum_String")
                .put("Video Format", "Video_Format")
                .put("Video Format Profile", "Video_Format_Profile")
                .put("Video CodecID", "Video_CodecID")
                .put("Video CodecID Info", "Video_CodecID_Info")
                .put("Video CodecID Hint", "Video_CodecID_Hint")
                .put("Video CodecID Description", "Video_CodecID_Description")
                .put("Video Duration String", "Video_Duration_String")
                .put("Video BitRate Mode String", "Video_BitRate_Mode_String")
                .put("Video BitRate String", "Video_BitRate_String")
                .put("Video Width String", "Video_Width_String")
                .put("Video Height String", "Video_Height_String")
                .put("Video DisplayAspectRatio String", "Video_DisplayAspectRatio_String")
                .put("For", "For")
                .put("Video FrameRate String", "Video_FrameRate_String")
                .put("Video Standard", "Video_Standard")
                .put("Video ColorSpace", "Video_ColorSpace")
                .put("Video ChromaSubsampling", "Video_ChromaSubsampling")
                .put("Video BitDepth String", "Video_BitDepth_String")
                .put("Video ScanType String", "Video_ScanType_String")
                .put("Video Compression Mode String", "Video_Compression_Mode_String")
                .put("Video StreamSize String", "Video_StreamSize_String")
                .put("Audio Format", "Audio_Format")
                .put("Audio Format Version", "Audio_Format_Version")
                .put("Audio CodecID", "Audio_CodecID")
                .put("Audio CodecID Info", "Audio_CodecID_Info")
                .put("Audio CodecID Description", "Audio_CodecID_Description")
                .put("Audio Duration String", "Audio_Duration_String")
                .put("Audio BitRate Mode String", "Audio_BitRate_Mode_String")
                .put("Audio BitRate String", "Audio_BitRate_String")
                .put("Audio Channel s  String", "Audio_Channel_s__String")
                .put("Audio SamplingRate String", "Audio_SamplingRate_String")
                .put("Audio BitDepth String", "Audio_BitDepth_String")
                .put("Audio StreamSize String", "Audio_StreamSize_String")
                .build();

        MP3_FIELDS = ImmutableMap.<String, String>builder()
                .put("Content-Type", MIME_FIELD)
                .put("title", "title")
                .put("composer", "composer")
                .put("xmpDM:audioCompressor", "audioCompressor")
                .put("xmpDM:releaseDate", "releaseDate")
                .put("xmpDM:album", "album")
                .put("xmpDM:artist", "artist")
                .put("xmpDM:genre", "genre")
                .build();

        WAV_FIELDS = ImmutableMap.<String, String>builder()
                .put("Content-Type", MIME_FIELD)
                .put("xmpDM:audioSampleType", "audioSampleType")
                .put("xmpDM:audioSampleRate", "audioSampleRate")
                .put("bits", "bits")
                .put("encoding", "encoding")
                .put("channels", "channels")
                .build();

        PDF_FIELDS = ImmutableMap.<String, String>builder()
                .put("Content-Type", MIME_FIELD)
                .put("Creation-Date", CREATE_DATE_FIELD)
                .put("Last-Modified", MODIFY_DATE_FIELD)
                .put("xmpTPg:NPages", "Page-Count")
                .put("title", "title")
                .build();

        PLAIN_TEXT_FIELDS = ImmutableMap.<String, String>builder()
                .put("Content-Type", MIME_FIELD)
                .put("Content-Encoding", "Content-Encoding")
                .build();

        HTML_FIELDS = ImmutableMap.<String, String>builder()
                .put("Content-Language", "language")
                .build();

        POWERPOINT_FIELDS = ImmutableMap.<String, String>builder()
                .put("Content-Type", MIME_FIELD)
                .put("Creation-Date", CREATE_DATE_FIELD)
                .put("Last-Modified", MODIFY_DATE_FIELD)
                .put("Application-Name", "Application-Name")
                .put("Application-Version", "Application-Version")
                .put("Slide-Count", "Slide-Count")
                .put("Word-Count", "Word-Count")
                .put("Paragraph-Count", "Paragraph-Count")
                .put("Presentation-Format", "Presentation-Format")
                .put("title", "title")
                .build();

        WORD_FIELDS = ImmutableMap.<String, String>builder()
                .put("Content-Type", MIME_FIELD)
                .put("Creation-Date", CREATE_DATE_FIELD)
                .put("Last-Modified", MODIFY_DATE_FIELD)
                .put("Application-Name", "Application-Name")
                .put("Application-Version", "Application-Version")
                .put("Word-Count", "Word-Count")
                .put("Paragraph-Count", "Paragraph-Count")
                .put("Character Count", "Character-Count")
                .put("title", "title")
                .build();

        EXCEL_FIELDS = ImmutableMap.<String, String>builder()
                .put("Content-Type", MIME_FIELD)
                .put("Creation-Date", CREATE_DATE_FIELD)
                .put("Last-Modified", MODIFY_DATE_FIELD)
                .put("Application-Name", "Application-Name")
                .put("Application-Version", "Application-Version")
                .build();
    }

    /*public LegacyTechnicalMetadataUpdater(Component component, AspireObject object, String mimeType){
        this.component = component;
        this.logger = (ALogger)this.component;
        this.objectId = object.getAttribute("id");
        this.object = object;
        this.mimeType = mimeType;
    }*/

    public LegacyTechnicalMetadataUpdater(Component component, Job job){
        this.component = component;
        this.logger = (ALogger)this.component;
        this.job = job;
        this.jobInfo = Jobs.getJobInfo(this.job);
        this.objectId = jobInfo.getObjectId();
    }

    /**
     * This function get tika metadata-extraction and add this information to the aspire object
     * @throws AspireException
     */
    public AspireObject updateMetadata() throws AspireException
    {
        if (jobInfo.getTMDContentFile() != null) {
            AspireObject object = jobInfo.getDigitalObject();
            try {
                logger.info("Extracting Legacy Technical Metadata from objectId: " + objectId + " file: %s", file);
                setTechnicalMetadata();
                logger.debug("*** objectId: " + object + " Legacy TMD input Object: " + object.toXmlString(true));
                updateTechnicalMetadata(object);
                logger.debug("**** legacy object with regen metadata for objectId: " + objectId + "\n" + object.toXmlString(true));
                logger.debug("*** Updating legacy parent object XML... for " + objectId);
                updateTechnicalMetadataInParentJob();

                return object;
            } catch (AspireException ex) {
                logger.error(ex, "Failed to extract technical metadata objectId: " + objectId + " ; " + ex.getMessage());
            }
        }
        return null;
    }

    private void setFileMetadata() throws AspireException{
        fileMetadata = Tika.extractTechnicalMetaData(file);
    }

    private void setTechnicalMetadata() throws AspireException{
        setFileMetadata();
        createMetadataObject();
        addSize();

        switch(jobInfo.getMimeType()){
            case MimeTypes.BMP:
                setMetadata(BMP_FIELDS);
                break;
            case MimeTypes.GIF:
                setMetadata(GIF_FIELDS);
                break;
            case MimeTypes.JPEG:
                setJpegMetadata(JPEG_FIELDS);
                break;
            case MimeTypes.JPEG2000:
                setJpegMetadata(JPEG2000_FIELDS);
                break;
            case MimeTypes.TIFF:
                setMetadata(TIFF_FIELDS);
                break;
            case MimeTypes.MP3:
                setMetadata(MP3_FIELDS);
                break;
            case MimeTypes.WAV:
                setMetadata(WAV_FIELDS);
                break;
            case MimeTypes.MP4_VIDEO:
                setMetadata(MP4_FIELDS);
                break;
            case MimeTypes.WMV:
                setMetadata(WMV_FIELDS);
                break;
            case MimeTypes.AVI:
            case MimeTypes.REALMEDIA:
            case MimeTypes.QUICKTIME_VIDEO:
                setMetadata(VIDEO_FIELDS);
                break;
            case MimeTypes.PDF:
                setMetadata(PDF_FIELDS);
                break;
            case MimeTypes.PLAIN_TEXT:
                setMetadata(PLAIN_TEXT_FIELDS);
                break;
            case MimeTypes.HTML:
                setMetadata(HTML_FIELDS);
                break;
            case MimeTypes.WORD:
                setMetadata(WORD_FIELDS);
                break;
            case MimeTypes.EXCEL:
                setMetadata(EXCEL_FIELDS);
                break;
            case MimeTypes.POWERPOINT:
                setMetadata(POWERPOINT_FIELDS);
                break;
        }
    }

    private void setMetadata(ImmutableMap<String,String> fields) throws AspireException{
        for (Entry<String, String> field : fields.entrySet()){
            String fieldName = field.getKey();
            String tagName = field.getValue();
            addField(fieldName, tagName);
        }
    }

    private AspireObject updateTechnicalMetadata (AspireObject object) throws AspireException{
        object.set(metadataObject);
        return metadataObject;
    }

    private void updateTechnicalMetadataInParentJob() throws AspireException{
        Lock lock = jobInfo.getObjectsXmlLock();
        lock.lock();

        try{
            updateTechnicalMetadataInParentDescription();
        } finally{
            lock.unlock();
        }
    }

    private void updateTechnicalMetadataInParentDescription() throws AspireException{
        AspireObject objects = jobInfo.getParent().getJobData().get(ObjectsXml.OBJECTS_XML_TOP_LEVEL_ELEMENT);
        AspireObject object = ObjectsXml.findObject(objectId, objects);
        updateTechnicalMetadata(object);
        logger.debug("**** parent object XML: "+object.toXmlString(true));
    }

    public void setFile(File file) {
        this.file = file;
    }

    private void createMetadataObject(){
        metadataObject = new AspireObject(Objects.TECHNICAL_METADATA_NODE);
    }

    private void setJpegMetadata(ImmutableMap<String,String> fields) throws AspireException {
        setMetadata(fields);
        addResolutionField("Resolution Units", "X Resolution", "Y Resolution");
    }

    private void addResolutionField(String resolutionUnitPropertyName, String xResolutionPropertyName,
                                    String yResolutionPropertyName){
        String resolutionUnit = getValue(resolutionUnitPropertyName);
        String resolutionHorizontal = getValue(xResolutionPropertyName);
        String resolutionVertical = getValue(yResolutionPropertyName);
        if (resolutionUnit != null && resolutionHorizontal != null && resolutionVertical != null){
            AspireObject resolution = new AspireObject("resolution");
            resolution.setAttribute("x", resolutionHorizontal);
            resolution.setAttribute("y", resolutionVertical);
            resolution.setAttribute("units", resolutionUnit);
            metadataObject.add(resolution);
        }
    }

    private void addField(String name, String alias) throws AspireException{
        String value = getValue(name);
        if (StringUtilities.isNotEmpty(value)){
            metadataObject.add(alias, value);
        }
    }

    private void addField(String name, Object value) throws AspireException{
        metadataObject.add(name, value);
    }

    private String getValue(String name){
        return cleanse(fileMetadata.get(name));
    }

    private String cleanse(String input){
        if (input == null){
            return input;
        } else {
            return XMLUtilities.removeIllegalXMLChars(input);
        }
    }

    private void addSize() throws AspireException {
        addField(SIZE_FIELD, file.length());
    }
}
