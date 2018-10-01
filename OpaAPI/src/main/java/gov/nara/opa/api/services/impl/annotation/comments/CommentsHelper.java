package gov.nara.opa.api.services.impl.annotation.comments;

import gov.nara.opa.api.dataaccess.user.UserAccountDao;
import gov.nara.opa.api.security.OPAAuthenticationProvider;
import gov.nara.opa.common.valueobject.annotation.comments.CommentValueObject;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommentsHelper {

	@Autowired
	private UserAccountDao userAccountDao;

	public CommentValueObject createCommentForInsert(Integer accountId,
			String naId, String objectId, String text, Integer pageNum) {
		CommentValueObject comment = new CommentValueObject();
		comment.setAccountId(accountId);
		comment.setAnnotation(text);
		comment.setAnnotationMD5(DigestUtils.md5Hex(text));
		comment.setAnnotationTS(new Timestamp(new Date().getTime()));
		comment.setNaId(naId);
		comment.setObjectId(objectId);
		comment.setStatus(true);
		comment.setPageNum(pageNum);
		return comment;
	}

	public UserAccountValueObject getSessionUserAccount() {
		Integer accountId = OPAAuthenticationProvider
				.getAccountIdForLoggedInUser();
		return userAccountDao.selectByAccountId(accountId);
	}
}
