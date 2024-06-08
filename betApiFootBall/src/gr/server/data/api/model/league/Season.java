package gr.server.data.api.model.league;

/**
 * We have retrieved a list of leagues. 
 * Then by using the league id we retrieve a list of seasons.
 * The last in the list is the current season.
 * Finally we use the last season's id to retrieve the standing table.
 * 
 * @author liako
 *
 */
public class Season implements Comparable<Season>{
	//premier league 18686
	int id;
	Integer year_start;
	Integer year_end;
	int league_id;
	String slug;
	String name;
	LeagueInfo leagueInfo;
	
//	id:18686
//	id:31497
//	league_id:317
	
	//added by me
	StandingTable standingTable;
	
	
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
	
	
	
	public StandingTable getStandingTable() {
		return standingTable;
	}

	public void setStandingTable(StandingTable standingTable) {
		this.standingTable = standingTable;
	}
	
	

	public LeagueInfo getLeagueInfo() {
		return leagueInfo;
	}

	public void setLeagueInfo(LeagueInfo leagueInfo) {
		this.leagueInfo = leagueInfo;
	}

	@Override
	public boolean equals(Object obj) {
		if (! (obj instanceof Season)) {
			return false;
		}
		
		Season other = (Season) obj;
		return this.id == (other.id);
	}
	
	@Override
	public int hashCode() {
		return this.id * 33;
	}
	
	
	
}
