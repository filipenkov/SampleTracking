package com.atlassian.renderer.wysiwyg;

/**
 * Created by IntelliJ IDEA.
 * User: Tomd
 * Date: 14/07/2005
 * Time: 17:10:22
 * To change this template use File | Settings | File Templates.
 */
public class TestBigMarkup extends WysiwygTest
{
    public void testFAQ()
    {
        testMarkup("This is an FAQ of frequently requested email questions and communications.\n" +
                "\nh2. Quick Nav {anchor:Quick Nav}\n\n" +
                "# [*Intros And Signoffs*|#Intros And Signoffs Section]\n" +
                "# [*Evaluating*|#Evaluating Section]\n" +
                "# [*Purchasing*|#Purchasing Section]\n" +
                "# [*Product Features and Benefits*|#Product Features and Benefits Section]\n" +
                "# [*Product Comparisons*|#Product Comparisons Section]\n" +
                "# [*Setting Up and Using JIRA*|#Setting Up and Using JIRA Section]\n" +
                "# [*Open Source and Non Profit*|#Open Source and Non Profit Section]\n" +
                "# [*Upgrades*|#Upgrades Section]\n" +
                "# [*Renewals & Maintenance*|#Renewals and Maintenance Section]\n" +
                "# [*Pricing Page*|#Pricing Page Section]\n" +
                "# [*Partners Page*|#Partners Page Section]\n" +
                "# [*Licensing & License Agreements*|#Licensing and License Agreements Section]\n" +
                "# [*Marketing Requests*|#Marketing Requests Section]\n" +
                "# [*Human Resources Page*|#Human Resources Page Section]\n" +
                "# [*Customer Profiles*|#Customer Profiles Section]" +
                "\\\\\n" +
                "\\\\\n" +
                "Back to [top|#Quick Nav]" +
                "\\\\\n" +
                "\\\\\n" +
                "h2. Questions\n" +
                "\n" +
                "# [*Intros And Signoffs*|#Intros And Signoffs] {anchor:Intros And Signoffs Section}\n" +
                "## [*Intros*|#Intros]\n" +
                "## [*Signoffs*|#Signoffs]\n" +
                "## [*For Ben*|#For Ben]" +
                "\\\\\n" +
                "\\\\\n" +
                "Back to [top|#Quick Nav] or [section|#Intros And Signoffs Section]" +
                "\\\\\n" +
                "\\\\\n" +
                "# [*Evaluating*|#Evaluating] {anchor:Evaluating Section}\n" +
                "## [*JIRA*|#JIRA Evaluating]\n" +
                "### [#JIRA Evaluation Addition]\n" +
                "### [#JIRA Evaluation Extension]\n" +
                "### [#JIRA Evaluation Extension via Other Contact]\n" +
                "### [#JIRA WebEx Followup]\n" +
                "### [#Evaluation License Generation Problems JIRA]\n" +
                "### [Accessing Via License Key|#Accessing Via License Key JIRA]\n" +
                "### [Extended Evaluations|#Extended Evaluations JIRA]\n" +
                "## [*Confluence*|#Confluence Evaluating]\n" +
                "### [#Confluence Evaluation Addition]\n" +
                "### [#Confluence Evaluation Extension]\n" +
                "### [#Confluence Evaluation Extension via Other Contact]\n" +
                "### [#Ran out of Time to Evaluate Confluence]\n" +
                "### [Evaluation License Generation Problems|#Evaluation License Generation Problems Confluence]\n" +
                "### [Accessing Via License Key|#Accessing Via License Key Confluence]\n" +
                "### [Extended Evaluations|#Extended Evaluations Confluence]\n" +
                "## [*Both Products*|#Both Products Evaluating]\n" +
                "### [#Both Products Evaluation Addition]\n" +
                "### [#Both Products Evaluation Extension]\n" +
                "### [#Both Products Evaluation Extension via Other Contact]\n" +
                "### [Evaluation License Generation Problems|#Evaluation License Generation Problems Both Products]\n" +
                "### [Extended Evaluations|#Extended Evaluations Both Products]\n" +
                "## [*Evaluator Report Standard Emails*|#Evaluator Report Standard Emails]\n" +
                "### [*JIRA*|#JIRA Evaluator Report Standard Emails]\n" +
                "#### [#JIRA Evaluation 0 to 7 Days]\n" +
                "#### [#JIRA Evaluation 0 to 7 Days Already Had Reply From Support]\n" +
                "#### [#JIRA Evaluation 23 to 30 Days]\n" +
                "### [*Confluence*|#Confluence Evaluator Report Standard Emails]\n" +
                "#### [#Confluence Evaluation 0 to 7 Days]\n" +
                "#### [#Confluence Evaluation 23 to 30 Days]\n" +
                "### [*Both Products*|#Both Products Evaluator Report Standard Emails]\n" +
                "#### [#Both Products Evaluation 0 to 7 Days]\n" +
                "#### [#Both Products Evaluation 23 to 30 Days]\n" +
                "### [*New Related User Blurbs*|#New Related User Blurbs]\n" +
                "#### [0 to 7 Days|#0 to 7 Days New Related User Blurbs]\n" +
                "#### [23 to 30 Days|#23 to 30 Days New Related User Blurbs]" +
                "\\\\\n" +
                "\\\\\n" +
                "Back to [top|#Quick Nav] or [section|#Evaluating Section]" +
                "\\\\\n" +
                "\\\\\n" +
                "# [*Purchasing*|#Purchasing] {anchor:Purchasing Section}\n" +
                "## [*General*|#General Purchasing]\n" +
                "### [#Transaction Difficulties]\n" +
                "#### [Transaction Difficulties No Repsonse|#Transaction Difficulties No Response]\n" +
                "#### [#Transaction Difficulties Second Attempt]\n" +
                "### [#Payment not yet Identified]\n" +
                "### [#Further Payment Required]\n" +
                "### [#Purchase Orders With Unreasonable Terms]\n" +
                "#### [#Purchase Order Requires License Release Prior to Payment]\n" +
                "#### [#Purchase Order With Unreasonable Terms Generally]\n" +
                "#### [#Purchase Orders from Quotes]\n" +
                "### [#RFP Request]\n" +
                "### [#Quote Request]\n" +
                "### [#Request for Delivery of Software in Physical Form]\n" +
                "### [#Outstanding Invoices for Services]\n" +
                "### [#Product Support Concerns]\n" +
                "### [#Request for License Prior to Confirmation]\n" +
                "### [#Request for More Than One Technical Contact]\n" +
                "### [#Technical Contact Request]\n" +
                "### [#Tech Contact for Invoice Emails]\n" +
                "### [#Remittance Advice]\n" +
                "### [#Tax Issues]\n" +
                "#### [#Requesting Withholding Tax Forms]" +
                "\\\\\n" +
                "\\\\\n" +
                "## [*JIRA*|#JIRA Purchasing]\n" +
                "### [*Purchase Requests*|#Purchase Requests JIRA]\n" +
                "#### [#JIRA Purchase Request]\n" +
                "#### [Small Business or Split Payment Standard|#Small Business or Split Payment JIRA Standard]\n" +
                "#### [Split Payment Professional|#Split Payment JIRA Professional]\n" +
                "#### [Split Payment Enterprise|#Split Payment JIRA Enterprise]\n" +
                "#### [Enterprise Discount Request|#Enterprise Discount Request JIRA]\n" +
                "#### [Academic License Request|#Academic License Request JIRA]\n" +
                "#### [Perforce Plugin|#Perforce Plugin Request JIRA]\n" +
                "#### [Purchase Orders|#Purchase Orders JIRA]\n" +
                "#### [#Lapsed JIRA License - Purchasing a New License]\n" +
                "### [*Purchase Confirmations*|#Purchase Confirmations JIRA]\n" +
                "#### [Enterprise|#Enterprise JIRA Purchase Confirmations]\n" +
                "#### [Professional|#Professional JIRA Purchase Confirmations]\n" +
                "#### [Standard|#Standard JIRA Purchase Confirmations]\n" +
                "#### [Academic|#Academic JIRA Purchase Confirmations]\n" +
                "#### [Renewals|#Renewals JIRA Purchase Confirmations]\n" +
                "#### [#Free JIRA T shirt]\n" +
                "#### [Perforce Plugin|#Perforce Plugin Purchase Confirmations]\n" +
                "#### [Small Business/ Split Payment: Second Payment Confirmation|#Small Business Split Payment Second Payment Confirmation JIRA]\n" +
                "#### [Refund Overpayment|#Refund Overpayment JIRA]\n" +
                "### [*Purchase Invoices*|#Purchase Invoices JIRA]\n" +
                "#### [Enterprise|#Enterprise JIRA Purchase Invoices]\n" +
                "#### [Professional|#Professional JIRA Purchase Invoices]\n" +
                "#### [Standard|#Standard JIRA Purchase Invoices]\n" +
                "#### [Academic|#Academic JIRA Purchase Invoices]\n" +
                "#### [Renewals|#Renewals JIRA Purchase Invoices]\n" +
                "#### [#JIRA Quote]\n" +
                "#### [PO Addition to Invoice|#PO Addition to Invoice JIRA]\n" +
                "### [*Outstanding/Changed Invoices*|#Outstanding/Changed Invoices JIRA]\n" +
                "#### [Outstanding Invoices 30 Days|#Outstanding Invoices 30 Days JIRA]\n" +
                "#### [Outstanding Invoices 60 Days|#Outstanding Invoices 60 Days JIRA]\n" +
                "#### [Outstanding Invoices 90\\+ Days|#Outstanding Invoices 90+ Days JIRA]\n" +
                "#### [Outstanding Renewal Invoices|#Outstanding Renewal Invoices JIRA]\n" +
                "#### [#Outstanding JIRA Invoices Price Rise]\n" +
                "#### [#Current JIRA Invoices Price Rise]\n" +
                "#### [Outstanding Quote Follow up|#Outstanding Quote Follow up JIRA]\n" +
                "#### [Invoice Nearly Outstanding Reminder|#Invoice Nearly Outstanding Reminder JIRA]" +
                "\\\\\n" +
                "\\\\\n" +
                "## [*Confluence*|#Confluence Purchasing]\n" +
                "### [*Purchase Requests*|#Purchase Requests Confluence]\n" +
                "#### [#Confluence Purchase Request]\n" +
                "#### [Small Business or Split Payment Team|#Small Business or Split Payment Confluence Team]\n" +
                "#### [Split Payment Workgroup|#Split Payment Confluence Workgroup]\n" +
                "#### [Split Payment Enterprise|#Split Payment Confluence Enterprise]\n" +
                "#### [#Confluence Academic License Request]\n" +
                "#### [Purchase Orders|#Purchase Orders Confluence]\n" +
                "#### [#Lapsed Confluence License - Purchasing a New License]\n" +
                "### [*Purchase Confirmations*|#Purchase Confirmations Confluence]\n" +
                "#### [Enterprise|#Enterprise Confluence Purchase Confirmations]\n" +
                "#### [Workgroup|#Workgroup Confluence Purchase Confirmations]\n" +
                "#### [Team|#Team Confluence Purchase Confirmations]\n" +
                "#### [Academic|#Academic Confluence Purchase Confirmations]\n" +
                "#### [Renewals|#Renewals Confluence Purchase Confirmations]\n" +
                "#### [#Free Confluence T shirt]\n" +
                "#### [Small Business/ Split Payment: Second Payment Confirmation|#Small Business Split Payment Second Payment Confirmation Confluence]\n" +
                "#### [Refund Overpayment|#Refund Overpayment Confluence]\n" +
                "### [*Purchase Invoices*|#Confluence Purchase Invoices]\n" +
                "#### [Enterprise|#Enterprise Confluence Purchase Invoices]\n" +
                "#### [Workgroup|#Workgroup Confluence Purchase Invoices]\n" +
                "#### [Team|#Team Confluence Purchase Invoices]\n" +
                "#### [Academic|#Academic Confluence Purchase Invoices]\n" +
                "#### [Renewals|#Renewals Confluence Purchase Invoices]\n" +
                "#### [Quote|#Confluence Quote]\n" +
                "#### [PO Addition to Invoice|#PO Addition to Invoice Confluence]\n" +
                "### [*Outstanding/Changed Invoices*|#Outstanding/Changed Invoices Confluence]\n" +
                "#### [Outstanding Invoices 30 Days|#Outstanding Invoices 30 Days Confluence]\n" +
                "#### [Outstanding Invoices 60 Days|#Outstanding Invoices 60 Days Confluence]\n" +
                "#### [Outstanding Invoices 90\\+ Days|#Outstanding Invoices 90+ Days Confluence]\n" +
                "#### [Outstanding Renewal Invoices|#Outstanding Renewal Invoices Confluence]\n" +
                "#### [Outstanding Quote Follow up|#Outstanding Quote Follow up Confluence]\n" +
                "#### [Invoice Nearly Outstanding Reminder|#Invoice Nearly Outstanding Reminder Confluence]" +
                "\\\\\n" +
                "\\\\\n" +
                "## [*Both Products*|#Both Products Purchasing]\n" +
                "### [*Purchase Requests*|#Purchase Requests Both Products]\n" +
                "#### [Any Versions|#Any Versions Both Products]\n" +
                "#### [Academic|#Both Products Academic Purchase Requests]\n" +
                "#### [#JIRA Standard and Confluence Team]\n" +
                "#### [Discount or Split Payment Both Products - JIRA Standard and Confluence Team|#Discount or Split Payment JIRA Standard and Confluence Team]\n" +
                "#### [Discount or Split Payment Both Products - Other Editions|#Discont or Split Payment Both Products Other Editions]\n" +
                "#### [#Lapsed Licenses Both Products - Purchasing New Licenses]\n" +
                "### [*Purchase Confirmations*|#Purchase Confirmations Both Products]\n" +
                "#### [Enterprise|#Enterprise Both Products Purchasing]\n" +
                "#### [Commercial|#Commercial Both Products Purchasing]\n" +
                "#### [Academic|#Academic Both Products Purchasing]\n" +
                "#### [#Free T shirts Both Products]\n" +
                "#### [Small Business/ Split Payment: Second Payment Confirmation|#Small Business Split Payment Second Payment Confirmation Both Products]\n" +
                "### [*Outstanding Invoices*|#Outstanding Invoices Both Products]\n" +
                "#### [#JIRA and Confluence Combined Outstanding Invoices]\n" +
                "#### [Invoice Nearly Outstanding Reminder|#Invoice Nearly Outstanding Reminder Both Products]" +
                "\\\\\n" +
                "\\\\\n" +
                "Back to [top|#Quick Nav] or [section|#Purchasing Section]" +
                "\\\\\n" +
                "\\\\\n" +
                "# [*Product Features and Benefits*|#Product Features and Benefits] {anchor:Product Features and Benefits Section}\n" +
                "## [*General*|#General Product Features and Benefits]\n" +
                "### [#Feature Requests Long]\n" +
                "### [#Feature Requests Short]\n" +
                "### [#Source Access]\n" +
                "### [#Ownership of Custom Code]\n" +
                "### [#Customer Testimonials Permission]\n" +
                "## [*JIRA*|#JIRA Product Features and Benefits]\n" +
                "### [*General Features*|#General JIRA Product Features and Benefits]\n" +
                "#### [#Enterprise Features and Benefits]\n" +
                "#### [#Helpdesk Software]\n" +
                "#### [#Security Concerns]\n" +
                "#### [#JIRA via ASP]\n" +
                "#### [#Expected Bandwidth Usage per Client]\n" +
                "#### [#Little or No Administration Required]\n" +
                "#### [#Simplicity and Ease of Use]\n" +
                "#### [#Intuitive User Interface]\n" +
                "#### [#Web-based Client]\n" +
                "#### [Sales' Access to Customer Reported Issues and the Status/ Resolution|#Sales' Access to Customer Reported Issues and the Status Resolution]\n" +
                "#### [#SLA (Service Level Agreement) Monitoring Facility]\n" +
                "#### [#Multi Customer Support]\n" +
                "#### [#Multi Location Support]\n" +
                "#### [#Built in Spell Checker]\n" +
                "#### [#Searchable Knowledge Database]\n" +
                "#### [#Microsoft Excel Integration]\n" +
                "#### [#Notice Board]\n" +
                "#### [#Canned Answers and Dynamic FAQ]\n" +
                "#### [#Support PDA/WAP]\n" +
                "#### [#Web Surveys]\n" +
                "#### [#Voice Mail Logging]\n" +
                "### [*Installation and Configuration*|#Installation and Configuration]\n" +
                "#### [#Running JIRA Under SSL]\n" +
                "#### [#Storing user/customer data in JIRA]\n" +
                "#### [#User Management]\n" +
                "#### [#Running JIRA on Windows]\n" +
                "#### [#Secured/ Encrypted Authentication to the Server]\n" +
                "#### [#Privelege Determined Access for Customers]\n" +
                "#### [#Linux for the Server is a Preference]\n" +
                "#### [#File Attachments]\n" +
                "### [*Project Management*|#Project Management]\n" +
                "#### [#Support of Critical Path Diagrams]\n" +
                "#### [#Resource allocation per version/phase]\n" +
                "#### [#Time estimates for each phase]\n" +
                "#### [Sub-tasks|#Sub tasks]\n" +
                "#### [#Due Date]\n" +
                "#### [#Ability to put Ticket through Different Phases (Flexible Workflow)]\n" +
                "#### [#History of Assignees Including Date and Time Stamps]\n" +
                "#### [#Interface to Maintain Users, Passwords and Privelege Levels]\n" +
                "### [*Issues* (Tickets)|#Issues JIRA Product Features and Benefits]\n" +
                "#### [#Issue Escalation]\n" +
                "#### [#Ability to Dump Ticket Data to a Text File]\n" +
                "#### [#Ability to Print Ticket Easily as you are Viewing it]\n" +
                "#### [#Fast Keyword Searching]\n" +
                "#### [#Fields are Configurable]\n" +
                "#### [#Fields can be Mandatory and Blank on Initially Opening Ticket]\n" +
                "#### [Submitting Issues Through a Web-based Form|#Submitting Issues Through a Web based Form]\n" +
                "#### [#Extracting a List of Issues from JIRA to an External System]\n" +
                "#### [#Can fields (such as the description box) have default text]\n" +
                "#### [#Fields have Drop Down Lists]\n" +
                "#### [#Support for Agile Process]\n" +
                "#### [#Ticket Scheduling and To Do Calendar]\n" +
                "#### [#Automatic Assignment of Issues]\n" +
                "#### [#Severity Table that Automatically Calculates and Suggests a Due Date of a Case]\n" +
                "#### [#Can a group (eg developers) be restricted from closing a bug]\n" +
                "#### [#Change history are non-deletable]\n" +
                "### [*Email Notifications*|#Email Notifications]\n" +
                "#### [#Email notifications]\n" +
                "#### [#Reminder emails]\n" +
                "#### [#Integration with email client]\n" +
                "#### [#Creating Issues From email]\n" +
                "#### [#Notifications]\n" +
                "#### [#Immediate Notifications for Bug Changes]\n" +
                "#### [Email Notification of New Ticket/ Variety Of Notifications|#Email Notification of New Ticket Variety Of Notifications]\n" +
                "#### [#Email Reminders that CRs are in your Queue]\n" +
                "#### [#Email Tickets into Bug Tracker]\n" +
                "#### [#Rule Based Email and Paging Notification]\n" +
                "### [*Reporting*|#Reporting]\n" +
                "#### [#Reporting]\n" +
                "### [*Customisation and Extendibility*|#Customisation and Extendibility]\n" +
                "#### [#Opening JIRA up to Customers]\n" +
                "#### [#Plugins]\n" +
                "#### [Source Access|#Source Access JIRA]\n" +
                "#### [#Limiting Attachment Sizes]\n" +
                "#### [#Ability to have Numerous Interfaces that Access the same Database]\n" +
                "#### [#Automatically Opening Ticket Based on System Event]\n" +
                "#### [#Built-in Escalation that can be Customized]\n" +
                "#### [#Expert System that is Automatically fed by Tickets Opened]\n" +
                "#### [#No Multiple Entry of Description Info/ Perforce Integration]" +
                "\\\\\n" +
                "\\\\\n" +
                "## [*Confluence*|#Confluence Products Features and Benefits]\n" +
                "### [#Overview for Software Development Use]\n" +
                "### [#Confluence integration with JIRA]\n" +
                "### [#Why Choose Confluence]" +
                "\\\\\n" +
                "\\\\\n" +
                "Back to [top|#Quick Nav] or [section|#Product Features and Benefits Section]" +
                "\\\\\n" +
                "\\\\\n" +
                "# [*Product Comparisons*|#Product Comparisons] {anchor:Product Comparisons Section}\n" +
                "## [*General*|#General Product Comparisons]\n" +
                "## [*JIRA*|#JIRA Product Comparisons]\n" +
                "### [JIRA Vs Bugzilla|# JIRA Vs Bugzilla]\n" +
                "### [#JIRA Vs TestTrack Pro]\n" +
                "### [#JIRA Vs MS Project]\n" +
                "## [*Confluence*|#Confluence Product Comparisons]\n" +
                "### [#Confluence Vs SnipSnap]\n" +
                "### [#Confluence vs Jotspot vs SocialText]\n" +
                "### [#Confluence vs Message Boards]" +
                "\\\\\n" +
                "\\\\\n" +
                "Back to [top|#Quick Nav] or [section|#Product Comparisons Section]" +
                "\\\\\n" +
                "\\\\\n" +
                "# [*Setting Up and Using JIRA*|#Setting Up and Using JIRA] {anchor:Setting Up and Using JIRA Section}\n" +
                "## [*General*|#General Setting Up and Using JIRA]\n" +
                "## [*JIRA*|#JIRA Setting Up and Using JIRA]\n" +
                "### [#For Business Projects]\n" +
                "### [#Importing Issues into JIRA]\n" +
                "## [*Confluence*|#Confluence Setting Up and Using JIRA]" +
                "\\\\\n" +
                "\\\\\n" +
                "Back to [top|#Quick Nav] or [section|#Setting Up and Using JIRA Section]" +
                "\\\\\n" +
                "\\\\\n" +
                "# [*Open Source and Non Profit*|#Open Source and Non Profit] {anchor:Open Source and Non Profit Section}\n" +
                "## [*General*|#General Open Source and Non Profit]\n" +
                "### [#Not aware of Open Source Licenses]\n" +
                "## [*JIRA*|#JIRA Open Source and Non Profit]\n" +
                "### [Open Source License Request|#Open Source License Request JIRA]\n" +
                "### [Open Source License More Required|#Open Source License More Required JIRA]\n" +
                "### [Open Source License Grant|#Open Source License Grant JIRA]\n" +
                "### [Non-Profit Request|#Non Profit Request JIRA]\n" +
                "### [Non-Profit More Required|#Non Profit More Required JIRA]\n" +
                "### [Non-Profit Grant|#Non Profit Grant JIRA]\n" +
                "## [*Confluence*|#Confluence Open Source and Non Profit]\n" +
                "### [Open Source License Request|#Open Source License Request Confluence]\n" +
                "### [Open Source License Request More Required|#Open Source License Request More Required Confluence]\n" +
                "### [Open Source License Grant|#Open Source License Grant Confluence]\n" +
                "### [Non-Profit Request|#Non Profit Request Confluence]\n" +
                "### [Non-Profit Grant|#Non Profit Grant Confluence]\n" +
                "## [*Combined*|#Combined Open Source and Non Profit]\n" +
                "### [Combined JIRA/Confluence Grants|#Combined JIRA and Confluence Grants]\n" +
                "## [*Personal Licenses*|#Personal Licenses]\n" +
                "### [#Confluence Personal License]" +
                "\\\\\n" +
                "\\\\\n" +
                "Back to [top|#Quick Nav] or [section|#Open Source and Non Profit Section]" +
                "\\\\\n" +
                "\\\\\n" +
                "# [*Upgrades*|#Upgrades] {anchor:Upgrades Section}\n" +
                "## [*General*|#General Upgrades]\n" +
                "### [#How to Upgrade your JIRA Instance]\n" +
                "### [#How to Downgrade your JIRA Instance]\n" +
                "## [*JIRA*|#JIRA Upgrades]\n" +
                "### [Enterprise Commercial Upgrade - From Professional to Enterprise|#Enterprise Commercial Upgrade From Professional to Enterprise]\n" +
                "### [Enterprise Academic Upgrade - From Professional to Enterprise|#Enterprise Academic Upgrade From Professional to Enterprise]\n" +
                "### [#Downgrading JIRA versions]\n" +
                "## [*Confluence*|#Confluence Upgrades]\n" +
                "### [Requesting Enterprise Upgrade - From 25 Users|#Requesting Enterprise Upgrade From 25 Users]\n" +
                "### [Requesting Enterprise Upgrade - From 50 Users|#Requesting Enterprise Upgrade From 50 Users]\n" +
                "### [#Requesting 50 User Upgrade From 25 Users]\n" +
                "### [Requesting Academic Team > Workgroup Upgrade|#Requesting Confluence Academic Team to Workgroup Upgrade]" +
                "\\\\\n" +
                "\\\\\n" +
                "Back to [top|#Quick Nav] or [section|#Upgrades Section]" +
                "\\\\\n" +
                "\\\\\n" +
                "# [*Renewals & Maintenance*|#Renewals and Maintenance] {anchor:Renewals and Maintenance Section}\n" +
                "## [*General*|#General Renewals and Maintenance]\n" +
                "### [#Do I Have To Renew?]\n" +
                "### [Am I Entitled To The Latest Update - Within Maintenance|#Am I Entitled To The Latest Update Within Maintenance]\n" +
                "### [#Complaints about Support During Renewal]\n" +
                "### [#Requesting JIRA & Confluence Enterprise Joint Renewal]\n" +
                "## [*JIRA*|#JIRA Renewals and Maintenance]\n" +
                "### [#Requesting JIRA Standard Maintenance Renewal]\n" +
                "### [#Requesting JIRA Professional Maintenance Renewal]\n" +
                "### [#Requesting JIRA Enterprise Maintenance Renewal]\n" +
                "### [#Requesting JIRA Academic Professional Maintenance Renewal]\n" +
                "### [#JIRA Update Request for Expired License]\n" +
                "### [#JIRA Standard Request for Update Outside of Maintenance]\n" +
                "### [#JIRA Professional Request for Update Outside of Maintenance]\n" +
                "### [#JIRA Enterprise Request for Update Outside of Maintenance]\n" +
                "### [Why Renew JIRA?|#Why Renew JIRA]\n" +
                "### [#JIRA Professional Renewal Price Objection]\n" +
                "### [#JIRA Professional Renewal Price Objection 2]\n" +
                "### [Requesting JIRA Renewal - Xmas Renewal Offer|#Requesting JIRA Renewal Xmas Renewal Offer]\n" +
                "### [#Xmas Renewal Promotional Email]\n" +
                "## [Renewal Reminder Emails|#JIRA Renewal Reminder Emails]\n" +
                "### [#60 Days JIRA Template]\n" +
                "### [60 Days Pre-expiry Emails|#JIRA 60 Days Pre-expiry Emails]\n" +
                "#### [JIRA Standard Renewal Email|#JIRA Standard Renewal Email 60 Days]\n" +
                "#### [JIRA Professional Renewal Email|#JIRA Professional Renewal Email 60 Days]\n" +
                "#### [JIRA Enterprise Renewal Email|#JIRA Enterprise Renewal Email 60 Days]\n" +
                "#### [JIRA Standard Academic Renewal Email|#JIRA Standard Academic Renewal Email 60 Days]\n" +
                "#### [JIRA Professional Academic Renewal Email|#JIRA Professional Academic Renewal Email 60 Days]\n" +
                "#### [JIRA Enterprise Academic Renewal Email|#JIRA Enterprise Academic Renewal Email 60 Days]\n" +
                "### [#15 Days JIRA Template]\n" +
                "### [15 Days Pre-expiry Emails|#JIRA 15 Days Pre-expiry Emails]\n" +
                "#### [JIRA Standard Renewal Email|#JIRA Standard Renewal Email 15 Days]\n" +
                "#### [JIRA Professional Renewal Email|#JIRA Professional Renewal Email 15 Days]\n" +
                "#### [JIRA Enterprise Renewal Email|#JIRA Enterprise Renewal Email 15 Days]\n" +
                "#### [JIRA Standard Academic Renewal Email|#JIRA Standard Academic Renewal Email 15 Days]\n" +
                "#### [JIRA Professional Academic Renewal Email|#JIRA Professional Academic Renewal Email 15 Days]\n" +
                "#### [JIRA Enterprise Academic Renewal Email|#JIRA Enterprise Academic Renewal Email 15 Days]\n" +
                "### [#30 Days JIRA Template]\n" +
                "### [30 Days Post expiry Emails|#JIRA 30 Days Post expiry Emails]\n" +
                "#### [JIRA Standard Renewal Email|#JIRA Standard Renewal Email 30 Days]\n" +
                "#### [JIRA Professional Renewal Email|#JIRA Professional Renewal Email 30 Days]\n" +
                "#### [JIRA Enterprise Renewal Email|#JIRA Enterprise Renewal Email 30 Days]\n" +
                "#### [JIRA Standard Academic Renewal Email|#JIRA Standard Academic Renewal Email 30 Days]\n" +
                "#### [JIRA Professional Academic Renewal Email|#JIRA Professional Academic Renewal Email 30 Days]\n" +
                "#### [JIRA Enterprise Academic Renewal Email|#JIRA Enterprise Academic Renewal Email 30 Days]\n" +
                "## [*Confluence*|#Confluence Renewals and Maintenance]\n" +
                "### [#Requesting Confluence Team Maintenance Renewal]\n" +
                "### [#Requesting Confluence Workgroup Maintenance Renewal]\n" +
                "### [#Requesting Confluence Enterprise Maintenance Renewal]\n" +
                "## [Renewal Reminder Emails|#Confluence Renewal Reminder Emails]\n" +
                "### [#60 Days Confluence Template]\n" +
                "### [60 Days Pre-expiry Emails|#Confluence 60 Days Pre-expiry Emails]\n" +
                "#### [Confluence Team Renewal Email|#Confluence Team Renewal Email 60 Days]\n" +
                "#### [Confluence Workgroup Renewal Email|#Confluence Workgroup Renewal Email 60 Days]\n" +
                "#### [Confluence Enterprise Renewal Email|#Confluence Enterprise Renewal Email 60 Days]\n" +
                "#### [Confluence Team Academic Renewal Email|#Confluence Team Academic Renewal Email 60 Days]\n" +
                "#### [Confluence Workgroup Academic Renewal Email|#Confluence Workgroup Academic Renewal Email 60 Days]\n" +
                "#### [Confluence Enterprise Academic Renewal Email|#Confluence Enterprise Academic Renewal Email 60 Days]\n" +
                "### [#15 Days Confluence Template]\n" +
                "### [15 Days Pre-expiry Emails|#Confluence 15 Days Pre-expiry Emails]\n" +
                "#### [Confluence Team Renewal Email|#Confluence Team Renewal Email 15 Days]\n" +
                "#### [Confluence Workgroup Renewal Email|#Confluence Workgroup Renewal Email 15 Days]\n" +
                "#### [Confluence Enterprise Renewal Email|#Confluence Enterprise Renewal Email 15 Days]\n" +
                "#### [Confluence Team Academic Renewal Email|#Confluence Team Academic Renewal Email 15 Days]\n" +
                "#### [Confluence Workgroup Academic Renewal Email|#Confluence Workgroup Academic Renewal Email 15 Days]\n" +
                "#### [Confluence Enterprise Academic Renewal Email|#Confluence Enterprise Academic Renewal Email 15 Days]\n" +
                "### [#30 Days Confluence Template]\n" +
                "### [30 Days Post expiry Emails|#Confluence 30 Days Post expiry Emails]\n" +
                "#### [Confluence Team Renewal Email|#Confluence Team Renewal Email 30 Days]\n" +
                "#### [Confluence Workgroup Renewal Email|#Confluence PWorkgroup Renewal Email 30 Days]\n" +
                "#### [Confluence Enterprise Renewal Email|#Confluence Enterprise Renewal Email 30 Days]\n" +
                "#### [Confluence Team Academic Renewal Email|#Confluence Team Academic Renewal Email 30 Days]\n" +
                "#### [Confluence Workgroup Academic Renewal Email|#Confluence Workgroup Academic Renewal Email 30 Days]\n" +
                "#### [Confluence Enterprise Academic Renewal Email|#Confluence Enterprise Academic Renewal Email 30 Days]\n" +
                "#### [Team Renewal Email|#Team Renewal Email Confluence]\n" +
                "#### [Workgroup Renewal Email|#Workgroup Renewal Email Confluence]\n" +
                "#### [Enterprise Renewal Email|#Enterprise Renewal Email Confluence]" +
                "\\\\\n" +
                "\\\\\n" +
                "Back to [top|#Quick Nav] or [section|#Renewals and Maintenance Section]" +
                "\\\\\n" +
                "\\\\\n" +
                "# [*Pricing Page*|#Pricing Page] {anchor:Pricing Page Section}\n" +
                "## [*General*|#General Pricing]\n" +
                "### [#General Price Objection Including Multiple Purchase Dicounts]\n" +
                "### [#Product Pricing]\n" +
                "### [Licensing|#Licensing General Pricing]\n" +
                "## [*JIRA*|#JIRA Pricing]\n" +
                "### [#Discounts In Return For Language Pack]\n" +
                "### [#JIRA Price Objection]\n" +
                "## [*Confluence*|#Confluence Pricing]\n" +
                "### [#Confluence Price Objection]" +
                "\\\\\n" +
                "\\\\\n" +
                "Back to [top|#Quick Nav] or [section|#Pricing Page Section]" +
                "\\\\\n" +
                "\\\\\n" +
                "# [*Partners Page*|#Partners Page] {anchor:Partners Page Section}\n" +
                "## [*General*|#General Partners]\n" +
                "### [#Reseller Expressing Interest]\n" +
                "### [#VAR Expressing Interest]\n" +
                "### [Reminder|#Reminder Partners]\n" +
                "### [Internationalisation|#Internationalisation Partners]\n" +
                "### [General Terms|#General Terms Partners]\n" +
                "### [#VAR Agreement Signature Request]\n" +
                "### [#OEM Interest]\n" +
                "### [#Partner Purchase Request]\n" +
                "### [#JIRA Plugin Partner]\n" +
                "### [#Partner Exclusivity]\n" +
                "### [#Product Demonstration Licenses]\n" +
                "### [Welcome to Partner Program; Creating Own Password|#Welcome to Partner Program Creating Own Password]\n" +
                "### [#Unsigned Partner Agreement Followup]\n" +
                "## [*Purchase Confirmations VAR/ Reseller*|#Purchase Confirmations VAR Reseller]\n" +
                "### [JIRA Purchase Confirmations|#JIRA Purchase Confirmations VAR Reseller]\n" +
                "### [Confluence Purchase Confirmations|#Confluence Purchase Confirmations VAR Reseller]\n" +
                "### [Both Products Purchase Confirmations|#Both Products Purchase Confirmations VAR Reseller]\n" +
                "## [*Purchase Confirmations Technical Contact*|#Purchase Confirmations Technical Contact]\n" +
                "### [JIRA Purchase Confirmations|#JIRA Purchase Confirmations Technical Contact]\n" +
                "### [Confluence Purchase Confirmations|#Confluence Purchase Confirmations Technical Contact]\n" +
                "### [Both Products Purchase Confirmations|#Both Products Purchase Confirmations Technical Contact]" +
                "\\\\\n" +
                "\\\\\n" +
                "Back to [top|#Quick Nav] or [section|#Partners Page Section]" +
                "\\\\\n" +
                "\\\\\n" +
                "# [*Licensing & License Agreements*|#Licensing and License Agreements] {anchor:Licensing and License Agreements Section}\n" +
                "## [*General*|#General Licensing and License Agreements]\n" +
                "### [#Requesting Changes to the License Agreement]\n" +
                "### [#Requesting Changes to the License Agreement 2]\n" +
                "### [What Does My License Entitle Me To?|#What Does My License Entitle Me To]\n" +
                "## [*JIRA*|#JIRA Licensing and License Agreements]\n" +
                "## [*Confluence*|#Confluence Licensing and License Agreements]\n" +
                "### [#Confluence Personal License Request]" +
                "\\\\\n" +
                "\\\\\n" +
                "Back to [top|#Quick Nav] or [section|#Licensing and License Agreements Section]" +
                "\\\\\n" +
                "\\\\\n" +
                "# [*Marketing Requests*|#Marketing Requests] {anchor:Marketing Requests Section}\n" +
                "## [*Case Studies*|#Case Studies Marketing Requests]\n" +
                "### [JIRA|#JIRA Marketing Requests]\n" +
                "### [Confluence|#Confluence Marketing Requests]" +
                "\\\\\n" +
                "\\\\\n" +
                "Back to [top|#Quick Nav] or [section|#Marketing Requests Section]" +
                "\\\\\n" +
                "\\\\\n" +
                "# [*Human Resources Page*|#Human Resources Page] {anchor:Human Resources Page Section}\n" +
                "## [*Work Placement*|#Work Placement]\n" +
                "### [#Request From Overseas]" +
                "\\\\\n" +
                "\\\\\n" +
                "Back to [top|#Quick Nav] or [section|#Human Resources Page Section]" +
                "\\\\\n" +
                "\\\\\n" +
                "# [*Customer Profiles*|Customer Profiles] {anchor:Customer Profiles Section}\n" +
                "## [*General*|#General Customer Profiles]\n" +
                "### [#Address Check Email]\n" +
                "### [#Technical Contact Check]" +
                "\\\\\n" +
                "\\\\\n" +
                "Back to [top|#Quick Nav] or [section|#Customer Profiles Section]" +
                "\\\\\n" +
                "\\\\\n" +
                "{include:Intros and Signoffs}\n" +
                "{include:Evaluating}\n" +
                "{include:Purchasing}\n" +
                "{include:Product Features and Benefits}\n" +
                "{include:Product Comparisons}\n" +
                "{include:Setting up and Using JIRA}\n" +
                "{include:Open Source and Non Profit}\n" +
                "{include:Upgrades}\n" +
                "{include:Renewals and Maintenance}\n" +
                "{include:Pricing Page}\n" +
                "{include:Partners Page}\n" +
                "{include:Licensing and License Agreements}\n" +
                "{include:Marketing Requests}\n" +
                "{include:Human Resources Page}\n" +
                "{include:Customer Profiles}");
    }
}
