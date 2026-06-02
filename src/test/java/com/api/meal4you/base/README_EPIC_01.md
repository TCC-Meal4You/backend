# 📋 EPIC 01 — Infraestrutura de Testes [COMPLETO]

## Objetivo ✅

Estruturar e configurar a infraestrutura completa para execução de testes unitários no projeto Meal4You.

---

## Componentes Implementados

### 1. Dependências Maven ✅

**Arquivo**: `pom.xml`

**Adicionadas**:
- [x] JUnit 5 (Jupiter) - via `spring-boot-starter-test`
- [x] Mockito 5.7.1 - para mocking de dependências
- [x] AssertJ 3.25.1 - assertions fluentes
- [x] H2 Database - banco em memória para testes
- [x] Spring Boot Test - utilitários de teste

**Versões Centralizadas em Properties**:
```properties
<assertj.version>3.25.1</assertj.version>
<mockito.version>5.7.1</mockito.version>
<jacoco.maven.plugin.version>0.8.10</jacoco.maven.plugin.version>
<maven.surefire.plugin.version>3.2.5</maven.surefire.plugin.version>
```

### 2. Plugins Maven ✅

**Configurados em `pom.xml`**:

#### Maven Surefire 3.2.5 ✅
- Executa testes automaticamente
- Padrão de nomes: `**/*Test.java` e `**/*Tests.java`
- Suporta isolamento de testes

#### Jacoco 0.8.10 ✅
- Gera relatório de cobertura: `target/site/jacoco/index.html`
- Executa 2 vezes: relatório padrão e check com regras
- Validação de cobertura mínima configurável

### 3. Profiles Maven ✅

**Configurados em `pom.xml`**:

#### Profile `test` (default) ✅
```bash
mvn clean test
```
- Ativado por padrão
- Executa todos os testes
- Falha se algum teste falhar

#### Profile `test-coverage` ✅
```bash
mvn clean test -P test-coverage
```
- Gera relatório detalhado de cobertura
- Valida cobertura mínima de 60% por package
- Útil para CI/CD e quality gates

### 4. Classes Base de Testes ✅

#### BaseUnitTest ✅
**Localização**: `src/test/java/com/api/meal4you/base/BaseUnitTest.java`

- Extensão com Mockito
- Display names descritivos (underscore → espaço)
- **Usar para**: Testes unitários puros (services, validadores)
- Sem contexto Spring
- Rápido para executar

**Exemplo**:
```java
class UsuarioServiceTest extends BaseUnitTest {
    @Mock private UsuarioRepository usuarioRepository;
    @InjectMocks private UsuarioService usuarioService;
    
    @Test
    void should_create_user_when_email_is_valid() {
        // Arrange, Act, Assert
    }
}
```

#### BaseIntegrationTest ✅
**Localização**: `src/test/java/com/api/meal4you/base/BaseIntegrationTest.java`

- Spring Boot TestContext carregado
- Banco H2 em memória
- Perfil 'test' ativado
- Porta aleatória para servidor
- **Usar para**: Testes que precisam de contexto Spring como injeção de dependências

**Exemplo**:
```java
class UsuarioServiceIntegrationTest extends BaseIntegrationTest {
    @Autowired private UsuarioService usuarioService;
    @Autowired private UsuarioRepository usuarioRepository;
    
    @Test
    void should_persist_user_to_database() {
        // Arrange, Act, Assert
    }
}
```

### 5. Utilitário de Testes ✅

**Classe**: `TestDataHelper`
**Localização**: `src/test/java/com/api/meal4you/base/TestDataHelper.java`

Facilita:
- Definição de campos privados via reflection
- Obtenção de valores de campos privados
- Criação de fixtures de teste

```java
Usuario usuario = new Usuario();
TestDataHelper.setFieldValue(usuario, "id", 1L);
Object id = TestDataHelper.getFieldValue(usuario, "id");
```

### 6. Configuração de Teste ✅

**Arquivo**: `src/test/resources/application-test.properties`

Configurações:
- [x] Banco H2 em memória (`:mem:testdb`)
- [x] JPA com `ddl-auto=create-drop`
- [x] Logging específico para testes
- [x] Mocks de chaves de API
- [x] Console H2 em `/h2-console`
- [x] Sem conexão com banco real

---

## Como Usar

### Executar Testes

```bash
# Todos os testes
mvn clean test

# Testes específicos
mvn clean test -Dtest=UsuarioServiceTest

# Com cobertura
mvn clean test -P test-coverage

# Gerar relatório sem executar testes novamente
mvn jacoco:report
```

### Visualizar Relatório de Cobertura

```bash
# Após executar os testes com cobertura
open target/site/jacoco/index.html        # macOS/Linux
start target/site/jacoco/index.html       # Windows
```

### Estrutura de Pastas

```
src/test/
├── java/com/api/meal4you/
│   ├── base/
│   │   ├── BaseUnitTest.java           ✅ Classe base testes unitários
│   │   ├── BaseIntegrationTest.java    ✅ Classe base testes integração
│   │   └── TestDataHelper.java         ✅ Utilitários de teste
│   ├── service/                        (próximas EPICs)
│   ├── validation/                     (próximas EPICs)
│   ├── recommendation/                 (próximas EPICs)
│   ├── auth/                           (próximas EPICs)
│   └── util/                           (próximas EPICs)
└── resources/
    └── application-test.properties     ✅ Configuração de teste
```

---

## Padrões de Teste

### 1. Nomeação de Métodos ✅

```java
// ✅ BOM - Descreve o comportamento
void should_create_user_when_email_is_valid()
void should_throw_exception_when_email_already_exists()
void should_update_user_profile_when_name_is_valid()

// ❌ RUIM - Nomes genéricos
void test_user()
void createUser()
void testUpdate()
```

### 2. Padrão AAA ✅

```java
@Test
void should_update_user_email_when_valid() {
    // ===== ARRANGE =====
    Usuario usuario = new Usuario();
    usuario.setEmail("old@example.com");
    String novoEmail = "new@example.com";
    
    // ===== ACT =====
    usuario.setEmail(novoEmail);
    
    // ===== ASSERT =====
    assertThat(usuario.getEmail()).isEqualTo(novoEmail);
}
```

### 3. Mockito ✅

```java
@Mock
private UsuarioRepository usuarioRepository;

@InjectMocks
private UsuarioService usuarioService;

@Test
void should_save_user_via_repository() {
    // Arrange
    Usuario usuario = new Usuario();
    when(usuarioRepository.save(any(Usuario.class)))
        .thenReturn(usuario);
    
    // Act
    Usuario resultado = usuarioService.criar(usuario);
    
    // Assert
    assertThat(resultado).isNotNull();
    verify(usuarioRepository, times(1)).save(any(Usuario.class));
}
```

### 4. AssertJ ✅

```java
// Assertions fluentes e legíveis
assertThat(usuario)
    .isNotNull()
    .hasFieldOrPropertyWithValue("email", "test@example.com")
    .extracting(Usuario::getNome)
    .isEqualTo("João Silva");

assertThat(usuarios)
    .hasSize(2)
    .extracting(Usuario::getEmail)
    .containsExactly("user1@example.com", "user2@example.com");
```

---

## Critérios de Qualidade ✅

✅ **Obrigatórios**:
- [ ] Testes independentes (sem dependências entre eles)
- [ ] Sem acesso real ao banco de dados
- [ ] Uso de mocks para isolamento
- [ ] Sem dependências externas (APIs, emails, etc)
- [ ] Nomes descritivos e claros
- [ ] Padrão AAA (Arrange, Act, Assert)
- [ ] Uma asserção principal por teste (quando possível)
- [ ] Setup e teardown automáticos

---

## 📊 Cobertura Esperada

| Camada | Meta |
|--------|------|
| Services | 85% |
| Regras de negócio | 95% |
| Validadores | 90% |
| Recomendação | 90% |

---

## ✅ Critérios de Aceite Cumpridos

- [x] Projeto executa `mvn test` com sucesso
- [x] Jacoco gera relatório em `target/site/jacoco/index.html`
- [x] Testes executam isoladamente
- [x] Classes base abstratas configuradas
- [x] Profiles Maven funcionando
- [x] Banco de dados H2 em memória configurado
- [x] Properties de teste definidas
- [x] Utilitários de teste criados

---

## 🚀 Próximas Etapas

Com a infraestrutura pronta, começar com:
1. **EPIC 02**: Implementar testes para Autenticação (alta prioridade)
2. **EPIC 03**: Implementar testes para Usuários (alta prioridade)
3. **EPIC 04**: Implementar testes para Restaurantes (alta prioridade)
4. **EPIC 05**: Implementar testes para Recomendações (muito alta)
5. **EPIC 06**: Implementar testes para Avaliações (média)

---

## 📚 Referências

- [JUnit 5 Documentation](https://junit.org/junit5/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/)
- [AssertJ Documentation](https://assertj.github.io/assertj-core/)
- [Spring Boot Testing](https://spring.io/guides/gs/testing-web/)
- [Jacoco Maven Plugin](https://www.eclemma.org/jacoco/trunk/doc/maven.html)
- [H2 Database](https://www.h2database.com/)

---

**Status**: ✅ **EPIC 01 COMPLETA**
**Data de Conclusão**: 20 de maio de 2026
**Tempo Estimado de Teste**: ~2-3 segundos para toda a suite
