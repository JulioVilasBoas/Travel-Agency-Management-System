package gui;

import javax.swing.*;
import java.awt.*;

public class MainScreen extends JFrame {

    public MainScreen() {

        setTitle("Horizonte Viagens - INTERFACE");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        //aba de consultas
        ConsultasPanel consultasPanel = new ConsultasPanel();
        tabbedPane.addTab("Consultas/Views/Trigger", consultasPanel);

        //aba de clientes
        ClientePanel clientePanel = new ClientePanel();
        tabbedPane.addTab("Gerenciar clientes", clientePanel);

        //aba de funcionarios
        FuncionarioPanel funcionarioPanel = new FuncionarioPanel();
        tabbedPane.addTab("Gerenciar funcionários", funcionarioPanel);

        //aba de destinos
        DestinoPanel destinoPanel = new DestinoPanel();
        tabbedPane.addTab("Gerenciar Destinos", destinoPanel);

        //aba de fornecedores
        FornecedorPanel fornecedorPanel = new FornecedorPanel();
        tabbedPane.addTab("Gerenciar Fornecedores", fornecedorPanel);

        //aba de ferramentas (procedimentos)
        FerramentasPanel ferramentasPanel = new FerramentasPanel();
        tabbedPane.addTab("Ferramentas (procedures)", ferramentasPanel);

        //aba de estatisticas
        EstatisticasPanel estatisticasPanel = new EstatisticasPanel();
        tabbedPane.addTab("Estatística (graficos)", estatisticasPanel);


        add(tabbedPane);
    }
}