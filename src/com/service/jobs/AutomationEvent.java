package com.service.jobs;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bidcrm.auth.impl.EmailClickerImpl;
import com.bidcrm.data.util.JsonUtil;

import cms.service.template.TemplateTable;
import cms.service.template.TemplateUtility;
import cms.service.util.Base64Util;
import cms.service.util.PrintTime;

public class AutomationEvent extends EmailClickerImpl {
	
	static Log logger = LogFactory.getLog(AutomationEvent.class);
	TemplateUtility tu = new TemplateUtility();
	private String id;

	private Integer time;

	private String automationid;

	private String automationname;

	private String firstname;
	private String lastname;
	private String url;
	private String email;
	private String nextemailsettingid;
	private String emailresponseid;
	private String campaignid;
	private String emailsubject;
	private String stagecode;
	private String channelscode;
	private String groupuser;
	private String genuser;
	private String wait;
	private String contenttype;
	private String emailcontactobjid;

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setTime(Integer time) {
		this.time = time;
	}

	public Integer getTime() {
		return time;
	}

	public String getAutomationid() {
		return automationid;
	}

	public void setAutomationid(String automationid) {
		this.automationid = automationid;
	}

	public String getAutomationname() {
		return automationname;
	}

	public void setAutomationname(String automationname) {
		this.automationname = automationname;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNextemailsettingid() {
		return nextemailsettingid;
	}

	public void setNextemailsettingid(String nextemailsettingid) {
		this.nextemailsettingid = nextemailsettingid;
	}

	public String getEmailresponseid() {
		return emailresponseid;
	}

	public void setEmailresponseid(String emailresponseid) {
		this.emailresponseid = emailresponseid;
	}

	public String getCampaignid() {
		return campaignid;
	}

	public void setCampaignid(String campaignid) {
		this.campaignid = campaignid;
	}

	public String getEmailsubject() {
		return emailsubject;
	}

	public void setEmailsubject(String emailsubject) {
		this.emailsubject = emailsubject;
	}

	public String getStagecode() {
		return stagecode;
	}

	public void setStagecode(String stagecode) {
		this.stagecode = stagecode;
	}

	public String getChannelscode() {
		return channelscode;
	}

	public void setChannelscode(String channelscode) {
		this.channelscode = channelscode;
	}

	public String getGroupuser() {
		return groupuser;
	}

	public void setGroupuser(String groupuser) {
		this.groupuser = groupuser;
	}

	public String getGenuser() {
		return genuser;
	}

	public void setGenuser(String genuser) {
		this.genuser = genuser;
	}

	public String getWait() {
		return wait;
	}

	public void setWait(String wait) {
		this.wait = wait;
	}
	
	public String getContenttype() {
		return contenttype;
	}

	public void setContenttype(String contenttype) {
		this.contenttype = contenttype;
	}

	public String getEmailcontactobjid() {
		return emailcontactobjid;
	}

	public void setEmailcontactobjid(String emailcontactobjid) {
		this.emailcontactobjid = emailcontactobjid;
	}

	public boolean executeTask() {

		this.url = "https://www.bidcrm.com/" + this.url;
		String emailcontent = "";
		String templateid = "";
		String surveyurl = "";
		String videourl = "";
		String emailcontentreplaced = "";
		String portalurl = "";
		
		String clickerscript = "";
		int sent = 0;
		int failed = 0;
		
		if (!tu.isEmptyValue(contenttype) && contenttype.equalsIgnoreCase("html")) {
			this.ishtmlbody = true;
		}

		try {
			emailcontent = JsonUtil.readURLFile(url);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		if (!tu.isEmptyValue(emailcontent)) {

			String attrsql = "select *from table_emailattribute where emailattribute2emailsetting='" + nextemailsettingid + "'";
			TemplateTable attrs = tu.getResultSet(attrsql);
			if (attrs != null && attrs.getRowCount() > 0) {
				for (int i = 0; i < attrs.getRowCount(); i++) {
					String attrname = attrs.getFieldValue("name", i);
					String attrval = attrs.getFieldValue("value", i);
					String attrtype = attrs.getFieldValue("attributetype", i);
					String linktext = attrs.getFieldValue("linktext", i);
					if (attrval.contains("https:") || attrval.contains("http:") || attrval.contains("/bidcrm")) {
						attrval = attrval.replaceAll(" ", "");
					}

					emailcontent = emailcontent.replace("@" + attrname, attrval);
				}
			}
		}
		// replace portalurl and surveyurl
		if (!tu.isEmptyValue(campaignid)) {
			TemplateTable campaign = tu.getResultSet("select *from table_campaign where objid='" + campaignid + "'");
			if (campaign != null && campaign.getRowCount() > 0) {
				surveyurl = campaign.getFieldValue("surveyurl", campaign.getRowCount() - 1);
				videourl = campaign.getFieldValue("videourl", campaign.getRowCount() - 1);
			}
		}

		sendto = email;
		
		TemplateTable emailcontact = tu.getResultSet("select c.* from table_emailcontact c, table_contactlist l "
				+ " where c.emailcontact2emaillist=l.contactlist2emaillist and l.contactlist2campaign='" + campaignid 
				+ "' and upper(c.email)=upper('"+email+"')");
		
		if(emailcontact!=null &&emailcontact.getRowCount()>0){
			emailcontactobjid=emailcontact.getFieldValue("objid", emailcontact.getRowCount()-1);
		}

		emailcontentreplaced = emailcontent;
		if (!tu.isEmptyValue(firstname)) {
			emailcontentreplaced = emailcontentreplaced.replaceAll("Hi there", "Hi " + firstname);
			emailcontentreplaced = emailcontentreplaced.replaceAll("Hi There", "Hi " + firstname);
			emailcontentreplaced = emailcontentreplaced.replaceAll("hi there", "Hi " + firstname);
			emailcontentreplaced = emailcontentreplaced.replaceAll("@firstname", firstname);
			emailcontentreplaced = emailcontentreplaced.replaceAll("hello", "Hi " + firstname);
			emailcontentreplaced = emailcontentreplaced.replaceAll("Hello", "Hi " + firstname);

		}

		String userlogin = "";
		if (!tu.isEmptyValue(genuser)) {
			TemplateTable user = tu.getResultSet("select loginname||';'||password as userlogin "
					+ "from table_user where loginname='" + genuser + "' and groupuser='" + groupuser + "'");
			if (user != null && user.getRowCount() > 0) {
				userlogin = user.getFieldValue("userlogin", user.getRowCount() - 1);
			}
		}

		String portaltoken = "";
		try {
			portaltoken = new String(Base64Util.encode(userlogin.trim().getBytes()));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		portalurl = "https://www.bidcrm.com/bidcrm/portal&#63;referer=" + emailcontactobjid + "&action=sampleportal-"
				+ campaignid + "&setter=" + nextemailsettingid + "&servicekey=";

		if (!tu.isEmptyValue(portalurl)) {

			portalurl = portalurl + portaltoken;

			portalurl = "<a href=\"" + portalurl + "\">Click Here</a>";
			emailcontentreplaced = emailcontentreplaced.replace("@portalurl", portalurl);
		}
		if (!tu.isEmptyValue(surveyurl)) {

			emailcontentreplaced = emailcontentreplaced.replace("@surveyurl", surveyurl);
		}
		if (!tu.isEmptyValue(videourl)) {

			emailcontentreplaced = emailcontentreplaced.replace("@videourl", videourl);
		}
		try {
			doSetup();
			String imgurl = "https://www.bidcrm.com/bidcrm/portal/" + campaignid + "-" + emailcontactobjid + "-"
					+ nextemailsettingid + "/open.gif";
			String tracker = "<div id=\"div1\" style=\"visibility: hidden\">" + "<img  src=\"" + imgurl
					+ "\"  style=\"display:none\" alt=\"\" />" + "</div>";
			if (emailcontentreplaced.contains("@tracker")) {
				emailcontentreplaced = emailcontentreplaced.replaceAll("@tracker", tracker);
			} else if (emailcontentreplaced.contains("</body>")) {
				emailcontentreplaced = emailcontentreplaced.replaceAll("</body>", tracker);
			} else if (emailcontentreplaced.contains("</html>")) {
				emailcontentreplaced = emailcontentreplaced.replaceAll("</html>", tracker);
			} else {
				emailcontentreplaced += tracker;
			}

			String clickurl = "https://www.bidcrm.com/bidcrm/portal/" + campaignid + "-" + emailcontactobjid + "-"
					+ nextemailsettingid + "/click.gif";
			String script = "\n<script>" + "\n" + clickerscript + "\n" + getClicker(clickurl) + "\n</script>";
			//logger.info(script);
			if (emailcontentreplaced.contains("@clicker")) {

				emailcontentreplaced = emailcontentreplaced.replaceAll("@clicker", clickurl);
			} else if (emailcontentreplaced.contains("<head>")) {
				emailcontentreplaced = emailcontentreplaced.replace("<head>", "<head>\n" + script);
			} else {
				emailcontentreplaced = "<head>\n" + script + "</head>";
			}

			//logger.info(emailcontentreplaced);
			// finally add script

			if (sendmail(emailsubject, emailcontentreplaced)) {
				sent++;
				PrintTime time= new PrintTime();
				String now=time.getDateByFormat(0, null);
				String insertauto="Insert into TABLE_AUTOMATIONLOG (OBJID,NAME,DESCRIPTION,STAGECODE,CHANNELSCODE,STATUS,"+
						"AUTOMATIONLOG2EMAILSETTING,AUTOMATIONLOG2EMAILRESPONSE,BQN,ORIGINID,DESTINITIONID,"+
						"GROUPUSER,GENUSER,GENDATE,MODUSER,MODDATE) values ("+
								tu.getPrimaryKey()+",'"+
						        this.getEmailsubject()+"','"+
								" Sent " +this.getEmailsubject()+ " at "+now+"','"+
								this.stagecode+"','"+
								this.channelscode+"','"+
								"Sent','"+
								this.nextemailsettingid+"','"+
								this.emailresponseid+"',"+
								"null,null,null,'"+
								this.genuser+"','"+
								this.groupuser+"',"+
								"sysdate,null,null)";
				
				tu.executeQuery(insertauto);
				
			} else {
				failed++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	

}