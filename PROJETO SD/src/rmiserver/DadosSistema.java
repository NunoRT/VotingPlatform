package rmiserver;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Estrutura que guarda todos os dados do sistema
 */
public class DadosSistema implements Serializable {
    private ArrayList<Election> listaEleicoes = new ArrayList<>();
    private ArrayList<Elector> listaEleitores = new ArrayList<>();
    private ArrayList<MesaDeVoto> listaMesas = new ArrayList<>();
    private ArrayList<Election> listaEleicoesPassadas = new ArrayList<>();

    // Add's

    /**
     * Método que adiciona uma nova eleição à lista de eleições
     * @param e
     */
    public void addEleicao(Election e) { listaEleicoes.add(e); }

    /**
     * Método que adiciona eleitor à lista de eleitores
     * @param e
     */
    public void addEleitor(Elector e) { listaEleitores.add(e); }

    /**
     * Método que adiciona uma mesa à lista de mesas
     * @param e
     */
    public void addMesa(MesaDeVoto e) {listaMesas.add(e); }

    // Get's

    /**
     * Método que retorna a lista de eleitores
     * @return
     */
    public ArrayList<Elector> getListaEleitores() {
        return listaEleitores;
    }

    /**
     * Método que retorna a lista de eleições
     * @return
     */
    public ArrayList<Election> getListaEleicoes() {
        return listaEleicoes;
    }

    /**
     * Método que retorna a lista de mesas
     * @return
     */
    public ArrayList<MesaDeVoto> getListaMesas() {return listaMesas; }

    /**
     * Método que retorna a lista de eleições passadas
     * @return
     */
    public ArrayList<Election> getListaEleicoesPassadas() { return listaEleicoesPassadas; }

    // Outros métodos

    /**
     * Método que adiciona uma eleição à lista de eleições passadas
     * @param e
     */
    public void addListaEleicoesPassadas(Election e) {
        if (!listaEleicoesPassadas.contains(e)) {
            listaEleicoesPassadas.add(e);
        }
    }

    /**
     * Método que remove uma eleição da lista de eleições
     * @param e
     */
    public void removeEleicoes(Election e){
        try {
            if(listaEleicoes.contains(e)){
                listaEleicoes.remove(e);
            }
        } catch (Exception ex) {
            return;
        }
    }

    /**
     * Método que remove uma eleição específica de todas as mesas a que esteja associada
     * @param e
     */
    public void removeEleicaoFromMesas(Election e) {
        try {
            for (MesaDeVoto m: listaMesas) {
                if (m.getListaEleicoes().contains(e)) {
                    m.getListaEleicoes().remove(e);
                }
            }
        } catch (Exception ex) {
            return;
        }
    }
}
