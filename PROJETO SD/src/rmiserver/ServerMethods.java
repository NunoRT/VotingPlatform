package rmiserver;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Interface servidor RMI
 */
public interface ServerMethods extends Remote {
    // Funções ServerRMI
    public int checkMe() throws RemoteException;
    public void notificaResultados(Election eleicao) throws RemoteException;
    public void apagaVotacaoDosEleitores(String titulo) throws RemoteException;
    public void readToObejctFile() throws IOException, ClassNotFoundException;
    public void writeObjectFile() throws RemoteException;

    // Funções ClientRMI
    public void connected(ClientMethods newUser) throws RemoteException;
    public String registPeople(String name, String password, String department, String phoneNumber, String morada, int cc, String validadeCC, String tipo) throws RemoteException;
    public ArrayList<Elector> printEleitores() throws RemoteException;
    public String registElection(String titulo, String descricao, String dataInicio, int horaInicio, int minutoInicio, String dataFim, int horaFim, int minutoFim, ArrayList<String> podeBotar) throws RemoteException;
    public String createCandidateList(String titulo, CandidateList lista) throws RemoteException;
    public String removeCandidateList(String eleicao, String lista) throws RemoteException;
    public ArrayList<Election> printEleicoes() throws RemoteException;
    public ArrayList<MesaDeVoto> listMesaVoto() throws RemoteException;
    public String removeMesaVoto(String nome) throws RemoteException;
    public String associaMesa(String nomeMesa, String tituloEleicao) throws RemoteException;
    public ArrayList<Election> getEleicoesPassadas() throws RemoteException;
    public String changeTitulo(String antigo, String novo) throws RemoteException;
    public String changeDescricao(String eleicao, String descricao) throws RemoteException;
    public String changeInstanteInicio(String eleicao, String dataInicio, int horaInicio, int minInicio) throws RemoteException;
    public String changeInstanteFim(String eleicao, String dataFim, int horaFim, int minFim) throws RemoteException;
    public String addMesa(String nome, String add) throws RemoteException;
    public Election getElection(String titulo) throws RemoteException;

    // Proteções
    public boolean checkElection(String titulo) throws RemoteException;
    public boolean checkMesa(String nome) throws RemoteException;
    public boolean checkElectorByCC(int cc) throws RemoteException;
    public boolean checkListaCandidata(String titulo, String nomeLista) throws RemoteException;
    public boolean checkEndereco(String endereco) throws RemoteException;
    public boolean checkPorta(int porta) throws RemoteException;
    public boolean checkCandidate(String tituloEleicao, int cc) throws RemoteException;
    public int eleicaoADecorrer(Election e) throws RemoteException;

        // Funções MultiCastServer
    public void mesaConnected(MulticastMethods newMesa) throws RemoteException;
    public String ativaMesa(String nome) throws RemoteException;
    public String desativaMesa(String nome) throws RemoteException;
    public Elector checkElector(int cc) throws RemoteException;
    public ArrayList<Election> getEletionsOfMesaVoto(String nome) throws RemoteException;
    public void doVoteConfirmation(String nomeEleicao, String voto, String username, int cc, String nome1, int totalMesa, String instante) throws RemoteException;

}
