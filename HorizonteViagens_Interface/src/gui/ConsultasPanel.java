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
    //botões
    private JButton query1Button, query2Button, query3Button, query4Button;
    //botões novos
    private JButton view1Button, view2Button, queryAntiJoinButton, queryFullJoinButton,
            querySub1Button, querySub2Button, triggerLogButton;

    public ConsultasPanel() {
        setLayout(new BorderLayout(10, 10));
        JPanel topButtonPanel = new JPanel(new GridLayout(0, 1, 10, 10));

        //painel de cconsultas
        JPanel originalQueriesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        originalQueriesPanel.setBorder(BorderFactory.createTitledBorder("Consultas"));
        query1Button = new JButton("Vendas por Funcionário (JOIN)");
        query2Button = new JButton("Top 5 Pacotes Caros (LIMIT)");
        query3Button = new JButton("Reservas Pendentes (WHERE)");
        query4Button = new JButton("Serviços por Fornecedor (JOIN)");
        originalQueriesPanel.add(query1Button);
        originalQueriesPanel.add(query2Button);
        originalQueriesPanel.add(query3Button);
        originalQueriesPanel.add(query4Button);

        //painel de views (comfiltro)
        JPanel viewsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        viewsPanel.setBorder(BorderFactory.createTitledBorder("Views (com Filtro)"));
        view1Button = new JButton("Relatório de Reservas (View)");
        view2Button = new JButton("Roteiro de Pacotes (View)");
        viewsPanel.add(view1Button);
        viewsPanel.add(view2Button);

        //painel de consultas
        JPanel advancedQueriesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        advancedQueriesPanel.setBorder(BorderFactory.createTitledBorder("Consultas Avançadas"));
        queryAntiJoinButton = new JButton("Clientes Sem Reserva (Anti-JOIN)");
        queryFullJoinButton = new JButton("Fornecedores e Serviços (Full-JOIN)");
        querySub1Button = new JButton("Reservas Acima da Média (Sub-Q)");
        querySub2Button = new JButton("Func. com +3 Vendas (Sub-Q)");
        advancedQueriesPanel.add(queryAntiJoinButton);
        advancedQueriesPanel.add(queryFullJoinButton);
        advancedQueriesPanel.add(querySub1Button);
        advancedQueriesPanel.add(querySub2Button);

        //painel dos triggers
        JPanel logPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        logPanel.setBorder(BorderFactory.createTitledBorder("Trigger"));
        triggerLogButton = new JButton("Ver Log de Mudança de Status");
        logPanel.add(triggerLogButton);

        topButtonPanel.add(originalQueriesPanel);
        topButtonPanel.add(viewsPanel);
        topButtonPanel.add(advancedQueriesPanel);
        topButtonPanel.add(logPanel);

        resultTextArea = new JTextArea("Selecione uma consulta para exibir os resultados.");
        resultTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultTextArea);

        add(topButtonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        //acoes dos botoes
        query1Button.addActionListener(e -> executeQuery(1, null));
        query2Button.addActionListener(e -> executeQuery(2, null));
        query3Button.addActionListener(e -> executeQuery(3, null));
        query4Button.addActionListener(e -> executeQuery(4, null));

        view1Button.addActionListener(e -> {
            String param = JOptionPane.showInputDialog(this, "Digite o CPF do cliente para filtrar (ou deixe em branco para ver todos):");
            executeQuery(5, param);
        });

        view2Button.addActionListener(e -> {
            String param = JOptionPane.showInputDialog(this, "Digite o ID do pacote para filtrar (ou deixe em branco para ver todos):");
            executeQuery(6, param);
        });

        queryAntiJoinButton.addActionListener(e -> executeQuery(7, null));
        queryFullJoinButton.addActionListener(e -> executeQuery(8, null));
        querySub1Button.addActionListener(e -> executeQuery(9, null));
        querySub2Button.addActionListener(e -> executeQuery(10, null));
        triggerLogButton.addActionListener(e -> executeQuery(11, null));
    }

    private void executeQuery(int queryNumber, String param) {
        String sql = "";
        String header = "";
        boolean hasParam = (param != null && !param.trim().isEmpty());

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
            case 5: //view 1 (V_Relatorio_Reservas)
                sql = "SELECT * FROM V_Relatorio_Reservas";
                if (hasParam) {
                    sql += " WHERE cpf_cliente = ?";
                }
                header = "Relatório de Reservas (View):\n-----------------------------------\n";
                break;
            case 6: //view 2 (V_Roteiro_Pacote_Detalhado)
                sql = "SELECT * FROM V_Roteiro_Pacote_Detalhado";
                if (hasParam) {
                    sql += " WHERE id_pacote = ?";
                }
                header = "Roteiro de Pacotes (View):\n-----------------------------------\n";
                break;
            case 7: //anti-Join
                sql = "SELECT c.nome, c.cpf FROM Cliente c LEFT JOIN Reserva r ON c.cpf = r.cliente_cpf WHERE r.id_reserva IS NULL;";
                header = "Clientes que Nunca Fizeram Reserva (Anti-Join):\n-----------------------------------\n";
                break;
            case 8: //full-outer-Join
                sql = "SELECT fo.nome_fantasia, s.descricao AS nome_servico FROM Fornecedor fo LEFT JOIN Servico s ON fo.cnpj = s.fornecedor_cnpj " +
                        "UNION " +
                        "SELECT fo.nome_fantasia, s.descricao AS nome_servico FROM Fornecedor fo RIGHT JOIN Servico s ON fo.cnpj = s.fornecedor_cnpj;";
                header = "Fornecedores e Seus Serviços (Full-Join):\n-----------------------------------\n";
                break;
            case 9: //subconsulta 1
                sql = "SELECT id_reserva, cliente_cpf, valor_total_reserva FROM Reserva " +
                        "WHERE valor_total_reserva > (SELECT AVG(valor_total_reserva) FROM Reserva);";
                header = "Reservas com Valor Acima da Média (Subconsulta):\n-----------------------------------\n";
                break;
            case 10: //subconsulta 2
                sql = "SELECT nome FROM Funcionario WHERE id_func IN ( " +
                        "SELECT id_func FROM Reserva GROUP BY id_func HAVING COUNT(id_reserva) >= 3);";
                header = "Funcionários com 3 ou Mais Vendas (Subconsulta):\n-----------------------------------\n";
                break;
            case 11: //Log do trigger
                sql = "SELECT * FROM LOG_Mudanca_Status_Reserva ORDER BY data_hora_mudanca DESC;";
                header = "Log de Auditoria de Status (Efeito do Trigger):\n-----------------------------------\n";
                break;
        }

        resultTextArea.setText(header);

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                resultTextArea.setText("Falha ao conectar ao banco de dados.");
                return;
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                if (hasParam) {
                    pstmt.setString(1, param);
                }
                try (ResultSet rs = pstmt.executeQuery()) {
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
                }
            }
        } catch (SQLException ex) {
            resultTextArea.setText("Erro ao executar a consulta SQL:\n" + ex.getMessage());
            ex.printStackTrace();
        }
    }
}