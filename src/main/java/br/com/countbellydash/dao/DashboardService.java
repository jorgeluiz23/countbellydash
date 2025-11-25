package br.com.countbellydash.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DashboardService {

    private static final Random random = new Random();

    // ----------------------------------------------------
    // INNER CLASS: Item da Agenda (CORRIGIDO)
    // ----------------------------------------------------
    public static class AgendaItem {
        public String nomeCliente;
        public String dataHora;
        public String clienteType; // <<<<< CORREÇÃO: NOVO CAMPO ADICIONADO

        // Construtor atualizado para aceitar o novo campo
        public AgendaItem(String nomeCliente, String dataHora, String clienteType) {
            this.nomeCliente = nomeCliente;
            this.dataHora = dataHora;
            this.clienteType = clienteType;
        }
    }
    
    // ----------------------------------------------------
    // 1. MÉTODO PRINCIPAL: Puxa todas as métricas dos cartões (Mantido)
    // ----------------------------------------------------
    public DashboardData fetchDashboardData() {
        DashboardData data = new DashboardData(0, 0, 0, 0, 0, 0, "ERRO: Desconectado");
        Connection conn = null;

        try {
            conn = Conexao.getConnection();
            
            // Define o status como conectado assim que a conexão for bem-sucedida
            data.setStatusConexao("Conectado ao Supabase (PostgreSQL)");
            
            // QUERY 1: Contagens Principais (Total, Bot, Humano)
            String queryMain = "SELECT COUNT(session_id) AS total, " +
                               "SUM(CASE WHEN status = 'Bot' THEN 1 ELSE 0 END) AS aguardandoBot, " +
                               "SUM(CASE WHEN status = 'Humano' THEN 1 ELSE 0 END) AS atendimentoHumano " +
                               "FROM sessions;";
            
            try (PreparedStatement stmt = conn.prepareStatement(queryMain);
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    data.setClientesAtendidosTotal(rs.getInt("total"));
                    data.setClientesAguardando(rs.getInt("aguardandoBot"));
                    data.setAtendimentoHumano(rs.getInt("atendimentoHumano"));
                }
            }

            // QUERY 2: Segmentação (PF/PJ)
            String querySegment = "SELECT " +
                                  "SUM(CASE WHEN client_type = 'PF' THEN 1 ELSE 0 END) AS clientesPF, " +
                                  "SUM(CASE WHEN client_type = 'PJ' THEN 1 ELSE 0 END) AS clientesPJ " +
                                  "FROM sessions;";
            
            try (PreparedStatement stmt = conn.prepareStatement(querySegment);
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    data.setClientesPF(rs.getInt("clientesPF"));
                    data.setClientesPJ(rs.getInt("clientesPJ"));
                }
            }
            
            // QUERY 3: Agendamentos (Mantida a contagem apenas de 'Agendada')
            String queryAppointments = "SELECT COUNT(appointment_id) AS agendadas FROM appointments WHERE status = 'Agendada';";
            
            try (PreparedStatement stmt = conn.prepareStatement(queryAppointments);
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    data.setReunioesAgendadas(rs.getInt("agendadas"));
                }
            }
// tentnado conectar no supabase pra incluir os dados
        } catch (SQLException e) {
            System.err.println("NÃO ACHEI ND NO SUPA: " + e.getMessage());
            e.printStackTrace();
            data.setStatusConexao("ERRO CABULOSOOOO, A CONEXÃO COM O SUPA EMBAÇOU: " + e.getMessage());
        } finally {
            Conexao.closeConnection(conn);
        }
        
        return data;
    }

    // ----------------------------------------------------
    // 2. MÉTODO PARA DETALHES DA AGENDA (PAINEL LATERAL) - ATUALIZADO
    // ----------------------------------------------------
    public List<AgendaItem> fetchAgendaDetails() {
        List<AgendaItem> agenda = new ArrayList<>();
        Connection conn = null;
        
        // QUERY 4: Lista TODAS as reuniões agendadas/ativas, ordenadas por data.
        // O Main.java fará a separação entre Passada e Futura.
        String query = "SELECT client_name, client_type, schedule_time FROM appointments " +
                       "WHERE status = 'Agendada' " + // Apenas as ativas
                       "ORDER BY schedule_time;";

        try {
            conn = Conexao.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    String name = rs.getString("client_name");
                    String type = rs.getString("client_type"); // Puxa o tipo de cliente
                    // Formatação da data para o formato yyyy-MM-dd HH:mm (esperado pelo Main.java)
                    String time = rs.getTimestamp("schedule_time").toString().substring(0, 16); 
                    
                    // Cria o AgendaItem com o NOVO campo 'type'
                    agenda.add(new AgendaItem(name, time, type)); 
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar detalhes da agenda: " + e.getMessage());
            return new ArrayList<>(); 
        } finally {
            Conexao.closeConnection(conn);
        }
        
        return agenda;
    }

    // ----------------------------------------------------
    // 3. MÉTODO PARA VOLUME DE MENSAGENS POR HORA (Mantido com MOCK)
    // ----------------------------------------------------
    public Map<String, Integer> fetchDailyMessageVolume() {
        Map<String, Integer> volume = new LinkedHashMap<>();
        String[] horas = {"09h", "10h", "11h", "12h", "13h", "14h", "15h", "16h", "17h", "18h"};
        int[] valores = {55, 62, 80, 45, 30, 70, 65, 85, 50, 40};
        
        for (int i = 0; i < horas.length; i++) {
            volume.put(horas[i], valores[i] + random.nextInt(15));
        }
        return volume;
    }
}