package rmiserver;
import java.io.InputStream;
import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

/**
 * Classe Servidor multicast (mesa de voto)
 */
public class MulticastServer extends UnicastRemoteObject implements MulticastMethods {
    private Properties config;
    private String nome1;
    private Scanner sc = new Scanner(System.in);
    private String MULTICAST_ADRESS_LISTENER;
    private String MULTICAST_ADRESS_SENDER;
    private int PORT;
    private Runnable listener;
    private final ArrayList<Election> listaEleicoes = new ArrayList<>();
    private Elector eleitorQueVota;
    private Election eleicaoAVotar;
    private ServerMethods electionsUC;
    private Calendar calendar;
    private int totalVotos = 0;
    private String getVoted = null;

    private String MENUMULTICAST = ("     __  __                       _       __      __   _        \n" +
                                    "    |  \\/  |                     | |      \\ \\    / /  | |       \n" +
                                    "    | \\  / | ___  ___  __ _    __| | ___   \\ \\  / /__ | |_ ___  \n" +
                                    "    | |\\/| |/ _ \\/ __|/ _` |  / _` |/ _ \\   \\ \\/ / _ \\| __/ _ \\ \n" +
                                    "    | |  | |  __/\\__ \\ (_| | | (_| |  __/    \\  / (_) | || (_) |\n" +
                                    "    |_|  |_|\\___||___/\\__,_|  \\__,_|\\___|     \\/ \\___/ \\__\\___/ \n" +
                                    "                                                            \n" +
                                    "                                                            \n" +
                                    "\t\t--------  MENU MESA DE VOTO --------\n" +
                                    "\t\tEnviar voto                        1\n" +
                                    "\t\tSair                               2");
    private String MENUELEI????ES = ("\t\t--------  LISTA DE ELEI????ES --------\n");

    private MulticastServer(Properties config, ServerMethods electionsUC) throws RemoteException {
        this.config = config;
        this.electionsUC = electionsUC;
        chooseMesa(electionsUC);
    }

    /**
     * M??todo que busca as mesas de voto inativas na db e trata de ativar a escolhida.
     * Apenas quando se liga o Multicast Server ?? que a mesa escolhida ?? ativada.
     * Quando este se desliga, a mesma ?? desativada mas n??o removida pois pode voltar a ser ativada
     * se, por exemplo, se quisesse mudar a m??quina de local.
     * @param electionsUC
     * @throws RemoteException
     */

    public void chooseMesa(ServerMethods electionsUC) throws RemoteException {

        // Array que vai buscar as mesas de votos ?? db atrav??s do Server RMI

        ArrayList<MesaDeVoto> listaMesas = electionsUC.listMesaVoto();
        int i = 1;
        int inactive = 0;
        int escolha;

        // Se o array estiver vazio, ent??o n??o h?? mesas criadas

        if (listaMesas.isEmpty()) {
            System.out.println("\t\tNenhuma mesa criada, por favor crie uma primeiro...\n");
            System.exit(0);
        }

        // Verifica se h?? alguma mesa inativa para se poder ativar, sen??o n??o se pode fazer nada

        for (MesaDeVoto m: listaMesas) {
            if (!m.getIsActive()) {
                inactive = 1;
                break;
            }
        }

        if (inactive == 0) {
            System.out.println("\t\tTodas as mesas criadas j?? est??o ativas...\n");
            System.exit(0);
        }

        while (true) {
            System.out.println("\t\tQue mesa est?? a ativar?\n");
            for (MesaDeVoto m: listaMesas) {
                System.out.println("\t\t" + i + "          " + m.getNome());
                i++;
            }
            escolha = getIntKeyboard("\n\t\tEscolha:");
            try {
                clearScreen();

                if (listaMesas.get(escolha - 1).getIsActive()) {
                    System.out.println("\t\tEssa mesa j?? est?? ativa, tente outra vez...");
                }

                else {

                    // Verifica se a mesa escolhida tem elei????es associadas

                    if (!listaMesas.get(escolha - 1).getListaEleicoes().isEmpty()) {

                        // Como tem, ativa a mesa e muda o parametros da mesma

                        nome1 = listaMesas.get(escolha - 1).getNome();
                        electionsUC.ativaMesa(nome1);
                        String[] address = listaMesas.get(escolha - 1).getAdd().split(";");
                        MULTICAST_ADRESS_SENDER = address[0];
                        MULTICAST_ADRESS_LISTENER = address[1];
                        PORT = Integer.parseInt(address[2]);
                        listener = MulticastServer.this::listenFromVoteTerminals;
                        clearScreen();
                        System.out.println("\t\tAtivou a mesa " + nome1 + "\n");
                    }
                    else {
                        clearScreen();
                        System.out.println("\t\tMesa n??o est?? associada a elei????es, associe primeiro...\n");
                        System.exit(0);
                    }
                    return;
                }
            } catch (Exception e) {
                i = 1;
                clearScreen();
                System.out.println("\t\tOp????o errada, tente outra vez...\n");
            }
        }
    }

    /**
     * Prote????es de inteiros e est??tica do terminal
     * @param message
     * @return
     */

    public int getIntKeyboard(String message) {
        sc = new Scanner(System.in);
        System.out.print(message + "\n\t\t>>> ");
        while (!sc.hasNextInt())
        {
            sc.nextLine();
            System.out.print("\t\tDigite um n??mero inteiro...\n\t\t>>> ");
        }
        return sc.nextInt();
    }

    /**
     * M??todo que limpa o terminal
     */

    private static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /**
     * Menu principal da mesa de voto
     * @param electionsUC
     * @throws RemoteException
     */

    public void welcomePage(ServerMethods electionsUC) throws RemoteException {

        while (true) {

            int num = getIntKeyboard(MENUMULTICAST);

            switch (num) {
                case 1:

                    // AAutentica????o do eleitor

                    clearScreen();
                    int cc = getIntKeyboard("\t\tCart??o de cidad??o:");

                    // Verifica se eleitor existe na db, devolve null se n??o existir

                    eleitorQueVota = electionsUC.checkElector(cc);
                    if (eleitorQueVota != null) {
                        clearScreen();

                        // Ap??s autenticar mostra as elei????es que est??o disponiveis para votar

                        eleicaoAVotar = printEleicoes(electionsUC);
                        if (eleicaoAVotar == null) {
                            break;
                        }
                        sendMessage("request");
                    } else {
                        clearScreen();
                        System.out.println("\t\tEleitor n??o consta nos cadernos eleitorais...\n");
                    }
                    break;
                case 2:

                    // Sair e desativa a mesa

                    clearScreen();
                    System.out.println("\t\t" + electionsUC.desativaMesa(nome1) + "\n");
                    System.exit(0);
                    break;
                default:
                    clearScreen();
                    System.out.println("\t\tOp????o inv??lida...\n");

            }
        }
    }

    /**
     * M??todo que d?? lista as elei????es associadas ?? mesa de voto em qest??o.
     * Se a escolha for feita corretamente, a mesa de voto envia um request aos terminais de voto para
     * abrir um que esteja bloqueado.
     * @param electionsUC
     * @return
     * @throws RemoteException
     */

    public Election printEleicoes(ServerMethods electionsUC) throws RemoteException {

        // Array que vai buscar as elei????es que a mesa tem ao Server RMI

        int i = 1;
        int escolha;
        ArrayList<Election> elections = new ArrayList<>();

        // S?? vai buscar as que est??o a decorrer

        for (Election e: electionsUC.getEletionsOfMesaVoto(nome1)) {
            if (electionsUC.eleicaoADecorrer(e) == 1) {
               elections.add(e);
            }
        }

        if (elections.isEmpty()) {
            System.out.println("\t\tEsta mesa n??o tem elei????es a decorrer...\n");
            return null;
        }

        while (true) {
            System.out.println(MENUELEI????ES);

            for (Election e: elections) {
                System.out.println("\t\t" + i + "          " + e.getTitulo());
                i++;
            }

            escolha = getIntKeyboard("\n\t\tEscolha:");
            try {

                // Ap??s escolher a elei????o verifica se o tipo da elei????o ?? a mesma do eleitor

                if (elections.get(escolha - 1).getPodeBotar().contains(eleitorQueVota.getTipo())) {

                    // Verifica se o eleitor j?? votou nesta elei????o

                    if (!eleitorQueVota.getEleicoesVotadas().contains(elections.get(escolha - 1).getTitulo())) {
                        clearScreen();
                        System.out.println("\t\t[Aceda o terminal de voto]\n");
                        return elections.get(escolha - 1);
                    }
                    else {
                        clearScreen();
                        System.out.println("\t\tEleitor j?? votou nesta elei????o...\n");
                        return null;
                    }
                }
                else {
                    clearScreen();
                    System.out.println("\t\tEleitor eleg??vel para esta elei????o...\n");
                    return null;
                }
            } catch (Exception e) {
                clearScreen();
                System.out.println("\t\tOp????o errada, tente outra vez...\n");
            }
        }
    }

    /**
     * M??todo que est?? ?? escuta por mensagens dos clientes
     */

    public void listenFromVoteTerminals() {
        MulticastSocket socket = null;

        // Sempre que a mesa de voto ?? ativada, esta envia um "type|alive" para os terminais de voto para receber
        // os votos que n??o foram enviados caso a mesa de voto tenha ido abaixo

        sendMessage("type|alive");
        try {
            while (true) {
                socket = new MulticastSocket(PORT);
                InetAddress group = InetAddress.getByName(MULTICAST_ADRESS_LISTENER);
                socket.joinGroup(group);
                byte[] buffer = new byte[256];
                DatagramPacket received_packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(received_packet);
                String message = new String(received_packet.getData(), 0, received_packet.getLength());
                HandlerServer(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            assert socket != null;
            socket.close();
        }
    }

    /**
     * M??todo que trata de desconstruir a mensagem recebida e enviar a resposta correta ao cliente correto
     * Ter em aten????o que cada cliente tem um id pr??prio que envia na mensagem. Desta forma o server consegue
     * estabelecer contacto apenas com esse cliente, pois os outros ao comparar que o id da mensagem n??o ?? o mesmo
     * que o deles, v??o ignor??-la.
     * @param message
     * @throws RemoteException
     */

    public void HandlerServer(String message) throws RemoteException, InterruptedException, NotBoundException {
        String[] split = message.split("\\;");
        String messagetosend = "";
        String id;
        HashMap<String, String> protocolo = new HashMap<>();
        for (int i = 0; i < split.length; i++) {
            String[] split_aux = split[i].split("\\|");
            protocolo.put(split_aux[0], split_aux[1]);
        }
        switch (protocolo.get("type")) {
            case "answer":

                //Meter id pass e login e mandar username e pass

                id = protocolo.get("id");
                String username = eleitorQueVota.getName();
                String password = eleitorQueVota.getPassword();
                int cc = eleitorQueVota.getCC();
                messagetosend = "type|login;username|" + username + ";password|" + password + ";cc|" + cc + ";id|" + id + ";";
                sendMessage(messagetosend);
                break;

            case "status":

                //send item_list

                if(protocolo.get("logged").equals("on")){
                    int i = 0;
                    int count = eleicaoAVotar.getListaListas().size();
                    messagetosend = "type|item_list;item_count|" + count + ";";
                    id = protocolo.get("id");
                    for (CandidateList c: eleicaoAVotar.getListaListas()) {
                        messagetosend = messagetosend.concat("item_" + i + "_name|" + c.getNome() + ";");
                        i++;
                    }
                    messagetosend = messagetosend.concat("eleicao|" + eleicaoAVotar.getTitulo() + ";");
                    messagetosend = messagetosend.concat("id|" + id + ";");
                    sendMessage(messagetosend);
                }
                break;

            case "vote":
                String nomeEleicao;
                String vote;
                String user_name;
                int cidadao;
                id = protocolo.get("id");
                nomeEleicao = protocolo.get("eleicao");
                vote = protocolo.get("vote");
                user_name = protocolo.get("username");
                cidadao = Integer.parseInt(protocolo.get("cc"));
                doVote(nomeEleicao,vote,user_name, cidadao, electionsUC);
                messagetosend = "type|received;id|" + id + ";" ;
                sendMessage(messagetosend);
                break;
        }
    }

    /**
     * M??todo que envia as mensagens para o grupo dos clients
     * @param message
     */

    public void sendMessage(String message) {
        MulticastSocket socket;

        try {
            if(message.equals("request")){
                message ="type|request;";
            }
            socket = new MulticastSocket();
            byte[] buffer = message.getBytes();
            InetAddress group = InetAddress.getByName(MULTICAST_ADRESS_SENDER);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            socket.send(packet);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * M??todo que trata de enviar o voto para o Server RMI
     * @param nomeEleicao
     * @param voto
     * @param username
     * @param cc
     * @param electionsUC
     * @throws RemoteException
     */

    public void doVote(String nomeEleicao, String voto, String username, int cc, ServerMethods electionsUC) throws RemoteException, NotBoundException {

        totalVotos++;
        calendar = Calendar.getInstance();
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);
        String instante = hours + ":" + minutes + ":" + seconds;

        try {
            electionsUC.doVoteConfirmation(nomeEleicao, voto, username, cc, nome1, totalVotos, instante);
        } catch (Exception e) {
            System.out.println("Algo correu mal...");
            return;
        }
    }

    public static void main(String[] args) throws RemoteException {
        clearScreen();
        int REPS = 5;
        String configFile = "configurations.properties";
        InputStream inputStream = MulticastServer.class.getClassLoader().getResourceAsStream(configFile);
        Properties config = new Properties();
        try {
            config.load(inputStream);
        } catch (Exception e) {
            System.out.println("\t\tN??o ?? possivel ler o ficheiro...");
            return;
        }

        ServerMethods electionsUC;
        MulticastServer multicast;

        // Caso o server RMI v?? abaixo, a mesa trata de reestabelecer a connex??o

        for (int i = 0; i < REPS; i++) {
            try {
                electionsUC = (ServerMethods) LocateRegistry.getRegistry(config.getProperty("REGISTRYIP"), Integer.parseInt(config.getProperty("REGISTRYPORT"))).lookup(config.getProperty("LOOKUP"));
                multicast = new MulticastServer(config, electionsUC);
                electionsUC.mesaConnected(multicast);
                new Thread(multicast.listener).start();
                multicast.welcomePage(electionsUC);
                return;

            } catch (Exception e){
                System.out.println("\t\tA tentar conex??o...");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie){
                    System.out.println("\t\tSleep interrompido");
                }
            }
        }
        System.out.println("\t\tServidor est?? offline");

    }
}