package rmiserver;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Classe com dados da eleição
 */
public class Election implements Serializable {

    private String titulo;
    private String descricao;
    private String dataInicio;
    private int horaInicio;
    private int minInicio;
    private String dataFim;
    private int horaFim;
    private int minFim;
    private final ArrayList<String> podeBotar;
    private final ArrayList<CandidateList> listaListas = new ArrayList<>();
    private final HashMap<String, String> historicoVotos = new HashMap<>();
    private int votosAbs = 0;
    private int votosBr = 0;
    private int votosNul = 0;
    private String winner;
    private boolean aDecorrer;

    public Election(String titulo, String descricao, String dataInicio, int horaInicio, int minInicio, String dataFim, int horaFim, int minFim, ArrayList<String> podeBotar) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.dataInicio = dataInicio;
        this.horaInicio = horaInicio;
        this.minInicio = minInicio;
        this.dataFim = dataFim;
        this.horaFim = horaFim;
        this.minFim = minFim;
        this.podeBotar = podeBotar;
        this.aDecorrer = false;
    }

    // Get's

    /**
     * Método que retorna o titulo
     * @return
     */
    public String getTitulo() { return titulo; }

    /**
     * Método que retorna a descrição
     * @return
     */
    public String getDescricao() { return descricao; }

    /**
     * Método que retorna a data de inicio
     * @return
     */
    public String getDataInicio() { return dataInicio; }

    /**
     * Método que retorna a hora de inicio
     * @return
     */
    public int getHoraInicio() { return horaInicio; }

    /**
     * Método que retorna o minuto de inicio
     * @return
     */
    public int getMinInicio() { return minInicio; }

    /**
     * Método que retorna a data de fim
     * @return
     */
    public String getDataFim() { return dataFim; }

    /**
     * Método que retorna a hora de fim
     * @return
     */
    public int getHoraFim() { return horaFim; }

    /**
     * Método que retorna o minuto de fim
     * @return
     */
    public int getMinFim() { return minFim; }

    /**
     * Método que retorna quem pode votar
     * @return
     */
    public ArrayList<String> getPodeBotar() { return podeBotar; }

    /**
     * Método que retorna as listas candidatas
     * @return
     */
    public ArrayList<CandidateList> getListaListas() { return listaListas; }

    /**
     * Método que retorna os votos absolutos
     * @return
     */
    public int getVotosAbs() { return votosAbs; }

    /**
     * Método que retorna os votos brancos
     * @return
     */
    public int getVotosBr() { return votosBr; }

    /**
     * Método que retorna os votos nulos
     * @return
     */
    public int getVotosNul() { return votosNul; }

    /**
     * Método que retorna o vencedor da eleição
     * @return
     */
    public String getWinner() { return winner; }

    /**
     * Método que retorna true se a eleição está a decorrer, false caso contrário
     * @return
     */
    public boolean getADecorrer() { return aDecorrer; }

    /**
     * Método que retorna o histórico dos votos na eleição
     * @return
     */
    public HashMap<String, String> getHistoricoVotos() { return historicoVotos; }

    // Set's

    /**
     * Método que troca o titulo
     * @param titulo
     */
    public void setTitulo(String titulo) { this.titulo = titulo; }

    /**
     * Método que troca a descrição
     * @param desc
     */
    public void setDescricao(String desc) { this.descricao = desc; }

    /**
     * Método que incrementa 1 aos votos absolutos
     */
    public void setVotosAbs() { this.votosAbs += 1; }

    /**
     * Método que incrementa 1 aos votos brancos
     */
    public void setVotosBr() { this.votosBr += 1; }

    /**
     * Método que incrementa 1 aos votos nulos
     */
    public void setVotosNul() { this.votosNul += 1; }

    /**
     * Método que troca a data de inicio
     * @param dataInicio
     */
    public void setDataInicio(String dataInicio) { this.dataInicio = dataInicio; }

    /**
     * Método que troca a hora de inicio
     * @param horaInicio
     */
    public void setHoraInicio(int horaInicio) { this.horaInicio = horaInicio; }

    /**
     * Método que troca o minuto de inicio
     * @param minInicio
     */
    public void setMinInicio(int minInicio) { this.minInicio = minInicio; }

    /**
     * Método que troca a data de fim
     * @param dataFim
     */
    public void setDataFim(String dataFim) { this.dataFim = dataFim; }

    /**
     * Método que troca a hora de fim
     * @param horaFim
     */
    public void setHoraFim(int horaFim) { this.horaFim = horaFim; }

    /**
     * Método que troca o minuto de fim
     * @param minFim
     */
    public void setMinFim(int minFim) { this.minFim = minFim; }

    /**
     * Método que guarda o vencedor da eleição
     * @param lista
     */
    public void setWinner(String lista) { this.winner = lista; }

    /**
     * Método que troca o estado da eleição
     * @param b
     */
    public void setADecorrer(boolean b) { this.aDecorrer = b; }

    // Outros métodos

    /**
     * Método que adiciona uma lista candidata à eleição
     * @param candidateList
     */
    public void addCandidateList(CandidateList candidateList) {
        if (!listaListas.contains(candidateList)) {
            listaListas.add(candidateList);
        }
    }

    /**
     * Método que adiciona uma entrada no histórico de votações
     * @param eleitor
     * @param mesa_instante
     */
    public void addHistoricoVoto(String eleitor, String mesa_instante) {
        historicoVotos.put(eleitor, mesa_instante);
    }

    /**
     * Método toString, auto-explicativo
     * @return
     */
    @Override
    public String toString() {
        return  titulo;
    }
}
