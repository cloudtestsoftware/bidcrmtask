package com.service.jobs;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.json.JSONObject;
import com.bidcrm.auth.impl.EmailClickerImpl;
import com.bidcrm.data.util.JsonUtil;

import cms.service.template.TemplateTable;
import cms.service.template.TemplateUtility;
import cms.service.util.Base64Util;

public class EmailListEvent extends EmailClickerImpl{
	
	static Log logger = LogFactory.getLog(EmailListEvent.class);
	TemplateUtility tu= new TemplateUtility();
	
	private String emaillistid;
	private String campaignid;
	private String emailsettingid;
	private String contactlistid;
	private String groupuser;
	private String username;
	private String stagecode;
	private String channelscode;
	private String contenttype;
	private int time;
	private String id;
	
	
	public String getChannelscode() {
		return channelscode;
	}
	public void setChannelscode(String channelscode) {
		this.channelscode = channelscode;
	}
	public String getContenttype() {
		return contenttype;
	}
	public void setContenttype(String contenttype) {
		this.contenttype = contenttype;
	}
	public String getContactlistid() {
		return contactlistid;
	}
	public void setContactlistid(String contactlistid) {
		this.contactlistid = contactlistid;
	}
	public String getStagecode() {
		return stagecode;
	}
	public void setStagecode(String stagecode) {
		this.stagecode = stagecode;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getGroupuser() {
		return groupuser;
	}
	public void setGroupuser(String groupuser) {
		this.groupuser = groupuser;
	}
	
	public int getTime() {
		return time;
	}
	public void setTime(int time) {
		this.time = time;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getEmaillistid() {
		return emaillistid;
	}
	public void setEmaillistid(String emaillistid) {
		this.emaillistid = emaillistid;
	}
	public String getCampaignid() {
		return campaignid;
	}
	public void setCampaignid(String campaignid) {
		this.campaignid = campaignid;
	}
	public String getEmailsettingid() {
		return emailsettingid;
	}
	public void setEmailsettingid(String emailsettingid) {
		this.emailsettingid = emailsettingid;
	}
	
	

	public JSONObject executeEmailTask() throws Exception{
	
    	String status=this.stagecode;
    	String subject="";
    	String objid=this.getContactlistid();
    	int sent=0;
    	int failed=0;
    	int total=0;
    	String filepath="";
    	String emailcontent="";
    	String emailcontentreplaced="";
    	String emailcontactid="";
    	JSONObject contactlist=new JSONObject();
        Element resourceElm;
        String clickerscript="";
        String portalurl="";
        String surveyurl="";
        String videourl="";
      
        
		String sql="select c.contactlist2campaign,c.contactlist2emaillist, decode(t.usemaster,'1',m.url,nvl(t.uploadurl,m.url)) url ,"
				+ "t.objid,t.name,t.contenttype,t.emailsubject,t.channelscode "+
				" from table_contactlist c, table_emailsetting t,table_mastertemplate m where "+
				            " m.objid=t.emailsetting2mastertemplate and c.contactlist2campaign=t.emailsetting2campaign and t.stagecode='"+status + "' and c.objid='"+objid+"'";
		    	
    
    	String resendsql="select * from table_contactlistlog l where l.contactlistlog2contactlist='"+objid+"'"+
		            " and l.totalemail=l.totalsent and l.stagecode='"+status+"'";
    	
    	TemplateTable resend=new TemplateUtility().getResultSet(resendsql); 
    	
    	if(resend!=null &&resend.getRowCount()>0){
    		contactlist.put("errmessage", "You already sent email successfuly for this round! You can not send email again for the same round!");
			contactlist.put("total", resend.getFieldValue("totalemail", resend.getRowCount()-1));
			contactlist.put("emailsent", resend.getFieldValue("totalsent", resend.getRowCount()-1));
			contactlist.put("failed", resend.getFieldValue("totalinvalid", resend.getRowCount()-1));
			return contactlist;
    	}
    	
    	TemplateTable urldata=new TemplateUtility().getResultSet(sql); 
    	if(urldata!=null && urldata.getRowCount()>0){
    	   filepath=urldata.getFieldValue("url", urldata.getRowCount()-1);
    	   String templateid=urldata.getFieldValue("objid", urldata.getRowCount()-1);
    	   subject=urldata.getFieldValue("emailsubject", urldata.getRowCount()-1);
    	   channelscode=urldata.getFieldValue("channelscode", urldata.getRowCount()-1);
    	   emailsettingid=urldata.getFieldValue("objid", urldata.getRowCount()-1);
    	   contenttype=urldata.getFieldValue("contenttype", urldata.getRowCount()-1);
    	   emailcontactid=urldata.getFieldValue("contactlist2emaillist", urldata.getRowCount()-1);
    	   if(campaignid.length()!=32){
    		   campaignid=urldata.getFieldValue("contactlist2campaign", urldata.getRowCount()-1);
    	   }
    	  if(!tu.isEmptyValue(contenttype) && contenttype.equalsIgnoreCase("html")){
    		  this.ishtmlbody=true;
    	  }
    	  if(!tu.isEmptyValue(filepath)){
    		 // String uri=uriInfo.getBaseUri().toString();
				String url="https://www.bidcrm.com/"+filepath;
				
				try {
					emailcontent=JsonUtil.readURLFile(url);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					contactlist.put("errmessage", "could not read template file url="+url);
					contactlist.put("total", 0);
					contactlist.put("emailsent", 0);
					contactlist.put("failed", 0);
				}
				if(!tu.isEmptyValue(emailcontent)){
					
					String attrsql= "select *from table_emailattribute where emailattribute2emailsetting='"+templateid+"'";
					TemplateTable attrs=tu.getResultSet(attrsql);
					if(attrs!=null &&attrs.getRowCount()>0){
						for(int i=0;i<attrs.getRowCount();i++){
							String attrname=attrs.getFieldValue("name", i);
							String attrval=attrs.getFieldValue("value", i);
							String attrtype=attrs.getFieldValue("attributetype", i);
							String linktext=attrs.getFieldValue("linktext", i);
							if(attrval.contains("https:")||attrval.contains("http:")||attrval.contains("/bidcrm"))
							{
								attrval=attrval.replaceAll(" ", "");
							}
							/*
							if(!tu.isEmptyValue(attrtype) && (attrtype.equalsIgnoreCase("link")||attrtype.equalsIgnoreCase("image"))){
								//clickerscript+=getVar(attrname,attrval);
								String alink=getLinkText(attrval,linktext);
								if(tu.isEmptyValue(linktext)){
									linktext="Click Here";
								}
								emailcontent=emailcontent.replace("@"+attrname, alink);
							}else{
								emailcontent=emailcontent.replace("@"+attrname, attrval);
							}
							*/
							emailcontent=emailcontent.replace("@"+attrname, attrval);
						}
					}
				}
				//replace portalurl and surveyurl
				if(!tu.isEmptyValue(campaignid)){
					TemplateTable campaign=tu.getResultSet("select *from table_campaign where objid='"+campaignid+"'");
					if(campaign!=null && campaign.getRowCount()>0){
						 surveyurl=campaign.getFieldValue("surveyurl", campaign.getRowCount()-1);
						 videourl=campaign.getFieldValue("videourl", campaign.getRowCount()-1);
					}
				}
				
				// get emaillist
				String emaillist="select * from table_emailcontact where emailcontact2emaillist='"+emailcontactid+"'";
				TemplateTable emails=tu.getResultSet(emaillist);
				
				if(emails!=null &&emails.getRowCount()>0){
					total=emails.getRowCount();
					for (int k=0;k<emails.getRowCount();k++){
						sendto=emails.getFieldValue("email", k);
						String firstname=emails.getFieldValue("firstname", k);
						String emailcontactobjid=emails.getFieldValue("objid", k);
						emailcontentreplaced=emailcontent;
						if(!tu.isEmptyValue(firstname)){
							emailcontentreplaced=emailcontentreplaced.replaceAll("Hi there", "Hi "+firstname);
							emailcontentreplaced=emailcontentreplaced.replaceAll("Hi There", "Hi "+firstname);
							emailcontentreplaced=emailcontentreplaced.replaceAll("hi there", "Hi "+firstname);
							emailcontentreplaced=emailcontentreplaced.replaceAll("@firstname", firstname);
							emailcontentreplaced=emailcontentreplaced.replaceAll("hello", "Hi "+firstname);
							emailcontentreplaced=emailcontentreplaced.replaceAll("Hello", "Hi "+firstname);
							
						}
						
						String userlogin="";
						if(!tu.isEmptyValue(username)){
							TemplateTable user=tu.getResultSet("select loginname||';'||password as userlogin " 
						+ "from table_user where loginname='"+username+"' and groupuser='"+groupuser+"'");
							if(user!=null &&user.getRowCount()>0){
								userlogin=user.getFieldValue("userlogin", user.getRowCount()-1);
							}
						}
						
						String  portaltoken=new String(Base64Util.encode(userlogin.trim().getBytes()));
						portalurl=portalurl = "https://www.bidcrm.com/bidcrm/portal&#63;referer="+emailcontactobjid+"&action=sampleportal-"+campaignid+"&setter="+emailsettingid+"&servicekey=";
						
						
						if(!tu.isEmptyValue(portalurl)){
							
							portalurl=portalurl+portaltoken;
							
							portalurl="<a href=\""+portalurl+"\">Click Here</a>";
							emailcontentreplaced=emailcontentreplaced.replace("@portalurl", portalurl);
						}
						if(!tu.isEmptyValue(surveyurl)){
						
							emailcontentreplaced=emailcontentreplaced.replace("@surveyurl", surveyurl);
						}
						if(!tu.isEmptyValue(videourl)){
						
							emailcontentreplaced=emailcontentreplaced.replace("@videourl", videourl);
						}
						try{
							doSetup();
							String imgurl="https://www.bidcrm.com/bidcrm/portal/"+campaignid+"-"+emailcontactobjid+"-"+emailsettingid+"/open.gif";
							String tracker="<div id=\"div1\" style=\"visibility: hidden\">"+
									"<img  src=\""+ imgurl+"\"  style=\"display:none\" alt=\"\" />"+
									"</div>";
							if(emailcontentreplaced.contains("@tracker")){
								emailcontentreplaced=emailcontentreplaced.replaceAll("@tracker", tracker);
							}else if(emailcontentreplaced.contains("</body>")){
								emailcontentreplaced=emailcontentreplaced.replaceAll("</body>", tracker);
							}else if(emailcontentreplaced.contains("</html>")){
								emailcontentreplaced=emailcontentreplaced.replaceAll("</html>", tracker);
							}else{
								emailcontentreplaced+=tracker;
							}
							
							String clickurl="https://www.bidcrm.com/bidcrm/portal/"+campaignid+"-"+emailcontactobjid+"-"+emailsettingid+"/click.gif";
							String script="\n<script>"+
									 "\n"+clickerscript+
									 "\n"+getClicker(clickurl)+
									 "\n</script>";
							//logger.info(script);
							if(emailcontentreplaced.contains("@clicker")){
								
								emailcontentreplaced=emailcontentreplaced.replaceAll("@clicker", clickurl);
							}else if(emailcontentreplaced.contains("<head>")){
								emailcontentreplaced=emailcontentreplaced.replace("<head>", "<head>\n"+script);
							}else{
								emailcontentreplaced="<head>\n"+script+"</head>";
							}
							
							//logger.info(emailcontentreplaced);
							//finally add script
						    
							
							if(sendmail(  subject, emailcontentreplaced)){
								sent++;
							}else{
								failed++;
							}
						}catch(Exception e){
							e.printStackTrace();
						}
					}
					contactlist.put("total", total);
					contactlist.put("emailsent", sent);
					contactlist.put("failed", failed);
				}
    	  }else{
    		    contactlist.put("errmessage", "Email Template missing for this round! Please upload email template");
				contactlist.put("total", 0);
				contactlist.put("emailsent", 0);
				contactlist.put("failed", 0);
				
    	  }
    	}else{
    		contactlist.put("errmessage", "Email Template missing for this round! Please upload email template");
			contactlist.put("total", 0);
			contactlist.put("emailsent", 0);
			contactlist.put("failed", 0);
    	}
	    	if(sent>0){
	    	String insertlog=" insert into table_contactlistlog(objid,contactlistlog2contactlist,note,totalemail,"
	    			+ "totalsent,totalinvalid,stagecode,logdate,groupuser,genuser,gendate)values("+tu.getPrimaryKey()+
	    			",'"+objid
	    			+"','"+contactlist.toString()
	    			+"',"+contactlist.get("total")
	    			+","+contactlist.get("emailsent")
	    			+","+contactlist.get("failed")
	    			+",'"+status
	    			+"',sysdate"
	    			+",'"+groupuser
	    			+"','"+username
	    			+"',sysdate)";
	    			
	    		tu.executeQuery(insertlog);
	    	if(contactlist.get("emailsent").equals(contactlist.get("total"))){
	    		String updatesql="update table_contactlist set @fld='1' ,stagecode='@status', url='"+filepath+"' where objid='"+objid+"'";
	        	if(status.equals("10")){
	        		updatesql=updatesql.replace("@fld", "firstround");
	        		updatesql=updatesql.replace("@status", "10");
	        	}else if(status.equals("20")){
	        		updatesql=updatesql.replace("@fld", "secondround");
	        		updatesql=updatesql.replace("@status", "20");
	        	}else if(status.equals("30")){
	        		updatesql=updatesql.replace("@fld", "thirdround");
	        		updatesql=updatesql.replace("@status", "30");
	        	}else if(status.equals("40")){
	        		updatesql=updatesql.replace("@fld", "fourthround");
	        		updatesql=updatesql.replace("@status", "40");
	        	}else if(status.equals("50")){
	        		updatesql=updatesql.replace("@fld", "fifthround");
	        		updatesql=updatesql.replace("@status", "50");
	        	}else if(status.equals("60")){
	        		updatesql=updatesql.replace("@fld", "sixthround");
	        		updatesql=updatesql.replace("@status", "60");
	        	}else if(status.equals("70")){
	        		updatesql=updatesql.replace("@fld", "seventhround");
	        		updatesql=updatesql.replace("@status", "70");
	        	}else if(status.equals("80")){
	        		updatesql=updatesql.replace("@fld", "eigthtround");
	        		updatesql=updatesql.replace("@status", "80");
	        	}else if(status.equals("90")){
	        		updatesql=updatesql.replace("@fld", "ninthround");
	        		updatesql=updatesql.replace("@status", "90");
	        	}else if(status.equals("100")){
	        		updatesql=updatesql.replace("@fld", "tenthround");
	        		updatesql=updatesql.replace("@status", "100");
	        	}else if(status.equals("110")){
	        		updatesql=updatesql.replace("@fld", "reminder1");
	        		updatesql=updatesql.replace("@status", "110");
	        	}else if(status.equals("120")){
	        		updatesql=updatesql.replace("@fld", "reminder2");
	        		updatesql=updatesql.replace("@status", "120");
	        	}else if(status.equals("130")){
	        		updatesql=updatesql.replace("@fld", "reminder3");
	        		updatesql=updatesql.replace("@status", "130");
	        	}				
	        	tu.executeQuery(updatesql);
	    	}
    	}
      
        //logger.info(result);
 
        return contactlist ;
	}
	
	

}
