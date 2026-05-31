# MaridoDeAluguel — App Cliente (Usuário)

## Visão Geral
Aplicativo Android (Jetpack Compose) para clientes solicitarem serviços de prestadores (o "Marido de Aluguel"). Este repositório contém o cliente para usuários finais (cadastro, login, solicitar serviço, escolher prestador, histórico, avaliações, gerenciamento de endereços e notificações). A comunicação com o backend acontece via Retrofit (chamadas REST) e parte do fluxo de autenticação usa AWS Cognito (signup/signin).

**Principais tecnologias:**
- Kotlin + Jetpack Compose (UI)
- Retrofit + OkHttp + Gson (network)
- AWS Cognito SDK (autenticação)
- EncryptedSharedPreferences (armazenamento de sessão)

---

## Estrutura do Projeto

### Localização principal
```
app/src/main/java/com/example/doesitusuario/
├── data/
│   ├── model/
│   │   └── ServiceModels.kt           # Modelos de dados (DTOs) para requisições/respostas da API
│   ├── network/
│   │   ├── ApiService.kt              # Endpoints REST (Retrofit) - definição de todas as rotas
│   │   ├── RetrofitClient.kt          # Configuração do Retrofit/OkHttp com interceptor de Authorization
│   │   ├── CognitoService.kt          # Integração com AWS Cognito (signup/signin direto)
│   │   ├── SessionManager.kt          # Armazenamento seguro da sessão (token, dados do usuário)
│   │   └── PreferencesManager.kt      # Preferências locais de notificações (flags simples)
│   ├── repository/
│   │   ├── UserRepository.kt          # Regras de negócio: autenticação, perfil, endereços, notificações
│   │   └── ServiceRepository.kt       # Regras de negócio: serviços, categorias, prestadores, pedidos, avaliações
│   └── notifications/
│       └── NotificationStore.kt       # Armazenamento local de notificações e sincronização com histórico
├── ui/
│   ├── navigation/
│   │   └── NavGraph.kt                # Rotas e fluxo de navegação entre telas
│   ├── screens/                       # Telas Compose organizadas por funcionalidade
│   │   ├── login/                     # LoginScreen, RegisterScreen, ForgotPasswordModal
│   │   ├── home/                      # HomeScreen (dashboard principal)
│   │   ├── orders/                    # OrderScreen (criar pedido), AvailableProvidersScreen, OrderDetailScreen
│   │   ├── history/                   # HistoryScreen (listar pedidos)
│   │   ├── notifications/             # NotificationScreen
│   │   ├── addresses/                 # AddressScreen, AddressFormScreen
│   │   ├── profiles/                  # ProfileScreen (dados do usuário)
│   │   ├── settings/                  # SettingScreen (preferências, logout)
│   │   ├── payments/                  # PaymentScreen (placeholder para futuro)
│   │   ├── rating/                    # Componentes de avaliação
│   │   └── login/                     # Componentes compartilhados (DarkTextField, ErrorBanner, SuccessBanner)
│   ├── viewmodel/                     # ViewModels que orquestram repositórios
│   │   ├── AuthViewModel.kt           # Login e registro
│   │   ├── ServiceRequestViewModel.kt # Carregamento de formulário para criar pedidos
│   │   ├── NotificationViewModel.kt   # Carregamento de notificações
│   │   ├── AddressViewModel.kt        # Carregamento de endereços
│   │   └── HomeViewModel.kt           # (TBD - dados da tela inicial)
│   └── theme/
│       └── Colors/Theme definitions
└── MainActivity.kt                    # Activity raiz - inicializa SessionManager e SetupNavGraph
```

### Modelos de dados principais (`ServiceModels.kt`)
- **Autenticação:**
  - `UserLoginProfileDTO` — { email, password }
  - `UserRegisterProfileDTO` — { name, email, password, phone, cpf, birthDate (dd/mm/yyyy), gender, role, address* }
  - `AuthResponse` — { token?, id, nome?, email?, tipo (role)?, telefone?, cpf?, data_nascimento?, genero?, cep?, rua?, numero?, bairro?, cidade?, estado?, rating? }

- **Categorias e Prestadores:**
  - `ServiceCategory` — { id, nome, basePrice, icon? }
  - `ProviderDTO` — { id, name, email?, rating?, ratingCount?, online (isOnline), specialtyPrice? }

- **Pedidos:**
  - `ServiceRequestCreate` — { categoryId, description, type (IMMEDIATE|SCHEDULED), scheduledAt?, preferredProviderId?, addressId? }
  - `ServiceRequestDTO` — { id, servico, cliente?, prestador?, data, valor, endereco?, descricao?, status, status_id?, minha_role?, nome_parte? }
  - `OrderListResponse` — { pedidos: List<ServiceRequestDTO> }

- **Endereços:**
  - `AddressDTO` — { id, titulo (tag)?, cep?, rua?, numero?, complemento?, referencia?, bairro?, cidade?, estado?, is_favorite (isDefault), formatado? }
  - `AddressListResponse` — { enderecos: List<AddressDTO> }

- **Notificações:**
  - `NotificationDTO` — { titulo, mensagem, dt_notificacao }
  - `NotificationListResponse` — { notificacoes: List<NotificationDTO> }

- **Avaliação:**
  - `RatingRequest` — { serviceRequestId, stars, comment }

- **Formulário de Solicitar Serviço:**
  - `ServiceRequestFormResponse` — { genero, mostrar_filtro_mulheres, enderecos: List<AddressDTO>, servicos: List<ServiceCategory> }

### Endpoints da API (`ApiService.kt`)
**Autenticação:**
- `POST api/auth/login` — Login (reservado para futuro; atualmente usa Cognito direto)
- `POST api/auth/register` — Cadastro (reservado para futuro; atualmente usa Cognito direto)
- `POST api/auth/forgot-password` — Recuperação de senha (em migração)
- `POST api/auth/verify-code` — Verificar código (em migração)
- `POST api/auth/reset-password` — Resetar senha (em migração)

**Usuário:**
- `GET usuario` — Obter dados do usuário logado
- `PUT api/users/me` — Atualizar perfil
- `PUT api/users/me/password` — Alterar senha
- `DELETE api/users/me` — Deletar conta

**Catégories:**
- `GET api/categories` — Listar categorias de serviço
- `GET solicitar-servico` — Obter formulário para criar pedido (inclui gênero, filtros, endereços e categorias)

**Prestadores:**
- `GET api/providers/for-category/{categoryId}?onlineOnly={bool}&onlyWomen={bool}` — Listar prestadores filtrando por categoria, disponibilidade e gênero

**Pedidos:**
- `POST api/requests` — Criar novo pedido
- `GET meus-pedidos?status={status}` — Listar pedidos do usuário (filtro opcional de status)
- `GET detalhes-pedido?id={id}` — Obter detalhes de um pedido específico
- `PUT api/requests/{id}/cancel` — Cancelar um pedido
- `PUT api/requests/{id}/confirm-finish` — Confirmar que o prestador terminou o serviço

**Avaliações:**
- `POST api/ratings` — Avaliar um serviço concluído

**Endereços:**
- `GET meus-enderecos` — Listar endereços do usuário
- `POST cadastrar-endereco` — Criar novo endereço
- `PUT atualizar-endereco?id={id}` — Atualizar endereço
- `PUT api/addresses/{id}/default` — Definir endereço como padrão
- `DELETE api/addresses/{id}` — Remover endereço

**Notificações:**
- `GET minhas-notificacoes` — Listar notificações do usuário

---

## Regras de Negócio Principais

### 1. Autenticação e Sessão
- **Cadastro (Register):** realizado via AWS Cognito (`CognitoService.signUp`), que exige:
  - Atributos padrão: nome (name), email (username), senha
  - Data de nascimento formatado como `YYYY-MM-DD`
  - Atributos customizados: cpf, custom:tipo_usuario (fixado como "CLIENTE"), custom:id_genero (mapeado: "Masculino"→"1", "Feminino"→"2", "Outros"→"3", outro→"4"), telefone, endereço (rua, numero, bairro, cidade, estado, cep)

- **Login:** realizado via Cognito (`CognitoService.signIn`)
  - Entrada: email e senha
  - Saída: idToken (reutilizado como bearer token)
  - Pós-login: app chama `GET usuario` para obter/atualizar dados completos do usuário

- **Armazenamento de Sessão:** `SessionManager` (singleton usando `EncryptedSharedPreferences`)
  - Dados armazenados: token, userId, userName, userEmail, userPhone, userCpf, userBirthDate, userGender, addressCep, addressStreet, addressNumber, addressNeighborhood, addressCity, addressState, rating
  - `SessionManager.isLoggedIn()` verifica se token não está vazio
  - `SessionManager.bearerToken()` retorna "Bearer <token>" para requisições
  - `SessionManager.clear()` limpa a sessão ao logout

- **Interceptor de Autorização:** `RetrofitClient` adiciona automaticamente o header `Authorization: Bearer <token>` a todas as requisições quando `SessionManager.token` estiver preenchido

### 2. Solicitação de Serviço
- **Fluxo:** Cliente seleciona categoria → escolhe modo (Agora | Agendar) → busca prestadores → seleciona prestador → cria pedido
  
- **Modo "Agora" (IMMEDIATE):** 
  - Apenas prestadores online são exibidos (`onlineOnly=true`)
  - Sem necessidade de agendamento prévio
  - Frete imediato do prestador se estiver na plataforma
  
- **Modo "Agendar" (SCHEDULED):**
  - Mostrados prestadores online E offline
  - Requer data (datepicker) e hora válidas (formato `HH:mm`)
  - Data/hora é convertida para ISO-8601 (`YYYY-MM-DDTHH:mm:00`)

- **Preço efetivo:** `provider.specialtyPrice` se existir; caso contrário, `ServiceCategory.basePrice`

- **Preferências de filtro:**
  - O formulário (`GET solicitar-servico`) fornece flag `mostrar_filtro_mulheres`
  - Se true, usuário pode filtrar apenas prestadores mulheres (`onlyWomen=true`)

- **Criação de pedido:**
  - Entrada: `ServiceRequestCreate` com categoryId, description, type, scheduledAt, preferredProviderId, addressId
  - Saída esperada: `ServiceRequestDTO` com id, status inicial (usualmente "PENDENTE"), data, valor

### 3. Histórico e Status de Pedidos
- **Status possíveis:** PENDENTE, AGENDADO, EM ANDAMENTO, CONCLUIDO, CANCELADO, REFUSED
  
- **Filtros na UI:** TODOS, PENDENTE, AGENDADO, EM ANDAMENTO, CONCLUIDO, CANCELADO
  - `GET meus-pedidos?status={status}` (status é opcional; se omitido, retorna todos)
  
- **Sincronização de Notificações:** `NotificationStore.syncFromHistory` converte pedidos em notificações:
  - ACCEPTED/AGENDADO → "Pedido aceito! ✅" + nome do prestador
  - COMPLETED/CONCLUIDO → "Serviço concluído! 🎉" + botão para avaliar
  - REFUSED → "Agendamento recusado ❌"
  - CANCELLED/CANCELADO → "Pedido cancelado"
  - Cada notificação tem `id` determinístico `{pedidoId}_{STATUS}` para evitar duplicação
  - Flag `read` é preservada ao re-sincronizar

### 4. Avaliação de Serviço
- **Pré-requisito:** Pedido deve estar em status COMPLETED/CONCLUIDO
  
- **Dados da avaliação:** { serviceRequestId, stars (Int 1-5), comment (String) }
  
- **Validações:**
  - Backend pode rejeitar se usuário já avaliou (tratado como erro "Você já avaliou este serviço")
  - App armazena localmente `SessionManager.ratedRequestIds` para evitar UI confusa

- **POST /api/ratings** retorna sucesso (200) ou erro com mensagem

### 5. Endereços
- **Operações:**
  - Listar: `GET meus-enderecos`
  - Criar: `POST cadastrar-endereco`
  - Atualizar: `PUT atualizar-endereco?id={id}`
  - Definir como padrão: `PUT api/addresses/{id}/default`
  - Remover: `DELETE api/addresses/{id}`

- **Campo `is_favorite`:** booleano que marca o endereço padrão
  - Ao criar pedido, o endereço padrão é pré-selecionado automaticamente
  - Se nenhum é marcado como favorito, usa-se o primeiro da lista

- **Validações locais:** CEP (8 dígitos), campos de texto não-vazios

### 6. Perfil do Usuário
- **Dados obtidos via `GET usuario`:** id, nome, email, telefone, cpf, data_nascimento, genero, endereço, rating
  
- **Atualização:** `PUT api/users/me` com `Map<String, String>`
  
- **Alteração de senha:** `PUT api/users/me/password` requer senha atual e nova
  - Validações backend: senha atual incorreta, comprimento insuficiente

- **Deleção de conta:** `DELETE api/users/me` (irreversível)

### 7. Notificações
- **Armazenamento local:** `NotificationStore` mantém lista `mutableStateListOf<NotificationItem>`
  
- **Sincronização:**
  - Backend fornece via `GET minhas-notificacoes` (retorna `NotificationListResponse` com lista `NotificationDTO`)
  - App também gera notificações derivadas do histórico de pedidos
  
- **Estado de leitura:** cada notificação tem flag `read`
  - `markAllRead()` marca todas como lidas
  - `markRead(id)` marca uma específica como lida
  - Ao re-sincronizar, preserva estado `read` (não marca como não-lida ao atualizar duplicata)

### 8. Preferências de Notificação
- **`PreferencesManager`** (atualmente em memória, singleton):
  - `pushEnabled` (default: true)
  - `emailEnabled` (default: true)
  - `smsEnabled` (default: false)
  - `whatsappEnabled` (default: true)
  - **Nota:** Atualmente não são persistidas entre execuções; para produção, mover para `EncryptedSharedPreferences`

---

## Fluxos Detalhados

### FLUXO 1: Autenticação e Login

**Arquivos relevantes:**
- `CognitoService.kt` (signIn)
- `UserRepository.login`
- `AuthViewModel.login`
- `LoginScreen.kt`

**Entradas (UI):**
- Email (string não-vazia, validação de formato email)
- Senha (string não-vazia)

**Validações locais:**
- Email e senha não vazios (botão habilitado condicionalmente)

**Fluxo de execução:**
1. Usuário preenche email e senha
2. Clica em "Acessar conta" (`LoginScreen.kt` chama `viewModel.login(email, senha)`)
3. `AuthViewModel.login` seta `_isLoading = true` e chama `UserRepository.login`
4. `UserRepository.login` → `CognitoService.signIn(email, password)`
5. Cognito autentica:
   - Gera `authParameters` com USERNAME, PASSWORD, SECRET_HASH (calculado via HMAC-SHA256)
   - Envia `InitiateAuthRequest` com `authFlow = UserPasswordAuth`
   - Retorna `authenticationResult` contendo `idToken`

**Resultados esperados:**

*Em sucesso:*
- Cognito retorna idToken
- `UserRepository.login` seta `SessionManager.token = idToken`
- Chama `getCurrentUser()` (`GET usuario`)
- Retorna `AuthResponse` com dados (id, nome, email, telefone, cpf, etc.)
- `UserRepository.saveSession` persiste dados em SessionManager
- UI navega para tela `home`
- Mensagem de sucesso (nenhuma, apenas navigação silenciosa)

*Em erro:*
- `NotAuthorizedException` → mensagem: "E-mail ou senha incorretos."
- `UserNotFoundException` → mensagem: "Usuário não encontrado."
- `UserNotConfirmedException` → mensagem: "Conta não confirmada. Verifique seu e-mail."
- `PasswordResetRequiredException` → mensagem: "Redefinição de senha necessária."
- Altre exceções → message retornado pela exceção
- Erro de conexão → mensagem: "Sem conexão com o servidor (Verifique sua internet)"
- UI exibe banner de erro (vermelho) que desaparece em 5 segundos

**Observações:**
- Apesar de existir `POST api/auth/login` no ApiService, o app usa Cognito direto
- Token Cognito (idToken) é reutilizado como bearer token para todas as requisições

---

### FLUXO 2: Cadastro (Register)

**Arquivos relevantes:**
- `RegisterScreen.kt`
- `AuthViewModel.register`
- `UserRepository.register`
- `CognitoService.signUp`

**Entradas (UI) — campos obrigatórios:**
- Nome completo (apenas letras e espaços)
- CPF (11 dígitos)
- Data de nascimento (formato dd/mm/yyyy)
- Gênero (dropdown: "Masculino", "Feminino", "Outros", "Não informar")
- CEP (8 dígitos)
- Rua (texto)
- Número (dígitos)
- Bairro (texto)
- Cidade (texto)
- Estado (2 letras, uppercase)
- Celular (até 11 dígitos)
- Email (contém '@')
- Senha (força forte exigida)

**Validações locais:**
- Nome: apenas letras e espaços, não-vazio
- CPF: exatamente 11 dígitos
- Data nascimento: formato dd/mm/yyyy valido, idade >= 18 e <= 110 anos
- Gênero: selecionado (não-vazio)
- CEP: exatamente 8 dígitos
- Rua: não-vazio
- Número: não-vazio, dígitos
- Bairro: não-vazio
- Cidade: não-vazio
- Estado: exatamente 2 letras, convertidas para uppercase
- Celular: não-vazio, até 11 dígitos
- Email: contém '@'
- Senha: >= 8 caracteres, pelo menos 1 uppercase, 1 lowercase, 1 digit, 1 non-alphanumeric

Se alguma validação falha, o botão "Criar conta" é desabilitado e/ou mensagens de erro aparecem acima do respectivo campo.

**Fluxo de execução:**
1. Usuário preenche todos os campos
2. Clica em "Criar conta"
3. `RegisterScreen` valida localmente todos os campos
4. Se válido, monta `UserRegisterProfileDTO` e chama `viewModel.register(dto)`
5. `AuthViewModel.register` chama `UserRepository.register(dto)`
6. `UserRepository.register`:
   - Converte data: `dd/mm/yyyy` → `YYYY-MM-DD`
   - Mapeia gênero: "Masculino" → "1", "Feminino" → "2", "Outros" → "3", outro → "4"
   - Chama `CognitoService.signUp` com todos os dados
7. `CognitoService.signUp`:
   - Monta `attributeList` com atributos padrão e customizados
   - Calcula `secretHash = HMAC-SHA256(email, clientSecret)` Base64-encoded
   - Envia `SignUpRequest` para Cognito
   - Cognito cria usuário (requer confirmação via email)
   - Retorna sucesso ou exceção

**Resultados esperados:**

*Em sucesso:*
- `Result.success(Unit)` → `AuthViewModel` chama callback
- Mensagem de sucesso: "Cadastro realizado com sucesso! Verifique seu e-mail."
- UI navega de volta para LoginScreen com `savedStateHandle.set("success_msg", message)`
- LoginScreen exibe banner de sucesso

*Em erro:*
- `UsernameExistsException` → "Este e-mail já está cadastrado."
- `InvalidPasswordException` → "A senha não atende aos requisitos de segurança."
- `InvalidParameterException` → "Dados inválidos. Verifique os campos."
- `CodeDeliveryFailureException` → "Erro ao enviar código de verificação."
- Outras → messagem de exceção ou "Erro ao criar conta"
- UI exibe banner de erro (vermelho) que desaparece em 5 segundos

**Observações:**
- Novo usuário criado em Cognito com status "UNCONFIRMED" até confirmar email
- `custom:tipo_usuario` é fixado como "CLIENTE"
- Após confirmar email no Cognito, usuário pode fazer login normalmente

---

### FLUXO 3: Carregar Formulário de Solicitação de Serviço

**Arquivos relevantes:**
- `ServiceRequestViewModel.loadFormData`
- `ServiceRepository.getServiceRequestForm`
- `OrderScreen.kt`

**Entrada:**
- (sem parâmetros — aciona ao abrir tela OrderScreen)

**Chamada:**
- `GET solicitar-servico` (sem Authorization necessária? requerida?)

**Resposta esperada:**
```json
{
  "genero": "M|F|O|N",
  "mostrar_filtro_mulheres": true|false,
  "enderecos": [
    {
      "id": 1,
      "titulo": "Casa",
      "cep": "12345678",
      "rua": "Rua X",
      "numero": "123",
      "complemento": "apto 1",
      "referencia": "perto da padaria",
      "bairro": "Centro",
      "cidade": "São Paulo",
      "estado": "SP",
      "is_favorite": true,
      "formatado": "Rua X, 123, Centro, São Paulo, SP"
    }
  ],
  "servicos": [
    {
      "id": 1,
      "nome": "Limpeza",
      "basePrice": 100.00,
      "icon": "..."
    }
  ]
}
```

**Fluxo de execução:**
1. `OrderScreen` monta e exibe quando aberto
2. `LaunchedEffect(Unit)` chama `viewModel.loadFormData()`
3. `ServiceRequestViewModel.loadFormData` chama `repository.getServiceRequestForm()`
4. Retrofit executa `GET solicitar-servico`
5. Backend retorna `ServiceRequestFormResponse`
6. ViewModel armazena em `_formData`

**Resultados esperados:**

*Em sucesso:*
- `formData` preenchido com serviços, endereços, flags
- UI renderiza:
  - Cards de categorias (serviços) para seleção horizontal
  - Filtro "Somente prestadores mulheres" se `mostrar_filtro_mulheres == true`
  - Seletor de endereço (pré-seleciona `is_favorite==true` ou primeiro da lista)
  - Opções de timing: "Agora" (default) ou "Agendar"
  - Campo de comentário (opcional)

*Em erro:*
- `_errorMessage` preenchido com mensagem
- UI exibe loading ou mensagem de erro
- Botão "Buscar Profissional" desabilitado

**Validações/Comportamento:**
- Se `timeMode == "Agora"`: apenas botão "Agora" visível, datepicker/time escondidos
- Se `timeMode == "Agendar"`: datepicker e time input visíveis
- "Buscar Profissional" só habilitado se categoria selecionada + (Agora OU (Agendar + data + hora válida))

---

### FLUXO 4: Buscar Prestadores (Providers)

**Arquivos relevantes:**
- `ServiceRepository.getProvidersForCategory`
- `AvailableProvidersScreen.kt`

**Entradas:**
- `categoryId` (Long) — categoria selecionada
- `onlineOnly` (Boolean) — true se modo "Agora", false se "Agendar"
- `onlyWomen` (Boolean) — filtro de gênero

**Chamada:**
- `GET api/providers/for-category/{categoryId}?onlineOnly={true|false}&onlyWomen={true|false}`

**Resposta esperada:**
```json
[
  {
    "id": 123,
    "name": "João Silva",
    "email": "joao@email.com",
    "rating": 4.8,
    "ratingCount": 45,
    "online": true,
    "specialtyPrice": 150.00
  }
]
```

**Fluxo de execução:**
1. `OrderScreen` navega para `AvailableProvidersScreen` com parâmetros (catId, onlyWomen, mode, date, time, addrId, comment, basePrice)
2. `AvailableProvidersScreen.LaunchedEffect(Unit)` calcula `onlineOnly = (mode == "Agora")`
3. Chama `ServiceRepository.getProvidersForCategory(catId, onlineOnly, onlyWomen)`
4. Retrofit executa GET
5. Backend retorna lista de `ProviderDTO`

**Resultados esperados:**

*Em sucesso (providers encontrados):*
- Lista preenchida com cards de prestadores
- Cada card mostra:
  - Nome do prestador
  - Status online (ponto verde se online)
  - Rating (ex: ⭐ 4.8 (45) — se ratingCount > 0)
  - Preço efetivo: `specialtyPrice` se existe, senão `basePrice`
  - Badge "Preço próprio" se `specialtyPrice` presente
- Usuário clica em prestador para selecioná-lo (toggle)
- Botão "Confirmar Pedido" ativado quando prestador selecionado
- Bottom bar mostra nome e preço do selecionado

*Em sucesso (sem providers):*
- Box centralizado com icon e mensagem:
  - Se `onlineOnly==true`: "Nenhum prestador online no momento.\nTente o modo Agendar."
  - Senão: "Nenhum prestador disponível para esta categoria."

*Em erro:*
- Mensagem de erro exibida: "Erro ao carregar prestadores" ou mensagem específica
- UI permite retry

**Validações/Comportamento:**
- `onlineOnly` é derivada de `mode`: "Agora" → true, "Agendar" → false
- Preço exibido = provider.specialtyPrice OU basePrice

---

### FLUXO 5: Criar Pedido (Submeter Solicitação)

**Arquivos relevantes:**
- `ServiceRepository.createRequest`
- `AvailableProvidersScreen.kt` (botão confirmar)

**Entradas:**
- `categoryId` (Long)
- `description` (String) — comentário do usuário
- `type` (String) — "IMMEDIATE" ou "SCHEDULED"
- `scheduledAt` (String|null) — ISO-8601, apenas se SCHEDULED
- `preferredProviderId` (Long) — id do prestador selecionado
- `addressId` (Long) — id do endereço selecionado

**Construção de body (em `AvailableProvidersScreen`):**
```kotlin
val scheduledAt = if (mode == "Agendar" && date != null && time != null) {
    buildIsoDateTime(date, time)  // ex: "2025-03-15T18:30:00"
} else null

repository.createRequest(
    categoryId, description, 
    type = if (mode == "Agora") "IMMEDIATE" else "SCHEDULED",
    scheduledAt, preferredProviderId, addressId
)
```

**Chamada:**
- `POST api/requests`
```json
{
  "categoryId": 1,
  "description": "Descrição do serviço",
  "type": "IMMEDIATE|SCHEDULED",
  "scheduledAt": "2025-03-15T18:30:00",
  "preferredProviderId": 123,
  "addressId": 456
}
```

**Resposta esperada:**
```json
{
  "id": 789,
  "servico": "Limpeza",
  "cliente": "João Cliente",
  "prestador": "Maria Prestadora",
  "data": "2025-03-15T18:30:00",
  "valor": 150.00,
  "endereco": "Rua X, 123",
  "descricao": "...",
  "status": "PENDENTE",
  "status_id": 1,
  "mine_role": "CLIENTE",
  "nome_parte": "Maria Prestadora"
}
```

**Fluxo de execução:**
1. Usuário seleciona prestador e clica "Confirmar Pedido"
2. `AvailableProvidersScreen` monta `scheduledAt` se modo Agendar
3. Chama `repository.createRequest(...)`
4. `isCreating = true` (desabilita botão, mostra loading)
5. Retrofit executa POST
6. Backend cria pedido e retorna `ServiceRequestDTO`

**Resultados esperados:**

*Em sucesso:*
- `ServiceRequestDTO` retornado com id e status (usualmente "PENDENTE")
- UI mostra `successMsg = "Pedido enviado para {providerName}!"`
- Aguarda 1.5 segundos
- Navega de volta para `home` com `popUpTo("home")` inclusive
- Banner de sucesso desaparece após 3 segundos

*Em erro:*
- `errorMsg` preenchida com mensagem (ex: "Erro ao criar solicitação")
- Banner de erro exibido (desaparece após 5 segundos)
- Botão volta a estar habilitado para retry

**Observações:**
- Formato ISO-8601 construído em `buildIsoDateTime(date, time)`:
  - Input date: "15 Mar, 2025" (formatado pelo datepicker do cliente)
  - Input time: "18:30" ou "1830" (4 dígitos)
  - Output: "2025-03-15T18:30:00"
- Se modo "Agora", `scheduledAt` é null (omitido)
- Se `addressId == 0`, pode ser omitido (backend provavelmente usa endereço default)

---

### FLUXO 6: Listar Histórico de Pedidos

**Arquivos relevantes:**
- `ServiceRepository.getHistory`
- `HistoryScreen.kt`
- `NotificationStore.syncFromHistory`

**Entradas:**
- `status` (String|null) — filtro opcional (TODOS, PENDENTE, AGENDADO, EM ANDAMENTO, CONCLUIDO, CANCELADO)

**Chamada:**
- `GET meus-pedidos?status={status}` (status omitido se "TODOS")

**Resposta esperada:**
```json
{
  "pedidos": [
    {
      "id": 789,
      "servico": "Limpeza",
      "cliente": "João",
      "prestador": "Maria",
      "data": "2025-03-15T18:30:00",
      "valor": 150.00,
      "endereco": "...",
      "descricao": "...",
      "status": "CONCLUIDO|AGENDADO|...",
      "status_id": 2,
      "minha_role": "CLIENTE",
      "nome_parte": "Maria"
    }
  ]
}
```

**Fluxo de execução:**
1. `HistoryScreen` monta com filtro padrão "TODOS"
2. Chama `repository.getHistory(statusParam)` em `LaunchedEffect(filtro)`
3. Retrofit executa GET
4. Backend retorna `OrderListResponse` com lista `pedidos`
5. `NotificationStore.syncFromHistory(pedidos)` é chamada para gerar notificações derivadas:
   - Mapeia cada pedido conforme status:
     - ACCEPTED/AGENDADO → `NotificationItem` com `id = "{pedidoId}_ACCEPTED"`, título "Pedido aceito! ✅"
     - COMPLETED/CONCLUIDO → `NotificationItem` com `id = "{pedidoId}_COMPLETED"`, título "Serviço concluído! 🎉"
     - REFUSED → `NotificationItem` com `id = "{pedidoId}_REFUSED"`, título "Agendamento recusado ❌"
     - CANCELLED/CANCELADO → `NotificationItem` com `id = "{pedidoId}_CANCELLED"`, título "Pedido cancelado"
   - Preserva flag `read` se notificação duplicada já existia
   - Ordena decrescente por `timestamp` (mais recente no topo)

**Resultados esperados:**

*Em sucesso:*
- Lista de pedidos renderizada com cards:
  - Ícone da categoria
  - Nome do serviço
  - Nome da outra parte (prestador se cliente, cliente se prestador)
  - Valor formatado como "R$ XX.XX"
  - Data formatada (ex: "15/03/2025 18:30")
  - Status badge com cor específica:
    - PENDENTE: fundo laranja claro, texto laranja
    - AGENDADO: fundo azul claro, texto azul
    - EM ANDAMENTO: fundo verde claro, texto verde
    - CONCLUIDO: fundo verde claro, texto verde
    - CANCELADO: fundo vermelho claro, texto vermelho
- LazyRow de filtros no topo permitindo trocar de abas
- Click em card navega para `order-detail/{id}`

*Em sucesso (lista vazia):*
- Mensagem: "Nenhum pedido encontrado"

*Em erro:*
- Mensagem: "Erro ao carregar pedidos"
- Botão "Tentar novamente" para retry

**Validações/Comportamento:**
- Ao trocar filtro, `LaunchedEffect(filtro)` dispara nova chamada
- Data ISO é convertida para formato legível (função `formatIsoDate`)

---

### FLUXO 7: Obter Detalhes de um Pedido

**Arquivos relevantes:**
- `ServiceRepository.getById`
- `OrderDetailScreen.kt`

**Entradas:**
- `id` (Long) — id do pedido

**Chamada:**
- `GET detalhes-pedido?id={id}`

**Resposta esperada:**
- `ServiceRequestDTO` completo

**Fluxo de execução:**
1. `HistoryScreen` click em card chama `onNavigateToDetail(pedido.id)`
2. Navega para `order-detail/{id}` (NavGraph passa id como argumento)
3. `OrderDetailScreen` usa `state.arguments?.getLong("id")` para obter id
4. `LaunchedEffect(Unit)` chama `repository.getById(id)`
5. Retrofit executa GET

**Resultados esperados:**

*Em sucesso:*
- Tela de detalhes renderiza:
  - Serviço (nome, preço)
  - Prestador/Cliente (nome, rating)
  - Data e hora
  - Endereço
  - Descrição
  - Status com badge colorida
  - Ações disponíveis conforme status:
    - Se PENDENTE: botão "Cancelar"
    - Se EM ANDAMENTO: botões "Cancelar" e "Confirmar Finalização"
    - Se CONCLUIDO: botão "Avaliar" (se ainda não avaliado)
    - Se CANCELADO: (sem ações adicionais)

*Em erro:*
- Mensagem de erro com retry

**Ações possíveis:**
- Cancelar: `PUT api/requests/{id}/cancel`
- Confirmar finalização: `PUT api/requests/{id}/confirm-finish`
- Avaliar: `POST api/ratings` (fluxo separado — Fluxo 8)

---

### FLUXO 8: Avaliar Serviço (Rate)

**Arquivos relevantes:**
- `ServiceRepository.rate`
- Tela de avaliação (UI em screens/rating)

**Entradas:**
- `serviceRequestId` (Long)
- `stars` (Int 1-5)
- `comment` (String)

**Chamada:**
- `POST api/ratings`
```json
{
  "serviceRequestId": 789,
  "stars": 5,
  "comment": "Excelente serviço!"
}
```

**Fluxo de execução:**
1. Usuário vê botão "Avaliar" em pedido com status CONCLUIDO
2. Clica → abre diálogo/tela de avaliação
3. Seleciona quantidade de estrelas (1-5) e digita comentário
4. Clica "Enviar avaliação"
5. Chama `repository.rate(serviceRequestId, stars, comment)`
6. Retrofit executa POST

**Resultados esperados:**

*Em sucesso:*
- Backend retorna 200 OK
- UI mostra mensagem de sucesso: "Avaliação enviada com sucesso!"
- Botão "Avaliar" desaparece/desabilita
- `SessionManager.ratedRequestIds` é atualizada para evitar re-avaliação
- Histórico é re-carregado para refletir estado

*Em erro:*
- Se backend retorna erro com texto "já avaliou": mensagem "Você já avaliou este serviço"
- Outro erro: "Erro ao enviar avaliação"
- Usuário pode tentar novamente

**Validações:**
- Mínimo 1 estrela, máximo 5
- Comentário é opcional (pode estar vazio)

---

### FLUXO 9: Gerenciar Endereços

**Arquivos relevantes:**
- `UserRepository.getAddresses, createAddress, updateAddress, deleteAddress, setDefaultAddress`
- `AddressViewModel.kt`
- `AddressScreen.kt`, `AddressFormScreen.kt`

#### 9.1 Listar Endereços
**Chamada:** `GET meus-enderecos`

**Resposta esperada:**
```json
{
  "enderecos": [
    {
      "id": 1,
      "titulo": "Casa",
      "cep": "12345678",
      "rua": "Rua X",
      "numero": "123",
      "complemento": "apto 101",
      "referencia": "perto da escola",
      "bairro": "Centro",
      "cidade": "São Paulo",
      "estado": "SP",
      "is_favorite": true,
      "formatado": "Rua X, 123, Centro, São Paulo, SP"
    }
  ]
}
```

**Fluxo:**
1. `AddressScreen` monta
2. `ViewModel.loadAddresses()` chama `repository.getAddresses()`
3. Retrofit GET
4. UI renderiza lista com cards

**Resultados:**
- Lista de endereços com opção de editar/deletar
- Card do endereço padrão marcado com estrela ou badge

#### 9.2 Criar Endereço
**Chamada:** `POST cadastrar-endereco` com body `Map<String, String>`

**Campos esperados:**
```json
{
  "titulo": "Casa",
  "cep": "12345678",
  "rua": "Rua X",
  "numero": "123",
  "complemento": "apto 101",
  "referencia": "perto da escola",
  "bairro": "Centro",
  "cidade": "São Paulo",
  "estado": "SP"
}
```

**Validações locais (AddressFormScreen):**
- titulo: não-vazio
- cep: 8 dígitos
- rua: não-vazio
- numero: não-vazio
- bairro: não-vazio
- cidade: não-vazio
- estado: 2 caracteres

**Fluxo:**
1. Usuário clica "Adicionar Endereço"
2. Navega para `address-new`
3. Preenche formulário
4. Clica "Salvar"
5. `AddressFormScreen` valida e chama `repository.createAddress(body)`
6. Retrofit POST

**Resultados:**
- Em sucesso: `AddressDTO` criado, volta para lista de endereços
- Em erro: mensagem de erro

#### 9.3 Atualizar Endereço
**Chamada:** `PUT atualizar-endereco?id={id}` com body `Map<String, String>`

**Fluxo:**
1. Usuário clica em endereço existente → editar
2. Navega para `address-edit/{addressId}`
3. Formulário pré-preenchido com dados atuais
4. Altera campos conforme desejado
5. Clica "Salvar"
6. `AddressFormScreen` valida e chama `repository.updateAddress(id, body)`

**Resultados:**
- Em sucesso: endereço atualizado, volta para lista
- Em erro: mensagem

#### 9.4 Definir Endereço como Padrão
**Chamada:** `PUT api/addresses/{id}/default` (requer Authorization)

**Fluxo:**
1. Usuário clica em estrela/badge do endereço para marcar como padrão
2. Chama `repository.setDefaultAddress(id)`
3. Retrofit PUT

**Resultados:**
- Em sucesso: `is_favorite` atualizado, card marcado como favorito
- Lista atualiza refletindo novo padrão

#### 9.5 Deletar Endereço
**Chamada:** `DELETE api/addresses/{id}`

**Fluxo:**
1. Usuário clica botão delete em card
2. Confirma (dialog "Tem certeza?")
3. Chama `repository.deleteAddress(id)`
4. Retrofit DELETE

**Resultados:**
- Em sucesso: endereço removido da lista
- Em erro: mensagem

---

### FLUXO 10: Notificações

**Arquivos relevantes:**
- `UserRepository.getNotifications` (chama `GET minhas-notificacoes`)
- `NotificationViewModel.kt`
- `NotificationStore.kt`

**Chamada (backend):** `GET minhas-notificacoes` → `NotificationListResponse`

**Chamada (local - derive do histórico):** `NotificationStore.syncFromHistory(pedidos)`

**Fluxo de execução:**
1. Ao abrir `NotificationScreen`:
   - Chama `viewModel.loadNotifications()` para carregar do backend
   - Exibe `NotificationStore.items` (que inclui ambas: reais + derivadas do histórico)

2. `NotificationStore.addOrUpdate` adiciona notificação ou atualiza preservando `read`:
   ```kotlin
   val idx = items.indexOfFirst { it.id == item.id }
   if (idx >= 0) {
       items[idx] = item.copy(read = items[idx].read)  // preserva flag read
   } else {
       items.add(0, item)  // novo, no topo
   }
   ```

3. Usuário clica em notificação → navega para detalhes do pedido
   - `NotificationStore.markRead(id)` marca como lida
   - UI atualiza (badge de unread desaparece)

4. `NotificationStore.markAllRead()` marca todas como lidas

**Resultados esperados:**

*Em sucesso:*
- Lista de notificações com:
  - Título
  - Mensagem
  - Timestamp formatado
  - Indicador visual se lida/não-lida (ex: cor ou ícone)
- Contador de não-lidas visível (ex: na aba Notificações)
- Click navega para detalhes do pedido relacionado
- Pull-to-refresh pode re-sincronizar

*Em erro (sem notificações):*
- Mensagem: "Sem notificações no momento"

---

### FLUXO 11: Editar Perfil

**Arquivos relevantes:**
- `UserRepository.updateProfile`
- `ProfileScreen.kt`

**Chamada:** `PUT api/users/me` com body `Map<String, String>`

**Campos atualizáveis (exemplo):**
```json
{
  "name": "João Silva",
  "email": "joao.novo@email.com",
  "phone": "11987654321",
  "... etc"
}
```

**Fluxo:**
1. Usuário abre `ProfileScreen`
2. Vê dados atuais (carregados de `SessionManager`)
3. Edita campo(s)
4. Clica "Salvar"
5. Chama `repository.updateProfile(body)`
6. Retrofit PUT com Authorization

**Resultados:**

*Em sucesso:*
- Backend retorna `AuthResponse` atualizado
- `UserRepository.saveSession` persiste novos dados em SessionManager
- UI mostra mensagem: "Perfil atualizado com sucesso"
- Tela de perfil reflete mudanças

*Em erro:*
- Mensagem: "Erro ao salvar perfil" ou específica do backend

---

### FLUXO 12: Alterar Senha

**Arquivos relevantes:**
- `UserRepository.changePassword`
- `SettingScreen.kt` (opção "Alterar Senha")

**Chamada:** `PUT api/users/me/password` com body:
```json
{
  "currentPassword": "senha_atual",
  "newPassword": "nova_senha"
}
```

**Validações locais:**
- Nova senha não-vazia
- Confirmação de senha confere (se houver campo duplicado na UI)

**Fluxo:**
1. Usuário abre diálogo "Alterar Senha"
2. Insere senha atual e nova
3. Clica confirmar
4. Chama `repository.changePassword(current, newPass)`
5. Retrofit PUT

**Resultados:**

*Em sucesso:*
- Backend retorna 200
- Mensagem: "Senha alterada com sucesso"
- Diálogo fecha

*Em erro (tratamento customizado no repositório):*
- Se erro contém "atual incorreta": "Senha atual incorreta"
- Se erro contém "6 caracteres": "Nova senha deve ter pelo menos 6 caracteres"
- Senão: "Erro ao alterar senha"
- UI exibe erro; usuário pode tentar novamente

---

### FLUXO 13: Logout

**Arquivos relevantes:**
- `SessionManager.clear`
- `SettingScreen.kt` (botão "Sair")
- `NavGraph.kt`

**Fluxo:**
1. Usuário clica "Sair"
2. Confirma logout (dialog opcional)
3. Code:
   ```kotlin
   SessionManager.clear()  // limpa token, dados, listas locais
   nav.navigate("login") { popUpTo(0) { inclusive = true } }
   ```
4. Navega de volta para LoginScreen, limpando todo o backstack

**Resultados:**
- SessionManager vazio (`isLoggedIn() == false`)
- Retrofit já não adiciona Authorization header
- Usuário deslogado

---

## Tratamento de Erros e Mensagens de Validação

### Erros de Autenticação (Cognito)
| Exceção | Mensagem Exibida |
|---------|------------------|
| `NotAuthorizedException` | "E-mail ou senha incorretos." |
| `UserNotFoundException` | "Usuário não encontrado." |
| `UserNotConfirmedException` | "Conta não confirmada. Verifique seu e-mail." |
| `PasswordResetRequiredException` | "Redefinição de senha necessária." |
| `UsernameExistsException` | "Este e-mail já está cadastrado." |
| `InvalidPasswordException` | "A senha não atende aos requisitos de segurança." |
| `InvalidParameterException` | "Dados inválidos. Verifique os campos." |
| `CodeDeliveryFailureException` | "Erro ao enviar código de verificação." |
| Conexão perdida | "Sem conexão com o servidor" |

### Erros de Requisições Retrofit
| Contexto | Mensagem Padrão |
|----------|-----------------|
| Qualquer falha sem body | "Sem conexão com o servidor" |
| `getCurrentUser` falha | "Erro ao carregar dados do usuário" |
| Categories falha | "Erro ao carregar categorias" |
| Form falha | "Erro ao carregar dados do formulário" |
| Providers falha | "Nenhum prestador encontrado" |
| Create request falha | "Erro ao criar solicitação" |
| Rate falha (duplicado) | "Você já avaliou este serviço" (se erro contém "já avaliou") |
| Rate falha (genérico) | "Erro ao enviar avaliação" |
| History falha | "Erro ao carregar pedidos" |
| Addresses falha | "Erro ao carregar endereços" |
| Create address falha | "Erro ao criar endereço" |
| Update address falha | "Erro ao atualizar endereço" |
| Delete address falha | "Erro ao remover endereço" |
| SetDefault address falha | "Erro ao definir endereço padrão" |
| Notifications falha | "Erro ao carregar notificações" |

### Validações da UI (RegisterScreen)
| Campo | Validação | Mensagem/Comportamento |
|-------|-----------|------------------------|
| Nome | Apenas letras e espaços | Rejeita outros caracteres em tempo real |
| CPF | Exatamente 11 dígitos | Rejeita não-dígitos; valida comprimento |
| Birthdate | dd/mm/yyyy, idade >= 18 e <= 110 | Exibe erro "Deve ter pelo menos 18 anos" ou "Data inválida" |
| Gênero | Seleção obrigatória | Botão desabilitado se não selecionado |
| CEP | Exatamente 8 dígitos | Rejeita não-dígitos; valida comprimento |
| Estado | 2 letras, uppercase | Converte automaticamente para upper; valida length |
| Telefone | Até 11 dígitos | Rejeita não-dígitos |
| Email | Contém '@' | Não permite espaços; valida presença de @ |
| Senha | >= 8 chars, Upper, Lower, Digit, Special | Campo visual mostra força (verde/amarelo/vermelho) |
| Geral | Todos os campos preenchidos corretamente | Botão "Criar conta" desabilitado até todas as validações passarem |

### Validações da UI (LoginScreen)
| Campo | Validação |
|-------|-----------|
| Email | Não-vazio |
| Senha | Não-vazio |
| Geral | Botão "Acessar conta" habilitado apenas com ambos preenchidos e não carregando |

### Validações da UI (OrderScreen)
| Condição | Comportamento |
|----------|---------------|
| Sem categoria | Botão "Buscar Profissional" desabilitado |
| Modo "Agora" | Datepicker e time input ocultos|
| Modo "Agendar" sem data | Botão desabilitado |
| Modo "Agendar" com data mas sem hora | Botão desabilitado |
| Modo "Agendar" com data e hora válida (4 dígitos HH:mm) | Botão habilitado |

---

## Pontos Críticos de Implementação

### Segurança
- **Cognito CLIENT_SECRET:** Atualmente hard-coded em `CognitoService.kt` — risco de segurança. **Recomendação:** Movê-lo para arquivo de build time (buildConfigField ou secret management service).
- **EncryptedSharedPreferences:** Código usa corretamente; certifique-se de que permissões Android estejam adequadas.
- **Token em memória:** `SessionManager.token` fica em memória enquanto app executando; considerar limpá-lo em background (não impedido atualmente).

### Performance
- **Lazy loading:** Telas usam `LazyColumn` e `LazyRow` para otimizar renderização.
- **Re-composição:** UI states estão em ViewModels (StateFlow) para evitar re-renders desnecessários.
- **Caching:** Sem cache explícito de respostas; cada tela re-carrega dados sempre que montada (pode melhorar).

### Maintainability
- **Base URL:** Em `RetrofitClient.BASE_URL` — altere conforme ambiente (dev/prod).
- **Mensagens:** Mapeadas em repositórios e ViewModels; centralize em strings.xml para i18n futuro.
- **Endpoints:** Documentados em `ApiService.kt` (interface clara).
- **DTOs:** Todas as classes de dados em `ServiceModels.kt` com `@SerializedName` explícitos.

### Extensões Futuras
- **Offline mode:** Considerar sincronização local com banco de dados (Room).
- **Push notifications:** `PreferencesManager.pushEnabled` suporta flags, mas sem implementação de FCM.
- **Persistência de preferências:** `PreferencesManager` não persiste atualmente; implementar com `EncryptedSharedPreferences`.
- **Forgot password:** Endpoints existem mas retornam "Funcionalidade em migração".
- **2FA:** Não implementado; considerar após migração completa.

---

## Como Navegar o Código

### Para entender o fluxo de autenticação
Leia na ordem:
1. `CognitoService.kt` — integração com Cognito
2. `UserRepository.kt` → métodos `login` e `register`
3. `LoginScreen.kt` e `RegisterScreen.kt` — UI e validações locais
4. `AuthViewModel.kt` — orquestração

### Para entender fluxo de pedidos
Leia na ordem:
1. `ServiceRepository.kt` → métodos `getServiceRequestForm`, `getProvidersForCategory`, `createRequest`
2. `ServiceRequestViewModel.kt` → carrega formulário
3. `OrderScreen.kt` → seleção de serviço
4. `AvailableProvidersScreen.kt` → seleção de prestador e criação
5. `HistoryScreen.kt` → visualização de histórico

### Para entender sessão e headers
Leia:
1. `SessionManager.kt` → armazenamento criptografado
2. `RetrofitClient.kt` → interceptor de Authorization

### Para entender notificações
Leia:
1. `NotificationStore.kt` → armazenamento local e sincronização
2. `NotificationViewModel.kt` → carregamento do backend
3. `UserRepository.getNotifications` → chamada `GET minhas-notificacoes`

### Para entender endereços
Leia:
1. `UserRepository.kt` → métodos `getAddresses`, `createAddress`, `updateAddress`, `deleteAddress`, `setDefaultAddress`
2. `AddressViewModel.kt`
3. `AddressScreen.kt` e `AddressFormScreen.kt` — UI

---

## Conclusão

Este aplicativo é um cliente moderno de serviços "on-demand" com:
- ✅ Autenticação segura (Cognito + JWT)
- ✅ Fluxo completo de solicitação de serviço (categorias → prestadores → pedido)
- ✅ Histórico e avaliações
- ✅ Gerenciamento de endereços e perfil
- ✅ Notificações em tempo real (derivadas do histórico + backend)
- ✅ UI responsiva com Jetpack Compose
- ✅ Armazenamento seguro da sessão

Os padrões usados (Repository, ViewModel, Retrofit, Compose) são modernos e facilitam manutenção e extensão. O código está bem-estruturado por funcionalidade (data, ui, uimodels) e pronto para growth.
