package nearlab.erauvisit.Data;

/**
 * Created by christophe on 11/7/2014.
 */
public class DataSingleton {

    private static DataSingleton ourInstance = new DataSingleton();

    public static DataSingleton getInstance() {
        return ourInstance;
    }

    private DataSingleton() {
    }
}
