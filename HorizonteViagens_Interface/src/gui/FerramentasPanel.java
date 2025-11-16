package gui;

import database.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FerramentasPanel extends JPanel {

    private JButton spAtualizarStatusButton;
    private JButton spSincronizarReservaButton;

    public FerramentasPanel() {
        setLayout(new BorderLayout(10, 10));
        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createTitledBorder("Executar Procedimentos Armazenados (Stored Procedures)"));

        spAtualizarStatusButton = new JButton("SP: Atualizar Status de Pagamento");
        spSincronizarReservaButton = new JButton("SP: Sincronizar Status da Reserva");

        buttonPanel.add(spAtualizarStatusButton);
        buttonPanel.add(spSincronizarReservaButton);

        JTextArea infoArea = new JTextArea(
                "Use esta seção para executar operações complexas no banco de dados.\n\n" +
                        "1. Atualizar Status: \n" +
                        "   Força a mudança do status de uma reserva (ex: 'Pago', 'Pendente', 'Cancelado').\n" +
                        "   Isso irá disparar o Trigger de auditoria.\n\n" +
                        "2. Sincronizar Status:\n" +
                        "   Usa o cursor para ler todas as parcelas de uma reserva. \n" +
                        "   Se TODAS estiverem 'Pagas', atualiza a reserva principal para 'Pago'. \n" +
                        "   Se UMA estiver 'Pendente', atualiza para 'Pendente'."
        );
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setBackground(getBackground());
        infoArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buttonPanel, BorderLayout.NORTH);
        add(infoArea, BorderLayout.CENTER);

        //acoes
        spAtualizarStatusButton.addActionListener(e -> executarAtualizacaoStatus());
        spSincronizarReservaButton.addActionListener(e -> executarSincronizacaoReserva());
    }

    private void executarAtualizacaoStatus() {
        String idReserva = JOptionPane.showInputDialog(this, "Digite o ID da Reserva:", "Executar SP_Atualizar_Status_Pagamento", JOptionPane.QUESTION_MESSAGE);
        if (idReserva == null || idReserva.trim().isEmpty()) return;

        String novoStatus = JOptionPane.showInputDialog(this, "Digite o NOVO status (ex: Pago, Pendente, Cancelado):", "Executar SP_Atualizar_Status_Pagamento", JOptionPane.QUESTION_MESSAGE);
        if (novoStatus == null || novoStatus.trim().isEmpty()) return;

        String sql = "CALL SP_Atualizar_Status_Pagamento(?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(idReserva));
            pstmt.setString(2, novoStatus);

            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "Procedimento executado! Status da Reserva " + idReserva + " atualizado para '" + novoStatus + "'.",
                    "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao executar procedimento: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void executarSincronizacaoReserva() {
        String idReserva = JOptionPane.showInputDialog(this, "Digite o ID da Reserva a ser sincronizada:", "Executar SP_Sincronizar_Status_Reserva_Pelo_Pagamento", JOptionPane.QUESTION_MESSAGE);
        if (idReserva == null || idReserva.trim().isEmpty()) return;

        String sql = "CALL SP_Sincronizar_Status_Reserva_Pelo_Pagamento(?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(idReserva));

            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "Procedimento executado! O status da Reserva " + idReserva + " foi sincronizado com base em suas parcelas.",
                    "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao executar procedimento: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}