package gui;

import database.DatabaseConnection;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class EstatisticasPanel extends JPanel {

    private JComboBox<String> dispersaoComboBox;
    private JComboBox<String> pizzaComboBox;
    private JComboBox<String> barraComboBox;
    private JButton radarButton;

    // Onde o gráfico vai ser desenhado
    private JPanel chartContainer;

    // Flag para evitar loops de eventos em combos
    private boolean isAdjusting = false;

    public EstatisticasPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Painel de controles (topo)
        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Combo Dispersão
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

        // Combo Pizza
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

        // Combo Barra
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

        // Botão Radar
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        radarButton = new JButton("Mostrar Gráfico de Radar");
        controlsPanel.add(radarButton, gbc);

        add(controlsPanel, BorderLayout.NORTH);

        // Painel para conter o gráfico
        chartContainer = new JPanel(new BorderLayout());
        chartContainer.setBorder(BorderFactory.createTitledBorder("Visualização do Gráfico"));
        add(chartContainer, BorderLayout.CENTER);

        // Ações (usando isAdjusting para evitar loop)
        dispersaoComboBox.addActionListener(e -> {
            if (isAdjusting) return;
            exibirGraficoDispersao();
        });

        pizzaComboBox.addActionListener(e -> {
            if (isAdjusting) return;
            exibirGraficoPizza();
        });

        barraComboBox.addActionListener(e -> {
            if (isAdjusting) return;
            exibirGraficoBarra();
        });

        radarButton.addActionListener(e -> exibirGraficoRadar());
    }

    /* ==================== HELPERS GERAIS ==================== */

    private void mostrarChart(JFreeChart chart) {
        chartContainer.removeAll();
        if (chart != null) {
            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setMouseWheelEnabled(true);      // zoom com scroll
            chartPanel.setMouseZoomable(true, false);   // zoom arrastando com o mouse
            chartContainer.add(chartPanel, BorderLayout.CENTER);
        } else {
            chartContainer.add(
                    new JLabel("Nenhum dado para exibir.", SwingConstants.CENTER),
                    BorderLayout.CENTER
            );
        }
        chartContainer.revalidate();
        chartContainer.repaint();
    }

    private void limparOutrosCombos(JComboBox<?> ativo) {
        // evita loop dos listeners usando a flag
        isAdjusting = true;
        try {
            if (ativo != dispersaoComboBox && dispersaoComboBox.getSelectedIndex() != 0) {
                dispersaoComboBox.setSelectedIndex(0);
            }
            if (ativo != pizzaComboBox && pizzaComboBox.getSelectedIndex() != 0) {
                pizzaComboBox.setSelectedIndex(0);
            }
            if (ativo != barraComboBox && barraComboBox.getSelectedIndex() != 0) {
                barraComboBox.setSelectedIndex(0);
            }
        } finally {
            isAdjusting = false;
        }
    }

    /* ==================== GRÁFICOS DE PIZZA ==================== */

    private void exibirGraficoPizza() {
        limparOutrosCombos(pizzaComboBox);
        int selectedIndex = pizzaComboBox.getSelectedIndex();

        switch (selectedIndex) {
            case 1: // Gênero
                mostrarChart(criarPizzaGenero());
                break;
            case 2: // Normalmente viaja (companhia_viagem)
                mostrarChart(criarPizzaCompanhiaViagem());
                break;
            case 3: // Preferência de destino
                mostrarChart(criarPizzaTipoDestino());
                break;
            default:
                mostrarChart(null);
        }
    }

    private JFreeChart criarPizzaGenero() {
        DefaultPieDataset dataset = new DefaultPieDataset();

        String sql = "SELECT genero, COUNT(*) AS total " +
                "FROM Pesquisa_Perfil_Cliente " +
                "WHERE genero IS NOT NULL AND genero <> '' " +
                "GROUP BY genero";

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[criarPizzaGenero] Conexão é null.");
                return null;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                boolean temDados = false;
                while (rs.next()) {
                    String genero = rs.getString("genero");
                    int total = rs.getInt("total");
                    dataset.setValue(genero, total);
                    temDados = true;
                }
                if (!temDados) {
                    System.out.println("[criarPizzaGenero] Nenhum registro encontrado.");
                    return null;
                }
            }
        } catch (SQLException e) {
            System.err.println("[criarPizzaGenero] Erro ao buscar dados: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        return ChartFactory.createPieChart(
                "Distribuição de Clientes por Gênero",
                dataset,
                true,
                true,
                false
        );
    }

    private JFreeChart criarPizzaCompanhiaViagem() {
        DefaultPieDataset dataset = new DefaultPieDataset();

        String sql = "SELECT companhia_viagem, COUNT(*) AS total " +
                "FROM Pesquisa_Perfil_Cliente " +
                "WHERE companhia_viagem IS NOT NULL AND companhia_viagem <> '' " +
                "GROUP BY companhia_viagem";

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[criarPizzaCompanhiaViagem] Conexão é null.");
                return null;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                boolean temDados = false;
                while (rs.next()) {
                    String companhia = rs.getString("companhia_viagem");
                    int total = rs.getInt("total");
                    dataset.setValue(companhia, total);
                    temDados = true;
                }
                if (!temDados) {
                    System.out.println("[criarPizzaCompanhiaViagem] Nenhum registro encontrado.");
                    return null;
                }
            }
        } catch (SQLException e) {
            System.err.println("[criarPizzaCompanhiaViagem] Erro ao buscar dados: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        return ChartFactory.createPieChart(
                "Com quem o Cliente Normalmente Viaja",
                dataset,
                true,
                true,
                false
        );
    }

    private JFreeChart criarPizzaTipoDestino() {
        DefaultPieDataset dataset = new DefaultPieDataset();

        String sql = "SELECT tipo_destino, COUNT(*) AS total " +
                "FROM Pesquisa_Perfil_Cliente " +
                "WHERE tipo_destino IS NOT NULL AND tipo_destino <> '' " +
                "GROUP BY tipo_destino";

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[criarPizzaTipoDestino] Conexão é null.");
                return null;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                boolean temDados = false;
                while (rs.next()) {
                    String destino = rs.getString("tipo_destino");
                    int total = rs.getInt("total");
                    dataset.setValue(destino, total);
                    temDados = true;
                }
                if (!temDados) {
                    System.out.println("[criarPizzaTipoDestino] Nenhum registro encontrado.");
                    return null;
                }
            }
        } catch (SQLException e) {
            System.err.println("[criarPizzaTipoDestino] Erro ao buscar dados: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        return ChartFactory.createPieChart(
                "Preferência de Tipo de Destino",
                dataset,
                true,
                true,
                false
        );
    }

    /* ==================== GRÁFICOS DE BARRA ==================== */

    private void exibirGraficoBarra() {
        limparOutrosCombos(barraComboBox);
        int selectedIndex = barraComboBox.getSelectedIndex();

        switch (selectedIndex) {
            case 1: // Duração média (distribuição por faixa de duração)
                mostrarChart(criarBarraDuracao());
                break;
            case 2: // Faixa Etária
                mostrarChart(criarBarraFaixaEtaria());
                break;
            case 3: // Preferência atributos do pacote (criterio_pacote)
                mostrarChart(criarBarraCriterioPacote());
                break;
            case 4: // Renda Média (distribuição por faixa de renda)
                mostrarChart(criarBarraRendaFamiliar());
                break;
            default:
                mostrarChart(null);
        }
    }

    private JFreeChart criarBarraFaixaEtaria() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        String sql = "SELECT faixa_etaria, COUNT(*) AS total " +
                "FROM Pesquisa_Perfil_Cliente " +
                "WHERE faixa_etaria IS NOT NULL AND faixa_etaria <> '' " +
                "GROUP BY faixa_etaria " +
                "ORDER BY faixa_etaria";

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[criarBarraFaixaEtaria] Conexão é null.");
                return null;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                boolean temDados = false;
                while (rs.next()) {
                    String faixa = rs.getString("faixa_etaria");
                    int total = rs.getInt("total");
                    dataset.addValue(total, "Clientes", faixa);
                    temDados = true;
                }
                if (!temDados) {
                    System.out.println("[criarBarraFaixaEtaria] Nenhum registro encontrado.");
                    return null;
                }
            }
        } catch (SQLException e) {
            System.err.println("[criarBarraFaixaEtaria] Erro ao buscar dados: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        return ChartFactory.createBarChart(
                "Distribuição de Clientes por Faixa Etária",
                "Faixa Etária",
                "Quantidade de Clientes",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );
    }

    private JFreeChart criarBarraDuracao() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        String sql = "SELECT duracao_media, COUNT(*) AS total " +
                "FROM Pesquisa_Perfil_Cliente " +
                "WHERE duracao_media IS NOT NULL AND duracao_media <> '' " +
                "GROUP BY duracao_media " +
                "ORDER BY duracao_media";

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[criarBarraDuracao] Conexão é null.");
                return null;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                boolean temDados = false;
                while (rs.next()) {
                    String duracao = rs.getString("duracao_media");
                    int total = rs.getInt("total");
                    dataset.addValue(total, "Clientes", duracao);
                    temDados = true;
                }
                if (!temDados) {
                    System.out.println("[criarBarraDuracao] Nenhum registro encontrado.");
                    return null;
                }
            }
        } catch (SQLException e) {
            System.err.println("[criarBarraDuracao] Erro ao buscar dados: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        return ChartFactory.createBarChart(
                "Duração Média das Viagens (Frequência)",
                "Duração",
                "Quantidade de Clientes",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );
    }

    private JFreeChart criarBarraCriterioPacote() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        String sql = "SELECT criterio_pacote, COUNT(*) AS total " +
                "FROM Pesquisa_Perfil_Cliente " +
                "WHERE criterio_pacote IS NOT NULL AND criterio_pacote <> '' " +
                "GROUP BY criterio_pacote";

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[criarBarraCriterioPacote] Conexão é null.");
                return null;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                boolean temDados = false;
                while (rs.next()) {
                    String criterio = rs.getString("criterio_pacote");
                    int total = rs.getInt("total");
                    dataset.addValue(total, "Clientes", criterio);
                    temDados = true;
                }
                if (!temDados) {
                    System.out.println("[criarBarraCriterioPacote] Nenhum registro encontrado.");
                    return null;
                }
            }
        } catch (SQLException e) {
            System.err.println("[criarBarraCriterioPacote] Erro ao buscar dados: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        return ChartFactory.createBarChart(
                "Critério Mais Importante na Escolha do Pacote",
                "Critério",
                "Quantidade de Clientes",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );
    }

    private JFreeChart criarBarraRendaFamiliar() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        String sql = "SELECT renda_familiar, COUNT(*) AS total " +
                "FROM Pesquisa_Perfil_Cliente " +
                "WHERE renda_familiar IS NOT NULL AND renda_familiar <> '' " +
                "GROUP BY renda_familiar";

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[criarBarraRendaFamiliar] Conexão é null.");
                return null;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                boolean temDados = false;
                while (rs.next()) {
                    String renda = rs.getString("renda_familiar");
                    int total = rs.getInt("total");
                    dataset.addValue(total, "Clientes", renda);
                    temDados = true;
                }
                if (!temDados) {
                    System.out.println("[criarBarraRendaFamiliar] Nenhum registro encontrado.");
                    return null;
                }
            }
        } catch (SQLException e) {
            System.err.println("[criarBarraRendaFamiliar] Erro ao buscar dados: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        return ChartFactory.createBarChart(
                "Distribuição de Clientes por Renda Familiar",
                "Faixa de Renda",
                "Quantidade de Clientes",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );
    }

    /* ==================== GRÁFICOS DE DISPERSÃO ==================== */

    /**
     * Cria um scatter plot categórico genérico, mapeando texto -> índice em tempo de execução.
     */
    private JFreeChart criarScatterGenerico(
            String titulo,
            String labelX,
            String labelY,
            String colunaX,
            String colunaY
    ) {
        XYSeries series = new XYSeries("Clientes");

        String sql = "SELECT " + colunaX + ", " + colunaY + ", COUNT(*) AS total " +
                "FROM Pesquisa_Perfil_Cliente " +
                "WHERE " + colunaX + " IS NOT NULL AND " + colunaX + " <> '' " +
                "  AND " + colunaY + " IS NOT NULL AND " + colunaY + " <> '' " +
                "GROUP BY " + colunaX + ", " + colunaY;

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[criarScatterGenerico] Conexão é null.");
                return null;
            }

            // mapeia categoria -> índice (mantendo ordem de inserção)
            Map<String, Integer> mapaX = new LinkedHashMap<>();
            Map<String, Integer> mapaY = new LinkedHashMap<>();
            int proxX = 0;
            int proxY = 0;

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                boolean temDados = false;
                while (rs.next()) {
                    String valorX = rs.getString(colunaX);
                    String valorY = rs.getString(colunaY);
                    int total = rs.getInt("total");

                    if (valorX == null || valorY == null) continue;

                    Integer xIndex = mapaX.get(valorX);
                    if (xIndex == null) {
                        xIndex = proxX++;
                        mapaX.put(valorX, xIndex);
                    }

                    Integer yIndex = mapaY.get(valorY);
                    if (yIndex == null) {
                        yIndex = proxY++;
                        mapaY.put(valorY, yIndex);
                    }

                    // adiciona o ponto 'total' vezes para evidenciar concentração
                    for (int i = 0; i < total; i++) {
                        series.add(xIndex, yIndex);
                    }
                    temDados = true;
                }

                if (!temDados) {
                    System.out.println("[criarScatterGenerico] Nenhum registro encontrado para "
                            + colunaX + " x " + colunaY);
                    return null;
                }
            }

            XYSeriesCollection dataset = new XYSeriesCollection(series);

            JFreeChart chart = ChartFactory.createScatterPlot(
                    titulo,
                    labelX,
                    labelY,
                    dataset,
                    PlotOrientation.VERTICAL,
                    false,   // legenda
                    true,    // tooltips
                    false    // URLs
            );

            XYPlot plot = (XYPlot) chart.getPlot();
            String[] labelsX = mapaX.keySet().toArray(new String[0]);
            String[] labelsY = mapaY.keySet().toArray(new String[0]);

            SymbolAxis xAxis = new SymbolAxis(labelX, labelsX);
            SymbolAxis yAxis = new SymbolAxis(labelY, labelsY);
            plot.setDomainAxis(xAxis);
            plot.setRangeAxis(yAxis);

            return chart;
        } catch (SQLException e) {
            System.err.println("[criarScatterGenerico] Erro ao buscar dados: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void exibirGraficoDispersao() {
        limparOutrosCombos(dispersaoComboBox);
        int idx = dispersaoComboBox.getSelectedIndex();

        switch (idx) {
            case 1: // Duração x Renda
                mostrarChart(criarScatterGenerico(
                        "Relação entre Duração da Viagem e Renda Familiar",
                        "Duração Média",
                        "Renda Familiar",
                        "duracao_media",
                        "renda_familiar"
                ));
                break;
            case 2: // Orçamento x Renda
                mostrarChart(criarScatterGenerico(
                        "Relação entre Orçamento por Pessoa e Renda Familiar",
                        "Orçamento por Pessoa",
                        "Renda Familiar",
                        "orcamento_pessoa",
                        "renda_familiar"
                ));
                break;
            case 3: // Tipo Hospedagem x Gênero
                mostrarChart(criarScatterGenerico(
                        "Tipo de Hospedagem x Gênero",
                        "Tipo de Hospedagem",
                        "Gênero",
                        "tipo_hospedagem",
                        "genero"
                ));
                break;
            case 4: // Duração x Orçamento
                mostrarChart(criarScatterGenerico(
                        "Relação entre Duração da Viagem e Orçamento por Pessoa",
                        "Duração Média",
                        "Orçamento por Pessoa",
                        "duracao_media",
                        "orcamento_pessoa"
                ));
                break;
            case 5: // Hospedagem x Orçamento
                mostrarChart(criarScatterGenerico(
                        "Tipo de Hospedagem x Orçamento por Pessoa",
                        "Tipo de Hospedagem",
                        "Orçamento por Pessoa",
                        "tipo_hospedagem",
                        "orcamento_pessoa"
                ));
                break;
            default:
                System.out.println("[exibirGraficoDispersao] Opção ainda não implementada.");
                mostrarChart(null);
        }
    }

    /* ==================== RADAR (PLACEHOLDER) ==================== */

    private void exibirGraficoRadar() {
        limparOutrosCombos(null);
        System.out.println("[exibirGraficoRadar] Gráfico de radar ainda não implementado.");
        mostrarChart(null);
    }

    /* ==================== MAIN DE TESTE ISOLADO ==================== */

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Teste - Dashboard Estatístico");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new EstatisticasPanel());
            frame.setSize(1000, 700);
            frame.setLocationRelativeTo(null); // centraliza
            frame.setVisible(true);
        });
    }
}
