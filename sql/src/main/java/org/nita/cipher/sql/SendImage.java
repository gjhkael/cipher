package org.nita.cipher.sql;

import org.nita.cipher.sql.data.Picture_W;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;


public class SendImage {
		public void send(Picture_W info)throws InterruptedException{
			Context context = ZMQ.context(1);
			
			// Prepare our context and sockets
			Socket client = context.socket(ZMQ.REQ);
			ZHelper.setId (client); // Set a printable identity
			
			client.connect("ipc://frontend.ipc");
			
			//<!--spark -->
		/*	System.out.println(String.format("Version string: %s, Version int: %d",
					ZMQ.getVersionString(),
					ZMQ.getFullVersion()));*/
			Picture_W test=info;
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutput out = null;
			byte[] t=null;
            byte[] tt = null;
			try{
				out=new ObjectOutputStream(bos);
				out.writeObject(test);
                t=bos.toByteArray();
                tt =new byte[t.length+1];
                for(int i=0;i<t.length;i++){
                    tt[i]=t[i];
                }
                tt[t.length]=1;
			}catch(IOException ex){
				ex.printStackTrace();
			}finally{
				try{
					if(out!=null){
						out.close();
					}
				}catch(IOException ex){
					ex.printStackTrace();
				}
				try {
				    bos.close();
				}catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			System.out.println(tt.length);
			client.send(tt);
			System.out.println("发布了新消息,名字："+test.getName());
			client.close();
			context.term ();	
		}
}