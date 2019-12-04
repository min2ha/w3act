package models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import play.Logger;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import scala.NotImplementedError;
import uk.bl.Const;
import uk.bl.api.Utils;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Page;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This calls supports crawl permissions workflow and
 * handles crawl permission requests sent by e-mail to the owner.
 */
@Entity
@Table(name = "crawl_permission")
public class CrawlPermission extends ActModel {

	/**
	 * file id
	 */
	private static final long serialVersionUID = -2250099575463302989L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "crawl_permission_seq")
	public Long id;
	
//	the permission can be inherited from a 'parent' target. 
//	Targets could in theory have multiple crawl permissions. This is most likely in the case where a 
//	permission was sent and then cancelled for whatever reason, and then another one sent to supersede it.
	@JsonIgnore
	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name="target_id")
    @Required(message="Target is required")
	public Target target;
	
	@JsonIgnore
	@OneToMany(mappedBy = "crawlPermission", cascade = CascadeType.ALL, fetch=FetchType.EAGER)
	public List<CommunicationLog> communicationLogs;
    
	//bi-directional many-to-one association to MailTemplate
	@ManyToOne(cascade = CascadeType.REFRESH, fetch=FetchType.EAGER)
	@JoinColumn(name="mailtemplate_permission_request_id")
	public MailTemplate permissionRequestMailTemplate;

	//bi-directional many-to-one association to MailTemplate
	@ManyToOne(cascade = CascadeType.REFRESH, fetch=FetchType.EAGER)
	@JoinColumn(name="mailtemplate_acknowledgement_id")
	public MailTemplate acknowledgementMailTemplate;
	
	//bi-directional many-to-one association to ContactPerson
	@ManyToOne(cascade = CascadeType.REFRESH, fetch=FetchType.EAGER)
	@JoinColumn(name="contactPerson_id")
    @Required(message="Contact Person is required")
	public ContactPerson contactPerson;
	
    @Column(columnDefinition = "text")
    @Required(message="Name is required")
    public String name;
    
    @Column(columnDefinition = "text")
    public String description;
    
    @Column(columnDefinition = "text")
    public String anyOtherInformation;
    
	@JsonIgnore
	@ManyToOne(cascade = CascadeType.REFRESH, fetch=FetchType.EAGER)
	@JoinColumn(name="archivist_id")
    public User user; 
    
    /**
     * Records status of permission process e.g. 
     * Not Initiated, Queued, Pending, Refused, Granted
     * Usually populated by system actions, but may also be modified by Archivist 
     */
    @Column(columnDefinition = "text")
    public String status; 
    
	@ManyToOne
	@JoinColumn(name="license_id")
    private License license; 
    
	// Do not include the token as this is the 'secret' used to grant licenses
	@JsonIgnore
	public String token;

    /**
     * This is a checkbox defining whether follow up e-mails should be send.
     */
    public Boolean requestFollowup;
    
    /**
     * This is a checkbox defining whether any content on this web site subject 
     * to copyright and/or the database right held by another party.
     */
    public Boolean thirdPartyContent;
    
    /**
     * This is a checkbox defining whether owner allows the archived web site 
     * to be used in any future publicity for the Web Archive.
     */
    public Boolean publish;
    
    /**
     * This is a checkbox defining whether owner agrees to archive web site.
     */
    public Boolean agree;
    
    /**
     * This is a string which captures the date on which the licence is requested.
     */
    @Column(name="requested_at")
    public Timestamp requestedAt;
    
    /**
     * This is a readonly field which captures the date on which the licence is agreed.
     */
    @Column(name="granted_at")
    public Timestamp grantedAt;
    
    @Transient
	public String requestedAtISO;

	@Transient
	public String grantedAtISO;
    
    
	public String getRequestedAtISO() {
		if (requestedAt != null) {
			requestedAtISO = Utils.INSTANCE.convertToDateTimeISO(requestedAt);
		}
		return requestedAtISO;
	}

	public String getGrantedAtISO() {
		if (grantedAt != null) {
			grantedAtISO = Utils.INSTANCE.convertToDateTimeISO(grantedAt);
		}
		return grantedAtISO;
	}

	public static final Model.Finder<Long, CrawlPermission> find = new Model.Finder<Long, CrawlPermission>(Long.class, CrawlPermission.class);

    public CrawlPermission() {
    	this(null, null, null);
    }
    
    public CrawlPermission(Long id, String url) {
		this(id, url, null);
	}

	public CrawlPermission(Long id, String url, String name) {
		this.id = id;
		this.url = url;
		this.name = name;
		this.token = UUID.randomUUID().toString();
	}
	
	public License getLicense() {
		return this.license;
	}
	
	public void setLicense( License l ) {
		this.license = l;
	}

	public MailTemplate getPermissionRequestMailTemplate() {
		return permissionRequestMailTemplate;
	}

	public void setPermissionRequestMailTemplate(MailTemplate permissionRequestMailTemplate) {
		this.permissionRequestMailTemplate = permissionRequestMailTemplate;
	}

	public MailTemplate getAcknowledgementMailTemplate() {
		return acknowledgementMailTemplate;
	}

	public void setAcknowledgementMailTemplate(MailTemplate acknowledgementMailTemplate) {
		this.acknowledgementMailTemplate = acknowledgementMailTemplate;
	}

	/**
     * This method evaluates if element is in a list separated by list delimiter e.g. ', '.
     * @param subject
     * @return true if in list
     */
	@Transient
	@JsonIgnore
    public boolean hasContactPerson(String curContactPerson) {
//    	boolean res = false;
//    	res = Utils.hasElementInList(curContactPerson, contactPerson);
//    	return res;
    	throw new NotImplementedError();
    }
	
	@Transient
	@JsonIgnore
	public boolean isCompleted() {
		if( Const.CrawlPermissionStatus.GRANTED.toString().equals(this.status) ||
				Const.CrawlPermissionStatus.SUPERSEDED.toString().equals(this.status) ||
				Const.CrawlPermissionStatus.REFUSED.toString().equals(this.status) ||
				Const.CrawlPermissionStatus.EMAIL_REJECTED.toString().equals(this.status) ) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CrawlPermission other = (CrawlPermission) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
    public static CrawlPermission findByName(String name) {
        return find.where()
                   .eq("name",
                       name)
                   .findUnique();
    }
    
    /**
     * Retrieve an object by Id (id).
     * @param nid
     * @return object 
     */
    public static CrawlPermission findById(Long id) {
    	CrawlPermission res = find
				.fetch("permissionRequestMailTemplate") /* Don't know why this is necessary, but these entities are not fetched otherwise */
				.fetch("acknowledgementMailTemplate")
				.where().eq(Const.ID, id).findUnique();
    	return res;
    }          
    
    /**
	 * The number of requests that were sent.
	 */
	public Long numberRequests;

	public static CrawlPermission findByToken(String token) {
    	CrawlPermission res = find
				.fetch("license")
				.fetch("contactPerson")
				.fetch("permissionRequestMailTemplate") /* Don't know why this is necessary, but these entities are not fetched otherwise */
				.fetch("acknowledgementMailTemplate")
				.where().eq("token", token).findUnique();
    	return res;
    }
    
    /**
     * Retrieve a crawl permission by URL.
     * @param url
     * @return crawl permission name
     */
    public static CrawlPermission findByUrl(String url) {
//    	Logger.debug("permission findByUrl: " + url);
    	CrawlPermission res = new CrawlPermission();
    	if (url != null && url.length() > 0 && !url.equals(Const.NONE)) {
    		res = find.where().eq(Const.URL, url).findUnique();
    	} else {
    		res.name = Const.NONE;
    	}
    	return res;
    }

    /**
     * @param target The field URL
     * @return
     */
    public static CrawlPermission findByTarget(String target) {
    	CrawlPermission res = new CrawlPermission();
    	if (target != null && target.length() > 0 && !target.equals(Const.NONE)) {
    		res = find.where().eq(Const.TARGET, target).findUnique();
    	} else {
    		res.name = Const.NONE;
    	}
    	return res;
    }

    /**
     * This method is used to show crawl permission in a table.
     * It shows none value if no entry was found in database.
     * @param url
     * @return
     */
    public static CrawlPermission showByUrl(String url) {
//    	Logger.debug("permission findByUrl: " + url);
    	CrawlPermission res = new CrawlPermission();
    	if (url != null && url.length() > 0 && !url.equals(Const.NONE)) {
    		try {
    			res = find.where().eq(Const.URL, url).findUnique();
            	if (res == null) {
                	res = new CrawlPermission();
                	res.name = Const.NONE;            	}
    		} catch (Exception e) {
    			Logger.debug("crawl permission could not be find in database: " + e);
    		}
    	} else {
        	res.name = Const.NONE;
    	}
//    	Logger.debug("permission res: " + res);
    	return res;
    }

    public static CrawlPermission showByToken(String token) {
    	CrawlPermission crawlPermission = CrawlPermission.findByToken(token);
    	return crawlPermission;
    }
	/**
	 * This method filters crawl permissions by name and returns a list 
	 * of filtered CrawlPermission objects.
	 * @param name
	 * @return
	 */
	public static List<CrawlPermission> filterByName(String name) { 
		List<CrawlPermission> res = new ArrayList<CrawlPermission>();
        ExpressionList<CrawlPermission> ll = find.where().icontains(Const.NAME, name);
    	res = ll.findList();
		return res;
	}
        
	/**
	 * This method filters crawl permissions by contact person and returns a list 
	 * of filtered CrawlPermission objects.
	 * @param url The identifier for contact person
	 * @return
	 */
	public static List<CrawlPermission> filterByContactPerson(String url) { 
		List<CrawlPermission> res = new ArrayList<CrawlPermission>();
        ExpressionList<CrawlPermission> ll = find.where().icontains(Const.CONTACT_PERSON, url);
    	res = ll.findList();
		return res;
	}
        
	/**
	 * Find out crawl permission by target that was submitted by owner.
	 * @param cur_target
	 * @return permission list
	 */
	public static List<CrawlPermission> filterByTarget(String cur_target) {
		List<CrawlPermission> res = new ArrayList<CrawlPermission>();
        ExpressionList<CrawlPermission> ll = find.where().icontains(Const.TARGET, cur_target);
    	res = ll.findList();
		return res;
	}
        
	/**
	 * This method filters crawl permissions by status and returns a list 
	 * of filtered CrawlPermission objects.
	 * @param status
	 * @return
	 */
	public static List<CrawlPermission> filterByStatus(String status) {
		List<CrawlPermission> res = new ArrayList<CrawlPermission>();
        ExpressionList<CrawlPermission> ll = find.where().icontains(Const.STATUS, status);
    	res = ll.findList();
		return res;
	}
        
    /**
     * Retrieve all crawl permissions.
     */
    public static List<CrawlPermission> findAll() {
        return find.all();
    }
    
    /**
     * This method enables replacing of placeholders in mail text by given value.
     * @param text The text of an email.
     * @param placeHolder The placeholder string e.g. ||URL||
     * @param value The value that overwrites placeholder
     * @return updated text
     */
    public static String replaceStringInText(String text, String placeHolder, String value) {
    	String res = text;
    	res = text.replace(placeHolder, value);
    	return res;
    }
    
    /**
     * This method enables replacing of two place holders in mail text by given values.
     * @param text The text of an email.
     * @param placeHolderUrl The placeholder string for crawl URL ||URL||
     * @param placeHolderLink The placeholder string for unique license URL ||LINK||
     * @param valueUrl The value that overwrites associated placeholder
     * @param valueLink The value that overwrites associated placeholder
     * @return updated text
     */
    public static String replaceTwoStringsInText(String text, String placeHolderUrl, String placeHolderLink, 
    		String valueUrl, String valueLink) {
    	String res = text;
		Logger.debug("replaceTwoStringsInText valueUrl: " + valueUrl);
		Logger.debug("++++++++ replaceTwoStringsInText placeholder string for unique license URL ||LINK||, aka placeHolderLink: " + placeHolderLink);

		Logger.debug("The value that overwrites associated placeholder ||URL||: " + valueUrl);
		Logger.debug("The value that overwrites associated placeholder ||LINK||: " + valueLink);

    	List<String> placeHolders = new ArrayList<String>();
    	placeHolders.add(placeHolderUrl);
    	placeHolders.add(placeHolderLink);
    	List<String> values = new ArrayList<String>();
    	values.add(valueUrl);
    	values.add(valueLink);
    	res = replacePlaceholdersInText(text, placeHolders, values);
    	return res;
    }
    
    /**
     * This method enables replacing of place holders in mail text by given values.
     * @param text The text of an email.
     * @param placeHolders The placeholder list in string format e.g. ||URL||, ||LINK||
     * @param values The value that overwrites place holders
     * @return updated text
     */
    public static String replacePlaceholdersInText(String text, List<String> placeHolders, List<String> values) {
		Logger.debug("+++++ replacePlaceholdersInText, placeHolders and values count: " + placeHolders.size() +", " + values.size() );
    	String res = text;
    	if (placeHolders != null && placeHolders.size() > 0 
    			&& values != null && values.size() > 0
    			&& placeHolders.size() == values.size()) {
    		int counter = placeHolders.size();
    		for (int i = 0; i < counter; i++) {
    			Logger.debug("replacePlaceholdersInText placeholder: " + placeHolders.get(i) +
    					", value: " + values.get(i));
    	    	res = res.replace(placeHolders.get(i), values.get(i));
    		}
    	}
    	return res;
    }    
    
    /**
     * Return a page of crawl permission 
     *
     * @param page Page to display
     * @param pageSize Number of Users per page
     * @param sortBy Crawl permission property used for sorting
     * @param order Sort order (either or asc or desc)
     * @param filter Filter applied on the name column
	 * @param status The status of crawl permission request (e.g. QUEUED, PENDING...)
	 * @param organisation The id of organisation the target belongs to (e.g. 1 - (BL), ...)
     */
    public static Page<CrawlPermission> page(int page, int pageSize, String sortBy, String order, String filter,
                                             String status, String organisation) {
        // Set up query:
        ExpressionList<CrawlPermission> q = find.where().icontains("name", filter);
        // Add optional status filter:
        if (!"-1".equals(status)) {
            q = q.eq("status", status);
        }
        // Add optional organisation filter:
        if (!"-1".equals(organisation)) {
            q = q.eq("user.organisation.id", Integer.valueOf(organisation));
        }
        // Strip out NULLs when sorting by these dates:
        if ("grantedAt".equals(sortBy) || "requestedAt".equals(sortBy)) {
            q = q.isNotNull(sortBy);
        }
        // Query and return paged list:
        return q.orderBy(sortBy + " " + order)
                .findPagingList(pageSize)
                .setFetchAhead(false)
                .getPage(page);
    }

    public static Page<CrawlPermission> targetPager(int page, int pageSize, String sortBy, String order, Long targetId) {

        return find.where()
        		.eq("target.id", targetId)
        		.orderBy(sortBy + " " + order)
        		.findPagingList(pageSize)
        		.setFetchAhead(false)
        		.getPage(page);
    }

    
    public static CrawlPermission create(Long id, String url) {
    	return new CrawlPermission(id, url);
    }
    
    public static CrawlPermission create(Long id, String url, String name) {
    	return new CrawlPermission(id, url, name);
    }
    
	
    public static Map<String,String> options() {
        LinkedHashMap<String,String> options = new LinkedHashMap<String,String>();
        for(CrawlPermission c: find.all()) {
            options.put(c.id.toString(), c.name);
        }
        return options;
    }

	@Override
	public String toString() {
		return "CrawlPermission [target=" + ((target!=null) ? target.id : "NULL") + ", permissionRequestMailTemplate="
				+ permissionRequestMailTemplate + ", acknowledgementMailTemplate="
				+ acknowledgementMailTemplate + ", contactPerson=" + contactPerson + ", name="
				+ name + ", description=" + description
				+ ", anyOtherInformation=" + anyOtherInformation + ", user="
				+ user + ", status=" + status + ", license=" + license
				+ ", requestFollowup=" + requestFollowup + ", numberRequests="
				+ numberRequests + ", thirdPartyContent=" + thirdPartyContent
				+ ", publish=" + publish + ", agree=" + agree + ", requestedAt=" + requestedAt
				+ ", grantedAt=" + grantedAt + ", token=" + token + "]";
	}  
}