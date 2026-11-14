# Agendador de Tarefas

Backend em microsserviços com um **BFF** como único ponto de entrada.

```
cliente ──► bff :8080 (JWT, Swagger, ViaCEP)
              ├──► usuario :8081 ──► PostgreSQL
              └──► tarefas :8082 ──► MongoDB
```

- **bff**: cadastro/login, valida o JWT e centraliza a segurança. Repassa ao `usuario` e `tarefas` o e-mail do token no header `X-Usuario-Email`.
- **usuario**: usuário, endereços e telefones (PostgreSQL). Guarda a senha com BCrypt.
- **tarefas**: tarefas (MongoDB), sempre filtradas pelo dono.

`usuario` e `tarefas` não têm autenticação própria: **não os exponha fora da sua rede**. Só o BFF deve alcançá-los.

## Requisitos
Java 21, Maven 3.9+, Docker.

## Subir
```bash
# 1. bancos
cp .env.example .env
docker compose up -d

# 2. variáveis (bash; no PowerShell use $env:NOME="valor")
export JWT_SECRET="uma-chave-com-pelo-menos-32-caracteres-aqui"

# 3. serviços (um terminal cada)
mvn -f usuario/pom.xml spring-boot:run
mvn -f tarefas/pom.xml spring-boot:run
mvn -f bff/pom.xml spring-boot:run
```
Variáveis opcionais (valores padrão em `.env.example`): `DB_URL`, `DB_USER`, `DB_PASSWORD`, `MONGO_URI`, `USUARIO_URL`, `TAREFAS_URL`, `JWT_EXPIRATION_MS`.

Swagger: http://localhost:8080/swagger-ui.html (botão **Authorize** → cole o token).

Testes: `mvn -f usuario/pom.xml test` (idem `tarefas` e `bff`).

## Endpoints (todos pelo BFF, :8080)
| Método | Rota | Auth |
|---|---|---|
| POST | /usuarios | não |
| POST | /usuarios/login | não |
| GET | /usuarios?email= (sem email = o seu) | sim |
| PUT | /usuarios | sim |
| DELETE | /usuarios/{email} | sim |
| POST / PUT | /usuarios/endereco (PUT com ?id=) | sim |
| POST / PUT | /usuarios/telefone (PUT com ?id=) | sim |
| POST | /tarefas | sim |
| GET | /tarefas?dataInicial=&dataFinal= (ISO, opcionais) | sim |
| GET | /tarefas/{id} | sim |
| PATCH | /tarefas?id=&status= | sim |
| DELETE | /tarefas?id= | sim |
| GET | /endereco/{cep} (ViaCEP) | sim |

Status: `PENDENTE`, `EM_ANDAMENTO`, `CONCLUIDA`, `CANCELADA`.

## Exemplos
```bash
curl -X POST localhost:8080/usuarios -H 'Content-Type: application/json' \
  -d '{"nome":"Ana","email":"ana@mail.com","senha":"segredo1"}'

TOKEN=$(curl -s -X POST localhost:8080/usuarios/login -H 'Content-Type: application/json' \
  -d '{"email":"ana@mail.com","senha":"segredo1"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')

curl -X POST localhost:8080/tarefas -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"nome":"Reunião","descricao":"Sprint","dataEvento":"2030-01-15T10:00:00"}'

curl "localhost:8080/tarefas?dataInicial=2030-01-01T00:00:00&dataFinal=2030-12-31T23:59:59" \
  -H "Authorization: Bearer $TOKEN"

curl -X PATCH "localhost:8080/tarefas?id=<ID>&status=CONCLUIDA" -H "Authorization: Bearer $TOKEN"

curl localhost:8080/endereco/01001000 -H "Authorization: Bearer $TOKEN"

curl -X POST localhost:8080/usuarios/endereco -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"rua":"Praça da Sé","numero":100,"cidade":"São Paulo","estado":"SP","cep":"01001000"}'
```

## Erros
Formato único: `{"timestamp","status","mensagem","path"}`.
400 validação · 401 token ausente/inválido ou login errado · 403 acesso a dados de outro usuário · 404 não encontrado/CEP inexistente · 409 e-mail duplicado · 502 falha do ViaCEP ou de serviço interno · 503 serviço interno fora do ar.

## Limitações conhecidas
- Excluir a conta não apaga as tarefas do usuário (ficam órfãs no MongoDB).
- O JWT não é revogável antes de expirar (sem refresh token nem logout).
- `ddl-auto: update` é prático para desenvolvimento; em produção use migrações (Flyway).
