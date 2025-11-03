package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Dados do banco de dados
    private static final String URL = "jdbc:mysql://localhost:3306/mysql_database";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    public static Connection getConnection() {
        try {
            // Tenta carregar a classe do driver JDBC do mySQL
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Tenta obter a conexão usando o DriverManager
            System.out.println("Conectando ao banco de dados...");
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conexão bem-sucedida!");
            return connection;
        } catch (ClassNotFoundException e) {
            // Este erro acontece se o .jar do Connector/J não foi adicionado ao projeto
            System.err.println("Erro: Driver JDBC do MySQL não encontrado!");
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            // Este erro acontece se a URL, user ou password estiverem errados
            // ou se o servidor MySQL não estiver a rodar
            System.err.println("Erro ao conectar ao banco de dados: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
