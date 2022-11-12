package mwbm;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.lang.Math;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;


public class Agent implements AuctionRMI, Runnable{

    int alpha;
    int n;
    int agent_id;
    volatile List<Double> prices;
    volatile List<Integer> bidders = new ArrayList<>();
    List<Double> beta;
    volatile List<Integer> count = new ArrayList<>();
    Map<Integer, Integer> neighbors;
    double epsilon;
    int myPort;
    ReentrantLock lock;
    int communicationCycles = 0;

    int nRMI = 0;

    volatile boolean heardDone;

    List<Double> value = new ArrayList<>();

    Registry registry;
    AuctionRMI stub;

    AtomicBoolean dead;// for testing

    @Override
    public String toString() {
        return "Agent{" +
                "alpha=" + alpha +
                ", agent_id=" + agent_id +
                ", prices=" + prices +
                ", bidders=" + bidders +
                ", beta=" + beta +
                ", count=" + count +
                ", neighbors=" + neighbors +
                ", myPort=" + myPort +
                ", communicationCycles=" + communicationCycles +
                ", nRMI=" + nRMI +
                ", heardDone=" + heardDone +
                ", value=" + value +
                '}';
    }

    public Agent(int agent_id, List<Double> prices, List<Double> beta, HashMap<Integer, Integer> neighbors, int myPort, int n, double epsilon) {
        this.agent_id = agent_id;
        this.prices = prices;
        this.beta = beta;
        this.neighbors = neighbors;
        this.myPort = myPort;
        this.n = n;
        int m = beta.size();
        this.epsilon = epsilon;
        this.lock = new ReentrantLock();

        this.heardDone = false;

        this.dead = new AtomicBoolean(false);

        for (int i = 0; i < n; i++) {
            this.count.add(0);
        }

        for(int j = 0; j<m; j++){
            this.value.add(this.beta.get(j) - this.prices.get(j));
        }

        double maxValue=Integer.MIN_VALUE;
        for (int j = 0; j<m; j++){
            if(maxValue < this.beta.get(j)-this.prices.get(j)){
                maxValue = this.beta.get(j)-this.prices.get(j);
                this.alpha = j;
            }
        }

        for (int j = 0; j < m; j++) {
            this.bidders.add(-1);
        }
        this.bidders.set(this.alpha, agent_id);

        try{
            System.setProperty("java.rmi.server.hostname", "127.0.0.1");
            registry = LocateRegistry.createRegistry(myPort);
            stub = (AuctionRMI) UnicastRemoteObject.exportObject(this, myPort);
            registry.rebind("Auction", stub);
        } catch(Exception e){
            e.printStackTrace();
        }

    }

    public void Call(String rmi, List<Double> p, List<Integer> b, List<Integer> c, Integer id, boolean done, int caller){
        mwbm.AuctionRMI stub;
        try{
//            System.out.println("call port: " + this.neighbors.get(id));
            Registry registry= LocateRegistry.getRegistry(this.neighbors.get(id));
            stub=(mwbm.AuctionRMI) registry.lookup("Auction");
            if(rmi.equals("Auction"))
                stub.Auction(p, b, c, false, done, caller);
            else
                System.out.println("Wrong parameters!");
        } catch(Exception e){
            System.out.println(e);
            
        }
    }


    public void StartAgent()  {
        Thread thread = new Thread(this);
// make sure the threads will be killed when all other threads finish
        thread.setDaemon(true);
        thread.start();
//        new Thread(this).start();
    }

    @Override
    public void run() {
        // initial call to Auction method
        Auction(this.prices, this.bidders, this.count, true, false, -1);
        System.out.println("Done with run");
//        int flag = 0;
//        while(flag == 0){
////            System.out.println(this.count);
//            int tempCount = 0;
//            for(int i = 0; i<this.n; i++) {
//                if(this.count.get(i) >= this.n - 1) {
//                    tempCount += 1;
//                }
//            }
//            if(tempCount == n) {
//                flag = 1;
//            }
//        }
    }


    public void Auction(List<Double> p, List<Integer> b, List<Integer> c, boolean first, boolean done, int caller){
//        System.out.println(Thread.currentThread().getId()+" $$$");
//        System.out.println(Thread.activeCount());
        if(!first)
            this.communicationCycles++;

//        if(this.heardDone) {
//            System.out.println("I'm done "+this.agent_id);
//            return;
//        }

        List<Double> tempPrices = new ArrayList<>();
        for(int j = 0; j<prices.size(); j++){
            tempPrices.add(prices.get(j));
        }

        int m = this.prices.size();
        double previousP_i_alpha = this.prices.get(this.alpha);
        
        for(int i = 0; i < n; i++) {
            this.count.set(i, Math.max(this.count.get(i), c.get(i)));
        }

        for(int j = 0; j < m; j++) {
            if(this.prices.get(j) < p.get(j) || ((Objects.equals(this.prices.get(j), p.get(j))) && this.bidders.get(j) < b.get(j))) {
                this.prices.set(j, p.get(j));
                this.bidders.set(j, b.get(j));
            }
        }

        double v_i = Integer.MIN_VALUE;
        for(int j=0; j<m; j++){
//            System.out.println("this.agent: "+ this.agent_id + " " + (this.beta.get(j) - this.prices.get(j)));
            if(this.beta.get(j) - this.prices.get(j) > v_i){
                v_i = this.beta.get(j) - this.prices.get(j);
            }
        }

        for(int j = 0; j<m; j++){
            this.value.set(j, this.beta.get(j) - this.prices.get(j));
        }

        System.out.println("Step 1 + received from: "+caller+"\n" + this);

        if(previousP_i_alpha<=this.prices.get(this.alpha) && this.bidders.get(this.alpha) != this.agent_id){
            this.count.set(this.agent_id, 0);
            double maxNetValue = Integer.MIN_VALUE;
            int itemWithMaxNetValue = -1;
            for(int j=0; j<m; j++){
                if(this.beta.get(j) - this.prices.get(j) > maxNetValue){
                    maxNetValue = this.beta.get(j) - this.prices.get(j);
                    itemWithMaxNetValue = j;
                }
            }
            this.alpha = itemWithMaxNetValue;
//            if(this.bidders.get(this.alpha) != -1){
                double w_i = Integer.MIN_VALUE;
                for(int j=0; j<m; j++){
                    if(j!=this.alpha) {
                        if (this.beta.get(j) - this.prices.get(j) > w_i) {
                            w_i = this.beta.get(j) - this.prices.get(j);
                        }
                    }
                }

                System.out.println(this.epsilon);
                double gamma = v_i - w_i + this.epsilon;
                System.out.println("this.agent_id " + this.agent_id + " v_i: " + v_i + " w_i: " + w_i + " gamma " + gamma + " " + tempPrices);

                this.prices.set(this.alpha, gamma+this.prices.get(this.alpha));
//            }

            this.bidders.set(this.alpha, this.agent_id);

        }
        else{
            if(!first)
                this.count.set(this.agent_id, this.count.get(this.agent_id)+1);
        }

        System.out.println(this);

        int tempCount = 0;
        for(int i = 0; i<this.n;i++) {
            if(this.count.get(i) >= (this.n - 1)) {
                tempCount += 1;
            }
        }
        if(tempCount == this.n) {
            System.out.println("Exit condition reached for agent: "+this.agent_id);
            this.heardDone = true;
            return;
//            done = true;
        }


        // make rmi calls to neighbors
        for(Map.Entry<Integer, Integer> entry : this.neighbors.entrySet()) {
//            System.out.println("entry.getKey() " + entry.getKey() + " this.agent_id: " + this.agent_id + " this.neighbors: " + this.neighbors);
            this.nRMI++;
            new Thread(() -> Call("Auction", this.prices, this.bidders, this.count, entry.getKey(), done, this.agent_id)).start();
//            Call("Auction", this.prices, this.bidders, this.count, entry.getKey(), done, this.agent_id);
        }
    }


    public void Kill(){
        this.dead.getAndSet(true);
        if(this.registry != null){
            try {
                UnicastRemoteObject.unexportObject(this.registry, true);
            } catch(Exception e){
                System.out.println("None reference");
            }
        }
    }


}

//        lock.lock();
//        System.out.println("##############################################");
////        System.out.println("this.agent_id " + this.agent_id);
//        System.out.println("this.agent_id " + this.agent_id + " this.alpha " + this.alpha);
//        System.out.println("this.agent_id " + this.agent_id + " this.prices " + this.prices);
//        System.out.println("this.agent_id " + this.agent_id + " this.bidders " + this.bidders);
//        System.out.println("this.agent_id " + this.agent_id + " this.count " + this.count);
//        System.out.println("this.agent_id " + this.agent_id + " tempCount " + tempCount);
//        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
//        lock.unlock();

//        System.out.println(this);



//        if(done) {
//            if(!heardDone) {
//                heardDone = true;
//                for (Map.Entry<Integer, Integer> entry : this.neighbors.entrySet()) {
////            System.out.println("entry.getKey() " + entry.getKey() + " this.agent_id: " + this.agent_id + " this.neighbors: " + this.neighbors);
//                    this.nRMI++;
//                    Call("Auction", this.prices, this.bidders, this.count, entry.getKey(), true);
//                }
//            }
//            System.out.println("Bye!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//            return;
//        }


//        int v_i = Integer.MIN_VALUE;
//        for(int j=0; j<m; j++){
//            System.out.println("this.agent: "+ this.agent_id + " " + (this.beta.get(j) - this.prices.get(j)));
//            if(this.beta.get(j) - this.prices.get(j) > v_i){
//                v_i = this.beta.get(j) - this.prices.get(j);
//            }
//        }