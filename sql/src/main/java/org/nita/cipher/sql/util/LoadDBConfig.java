package org.nita.cipher.sql.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by havstack on 3/28/15.
 */
public class LoadDBConfig {
    private Properties properties;


    public LoadDBConfig(String propertyPath) {
        properties = new Properties();

        //read properties from file
        InputStream in = null;
        try{
            in = getClass().getResourceAsStream(propertyPath);
            properties.load(in);
            in.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public String getDriverClass(){
        return properties.getProperty("driverClass");
    }

    public String getURL(){
        return properties.getProperty("url");
    }

    public String getUserName(){
        return properties.getProperty("username");
    }

    public String getPassword(){
        return properties.getProperty("password");
    }

    public static void main(String... args){
        LoadDBConfig ldbc = new LoadDBConfig("/db.properties");
        System.out.println(ldbc.getDriverClass());
    }
}
