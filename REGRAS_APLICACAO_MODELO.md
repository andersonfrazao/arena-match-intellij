# Regras Extraidas da Aplicacao Modelo

Origem analisada: `c:\desenvolvimento\repository\intellij\git\arenamatch`

## Regras Aproveitaveis

1. O cadastro valida CPF, quando a flag `arenamatch.validation.cpf-enabled` esta ativa.
2. CPF e celular sao salvos sem mascara.
3. E-mail deve ser unico.
4. CPF deve ser unico.
5. Senha de cadastro deve ter no minimo 6 caracteres.
6. Senha e armazenada com BCrypt.
7. Novo usuario entra como `REPRESENTANTE`.
8. Novo usuario comeca em plano `TRIAL`.
9. O trial dura uma quantidade parametrizada de dias, padrao `90`.
10. O aceite dos termos e gravado no cadastro.
11. A conta pode exigir ativacao por e-mail, controlada por `arenamatch.validation.email-activation-enabled`.
12. Quando exige ativacao, o usuario fica `PENDENTE_ATIVACAO`.
13. Codigo de ativacao tem 5 digitos e expira em 15 minutos.
14. Login bloqueia usuario com conta pendente de ativacao.
15. Recuperacao de senha usa codigo de 5 digitos com validade de 15 minutos.
16. Ao redefinir senha, o codigo de recuperacao e apagado.
17. Cadastro/edicao de time busca coordenadas por CEP quando latitude/longitude nao vem preenchidas.
18. Time mandante deve cadastrar horarios com exatamente 2 horas na aplicacao modelo.
19. Ao editar cadastro, se o time tiver jogo futuro `PENDENTE` ou `AGENDADO`, a edicao e bloqueada.
20. Ao editar disponibilidade, a agenda antiga do time e removida e recriada.
21. Categoria invalida ou vazia na disponibilidade bloqueia atualizacao.
22. Busca de adversarios nao permite data passada.
23. Busca/agendamento respeita antecedencia minima parametrizada, padrao `3` dias.
24. Busca so retorna adversarios se o proprio time tambem joga no dia escolhido.
25. Busca so retorna adversarios com mando de campo oposto ao time logado.
26. Busca exclui o proprio time.
27. Busca filtra por raio geografico.
28. Plano `BASICO` tem raio maximo de busca parametrizado, padrao `10 km`.
29. Busca exclui times que ja possuem jogo `AGENDADO` naquela data.
30. Busca informa se ja existe convite pendente entre os times na data.
31. Antes de buscar ou criar desafio, o time nao pode ter placar pendente.
32. Um desafio nao pode ser criado para data anterior a antecedencia minima.
33. Um time desafiado nao pode ter jogo confirmado na mesma data.
34. Nao pode existir convite pendente com o mesmo adversario na mesma data.
35. O desafiante precisa ter agenda cadastrada para o dia escolhido.
36. O desafiado tambem precisa ter agenda cadastrada para o dia escolhido.
37. O mandante da partida e definido por quem tem mando de campo; se ambos tem ou ambos nao tem, o desafiado vira mandante.
38. O horario oficial da partida vem da agenda do mandante.
39. Se houver categoria no desafio, tenta usar a agenda do mandante daquela categoria.
40. Convite comeca com status `PENDENTE`.
41. Ao aceitar convite, se algum dos times ja tiver jogo `AGENDADO` na data, o convite e cancelado e a aceitacao falha.
42. Somente convites `PENDENTE` podem ser removidos diretamente.
43. Cancelamento so pode ser solicitado para jogo `AGENDADO`.
44. So participante da partida pode solicitar cancelamento.
45. Motivo de cancelamento e obrigatorio.
46. Motivo de cancelamento tem limite de 350 caracteres.
47. Cancelamento respeita antecedencia minima parametrizada, padrao `3` dias.
48. Quem solicitou cancelamento nao pode responder a propria solicitacao.
49. Aceitar cancelamento muda a partida para `CANCELADO`.
50. Recusar cancelamento retorna a partida para `AGENDADO` e limpa motivo/solicitante.
51. Depois do jogo, placar pendente bloqueia busca e criacao de novos desafios.
52. Quando um time informa placar, o status vira `AGUARDANDO_CONFIRMACAO`.
53. O adversario deve confirmar ou contestar o placar.
54. Placar sem resposta e confirmado automaticamente apos prazo parametrizado, padrao `3` dias.
55. Placar confirmado atualiza estatisticas: jogos, gols pro, gols contra, vitorias, empates, derrotas e pontos.
56. Vitoria soma 3 pontos.
57. Empate soma 1 ponto.
58. Ranking so inclui times com pelo menos 1 partida jogada.
59. Ranking so inclui usuarios `PRO` pagos ou `TRIAL` ainda valido.
60. Ranking ordena por vitorias, saldo de gols e gols pro.
61. Agenda mostra estados distintos: jogo confirmado futuro, convite pendente, cancelado/solicitacao de cancelamento, placar pendente e jogo realizado.
62. Endereco do jogo e o endereco do time dono do campo.
63. Valor de taxa exibido vem do time dono do campo.
64. Trial expirado converte usuario para plano `BASICO`, pagamento `EXPIRADO` e assinatura `VENCIDO`.

## Regras de Liga Para Depois

65. Existem regras de criacao de liga, convite para liga, solicitacao de entrada, chat de liga e notificacoes de liga.
66. Como ligas foram removidas da UI atual, essas regras devem ficar fora da proxima etapa.
