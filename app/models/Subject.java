package models;

import java.util.List;

import javax.persistence.*;

import com.avaje.ebean.Page;
import com.fasterxml.jackson.annotation.JsonIgnore;

import play.cache.Cached;
import play.db.ebean.Model;
import uk.bl.Const;

@Entity
@DiscriminatorValue("subject")
public class Subject extends Taxonomy {

	private static final long serialVersionUID = 3535758346565569620L;

	@JsonIgnore
    @ManyToOne
	@JoinColumn(name = "parent_id")
	public Subject parent;
	
    @OneToMany(mappedBy="parent")
	public List<Subject> children;

	@JsonIgnore
	@ManyToMany
	@JoinTable(name = "subject_target", joinColumns = { @JoinColumn(name = "subject_id", referencedColumnName="id") },
			inverseJoinColumns = { @JoinColumn(name = "target_id", referencedColumnName="id") })
	public List<Target> targets;
	
	public static Model.Finder<Long,Subject> find = new Model.Finder<Long, Subject>(Long.class, Subject.class);

    /**
     * Retrieve all subjects.
     */
    public static List<Subject> findAllSubjects() {
        return find.orderBy("name asc").findList();
    }

    public static Subject findById(Long id) {
    	return find.byId(id);
    }

	public static String findNameById(Long id) {
		return find.byId(id).name;
	}

	public static Subject findByUrl(String url) {
    	return find.where().eq(Const.URL, url).findUnique();
    }

	@Cached(key = "SubjectsData")
	public static List<Subject> getFirstLevelSubjects() {
	       return find.where().isNull("parent").order().asc("name").findList();
	}
	
	public static List<Subject> findChildrenByParentId(Long parentId) {
		return find.where().eq("t0.parent_id", parentId).order().asc("name").findList();
	}
	
	public static Page<Subject> pager(int page, int pageSize, String sortBy, String order, String filter) {

        return find.where().contains("title", filter)
        		.orderBy(sortBy + " " + order)
        		.findPagingList(pageSize)
        		.setFetchAhead(false)
        		.getPage(page);
    }

}
