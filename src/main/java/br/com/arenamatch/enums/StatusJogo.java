package br.com.arenamatch.enums;

public enum StatusJogo {
    PENDENTE,   // Convite enviado, aguardando resposta
    CONFIRMADO, // Adversário aceitou
    RECUSADO,   // Adversário não aceitou
    CANCELADO   // Desmarcado após confirmação
}