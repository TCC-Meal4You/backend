package com.api.meal4you.base;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Classe base para testes de integração com contexto Spring.
 * 
 * Carrega o contexto do Spring Boot com perfil de teste.
 * Use apenas para testes que necessitam de contexto Spring.
 * Para testes unitários puros, use {@link BaseUnitTest}.
 * 
 * Configuração:
 * - Contexto Spring carregado completamente
 * - Perfil 'test' ativado
 * - Banco de dados H2 em memória
 * - Porta aleatória para servidor
 * 
 * Exemplo de uso:
 * <pre>
 * class UsuarioServiceIntegrationTest extends BaseIntegrationTest {
 *     
 *     @Autowired
 *     private UsuarioService usuarioService;
 *     
 *     @Autowired
 *     private UsuarioRepository usuarioRepository;
 *     
 *     @Test
 *     void should_persist_user_to_database() {
 *         // Arrange
 *         UsuarioRequestDTO dto = new UsuarioRequestDTO("test@example.com", "Nome", "senha123");
 *         
 *         // Act
 *         UsuarioResponseDTO result = usuarioService.cadastrarUsuario(dto);
 *         
 *         // Assert
 *         assertThat(usuarioRepository.findByEmail("test@example.com"))
 *             .isPresent()
 *             .hasValueSatisfying(u -> assertThat(u.getNome()).isEqualTo("Nome"));
 *     }
 * }
 * </pre>
 * 
 * @author Meal4You Team
 * @version 1.0.0
 * @since 2026-05-20
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public abstract class BaseIntegrationTest {
    
    // Classe base para testes com contexto Spring
    
}
