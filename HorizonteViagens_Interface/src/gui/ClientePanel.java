package gui;

import com.mysql.cj.protocol.x.XMessage;
import database.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClientePanel extends JPanel {
    private JTextField cpfField, nomeField, ruaField, numeroField, cidadeField, categoriaField;
    private JButton addButton, updateButton, deleteButton, findButton;

    public ClientePanel() {
        setLayout(new BorderLayout(10, 10));

        //painel do formulário
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        formPanel.add(new JLabel("CPF (apenas números):"));
        cpfField = new JTextField();
        formPanel.add(cpfField);

        formPanel.add(new JLabel("Nome Completo:"));
        nomeField = new JTextField();
        formPanel.add(nomeField);

        formPanel.add(new JLabel("Rua:"));
        ruaField = new JTextField();
        formPanel.add(ruaField);

        formPanel.add(new JLabel("Número:"));
        numeroField = new JTextField();
        formPanel.add(numeroField);

        formPanel.add(new JLabel("Cidade:"));
        cidadeField = new JTextField();
        formPanel.add(cidadeField);
        formPanel.add(new JLabel("Categoria (via Função):"));
        categoriaField = new JTextField();
        categoriaField.setEditable(false);
        formPanel.add(categoriaField);

        //painel dos botoes
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        addButton = new JButton("Adicionar");
        updateButton = new JButton("Atualizar");
        deleteButton = new JButton("Deletar");
        findButton = new JButton("Buscar por CPF");

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(findButton);

        add(formPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);

        //acoes botoes
        addButton.addActionListener(e -> adicionarCliente());
        updateButton.addActionListener(e -> atualizarCliente());
        deleteButton.addActionListener(e -> deletarCliente());
        findButton.addActionListener(e -> buscarCliente());
    }

    private void adicionarCliente() {
        if (cpfField.getText().trim().isEmpty() ||
                nomeField.getText().trim().isEmpty() ||
                ruaField.getText().trim().isEmpty() ||
                numeroField.getText().trim().isEmpty() ||
                cidadeField.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(this, "Erro ao adicionar, preencha todos os campos.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);

            return;
        }

        String sql = "INSERT INTO Cliente (cpf, nome, rua, numero, cidade) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cpfField.getText());
            pstmt.setString(2, nomeField.getText());
            pstmt.setString(3, ruaField.getText());
            pstmt.setString(4, numeroField.getText());
            pstmt.setString(5, cidadeField.getText());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Cliente adicionado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                limparCampos();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao adicionar cliente: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void atualizarCliente() {
        if (cpfField.getText().trim().isEmpty() ||
                nomeField.getText().trim().isEmpty() ||
                ruaField.getText().trim().isEmpty() ||
                numeroField.getText().trim().isEmpty() ||
                cidadeField.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(this, "Erro ao adicionar, preencha todos os campos.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);

            return;
        }

        String sql = "UPDATE Cliente SET nome = ?, rua = ?, numero = ?, cidade = ? WHERE cpf = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nomeField.getText());
            pstmt.setString(2, ruaField.getText());
            pstmt.setString(3, numeroField.getText());
            pstmt.setString(4, cidadeField.getText());
            pstmt.setString(5, cpfField.getText());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Cliente atualizado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Nenhum cliente encontrado com o CPF informado.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar cliente: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deletarCliente() {
        String sql = "DELETE FROM Cliente WHERE cpf = ?";
        int confirm = JOptionPane.showConfirmDialog(this, "Tem certeza que deseja deletar este cliente?", "Confirmação", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, cpfField.getText());

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this, "Cliente deletado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    limparCampos();
                } else {
                    JOptionPane.showMessageDialog(this, "Nenhum cliente encontrado com o CPF informado.", "Aviso", JOptionPane.WARNING_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao deletar cliente: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void buscarCliente() {
        String sql = "SELECT nome, rua, numero, cidade, FN_Categoria_Cliente(cpf) AS categoria FROM Cliente WHERE cpf = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cpfField.getText());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                nomeField.setText(rs.getString("nome"));
                ruaField.setText(rs.getString("rua"));
                numeroField.setText(rs.getString("numero"));
                cidadeField.setText(rs.getString("cidade"));
                categoriaField.setText(rs.getString("categoria"));

            } else {
                JOptionPane.showMessageDialog(this, "Nenhum cliente encontrado com o CPF informado.", "Aviso", JOptionPane.WARNING_MESSAGE);
                limparCamposParcialmente();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao buscar cliente: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limparCampos() {
        cpfField.setText("");
        limparCamposParcialmente();
    }

    private void limparCamposParcialmente() {
        nomeField.setText("");
        ruaField.setText("");
        numeroField.setText("");
        cidadeField.setText("");
        categoriaField.setText("");

    }
}