package rmiserver;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface do multicast Server (mesa de voto)
 */
public interface MulticastMethods extends Remote {
    public void chooseMesa(ServerMethods electionsUC) throws RemoteException;
    public int getIntKeyboard(String message) throws RemoteException;
    public void welcomePage(ServerMethods electionsUC) throws RemoteException;
    public Election printEleicoes(ServerMethods electionsUC) throws RemoteException;
    public void listenFromVoteTerminals() throws RemoteException;
    public void HandlerServer(String message) throws RemoteException, InterruptedException, NotBoundException;
    public void sendMessage(String message) throws RemoteException;
    public void doVote(String nomeEleicao, String voto, String username, int cc, ServerMethods electionsUC) throws RemoteException, NotBoundException;
}

