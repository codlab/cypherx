package eu.codlab.cypherx.webservice.models;

/**
 * Created by kevinleperf on 11/07/15.
 */
public class Authent {
    public String guid;
    public String pass;

    public Authent(String guid, String pass) {
        this.guid = guid;
        this.pass = pass;
    }
}
