package mwbm;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * This is the interface of RMI call. You should implement each method defined below.
 * Please don't change the interface.
 */
public interface AuctionRMI extends Remote{
    void Auction(List<Double> p, List<Integer> b, List<Integer> c, boolean b1, boolean done, int caller) throws RemoteException;
}
