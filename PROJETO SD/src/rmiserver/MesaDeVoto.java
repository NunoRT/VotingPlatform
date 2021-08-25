package rmiserver;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Classe com dados da mesa de voto
 */
public class MesaDeVoto implements Serializable {

    private final String nome;
    private final String add;
    private boolean isActive;
    private final ArrayList<Election> listaEleicoes = new ArrayList<>();
    private final ArrayList<String> listaVotantes = new ArrayList<>();
    private int totalVotos;

    public MesaDeVoto(String nome, String add) {
        this.nome = nome;
        this.add = add;
        this.totalVotos = 0;
        isActive = false;
    }

    // Get's

    /**
     * Método que retorna o nome
     * @return
     */
    public String getNome() { return nome; }

    /**
     * Método que retorna os endereços, a porta, e a quantidade de terminais
     * @return
     */
    public String getAdd() { return add; }

    /**
     * Método que retorna a lista de eleições a que a mesa está associada
     * @return
     */
    public ArrayList<Election> getListaEleicoes() { return listaEleicoes; }

    /**
     * Método que retorna a lista de votantes na mesa
     * @return
     */
    public ArrayList<String> getListaVotantes() { return listaVotantes; }

    /**
     * Método que retorna o estado da mesa
     * @return
     */
    public boolean getIsActive() { return isActive; }

    /**
     * Método que retorna a quantidade de votos da mesa
     * @return
     */
    public int getTotalVotos() { return totalVotos; }

    // Set's

    /**
     * Método que altera o estado da mesa
     * @param active
     */
    public void setActive(boolean active) { isActive = active; }

    /**
     * Método que incrementa o número de votos da mesa
     * @param totalV
     */
    public void setTotalVotos(int totalV) { totalVotos = totalV; }

    // Outros métodos

    /**
     * Método que adiciona uma eleição à lista de eleições associadas
     * @param e
     */
    public void addElection(Election e) {
        if (!listaEleicoes.contains(e)) {
            listaEleicoes.add(e);
        }
    }

    /**
     * Método que adiciona um votante à lista de votantes
     * @param e
     */
    public void addVotante(String e) {
        if (!listaVotantes.contains(e)) {
            listaVotantes.add(e);
        }
    }

    /**
     * Método toString, auto-explicativo
     * @return
     */
    @Override
    public String toString() {
        return nome;
    }
}
