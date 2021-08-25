package rmiserver;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Classe de dados do eleitor
 */
public class Elector implements Serializable {
    private String name;
    private String password;
    private String department;
    private int phoneNumber;
    private String morada;
    private int cc;
    private String validadeCC;
    private String tipo;
    private ArrayList<String> eleicoesVotadas = new ArrayList<>();

    public Elector(String name, String password, String department, int phoneNumber, String morada, int cc, String validadeCC, String tipo) {
        this.name = name;
        this.password = password;
        this.department = department;
        this.phoneNumber = phoneNumber;
        this.morada = morada;
        this.cc = cc;
        this.validadeCC = validadeCC;
        this.tipo = tipo;
    }

    // Get's

    /**
     * Método que retorna o nome do eleitor
     * @return
     */
    public String getName() { return this.name; }

    /**
     * Método que retorna a password do eleitor
     * @return
     */
    public String getPassword() {return this.password; }

    /**
     * Método que retorna o departamento do eleitor
     * @return
     */
    public String getDepartment() { return this.department; }

    /**
     * Método que retorna o número de telemóvel do eleitor
     * @return
     */
    public int getPhoneNumber() { return this.phoneNumber; }

    /**
     * Método que retorna a morada do eleitor
     * @return
     */
    public String getMorada() { return this.morada; }

    /**
     * Método que retorna o cc do eleitor
     * @return
     */
    public int getCC() { return this.cc; }

    /**
     * Método que retorna a validade do cc do eleitor
     * @return
     */
    public String getValidadeCC() { return this.validadeCC; }

    /**
     * Método que retorna o tipo do eleitor
     * @return
     */
    public String getTipo() {return this.tipo; }

    /**
     * Método que retorna a lista de eleições em que o eleitor votou
     * @return
     */
    public ArrayList<String> getEleicoesVotadas() { return this.eleicoesVotadas; }

    // Outros métodos

    /**
     * Método que adiciona uma eleição em que o eleitor votou à lista de eleições votadas
     * @param titulo
     */
    public void addEleicaoVotada(String titulo) { eleicoesVotadas.add(titulo); }

    /**
     * Método toString, auto-explicativo
     * @return
     */
    @Override
    public String toString() {
        return name;
    }
}
