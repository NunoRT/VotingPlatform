package rmiserver;
import java.io.*;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Classe Client RMI (Admin console)
 */
public class ClientRMI extends UnicastRemoteObject implements ClientMethods {

    static private final String ADMINMENU = "                 _           _          _____                      _      \n" +
            "        /\\      | |         (_)        / ____|                    | |     \n" +
            "       /  \\   __| |_ __ ___  _ _ __   | |     ___  _ __  ___  ___ | | ___ \n" +
            "      / /\\ \\ / _` | '_ ` _ \\| | '_ \\  | |    / _ \\| '_ \\/ __|/ _ \\| |/ _ \\\n" +
            "     / ____ \\ (_| | | | | | | | | | | | |___| (_) | | | \\__ \\ (_) | |  __/\n" +
            "    /_/    \\_\\__,_|_| |_| |_|_|_| |_|  \\_____\\___/|_| |_|___/\\___/|_|\\___|\n" +
            "                                                                          \n" +
            "                                                                          \n" +
            "\t\t------------------- MENU -------------------\n\n" +
            "\t\tRegistar pessoas                           1\n" +
            "\t\tCriar eleição                              2\n" +
            "\t\tGerir listas de candidatos a uma eleição   3\n" +
            "\t\tGerir mesas de voto                        4\n" +
            "\t\tAlterar propriedades de uma eleição        5\n" +
            "\t\tSaber em que local votou cada eleitor      6\n" +
            "\t\tMostrar o estado das mesas de voto         7\n" +
            "\t\tConsultar eleições passadas                8\n" +
            "\t\tConsultar lista de eleições                9\n" +
            "\t\tConsultar lista de eleitores               10\n" +
            "\t\tSair                                       11";

    static private final String MESASMENU = "                 _           _          _____                      _      \n" +
            "        /\\      | |         (_)        / ____|                    | |     \n" +
            "       /  \\   __| |_ __ ___  _ _ __   | |     ___  _ __  ___  ___ | | ___ \n" +
            "      / /\\ \\ / _` | '_ ` _ \\| | '_ \\  | |    / _ \\| '_ \\/ __|/ _ \\| |/ _ \\\n" +
            "     / ____ \\ (_| | | | | | | | | | | | |___| (_) | | | \\__ \\ (_) | |  __/\n" +
            "    /_/    \\_\\__,_|_| |_| |_|_|_| |_|  \\_____\\___/|_| |_|___/\\___/|_|\\___|\n" +
            "                                                                          \n" +
            "                                                                          \n" +
            "\t\t---------------- MESAS VOTO ----------------\n\n" +
            "\t\tCriar mesas voto                           1\n" +
            "\t\tRemover mesa voto                          2\n" +
            "\t\tAssociar mesa voto a eleição               3\n" +
            "\t\tSair                                       4";

    static private final String ELEICAOMENU = "                 _           _          _____                      _      \n" +
            "        /\\      | |         (_)        / ____|                    | |     \n" +
            "       /  \\   __| |_ __ ___  _ _ __   | |     ___  _ __  ___  ___ | | ___ \n" +
            "      / /\\ \\ / _` | '_ ` _ \\| | '_ \\  | |    / _ \\| '_ \\/ __|/ _ \\| |/ _ \\\n" +
            "     / ____ \\ (_| | | | | | | | | | | | |___| (_) | | | \\__ \\ (_) | |  __/\n" +
            "    /_/    \\_\\__,_|_| |_| |_|_|_| |_|  \\_____\\___/|_| |_|___/\\___/|_|\\___|\n" +
            "                                                                          \n" +
            "                                                                          \n" +
            "\t\t----------------- ELEIÇÃO -----------------\n\n" +
            "\t\tAlterar título da eleição                  1\n" +
            "\t\tAlterar descrição da eleição               2\n" +
            "\t\tAlterar instante de início                 3\n" +
            "\t\tAlterar instante de fim                    4\n" +
            "\t\tSair                                       5";

    static private final String LISTASMENU = "                 _           _          _____                      _      \n" +
            "        /\\      | |         (_)        / ____|                    | |     \n" +
            "       /  \\   __| |_ __ ___  _ _ __   | |     ___  _ __  ___  ___ | | ___ \n" +
            "      / /\\ \\ / _` | '_ ` _ \\| | '_ \\  | |    / _ \\| '_ \\/ __|/ _ \\| |/ _ \\\n" +
            "     / ____ \\ (_| | | | | | | | | | | | |___| (_) | | | \\__ \\ (_) | |  __/\n" +
            "    /_/    \\_\\__,_|_| |_| |_|_|_| |_|  \\_____\\___/|_| |_|___/\\___/|_|\\___|\n" +
            "                                                                          \n" +
            "                                                                          \n" +
            "\t\t----------------- LISTAS -----------------\n\n" +
            "\t\tCriar uma lista candidata                 1\n" +
            "\t\tRemover uma lista candidata               2\n" +
            "\t\tSair                                      3";

    static private final String ADMINQUEMVOTA = "\t\tEstudantes         1\n" +
            "\t\tDocentes           2\n" +
            "\t\tFuncionários       3";
    private ServerMethods electionsUC;
    private Properties config;
    private Scanner keyboard;
    private int keyboardInput;
    private BufferedReader keyboardStrings = new BufferedReader(new InputStreamReader(System.in));
    private final String PHONE_REGEX = "^9([0-9]){8}$";

    protected ClientRMI(ServerMethods electionsUC, Properties config) throws RemoteException {
        super();
        this.config = config;
        this.electionsUC = electionsUC;
    }

    /**
     * Proteção para leitura de integer
     * @param message
     * @return keyboardInput
     */
    public int getIntKeyboard(String message) {
        keyboard = new Scanner(System.in);
        System.out.print(message + "\n\t\t>>> ");
        while (!keyboard.hasNextInt())
        {
            keyboard.nextLine();
            System.out.print("\t\tDigite um número inteiro...\n\t\t>>> ");
        }
        keyboardInput = keyboard.nextInt();
        return keyboardInput;
    }

    /**
     * Proteção para leitura de linha
     * @param message
     * @return input
     */
    public String readInputMessages(String message) {
        System.out.print(message+"\n\t\t>>> ");
        String input="";
        do {
            try {
                input = keyboardStrings.readLine();
                if (input.trim().length() == 0){
                    System.out.print("\t\tEscreva alguma coisa...\n\t\t>>> ");
                }
            } catch (IOException e) {
                System.out.print("\t\tErro a ler, tente outra vez...\n\t\t>>> ");
            }
        }while (input.trim().length() == 0);
        return input;
    }

    /**
     * Método estético para o uso do terminal
     * @throws IOException
     * @throws RemoteException
     */
    public void pressToContinue() throws IOException {
        System.out.println("\n\n\t\tPrima qualquer tecla para continuar...");
        System.in.read();
    }

    /**
     * Método invocado pelo Server RMI para notificar que ocorreu um voto
     * @param vote
     * @throws RemoteException
     */
    public void getVoteNotifying(String vote) throws RemoteException {
        System.out.println("\n\t\t" + vote);
    }

    /**
     * Formata uma hora dada por input
     * @param sms
     * @return
     */
    public String setInicialDate(String sms) {
        Date dataInicial;
        SimpleDateFormat sdt = new SimpleDateFormat("dd/MM/yyyy");
        String dataAtual = sdt.format(new Date());

        while(true) {
            try {

                String dateAsString = readInputMessages(sms);
                dataInicial = sdt.parse(dateAsString);

                if (sdt.parse(dataAtual).compareTo(dataInicial) <= 0){
                    return sdt.format(dataInicial);
                }
                else {
                    System.out.println("\t\tEsta data é inferior à data atual, tente outra vez...\n");
                }

            } catch (Exception e) {
                System.out.println("\t\tTipo de dados inválido, tente outra vez...\n");
            }
        }
    }

    /**
     * Método para dar input de uma data posterior a outra. Esta tem em conta que não pode ser inferior à primeira
     * @param initialDate
     * @param sms
     * @return
     */
    public String setFinalDate(String initialDate, String sms) {
        Date dataInput = null;
        SimpleDateFormat sdt = new SimpleDateFormat("dd/MM/yyyy");

        while(true) {
            try {
                String dateAsString = readInputMessages(sms);
                dataInput = sdt.parse(dateAsString);

                if(sdt.parse(initialDate).compareTo(dataInput) <= 0){
                    return sdt.format(dataInput);
                }
                else {
                    System.out.println("\t\tEsta data é inferior à data de inicio da eleição, tente outra vez...\n");
                }

            } catch (Exception e) {
                System.out.println("\t\tTipo de dados inválido, tente outra vez...\n");
            }
        }
    }

    /**
     * Proteção para tipos de eleitor
     * @param sms
     * @return
     */
    public String typeOfElector(String sms) {
        String type;

        while (true) {
            type = readInputMessages(sms);

            if (type.toLowerCase(Locale.ROOT).equals("estudante")) {
                return "Estudante";
            }
            else if (type.toLowerCase(Locale.ROOT).equals("docente")) {
                return "Docente";
            }
            else if (type.toLowerCase(Locale.ROOT).equals("funcionário")) {
                return "Funcionário";
            }
            else {
                System.out.println("\t\tTipo inválido, tente outra vez...\n");
            }
        }
    }

    /**
     * Método que regista um novo eleitor e envia para o Server RMI para criar o objeto e guardar na db
     * @param electionsUC
     * @throws RemoteException
     */
    public void newElector(ServerMethods electionsUC) throws RemoteException {

        String name;
        String password;
        String department;
        String telemovel;
        String morada;
        int cc;
        String validadeCC;
        String tipo;

        name = readInputMessages("\t\tNome: ");
        password = readInputMessages("\t\tPassword: ");
        department = readInputMessages("\t\tDepartment: ");

        while (true) {
            telemovel = readInputMessages("\t\tTelemóvel:");

            if (!telemovel.matches(PHONE_REGEX)) {
                System.out.println("\t\tNúmero de telemovel inválido, tente outra vez...");
            } else {
                break;
            }
        }

        morada = readInputMessages("\t\tMorada: ");

        while (true) {
            cc = getIntKeyboard("\t\tCartão de cidadão: ");

            if (electionsUC.checkElectorByCC(cc)) {
                System.out.println("\t\tJá existe um eleitor associado a este cartão de cidadão, tente outra vez...");
            }
            else {
                break;
            }
        }

        validadeCC = setInicialDate("\t\tValidade cc (dd/MM/yyyy): ");
        tipo = typeOfElector("\t\tTipo de Eleitor: ");
        clearScreen();
        System.out.println("\t\t" + electionsUC.registPeople(name, password, department, telemovel, morada, cc, validadeCC,tipo) + "\n");
    }

    /**
     * Método que retorna uma ArrayList que contêm as restrições para os tipos de eleitor
     * @param sms
     * @return
     */
    public ArrayList<String> getPodeBotar(String sms) {
        ArrayList<String> quemBota = new ArrayList<>();
        String mais;
        int aux;

        while(true) {
            aux = 1;
            System.out.println(sms);
            int num = getIntKeyboard(ADMINQUEMVOTA);

            if (num == 1) {
                if (!quemBota.contains("Estudante")) {
                    quemBota.add("Estudante");
                } else {
                    System.out.println("Já adicionado!");
                }
            }
            else if (num == 2) {
                if (!quemBota.contains("Docente")) {
                    quemBota.add("Docente");
                } else {
                    System.out.println("Já adicionado!");
                }
            }
            else if (num == 3) {
                if (!quemBota.contains("Funcionário")) {
                    quemBota.add("Funcionário");
                } else {
                    System.out.println("Já adicionado!");
                }
            }
            else {
                clearScreen();
                System.out.println("\t\tOpção errada, tente outra vez...\n");
                aux = 0;
            }

            if (aux == 1) {
                while (true) {
                    mais = readInputMessages("\t\tQuer adicionar mais? (s/n)");

                    if (mais.equals("n")) {
                        return quemBota;
                    }
                    else if (mais.equals("s")) {
                        clearScreen();
                        break;
                    }
                    else {
                        clearScreen();
                        System.out.println("\t\tOpção inválida, tente outra vez...\n");
                    }
                }
            }
        }
    }

    /**
     * Método que regista uma nova eleição e envia para o Server RMI para criar o objeto e guardar na db
     * @param electionsUC
     * @throws RemoteException
     */
    public void newElection(ServerMethods electionsUC) throws RemoteException {

        String titulo;
        String descricao;
        String dataInicio;
        int horaInicio;
        int minutoInicio;
        String dataFim;
        int horaFim;
        int minutoFim;
        ArrayList<String> quemBota;

        while (true) {
            titulo = readInputMessages("\t\tTitulo da Eleição:");
            if (electionsUC.checkElection(titulo)) {
                System.out.println("\t\tEleição já existente, tente outro nome...\n");
            }
            else {
                break;
            }
        }
        descricao = readInputMessages("\t\tBreve Descrição:");
        dataInicio = setInicialDate("\t\tData de início (dd/MM/yyyy):");

        while (true) {
            horaInicio = getIntKeyboard("\t\tHora de Inicio:");

            if (horaInicio < 0 || horaInicio > 24) {
                System.out.println("\t\tHora inválida, tente outra vez...\n");
            }
            else {
                break;
            }
        }

        while (true) {
            minutoInicio = getIntKeyboard("\t\tMinuto de Inicio:");

            if (minutoInicio < 0 || minutoInicio > 59) {
                System.out.println("\t\tMinuto inválido, tente outra vez...\n");
            }
            else {
                break;
            }
        }

        dataFim = setFinalDate(dataInicio, "\t\tData de fim (dd/MM/yyyy):");

        while (true) {
            horaFim = getIntKeyboard("\t\tHora de Fim:");

            if (horaFim < 0 || horaFim > 24) {
                System.out.println("\t\tHora inválida, tente outra vez...\n");
            }
            else {
                break;
            }
        }

        while (true) {
            minutoFim = getIntKeyboard("\t\tHora de Fim:");

            if (minutoFim < 0 || minutoFim > 59) {
                System.out.println("\t\tMinuto inválido, tente outra vez...\n");
            }
            else {
                break;
            }
        }
        clearScreen();
        quemBota = getPodeBotar("\t\tQuem pode votar?");
        clearScreen();
        System.out.println("\t\t" + electionsUC.registElection(titulo, descricao, dataInicio, horaInicio, minutoInicio, dataFim, horaFim, minutoFim, quemBota) + "\n");

    }

    /**
     * getElectors e getElection serve apenas para pedir a lista de eleitores e de eleições e dar print da mesma
     * @param electionsUC
     * @throws RemoteException
     */
    public void getElectors(ServerMethods electionsUC) throws RemoteException {
        ArrayList<Elector> totalEleitor = electionsUC.printEleitores();

        if (totalEleitor.isEmpty()) {
            System.out.println("\t\tNada a apresentar...");
            return;
        }

        for (Elector e: totalEleitor) {
            System.out.println(
                    "\t\tNome: " + e.getName() + "\n" +
                            "\t\tCartão de cidadão: " + e.getCC() + "\n" +
                            "\t\tDepartamento: " + e.getDepartment() + "\n" +
                            "\t\t[Estudante/docente/funcionário]: " + e.getTipo() + "\n\n"
            );
        }
    }

    /**
     * Método que vai buscar a lista de eleições e imprime os dados
     * @param electionsUC
     * @throws RemoteException
     */
    public void getElection(ServerMethods electionsUC) throws RemoteException {
        ArrayList<Election> totalEleicoes = electionsUC.printEleicoes();
        String output = "";

        if (totalEleicoes.isEmpty()) {
            System.out.println("\t\tNada a apresentar...");
            return;
        }

        for (Election e: totalEleicoes) {
            output = output.concat(
                    "\t\tTitulo: " + e.getTitulo() + "\n" +
                            "\t\tData Inicio: " + e.getDataInicio() + " Hora: " + e.getHoraInicio() + ":" + e.getMinInicio() + "\n" +
                            "\t\tData Fim: " + e.getDataFim() + " Hora: " + e.getHoraFim() + ":" + e.getMinFim() + "\n" +
                            "\t\tQuem pode votar: " + e.getPodeBotar() + "\n" +
                            "\t\tListas candidatas: " + e.getListaListas() + "\n" +
                            "\t\tVotos absolutos: " + e.getVotosAbs() + " / brancos: " + e.getVotosBr() + " / nulos: " + e.getVotosNul() + "\n"
            );

            if (electionsUC.eleicaoADecorrer(e) == 1) {
                output = output.concat("\t\tEstado da eleição: A decorrer\n\n");
            }
            else if (electionsUC.eleicaoADecorrer(e) == -1) {
                output = output.concat("\t\tEstado da eleição: Acabada\n\n");
            }
            else {
                output = output.concat("\t\tEstado da eleição: Por começar\n\n");
            }

            System.out.println(output);
        }
    }

    /**
     * Cria uma nova lista candidata a uma dada eleição tendo em atenção aos tipos de eleitores que esta pode ter,
     * envia para o Server RMI para criar o objeto e guardar na db
     * @param electionsUC
     * @throws RemoteException
     */
    public void newCandidateList(ServerMethods electionsUC) throws RemoteException {

        int encontrou;
        int cc;
        String escolha;
        ArrayList<Elector> listaEleitores = electionsUC.printEleitores();
        ArrayList<String> restricao;
        String tituloEleicao;
        String nome;
        CandidateList lista;

        while (true) {
            tituloEleicao = readInputMessages("\t\tTítulo da eleição:");

            if (!electionsUC.checkElection(tituloEleicao)) {
                clearScreen();
                System.out.println("\t\tELeição não existe, tente outra vez...\n");
            }
            else {
                clearScreen();
                break;
            }
        }

        while (true) {
            nome = readInputMessages("\t\tNome da lista candidata:");

            if (electionsUC.checkListaCandidata(tituloEleicao, nome)) {
                clearScreen();
                System.out.println("\t\tJá existe uma lista com este nome, tente outra vez...\n");
            }
            else {
                clearScreen();
                break;
            }
        }

        restricao = getPodeBotar("\t\tPor que tipos é composta?");
        clearScreen();

        lista = new CandidateList(nome, restricao);

        while (true) {
            encontrou = 0;
            cc = getIntKeyboard("\t\tDigite o número do cartão de cidadão dos elementos da lista:");

            for(Elector e: listaEleitores) {
                if (e.getCC() == cc) {

                    if (electionsUC.checkCandidate(tituloEleicao, cc)) {
                        encontrou = 3;
                        break;
                    }
                    if (!lista.getRestricao().contains(e.getTipo())) {
                        encontrou = 2;
                    }
                    else {
                        if (!lista.addCandidato(e)) {
                            encontrou = 3;
                        }
                        else {
                            encontrou = 1;
                        }
                    }
                    break;
                }
            }

            if (encontrou == 1) {
                while (true) {
                    escolha = readInputMessages("\t\tQuer adicionar mais elementos? (s/n)");

                    if (escolha.equals("n")) {
                        clearScreen();
                        System.out.println("\t\t" + electionsUC.createCandidateList(tituloEleicao, lista) + "\n");
                        return;
                    }
                    else if (escolha.equals("s")) {
                        clearScreen();
                        break;
                    }
                    else {
                        System.out.println("\t\tOpção inválida, tente outra vez...\n");
                    }
                }
            }
            else if (encontrou == 2) {
                clearScreen();
                System.out.println("\t\tTipo de eleitor inválido para esta lista, tente outra vez...\n");

            }
            else if (encontrou == 3) {
                clearScreen();
                System.out.println("\t\tEleitor já adicionado noutra lista, tente outra vez...\n");

            }
            else {
                clearScreen();
                System.out.println("\t\tNome inválido, tente outra vez...\n");
            }
        }
    }

    /**
     * Método que remove a lista candidata associada à eleição dada
     * @param electionsUC
     * @throws RemoteException
     */
    public void removeCandidateList(ServerMethods electionsUC) throws RemoteException {
        String tituloEleicao;
        String nome;

        while (true) {
            tituloEleicao = readInputMessages("\t\tTítulo da eleição:");

            if (!electionsUC.checkElection(tituloEleicao)) {
                clearScreen();
                System.out.println("\t\tELeição não existe, tente outra vez...\n");
            }
            else {
                clearScreen();
                break;
            }
        }

        while (true) {
            nome = readInputMessages("\t\tNome da lista candidata:");

            if (!electionsUC.checkListaCandidata(tituloEleicao, nome)) {
                clearScreen();
                System.out.println("\t\tNão existe nenhuma lista com esse nome, tente outra vez...\n");
            }
            else {
                clearScreen();
                break;
            }
        }

        System.out.println("\t\t" + electionsUC.removeCandidateList(tituloEleicao, nome));
    }

    /**
     * Método que cria uma mesa e envia para o Server RMI para criar o objeto e guardar na db.
     * O Server RMI verifica se já existe uma mesa com o mesmo nome. A mesa criada para estar inativa porque
     * ainda não foi ligada através do Multicast Server.
     * O nome e endereço da mesa é enviado para o ficheiro Properties
     * @param config
     * @throws IOException
     */
    public void CriaMesa(Properties config) throws IOException {
        File fn = new File("configurations.properties");
        FileOutputStream f = new FileOutputStream(fn);
        String nome;
        String end;

        try {

            while (true) {
                nome = readInputMessages("\t\tNome da Mesa: ");
                if (electionsUC.checkMesa(nome)) {
                    clearScreen();
                    System.out.println("\t\tJá existe uma mesa com esse nome, tente outra vez...\n");
                }
                else {
                    clearScreen();
                    break;
                }
            }

            while (true) {
                String address1 = readInputMessages("\t\t1º Endereço da Mesa (224.0.0.0 to 239.255.255.255): ");

                if (electionsUC.checkEndereco(address1)) {
                    clearScreen();
                    System.out.println("\t\tEndereço já existente, tente outra vez...\n");
                }
                else {
                    clearScreen();
                    end = address1;
                    break;
                }
            }

            while (true) {
                String address2 = readInputMessages("\t\t2º Endereço da Mesa (224.0.0.0 to 239.255.255.255): ");

                if (electionsUC.checkEndereco(address2) || end.equals(address2)) {
                    clearScreen();
                    System.out.println("\t\tEndereço já existente, tente outra vez...\n");
                }
                else {
                    clearScreen();
                    end = end.concat(";" + address2);
                    break;
                }
            }

            while (true) {
                int port = getIntKeyboard("\t\tIndique a porta:");

                if (electionsUC.checkPorta(port)) {
                    clearScreen();
                    System.out.println("\t\tPorta já associada a outra mesa, tente outra vez...\n");
                }
                else {
                    clearScreen();
                    end = end.concat(";" + port);
                    break;
                }
            }

            end = end.concat(";" + getIntKeyboard("\t\tQuantos terminais de voto para ativar?"));
            config.setProperty(nome,end);
            config.store(f, "");
            clearScreen();
            System.out.println(electionsUC.addMesa(nome, end));
        } finally {
            f.close();
        }
    }

    /**
     * Método que remove a mesa em questão, se esta existir.
     * O nome e endereço da mesa é removido para o ficheiro Properties.
     * @param config
     * @throws IOException
     */
    public void removeMesa(Properties config) throws IOException {
        File fn = new File("configurations.properties");
        FileOutputStream f = new FileOutputStream(fn);
        String nome;

        try {

            while (true) {
                nome = readInputMessages("\t\tNome da Mesa: ");
                if (!electionsUC.checkMesa(nome)) {
                    clearScreen();
                    System.out.println("\t\tNão existe nenhuma mesa com esse nome, tente outra vez...\n");
                }
                else {
                    break;
                }
            }

            config.remove(nome);
            config.store(f, "");
            clearScreen();
            System.out.println("\t\t" + electionsUC.removeMesaVoto(nome) + "\n");
        } finally {
            f.close();
        }
    }

    /**
     * Método que associa uma mesa já criada previamente a uma eleição.
     * @param electionsUC
     * @throws RemoteException
     */
    public void associaMesa(ServerMethods electionsUC) throws RemoteException {
        String nomeMesa;
        String tituloEleicao;

        while (true) {
            nomeMesa = readInputMessages("\t\tNome da Mesa: ");
            if (!electionsUC.checkMesa(nomeMesa)) {
                clearScreen();
                System.out.println("\t\tNão existe nenhuma mesa com esse nome, tente outra vez...\n");
            }
            else {
                clearScreen();
                break;
            }
        }

        while (true) {
            tituloEleicao = readInputMessages("\t\tTítulo da eleição:");

            if (!electionsUC.checkElection(tituloEleicao)) {
                clearScreen();
                System.out.println("\t\tELeição não existe, tente outra vez...\n");
            }
            else {
                break;
            }
        }
        clearScreen();
        System.out.println("\t\t" + electionsUC.associaMesa(nomeMesa, tituloEleicao) + "\n");
    }

    /**
     * Método que lista as mesas de voto e as suas propriedades
     * @param electionsUC
     * @throws RemoteException
     */
    public void OndeVotouCadaELeitor(ServerMethods electionsUC) throws RemoteException {
        String eleicao = readInputMessages("\t\tTítulo da eleição:");
        int passada = 0;
        Election e;
        e = electionsUC.getElection(eleicao);

        if (e == null) {

            for (Election el: electionsUC.getEleicoesPassadas()) {
                if (eleicao.equals(el.getTitulo())) {
                    e = el;
                    passada = 1;
                }
            }

            if (passada == 0) {
                clearScreen();
                System.out.println("\t\tNenhuma eleição com esse nome...\n");
                return;
            }
        }

        clearScreen();

        if (e.getHistoricoVotos().isEmpty()) {
            System.out.println("\t\tHistórico vazio...\n");
            return;
        }

        System.out.println("\t\t------------------- HISTÓRICO -------------------\n");

        for (Map.Entry<String,String> entry : e.getHistoricoVotos().entrySet()) {
            String nome = entry.getKey();
            String[] mesa_instante = entry.getValue().split(";");
            System.out.println("\t\t" + nome + " - votou na mesa " + mesa_instante[0] + " - no instante " + mesa_instante[1]);
        }

        System.out.println();
    }

    /**
     * Método que mostra o estado das mesas de voto (se estão ativas e propriedades)
     * @param electionsUC
     * @throws RemoteException
     */
    public void MostraEstadoMesas(ServerMethods electionsUC) throws RemoteException {
        int i = 1;

        if (electionsUC.listMesaVoto().isEmpty()) {
            System.out.println("\t\t[Nenhuma mesa de voto criada...]\n");
            return;
        }

        for (MesaDeVoto m: electionsUC.listMesaVoto()) {
            String[] address = m.getAdd().split(";");
            System.out.println("\t\tMesa de voto - " + i);
            i++;
            System.out.println("\t\t" + m.getNome() + " - " + address[0] + " - " + address[1]);
            System.out.println("\t\tPorta: " + address[2]);
            System.out.println("\t\tTerminais criados: " + address[3]);
            if (m.getIsActive()) {
                System.out.println("\t\tA mesa está ativa? Sim");
            }
            else {
                System.out.println("\t\tA mesa está ativa? Não");
            }
            for (Election e: m.getListaEleicoes()) {
                System.out.println("\t\tEleição " + e.getTitulo() + ": " + m.getTotalVotos() + " votos");
            }
            System.out.println();
        }
    }


    /**
     * Método que lista as eleições passadas e as suas propriedades
     * @param electionsUC
     * @throws RemoteException
     */
    public void consultarPassadas(ServerMethods electionsUC) throws RemoteException {
        ArrayList<Election> passadas = electionsUC.getEleicoesPassadas();

        if (passadas.isEmpty()) {
            System.out.println("\t\tNada a apresentar...");
            return;
        }

        for (Election e: passadas) {
            System.out.println(
                    "\t\tTitulo: " + e.getTitulo() + "\n" +
                            "\t\tData Inicio: " + e.getDataInicio() + " Hora: " + e.getHoraInicio() + ":" + e.getMinInicio() + "\n" +
                            "\t\tData Fim: " + e.getDataFim() + " Hora: " + e.getHoraFim() + ":" + e.getMinFim() + "\n" +
                            "\t\tQuem pode votar: " + e.getPodeBotar() + "\n" +
                            "\t\tListas candidatas: " + e.getListaListas() + "\n" +
                            "\t\tResultados:\n" + getPercentagens(e) + "\n" +
                            "\t\tVencedora: " + e.getWinner() + "\n" +
                            "\t\tVotos absolutos: " + e.getVotosAbs() + "/ brancos: " + e.getVotosBr() + "/ nulos: " + e.getVotosNul() + "\n\n"
            );
        }
    }

    /**
     * Método que retorna as percentagens das eleições dadas
     * @param e
     * @return
     */
    public String getPercentagens(Election e) {
        String percentagens = "";

        for (CandidateList c: e.getListaListas()) {
            try {
                float absolutos = (float) c.getVotosAbs();
                float totalEleicao = (float) (e.getVotosAbs() + e.getVotosBr() + e.getVotosNul());
                float votos = ((absolutos/totalEleicao) * 100);
                percentagens = percentagens.concat("\t\tA lista " + c.getNome() + " teve " + (votos) + "% dos voto(s)\n");
            } catch (Exception ex) {
                percentagens = percentagens.concat("\t\tA lista " + c.getNome() + " teve " + "0% dos votos\n");
            }
        }

        return percentagens;
    }

    /**
     * Método que mostra o menu inicial
     * @param electionsUC
     * @param config
     * @throws IOException
     * @throws ParseException
     */
    public void welcomePage(ServerMethods electionsUC, Properties config) throws IOException, ParseException {

        while (true) {

            keyboardInput = getIntKeyboard(ADMINMENU);

            switch(keyboardInput) {
                case 1:

                    // REGISTAR PESSOAS
                    clearScreen();
                    newElector(electionsUC);
                    break;

                case 2:

                    // CRIAR ELEIÇÃO
                    clearScreen();
                    newElection(electionsUC);
                    break;

                case 3:

                    // GERIR LISTAS DE CANDIDATOS A UMA ELEICAO
                    clearScreen();
                    GerirListasSubmenu(electionsUC);
                    break;

                case 4:

                    // GERIR MESAS DE VOTO
                    clearScreen();
                    GerirMesasSubmenu(electionsUC, config);
                    break;

                case 5:
                    // ALTERAR PROPRIEDADES DE UMA ELEIÇÃO
                    clearScreen();
                    GerirPropriedadesSubmenu(electionsUC, config);
                    break;

                case 6:

                    // SABER EM QUE LOCAL VOTOU CADA ELEITOR
                    clearScreen();
                    OndeVotouCadaELeitor(electionsUC);
                    pressToContinue();
                    break;

                case 7:

                    // MOSTRAR O ESTADO DAS MESAS DE VOTO
                    clearScreen();
                    MostraEstadoMesas(electionsUC);
                    pressToContinue();
                    clearScreen();
                    break;

                case 8:

                    // CONSULTAR ELEIÇÕES PASSADAS
                    clearScreen();
                    consultarPassadas(electionsUC);
                    pressToContinue();
                    clearScreen();
                    break;

                case 9:

                    // LISTA DE ELEIÇÕES
                    clearScreen();
                    getElection(electionsUC);
                    pressToContinue();
                    clearScreen();
                    break;

                case 10:

                    // LISTA DE ELEITORES
                    clearScreen();
                    getElectors(electionsUC);
                    pressToContinue();
                    clearScreen();
                    break;

                case 11:

                    // SAIR
                    clearScreen();
                    System.out.println("\t\tAté à próxima eleição!\n");
                    System.exit(0);

                default:

                    clearScreen();
                    System.out.println("\t\tOpção inválida, tente outra vez...\n");
            }
        }
    }

    /**
     * Sub menu para gerir mesas de voto
     * @param electionsUC
     * @param config
     * @throws IOException
     */
    public void GerirMesasSubmenu(ServerMethods electionsUC, Properties config) throws IOException {
        while (true) {

            keyboardInput = getIntKeyboard(MESASMENU);

            switch(keyboardInput) {
                case 1:

                    // CRIA MESA
                    clearScreen();
                    CriaMesa(config);
                    break;

                case 2:

                    // REMOVE MESA
                    clearScreen();
                    removeMesa(config);
                    break;

                case 3:

                    // ASSOCIAR UMA MESA A UMA ELEIÇÃO
                    clearScreen();
                    associaMesa(electionsUC);
                    break;

                case 4:

                    // SAIR
                    clearScreen();
                    return;

                default:

                    clearScreen();
                    System.out.println("\t\tOpção errada, tente outra vez...\n");

            }
        }
    }

    /**
     * Sub menu de gestão de listas candidatas
     * @param electionsUC
     * @throws IOException
     */
    public void GerirListasSubmenu(ServerMethods electionsUC) throws IOException {
        while (true) {

            keyboardInput = getIntKeyboard(LISTASMENU);

            switch (keyboardInput) {
                case 1:

                    // CRIA LISTA
                    clearScreen();
                    newCandidateList(electionsUC);
                    break;

                case 2:

                    // REMOVE LISTA
                    clearScreen();
                    removeCandidateList(electionsUC);
                    break;

                case 3:
                    // SAIR
                    clearScreen();
                    return;

                default:

                    clearScreen();
                    System.out.println("\t\tOpção errada, tente outra vez...\n");
            }
        }
    }

    /**
     * Sub menu para gerir propriedades de uma eleição
     * @param electionsUC
     * @param config
     * @throws IOException
     * @throws ParseException
     */
    public void GerirPropriedadesSubmenu(ServerMethods electionsUC, Properties config) throws IOException, ParseException {
        String eleicao;

        while (true) {

            keyboardInput = getIntKeyboard(ELEICAOMENU);

            switch(keyboardInput) {
                case 1:

                    // ALTERAR TITULO
                    clearScreen();
                    String tituloAntigo;
                    String tituloNovo;

                    while (true) {
                        tituloAntigo = readInputMessages("\t\tTítulo da eleição antigo:");

                        if (!electionsUC.checkElection(tituloAntigo)) {
                            clearScreen();
                            System.out.println("\t\tELeição não existe, tente outra vez...\n");
                        }
                        else {
                            clearScreen();
                            break;
                        }
                    }

                    while (true) {
                        tituloNovo = readInputMessages("\t\tTítulo da eleição novo:");

                        if (tituloAntigo.equals(tituloNovo)) {
                            clearScreen();
                            System.out.println("\t\tO nome não pode ser igual ao antigo, tente outra vez...\n");
                        }
                        else {
                            clearScreen();
                            break;
                        }
                    }

                    clearScreen();
                    System.out.println("\t\t" + electionsUC.changeTitulo(tituloAntigo, tituloNovo) + "\n");
                    break;

                case 2:

                    // ALTERAR DESCRIÇÃO
                    clearScreen();

                    while (true) {
                        eleicao = readInputMessages("\t\tTítulo da eleição:");

                        if (!electionsUC.checkElection(eleicao)) {
                            clearScreen();
                            System.out.println("\t\tELeição não existe, tente outra vez...\n");
                        }
                        else {
                            clearScreen();
                            break;
                        }
                    }

                    String descricao = readInputMessages("\t\tNova descrição:");
                    clearScreen();
                    System.out.println("\t\t" + electionsUC.changeDescricao(eleicao, descricao) + "\n");
                    break;

                case 3:

                    // ALTERAR INSTANTE INICIO
                    clearScreen();
                    int horaInicio;
                    int minutoInicio;

                    while (true) {
                        eleicao = readInputMessages("\t\tTítulo da eleição:");

                        if (!electionsUC.checkElection(eleicao)) {
                            clearScreen();
                            System.out.println("\t\tELeição não existe, tente outra vez...\n");
                        }
                        else {
                            clearScreen();
                            break;
                        }
                    }

                    String dataInicio = setInicialDate("\t\tNova data de início");

                    while (true) {
                        horaInicio = getIntKeyboard("\t\tNova hora de inicio:");

                        if (horaInicio < 0 || horaInicio > 24) {
                            System.out.println("\t\tNova hora inválida, tente outra vez...\n");
                        }
                        else {
                            break;
                        }
                    }

                    while (true) {
                        minutoInicio = getIntKeyboard("\t\tNovo minuto de início:");

                        if (minutoInicio < 0 || minutoInicio > 59) {
                            System.out.println("\t\tNovo minuto inválido, tente outra vez...\n");
                        }
                        else {
                            break;
                        }
                    }
                    clearScreen();
                    System.out.println("\t\t" + electionsUC.changeInstanteInicio(eleicao, dataInicio, horaInicio, minutoInicio) + "\n");
                    break;

                case 4:

                    // ALTERAR INSTANTE FIM
                    clearScreen();
                    int horaFim;
                    int minutoFim;

                    while (true) {
                        eleicao = readInputMessages("\t\tTítulo da eleição:");

                        if (!electionsUC.checkElection(eleicao)) {
                            clearScreen();
                            System.out.println("\t\tELeição não existe, tente outra vez...\n");
                        }
                        else {
                            clearScreen();
                            break;
                        }
                    }

                    String dataFim = setInicialDate("\t\tNova data de fim");

                    while (true) {
                        horaFim = getIntKeyboard("\t\tNova hora de fim:");

                        if (horaFim < 0 || horaFim > 24) {
                            System.out.println("\t\tNova hora inválida, tente outra vez...\n");
                        }
                        else {
                            break;
                        }
                    }

                    while (true) {
                        minutoFim = getIntKeyboard("\t\tNovo minuto de fim:");

                        if (minutoFim < 0 || minutoFim > 59) {
                            System.out.println("\t\tNovo minuto inválido, tente outra vez...\n");
                        }
                        else {
                            break;
                        }
                    }
                    clearScreen();
                    System.out.println("\t\t" + electionsUC.changeInstanteFim(eleicao, dataFim, horaFim, minutoFim) + "\n");
                    break;

                case 5:

                    // SAIR
                    clearScreen();
                    return;

                default:

                    clearScreen();
                    System.out.println("\t\tOpção inválida, tente outra vez...\n");

            }
        }
    }

    /**
     * Método que limpa o terminal de voto
     */
    private static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void main(String[] args) {
        clearScreen();
        int REPS = 5;

        // Set policy
        //System.getProperties().put("java.security.policy", "policy.all");
        //System.setSecurityManager(new RMISecurityManager());

        String configFile = "configurations.properties";
        InputStream inputStream = ClientRMI.class.getClassLoader().getResourceAsStream(configFile);
        Properties config = new Properties();
        try {
            config.load(inputStream);
        } catch (Exception e) {
            System.out.println("\t\tNão é possivel ler o ficheiro...");
            return;
        }
        for (int i = 0; i < REPS; i++) {
            try {
                ServerMethods electionsUC = (ServerMethods) LocateRegistry.getRegistry(config.getProperty("REGISTRYIP"), Integer.parseInt(config.getProperty("REGISTRYPORT"))).lookup(config.getProperty("LOOKUP"));
                ClientRMI newClient = new ClientRMI(electionsUC, config);
                electionsUC.connected(newClient);
                newClient.welcomePage(electionsUC, config);
                return;
            } catch (Exception e) {
                System.out.println(e);
                System.out.println("\t\tA tentar conexão...");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    System.out.println("\t\tSleep interrompido");
                }
            }
        }
        System.out.println("\t\tServidor está offline");
    }
}
