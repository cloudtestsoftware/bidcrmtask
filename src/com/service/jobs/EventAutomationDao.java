package com.service.jobs;

import cms.service.template.TemplateTable;
import cms.service.template.TemplateUtility;

public class EventAutomationDao {
	
	TemplateUtility tu=new TemplateUtility();
	
	public TemplateTable getAutomationTasks(){
		
		return tu.getResultSet("select *from table_automationtask");
		
	}
	
	public TemplateTable getEmailListReadyToSend(){
		String sql="select e.*,c.contactlist2emaillist from table_emailsetting e, table_contactlist c where e.startdate+to_number(substr(e.starttimecode,0,2))/24 < sysdate"+
				" and not exists (select *from table_contactlistlog l where l.contactlistlog2contactlist=e.emailsetting2contactlist)";
		return tu.getResultSet(sql);
	}

}
