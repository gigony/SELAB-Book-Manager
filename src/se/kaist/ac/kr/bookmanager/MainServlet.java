package se.kaist.ac.kr.bookmanager;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.extensions.appengine.auth.oauth2.AppIdentityCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gdata.client.GoogleService.CaptchaRequiredException;
import com.google.gdata.client.spreadsheet.CellQuery;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

@SuppressWarnings("serial")
public class MainServlet extends HttpServlet {
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // http://aspnetresources.com/blog/frames_webforms_and_rejected_cookies
    // http://www.okjsp.pe.kr/seq/31134
    // To solve iframe-related problem
    resp.setHeader("P3P", "CP='IDC DSP COR ADM DEVi TAIi PSA PSD IVAi IVDi CONi HIS OUR IND CNT'");
    // Korean problem
    // http://mcpaint.tistory.com/88
    resp.setContentType("text/html; charset=UTF-8");
    resp.setCharacterEncoding("UTF-8");
    OutputStreamWriter htmlWriter = new OutputStreamWriter(resp.getOutputStream(), "UTF-8");

    String userName = "unknown";
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();

    if (user != null && user.getEmail().endsWith("@se.kaist.ac.kr")) {
      userName = user.getNickname() + "(" + user.getEmail() + ")";
    } else {
      String destinationURL = req.getParameter("booknum") != null ? (req.getRequestURL().toString() + "?booknum=" + req
          .getParameter("booknum").toString()) : req.getRequestURL().toString();
      resp.sendRedirect(userService.createLoginURL(destinationURL, "se.kaist.ac.kr"));
      return;
    }

    try {
//      https://developers.google.com/api-client-library/java/google-api-java-client/oauth2
      
            
      SpreadsheetService sheetService = new GoogleAPI().spreadsheetService();
      
      //////////
//   // Define the URL to request.  This should never change.
//      URL SPREADSHEET_FEED_URL = new URL(
//          "https://spreadsheets.google.com/feeds/spreadsheets/private/full");
//
//      // Make a request to the API and get all spreadsheets.
//      SpreadsheetFeed feed = sheetService.getFeed(SPREADSHEET_FEED_URL, SpreadsheetFeed.class);
//      List<SpreadsheetEntry> spreadsheets = feed.getEntries();
//
//      // Iterate through all of the spreadsheets returned
//      String htmlt ="<html><body>";
//      htmlt += userName + "<br>";
//      htmlt += ""+ spreadsheets.size() + " entries <br>";
//      for (SpreadsheetEntry spreadsheet : spreadsheets) {
//        // Print the title of this spreadsheet to the screen
//        htmlt +=spreadsheet.getTitle().getPlainText() + ":" + spreadsheet.getId() + "|  <br>" ;
//        //System.out.println(spreadsheet.getTitle().getPlainText());
//        WorksheetFeed worksheetFeed = sheetService.getFeed(spreadsheet.getWorksheetFeedUrl(), WorksheetFeed.class);
//        List<WorksheetEntry> worksheets = worksheetFeed.getEntries();
//        for(WorksheetEntry worksheet:worksheets) {
//          htmlt +=">>>>" + worksheet.getCellFeedUrl() + "|  <br>" ;
//        }
//      }
//      
//      htmlt +="</body></html>";
//      htmlWriter.write(htmlt);
//      htmlWriter.close();
//      if(1==1)
//        return;
//      /////////////
//      1AHS0LGKVOW5IOncnYxroNvcpws5b6Hm2jWZhy8qHJRM

      URL cellFeedUrl = new URL("https://spreadsheets.google.com/feeds/cells/1AHS0LGKVOW5IOncnYxroNvcpws5b6Hm2jWZhy8qHJRM/od7/private/full");
      CellQuery query = new CellQuery(cellFeedUrl);
      query.setMinimumRow(1);
      query.setMaximumRow(1);
      query.setMinimumCol(7);
      query.setMaximumCol(7);
      CellFeed cellFeed = sheetService.query(query, CellFeed.class);

      int totalItems = 0;
      List<CellEntry> recordList = cellFeed.getEntries();
      totalItems = recordList.get(0).getCell().getNumericValue().intValue();

      cellFeedUrl = new URL("https://spreadsheets.google.com/feeds/cells/1AHS0LGKVOW5IOncnYxroNvcpws5b6Hm2jWZhy8qHJRM/od6/private/full");
      query = new CellQuery(cellFeedUrl);
      query.setMinimumRow(2);
      query.setMaximumRow(1 + totalItems);
      query.setMinimumCol(1);
      query.setMaximumCol(6);
      cellFeed = sheetService.query(query, CellFeed.class);
      // sheetService.setContentType(new ContentType("text/html; charset=UTF-8"));
      List<CellEntry> cellList = cellFeed.getEntries();
      String tableString = "<table id='hor-minimalist-b'><thead><tr>" + "<th scope='col'>Book No.</th>"
          + "<th scope='col'>Title</th>" + "<th scope='col'>Author</th>" + "<th scope='col'>Publisher</th>"
          + "<th scope='col'>Status</th>" + "<th scope='col'>Borrower</th>" + "<th scope='col'>Borrowed Time</th>"
          + "</tr>" + "</thead>" + "<tbody>";
      int rowIndex = 1;
      String[] dataList = new String[6];
      Iterator<CellEntry> iter = cellList.iterator();
      String btn;
      while (iter.hasNext()) {
        for (int i = 0; i < 6; i++) {
          CellEntry item = iter.next();
          dataList[i] = item.getCell().getValue();// new String(item.getCell().getValue().getBytes(), "UTF-8");
        }
        for (int i = 0; i < 6; i++) {

          switch (i) {
          case 0:
            if (dataList[3].equals("Lent out")) {
              btn = String.format(
                  "<span class='button black small'><a href='javascript:returnBook(\"%d\")'>Return</a></span>",
                  rowIndex);
              tableString += String.format("<tr class='borrowed'><td>%d</td><td>%s</td>", rowIndex, dataList[i]);
            } else {
              btn = String.format(
                  "<span class='button black small'><a href='javascript:borrowBook(\"%d\")'>Borrow</a></span>",
                  rowIndex);
              tableString += String.format("<tr><td>%d</td><td>%s</td>", rowIndex, dataList[i]);
            }
            break;
          case 3:
            if (dataList[i].equals("Lent out")) {
              String borrowerEmail = dataList[4];
              borrowerEmail = borrowerEmail.substring(borrowerEmail.lastIndexOf('(') + 1, borrowerEmail.length() - 1);
              borrowerEmail = borrowerEmail.toLowerCase();
              if (borrowerEmail.equals(user.getEmail().toLowerCase()))
                btn = String.format(
                    "<span class='button green small'><a href='javascript:returnBook(\"%d\")'>Return</a></span>",
                    rowIndex);
              else
                btn = dataList[i];
            } else {
              btn = String.format(
                  "<span class='button black small'><a href='javascript:borrowBook(\"%d\")'>Borrow</span>", rowIndex);
            }
            tableString += String.format("<td>%s</td>", btn);
            break;
          case 1:
          case 2:
          case 4:
            tableString += String.format("<td>%s</td>", dataList[i]);
            break;
          case 5:
            tableString += String.format("<td>%s</td></tr>", dataList[i]);
            rowIndex++;
            break;
          }
        }
      }
      tableString += "</tbody></table>";

      BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(getServletContext()
          .getRealPath("/WEB-INF/main.template")), "UTF-8"));

      String html = "";
      String lineStr = null;
      while ((lineStr = reader.readLine()) != null) {
        html += lineStr;
      }

      String destinationURL = req.getParameter("booknum") != null ? (req.getRequestURL().toString() + "?booknum=" + req
          .getParameter("booknum").toString()) : req.getRequestURL().toString();
      String loginoutString = "<a href='" + userService.createLogoutURL(destinationURL, "se.kaist.ac.kr")
          + "'> Log out </a>";

      int index = html.indexOf("LOGINOUT_AREA");
      html = html.substring(0, index) + loginoutString + html.substring(index + "LOGINOUT_AREA".length());

      index = html.indexOf("DATA_TABLE_AREA");
      html = html.substring(0, index) + tableString + html.substring(index + "DATA_TABLE_AREA".length());

      htmlWriter.write(html);

    } catch (CaptchaRequiredException e) {
      resp.sendRedirect("https://www.google.com/a/se.kaist.ac.kr/UnlockCaptcha");
      return;
    } catch (AuthenticationException e) {
      e.printStackTrace(new PrintWriter(htmlWriter));
    } catch (ServiceException e) {
      e.printStackTrace(new PrintWriter(htmlWriter));
    } catch (Throwable e) {
      e.printStackTrace(new PrintWriter(htmlWriter));
    }
    htmlWriter.close();

  }

  private String getLoginAddr() {
    return System.getProperty("google.id");
  }

  private String getLoginPass() {
    return System.getProperty("google.pw");
  }

}
