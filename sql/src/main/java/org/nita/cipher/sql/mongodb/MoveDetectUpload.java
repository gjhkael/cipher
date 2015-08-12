package org.nita.cipher.sql.mongodb;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;

/**
 * Created by guojianhua on 3/28/15.
 */
public class MoveDetectUpload {
    private static FileOutputStream os;
    public static void main(String[] args) {
        //File file =new File("/tmp/data_res/gjh.jpg");
        //SaveFile("movedetect",file,"gjh.jpg","192.168.1.241","gjh.jpg");
        GridFSDBFile f = retrieveFileOne("movedetect","gjh.jpg");
        System.out.println(f.getFilename());
        InputStream s= f.getInputStream();
        try {
            byte[] buf = new byte[1024*1024];
            s.read(buf);
            os = new FileOutputStream("/home/havstack/tt.jpg");
            os.write(buf);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    //存储图片
    public static void SaveFile(String collectionName, File file,
                                String fileid, String companyid, String filename) {
        try {
            @SuppressWarnings("deprecation")
            Mongo mongo = new Mongo("localhost", 27017);
            DB db = mongo.getDB("shsc");
            System.out.println("Connect to database successfully");
            // DBCollection coll = db.getCollection("movedetect");
            // System.out.println("Collection movedetect selected successfully");
            // 存储fs的根节点
            GridFS gridFS = new GridFS(db, collectionName);
            GridFSInputFile gfs = gridFS.createFile(file);
            gfs.put("cameraUrl", companyid);
            gfs.put("filename", fileid);
            gfs.put("contentType",filename.substring(filename.lastIndexOf(".")));
            gfs.save();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("存储文件时发生错误！！！");
        }
    }
    //显示图片
    public static GridFSDBFile retrieveFileOne(String collectionName,
                                               String filename) {
        try {
            @SuppressWarnings("deprecation")
            Mongo mongo = new Mongo("localhost", 27017);
            DB db = mongo.getDB("shsc");
            // 获取fs的根节点
            GridFS gridFS = new GridFS(db, collectionName);
            GridFSDBFile dbfile = gridFS.findOne(filename);
            //dbfile.writeTo(filename); //可以在这里保存图片到本地
            if (dbfile != null) {
                return dbfile;
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }

    //删除图片
    @SuppressWarnings("deprecation")
    public static void deleteFile(String collectionName,String filename){
        Mongo mongo;
        try {
            mongo = new Mongo("localhost", 27017);
            DB db = mongo.getDB("shsc");
            GridFS gridFS = new GridFS(db, collectionName);

            gridFS.remove(gridFS.findOne(filename));

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}

