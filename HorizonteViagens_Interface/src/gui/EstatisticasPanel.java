package gui;

import database.DatabaseConnection;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.SpiderWebPlot;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class EstatisticasPanel extends JPanel {

    // Combos de tipo de gráfico
    private JComboBox<String> dispersaoComboBox;
    private JComboBox<String> pizzaComboBox;
    private JComboBox<String> barraComboBox;
    private JButton radarButton;

    // Filtros
    private JComboBox<String> filtroGeneroCombo;
    private JComboBox<String> filtroFaixaCombo;
    private JComboBox<String> filtroRendaCombo;

    // Indicadores resumidos
    private JPanel indicadoresPanel;
    private JLabel totalRegistrosLabel;
    private JLabel mediaOrcamentoLabel;
    private JLabel medianaOrcamentoLabel;
    private JLabel modaOrcamentoLabel;
    private JLabel varianciaOrcamentoLabel;
    private JLabel desvioPadraoOrcamentoLabel;

    // Onde o gráfico vai ser desenhado
    private JPanel chartContainer;

    // Flag para evitar loops de eventos em combos
    private boolean isAdjusting = false;

    public EstatisticasPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ===================== PAINEL DE CONTROLES =====================
        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // ================== LINHA 0: Dispersão | Pizza ==================
        gbc.gridy = 0;

        // Gráficos de Dispersão
        gbc.gridx = 0;
        gbc.weightx = 0;
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
        gbc.weightx = 0.5;
        controlsPanel.add(dispersaoComboBox, gbc);

        // Gráficos de Pizza
        gbc.gridx = 2;
        gbc.weightx = 0;
        controlsPanel.add(new JLabel("Gráficos de Pizza:"), gbc);

        String[] pizzaOptions = {
                "Selecione um gráfico...",
                "Gênero",
                "Normalmente Viaja",
                "Preferência de Destino"
        };
        pizzaComboBox = new JComboBox<>(pizzaOptions);
        gbc.gridx = 3;
        gbc.weightx = 0.5;
        controlsPanel.add(pizzaComboBox, gbc);

        // ========== LINHA 1: Barra/Linha | Filtro Gênero ===============
        gbc.gridy = 1;

        // Gráficos de Barra/Linha
        gbc.gridx = 0;
        gbc.weightx = 0;
        controlsPanel.add(new JLabel("Gráficos de Barra/Linha:"), gbc);

        String[] barraOptions = {
                "Selecione um gráfico...",
                "Duração Média (Barra)",
                "Faixa Etária (Barra)",
                "Preferência Atributos Pacote (Barra)",
                "Renda Familiar (Linha)"
        };
        barraComboBox = new JComboBox<>(barraOptions);
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        controlsPanel.add(barraComboBox, gbc);

        // Filtro Gênero
        gbc.gridx = 2;
        gbc.weightx = 0;
        controlsPanel.add(new JLabel("Filtro Gênero:"), gbc);

        String[] generoOptions = {
                "Todos",
                "Feminino",
                "Masculino"
        };
        filtroGeneroCombo = new JComboBox<>(generoOptions);
        gbc.gridx = 3;
        gbc.weightx = 0.5;
        controlsPanel.add(filtroGeneroCombo, gbc);

        // ========== LINHA 2: Filtro Faixa Etária | Filtro Renda ========
        gbc.gridy = 2;

        // Filtro Faixa Etária
        gbc.gridx = 0;
        gbc.weightx = 0;
        controlsPanel.add(new JLabel("Filtro Faixa Etária:"), gbc);

        filtroFaixaCombo = new JComboBox<>();
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        controlsPanel.add(filtroFaixaCombo, gbc);
        carregarFaixasEtariasDoBanco(); // popula com o que vem do banco

        // Filtro Renda Familiar
        gbc.gridx = 2;
        gbc.weightx = 0;
        controlsPanel.add(new JLabel("Filtro Renda Familiar:"), gbc);

        String[] rendaOptions = {
                "Todos",
                "Até R$1.518,00",
                "De R$1.518,00 até R$3.000",
                "De R$3.000 até R$6.000",
                "De R$6.000 até R$12.000",
                "Mais de R$12.000"
        };
        filtroRendaCombo = new JComboBox<>(rendaOptions);
        gbc.gridx = 3;
        gbc.weightx = 0.5;
        controlsPanel.add(filtroRendaCombo, gbc);

        // ================== LINHA 3: Botão Radar centralizado ==========
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.weightx = 0;
        radarButton = new JButton("Gráfico de Radar (Critérios do Pacote)");
        controlsPanel.add(radarButton, gbc);

        // ===================== PAINEL DE INDICADORES =====================
        indicadoresPanel = new JPanel(new GridLayout(2, 3, 10, 5));
        indicadoresPanel.setBorder(BorderFactory.createTitledBorder("Indicadores Resumidos"));

        totalRegistrosLabel = new JLabel("Total de registros: -");
        mediaOrcamentoLabel = new JLabel("Média orçamento (R$): -");
        medianaOrcamentoLabel = new JLabel("Mediana orçamento (R$): -");
        modaOrcamentoLabel = new JLabel("Moda orçamento (R$): -");
        varianciaOrcamentoLabel = new JLabel("Variância orçamento: -");
        desvioPadraoOrcamentoLabel = new JLabel("Desvio padrão orçamento: -");

        indicadoresPanel.add(totalRegistrosLabel);
        indicadoresPanel.add(mediaOrcamentoLabel);
        indicadoresPanel.add(medianaOrcamentoLabel);
        indicadoresPanel.add(modaOrcamentoLabel);
        indicadoresPanel.add(varianciaOrcamentoLabel);
        indicadoresPanel.add(desvioPadraoOrcamentoLabel);

        // Topo = controles + indicadores
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.add(controlsPanel, BorderLayout.NORTH);
        topPanel.add(indicadoresPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // Painel para conter o gráfico
        chartContainer = new JPanel(new BorderLayout());
        chartContainer.setBorder(BorderFactory.createTitledBorder("Visualização do Gráfico"));
        add(chartContainer, BorderLayout.CENTER);

        // ===================== AÇÕES =====================

        // Combos de tipo de gráfico
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

        // Filtros: ao mudar qualquer filtro, redesenha gráfico atual + indicadores
        filtroGeneroCombo.addActionListener(e -> atualizarGraficoAtual());
        filtroFaixaCombo.addActionListener(e -> atualizarGraficoAtual());
        filtroRendaCombo.addActionListener(e -> atualizarGraficoAtual());

        // Inicializa indicadores
        atualizarIndicadores();
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

    /**
     * Filtros comuns (gênero, faixa etária, renda) para as consultas.
     * Usa parâmetros para evitar SQL injection.
     * Agora a faixa etária é filtrada por igualdade exata, usando
     * o texto que veio diretamente do banco.
     */
    private void appendCommonFilters(StringBuilder sql, List<String> params) {
        String genero = (String) filtroGeneroCombo.getSelectedItem();
        String faixa  = (String) filtroFaixaCombo.getSelectedItem();
        String renda  = (String) filtroRendaCombo.getSelectedItem();

        // ---- Filtro de gênero ----
        if (genero != null && !"Todos".equals(genero)) {
            sql.append(" AND genero = ?");
            params.add(genero);
        }

        // ---- Filtro de faixa etária ----
        if (faixa != null && !"Todos".equals(faixa)) {
            sql.append(" AND faixa_etaria = ?");
            params.add(faixa);
        }

        // ---- Filtro de renda familiar ----
        if (renda != null && !"Todos".equals(renda)) {
            sql.append(" AND renda_familiar = ?");
            params.add(renda);
        }
    }


    /**
     * Carrega as faixas etárias diretamente do banco e popula o combo de filtro.
     * Isso garante que o valor selecionado exista exatamente na coluna faixa_etaria.
     */
    private void carregarFaixasEtariasDoBanco() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("Todos"); // sempre primeira opção

        String sql = "SELECT DISTINCT faixa_etaria " +
                "FROM Pesquisa_Perfil_Cliente " +
                "WHERE faixa_etaria IS NOT NULL AND faixa_etaria <> '' " +
                "ORDER BY faixa_etaria";

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[carregarFaixasEtariasDoBanco] Conexão é null.");
                filtroFaixaCombo.setModel(model);
                return;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    String faixa = rs.getString("faixa_etaria");
                    if (faixa != null) {
                        faixa = faixa.trim();
                        if (!faixa.isEmpty()) {
                            model.addElement(faixa);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[carregarFaixasEtariasDoBanco] Erro ao buscar faixas: " + e.getMessage());
            e.printStackTrace();
        }

        filtroFaixaCombo.setModel(model);
    }

    /**
     * Redesenha o último tipo de gráfico que estava selecionado ao mudar os filtros.
     * Também atualiza os indicadores.
     */
    private void atualizarGraficoAtual() {
        atualizarIndicadores();

        if (pizzaComboBox.getSelectedIndex() > 0) {
            exibirGraficoPizza();
        } else if (barraComboBox.getSelectedIndex() > 0) {
            exibirGraficoBarra();
        } else if (dispersaoComboBox.getSelectedIndex() > 0) {
            exibirGraficoDispersao();
        } else {
            mostrarChart(null);
        }
    }

    /* ==================== INDICADORES RESUMIDOS ==================== */

    private void atualizarIndicadores() {
        StringBuilder sql = new StringBuilder(
                "SELECT genero, orcamento_pessoa " +
                        "FROM Pesquisa_Perfil_Cliente WHERE 1=1"
        );
        List<String> params = new ArrayList<>();
        appendCommonFilters(sql, params);

        int totalRegistros = 0;
        int qtdFem = 0;
        int qtdMasc = 0;
        List<Double> valoresOrcamento = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[atualizarIndicadores] Conexão é null.");
                return;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int idx = 1;
                for (String p : params) {
                    ps.setString(idx++, p);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        totalRegistros++;

                        String genero = rs.getString("genero");
                        if ("Feminino".equalsIgnoreCase(genero)) qtdFem++;
                        if ("Masculino".equalsIgnoreCase(genero)) qtdMasc++;

                        String orc = rs.getString("orcamento_pessoa");
                        Double valor = parseOrcamento(orc);
                        if (valor != null) {
                            valoresOrcamento.add(valor);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[atualizarIndicadores] Erro ao buscar dados: " + e.getMessage());
            e.printStackTrace();
        }

        // Percentuais de gênero
        double percFem = 0.0;
        double percMasc = 0.0;
        if (totalRegistros > 0) {
            percFem = qtdFem * 100.0 / totalRegistros;
            percMasc = qtdMasc * 100.0 / totalRegistros;
        }

        totalRegistrosLabel.setText(
                String.format("Total de registros: %d | Fem: %.1f%% | Masc: %.1f%%",
                        totalRegistros, percFem, percMasc)
        );

        if (valoresOrcamento.isEmpty()) {
            mediaOrcamentoLabel.setText("Média orçamento (R$): -");
            medianaOrcamentoLabel.setText("Mediana orçamento (R$): -");
            modaOrcamentoLabel.setText("Moda orçamento (R$): -");
            varianciaOrcamentoLabel.setText("Variância orçamento: -");
            desvioPadraoOrcamentoLabel.setText("Desvio padrão orçamento: -");
            return;
        }

        Double media = calcularMedia(valoresOrcamento);
        Double mediana = calcularMediana(valoresOrcamento);
        Double moda = calcularModa(valoresOrcamento);
        Double variancia = calcularVariancia(valoresOrcamento, media);
        Double desvio = Math.sqrt(variancia);

        mediaOrcamentoLabel.setText(String.format("Média orçamento (R$): %.2f", media));
        medianaOrcamentoLabel.setText(String.format("Mediana orçamento (R$): %.2f", mediana));
        modaOrcamentoLabel.setText(String.format("Moda orçamento (R$): %.2f", moda));
        varianciaOrcamentoLabel.setText(String.format("Variância orçamento: %.2f", variancia));
        desvioPadraoOrcamentoLabel.setText(String.format("Desvio padrão orçamento: %.2f", desvio));
    }

    private Double parseOrcamento(String valor) {
        if (valor == null) return null;

        // Lista de valores numéricos encontrados na string
        List<Double> numeros = new ArrayList<>();

        // Regex para pegar pedaços como "600", "1.500", "1.500,00", etc.
        Pattern pattern = Pattern.compile("(\\d+[\\.\\d]*,?\\d*)");
        Matcher matcher = pattern.matcher(valor);

        while (matcher.find()) {
            String numRaw = matcher.group(1);          // ex: "1.500,00"
            String limpo = numRaw.replace(".", "")     // tira pontos de milhar
                    .replace(",", ".")   // vírgula -> ponto
                    .trim();
            try {
                double v = Double.parseDouble(limpo);
                numeros.add(v);
            } catch (NumberFormatException ignored) {
                // ignora valores que não conseguir converter
            }
        }

        if (numeros.isEmpty()) {
            return null;
        }

        // Se tiver só um número (ex: "600,00"), usa ele
        if (numeros.size() == 1) {
            return numeros.get(0);
        }

        // Se tiver faixa ("600 a 1.500"), usa a média da faixa
        double soma = 0.0;
        for (double n : numeros) {
            soma += n;
        }
        return soma / numeros.size();
    }


    private Double calcularMedia(List<Double> valores) {
        double soma = 0.0;
        for (double v : valores) soma += v;
        return soma / valores.size();
    }

    private Double calcularMediana(List<Double> valores) {
        List<Double> copia = new ArrayList<>(valores);
        Collections.sort(copia);
        int n = copia.size();
        if (n % 2 == 1) {
            return copia.get(n / 2);
        } else {
            return (copia.get(n / 2 - 1) + copia.get(n / 2)) / 2.0;
        }
    }

    private Double calcularModa(List<Double> valores) {
        Map<Double, Integer> contagem = new HashMap<>();
        for (double v : valores) {
            contagem.put(v, contagem.getOrDefault(v, 0) + 1);
        }
        Double moda = null;
        int maxFreq = 0;
        for (Map.Entry<Double, Integer> e : contagem.entrySet()) {
            if (e.getValue() > maxFreq) {
                maxFreq = e.getValue();
                moda = e.getKey();
            }
        }
        return moda;
    }

    private Double calcularVariancia(List<Double> valores, Double media) {
        double soma = 0.0;
        for (double v : valores) {
            double diff = v - media;
            soma += diff * diff;
        }
        // variância populacional
        return soma / valores.size();
    }

    /* ==================== GRÁFICOS DE PIZZA ==================== */

    private void exibirGraficoPizza() {
        atualizarIndicadores();
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

        StringBuilder sql = new StringBuilder(
                "SELECT genero, COUNT(*) AS total " +
                        "FROM Pesquisa_Perfil_Cliente " +
                        "WHERE genero IS NOT NULL AND genero <> ''"
        );
        List<String> params = new ArrayList<>();
        appendCommonFilters(sql, params);
        sql.append(" GROUP BY genero");

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[criarPizzaGenero] Conexão é null.");
                return null;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int idx = 1;
                for (String p : params) {
                    ps.setString(idx++, p);
                }

                try (ResultSet rs = ps.executeQuery()) {
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

        StringBuilder sql = new StringBuilder(
                "SELECT companhia_viagem, COUNT(*) AS total " +
                        "FROM Pesquisa_Perfil_Cliente " +
                        "WHERE companhia_viagem IS NOT NULL AND companhia_viagem <> ''"
        );
        List<String> params = new ArrayList<>();
        appendCommonFilters(sql, params);
        sql.append(" GROUP BY companhia_viagem");

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[criarPizzaCompanhiaViagem] Conexão é null.");
                return null;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int idx = 1;
                for (String p : params) {
                    ps.setString(idx++, p);
                }

                try (ResultSet rs = ps.executeQuery()) {
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

        StringBuilder sql = new StringBuilder(
                "SELECT tipo_destino, COUNT(*) AS total " +
                        "FROM Pesquisa_Perfil_Cliente " +
                        "WHERE tipo_destino IS NOT NULL AND tipo_destino <> ''"
        );
        List<String> params = new ArrayList<>();
        appendCommonFilters(sql, params);
        sql.append(" GROUP BY tipo_destino");

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[criarPizzaTipoDestino] Conexão é null.");
                return null;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int idx = 1;
                for (String p : params) {
                    ps.setString(idx++, p);
                }

                try (ResultSet rs = ps.executeQuery()) {
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

    /* ==================== GRÁFICOS DE BARRA / LINHA ==================== */

    private void exibirGraficoBarra() {
        atualizarIndicadores();
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
            case 4: // Renda Familiar (linha)
                mostrarChart(criarLinhaRendaFamiliar());
                break;
            default:
                mostrarChart(null);
        }
    }

    private JFreeChart criarBarraFaixaEtaria() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        StringBuilder sql = new StringBuilder(
                "SELECT faixa_etaria, COUNT(*) AS total " +
                        "FROM Pesquisa_Perfil_Cliente " +
                        "WHERE faixa_etaria IS NOT NULL AND faixa_etaria <> ''"
        );
        List<String> params = new ArrayList<>();
        appendCommonFilters(sql, params);
        sql.append(" GROUP BY faixa_etaria ORDER BY faixa_etaria");

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[criarBarraFaixaEtaria] Conexão é null.");
                return null;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int idx = 1;
                for (String p : params) {
                    ps.setString(idx++, p);
                }

                try (ResultSet rs = ps.executeQuery()) {
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

        StringBuilder sql = new StringBuilder(
                "SELECT duracao_media, COUNT(*) AS total " +
                        "FROM Pesquisa_Perfil_Cliente " +
                        "WHERE duracao_media IS NOT NULL AND duracao_media <> ''"
        );
        List<String> params = new ArrayList<>();
        appendCommonFilters(sql, params);
        sql.append(" GROUP BY duracao_media ORDER BY duracao_media");

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[criarBarraDuracao] Conexão é null.");
                return null;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int idx = 1;
                for (String p : params) {
                    ps.setString(idx++, p);
                }

                try (ResultSet rs = ps.executeQuery()) {
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

        StringBuilder sql = new StringBuilder(
                "SELECT criterio_pacote, COUNT(*) AS total " +
                        "FROM Pesquisa_Perfil_Cliente " +
                        "WHERE criterio_pacote IS NOT NULL AND criterio_pacote <> ''"
        );
        List<String> params = new ArrayList<>();
        appendCommonFilters(sql, params);
        sql.append(" GROUP BY criterio_pacote");

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[criarBarraCriterioPacote] Conexão é null.");
                return null;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int idx = 1;
                for (String p : params) {
                    ps.setString(idx++, p);
                }

                try (ResultSet rs = ps.executeQuery()) {
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

    // Linha para distribuição por renda familiar (tendência / comparação)
    private JFreeChart criarLinhaRendaFamiliar() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        StringBuilder sql = new StringBuilder(
                "SELECT renda_familiar, COUNT(*) AS total " +
                        "FROM Pesquisa_Perfil_Cliente " +
                        "WHERE renda_familiar IS NOT NULL AND renda_familiar <> ''"
        );
        List<String> params = new ArrayList<>();
        appendCommonFilters(sql, params);
        sql.append(" GROUP BY renda_familiar");

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[criarLinhaRendaFamiliar] Conexão é null.");
                return null;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int idx = 1;
                for (String p : params) {
                    ps.setString(idx++, p);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    boolean temDados = false;
                    while (rs.next()) {
                        String renda = rs.getString("renda_familiar");
                        int total = rs.getInt("total");
                        dataset.addValue(total, "Clientes", renda);
                        temDados = true;
                    }
                    if (!temDados) {
                        System.out.println("[criarLinhaRendaFamiliar] Nenhum registro encontrado.");
                        return null;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[criarLinhaRendaFamiliar] Erro ao buscar dados: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        return ChartFactory.createLineChart(
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
     * Scatter categórico genérico, mapeando texto -> índice.
     * Também respeita os filtros.
     */
    private JFreeChart criarScatterGenerico(
            String titulo,
            String labelX,
            String labelY,
            String colunaX,
            String colunaY
    ) {
        XYSeries series = new XYSeries("Clientes");

        StringBuilder sql = new StringBuilder(
                "SELECT " + colunaX + ", " + colunaY + ", COUNT(*) AS total " +
                        "FROM Pesquisa_Perfil_Cliente " +
                        "WHERE " + colunaX + " IS NOT NULL AND " + colunaX + " <> '' " +
                        "AND " + colunaY + " IS NOT NULL AND " + colunaY + " <> ''"
        );
        List<String> params = new ArrayList<>();
        appendCommonFilters(sql, params);
        sql.append(" GROUP BY ").append(colunaX).append(", ").append(colunaY);

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[criarScatterGenerico] Conexão é null.");
                return null;
            }

            Map<String, Integer> mapaX = new LinkedHashMap<>();
            Map<String, Integer> mapaY = new LinkedHashMap<>();
            int proxX = 0;
            int proxY = 0;

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int pIdx = 1;
                for (String p : params) {
                    ps.setString(pIdx++, p);
                }

                try (ResultSet rs = ps.executeQuery()) {
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

                        for (int i = 0; i < total; i++) {
                            series.add(xIndex, yIndex);
                        }
                        temDados = true;
                    }

                    if (!temDados) {
                        System.out.println("[criarScatterGenerico] Nenhum registro para "
                                + colunaX + " x " + colunaY);
                        return null;
                    }
                }
            }

            XYSeriesCollection dataset = new XYSeriesCollection(series);

            JFreeChart chart = ChartFactory.createScatterPlot(
                    titulo,
                    labelX,
                    labelY,
                    dataset,
                    PlotOrientation.VERTICAL,
                    false,
                    true,
                    false
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
        atualizarIndicadores();
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
                mostrarChart(null);
        }
    }

    /* ==================== RADAR ==================== */

    private JFreeChart criarRadarCriterioPacote() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        StringBuilder sql = new StringBuilder(
                "SELECT criterio_pacote, COUNT(*) AS total " +
                        "FROM Pesquisa_Perfil_Cliente " +
                        "WHERE criterio_pacote IS NOT NULL AND criterio_pacote <> ''"
        );
        List<String> params = new ArrayList<>();
        appendCommonFilters(sql, params);
        sql.append(" GROUP BY criterio_pacote");

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[criarRadarCriterioPacote] Conexão é null.");
                return null;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int idx = 1;
                for (String p : params) {
                    ps.setString(idx++, p);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    boolean temDados = false;
                    while (rs.next()) {
                        String criterio = rs.getString("criterio_pacote");
                        int total = rs.getInt("total");
                        dataset.addValue(total, "Clientes", criterio);
                        temDados = true;
                    }
                    if (!temDados) {
                        System.out.println("[criarRadarCriterioPacote] Nenhum registro encontrado.");
                        return null;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[criarRadarCriterioPacote] Erro ao buscar dados: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        SpiderWebPlot plot = new SpiderWebPlot(dataset);
        plot.setStartAngle(0);
        plot.setInteriorGap(0.35);

        return new JFreeChart(
                "Radar - Critérios na Escolha do Pacote",
                JFreeChart.DEFAULT_TITLE_FONT,
                plot,
                true
        );
    }

    private void exibirGraficoRadar() {
        atualizarIndicadores();
        limparOutrosCombos(null);
        mostrarChart(criarRadarCriterioPacote());
    }

    /* ==================== MAIN DE TESTE ISOLADO ==================== */

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Teste - Dashboard Estatístico");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new EstatisticasPanel());
            frame.setSize(1200, 750);
            frame.setLocationRelativeTo(null); // centraliza
            frame.setVisible(true);
        });
    }
}
