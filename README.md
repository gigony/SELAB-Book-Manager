SELAB-Book-Manager
==================

GAE-based application that can manage book information by using  a spreadsheet and APIs on Google Docs.

I configured my lab's email infrastructure by using Google Apps on 2010, 
and people in my lab are using email/calendar service based on Google Apps until now.

There were so many public books in my lab and they were not managed well.
To make it easier for the people to use the books, I developed a kind of book rental system on top of Google Application Engine (GAE)
and connected with my lab's Google Apps.

People who need to borrow books require to connect a book rental page by using their ID of Google Apps and 
check available books, then click `borrow` button after getting books physically. 
Student who was chosen as a book master will be notified about borrowing and returning of books, through email.

Everyone was happy with this project.

Screenshots
-----------------
Click to view original images.
### Log-in
[![Login](https://github.com/gigony/SELAB-Book-Manager/raw/master/Screenshots/Login_th.png)](https://github.com/gigony/SELAB-Book-Manager/raw/master/Screenshots/Login.png)

### Main Page
[![MainPage](https://github.com/gigony/SELAB-Book-Manager/raw/master/Screenshots/MainPage_th.png)](https://github.com/gigony/SELAB-Book-Manager/raw/master/Screenshots/MainPage.png)

### Main Page2
[![MainPage2](https://github.com/gigony/SELAB-Book-Manager/raw/master/Screenshots/MainPage2_th.png)](https://github.com/gigony/SELAB-Book-Manager/raw/master/Screenshots/MainPage2.png)

Download
--------

`git clone git://github.com/gigony/SELAB-Book-Manager.git`

Usage & Development
-------------------
Click to view original images.

### Spreadsheet Settings
This project work with a Google Spreadsheet document containing three sheets.


##### BookList Sheet
[![SpreadSheet1](https://github.com/gigony/SELAB-Book-Manager/raw/master/Screenshots/SpreadSheet1_th.png)](https://github.com/gigony/SELAB-Book-Manager/raw/master/Screenshots/SpreadSheet1.png)


##### History Sheet
[![SpreadSheet2](https://github.com/gigony/SELAB-Book-Manager/raw/master/Screenshots/SpreadSheet2_th.png)](https://github.com/gigony/SELAB-Book-Manager/raw/master/Screenshots/SpreadSheet2.png)

This sheet also has information about \# of books and \# of names


##### Name Sheet 
[![SpreadSheet3](https://github.com/gigony/SELAB-Book-Manager/raw/master/Screenshots/SpreadSheet3_th.png)](https://github.com/gigony/SELAB-Book-Manager/raw/master/Screenshots/SpreadSheet3.png)

`Name + Email address`


Each sheet has unique address for accessing information from the sheet such as: 
  * BookList - http://spreadsheets.google.com/feeds/cells/ttRHdk6wziK4H-gC_ISC-UA/od6/private/full
  * History - http://spreadsheets.google.com/feeds/cells/ttRHdk6wziK4H-gC_ISC-UA/od7/private/full
  * Name - http://spreadsheets.google.com/feeds/cells/ttRHdk6wziK4H-gC_ISC-UA/od4/private/full

    (You can not access those addresses because those are private sheets)

Change above addresses in code to use this project for your own project.

And change `appengine-web.xml` to include account information for accessing document links.

[![Configuration](https://github.com/gigony/SELAB-Book-Manager/raw/master/Screenshots/Configuration_th.png)](https://github.com/gigony/SELAB-Book-Manager/raw/master/Screenshots/Configuration.png)



License
-------
[SELAB-Book-Manager is made available under the MIT license.](http://gigony.mit-license.org/2010)




TODO
----
  * Refactoring code (Currently, I have no plan to update)

