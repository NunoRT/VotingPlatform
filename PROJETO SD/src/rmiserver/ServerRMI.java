package rmiserver;
import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Classe Servidor RMI
 */
public class ServerRMI extends UnicastRemoteObject implements ServerMethods {
    private AtomicInteger pingNumber;
    private Elector newElector = null;
    private Election newElection = null;
    private ClientMethods client = null;
    private MulticastMethods mesa = null;
    private DadosSistema dados = null;
    private Thread checkTime = null;
    private Thread primary;

    /**
     * Construtor da classe que inicia a thread de verificação do tempo para saber se alguma eleição já começou
     * @param pingNumber
     * @throws RemoteException
     */

    public ServerRMI(int pingNumber, Properties config) throws RemoteException {
        this.pingNumber = new AtomicInteger(pingNumber);
        checkTime = new Thread(() -> {
            while(true) {
                System.out.println("A verificar tempo...");

                Date date = new Date();
                Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

                // Se a estrutura que guarda os dados ainda não tiver sido criada ele não verifica o tempo
                if (dados != null) {
                    // Verifica se cada eleição começou ou acabou
                    for (Election e: dados.getListaEleicoes()) {
                        if (e.getDataInicio().equals(formatter.format(date))) {
                            if (e.getHoraInicio() == calendar.get(Calendar.HOUR_OF_DAY)) {
                                if (e.getMinInicio() == calendar.get(Calendar.MINUTE)) {
                                    if(!e.getListaListas().isEmpty()) {
                                        System.out.println("Eleição: " + e.getTitulo() + " começou!");
                                        // Altera a variável que indica se a eleição está a decorrer para true
                                        e.setADecorrer(true);
                                        break;
                                    }
                                    else{
                                        // Se a eleição não contiver listas candidatas, a eleição não inicia porque não há vencedores
                                        System.out.println("Eleição não contém listas, a remover eleição...");
                                        dados.removeEleicoes(e);
                                        dados.removeEleicaoFromMesas(e);
                                        break;
                                    }
                                }
                            }
                        }

                        if (e.getDataFim().equals(formatter.format(date))) {
                            if (e.getHoraFim() == calendar.get(Calendar.HOUR_OF_DAY)) {
                                if (e.getMinFim() == calendar.get(Calendar.MINUTE)) {
                                    System.out.println("Eleição: " + e.getTitulo() + " acabou!");
                                    dados.removeEleicoes(e);
                                    dados.addListaEleicoesPassadas(e);
                                    dados.removeEleicaoFromMesas(e);
                                    apagaVotacaoDosEleitores(e.getTitulo());
                                    try {
                                        notificaResultados(e);
                                        break;
                                    } catch (RemoteException remoteException) {
                                        remoteException.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }

                try {
                    checkTime.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        checkTime.start();

        primary = new Thread(() -> {

            DatagramPacket packet;
            String message;
            byte[] buffer;
            int port;
            InetAddress address;
            DatagramSocket socket = null;
            try {
                socket = new DatagramSocket(Integer.parseInt(config.getProperty("UDP_PORT")));
            } catch (SocketException e) {
                e.printStackTrace();
            }

            while (true) {

                // Recebe
                try {
                    buffer = new byte[256];
                    packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    message = new String(packet.getData(), 0, packet.getLength());
                    port = packet.getPort();
                    address = packet.getAddress();
                    message = "type|isAlive";
                    buffer = message.getBytes();
                    packet = new DatagramPacket(buffer, buffer.length, address, port); // config
                    socket.send(packet);
                } catch (Exception e){
                    System.out.println("Exception: " + e);
                    break;
                }
            }

        });
        primary.start();

        try {
            readToObejctFile();
        } catch (FileNotFoundException f) {
            System.out.println("Nenhuma db encontrado! A criar um...");
            dados = new DadosSistema();
        } catch (IOException | ClassNotFoundException i) {
            System.out.println("Erro a ler ficheiro ou classe desconhecida");
            System.exit(0);
        }
    }

    /**
     * Método que notifica os resultados da eleição à admin console
     * @param eleicao
     * @throws RemoteException
     */

    public void notificaResultados(Election eleicao) throws RemoteException {
        String nomeLista = null;
        int votos = 0;

        String messageToAdmin = "";

        messageToAdmin = messageToAdmin.concat("Vou apurar os resultados da eleição " + eleicao.getTitulo() + "...\n" +
                                               "\t\tForam recebidos " + eleicao.getVotosAbs() + " voto(s) absoluto(s), " + eleicao.getVotosBr() + " voto(s) branco(s) e " + eleicao.getVotosNul() + " voto(s) nulo(s)" + "\n" +
                                               "\t\tA eleição contou com " + eleicao.getListaListas().size() + " lista(s) candidata(s)" + "\n");

        for (CandidateList c: eleicao.getListaListas()) {
            messageToAdmin = messageToAdmin.concat("\t\tA lista " + c.getNome() + " teve um total de " + c.getVotosAbs() + " voto(s)" + "\n");
            if (c.getVotosAbs() > votos) {
                nomeLista = c.getNome();
                votos = c.getVotosAbs();
            }
        }

        eleicao.setWinner(nomeLista);

        messageToAdmin = messageToAdmin.concat("\t\tA lista vencedora é: " + nomeLista);

        client.getVoteNotifying(messageToAdmin);
    }

    /**
     * Quando a eleição acaba, esta é apagada do array de eleições votadas de cada eleitor, para que
     * quando criada outra eleição com o mesmo nome, os eleitores possam voltar a votar
     * @param titulo
     */

    public void apagaVotacaoDosEleitores(String titulo) {
        for (Elector e: dados.getListaEleitores()) {
            e.getEleicoesVotadas().remove(titulo);
        }
        writeObjectFile();
    }

    /**
     * Método que guarda a estrutura de dados no ficheiro objeto
     * @throws IOException
     * @throws ClassNotFoundException
     */

    public void readToObejctFile() throws IOException, ClassNotFoundException{
            File f = new File("DadosSistema.obj");
            FileInputStream fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);
            dados = (DadosSistema) ois.readObject();
            ois.close();
    }

    /**
     * Método que lê do ficheiro objeto e carrega para a estrutura de dados
     */

    public void writeObjectFile() {
        File fn = new File("DadosSistema.obj");

        try {
            FileOutputStream f = new FileOutputStream(fn);
            ObjectOutputStream o = new ObjectOutputStream(f);

            o.writeObject(dados);

            o.close();
        } catch (FileNotFoundException e) {
            System.out.println("Erro a criar ficheiro...");
        } catch (IOException e) {
            System.out.println("Erro a escrever para o ficheiro...");
        }
    }

    /**
     * Método que verifica se uma eleição se encontra na db
     * @param titulo
     * @return
     * @throws RemoteException
     */

    public boolean checkElection(String titulo) throws RemoteException {
        for (Election e: dados.getListaEleicoes()) {
            if (e.getTitulo().equals(titulo)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Método que verifica se a eleição está a decorrer
     * @param e
     * @return
     * @throws RemoteException
     */

    public int eleicaoADecorrer(Election e) throws RemoteException {
        if (e.getADecorrer()) {
            return 1;
        }
        else if (dados.getListaEleicoesPassadas().contains(e)) {
            return -1;
        }
        return 0;
    }

    /**
     * Método que verifica se uma mesa de voto se encontra na db
     * @param nomeMesa
     * @return
     * @throws RemoteException
     */

    public boolean checkMesa(String nomeMesa) throws RemoteException {
        for (MesaDeVoto m: dados.getListaMesas()) {
            if (m.getNome().equals(nomeMesa)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Método que verifica se o eleitor se encontra na db através do cc
     * @param cc
     * @return
     * @throws RemoteException
     */

    public boolean checkElectorByCC(int cc) throws RemoteException {
        for (Elector e: dados.getListaEleitores()) {
            if (e.getCC() == cc) {
                return true;
            }
        }
        return false;
    }

    /**
     * Método que verifica se já existe uma lista candidata numa dada eleição
     * @param titulo
     * @param nomeLista
     * @return
     * @throws RemoteException
     */

    public boolean checkListaCandidata(String titulo, String nomeLista) throws RemoteException {
        for (Election e: dados.getListaEleicoes()) {
            if (e.getTitulo().equals(titulo)) {
                for (CandidateList c: e.getListaListas()) {
                    if (c.getNome().equals(nomeLista)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Método que verifica se já existe uma mesa com o endereço dado
     * @param endereco
     * @return
     * @throws RemoteException
     */

    public boolean checkEndereco(String endereco) throws RemoteException {
        for (MesaDeVoto m: dados.getListaMesas()) {
            String[] settings = m.getAdd().split(";");
            if (settings[0].equals(endereco) || settings[1].equals(endereco)) {
                return true;
            }
        }

        return false;
    }

    /**
     *  Método que verifica se já existe uma porta associada a outra mesa
     * @param porta
     * @return
     * @throws RemoteException
     */

    public boolean checkPorta(int porta) throws RemoteException {
        for (MesaDeVoto m: dados.getListaMesas()) {
            String[] settings = m.getAdd().split(";");
            if (settings[2].equals(String.valueOf(porta))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Método que verifica se, numa dada eleição, já existe o candidato em questão associado a outra lista
     * @param tituloEleicao
     * @param cc
     * @return
     * @throws RemoteException
     */

    public boolean checkCandidate(String tituloEleicao, int cc) throws RemoteException {
        for (Election e: dados.getListaEleicoes()) {
            if (e.getTitulo().equals(tituloEleicao)) {

                if (e.getListaListas().isEmpty()) {
                    return false;
                }

                for (CandidateList c: e.getListaListas()) {

                    if (c.getLista().isEmpty()) {
                        return false;
                    }

                    for (Elector el: c.getLista()) {
                        if (el.getCC() == cc) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Método que valida a ligação com o RMI Client
     * @param newUser
     * @throws RemoteException
     */

    public void connected(ClientMethods newUser) throws RemoteException {
        System.out.println("[Admin conectado]");
        client = newUser;
    }

    /**
     * Método que valida a ligação com o Multicast Server
     * @param newMesa
     * @throws RemoteException
     */

    public void mesaConnected(MulticastMethods newMesa) throws RemoteException {
        System.out.println("[Mesa connectada]");
        mesa = newMesa;
    }

    // MÉTODOS PARA AS MESAS DE VOTO

    /**
     * Ativa a mesa quando iniciada pelo MultiCast Server
     * @param nome
     * @return
     * @throws RemoteException
     */

    public String ativaMesa(String nome) throws RemoteException {
        // Procura a mesa no array de mesas de voto
        for (MesaDeVoto m: dados.getListaMesas()) {
            if (m.getNome().equals(nome)) {
                m.setActive(true);
                writeObjectFile();
                return "[Mesa ativa]";
            }
        }

        return "Mesa não encontrada...";
    }

    /**
     * Destiva a mesa quando iniciada pelo MultiCast Server
     * @param nome
     * @return
     * @throws RemoteException
     */

    public String desativaMesa(String nome) throws RemoteException {
        // Procura a mesa no array de mesas de voto
        for (MesaDeVoto m: dados.getListaMesas()) {
            if (m.getNome().equals(nome)) {
                m.setActive(false);
                writeObjectFile();
                return "[Mesa desativa]";
            }
        }

        return "Mesa não encontrada...";
    }

    /**
     * Adiciona a mesa na ArrayList de mesas da base de dados
     * @param nome
     * @param add
     * @return
     * @throws RemoteException
     */


    public String addMesa(String nome, String add) throws RemoteException {
        for (MesaDeVoto m: dados.getListaMesas()) {
            if (m.getNome().equals(nome)) {
                return "Já existe uma mesa com o mesmo nome...\n";
            }
        }

        MesaDeVoto mesa = new MesaDeVoto(nome, add);
        dados.addMesa(mesa);
        writeObjectFile();

        return "\n\t\t[Mesa de voto adicionada]\n\t\t>>> Corra MultiCastServer para ativar a mesa\n";
    }

    /**
     * Confirma a receção de um voto e envia a informação para a admin console
     * @param voto
     * @param totalMesa
     * @throws RemoteException
     */

    public void doVoteConfirmation(String nomeEleicao, String voto, String username, int cc, String nome1, int totalMesa, String instante) throws RemoteException {

        MesaDeVoto mesa = null;

        // Mete eleitor na lista de votantes da mesa de voto em questão
        for (MesaDeVoto m: dados.getListaMesas()) {
            if (m.getNome().equals(nome1)) {
                m.addVotante(username);
                m.setTotalVotos(totalMesa);
                mesa = m;
                break;
            }
        }

        // Mete eleição como votada no array do eleitor para que não vote de novo
        for (Elector e: dados.getListaEleitores()) {
            if (e.getCC() == cc) {
                e.addEleicaoVotada(nomeEleicao);
            }
        }

        // Dá update do voto na db
        for (Election e: dados.getListaEleicoes()) {
            if (e.getTitulo().equals(nomeEleicao)) {

                e.addHistoricoVoto(username, nome1 + ";" + instante);

                if (voto.equals("Branco")) {
                    e.setVotosBr();
                    break;
                }
                if (voto.equals("Nulo")) {
                    e.setVotosNul();
                    break;
                }

                e.setVotosAbs();
                for (CandidateList c: e.getListaListas()) {
                    if (c.getNome().equals(voto)) {
                        c.setVotosAbs();
                        break;
                    }
                }
            }
        }

        // Notifica a admin console que ouve um voto
        client.getVoteNotifying(">>> Voto na mesa " + nome1 + " - (Total de " + mesa.getTotalVotos() + " votos na mesa " + mesa.getNome() + ")");
        writeObjectFile();
    }

    /**
     * Verifica se o eleitor consta nos cadernos eleitorais
     * @param cc
     * @return
     */

    public Elector checkElector(int cc) {
        // Procura o eleitor através do cc e retorna-o se o encontrar na db
        for (Elector e: dados.getListaEleitores()) {
            if (e.getCC() == cc) {
                return e;
            }
        }

        return null;
    }

    /**
     * Método que procura a eleição na db
     * @param titulo
     * @return
     * @throws RemoteException
     */

    public Election getElection(String titulo) throws RemoteException {
        // Procura a eleição através do título e retorna-a se a encontrar na db
        for (Election e: dados.getListaEleicoes()) {
            if (e.getTitulo().equals(titulo)) {
                return e;
            }
        }

        return null;
    }


    /**
     * Retorna a lista de eleições associadas à mesa de voto
     * @param nome
     * @return
     * @throws RemoteException
     */

    public ArrayList<Election> getEletionsOfMesaVoto(String nome) throws RemoteException {
        for (MesaDeVoto m: dados.getListaMesas()) {
            if (m.getNome().equals(nome)) {
                return m.getListaEleicoes();
            }
        }

        return null;
    }


    // MÉTODOS PARA A ADMIN CONSOLE

    /**
     * Método que adiciona uma nova eleição
     * @param titulo
     * @param descricao
     * @param dataInicio
     * @param horaInicio
     * @param minInicio
     * @param dataFim
     * @param horaFim
     * @param minFim
     * @param podeBotar
     * @return
     * @throws RemoteException
     */

    public String registElection(String titulo, String descricao, String dataInicio, int horaInicio, int minInicio, String dataFim, int horaFim, int minFim, ArrayList<String> podeBotar) throws RemoteException {
        this.newElection = new Election(titulo, descricao, dataInicio, horaInicio, minInicio, dataFim, horaFim, minFim, podeBotar);
        dados.addEleicao(newElection);
        writeObjectFile();
        return "[SUCESS ELECTION REGISTRY] - " + titulo;
    }

    /**
     * Método que retorna a lista de eleições
     * @return
     * @throws RemoteException
     */

    public ArrayList<Election> printEleicoes() throws RemoteException {
        return dados.getListaEleicoes();
    }

    /**
     * Método que regista os eleitores
     * @param name
     * @param password
     * @param department
     * @param phoneNumber
     * @param morada
     * @param cc
     * @param validadeCC
     * @param tipo
     * @return
     * @throws RemoteException
     */

    public String registPeople(String name, String password, String department, String phoneNumber, String morada, int cc, String validadeCC, String tipo) throws RemoteException {
        this.newElector = new Elector(name, password, department, Integer.parseInt(phoneNumber), morada, cc, validadeCC, tipo);
        dados.addEleitor(newElector);
        writeObjectFile();
        return "[Eleitor adicionado] - " + name;
    }

    /**
     * Método que retorna a lista de eleitores
     * @return
     */

    public ArrayList<Elector> printEleitores() throws RemoteException {
        return dados.getListaEleitores();
    }

    /**
     * Método que cria uma lista candidata a uma eleição
     * @param titulo
     * @param lista
     * @return
     * @throws RemoteException
     */

    public String createCandidateList(String titulo, CandidateList lista) throws RemoteException {
        // Adiciona uma nova lista candidata. Se os tipos forem incompativeis não cria, pois não se pode ter listas
        // de estudantes numa eleição de docentes, por exemplo
        for (Election el: dados.getListaEleicoes()) {
            if (!el.getADecorrer()) {
                if (el.getTitulo().equals(titulo)){
                    if (el.getPodeBotar().containsAll(lista.getRestricao())) {
                        el.addCandidateList(lista);
                        writeObjectFile();
                        return "[Lista candidata adicionada] - Eleição: " + titulo;
                    }
                    else {
                        return "Tipos incompativeis...\n";
                    }
                }
            }
        }

        return "Eleição não encontrada ou já acabou...\n";
    }

    /**
     * Método que remove uma lista candidata de uma eleição
     * @param eleicao
     * @param lista
     * @return
     * @throws RemoteException
     */

    public String removeCandidateList(String eleicao, String lista) throws RemoteException {
        for (Election e: dados.getListaEleicoes()) {
            if (e.getTitulo().equals(eleicao)) {
                if (!e.getADecorrer()) {
                    for (CandidateList c: e.getListaListas()) {
                        if (c.getNome().equals(lista)) {
                            e.getListaListas().remove(c);
                            return "[Lista " + c.getNome() + " removida com sucesso]\n";
                        }
                    }
                }
                else {
                    return "Lista não pode ser removida porque a eleição já começou...\n";
                }
            }
        }

        return "Nome da eleição ou lista não existem...\n";
    }


    /**
     * Remove uma mesa de voto
     * @param nome
     * @return
     * @throws RemoteException
     */

    public String removeMesaVoto(String nome) throws RemoteException {
        // Remove uma mesa do array de mesas de voto, se existir o nome
        for (MesaDeVoto m: dados.getListaMesas()) {
            if (m.getNome().equals(nome)) {
                dados.getListaMesas().remove(m);
                writeObjectFile();
                return "[Mesa de voto removida, desligue o processo]";
            }
        }
        return "Nenhuma mesa com esse nome...";
    }

    /**
     * Associa uma mesa de voto a uma eleição
     * @param nomeMesa
     * @param tituloEleicao
     * @return
     * @throws RemoteException
     */

    public String associaMesa(String nomeMesa, String tituloEleicao) throws RemoteException {
        // Associa uma mesa de voto a uma eleição se a mesa e a eleição existirem
        for (Election e: dados.getListaEleicoes()) {
            if (e.getTitulo().equals(tituloEleicao)) {
                for (MesaDeVoto m: dados.getListaMesas()) {
                    if (m.getNome().equals(nomeMesa)) {
                        m.addElection(e);
                        writeObjectFile();
                        return "[Mesa adicionada a eleição] - Mesa: " + e.getTitulo();
                    }
                }
            }
        }

        return "Nenhuma mesa ou eleição com esse nome...";
    }

    /**
     * Retorna a lista das mesas de votos ativas
     * @return
     * @throws RemoteException
     */

    public ArrayList<MesaDeVoto> listMesaVoto() throws RemoteException {
        return dados.getListaMesas();
    }

    /**
     * Altera o título da eleição
     * @param antigo
     * @param novo
     * @return
     * @throws RemoteException
     */

    public String changeTitulo(String antigo, String novo) throws RemoteException{
        // Muda o titulo da eleição em questão se esta ainda não tiver começado
        for (Election e: dados.getListaEleicoes()) {
            if (e.getTitulo().equals(antigo)) {
                if (!e.getADecorrer()) {
                    e.setTitulo(novo);
                    writeObjectFile();
                    return "[Titulo alterado] - " + antigo + " para " + novo;
                }
                else {
                    return "Eleição já começou ou já acabou...";
                }
            }
        }

        return "Eleição não encontrada...";
    }

    /**
     * Método que altera a descrição da eleição
     * @param eleicao
     * @param descricao
     * @return
     * @throws RemoteException
     */

    public String changeDescricao(String eleicao, String descricao) throws RemoteException{
        // Muda a descrição em questão, se esta ainda não tiver começado.
        for (Election e: dados.getListaEleicoes()) {
            if (e.getTitulo().equals(eleicao)) {
                if (!e.getADecorrer()) {
                    e.setDescricao(descricao);
                    writeObjectFile();
                    return "[Descrição alterada]";
                }
                else {
                    return "Eleição já começou ou já acabou...";
                }
            }
        }

        return "Eleição não encontrada...";
    }

    /**
     * Método que altera o instante inicial da eleição
     * @param eleicao
     * @param dataInicio
     * @param horaInicio
     * @param minInicio
     * @return
     * @throws RemoteException
     */

    public String changeInstanteInicio(String eleicao, String dataInicio, int horaInicio, int minInicio) throws RemoteException{
        // Muda o instante inicial da eleição em questão se este ainda não tiver começado.
        for (Election e: dados.getListaEleicoes()) {
            if (e.getTitulo().equals(eleicao)) {
                if (!e.getADecorrer()) {
                    e.setDataInicio(dataInicio);
                    e.setHoraInicio(horaInicio);
                    e.setMinInicio(minInicio);
                    writeObjectFile();
                    return "[Instante incial alterado]";
                }
                else {
                    return "Eleição já começou ou já acabou...";
                }
            }
        }

        return "Eleição não encontrada...";
    }

    /**
     * Método que altera o instante final da eleição
     * @param eleicao
     * @param dataFim
     * @param horaFim
     * @param minFim
     * @return
     * @throws RemoteException
     */

    public String changeInstanteFim(String eleicao, String dataFim, int horaFim, int minFim) throws RemoteException{
        // Muda o instante final da eleição em questão se este ainda não tiver começado.
        for (Election e: dados.getListaEleicoes()) {
            if (e.getTitulo().equals(eleicao)) {
                if (!e.getADecorrer()) {
                    e.setDataFim(dataFim);
                    e.setHoraFim(horaFim);
                    e.setMinFim(minFim);
                    writeObjectFile();
                    return "[Instante final alterado]";
                }
                else {
                    return "Eleição já começou ou já acabou...";
                }
            }
        }

        return "Eleição não encontrada...";
    }

    /**
     * Retorna a lista do histórico de eleições
     * @return
     * @throws RemoteException
     */

    public ArrayList<Election> getEleicoesPassadas() throws RemoteException {
        return dados.getListaEleicoesPassadas();
    }

    /**
     * Método que envia ping para RMI server principal
     * @return
     * @throws RemoteException
     */

    public int checkMe() throws RemoteException {
        return this.pingNumber.intValue();
    }

    /**
     * Método que limpa o terminal
     */
    private static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void main(String[] args) throws RemoteException {
        clearScreen();
        String configFile = "rmiserver/configurations.properties";
        InputStream inputStream = ServerRMI.class.getClassLoader().getResourceAsStream(configFile);
        Properties config = new Properties();
        try {
            config.load(inputStream);
        } catch (Exception e) {
            System.out.println("Não é possível ler o ficheiro...");
            return;
        }
        connection(0, config);
    }

    /**
     * Trata da conexão do Server RMI usando o ficheiro properties
     * @param pingNumber
     * @param config
     * @throws RemoteException
     */

    public static void connection(int pingNumber, Properties config) throws RemoteException {
        try {
            Registry regis = LocateRegistry.createRegistry(Integer.parseInt(config.getProperty("REGISTRYPORT")));
            ServerRMI rmiServer = new ServerRMI(pingNumber, config);
            System.setProperty("java.rmi.server.hostname", config.getProperty("REGISTRYIP"));
            regis.rebind(config.getProperty("LOOKUP"), rmiServer);
            System.out.println("Servidor primário iniciado...");
            // Inicia a thread principal
        } catch (RemoteException re){
            System.out.println("Servidor secundário iniciado...");
            // Incia a thread secondary
            failover(pingNumber, config);
        }
    }

    /**
     * Verifica a conexão e abre o secundário quando o primário vai abaixo
     * @param pingNumber
     * @param config
     * @throws RemoteException
     */

    private static void failover(int pingNumber, Properties config) throws RemoteException {

        // Verifica de 2 em 2 segundo se o server foi abaixo. Ao 5º ping sem resposta, ativa o server secundário.

        Thread secondary = new Thread(() -> {
            int noResponse = 0;
            DatagramPacket packet;
            String message;
            byte[] buffer;
            int port = Integer.parseInt(config.getProperty("UDP_PORT"));

            DatagramSocket socket = null;
            try {
                socket = new DatagramSocket();
            } catch (SocketException e) {
                e.printStackTrace();
            }

            while (true) {

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException i) {
                    System.out.println("Interrompido");
                }

                if (noResponse == 5) {
                    try {
                        connection(pingNumber, config);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                }

                try {
                    message = "Tás a dormir?";
                    buffer = message.getBytes();
                    packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("localhost"), port); // config
                    socket.send(packet);
                } catch (Exception e) {
                    System.out.println("Exception: " + e);
                    break;
                }

                buffer = new byte[256];
                packet = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.setSoTimeout(2000);
                    socket.receive(packet);
                    noResponse = 0;
                    System.out.println("Connected");
                } catch (Exception e) {
                    noResponse++;
                    System.out.println("Disconnected");
                }
            }
        });
        secondary.start();
    }
}
