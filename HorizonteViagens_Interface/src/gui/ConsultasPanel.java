package gui;

import database.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConsultasPanel extends JPanel {

    private JTextArea resultTextArea;
    private JButton query1Button, query2Button, query3Button, query4Button;

    public ConsultasPanel() {
        setLayout(new BorderLayout(10, 10));

        //painel dos botoes
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        query1Button = new JButton("Vendas por Funcionário");
        query2Button = new JButton("Pacotes Mais Caros");
        query3Button = new JButton("Reservas Pendentes");
        query4Button = new JButton("Serviços por Fornecedor");

        buttonPanel.add(query1Button);
        buttonPanel.add(query2Button);
        buttonPanel.add(query3Button);
        buttonPanel.add(query4Button);

        // Área de texto para resultados
        resultTextArea = new JTextArea("Selecione uma consulta para exibir os resultados.");
        resultTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultTextArea);

        add(buttonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        //acoes dos botoes
        query1Button.addActionListener(e -> executeQuery(1));
        query2Button.addActionListener(e -> executeQuery(2));
        query3Button.addActionListener(e -> executeQuery(3));
        query4Button.addActionListener(e -> executeQuery(4));
    }

    private void executeQuery(int queryNumber) {
        String sql = "";
        String header = "";

        switch (queryNumber) {
            case 1: //JOIN
                sql = "SELECT f.nome AS nome_funcionario, COUNT(r.id_reserva) AS total_reservas " +
                        "FROM Funcionario f " +
                        "JOIN Reserva r ON f.id_func = r.id_func " +
                        "GROUP BY f.nome " +
                        "ORDER BY total_reservas DESC;";
                header = "Total de Vendas por Funcionário:\n-----------------------------------\n";
                break;
            case 2:
                sql = "SELECT nome_pacote, preco_total FROM Pacote_viagem ORDER BY preco_total DESC LIMIT 5;";
                header = "Top 5 Pacotes Mais Caros:\n-----------------------------------\n";
                break;
            case 3:
                sql = "SELECT id_reserva, cliente_cpf, valor_total_reserva FROM Reserva WHERE status_pagamento = 'Pendente';";
                header = "Reservas com Pagamento Pendente:\n-----------------------------------\n";
                break;
            case 4: //JOIN 2
                sql = "SELECT fo.nome_fantasia, COUNT(s.id_servico) as quantidade_servicos " +
                        "FROM Fornecedor fo " +
                        "JOIN Servico s ON fo.cnpj = s.fornecedor_cnpj " +
                        "GROUP BY fo.nome_fantasia;";
                header = "Quantidade de Serviços por Fornecedor:\n-----------------------------------\n";
                break;
        }

        resultTextArea.setText(header);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (conn == null) {
                resultTextArea.setText("Falha ao conectar ao banco de dados.");
                return;
            }

            StringBuilder results = new StringBuilder(resultTextArea.getText());
            int columnCount = rs.getMetaData().getColumnCount();
            boolean hasResults = false;

            while (rs.next()) {
                hasResults = true;
                for (int i = 1; i <= columnCount; i++) {
                    results.append(rs.getMetaData().getColumnLabel(i)).append(": ")
                            .append(rs.getString(i)).append(" | ");
                }
                results.setLength(results.length() - 3);
                results.append("\n");
            }

            if (!hasResults) {
                results.append("Nenhum resultado encontrado.");
            }
            resultTextArea.setText(results.toString());

        } catch (SQLException ex) {
            resultTextArea.setText("Erro ao executar a consulta SQL:\n" + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
