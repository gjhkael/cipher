package org.nita.cipher.sql.data;

import java.io.Serializable;

/**
 * Created by guojianhua on 3/28/15.
 */
public class Picture_W implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;			// 目标名字
    private String uri;				// 目标出现的位置
    private String receivedTime;    // 目标出现的时间
    private String path;            // 目标路径
    private String imageName;

    public Picture_W(){

    }
    public Picture_W(String name,String uri, String receivedTime,
                     String path,String imageName){
        this.name=name;
        this.uri=uri;
        this.receivedTime=receivedTime;
        this.path=path;
        this.imageName=imageName;
    }

    public Picture_W(String uri, String receivedTime,
                     String path){
        super();
        this.uri=uri;
        this.receivedTime=receivedTime;
        this.path=path;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getReceivedTime() {
        return receivedTime;
    }
    public void setReceivedTime(String receivedTime) {
        this.receivedTime = receivedTime;
    }
    public String getUri() {
        return uri;
    }
    public void setUri(String uri) {
        this.uri = uri;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }




}
