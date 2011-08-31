package org.opensixen.server.manager.ui.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.opensixen.server.manager.ui.db.DB;

public class MPackage {
	private String id;
	private String version;
	private String url;
	private String name;
	private String description;
	private Timestamp lastUpdate;
	
	
	
	/**
	 * 
	 */
	public MPackage() {
		super();
	}
	
	

	/**
	 * 
	 */
	private MPackage(ResultSet rs) throws SQLException {
		super();
		
		setId(rs.getString("id"));
		setVersion(rs.getString("version"));
		setName(rs.getString("name"));
		setDescription(rs.getString("description"));
		setUrl(rs.getString("url"));
		
	}
	
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}
	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the lastUpdate
	 */
	public Timestamp getLastUpdate() {
		return lastUpdate;
	}
	/**
	 * @param lastUpdate the lastUpdate to set
	 */
	public void setLastUpdate(Timestamp lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
			
	public static List<MPackage> getInstalled() throws SQLException	{
		ArrayList<MPackage> installed = new ArrayList<MPackage>();
		String sql = "select * from sys_package";
		PreparedStatement psmt = DB.getPsmt(sql);
		ResultSet rs = psmt.executeQuery();
		while (rs.next())	{
			MPackage pkg = new MPackage(rs);
			installed.add(pkg);
		}
		return installed;
	}
}
