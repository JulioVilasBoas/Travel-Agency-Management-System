CREATE VIEW V_Relatorio_Reservas AS
SELECT
    r.id_reserva,
    r.data_venda,
    r.status_pagamento,
    r.valor_total_reserva,
    c.nome AS nome_cliente,         
    c.cpf AS cpf_cliente,
    f.nome AS nome_funcionario,     
    p.nome_pacote,                  
    p.data_inicio,
    p.data_final
FROM
    Reserva r
JOIN Cliente c ON r.cliente_cpf = c.cpf
JOIN Funcionario f ON r.id_func = f.id_func
JOIN Pacote_viagem p ON r.id_pacote = p.id_pacote;
-- Essa visão une as informações do cliente, do funcionário que realizou a venda e do pacote que foi vendido. Ela facilita na criação de relatório de vendas, cálculo de comissões e pra checar o desempenho dos funcionários.

CREATE VIEW V_Roteiro_Pacote_Detalhado AS
SELECT
    pv.id_pacote,
    pv.nome_pacote,
    pv.preco_total,
    d.cidade AS cidade_destino,    
    d.pais AS pais_destino,
    s.tipo_servico,                 
    s.descricao AS descricao_servico,
    f.nome_fantasia AS nome_fornecedor  
FROM
    Pacote_viagem pv

JOIN Destino d ON pv.id_destino = d.id_destino
JOIN Composicao_Pacote cp ON pv.id_pacote = cp.id_pacote
JOIN Servico s ON cp.id_servico = s.id_servico
JOIN Fornecedor f ON s.fornecedor_cnpj = f.cnpj;
-- Essa visão cria um roteiro detalado pra cada pacote, mostrando para onde é a viagem, quais os serviços que ela inclui e quem são os fornecedores de cada um desses serviços. Ela facilita na hora de montar o itinerário pro cliente.
