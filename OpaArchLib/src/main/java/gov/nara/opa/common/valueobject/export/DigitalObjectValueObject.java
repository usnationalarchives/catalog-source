package gov.nara.opa.common.valueobject.export;

import gov.nara.opa.common.valueobject.annotation.comments.CommentValueObject;
import gov.nara.opa.common.valueobject.annotation.tags.TagValueObject;
import gov.nara.opa.common.valueobject.annotation.transcriptions.TranscriptionValueObject;

import java.util.ArrayList;
import java.util.List;

public class DigitalObjectValueObject {

	private String id;
	private Integer index;
	private String thumbnailPath;
	private String thumbnailMimeType;
	private String filePath;
	private String fileMimeType;
	private String description;
	private String designator;
	private List<TagValueObject> tags;
	private List<TranscriptionValueObject> transcriptions;
	private List<CommentValueObject> comments;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public String getThumbnailPath() {
		return thumbnailPath;
	}

	public void setThumbnailPath(String thumbnailPath) {
		this.thumbnailPath = thumbnailPath;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFileMimeType() {
		return fileMimeType;
	}

	public void setFileMimeType(String fileMimeType) {
		this.fileMimeType = fileMimeType;
	}

	public List<TagValueObject> getTags() {
		if (tags == null) {
			tags = new ArrayList<TagValueObject>();
		}
		return tags;
	}

	public List<TranscriptionValueObject> getTranscriptions() {
		if (transcriptions == null) {
			transcriptions = new ArrayList<TranscriptionValueObject>();
		}
		return transcriptions;
	}

	public List<CommentValueObject> getComments() {
		if (comments == null) {
			comments = new ArrayList<CommentValueObject>();
		}
		return comments;
	}

	public String getThumbnailMimeType() {
		return thumbnailMimeType;
	}

	public void setThumbnailMimeType(String thumbnailMimeType) {
		this.thumbnailMimeType = thumbnailMimeType;
	}

	public String getDesignator() {
		return designator;
	}

	public void setDesignator(String designator) {
		this.designator = designator;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}