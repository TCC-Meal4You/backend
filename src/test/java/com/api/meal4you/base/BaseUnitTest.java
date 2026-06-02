package com.api.meal4you.base;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Classe base abstrata para testes unitários do Meal4You.
 * 
 * Fornece configuração padrão para:
 * - JUnit 5 (Jupiter)
 * - Mockito para mocks
 * - Nomes de exibição descritivos
 * - Padrão AAA (Arrange, Act, Assert)
 * 
 * Todos os testes unitários devem estender esta classe.
 * 
 * Exemplo de uso:
 * <pre>
 * class UsuarioServiceTest extends BaseUnitTest {
 *     
 *     @Mock
 *     private UsuarioRepository usuarioRepository;
 *     
 *     @InjectMocks
 *     private UsuarioService usuarioService;
 *     
 *     @Test
 *     void should_create_user_when_email_is_valid() {
 *         // Arrange
 *         UsuarioRequestDTO dto = new UsuarioRequestDTO("test@example.com", "Nome", "senha123");
 *         
 *         // Act
 *         UsuarioResponseDTO result = usuarioService.cadastrarUsuario(dto);
 *         
 *         // Assert
 *         assertThat(result)
 *             .isNotNull()
 *             .hasFieldOrPropertyWithValue("email", "test@example.com");
 *     }
 * }
 * </pre>
 * 
 * @author Meal4You Team
 * @version 1.0.0
 * @since 2026-05-20
 */
@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public abstract class BaseUnitTest {
    
    // Classe base sem métodos, apenas configuração via anotações
    // Facilita manutenção centralizada de configurações de teste
    
}
