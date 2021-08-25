package rmiserver;
import java.io.*;
import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Properties;
import java.util.Scanner;
import java.util.UUID;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Classe Terminal de voto
 */
public class MulticastClient {
    private MulticastClientListener listener;
    private MulticastClientSender sender;
    private UUID id;

    /**
     * Construtor da classe que incia a thread de leitura de mesnagens vindas da mesa de voto
     * @param config
     * @param settings
     */

    MulticastClient(Properties config, String[] settings) {
        id = UUID.randomUUID();
        sender = new MulticastClientSender(config, settings[1], id, Integer.parseInt(settings[2]));
        listener = new MulticastClientListener(config, settings[0], sender, id, Integer.parseInt(settings[2]));
        listener.start();
    }

    /**
     * Método para limpar o terminal
     */

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void main(String[] args) {

        String configFile = "configurations.properties";
        InputStream inputStream = MulticastClient.class.getClassLoader().getResourceAsStream(configFile);
        Properties config = new Properties();
        try {
            config.load(inputStream);
        } catch (Exception e) {
            System.out.println("Cannot read config file");
            return;
        }

        try {
            String[] settings = config.getProperty(args[0]).split(";");

            if (Integer.parseInt(settings[3]) == 0) {
                System.out.println("Não há mais terminais disponiveis para ativar...\n");
                System.exit(0);
            } else {
                File fn = new File("configurations.properties");
                FileOutputStream f = new FileOutputStream(fn);

                try {
                    String value = settings[0] + ";" + settings[1] + ";" + settings[2] + ";" + (Integer.parseInt(settings[3]) - 1);
                    config.setProperty(args[0], value);
                    config.store(f, "");
                } finally {
                    f.close();
                }
            }

            new MulticastClient(config, settings);

        } catch (Exception e) {
            clearScreen();
            System.out.println("Nome da mesa incorreto...\n");
            System.exit(0);
        }
    }
}

class MulticastClientListener extends Thread {
    private final String MULTICAST_ADDRESS;
    private final int PORT;
    private final MulticastClientSender sender;
    private final UUID id;
    private Scanner keyboard;
    HashMap<String, String> user_and_pass;
    ArrayList<String> listas;
    private int sentVote;
    private String doVoteReturned;
    private final BufferedReader keyboardStrings = new BufferedReader(new InputStreamReader(System.in));
    private final String BEMVINDO = "\t\t   __      __   _   _             \n" +
                                    "\t\t   \\ \\    / /  | | (_)            \n" +
                                    "\t\t  __\\ \\  / /__ | |_ _ _ __   __ _ \n" +
                                    "\t\t / _ \\ \\/ / _ \\| __| | '_ \\ / _` |\n" +
                                    "\t\t|  __/\\  / (_) | |_| | | | | (_| |\n" +
                                    "\t\t \\___| \\/ \\___/ \\__|_|_| |_|\\__, |\n" +
                                    "\t\t                             __/ |\n" +
                                    "\t\t                            |___/ \n\n";

    private String ccEleitor;

    public MulticastClientListener(Properties config, String address,  MulticastClientSender sender, UUID id, int porta) {
        this.MULTICAST_ADDRESS = address;
        this.PORT = porta;
        this.sender = sender;
        this.id = id;
        this.sentVote = 0;
        user_and_pass = new HashMap<>();
        listas = new ArrayList<>();
    }

    /**
     * Proteção para receber um número inteiro
     * @param message
     * @return
     */

    private int getIntKeyboard(String message) {
        keyboard = new Scanner(System.in);
        System.out.print(message + "\n\t\t>>> ");
        while (!keyboard.hasNextInt())
        {
            keyboard.nextLine();
            System.out.print("\t\tDigite um número inteiro...\n\t\t>>> ");
        }
        return keyboard.nextInt();
    }

    /**
     * Proteção para receber uma string
     * @param message
     * @return
     */

    private String readInputMessages(String message){
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
     * Método de inicio da thread de leitura
     */

    public void run() {
        clearScreen();
        MulticastSocket socket = null;
        try {
            socket = new MulticastSocket(PORT);
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            socket.joinGroup(group);

            while (true) {

                byte[] buffer = new byte[256];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength());
                HandlerRequest(message);

            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }

    /**
     * Método que trata de filtrar as mensagens vindas da mesa de voto e responder
     * @param message
     * @throws InterruptedException
     */

    private void HandlerRequest(String message) throws InterruptedException {

        String messagetosend = "";
        UUID sameid;
        String id_aux;
        HashMap<String, String> protocolo = new HashMap<>();

        // HANDLER

        String[] split = message.split("\\;");

        for (String s : split) {
            String[] split_aux = s.split("\\|");
            protocolo.put(split_aux[0], split_aux[1]);
        }

        switch (protocolo.get("type")) {
            case "alive":
                sleep(1000);
                if (sentVote == 1) { // Tentativa de reenviar o voto à mesa caso não obtenha resposta
                    sender.sendMessage(doVoteReturned);
                    sentVote = 0;
                }
                break;

            case "request":
                messagetosend = "type|answer;id|" + id;
                sender.sendMessage(messagetosend);
                break;

            case "login":

                id_aux = protocolo.get("id");
                sameid = UUID.fromString(id_aux);
                if (!sameid.equals(id)){
                    return;
                }
                user_and_pass.put("username", protocolo.get("username"));
                ccEleitor = protocolo.get("cc");
                if (!doLogin(protocolo.get("username"), protocolo.get("password"))) {
                    return;
                }
                messagetosend = "type|status;logged|on;id|" + id.toString();
                sender.sendMessage(messagetosend);
                break;

            case "item_list":
                int num;
                String aux;
                num = Integer.parseInt(protocolo.get("item_count"));
                id_aux = protocolo.get("id");
                sameid = UUID.fromString(id_aux);
                if (!sameid.equals(id)) {
                    break;
                }
                listas.clear();
                for (int i = 0; i < num; i++) {
                    aux = "item_" + i + "_name";
                    listas.add(protocolo.get(aux));
                }
                doVoteReturned = doVote(listas, protocolo.get("eleicao"));
                sentVote = 1;
                break;

            case "received":
                id_aux = protocolo.get("id");
                sameid = UUID.fromString(id_aux);
                if (!sameid.equals(id)) {
                    break;
                }
                System.out.println("\t\tO seu voto foi enviado com sucesso, pode abandonar o local de voto");
                sentVote = 0;
                break;

            default:
                throw new IllegalStateException("\t\tUnexpected value: " + protocolo.get("type"));
        }
    }

    /**
     * Método que faz o login
     * @param username
     * @param password
     * @return
     */

    private boolean doLogin(String username, String password) {
        String messagetosend;
        String name, pass;
        System.out.println(BEMVINDO);
        while (true) {
            name = readInputMessages("\t\tNome de utilizador:");
            pass = readInputMessages("\t\tPassword:");
            if (name.equals(username) && pass.equals(password)) {
                messagetosend = "type|status;logged|on;id|" + id.toString();
                clearScreen();
                sender.sendMessage(messagetosend);
                return true;
            }
            else {
                clearScreen();
                System.out.println("\t\tNome de utilizador e password inválidos, tente outra vez....\n");
            }
        }
    }

    /**
     * Método que lista as possibilidades de voto para o eleitor votar
     * @param listas
     * @param nomeEleicao
     * @return
     */

    public String doVote(ArrayList<String> listas, String nomeEleicao) {
        int i = 1;
        listas.add("Branco");
        listas.add("Nulo");

        while(true) {
            System.out.println(BEMVINDO);
            System.out.println("\t\t------ Boletim de voto ------\n");
            for (String s: listas) {
                System.out.println("\t\t" + i + "            " + s);
                i++;
            }

            try {
                String voto = listas.get(getIntKeyboard("\n\t\tEscolha:") - 1);
                sender.sendMessage("type|vote;eleicao|" + nomeEleicao + ";vote|" + voto + ";username|" + user_and_pass.get("username") + ";cc|" + ccEleitor + ";id|" + id);
                clearScreen();
                return "type|vote;eleicao|" + nomeEleicao + ";vote|" + voto + ";username|" + user_and_pass.get("username") + ";cc|" + ccEleitor + ";id|" + id;
            } catch (Exception e) {
                clearScreen();
                i = 1;
                System.out.println("\t\tOpção errada, tente outra vez...\n");
            }
        }
    }

    /**
     * Método que limpa o terminal
     */

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}


class MulticastClientSender extends Thread {
    private final String MULTICAST_ADDRESS;
    private final int PORT;
    private UUID id;

    public MulticastClientSender(Properties config, String address, UUID id, int porta) {
        this.MULTICAST_ADDRESS = address;
        this.PORT = porta;
        this.id = id;
    }

    /**
     * Método que envia as mensagens
     * @param message
     */

    public void sendMessage(String message) {
        MulticastSocket socket = null;
        try {
            socket = new MulticastSocket();
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            socket.send(packet);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }
}
