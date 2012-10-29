package se.appengine.book;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gdata.client.GoogleService.CaptchaRequiredException;
import com.google.gdata.client.spreadsheet.CellQuery;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.Cell;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ContentType;
import com.google.gdata.util.ServiceException;

@SuppressWarnings("serial")
public class BookReturnServlet extends HttpServlet {
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	resp.setContentType("text/html; charset=UTF-8");
	resp.setCharacterEncoding("UTF-8");
	OutputStreamWriter htmlWriter = new OutputStreamWriter(resp.getOutputStream(), "UTF-8");

	// To solve iframe-related problem
	resp.setHeader("P3P", "CP='CAO PSA CONi OTR OUR DEM ONL'");
	htmlWriter.write("<html>" + "<head>" + "  <meta http-equiv='content-type' content='text/html; charset=UTF-8'>"
	        + "  <title>Return Result</title>" + "</head>" + "<body>");

	boolean success = false;
	String errorMsg = "";
	String mailSubject = "";
	String mailContent = "";
	String time = "";

	int bookNum = -1;
	try {
	    bookNum = Integer.parseInt(req.getParameter("booknum"));
	} catch (Exception e) {
	    htmlWriter.write("Error on booknum!");
	    htmlWriter.write(" <input id='closebtn' type='button' name='closebtn' value='Close window' onclick='javascript:closeWindow()'/>");
	    htmlWriter.write("</body></html>");
	    return;
	}
//	String hostedDomain = "se.kaist.ac.kr";
//	String nextUrl = req.getParameter("booknum") != null ? (req.getRequestURL() + "?booknum=" + req.getParameter("booknum").toString()) : req
//	        .getRequestURL().toString();
//	String scope = "http://www.google.com/m8/feeds"; //http://spreadsheets.google.com/feeds https://spreadsheets.google.com/feeds http://docs.google.com/feeds 
//	boolean secure = false; // set secure=true to request AuthSub tokens
//	boolean session = true;
//	String authSubUrl = AuthSubUtil.getRequestUrl(hostedDomain, nextUrl, scope, secure, session);
//	String singleUseToken = req.getParameter("token"); //AuthSubUtil.getTokenFromReply(req.getQueryString());
//	if (singleUseToken == null) {
//	    resp.sendRedirect(authSubUrl);
//	    return;
//	}
//	singleUseToken = URLDecoder.decode(singleUseToken, "UTF-8");
//	String sessionToken;
	try {

//	    sessionToken = AuthSubUtil.exchangeForSessionToken(singleUseToken, null);
//	    ContactsService service = new ContactsService("sebookmanager");
//	    service.setAuthSubToken(sessionToken, null);
//	    URL metafeedUrl = new URL("http://www.google.com/m8/feeds/contacts/default/full ");
//	    ContactFeed feed = service.getFeed(metafeedUrl, ContactFeed.class);
//	    List<Person> person = feed.getAuthors();
//	    String userName = "_";
//	    for (Person p : person) {
//		String user = p.getEmail();
//		if (user.endsWith("@se.kaist.ac.kr")) {
//		    userName = user;
//		    break;
//		}
//	    }
	    String userName = "unknown";
	    String userMail = "unknown";
	    UserService userService = UserServiceFactory.getUserService();
	    User user = userService.getCurrentUser();
	    if (user != null && user.getEmail().endsWith("@se.kaist.ac.kr")) {
		userMail = user.getEmail();
		userName = "(" + userMail + ")";
	    } else {
		String destinationURL = req.getParameter("booknum") != null ? (req.getRequestURL().toString() + "?booknum=" + req.getParameter(
		        "booknum").toString()) : req.getRequestURL().toString();
		resp.sendRedirect(userService.createLoginURL(destinationURL, "se.kaist.ac.kr"));
		htmlWriter.close();
		return;
	    }

	    SpreadsheetService sheetService = new SpreadsheetService("sebookmanager");	    
	    sheetService.setUserCredentials(getLoginAddr(), getLoginPass());	    

	    URL cellFeedUrl = new URL("http://spreadsheets.google.com/feeds/cells/ttRHdk6wziK4H-gC_ISC-UA/od7/private/full");

	    CellQuery query = new CellQuery(cellFeedUrl);
	    query.setMinimumRow(1);
	    query.setMaximumRow(1);
	    query.setMinimumCol(7);
	    query.setMaximumCol(9);
	    CellFeed cellFeed = sheetService.query(query, CellFeed.class);

	    int totalItems = 0, peopleNum = 0;//,updateIndex=0;
	    List<CellEntry> recordList = cellFeed.getEntries();
	    totalItems = recordList.get(0).getCell().getNumericValue().intValue();
	    peopleNum = recordList.get(2).getCell().getNumericValue().intValue();

	    if (bookNum <= 0 || bookNum > totalItems) {

		htmlWriter.write("Wrong book number!");
		htmlWriter.write(" <input id='closebtn' type='button' name='closebtn' value='Close window' onclick='javascript:closeWindow()'/>");
		htmlWriter.write("</body></html>");
		htmlWriter.close();
		return;
	    }

	    cellFeedUrl = new URL("http://spreadsheets.google.com/feeds/cells/ttRHdk6wziK4H-gC_ISC-UA/od4/private/full");
	    query = new CellQuery(cellFeedUrl);
	    query.setMinimumRow(2);
	    query.setMaximumRow(peopleNum + 1);
	    query.setMinimumCol(1);
	    query.setMaximumCol(2);
	    cellFeed = sheetService.query(query, CellFeed.class);
	    List<CellEntry> cellList = cellFeed.getEntries();
	    Map<String, Integer> emailMap = new HashMap<String, Integer>();
	    Map<Integer, String> nameMap = new HashMap<Integer, String>();

	    for (CellEntry entry : cellList) {
		Cell cell = entry.getCell();
		if (cell.getCol() == 1) {
		    emailMap.put(cell.getValue(), cell.getRow());
		} else if (cell.getCol() == 2) {
		    nameMap.put(cell.getRow(), cell.getValue());
		}
	    }
	    Integer nameIndex = emailMap.get(userMail);
	    if (nameIndex != null) {
		String name = nameMap.get(nameIndex);
		if (name != null) {
		    userName = name + userName;
		}
	    }

	    cellFeedUrl = new URL("http://spreadsheets.google.com/feeds/cells/ttRHdk6wziK4H-gC_ISC-UA/od6/private/full");

	    query = new CellQuery(cellFeedUrl);
	    query.setMinimumRow(1 + bookNum);
	    query.setMaximumRow(1 + bookNum);
	    query.setMinimumCol(1);
	    query.setMaximumCol(6);
	    cellFeed = sheetService.query(query, CellFeed.class);

	    cellList = cellFeed.getEntries();
	    if (cellList.get(3).getCell().getValue().equals("Lent out")) {
		String borrowerEmail = cellList.get(4).getCell().getValue();
		borrowerEmail = borrowerEmail.substring(borrowerEmail.lastIndexOf('(') + 1, borrowerEmail.length() - 1);
		borrowerEmail = borrowerEmail.toLowerCase();
		if (borrowerEmail.equals(user.getEmail().toLowerCase())) {

		    cellList.get(3).changeInputValueLocal("_");
		    cellList.get(3).update();
		    cellList.get(4).changeInputValueLocal("_");
		    cellList.get(4).update();
		    cellList.get(5).changeInputValueLocal("_");
		    cellList.get(5).update();

		    Calendar currentCalendar = Calendar.getInstance();
		    Date currentDate = changeTimeZone(currentCalendar.getTime(), TimeZone.getTimeZone("GMT+9"));
		    String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
		    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		    time = sdf.format(currentDate.getTime());

		    ListEntry newEntry = new ListEntry();
		    String nameValuePairs = String.format("BookNumber=%s^Title=%s^Status=%s^Name=%s^Time=%s", bookNum, cellList.get(0).getCell()
			    .getValue(), "Returned", userName, time);

		    // Split first by the commas between the different fields.
		    for (String nameValuePair : nameValuePairs.split("\\^")) {
			String[] parts = nameValuePair.split("=", 2);
			String tag = parts[0];
			String value = parts[1];
			newEntry.getCustomElements().setValueLocal(tag, value);
		    }
		    URL listFeedUrl = new URL("http://spreadsheets.google.com/feeds/list/ttRHdk6wziK4H-gC_ISC-UA/od7/private/full");
		    ListEntry insertedRow = sheetService.insert(listFeedUrl, newEntry);
		    htmlWriter.write("Success!");
		} else {
		    htmlWriter.write("Failed! The book was borrowed by other person!");
		}

	    } else {
		htmlWriter.write("Failed! The book is already existing!");
	    }
	    htmlWriter.write("</body></html>");
	    //http://www.google.com/support/forum/p/apps-script/thread?tid=03ee673e2e351368&hl=en&fid=03ee673e2e3513680004882b02b5ba7e&hltp=2
	    //http://code.google.com/intl/ko-KR/googleapps/faq.html
	    success = true;
	    mailSubject = String.format("[Book Return][%s] %s : %s", userName, bookNum, cellList.get(0).getCell().getValue());
	    mailContent = String.format("[%s] %s retunred a book \r\n  %s : %s ", time, userName, bookNum, cellList.get(0).getCell().getValue());

	} catch (CaptchaRequiredException e) {
	    htmlWriter
		    .write(" </br>Captcha exception is found!</br> Please visit <a href='https://www.google.com/a/se.kaist.ac.kr/UnlockCaptcha'> this site</a> and unlock captcha.</br> Please execute what you did again after the captcha is unlocked");
	    resp.getWriter().print("</body></html>");
	} catch (AuthenticationException e) {
	    e.printStackTrace(new PrintWriter(htmlWriter));
	    errorMsg = e.getMessage();
	} catch (ServiceException e) {
	    e.printStackTrace(new PrintWriter(htmlWriter));
	    errorMsg = e.getMessage();
	} catch (Throwable e) {
	    e.printStackTrace(new PrintWriter(htmlWriter));
	    errorMsg = e.getMessage();
	}

	//send email
	if (!success) {
	    mailSubject = "[Book Manage] There is an error on book management system.";
	    mailContent = "Please visit app engine to get detailed error message\r\n\r\n" + errorMsg;
	}
	Properties props = new Properties();
	Session session = Session.getDefaultInstance(props, null);
	try {
	    MimeMessage msg = new MimeMessage(session);
	    //http://www.hiteshagrawal.com/java/utf-8-encoding-email-content-using-java	    
	    msg.setHeader("Content-Type","text/plain; charset=UTF-8");
	    //msg.setHeader("Content-Transfer-Encoding", "quoted-printable");
	    
	    msg.setFrom(new InternetAddress("admin@sebookmanager.appspotmail.com", "BookManager"));
	    msg.addRecipient(Message.RecipientType.TO, new InternetAddress("bookmaster@se.kaist.ac.kr", "BookMaster"));
	    
	    msg.setSubject(mailSubject,"UTF-8");
	    msg.setText(mailContent,"UTF-8");
	    Transport.send(msg);
	    htmlWriter.write("</br></br>a messeage about this book is sent to book master(bookmaster@se.kaist.ac.kr)");
	} catch (AddressException e) {
	    e.printStackTrace(new PrintWriter(htmlWriter));

	} catch (MessagingException e) {
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

    // Change a date in another timezone  
    public static Date changeTimeZone(Date date, TimeZone zone) {
	Calendar first = Calendar.getInstance(zone);
	first.setTimeInMillis(date.getTime());

	Calendar output = Calendar.getInstance();
	output.set(Calendar.YEAR, first.get(Calendar.YEAR));
	output.set(Calendar.MONTH, first.get(Calendar.MONTH));
	output.set(Calendar.DAY_OF_MONTH, first.get(Calendar.DAY_OF_MONTH));
	output.set(Calendar.HOUR_OF_DAY, first.get(Calendar.HOUR_OF_DAY));
	output.set(Calendar.MINUTE, first.get(Calendar.MINUTE));
	output.set(Calendar.SECOND, first.get(Calendar.SECOND));
	output.set(Calendar.MILLISECOND, first.get(Calendar.MILLISECOND));

	return output.getTime();
    }
}
