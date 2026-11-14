# Segurança

## Versões suportadas
Apenas a versão em `main`. Projeto acadêmico, sem garantia de suporte.

## Como reportar uma vulnerabilidade
Não abra uma issue pública. Use o recurso **Report a vulnerability** (Security Advisories) deste repositório no GitHub. Respondo em até 7 dias e coordeno a correção antes de qualquer divulgação.

## Modelo de segurança
- Só o **bff** deve ser acessível pela rede. Ele valida o JWT (HS256, expiração configurável) e envia o e-mail do usuário aos serviços internos no header `X-Usuario-Email`.
- `usuario` e `tarefas` só aceitam chamadas com o header `X-Internal-Token` (segredo compartilhado com o BFF, mínimo 32 caracteres, comparação em tempo constante). Sem ele respondem 401. Ainda assim, mantenha-os em rede privada: quem obtiver o token interno pode se passar por qualquer usuário.
- Senhas: BCrypt. Segredos (JWT, token interno, bancos) só por variáveis de ambiente, sem valor padrão.
- Login: limite de 5 falhas por e-mail e 20 por IP a cada 15 minutos (HTTP 429); tempo de resposta equalizado para e-mail inexistente.
- Trocar a senha exige a senha atual, e tentativas erradas contam no mesmo limite do login.
- Logout, troca de senha e exclusão de conta invalidam todos os tokens emitidos antes.
- Cada usuário só lê, altera ou apaga os próprios dados (perfil, endereços, telefones, tarefas).
- Entradas validadas com tamanho máximo; listagem de tarefas limitada a 500 itens; no máximo 10 endereços, 10 telefones e 1000 tarefas por usuário (409 ao exceder).
- Bancos publicados apenas em `127.0.0.1` no Docker Compose.

## Verificações executadas
Testes automatizados (unitários e MockMvc), teste de ponta a ponta com PostgreSQL, MongoDB e ViaCEP reais, Gitleaks (histórico e árvore), Semgrep (`p/java`, `p/owasp-top-ten`, `p/github-actions`) e Trivy sobre os JARs gerados. CodeQL roda no CI.

## Limitações conhecidas
| Limitação | Impacto | Mitigação sugerida |
|---|---|---|
| Revogação de tokens e limite de login ficam em memória, por instância | Reiniciar o BFF devolve a validade aos tokens revogados; com várias instâncias não há compartilhamento; atrás de proxy o IP visto é o do proxy | Redis; `server.forward-headers-strategy` |
| Token interno único e estático | Vazamento permite agir como qualquer usuário | Rotação periódica; mTLS ou tokens por serviço |
| Sem limite global de requisições nem de tamanho de corpo | Abuso volumétrico | Gateway/WAF e limites do servidor |
| Cadastro revela se um e-mail já existe (409) | Enumeração de contas | Aceito por usabilidade; fluxo de confirmação por e-mail |
| `ddl-auto: update` | Evolução de schema sem controle | Flyway |
| Swagger UI público | Expõe o contrato da API | `springdoc.swagger-ui.enabled=false` em produção |
| Sem TLS entre serviços e bancos | Tráfego em claro | TLS em produção |
| CVE-2026-47884 e CVE-2026-47890 (`spring-webmvc` 6.2.19) | Sem correção na linha 6.2; só em 7.x (Spring Boot 4, mudança maior). O projeto não usa `XsltView` nem Server-Sent Events, os componentes afetados | Migrar para Boot 4 quando for viável; acompanhar um backport na linha 6.2 |
| Overrides de versão no `pom.xml` (Jackson, Tomcat, driver PostgreSQL) | Corrigem CVEs posteriores ao Spring Boot 3.5.16 | Remover quando o Boot incorporar as versões |
