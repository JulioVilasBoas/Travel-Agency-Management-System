DELIMITER //

CREATE FUNCTION FN_Categoria_Cliente(
    p_cliente_cpf CHAR(11)
)
RETURNS VARCHAR(20)
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_gasto_total DECIMAL(12, 2);
    SELECT SUM(valor_total_reserva)
    INTO v_gasto_total
    FROM Reserva
    WHERE cliente_cpf = p_cliente_cpf;

    CASE
        WHEN v_gasto_total >= 10000.00 THEN
            RETURN 'Cliente VIP';
        WHEN v_gasto_total >= 5000.00 THEN
            RETURN 'Cliente Ouro';
        ELSE
            RETURN 'Cliente Padrão';
    END CASE;

END //

DELIMITER ;
-- Justificativa funcao 1: Muito útil para o negócio, já que identifica os clientes de alto valor, classificando-os em: "vip", "ouro" e "padrão". Pode ser usado para aplicar descontos, oferecer pacotes exclusivos ou priorizar o atendimento.

DELIMITER //

CREATE FUNCTION FN_Total_Vendas_Funcionario(
    p_id_func INT
)
RETURNS INT
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_total_reservas INT;

    SELECT COUNT(id_reserva)
    INTO v_total_reservas
    FROM Reserva
    WHERE id_func = p_id_func;

    RETURN v_total_reservas;

END //

DELIMITER ;

-- Justificativa funcao 2: Útil para o gerenciamento de vendas. Pode ser usado para criar relatórios de desempenho, calcular o bônus e identificar os funcionarios produtivos de forma bem rápida.