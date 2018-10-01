package gov.nara.opa.api.controller.search;

import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.common.valueobject.annotation.tags.OpaTitle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.validation.Valid;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class SEOListController extends AbstractBaseController {
    private static OpaLogger logger = OpaLogger
            .getLogger(ApiSearchController.class);

    @Autowired
    private ConfigurationService configurationService;

    @Value("${naraBaseUrl}")
    private String naraBaseUrl;

    private int MAX_ROWS_PER_PAGE;

    @Autowired
    @Qualifier("opaDbDataSource")
    private DataSource ds;

    private static final String SELECT_OPA_TITLES = "SELECT na_id, opa_title "
            + "FROM opa_titles WHERE deleted = 0 ORDER BY na_id ASC LIMIT ? OFFSET ?";

    @RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM + "/titlelist/{pageNumber}" }, method = RequestMethod.GET)
    public ResponseEntity<String> get(@PathVariable("pageNumber") String pageNumber, HttpServletRequest request,
                                      HttpServletResponse response) {
        MAX_ROWS_PER_PAGE = configurationService.getConfig().getMaxTitlesPerRobotListing();
        int page = 1;
        if (!pageNumber.isEmpty()) {
            try{
                int tmp = Integer.parseInt(pageNumber);
                page = tmp;
            }catch(Exception e){
                // ignore it and assume page is 1
            }
        }

        List<OpaTitle> opaTitleRecords = new ArrayList<>();

        int offset = (page-1)*MAX_ROWS_PER_PAGE;

        try {
            Connection conn = ds.getConnection();
            PreparedStatement ps = conn.prepareStatement(SELECT_OPA_TITLES);
            ps.setInt(1,MAX_ROWS_PER_PAGE);
            ps.setInt(2,offset);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                String na_id = rs.getString("na_id");
                String title = rs.getString("opa_title");
                OpaTitle thisTitle = new OpaTitle();
                thisTitle.setNaId(na_id);
                thisTitle.setOpaTitle(title);
                opaTitleRecords.add(thisTitle);
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<html>\n" +
                "<head>\n" +
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
                "<meta name=\"robots\" content=\"noindex\">\n" +
                "<title>NAC Catalog List</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "<ul>");
        for(OpaTitle title : opaTitleRecords){
            sb.append("<li><a href=\"");
            sb.append(naraBaseUrl);
            sb.append("id/");
            sb.append(title.getNaId());
            sb.append("\">");
            sb.append(title.getOpaTitle());
            sb.append("</a></li>");
        }
        sb.append("</ul>\n");
        if(opaTitleRecords.size() == MAX_ROWS_PER_PAGE) {
            sb.append("<a href=\"./");
            sb.append(page + 1);
            sb.append("\">Next Page</a>");
        }
        sb.append("</body>\n" +
                "</html>");
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        ResponseEntity<String> entity = new ResponseEntity<String>(sb.toString(), headers, HttpStatus.OK);

        return entity;
    }
}
