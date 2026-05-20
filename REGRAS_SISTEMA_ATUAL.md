# Regras Extraidas do Sistema Atual

Origem analisada: `c:\desenvolvimento\repository\intellij\git\arena-match`

## Regras Implementadas

1. O cadastro de time exige senha obrigatoria.
2. A senha e a confirmacao de senha devem ser iguais.
3. A senha e armazenada com BCrypt.
4. O e-mail do time deve ser unico.
5. O CPF do responsavel deve ser unico.
6. O cadastro bloqueia e-mail ja cadastrado com erro de conflito.
7. O cadastro bloqueia CPF ja cadastrado com erro de conflito.
8. O cadastro busca endereco por CEP usando a integracao com Brasil API.
9. O cadastro busca latitude e longitude pelo endereco usando a integracao com Google Maps.
10. O endereco usado para geolocalizacao combina logradouro, numero e cidade.
11. O cadastro registra automaticamente a data de criacao do time.
12. O time possui um responsavel com nome, CPF, e-mail, senha e WhatsApp.
13. O time possui dados proprios como nome, mando de campo, taxa de jogo, escudo e endereco.
14. O mando de campo aceita os valores de negocio `MANDO` ou `VISITANTE`.
15. Quando o time nao esta como `MANDO`, a taxa de jogo e limpa no cadastro.
16. Quando o time esta como `MANDO`, a taxa de jogo informada e armazenada.
17. Disponibilidades de agenda sao opcionais no cadastro do time.
18. Quando uma disponibilidade e informada, dia da semana, categoria, hora inicial e hora final sao obrigatorios.
19. Nao e permitido cadastrar mais de uma disponibilidade para o mesmo dia da semana e categoria no mesmo time.
20. Horarios de disponibilidade devem estar em formato aceito por `LocalTime`, como `HH:mm`.
21. A hora final da disponibilidade deve ser maior que a hora inicial.
22. Times com mando de campo `MANDO` podem cadastrar intervalos de disponibilidade de no maximo 2 horas.
23. As disponibilidades sao vinculadas ao time cadastrado.
24. As categorias validas sao `ESPORTE`, `MESCLADO`, `VETERANO_35`, `VETERANO_40`, `MASTER` e `SUB20`.
25. A categoria `ESPORTE` tem descricao `Esporte (Livre)`.
26. A categoria `MESCLADO` tem descricao `Mesclado Esp.+Vet.(35+)`.
27. A categoria `VETERANO_35` tem descricao `Veterano (35+)`.
28. A categoria `VETERANO_40` tem descricao `Veterano (40+)`.
29. A categoria `MASTER` tem descricao `Master (50+)`.
30. A categoria `SUB20` tem descricao `Sub-20`.
31. O sistema aceita conversao de categoria pelo nome tecnico do enum ou pela descricao.
32. Os valores legados `Esporte`, `Veterano` e `Master` sao convertidos para `ESPORTE`, `VETERANO_35` e `MASTER`.
33. Categoria invalida bloqueia a conversao.
34. O login exige e-mail obrigatorio.
35. O login exige e-mail em formato valido.
36. O login exige senha obrigatoria.
37. A autenticacao busca o time pelo e-mail informado.
38. A autenticacao compara a senha digitada com o hash BCrypt salvo.
39. Login com e-mail inexistente ou senha incorreta falha com mensagem de usuario ou senha invalidos.
40. A busca de adversarios exige um time logado existente.
41. A busca considera apenas outros times, excluindo o proprio time logado.
42. A busca considera apenas times que possuem disponibilidade cadastrada.
43. A distancia entre times e calculada pelas coordenadas geograficas.
44. Quando nao ha latitude em algum dos times comparados, a distancia considerada fica como `0.0`.
45. A busca filtra adversarios por distancia maxima em quilometros.
46. A distancia maxima padrao da busca e `10.0 km`.
47. A busca filtra por cidade quando a cidade e informada.
48. O filtro de cidade ignora diferenca entre maiusculas e minusculas.
49. A busca filtra por dia da semana quando o filtro e informado e diferente de `Qualquer`.
50. A busca filtra por categoria quando a categoria e informada.
51. Cada disponibilidade compativel de um adversario gera um resultado de busca.
52. O resultado de busca informa time, categoria, dia, horario, distancia, mando de campo e data exata.
53. A data exata do resultado e a proxima data do dia da semana da disponibilidade, incluindo hoje quando coincidir.
54. A distancia exibida nos resultados e arredondada para uma casa decimal.
55. Os resultados de busca sao ordenados pela menor distancia.
56. Dias da semana aceitos para calculo sao domingo, segunda, terca, quarta, quinta, sexta e sabado.
57. O sistema aceita `terca` e `sabado` sem acento nas regras de dia da semana.
58. Dia da semana invalido bloqueia a operacao que depende dele.
59. Ao convidar um adversario pela busca, o time logado e enviado como mandante.
60. Ao convidar um adversario pela busca, o adversario selecionado e enviado como visitante.
61. O convite usa a data exata e o horario da disponibilidade retornada pela busca.
62. O envio de convite exige mandante e visitante.
63. Nao e permitido convidar o proprio time.
64. O envio de convite exige data do jogo, hora inicial e hora final.
65. Nao e permitido enviar convite para data passada.
66. Nao e permitido repetir convite entre o mesmo mandante e visitante, na mesma data, quando ja existe jogo `PENDENTE` ou `CONFIRMADO`.
67. O envio de convite exige que o mandante exista.
68. O envio de convite exige que o visitante exista.
69. O visitante precisa ter disponibilidade no mesmo dia da semana do jogo.
70. O visitante precisa ter disponibilidade com a mesma hora inicial do convite.
71. O visitante precisa ter disponibilidade com a mesma hora final do convite.
72. Convite sem disponibilidade compativel do visitante e bloqueado.
73. Todo convite criado inicia com status `PENDENTE`.
74. Os status possiveis de jogo sao `PENDENTE`, `CONFIRMADO`, `RECUSADO` e `CANCELADO`.
75. Apenas convites com status `PENDENTE` podem ser respondidos.
76. A resposta de convite so aceita novo status `CONFIRMADO` ou `RECUSADO`.
77. Responder convite com outro status e bloqueado.
78. Ao aceitar um convite, o jogo passa para `CONFIRMADO`.
79. Ao recusar um convite, o jogo passa para `RECUSADO`.
80. A agenda lista jogos em que o time participa como mandante ou visitante.
81. A listagem de jogos do time e ordenada pela data do jogo em ordem crescente.
82. A agenda monta periodos de 15 dias.
83. O periodo inicial da agenda comeca na data atual.
84. A agenda permite navegar para o periodo anterior de 15 dias.
85. A agenda permite navegar para o proximo periodo de 15 dias.
86. A agenda permite voltar para o periodo atual.
87. A agenda marca dia com jogo `CONFIRMADO` como dia de jogo.
88. A agenda marca dia com jogo `PENDENTE` como dia com convite.
89. Quando um dia tem jogo confirmado e convite pendente, o marcador de jogo confirmado tem prioridade.
90. A agenda exibe no detalhe do dia apenas jogos `CONFIRMADO` ou `PENDENTE`.
91. Se nao houver time logado na sessao, a agenda redireciona para o login.
92. A resposta de convite pela agenda recarrega os dados apos sucesso.
93. Jogo armazena mandante, visitante, data, hora inicial, hora final e status.
94. Um jogo sempre deve ter mandante.
95. Um jogo sempre deve ter visitante.
96. Um jogo sempre deve ter data.
97. Um jogo sempre deve ter hora inicial.
98. Um jogo sempre deve ter hora final.
99. Um jogo sempre deve ter status.
100. Disponibilidade sempre deve ter time, dia da semana, hora inicial, hora final e categoria.

## Regras do Modelo Ainda Nao Implementadas Aqui

101. Nao ha regra implementada de ativacao de conta por e-mail.
102. Nao ha regra implementada de recuperacao de senha.
103. Nao ha regra implementada de plano, trial, pagamento ou assinatura.
104. Nao ha regra implementada de antecedencia minima parametrizada para busca ou convite.
105. Nao ha regra implementada para bloquear busca quando ha placar pendente.
106. Nao ha regra implementada de resultado, placar, confirmacao de placar ou contestacao.
107. Nao ha regra implementada de ranking.
108. Nao ha regra implementada de cancelamento de jogo confirmado.
109. Nao ha regra implementada de edicao de time ou bloqueio de edicao por jogos futuros.
110. Nao ha regra implementada de liga.
