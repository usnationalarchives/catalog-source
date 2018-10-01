package gov.nara.opa.common.valueobject.annotation.comments;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.utils.TimestampUtils;
import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

public class CommentsCollectionValueObject extends AbstractWebEntityValueObject {

	private List<CommentValueObject> comments;
	private Integer totalComments = 0;
	private Timestamp lastModified;
	private Integer removedComments = 0;
	
	private String commentsFormat = "collapsed";

	public CommentsCollectionValueObject(List<CommentValueObject> comments) {
		if (comments == null) {
			throw new OpaRuntimeException(
					"The comments parameter cannot be null");
		}

		this.comments = comments;
		this.totalComments = comments.size();
	}

	@Override
	public Map<String, Object> getDatabaseContent() {
		return null;
	}

	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
		LinkedHashMap<String, Object> aspireContent = new LinkedHashMap<String, Object>();
		if (comments != null && comments.size() > 0) {
			aspireContent.put("@total", totalComments);
			aspireContent.put("@lastModified", TimestampUtils.getUtcString(getLastModified()));
			
			aspireContent.put("@commentsFormat", commentsFormat);
			
			int repliesCount = 0;
			for (int i = 0; i < comments.size(); ++i) {
				if (comments.get(i).getReplies() != null) {
					List<CommentValueObject> replies = comments.get(i).getReplies();
					repliesCount += replies.size();
					int removedReplies = 0;
					for (CommentValueObject reply : replies) {
						if (!reply.getStatus()) {
							removedReplies++;
						}
					}
					repliesCount -= removedReplies;
				}
			}
			aspireContent.put("@replies", repliesCount);
			aspireContent.put("comment", comments);
		}
		return aspireContent;
	}

	public Integer getTotalComments() {
		return totalComments;
	}
	
	public int getTotalCommentsIncludingRemoves() {
		return totalComments + removedComments;
	}

	public void setTotalRemovedComments(Integer removedComments) {
		this.totalComments = this.totalComments - removedComments;
		this.removedComments = removedComments;
	}

	public List<CommentValueObject> getComments() {
		return comments;
	}

	public Timestamp getLastModified() {
		return lastModified;
	}

	public void setLastModified(Timestamp lastModified) {
		this.lastModified = lastModified;
	}

	public String getCommentsFormat() {
		return commentsFormat;
	}

	public void setCommentsFormat(String commentsFormat) {
		this.commentsFormat = commentsFormat;
	}
}
