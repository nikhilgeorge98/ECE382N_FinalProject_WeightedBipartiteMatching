package mwbm;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.lang.Math;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;


public class AuctionAgent implements AuctionRMI, Runnable{

    long start;
    long end;
    double total;

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

    public AuctionAgent(int agent_id, List<Double> prices, List<Double> beta, HashMap<Integer, Integer> neighbors, int myPort, int n, double epsilon) {
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
        AuctionRMI stub;
        try{
            Registry registry= LocateRegistry.getRegistry(this.neighbors.get(id));
            stub=(AuctionRMI) registry.lookup("Auction");
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
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run() {
        // initial call to Auction method
        Auction(this.prices, this.bidders, this.count, true, false, -1);
        System.out.println("Done with run");
    }


    public void Auction(List<Double> p, List<Integer> b, List<Integer> c, boolean first, boolean done, int caller){
        if(!first)
            this.communicationCycles++;
        else {
            this.start = System.nanoTime();
            System.out.println("Starttime for Agent: " + this.agent_id + " = " + this.start);
        }

        if(this.heardDone) {
//            System.out.println("I'm done "+this.agent_id);
            return;
        }

        List<Double> tempPrices = new ArrayList<>();
        for(int j = 0; j<prices.size(); j++){
            tempPrices.add(prices.get(j));
        }

        int m = this.prices.size();
        double previousP_i_alpha = this.prices.get(this.alpha);

        for(int i = 0; i < n; i++) {
            this.count.set(i, Math.max(this.count.get(i), c.get(i))); //definitely wrong
        }

        for(int j = 0; j < m; j++) {
            if(this.prices.get(j) < p.get(j) || ((Objects.equals(this.prices.get(j), p.get(j))) && this.bidders.get(j) < b.get(j))) {
                this.prices.set(j, p.get(j));
                this.bidders.set(j, b.get(j));
            }
        }

        double v_i = Integer.MIN_VALUE;
        for(int j=0; j<m; j++){
            if(this.beta.get(j) - this.prices.get(j) > v_i){
                v_i = this.beta.get(j) - this.prices.get(j);
            }
        }

        for(int j = 0; j<m; j++){
            this.value.set(j, this.beta.get(j) - this.prices.get(j));
        }

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
            gamma = Math.round(100*gamma)/100.0;
            this.prices.set(this.alpha, gamma+this.prices.get(this.alpha));
            this.bidders.set(this.alpha, this.agent_id);

        }
        else{
            if(!first)
                this.count.set(this.agent_id, this.count.get(this.agent_id)+1);
        }

        int tempCount = 0;
        for(int i = 0; i<this.n;i++) {
            if(this.bidders.get(i) != - 1) {
                tempCount += 1;
            }
        }
        if(tempCount == this.n) {
            System.out.println("Exit condition reached for agent: "+this.agent_id+" "+this.bidders);
            this.end = System.nanoTime();
            System.out.println("Endtime for Agent: "+this.agent_id+" = "+this.end);
            this.total = (this.end - this.start)/1000000.0;
            System.out.println("Totaltime for Agent: "+this.agent_id+" = "+this.total);
            this.heardDone = true;

            return;
        }


        // make rmi calls to neighbors
        for(Map.Entry<Integer, Integer> entry : this.neighbors.entrySet()) {
            this.nRMI++;
            new Thread(() -> Call("Auction", this.prices, this.bidders, this.count, entry.getKey(), done, this.agent_id)).start();
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


    @Override
    public String toString() {
        return "Agent{" +
                "alpha=" + alpha +
                ", agent_id=" + agent_id +
                ", prices=" + prices +
                ", bidders=" + bidders +
                ", beta=" + beta +
                ", neighbors=" + neighbors +
                ", myPort=" + myPort +
                ", communicationCycles=" + communicationCycles +
                ", heardDone=" + heardDone +
                ", value=" + value +
                '}';
    }
}
