SELECT nome, rua, numero, cidade FROM Cliente WHERE cpf = 12345678900; 
-- SINTAXE JAVA: "SELECT nome, rua, numero, cidade FROM Cliente WHERE cpf = ?"

SELECT nome, cargo, data_admissao, supervisor 
FROM Funcionario 
WHERE id_func = 1;
-- SINTAXE JAVA: "SELECT nome, cargo, data_admissao, supervisor FROM Funcionario WHERE id_func = ?"

SELECT nome_pacote, preco_total 
FROM Pacote_viagem 
ORDER BY preco_total DESC LIMIT 5;
-- SINTAXE JAVA: "SELECT nome_pacote, preco_total FROM Pacote_viagem ORDER BY preco_total DESC LIMIT 5;"

SELECT id_reserva, cliente_cpf, valor_total_reserva FROM Reserva WHERE status_pagamento = 'Pendente';
-- SINTAXE JAVA: "SELECT id_reserva, cliente_cpf, valor_total_reserva FROM Reserva WHERE status_pagamento = 'Pendente';"

SELECT f.nome AS nome_funcionario, COUNT(r.id_reserva) AS total_reservas
FROM Funcionario f JOIN Reserva r ON f.id_func = r.id_func
GROUP BY f.nome ORDER BY total_reservas DESC;
-- SINTAXE JAVA: "SELECT f.nome AS nome_funcionario, COUNT(r.id_reserva) AS total_reservas " + "FROM Funcionario f " + "JOIN Reserva r ON f.id_func = r.id_func " + "GROUP BY f.nome " + "ORDER BY total_reservas DESC;";

SELECT fo.nome_fantasia, COUNT(s.id_servico) AS quantidade_servicos
FROM Fornecedor fo JOIN Servico s ON fo.cnpj = s.fornecedor_cnpj
GROUP BY fo.nome_fantasia;
-- SINTAXE JAVA: "SELECT fo.nome_fantasia, COUNT(s.id_servico) as quantidade_servicos FROM Fornecedor fo JOIN Servico s ON fo.cnpj = s.fornecedor_cnpj GROUP BY fo.nome_fantasia;"
 