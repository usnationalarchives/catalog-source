package gov.nara.opa.architecture.dataaccess;

import gov.nara.opa.architecture.web.validation.AbstractSearchResquestParameters;

import java.util.ArrayList;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author aolaru
 * @date Jun 3, 2014 All JDBC Template classes should extend from the abstract
 *       class. This class will take care of having the data source injected in
 *       it and setting the jdbcTemplate instance to be used for executing SQL.
 *       Unless overriden in subclasses, JDBC templates will be recycled at the
 *       of user session to free up resources. Special care must be taken
 *       regarding maintaining state in subclasses. If subclasses maintain state
 *       through instance variables it would be better to change the bean scope
 *       to prototype to avoid multiple concurrent threads overrinding the
 *       instance properties and corrupting their state.
 */
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION)
public class AbstractOpaDbJDBCTemplate implements InitializingBean {

  // Classes extending from this class will be setup to only work with the
  // OpaDB. If more DBs will be needed for the Opa Webapp a separate
  // AbstractJdbcTemplate needs to be setup which would be configured with a
  // different data source.
  @Autowired
  @Qualifier("opaDbDataSource")
  private DataSource dataSource;

  private JdbcTemplate jdbcTemplate;

  private NamedParameterJdbcTemplate namedJdbcTemplate;

  @Override
  public void afterPropertiesSet() throws Exception {
    jdbcTemplate = new JdbcTemplate(getDataSource());
    namedJdbcTemplate = new NamedParameterJdbcTemplate(getDataSource());
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public JdbcTemplate getJdbcTemplate() {
    return jdbcTemplate;
  }

  public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public NamedParameterJdbcTemplate getNamedJdbcTemplate() {
    return namedJdbcTemplate;
  }

  public void setNamedJdbcTemplate(NamedParameterJdbcTemplate namedJdbcTemplate) {
    this.namedJdbcTemplate = namedJdbcTemplate;
  }

  protected static void appendAndParamToSql(String paramName,
      Object paramValue, StringBuffer sql, ArrayList<Object> params) {
    appendAndParamToSql(paramName, paramValue, sql, params, false, false);
  }

  protected static void appendAndParamToSql(String paramName,
      Object paramValue, StringBuffer sql, ArrayList<Object> params,
      boolean likeStatement) {
    appendAndParamToSql(paramName, paramValue, sql, params, likeStatement,
        false);
  }
  
  protected static void appendAndParamToSql(String paramName,
	      Object paramValue, StringBuffer sql, ArrayList<Object> params,
	      boolean likeStatement, boolean includeNull) {
	  appendAndParamToSql(paramName, paramValue, sql, params, likeStatement, includeNull, false);
  }
  
  protected static void appendAndParamToSql(String paramName,
	      Object paramValue, StringBuffer sql, ArrayList<Object> params,
	      boolean likeStatement, boolean includeNull, boolean intToChar) {

    if (paramValue == null && !includeNull) {
      return;
    }
    if (params.size() > 0) {
      sql.append(" AND");
    }
    if (paramValue != null) {
      if (likeStatement) {
    	if(intToChar) {
	        sql.append(" convert(" + paramName + " USING utf8) LIKE ?");
    	} else {
	        sql.append(" " + paramName + " LIKE ?");
    	}
        params.add("%" + paramValue + "%");
      } else {
        sql.append(" " + paramName + " = ?");
        params.add(paramValue);
      }

    } else {
      if (includeNull) {
        sql.append(" " + paramName + " IS NULL");
      }
    }
  }
  
  protected static void appendSearchRequestClauses(StringBuffer sql,
      AbstractSearchResquestParameters requestParameters) {
    appendSearchRequestClauses(sql, requestParameters, true);
  }

  protected static void appendSearchRequestClauses(StringBuffer sql,
      AbstractSearchResquestParameters requestParameters, boolean hasLimit) {
    String sortField = requestParameters.getSortField();
    if (sortField != null) {
      sql.append(" ORDER BY " + sortField + " "
          + requestParameters.getSortDirection());
    }
    if(hasLimit) {
      if (requestParameters.getRows() != null) {
        sql.append(" LIMIT " + requestParameters.getRows());
      }
      if (requestParameters.getOffset() != null) {
        sql.append(" OFFSET " + requestParameters.getOffset());
      }
    }
  }
}
