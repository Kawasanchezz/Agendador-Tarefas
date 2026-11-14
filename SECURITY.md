# Segurança

## Versões suportadas
Apenas a versão em `main`. Projeto acadêmico, sem garantia de suporte.

## Como reportar uma vulnerabilidade
Não abra uma issue pública. Use o recurso **Report a vulnerability** (Security Advisories) deste repositório no GitHub. Respondo em até 7 dias e coordeno a correção antes de qualquer divulgação.

## Modelo de segurança
- Só o **bff** deve ser acessível pela rede. Ele valida o JWT (HS256, expiração configurável) e envia o e-mail do usuário aos serviços internos no header `X-Usuario-Email`.
- `usuario` e `tarefas` **não autenticam** quem os chama: quem alcançá-los diretamente pode se passar por qualquer usuário. Mantenha-os em rede privada (ou adicione mTLS/token de serviço antes de expô-los).
- Senhas: BCrypt. Segredos (JWT, bancos) só por variáveis de ambiente, sem valor padrão.
- Login: limite de 5 falhas por e-mail e 20 por IP a cada 15 minutos (HTTP 429); resposta de login com tempo equalizado para e-mail inexistente.
- Cada usuário só lê, altera ou apaga os próprios dados (perfil, endereços, telefones, tarefas).
- Entradas validadas com tamanho máximo; listagem de tarefas limitada a 500 itens.

## Limitações conhecidas
| Limitação | Impacto | Mitigação sugerida |
|---|---|---|
| Serviços internos confiam no header `X-Usuario-Email` | Acesso direto a eles equivale a controle total | Rede privada; mTLS ou token de serviço |
| JWT não é revogável; token de conta excluída vale até expirar | Conta excluída ainda pode criar tarefas até o token expirar | Expiração curta, refresh token e lista de revogação |
| Trocar a senha não exige a senha atual | Quem roubar um token válido pode trocar a senha | Exigir `senhaAtual` (mudança de contrato da API, ainda não aplicada) |
| Limite de login em memória, por instância | Não vale com várias instâncias do BFF; atrás de proxy o IP visto é o do proxy | Redis ou rate limit no gateway; `server.forward-headers-strategy` |
| Sem limite global de requisições nem de tamanho de corpo | Abuso volumétrico | Gateway/WAF e limites do servidor |
| Cadastro revela se um e-mail já existe (409) | Enumeração de contas | Aceito por usabilidade; fluxo de confirmação por e-mail |
| `ddl-auto: update` | Evolução de schema sem controle | Flyway |
| Swagger UI público | Expõe o contrato da API | Desativar em produção (`springdoc.swagger-ui.enabled=false`) |
| Sem TLS entre serviços e bancos | Tráfego em claro | TLS em produção |
