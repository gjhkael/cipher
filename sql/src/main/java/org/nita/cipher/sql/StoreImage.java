package org.nita.cipher.sql;

import org.nita.cipher.sql.data.Picture_W;
import org.nita.cipher.sql.mysqldb.UploadImage;
import org.zeromq.ZMQ;

import java.io.ByteArrayInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by havstack on 3/28/15.
 */
public class StoreImage {
    private static final int NBR_WORKERS = 30;
    private static class WorkerTask extends Thread
    {
        public void run()
        {
            ZMQ.Context context = ZMQ.context(1);
            // Prepare our context and sockets
            ZMQ.Socket worker = context.socket(ZMQ.REQ);
            ZHelper.setId (worker); // Set a printable identity

            worker.connect("ipc://backend.ipc");

            // Tell backend we're ready for work
            worker.send("READY");

            while(!Thread.currentThread ().isInterrupted ())
            {
                String address = worker.recvStr ();
                String empty = worker.recvStr ();
                assert (empty.length() == 0);

                // Get request, send reply
                //	String request = worker.recvStr ();
                //System.out.println("Worker: " + request);

                //<!--spark-->

                byte[] recs=worker.recv();
                System.out.println("The length of Data received by Worker : "+recs.length);
                ByteArrayInputStream bis = new ByteArrayInputStream(recs);
                ObjectInput in = null;
                try{
                    in=new ObjectInputStream(bis);
                    Picture_W o=(Picture_W) in.readObject();
                    UploadImage face=new UploadImage();
                    String sql="insert into picture_info2(picName,cameraUrl,appTime,imageData,imageName)values(?,?,?,?,?)";
                    face.storeImage(o.getPath(), o.getName(), o.getUri(),o.getReceivedTime(),sql,o.getImageName());
                    System.out.println("Finished a mysql database inserting operation.");

                }catch(Exception ex){
                    ex.printStackTrace();
                }finally{
                    try {
                        bis.close();
                    }catch (java.io.IOException ex) {
                        // ignore close exception
                    }
                    try {
                        if (in != null) {
                            in.close();
                        }
                    } catch (java.io.IOException ex) {
                        // ignore close exception
                    }
                }
                System.out.println("Worker:::::");
                //<!--spark-->

                worker.sendMore (address);
                worker.sendMore ("");
                worker.send("OK");
            }
            worker.close ();
            context.term ();
        }
    }

    /**
     * This is the main task. It starts the clients and workers, and then
     * routes requests between the two layers. Workers signal READY when
     * they start; after that we treat them as ready when they reply with
     * a response back to a client. The load-balancing data structure is
     * just a queue of next available workers.
     */
    public static void main (String[] args) {
        System.out.println("Initialing context......");
        ZMQ.Context context = ZMQ.context(1);
        // Prepare our context and sockets
        ZMQ.Socket frontend = context.socket(ZMQ.ROUTER);
        ZMQ.Socket backend = context.socket(ZMQ.ROUTER);
        frontend.bind("ipc://frontend.ipc");
        backend.bind("ipc://backend.ipc");

        for (int workerNbr = 0; workerNbr < NBR_WORKERS; workerNbr++)
            new WorkerTask().start();

        // Here is the main loop for the least-recently-used queue. It has two
        // sockets; a frontend for clients and a backend for workers. It polls
        // the backend in all cases, and polls the frontend only when there are
        // one or more workers ready. This is a neat way to use 0MQ's own queues
        // to hold messages we're not ready to process yet. When we get a client
        // reply, we pop the next available worker, and send the request to it,
        // including the originating client identity. When a worker replies, we
        // re-queue that worker, and we forward the reply to the original client,
        // using the reply envelope.

        // Queue of available workers
        Queue<String> workerQueue = new LinkedList<String>();

        System.out.println(NBR_WORKERS + " Wokers has been started to store result.");
        System.out.println("Finishing the process of Initialization and working ......");
        while (!Thread.currentThread().isInterrupted()) {

            // Initialize poll set
            ZMQ.Poller items = new ZMQ.Poller(2);

            //  Always poll for worker activity on backend
            items.register(backend, ZMQ.Poller.POLLIN);

            //  Poll front-end only if we have available workers
            if(workerQueue.size() > 0)
                items.register(frontend, ZMQ.Poller.POLLIN);

            if (items.poll() < 0)
                break;

            // Handle worker activity on backend
            if (items.pollin(0)) {

                // Queue worker address for LRU routing
                workerQueue.add (backend.recvStr ());

                // Second frame is empty
                String empty = backend.recvStr ();
                assert (empty.length() == 0);

                // Third frame is READY or else a client reply address
                String clientAddr = backend.recvStr ();

                // If client reply, send rest back to frontend
                if (!clientAddr.equals("READY")) {

                    empty = backend.recvStr ();
                    assert (empty.length() == 0);

                    String reply = backend.recvStr ();
                    frontend.sendMore(clientAddr);
                    frontend.sendMore("");
                    frontend.send(reply);

                    //if (--clientNbr == 0)
                    //break;
                }

            }

            if (items.pollin(1)) {
                // Now get next client request, route to LRU worker
                // Client request is [address][empty][request]
                String clientAddr = frontend.recvStr ();

                String empty = frontend.recvStr ();
                assert (empty.length() == 0);

                //String request = frontend.recvStr ();
                byte[] request = frontend.recv();

                String workerAddr = workerQueue.poll();

                backend.sendMore (workerAddr);
                backend.sendMore ("");
                backend.sendMore (clientAddr );
                backend.sendMore ("");
                backend.send (request);

            }
        }

        frontend.close();
        backend.close();
        context.term();

    }



}
