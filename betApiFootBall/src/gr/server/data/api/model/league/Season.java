package gr.server.data.api.model.league;

public class Season implements Comparable<Season>{
	//premier league 18686
	int id;
	Integer year_start;
	Integer year_end;
	int league_id;
	String slug;
	String name;
//	League league;
	
	Standing standing;

	@Override
	public int compareTo(Season o) {
		return o.year_start - this.year_start;
	}

	public Integer getYear_start() {
		return year_start;
	}

	public void setYear_start(Integer year_start) {
		this.year_start = year_start;
	}

	public Integer getYear_end() {
		return year_end;
	}

	public void setYear_end(Integer year_end) {
		this.year_end = year_end;
	}

	public int getLeague_id() {
		return league_id;
	}

	public void setLeague_id(int league_id) {
		this.league_id = league_id;
	}

	public Standing getStanding() {
		return standing;
	}

	public void setStanding(Standing standing) {
		this.standing = standing;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
