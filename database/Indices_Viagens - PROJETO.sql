CREATE INDEX idx_pacote_preco
ON Pacote_viagem (preco_total DESC);

CREATE INDEX idx_reserva_status
ON Reserva (status_pagamento);