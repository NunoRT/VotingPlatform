package rmiserver;
import java.io.IOException;
import java.rmi.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Interface da admin console
 */
public interface ClientMethods extends Remote {
    public void getVoteNotifying(String vote) throws RemoteException;
    public int getIntKeyboard(String message) throws RemoteException;
    public String readInputMessages(String message) throws RemoteException;
    public void pressToContinue() throws IOException, RemoteException;
    public String setInicialDate(String sms) throws RemoteException;
    public String setFinalDate(String initialDate, String sms) throws RemoteException;
    public String typeOfElector(String sms) throws RemoteException;
    public void newElector(ServerMethods electionsUC) throws RemoteException;
    public ArrayList<String> getPodeBotar(String sms) throws RemoteException;
    public void newElection(ServerMethods electionsUC) throws RemoteException;
    public void getElectors(ServerMethods electionsUC) throws RemoteException;
    public void getElection(ServerMethods electionsUC) throws RemoteException;
    public void newCandidateList(ServerMethods electionsUC) throws RemoteException;
    public void removeCandidateList(ServerMethods electionsUC) throws RemoteException;
    public void CriaMesa(Properties config) throws IOException;
    public void removeMesa(Properties config) throws IOException;
    public void associaMesa(ServerMethods electionsUC) throws RemoteException;
    public void OndeVotouCadaELeitor(ServerMethods electionsUC) throws RemoteException;
    public void MostraEstadoMesas(ServerMethods electionsUC) throws RemoteException;
    public void consultarPassadas(ServerMethods electionsUC) throws RemoteException;
    public String getPercentagens(Election e) throws RemoteException;
    public void welcomePage(ServerMethods electionsUC, Properties config) throws IOException, ParseException;
    public void GerirMesasSubmenu(ServerMethods electionsUC, Properties config) throws IOException;
    public void GerirListasSubmenu(ServerMethods electionsUC) throws IOException;
    public void GerirPropriedadesSubmenu(ServerMethods electionsUC, Properties config) throws IOException, ParseException;
}