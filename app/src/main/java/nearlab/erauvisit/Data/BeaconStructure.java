package nearlab.erauvisit.Data;

/**
 * Created by christophe on 11/7/2014.
 */
public class BeaconStructure {
    private String name;
    private String UUID ;
    private int major;
    private int minor;
    private String URL;

    public BeaconStructure() {
    }

    public BeaconStructure(String UUID, int minor, int major, String URL) {
        this.UUID = UUID;
        this.minor = minor;
        this.major = major;
        this.URL = URL;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }
}
