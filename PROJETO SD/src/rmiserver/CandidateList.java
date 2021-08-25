package rmiserver;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Classe com dados da lista candidata
 */
public class CandidateList implements Serializable {
    private final ArrayList<Elector> lista;
    private final ArrayList<String> restricao;
    private final String nome;
    private int votosAbs = 0;

    /**
     * Construtor da lista candidata
     * @param nome
     * @param restricao
     */
    public CandidateList(String nome, ArrayList<String> restricao){
        this.nome = nome;
        this.restricao = restricao;
        this.lista = new ArrayList<>();
    }

    // Get's

    /**
     * Método que retorna o nome da lista candidata
     * @return nome
     */
    public String getNome() { return nome; }

    /**
     * Método que retorna a lista de eleitores associados à lista candidata
     * @return lista
     */
    public ArrayList<Elector> getLista() { return lista; }

    /**
     * Método que retorna os votos na lista candidata
     * @return votosAbs
     */
    public int getVotosAbs() { return votosAbs; }

    /**
     * Método que retorna o tipo de eleitor que a lista candidata é composta
     * @return restricao
     */
    public ArrayList<String> getRestricao() { return restricao; }

    // Set's

    /**
     * Método que incrementa mais um voto na lista candidata
     */
    public void setVotosAbs() { this.votosAbs += 1; }

    // Outros métodos

    /**
     * Método que adiciona um eleitor à lista candidata
     * @param e (Eleitor)
     * @return true or false
     */
    public boolean addCandidato(Elector e){
        if (!lista.contains(e)) {
            lista.add(e);
            return true;
        }
        return false;
    }

    /**
     * Método que verifica se o eleitor se encontra na lista candidata
     * @param e (Eleitor)
     * @return true or false
     */
    public boolean candidateIsIn(Elector e) {
        if (lista.contains(e)) {
            return true;
        }
        return false;
    }

    /**
     * Método toString auto-explicativo
     * @return String com nome
     */
    @Override
    public String toString() {
        return nome;
    }
}
