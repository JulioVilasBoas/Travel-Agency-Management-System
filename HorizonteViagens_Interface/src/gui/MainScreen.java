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
        tabbedPane.addTab("Consultas", consultasPanel);

        //aba de clientes
        ClientePanel clientePanel = new ClientePanel();
        tabbedPane.addTab("Gerenciar clientes", clientePanel);

        //aba de funcionarios
        FuncionarioPanel funcionarioPanel = new FuncionarioPanel();
        tabbedPane.addTab("Gerenciar funcionários", funcionarioPanel);

        //aba de estatisticas
        EstatisticasPanel estatisticasPanel = new EstatisticasPanel();
        tabbedPane.addTab("Estatística (graficos)", estatisticasPanel);


        add(tabbedPane);
    }
}
