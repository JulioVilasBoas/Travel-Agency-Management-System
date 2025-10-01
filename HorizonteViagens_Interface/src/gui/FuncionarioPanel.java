package gui;

import database.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FuncionarioPanel extends JPanel {

    private JTextField idField, nomeField, cargoField, dataAdmissaoField, supervisorField;
    private JButton addButton, updateButton, deleteButton, findButton;

    public FuncionarioPanel() {
        setLayout(new BorderLayout(10, 10));

        //painel do form
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        formPanel.add(new JLabel("ID (preenchido ao buscar o func):"));
        idField = new JTextField();
        idField.setEditable(false);
        formPanel.add(idField);

        formPanel.add(new JLabel("Nome Completo:"));
        nomeField = new JTextField();
        formPanel.add(nomeField);

        formPanel.add(new JLabel("Cargo:"));
        cargoField = new JTextField();
        formPanel.add(cargoField);

        formPanel.add(new JLabel("Data Admissão (AAAA-MM-DD):"));
        dataAdmissaoField = new JTextField();
        formPanel.add(dataAdmissaoField);

        formPanel.add(new JLabel("ID do Supervisor (se tiver):"));
        supervisorField = new JTextField();
        formPanel.add(supervisorField);

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

        // ações dos botões
        addButton.addActionListener(e -> adicionarFuncionario());
        updateButton.addActionListener(e -> atualizarFuncionario());
        deleteButton.addActionListener(e -> deletarFuncionario());
        findButton.addActionListener(e -> buscarFuncionario());
    }

    private void adicionarFuncionario() {
        String sql = "INSERT INTO Funcionario (nome, cargo, data_admissao, supervisor) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nomeField.getText());
            pstmt.setString(2, cargoField.getText());
            pstmt.setDate(3, java.sql.Date.valueOf(dataAdmissaoField.getText()));

            if (supervisorField.getText().isEmpty()) {
                pstmt.setNull(4, java.sql.Types.INTEGER);
            } else {
                pstmt.setInt(4, Integer.parseInt(supervisorField.getText()));
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Funcionário adicionado com sucesso!!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                limparCampos();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao adicionar funcionário: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void atualizarFuncionario() {
        String sql = "UPDATE Funcionario SET nome = ?, cargo = ?, data_admissao = ?, supervisor = ? WHERE id_func = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nomeField.getText());
            pstmt.setString(2, cargoField.getText());
            pstmt.setDate(3, java.sql.Date.valueOf(dataAdmissaoField.getText()));

            if (supervisorField.getText().isEmpty()) {
                pstmt.setNull(4, java.sql.Types.INTEGER);
            } else {
                pstmt.setInt(4, Integer.parseInt(supervisorField.getText()));
            }
            pstmt.setInt(5, Integer.parseInt(idField.getText()));

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Funcionário atualizado com sucesso!!!!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Nenhum funcionário encontrado com o ID informado.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar funcionário: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deletarFuncionario() {
        String sql = "DELETE FROM Funcionario WHERE id_func = ?";
        String idParaDeletar = JOptionPane.showInputDialog(this, "Digite o ID do funcionrio a ser deletado:");

        if (idParaDeletar != null && !idParaDeletar.trim().isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(this, "Tem certeza que deseja deletar o funcionário com ID " + idParaDeletar + "?", "Confirmação", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {

                    pstmt.setInt(1, Integer.parseInt(idParaDeletar));

                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        JOptionPane.showMessageDialog(this, "Funcionário deletado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                        limparCampos();
                    } else {
                        JOptionPane.showMessageDialog(this, "Nenhum funcionário encontrado com o ID informado.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erro ao deletar funcionário: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void buscarFuncionario() {
        String idParaBuscar = JOptionPane.showInputDialog(this, "Digite o ID do funcionário a ser buscado:");
        if (idParaBuscar != null && !idParaBuscar.trim().isEmpty()) {
            String sql = "SELECT nome, cargo, data_admissao, supervisor FROM Funcionario WHERE id_func = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, Integer.parseInt(idParaBuscar));
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    idField.setText(idParaBuscar);
                    nomeField.setText(rs.getString("nome"));
                    cargoField.setText(rs.getString("cargo"));
                    dataAdmissaoField.setText(rs.getDate("data_admissao").toString());

                    int supervisorId = rs.getInt("supervisor");
                    if (rs.wasNull()) {
                        supervisorField.setText("");
                    } else {
                        supervisorField.setText(String.valueOf(supervisorId));
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Nenhum funcionário encontrado com o ID informado.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    limparCampos();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao buscar funcionário: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void limparCampos() {
        idField.setText("");
        nomeField.setText("");
        cargoField.setText("");
        dataAdmissaoField.setText("");
        supervisorField.setText("");
    }
}
