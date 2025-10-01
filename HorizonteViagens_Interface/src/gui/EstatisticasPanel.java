package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URL;

public class EstatisticasPanel extends JPanel {

    private JLabel imageLabel;
    private JComboBox<String> dispersaoComboBox;
    private JComboBox<String> pizzaComboBox;
    private JComboBox<String> barraComboBox;
    private JButton radarButton;

    public EstatisticasPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        //Painel controles
        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        //menu dispersao
        gbc.gridx = 0;
        gbc.gridy = 0;
        controlsPanel.add(new JLabel("Gráficos de Dispersão:"), gbc);

        String[] dispersaoOptions = {
                "Selecione um gráfico...",
                "Duração x Renda",
                "Orçamento x Renda",
                "Tipo Hospedagem x Gênero",
                "Duração x Orçamento",
                "Hospedagem x Orçamento"
        };
        dispersaoComboBox = new JComboBox<>(dispersaoOptions);
        gbc.gridx = 1;
        controlsPanel.add(dispersaoComboBox, gbc);

        //menu pizza
        gbc.gridx = 0;
        gbc.gridy = 1;
        controlsPanel.add(new JLabel("Gráficos de Pizza:"), gbc);

        String[] pizzaOptions = {
                "Selecione um gráfico...",
                "Gênero",
                "Normalmente Viaja",
                "Preferência de Destino"
        };
        pizzaComboBox = new JComboBox<>(pizzaOptions);
        gbc.gridx = 1;
        controlsPanel.add(pizzaComboBox, gbc);

        //menu Barra
        gbc.gridx = 0;
        gbc.gridy = 2;
        controlsPanel.add(new JLabel("Gráficos de Barra:"), gbc);

        String[] barraOptions = {
                "Selecione um gráfico...",
                "Duração Média",
                "Faixa Etária",
                "Preferência Atributos Pacote",
                "Renda Média"
        };
        barraComboBox = new JComboBox<>(barraOptions);
        gbc.gridx = 1;
        controlsPanel.add(barraComboBox, gbc);

        // botao radar
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2; // Ocupa duas colunas
        radarButton = new JButton("Mostrar Gráfico de Radar");
        controlsPanel.add(radarButton, gbc);

        add(controlsPanel, BorderLayout.NORTH);

        //painel exibicao
        imageLabel = new JLabel("Selecione um gráfico para visualizar.", SwingConstants.CENTER);
        imageLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        JScrollPane scrollPane = new JScrollPane(imageLabel);
        add(scrollPane, BorderLayout.CENTER);

        //acoes
        dispersaoComboBox.addActionListener(e -> exibirGraficoDispersao());
        pizzaComboBox.addActionListener(e -> exibirGraficoPizza());
        barraComboBox.addActionListener(e -> exibirGraficoBarra());
        radarButton.addActionListener(e -> {
            ActionListener dListener = dispersaoComboBox.getActionListeners()[0];
            ActionListener pListener = pizzaComboBox.getActionListeners()[0];
            ActionListener bListener = barraComboBox.getActionListeners()[0];
            dispersaoComboBox.removeActionListener(dListener);
            pizzaComboBox.removeActionListener(pListener);
            barraComboBox.removeActionListener(bListener);

            dispersaoComboBox.setSelectedIndex(0);
            pizzaComboBox.setSelectedIndex(0);
            barraComboBox.setSelectedIndex(0);

            dispersaoComboBox.addActionListener(dListener);
            pizzaComboBox.addActionListener(pListener);
            barraComboBox.addActionListener(bListener);

            exibirGrafico("/Graficos/radar_prefAtributos_Pacote.jpg");
        });
    }

    private void exibirGraficoDispersao() {
        ActionListener pListener = pizzaComboBox.getActionListeners()[0];
        ActionListener bListener = barraComboBox.getActionListeners()[0];
        pizzaComboBox.removeActionListener(pListener);
        barraComboBox.removeActionListener(bListener);

        pizzaComboBox.setSelectedIndex(0);
        barraComboBox.setSelectedIndex(0);

        pizzaComboBox.addActionListener(pListener);
        barraComboBox.addActionListener(bListener);

        int selectedIndex = dispersaoComboBox.getSelectedIndex();
        String filename = "";
        switch (selectedIndex) {
            case 1: filename = "dispersao_Duracao_Renda.jpg"; break;
            case 2: filename = "dispersao_Orcamento_Renda.jpg"; break;
            case 3: filename = "dispersao_tipoHospedagem_Genero.jpg"; break;
            case 4: filename = "dispersao_Duracao_Orcamento.jpg"; break;
            case 5: filename = "dispersao_Hospedagem_Orcamento.jpg"; break;
            default: imageLabel.setIcon(null); imageLabel.setText("Selecione um gráfico para visualizar."); return;
        }
        exibirGrafico("/Graficos/" + filename);
    }

    private void exibirGraficoPizza() {
        ActionListener dListener = dispersaoComboBox.getActionListeners()[0];
        ActionListener bListener = barraComboBox.getActionListeners()[0];
        dispersaoComboBox.removeActionListener(dListener);
        barraComboBox.removeActionListener(bListener);

        dispersaoComboBox.setSelectedIndex(0);
        barraComboBox.setSelectedIndex(0);

        dispersaoComboBox.addActionListener(dListener);
        barraComboBox.addActionListener(bListener);

        int selectedIndex = pizzaComboBox.getSelectedIndex();
        String filename = "";
        switch (selectedIndex) {
            case 1: filename = "pizza_Genero.jpg"; break;
            case 2: filename = "pizza_normalViaja.jpg"; break;
            case 3: filename = "pizza_prefDestino.jpg"; break;
            default: imageLabel.setIcon(null); imageLabel.setText("Selecione um gráfico para visualizar."); return;
        }
        exibirGrafico("/Graficos/" + filename);
    }

    private void exibirGraficoBarra() {
        ActionListener dListener = dispersaoComboBox.getActionListeners()[0];
        ActionListener pListener = pizzaComboBox.getActionListeners()[0];
        dispersaoComboBox.removeActionListener(dListener);
        pizzaComboBox.removeActionListener(pListener);

        dispersaoComboBox.setSelectedIndex(0);
        pizzaComboBox.setSelectedIndex(0);

        dispersaoComboBox.addActionListener(dListener);
        pizzaComboBox.addActionListener(pListener);

        int selectedIndex = barraComboBox.getSelectedIndex();
        String filename = "";
        switch (selectedIndex) {
            case 1: filename = "barra_duracaoMedia.jpg"; break;
            case 2: filename = "barra_faixaEtaria.jpg"; break;
            case 3: filename = "barra_prefAtributos_Pacote.jpg"; break;
            case 4: filename = "barra_rendaMedia.jpg"; break;
            default: imageLabel.setIcon(null); imageLabel.setText("Selecione um gráfico para visualizar."); return;
        }
        exibirGrafico("/Graficos/" + filename);
    }

    private void exibirGrafico(String imagePath) {
        URL imageUrl = getClass().getResource(imagePath);

        if (imageUrl != null) {
            ImageIcon icon = new ImageIcon(imageUrl);
            imageLabel.setIcon(icon);
            imageLabel.setText(null);
        } else {
            imageLabel.setIcon(null);
            imageLabel.setText("Erro: Imagem não encontrada em 'src" + imagePath + "'");
            JOptionPane.showMessageDialog(this,
                    "A imagem não foi encontrada. Verifique se:\n1. O ficheiro está na pasta 'src/Graficos'.\n2. O nome do ficheiro está exatamente correto (incluindo a extensão .jpg).",
                    "Erro ao Carregar Imagem",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}