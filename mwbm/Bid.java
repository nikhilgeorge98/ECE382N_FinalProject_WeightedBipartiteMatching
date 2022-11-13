package mwbm;

import java.io.Serializable;
import java.util.List;

public class Bid implements Serializable {
    int caller;
    java.util.List<Double> prices;
    List<Integer> bidders;
    List<Integer> count;

    public Bid(List<Double> prices, List<Integer> bidders, List<Integer> count, int caller) {
        this.caller = caller;
        this.prices = prices;
        this.bidders = bidders;
        this.count = count;
    }

    @Override
    public String toString() {
        return "Bid{" +
                "caller=" + caller +
                ", prices=" + prices +
                ", bidders=" + bidders +
                ", count=" + count +
                '}';
    }
}
