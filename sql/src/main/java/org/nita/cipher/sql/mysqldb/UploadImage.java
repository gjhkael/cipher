package org.nita.cipher.sql.mysqldb;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by guojianhua on 3/28/15.
 */
public class UploadImage {

    Connection conn = DBConnection.getConnection();

    /**
     *
     * @param path imagePath
     * @param name detected person's name
     * @param uri from which video surveilliance
     * @param receivedTime sample time
     * @param sql
     * @param imageName
     * @return
     */
    public boolean storeImage(String path,String name,String uri,String receivedTime,String sql,String imageName){
        InputStream is=null;
        PreparedStatement stmt=null;
        try {
            stmt = conn.prepareStatement(sql);
            is=new FileInputStream(path);
            stmt.setString(1,name);
            stmt.setString(2,uri);
            stmt.setString(3,receivedTime);
            stmt.setBinaryStream(4, is);
            stmt.setString(5,imageName);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            try{
                if(is!=null)
                    try {
                        is.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                if(stmt!=null)
                    stmt.close();
            }catch(SQLException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    public static void main(String... args){
        String sql="insert into picture_info2(picName,cameraUrl,appTime,imageData,imageName)values(?,?,?,?,?)";
        new UploadImage().storeImage("/home/lizhaoxin/lena.jpg", "gjh","211","12",sql,"12");

    }
}
