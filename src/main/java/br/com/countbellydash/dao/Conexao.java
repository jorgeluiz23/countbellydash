package br.com.countbellydash.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {

    // 🔗 URL CORRIGIDA: Deve começar com 'jdbc:postgresql://' e NÃO deve incluir USER/PASSWORD.
    // Host: db.vpijpxpuqtkzicdqwjax.supabase.co
    // Porta: 5432
    // Banco: postgres
    private static final String URL = "jdbc:postgresql://db.vpijpxpuqtkzicdqwjax.supabase.co:5432/postgres"; 
    
    // 🔑 CREDENCIAIS: Estas são passadas separadamente no método getConnection.
    private static final String USER = "postgres"; 
    private static final String PASSWORD = "SY5AShv5SQpD5Rdi"; 

    public static Connection getConnection() throws SQLException {
        System.out.println("Tentando conectar ao Supabase...");
        // O Java usa estas três variáveis juntas para autenticar.
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Erro ao encerrar a connection mano: " + e.getMessage());
            }
        }
    }
}