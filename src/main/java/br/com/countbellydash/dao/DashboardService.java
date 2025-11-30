package br.com.countbellydash.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter; 
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DashboardService {

    private static final Random random = new Random();
    // Padrão de formatação de data/hora usado na agenda
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // CORREÇÃO DA AGENDA: Classe interna atualizada com o campo 'status'
    public static class AgendaItem {
        public String nomeCliente;
        public String dataHora;
        public String clienteType;    
        public String status; // 💡 NOVO CAMPO para diferenciar Agendada de Histórico

        // Construtor atualizado da agenda (agora com status)
        public AgendaItem(String nomeCliente, String dataHora, String clienteType, String status) {
            this.nomeCliente = nomeCliente;
            this.dataHora = dataHora;
            this.clienteType = clienteType;
            this.status = status;
        }
    }


    // 1. MÉTODO PRINCIPAL: Puxa todas as métricas dos cartões (QUERY COMBINADA)
    public DashboardData fetchDashboardData() {
        // Inicializa com zero e status de erro
        DashboardData data = new DashboardData(0, 0, 0, 0, 0, 0, "ERRO: Desconectado");
        Connection conn = null;

        try {
            conn = Conexao.getConnection();
            
            // Define o status como conectado assim que a conexão for bem-sucedida
            data.setStatusConexao("Conectado ao Supabase (PostgreSQL)");
            
            // 💡 QUERY 1 COMBINADA: Contagens Principais + Segmentação (Total, Bot, Humano, PF/PJ)
            String queryCombined = "SELECT " +
                                 "COUNT(session_id) AS total, " +
                                 "SUM(CASE WHEN status = 'Bot' THEN 1 ELSE 0 END) AS aguardandoBot, " +
                                 "SUM(CASE WHEN status = 'Humano' THEN 1 ELSE 0 END) AS atendimentoHumano, " +
                                 "SUM(CASE WHEN client_type = 'PF' THEN 1 ELSE 0 END) AS clientesPF, " +
                                 "SUM(CASE WHEN client_type = 'PJ' THEN 1 ELSE 0 END) AS clientesPJ " +
                                 "FROM sessions;";
            
            try (PreparedStatement stmt = conn.prepareStatement(queryCombined);
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    data.setClientesAtendidosTotal(rs.getInt("total"));
                    data.setClientesAguardando(rs.getInt("aguardandoBot"));
                    data.setAtendimentoHumano(rs.getInt("atendimentoHumano"));
                    data.setClientesPF(rs.getInt("clientesPF"));
                    data.setClientesPJ(rs.getInt("clientesPJ"));
                }
            }

            // QUERY 2: Agendamentos (Mantida a contagem apenas de 'Agendada' para o card principal)
            String queryAppointments = "SELECT COUNT(appointment_id) AS agendadas FROM appointments WHERE status = 'Agendada';";
            
            try (PreparedStatement stmt = conn.prepareStatement(queryAppointments);
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    data.setReunioesAgendadas(rs.getInt("agendadas"));
                }
            }

        } catch (SQLException e) {
            // Tratamento de erro melhorado
            System.err.println("ERRO DE CONEXÃO ou SQL com o Supabase: " + e.getMessage());
            e.printStackTrace();
            data.setStatusConexao("ERRO CABULOSOOOO, A CONEXÃO COM O SUPA EMBAÇOU: " + e.getMessage());
        } finally {
            Conexao.closeConnection(conn);
        }
        
        return data;
    }

    // 2. MÉTODO PARA DETALHES DA AGENDA (PAINEL LATERAL) - AGORA INCLUI O HISTÓRICO
    public List<AgendaItem> fetchAgendaDetails() {
        List<AgendaItem> agenda = new ArrayList<>();
        Connection conn = null;
        
        // 💡 QUERY ALTERADA: Lista reuniões com status 'Agendada' OU 'Realizada' (histórico),
        //                     ordenadas da mais recente para a mais antiga (DESC).
        String query = "SELECT client_name, client_type, schedule_time, status FROM appointments " +
                       "WHERE status = 'Agendada' OR status = 'Realizada' " + 
                       "ORDER BY schedule_time DESC;"; 

        try {
            conn = Conexao.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    String name = rs.getString("client_name");
                    String type = rs.getString("client_type");
                    String currentStatus = rs.getString("status"); 
                    
                    // Formatação de data robusta
                    String time = rs.getTimestamp("schedule_time")
                                    .toLocalDateTime()
                                    .format(DATE_FORMATTER);
                    
                    // Cria o AgendaItem com o NOVO campo 'status'
                    agenda.add(new AgendaItem(name, time, type, currentStatus));    
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

    // MÉTODO PARA VOLUME DE MENSAGENS POR HORA - (MOCK de Dados)
    // OBS: Manter o foco na integração com o banco para substituir este método.
    public Map<String, Integer> fetchDailyMessageVolume() {
        Map<String, Integer> volume = new LinkedHashMap<>();
        String[] horas = {"09h", "10h", "11h", "12h", "13h", "14h", "15h", "16h", "17h", "18h"};
        int[] valores = {55, 62, 90, 45, 30, 70, 65, 85, 50, 40};
        
        for (int i = 0; i < horas.length; i++) {
            volume.put(horas[i], valores[i] + random.nextInt(15));
        }
        return volume;
    }
}