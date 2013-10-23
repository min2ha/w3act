package controllers;

import play.*;
import play.mvc.*;
import play.data.*;
import static play.data.Form.*;

import java.util.*;

import com.avaje.ebean.ExpressionList;

import models.*;
import uk.bl.Const;
import views.html.*;

/**
 * Manage targets.
 */
@Security.Authenticated(Secured.class)
public class Targets extends AbstractController {
  
    /**
     * Display the targets.
     */
    public static Result index() {
    	List<Target> targetsAll = models.Target.findAll();
    	List<Target> targetsRes = targetsAll.subList(0, Const.ROWS_PER_PAGE);
        return ok(
            targets.render(
//                    "Targets", User.find.byId(request().username()), models.Target.findInvolving(), 
                    "Targets", User.find.byId(request().username()), targetsRes, 
                	User.findAll(), models.Organisation.findInvolving(),
                	Const.NONE, Const.NONE, Const.NONE, Const.NONE, Const.NONE, 
                	Const.NONE, Const.NONE, 0, models.Target.findAll().size()
            )
        );
    }

    public static Result offset(int offset) {
    	List<Target> targetsAll = models.Target.findAll();
    	List<Target> targetsRes = targetsAll.subList(offset*Const.ROWS_PER_PAGE, (offset+1)*Const.ROWS_PER_PAGE);
        return ok(
            targets.render(
//                    "Targets", User.find.byId(request().username()), models.Target.findInvolving(), 
                    "Targets", User.find.byId(request().username()), targetsRes, 
                	User.findAll(), models.Organisation.findInvolving(),
                	Const.NONE, Const.NONE, Const.NONE, Const.NONE, Const.NONE, 
                	Const.NONE, Const.NONE, offset, models.Target.findAll().size()
            )
        );
    }

    public static Result filterUrl() {
        String filter = getFormParam(Const.FILTER);
        return ok(
                targets.render(
                    "Targets", User.find.byId(request().username()), models.Target.filterUrl(filter), 
                	User.findAll(), models.Organisation.findInvolving(),
                	Const.NONE, Const.NONE, Const.NONE, Const.NONE, Const.NONE, 
                	Const.NONE, Const.NONE, 0, models.Target.findAll().size()
                )
            );
    }
    
    /**
     * Display the targets panel for this user URL.
     * @param curatorUrl
     * @param organisationUrl
     * @param collectionCategoryUrl
     * @param subjectUrl
     * @param crawlFrequency
     * @param depth
     * @param scope
     * @return
     */
    public static Result edit(String curatorUrl, String organisationUrl, String collectionCategoryUrl, 
    		String subjectUrl, String crawlFrequency, String depth, String scope, int offset, int limit) {
    	List<Target> targetsAll = models.Target.filterUserUrl(curatorUrl);
    	List<Target> targetsRes = targetsAll.subList(offset*Const.ROWS_PER_PAGE, (offset+1)*Const.ROWS_PER_PAGE);
        return ok(
                targets.render(
   			        "Targets", User.find.byId(request().username()), targetsRes, 
//   			        "Targets", User.find.byId(request().username()), models.Target.filterUserUrlExt(curatorUrl, offset), 
		        	User.findFilteredByUrl(curatorUrl), models.Organisation.findFilteredByUrl(organisationUrl),
			        	curatorUrl,  organisationUrl, collectionCategoryUrl, subjectUrl, crawlFrequency, depth, 
			        	scope, offset, models.Target.findAll().size()
                        )
                );
    }
    
	/**
	 * This method filters targets by given URLs.
	 * @return duplicate count
	 */
	public static List<Target> getSubjects() {
		List<Target> res = new ArrayList<Target>();
//		List<String> subjects = new ArrayList<String>();
		List<Target> allTargets = Target.find.all();
		Iterator<Target> itr = allTargets.iterator();
		while (itr.hasNext()) {
			Target target = itr.next();
			if (target.field_subject != null && target.field_subject.length() > 0 && !res.contains(target)) {
//				if (target.field_subject != null && target.field_subject.length() > 0 && !subjects.contains(target.field_subject)) {
		        ExpressionList<Target> ll = Target.find.where().contains("field_subject", target.field_subject);
		        if (ll.findRowCount() > 0) {
//		        	subjects.add(target.field_subject);
		        	res.add(target);
		        }
			}
		}
    	return res;
	}
	
	/**
	 * This method filters targets by given scope.
	 * @return scope list
	 */
	public static List<Target> getScope() {
		List<Target> res = new ArrayList<Target>();
		List<String> subjects = new ArrayList<String>();
		List<Target> allTargets = Target.find.all();
		Iterator<Target> itr = allTargets.iterator();
		while (itr.hasNext()) {
			Target target = itr.next();
			if (target.field_scope != null && target.field_scope.length() > 0 && !subjects.contains(target.field_scope)) {
		        ExpressionList<Target> ll = Target.find.where().contains("field_scope", target.field_scope);
		        if (ll.findRowCount() > 0) {
		        	res.add(target);
		        	subjects.add(target.field_scope);
		        }
			}
		}
    	return res;
	}
	
	/**
	 * This method retrieves targets from database for given taxonomy URL.
	 * @param url
	 * @return
	 */
	public static List<Target> getTargetsForTaxonomy(String url) {
		List<Target> res = new ArrayList<Target>();
		Logger.info("url: " + url);
		if (url != null) {
	        ExpressionList<Target> ll = Target.find.where().contains("field_collection_categories", url);
	        res = ll.findList();
		}
		Logger.info("res size: " + res.size());
		return res;
	}
	
	/**
	 * This method filters targets by given license.
	 * @return scope list
	 */
	public static List<Taxonomy> getLicense() {
		List<Taxonomy> res = new ArrayList<Taxonomy>();
		List<String> subjects = new ArrayList<String>();
		List<Target> allTargets = Target.find.all();
		Iterator<Target> itr = allTargets.iterator();
		while (itr.hasNext()) {
			Target target = itr.next();
			if (target.field_license != null && target.field_license.length() > 0 && !subjects.contains(target.field_license)) {
		        ExpressionList<Target> ll = Target.find.where().contains("field_license", target.field_license);
		        if (ll.findRowCount() > 0) {
		        	Taxonomy taxonomy = Taxonomy.findByUrl(target.field_license);
		        	Logger.info("target.field_license: " + target.field_license + ".");
		        	Logger.info("taxonomy url: " + taxonomy.url);
		        	Logger.info("license: " + taxonomy.name);
		        	res.add(taxonomy);
		        	subjects.add(target.field_license);
		        }
			}
		}
    	return res;
	}
	
	/**
	 * This method filters targets by crawl frequency.
	 * @return scope list
	 */
	public static List<Target> getCrawlFrequency() {
		List<Target> res = new ArrayList<Target>();
		List<String> subjects = new ArrayList<String>();
		List<Target> allTargets = Target.find.all();
		Iterator<Target> itr = allTargets.iterator();
		while (itr.hasNext()) {
			Target target = itr.next();
			if (target.field_crawl_frequency != null && target.field_crawl_frequency.length() > 0 && !subjects.contains(target.field_crawl_frequency)) {
		        ExpressionList<Target> ll = Target.find.where().contains("field_crawl_frequency", target.field_crawl_frequency);
		        if (ll.findRowCount() > 0) {
		        	res.add(target);
		        	subjects.add(target.field_crawl_frequency);
		        }
			}
		}
    	return res;
	}
	
	/**
	 * This method filters targets by depth.
	 * @return scope list
	 */
	public static List<Target> getDepth() {
		List<Target> res = new ArrayList<Target>();
		List<String> subjects = new ArrayList<String>();
		List<Target> allTargets = Target.find.all();
		Iterator<Target> itr = allTargets.iterator();
		while (itr.hasNext()) {
			Target target = itr.next();
			if (target.field_depth != null && target.field_depth.length() > 0 && !subjects.contains(target.field_depth)) {
		        ExpressionList<Target> ll = Target.find.where().contains("field_depth", target.field_depth);
		        if (ll.findRowCount() > 0) {
		        	res.add(target);
		        	subjects.add(target.field_depth);
		        }
			}
		}
    	return res;
	}
	
	/**
	 * This method filters targets by collection categories.
	 * @return scope list
	 */
	public static List<Taxonomy> getCollectionCategories() {
//		public static List<Target> getCollectionCategories() {
		List<Target> res = new ArrayList<Target>();
		List<String> subjects = new ArrayList<String>();
		List<Taxonomy> taxonomies = new ArrayList<Taxonomy>();
		List<Target> allTargets = Target.find.all();
		Iterator<Target> itr = allTargets.iterator();
		while (itr.hasNext()) {
			Target target = itr.next();
			if (target.field_collection_categories != null && target.field_collection_categories.length() > 0 && !subjects.contains(target.field_collection_categories)) {
		        ExpressionList<Target> ll = Target.find.where().contains("field_collection_categories", target.field_collection_categories);
		        if (ll.findRowCount() > 0) {
		        	res.add(target);
		        	subjects.add(target.field_collection_categories);
		        	Taxonomy taxonomy = Taxonomy.findByUrl(target.field_collection_categories);
		        	taxonomies.add(taxonomy);
		        }
			}
		}
    	return taxonomies;
//    	return res;
	}
	
}

