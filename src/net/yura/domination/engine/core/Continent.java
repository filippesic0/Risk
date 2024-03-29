// Yura Mamyrin, Group D

package net.yura.domination.engine.core;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;


/**
 * <p> Continent </p>
 * @author Yura Mamyrin
 */

public class Continent implements Serializable {

	private static final long serialVersionUID = 1L;

	private String idString; // used by the map editor

	private String name;
	private int color;
	private int armyValue;
	private Vector territoriesContained = new Vector();
	private transient Vector borderCountries;
	private transient Vector incomingborderCountries;
	public int[] playerown;
	public int id;

	/**
	 * Creates a continent object
         * @param id unique string id of the continent, without spaces
	 * @param name the continent name
         * @param numberOfArmies the number of armies recieved when the whole continent is owned
	 * @param color the continent colour
     * @param playerown[] the number of territories which players own in continent
	 */
	public Continent(String id, String name, int numberOfArmies, int color, int ID) {
		idString = id;
		this.name	= name;
		this.color	= color;
		armyValue	= numberOfArmies;
		playerown=new int[7];
		playerown[0]=0;
		playerown[1]=0;
		playerown[2]=0;
		playerown[3]=0;
		playerown[4]=0;
		playerown[5]=0;
		playerown[6]=0;
		this.id=ID;
	}

	public String toString() {

		if (armyValue!=0) {

			return idString+" ["+armyValue+"]";
		}

		return idString;

	}

	public String getIdString() {

		return idString;

	}

	public void setIdString(String a) {

		idString = a;

	}

	/**
	 * Returns the name of the continent
	 * @return name
	 */
	public String getName() {
		return name;
	}

	public void setName(String a) {
		name = a;
	}

	/**
	 * Returns the colour of the continent
	 * @return color
	 */
	public int getColor() {
		return color;
	}

	public void setColor(int a) {
		color = a;
	}

	/**
	 * Returns the number of armies the continent is worth
	 * @return armyValue
	 */
	public int getArmyValue() {
		return armyValue;
	}

	public void setArmyValue(int a) {
		armyValue = a;
	}

	/**
	 * Checks if the player owns all the territories within a continent
	 * @param p player object
	 * @return true if the player owns all the territories within a continent,
	 * otherwise false if the player does not own the all the territories
	 */
	public boolean isOwned(Player p) {
		for (int c=0; c< territoriesContained.size() ; c++) {

			if ( ((Country)territoriesContained.elementAt(c)).getOwner() != p ) {
				return false;
			}

		}
		return true;
	}
	
	/**
	 * Checks the percent of countries the player owns within a continent
	 * @param p player object
	 * @return the percentage
	 */
	public int getNumberOwned(Player p) {

		int ownedByPlayer=0;

		for (int c=0; c< territoriesContained.size() ; c++) {

			if ( ((Country)territoriesContained.elementAt(c)).getOwner() == p ) {
				ownedByPlayer++;
			}

		}
		return ownedByPlayer;
	}

	/*****
	 * @name getOwner
	 * @return player who owns the continent
	 *  else null if no one owns the continent
	 ****/
	public Player getOwner(){
		Player owner = ((Country)territoriesContained.elementAt(0)).getOwner();
		for (int c=1; c< territoriesContained.size() && owner != null ; c++) {
			if ( ((Country)territoriesContained.elementAt(c)).getOwner() != owner ) {
				owner = null;
			}
		}
		return owner;
	}
	
	public Vector getBorderCountries() {
		if (borderCountries == null) {
			Vector b = new Vector(2);
			for (int i = 0; i < territoriesContained.size(); i++) {
				Country country = (Country) territoriesContained.get(i);
				Vector w = country.getNeighbours();
				for (int k = 0; k < w.size(); k++) {
					if (((Country) w.elementAt(k)).getContinent() != this) {
						/* This is a territory to protect from */
						b.add(country);
						break;
					}
				}
			}
			this.borderCountries = b; 
		}
		return this.borderCountries;
	}
	
	public Vector getIncomingBorderCountries() {
		if (incomingborderCountries == null) {
			Vector b = new Vector(2);
			for (int i = 0; i < territoriesContained.size(); i++) {
				Country country = (Country) territoriesContained.get(i);
				List<Country> w = country.getIncomingNeighbours();
				for (int k = 0; k < w.size(); k++) {
					if (((Country) w.get(k)).getContinent() != this) {
						b.add(country);
						break;
					}
				}
			}
			this.incomingborderCountries = b; 
		}
		return this.incomingborderCountries;
	}

	/**
	 * Returns a vector of the territories contained within a continent
	 * @return territoriesContained
	 */
	public Vector getTerritoriesContained() {
		return territoriesContained;
	}

	/**
	 * Adds country to a continent
	 * @param t the country object
	 */
	public void addTerritoriesContained(Country t) {
		this.borderCountries = null;
		territoriesContained.add(t);
	}

	public boolean equals(Object o) {

		return (o != null &&
			o instanceof Continent &&
			((Continent)o).name.equals(name) &&
			((Continent)o).idString.equals(idString) &&
			((Continent)o).armyValue == armyValue
		);

	}

}
