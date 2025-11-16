package gui;

import database.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FornecedorPanel extends JPanel {

    private JTextField cnpjField, nomeFantasiaField, emailField, telefoneField;
    private JButton addButton, updateButton, deleteButton, findButton;

    public FornecedorPanel() {
        setLayout(new BorderLayout(10, 10));

        //painel formulário
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        formPanel.add(new JLabel("CNPJ (apenas números):"));
        cnpjField = new JTextField();
        formPanel.add(cnpjField);

        formPanel.add(new JLabel("Nome Fantasia:"));
        nomeFantasiaField = new JTextField();
        formPanel.add(nomeFantasiaField);

        formPanel.add(new JLabel("E-mail:"));
        emailField = new JTextField();
        formPanel.add(emailField);

        formPanel.add(new JLabel("Telefone:"));
        telefoneField = new JTextField();
        formPanel.add(telefoneField);

        //painel botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        addButton = new JButton("Adicionar");
        updateButton = new JButton("Atualizar");
        deleteButton = new JButton("Deletar");
        findButton = new JButton("Buscar por CNPJ");

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(findButton);

        add(formPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);

        //acoes dos botoes
        addButton.addActionListener(e -> adicionarFornecedor());
        updateButton.addActionListener(e -> atualizarFornecedor());
        deleteButton.addActionListener(e -> deletarFornecedor());
        findButton.addActionListener(e -> buscarFornecedor());
    }

    private void adicionarFornecedor() {
        if (cnpjField.getText().trim().isEmpty() || nomeFantasiaField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Erro ao adicionar, preencha CNPJ e Nome Fantasia.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "INSERT INTO Fornecedor (cnpj, nome_fantasia, e_mail, telefone) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cnpjField.getText());
            pstmt.setString(2, nomeFantasiaField.getText());
            pstmt.setString(3, emailField.getText());
            pstmt.setString(4, telefoneField.getText());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Fornecedor adicionado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                limparCampos();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao adicionar fornecedor: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void atualizarFornecedor() {
        if (cnpjField.getText().trim().isEmpty() || nomeFantasiaField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar, preencha CNPJ e Nome Fantasia.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "UPDATE Fornecedor SET nome_fantasia = ?, e_mail = ?, telefone = ? WHERE cnpj = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nomeFantasiaField.getText());
            pstmt.setString(2, emailField.getText());
            pstmt.setString(3, telefoneField.getText());
            pstmt.setString(4, cnpjField.getText());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Fornecedor atualizado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Nenhum fornecedor encontrado com o CNPJ informado.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar fornecedor: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deletarFornecedor() {
        String sql = "DELETE FROM Fornecedor WHERE cnpj = ?";
        int confirm = JOptionPane.showConfirmDialog(this, "Tem certeza que deseja deletar este fornecedor?", "Confirmação", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, cnpjField.getText());

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this, "Fornecedor deletado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    limparCampos();
                } else {
                    JOptionPane.showMessageDialog(this, "Nenhum fornecedor encontrado com o CNPJ informado.", "Aviso", JOptionPane.WARNING_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao deletar fornecedor: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void buscarFornecedor() {
        String sql = "SELECT nome_fantasia, e_mail, telefone FROM Fornecedor WHERE cnpj = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cnpjField.getText());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                nomeFantasiaField.setText(rs.getString("nome_fantasia"));
                emailField.setText(rs.getString("e_mail"));
                telefoneField.setText(rs.getString("telefone"));
            } else {
                JOptionPane.showMessageDialog(this, "Nenhum fornecedor encontrado com o CNPJ informado.", "Aviso", JOptionPane.WARNING_MESSAGE);
                limparCamposParcialmente();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao buscar fornecedor: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limparCampos() {
        cnpjField.setText("");
        limparCamposParcialmente();
    }

    private void limparCamposParcialmente() {
        nomeFantasiaField.setText("");
        emailField.setText("");
        telefoneField.setText("");
    }
}