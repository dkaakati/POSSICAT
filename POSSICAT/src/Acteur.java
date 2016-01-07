/**
 * 
 */

/**
 * @author François Esnault
 * @date 7 janv. 2016
 */
public class Acteur {
	Role role;
	String name;
	int dispo;
	public Acteur() {};
	public Acteur(Role role, String name) {
		this.role = role;
		this.name = name;
	}
	public Role getRole() {
		return role;
	}
	public void setRole(Role role) {
		this.role = role;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getDispo() {
		return dispo;
	}
	public void setDispo(int dispo) {
		this.dispo = dispo;
	}
	public String toString() {
		return "["+this.getRole()+ " " + this.getName() + "]";
	}
	
	public boolean equals(Object obj) {
	    if (obj == null) return false;
	    if (obj == this) return true;
	    if (!(obj instanceof Acteur)) return false;
	    Acteur o = (Acteur) obj;
	    return o.name == this.name;
	}
}
