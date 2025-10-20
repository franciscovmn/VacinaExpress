# VacinaExpress

Aplicação de console em Java que gerencia pessoas, vacinas e vacinações usando DB4O (banco de objetos).

## Visão geral
- Modelo de domínio:
  - [`modelo.Pessoa`](src/modelo/Pessoa.java) — [src/modelo/Pessoa.java](src/modelo/Pessoa.java)
  - [`modelo.Vacina`](src/modelo/Vacina.java) — [src/modelo/Vacina.java](src/modelo/Vacina.java)
  - [`modelo.Vacinacao`](src/modelo/Vacinacao.java) — [src/modelo/Vacinacao.java](src/modelo/Vacinacao.java)
  - [`modelo.Localizacao`](src/modelo/Localizacao.java) — [src/modelo/Localizacao.java](src/modelo/Localizacao.java)
- Acesso ao banco:
  - [`util.Db4oUtil`](src/util/Db4oUtil.java) — [src/util/Db4oUtil.java](src/util/Db4oUtil.java)  
    O arquivo de banco por padrão é `vacina_express.db4o` (raiz do projeto) — [vacina_express.db4o](vacina_express.db4o)
- Aplicações de console:
  - [`appconsole.Cadastrar`](src/appconsole/Cadastrar.java) — [src/appconsole/Cadastrar.java](src/appconsole/Cadastrar.java)
  - [`appconsole.Listar`](src/appconsole/Listar.java) — [src/appconsole/Listar.java](src/appconsole/Listar.java)
  - [`appconsole.Consultar`](src/appconsole/Consultar.java) — [src/appconsole/Consultar.java](src/appconsole/Consultar.java)
  - [`appconsole.Alterar`](src/appconsole/Alterar.java) — [src/appconsole/Alterar.java](src/appconsole/Alterar.java)
  - [`appconsole.Apagar`](src/appconsole/Apagar.java) — [src/appconsole/Apagar.java](src/appconsole/Apagar.java)

## Estrutura do repositório
- [.gitattributes](.gitattributes)
- [vacina_express.db4o](vacina_express.db4o)
- [libs/](libs/) — dependências (coloque aqui os JARs do DB4O)
- [src/](src/)
  - [src/appconsole/](src/appconsole/)
    - [Alterar.java](src/appconsole/Alterar.java)
    - [Apagar.java](src/appconsole/Apagar.java)
    - [Cadastrar.java](src/appconsole/Cadastrar.java)
    - [Consultar.java](src/appconsole/Consultar.java)
    - [Listar.java](src/appconsole/Listar.java)
  - [src/modelo/](src/modelo/)
    - [Localizacao.java](src/modelo/Localizacao.java)
    - [Pessoa.java](src/modelo/Pessoa.java)
    - [Vacina.java](src/modelo/Vacina.java)
    - [Vacinacao.java](src/modelo/Vacinacao.java)
  - [src/util/Db4oUtil.java](src/util/Db4oUtil.java)

## Pré-requisitos
- Java JDK (Ubuntu 24.04 no dev container).
- JARs do DB4O no diretório `libs/` (ex.: `libs/db4o-*.jar`).  
  Coloque os JARs em [libs/](libs/).

## Compilar
Abra um terminal na raiz do projeto e execute:
```bash
mkdir -p bin
javac -cp "libs/*" -d bin $(find src -name "*.java")