# EstudoRinha

Sim, esse projeto está sendo desenvolvido MUITO tempo depois do evento ter sido finalizado mas minha intenção é apenas estudar e tentar fazer minha "própria" implementação.

Além disso, no ponto do desenvolvimento desse projeto de estudo eu já tive conhecimento sobre algumas estratégias e análises que a comunidade BR fez em cima do desafio, portanto sim, é possível que minha implementeção tenha viés de truques de outras pessoas, mesmo eu estando escrevendo meu próprio código "do zero". Vale relembrar que meu objetivo principal é exercitar e ver até onde consigo fazer minha implementação sozinho, sem copiar e colar de outros projetos.

Por fim, o arquivo [ArrayColumnImpl.kt](src%2Fmain%2Fkotlin%2FArrayColumnImpl.kt) foi encontrado na internet por mim. Ele basicamente serve pra "adicionar suporte a colunas de tipo array ao Exposed", o que é compatível com o banco de dados que estou usando no projeto, o PostgreSQL. Além disso, ao que observei, esse código é de autoria do lendário [MrPowerGamerBR](https://www.youtube.com/@MrPowerGamerBR), que provavelmente deve ter usado isso no seu projeto [Loritta](https://github.com/LorittaBot/Loritta). Também cheguei a ver o próprio MrPowerGamerBR compartilhando um snippet de algo semelhante em uma [issue](https://github.com/JetBrains/Exposed/issues/150#issuecomment-625763361) do projeto oficial do [Exposed](https://github.com/JetBrains/Exposed).  

Aliás, já ia esquecendo, mas este projeto está_sendo/foi feito/comentado em LIVE no YouTube! Se quiserem, podem acompanhar a Live 1 neste [link](https://www.youtube.com/watch?v=GtX6Xm-M0EE). :)

# Outros detalhes

Eu tomei a liberdade de montar um script [devflow.bat](devflow.bat) básico para executar alguns comandos relacionados ao fluxo de desenvolvimento, sobretudo usando o Docker. Deixei comentários nele que dá pra ter uma ideia do que ele exatamente está
fazendo.