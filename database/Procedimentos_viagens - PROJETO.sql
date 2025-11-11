DELIMITER //

CREATE PROCEDURE SP_Sincronizar_Status_Reserva_Pelo_Pagamento(
    IN p_id_reserva INT
)
BEGIN
    DECLARE v_status_parcela VARCHAR(20);
    DECLARE v_reserva_esta_paga INT DEFAULT 1;
    DECLARE done INT DEFAULT FALSE;

    DECLARE cur_parcelas CURSOR FOR
        SELECT status_parcela
        FROM Pagamento
        WHERE id_reserva = p_id_reserva;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cur_parcelas;

    read_loop: LOOP
        FETCH cur_parcelas INTO v_status_parcela;
        IF done THEN
            LEAVE read_loop;
        END IF;
        IF v_status_parcela = 'Pendente' THEN
            SET v_reserva_esta_paga = 0;
            LEAVE read_loop;
        END IF;

    END LOOP;

    CLOSE cur_parcelas;
    IF v_reserva_esta_paga = 1 THEN
        UPDATE Reserva
        SET status_pagamento = 'Pago'
        WHERE id_reserva = p_id_reserva;
    ELSE
        UPDATE Reserva
        SET status_pagamento = 'Pendente'
        WHERE id_reserva = p_id_reserva;
    END IF;

END //

DELIMITER ;

-- justificativa procedimento 1: Utiliza um cursor pra verificar o status de todas as parcelas de uma reserva, depois atualiza o status da reserva principal. 


DELIMITER //

CREATE PROCEDURE SP_Atualizar_Status_Pagamento(
    IN p_id_reserva INT,
    IN p_novo_status VARCHAR(20)
)
BEGIN
    UPDATE Reserva
    SET status_pagamento = p_novo_status
    WHERE id_reserva = p_id_reserva;
END //

DELIMITER ;

-- justificativa procedimento 2: atualiza o status de pagamento.