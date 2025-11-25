package br.com.countbellydash.dao;

// Esta classe conterá todos os dados a serem exibidos no dashboard.
public class DashboardData {

    private int clientesAtendidosTotal;
    private int clientesAguardando;
    private int clientesPF;
    private int clientesPJ;
    private int atendimentoHumano;
    private int reunioesAgendadas;
    private String statusConexao;

    // Construtor original (mantido para compatibilidade, mas menos usado com DAO)
    public DashboardData(int atendidosTotal, int aguardando, int pf, int pj, int humano, int agendadas, String status) {
        this.clientesAtendidosTotal = atendidosTotal;
        this.clientesAguardando = aguardando;
        this.clientesPF = pf;
        this.clientesPJ = pj;
        this.atendimentoHumano = humano;
        this.reunioesAgendadas = agendadas;
        this.statusConexao = status;
    }

    // --- GETTERS (Seus getters originais) ---
    public int getClientesAtendidosTotal() { return clientesAtendidosTotal; }
    public int getClientesAguardando() { return clientesAguardando; }
    public int getClientesPF() { return clientesPF; }
    public int getClientesPJ() { return clientesPJ; }
    public int getAtendimentoHumano() { return atendimentoHumano; }
    public int getReunioesAgendadas() { return reunioesAgendadas; }
    public String getStatusConexao() { return statusConexao; }

    // --- SETTERS (NOVOS: Essenciais para o DAO) ---
    public void setClientesAtendidosTotal(int clientesAtendidosTotal) {
        this.clientesAtendidosTotal = clientesAtendidosTotal;
    }

    public void setClientesAguardando(int clientesAguardando) {
        this.clientesAguardando = clientesAguardando;
    }
    
    public void setClientesPF(int clientesPF) {
        this.clientesPF = clientesPF;
    }

    public void setClientesPJ(int clientesPJ) {
        this.clientesPJ = clientesPJ;
    }

    public void setAtendimentoHumano(int atendimentoHumano) {
        this.atendimentoHumano = atendimentoHumano;
    }

    public void setReunioesAgendadas(int reunioesAgendadas) {
        this.reunioesAgendadas = reunioesAgendadas;
    }
    
    public void setStatusConexao(String statusConexao) {
        this.statusConexao = statusConexao;
    }
}