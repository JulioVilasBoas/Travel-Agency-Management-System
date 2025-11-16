package gui;

import database.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DestinoPanel extends JPanel {

    private JTextField idField, cidadeField, paisField;
    private JButton addButton, updateButton, deleteButton, findButton;

    public DestinoPanel() {
        setLayout(new BorderLayout(10, 10));

        //painel formulário
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        formPanel.add(new JLabel("ID Destino (preenchido ao buscar):"));
        idField = new JTextField();
        idField.setEditable(false);
        formPanel.add(idField);

        formPanel.add(new JLabel("Cidade:"));
        cidadeField = new JTextField();
        formPanel.add(cidadeField);

        formPanel.add(new JLabel("País:"));
        paisField = new JTextField();
        formPanel.add(paisField);

        //painel dos botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        addButton = new JButton("Adicionar");
        updateButton = new JButton("Atualizar");
        deleteButton = new JButton("Deletar");
        findButton = new JButton("Buscar por ID");

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(findButton);

        add(formPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);

        //acoes dos botoes
        addButton.addActionListener(e -> adicionarDestino());
        updateButton.addActionListener(e -> atualizarDestino());
        deleteButton.addActionListener(e -> deletarDestino());
        findButton.addActionListener(e -> buscarDestino());
    }

    private void adicionarDestino() {
        if (cidadeField.getText().trim().isEmpty() || paisField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Erro ao adicionar, preencha Cidade e País.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "INSERT INTO Destino (cidade, pais) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, cidadeField.getText());
            pstmt.setString(2, paisField.getText());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Destino adicionado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                limparCampos();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao adicionar destino: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void atualizarDestino() {
        if (idField.getText().trim().isEmpty() || cidadeField.getText().trim().isEmpty() || paisField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Busque um destino antes de atualizar.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "UPDATE Destino SET cidade = ?, pais = ? WHERE id_destino = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cidadeField.getText());
            pstmt.setString(2, paisField.getText());
            pstmt.setInt(3, Integer.parseInt(idField.getText()));

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Destino atualizado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Nenhum destino encontrado com o ID informado.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar destino: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deletarDestino() {
        String idParaDeletar = JOptionPane.showInputDialog(this, "Digite o ID do Destino a ser deletado:");

        if (idParaDeletar != null && !idParaDeletar.trim().isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(this, "Tem certeza que deseja deletar o Destino com ID " + idParaDeletar + "?", "Confirmação", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                String sql = "DELETE FROM Destino WHERE id_destino = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {

                    pstmt.setInt(1, Integer.parseInt(idParaDeletar));

                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        JOptionPane.showMessageDialog(this, "Destino deletado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                        limparCampos();
                    } else {
                        JOptionPane.showMessageDialog(this, "Nenhum destino encontrado com o ID informado.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erro ao deletar destino: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void buscarDestino() {
        String idParaBuscar = JOptionPane.showInputDialog(this, "Digite o ID do Destino a ser buscado:");
        if (idParaBuscar != null && !idParaBuscar.trim().isEmpty()) {
            String sql = "SELECT cidade, pais FROM Destino WHERE id_destino = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, Integer.parseInt(idParaBuscar));
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    idField.setText(idParaBuscar);
                    cidadeField.setText(rs.getString("cidade"));
                    paisField.setText(rs.getString("pais"));
                } else {
                    JOptionPane.showMessageDialog(this, "Nenhum destino encontrado com o ID informado.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    limparCampos();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao buscar destino: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void limparCampos() {
        idField.setText("");
        cidadeField.setText("");
        paisField.setText("");
    }
}