DELIMITER //

CREATE TRIGGER TRG_Preencher_Valor_Reserva
BEFORE INSERT ON Reserva
FOR EACH ROW
BEGIN
    DECLARE v_preco_pacote DECIMAL(12, 2);

    SELECT preco_total
    INTO v_preco_pacote
    FROM Pacote_viagem
    WHERE id_pacote = NEW.id_pacote;
    SET NEW.valor_total_reserva = v_preco_pacote;
END //

DELIMITER ;

-- Justificativa trigger 1: Garante a integridade dos dados e a consistencia dos preços. Esse trigger vai impedir que um vendedor insira um valor errado (de forma manual). Além disso, congela o preço do pacote no momento da venda, para evitar o pacote mudar de preço depois da venda e o cliente ter que pagar mais do que foi informado.

-- log
CREATE TABLE LOG_Mudanca_Status_Reserva (
    id_log INT PRIMARY KEY AUTO_INCREMENT,
    id_reserva INT NOT NULL,
    data_hora_mudanca DATETIME DEFAULT CURRENT_TIMESTAMP,
    usuario_db VARCHAR(100),
    status_antigo VARCHAR(20),
    status_novo VARCHAR(20)
);

-- triger
DELIMITER //

CREATE TRIGGER TRG_Auditoria_Status_Reserva
AFTER UPDATE ON Reserva
FOR EACH ROW
BEGIN
    IF OLD.status_pagamento <> NEW.status_pagamento THEN
        INSERT INTO LOG_Mudanca_Status_Reserva
            (id_reserva, usuario_db, status_antigo, status_novo)
        VALUES
            (NEW.id_reserva, USER(), OLD.status_pagamento, NEW.status_pagamento);
    END IF;
END //

DELIMITER ;
-- Justificativa trigger 2: Pra uma agencia de viagens, é importante saber quando e quem alterou um status de pagamento. É crucial para o controle financeiro e prevenir as fraudes.