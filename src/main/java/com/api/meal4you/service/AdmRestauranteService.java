package com.api.meal4you.service;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.api.meal4you.dto.AdmRestauranteRequestDTO;
import com.api.meal4you.dto.AdmRestauranteResponseDTO;
import com.api.meal4you.dto.LoginRequestDTO;
import com.api.meal4you.dto.LoginResponseDTO;
import com.api.meal4you.dto.RedefinirSenhaRequestDTO;
import com.api.meal4you.entity.AdmRestaurante;
import com.api.meal4you.entity.Ingrediente;
import com.api.meal4you.entity.Refeicao;
import com.api.meal4you.entity.SocialLogin;
import com.api.meal4you.entity.Usuario;
import com.api.meal4you.mapper.AdmRestauranteMapper;
import com.api.meal4you.mapper.LoginMapper;
import com.api.meal4you.repository.AdmRestauranteRepository;
import com.api.meal4you.repository.IngredienteRepository;
import com.api.meal4you.repository.IngredienteRestricaoRepository;
import com.api.meal4you.repository.RefeicaoIngredienteRepository;
import com.api.meal4you.repository.RefeicaoRepository;
import com.api.meal4you.repository.RestauranteFavoritoRepository;
import com.api.meal4you.repository.RestauranteRepository;
import com.api.meal4you.repository.SocialLoginRepository;
import com.api.meal4you.repository.UsuarioAvaliaRepository;
import com.api.meal4you.repository.UsuarioRepository;
import com.api.meal4you.security.JwtUtil;
import com.api.meal4you.security.TokenStore;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdmRestauranteService {
    private final AdmRestauranteRepository admRepository;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;
    private final TokenStore tokenStore;
    private final VerificaEmailService verificaEmailService;
    private final EmailCodeSenderService emailCodeSenderService;
    private final GooglePeopleApiService googlePeopleApiService;
    private final SocialLoginRepository socialLoginRepository;
    private final RestauranteRepository restauranteRepository;
    private final IngredienteRepository ingredienteRepository;
    private final IngredienteRestricaoRepository ingredienteRestricaoRepository;
    private final RefeicaoRepository refeicaoRepository;
    private final RefeicaoIngredienteRepository refeicaoIngredienteRepository;
    private final RestauranteFavoritoRepository restauranteFavoritoRepository;
    private final UsuarioAvaliaRepository usuarioAvaliaRepository;
        private final UsuarioRepository usuarioRepository;

    public String getAdmLogadoEmail() {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getPrincipal() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Administrador não autenticado");
            }
            return authentication.getPrincipal().toString();
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao obter administrador logado: " + ex.getMessage());
        }
    }

    @Transactional
    public void enviarCodigoVerificacao(String email) {
        try {
            if (admRepository.findByEmail(email).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
            }
            String codigo = verificaEmailService.gerarESalvarCodigo(email);
            String subject = "Meal4You - Código de Verificação de Administrador";
            String body = "Olá! \n\nEsse é o seu código de verificação para concluir o cadastro: \n\n" + codigo + "\n\n ATENÇÃO: O CÓDIGO É VÁLIDO SOMENTE POR 5 MINUTOS.";
            emailCodeSenderService.enviarEmail(email, subject, body);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao enviar código de verificação: " + ex.getMessage());
        }
    }

    @Transactional
    public void solicitarAlteracaoEmail(String novoEmail) {
        try {
            if (admRepository.findByEmail(novoEmail).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Este e-mail já está em uso.");
            }

            String emailLogado = getAdmLogadoEmail(); // Pega e-mail do administrador logado
            AdmRestaurante adm = admRepository.findByEmail(emailLogado) // Pega o "objeto" do administrador logado
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Administrador não autenticado."));

            if (adm.getSenha() == null || adm.getSenha().isBlank()) { // <--- Isso serve para bloquear a alteração de e-mail para administradores que logam via social login (ou seja, sem senha cadastrada no banco)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Administrador criado via social login. Não pode alterar e-mail.");
            }

            String codigo = verificaEmailService.gerarESalvarCodigo(novoEmail);
            String subject = "Meal4You - Confirmação de Alteração de E-mail";
            String body = "Olá! Use este código para confirmar a alteração do seu e-mail: " + codigo;
            emailCodeSenderService.enviarEmail(novoEmail, subject, body);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao solicitar alteração de e-mail: " + ex.getMessage());
        }
    }

    @Transactional
    public AdmRestauranteResponseDTO atualizarEmail(String novoEmail, String codigoVerificacao) {
        try {
            String emailLogado = getAdmLogadoEmail();
            AdmRestaurante adm = admRepository.findByEmail(emailLogado)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Administrador não autenticado."));

            if (!verificaEmailService.validarCodigo(novoEmail, codigoVerificacao)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código de verificação inválido ou expirado.");
            }

            tokenStore.removerTodosTokensDaPessoa(adm.getEmail(), "ADMIN");
            adm.setEmail(novoEmail);
            admRepository.save(adm);
            return AdmRestauranteMapper.toResponse(adm);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao atualizar e-mail: " + ex.getMessage());
        }
    }

    @Transactional
    public AdmRestauranteResponseDTO cadastrarAdm(AdmRestauranteRequestDTO dto) {
        try {
            if (!verificaEmailService.validarCodigo(dto.getEmail(), dto.getCodigoVerificacao())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código de verificação inválido ou expirado.");
            }

            // Isso verifica se já existe uma conta de administrador ou de usuário com esse e-mail cadastrado
             Optional<Usuario> usuarioExistente = usuarioRepository.findByEmail(dto.getEmail());
            if (usuarioExistente.isPresent() || admRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado como administrador ou usuário.");
            }
            
            AdmRestaurante adm = AdmRestauranteMapper.toEntity(dto);
            adm.setSenha(encoder.encode(adm.getSenha()));
            admRepository.saveAndFlush(adm);
            return AdmRestauranteMapper.toResponse(adm);

        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao cadastrar administrador: " + ex.getMessage());
        }
    }

    @Transactional
    public AdmRestauranteResponseDTO buscarMeuPerfil() {
        try {
            String emailLogado = getAdmLogadoEmail();
            AdmRestaurante adm = admRepository.findByEmail(emailLogado)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Administrador não autenticado."));
            return AdmRestauranteMapper.toResponse(adm);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao buscar perfil do administrador: " + ex.getMessage());
        }
    }

    @Transactional
    public AdmRestauranteResponseDTO atualizarMeuPerfil(AdmRestauranteRequestDTO dto) {
        try {
            String emailLogado = getAdmLogadoEmail();
            AdmRestaurante adm = admRepository.findByEmail(emailLogado)                    
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Administrador não autenticado."));

            boolean alterado = false;

            if (dto.getNome() != null && !dto.getNome().isBlank() && !dto.getNome().equals(adm.getNome())) {
                adm.setNome(dto.getNome());
                alterado = true;
            }

            if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
                
                if (adm.getSenha() == null || adm.getSenha().isBlank()) { // <-- Isso bloqueia a alteração de senha se o administrador não tem senha cadastrada (criou conta via social login)
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Administrador criado via social login. Não pode definir senha.");
                }
                if (!encoder.matches(dto.getSenha(), adm.getSenha())) {
                    tokenStore.removerTodosTokensDaPessoa(adm.getEmail(), "ADMIN");
                    adm.setSenha(encoder.encode(dto.getSenha()));
                    alterado = true;
                }
            }

            if (!alterado) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nenhuma alteração detectada.");
            }

            admRepository.save(adm);
            return AdmRestauranteMapper.toResponse(adm);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao atualizar administrador: " + ex.getMessage());
        }
    }

    @Transactional
    public void deletarMinhaConta(String email) {
        try {
            String emailLogado = getAdmLogadoEmail();
            AdmRestaurante adm = admRepository.findByEmail(emailLogado)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Administrador não autenticado."));

            if (!email.equals(adm.getEmail())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail de confirmação incorreto.");
            }

            restauranteRepository.findByAdmin(adm).ifPresent(restaurante -> {

                List<Refeicao> refeicoes = refeicaoRepository.findByRestaurante(restaurante);
                if(!refeicoes.isEmpty()) {
                    refeicoes.forEach(refeicaoIngredienteRepository::deleteByRefeicao);
                    refeicaoRepository.deleteAll(refeicoes);
                }

                List<Ingrediente> ingredientes = ingredienteRepository.findByAdmin(adm);
                if(!ingredientes.isEmpty()) {
                    ingredientes.forEach(ingredienteRestricaoRepository::deleteByIngrediente);
                    ingredienteRepository.deleteAll(ingredientes);
                }

                usuarioAvaliaRepository.deleteByRestaurante(restaurante);
                restauranteFavoritoRepository.deleteByRestaurante(restaurante);
                restauranteRepository.delete(restaurante);
            });

            socialLoginRepository.deleteByAdm(adm);
            tokenStore.removerTodosTokensDaPessoa(adm.getEmail(), "ADMIN");
            admRepository.delete(adm);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao deletar conta: " + ex.getMessage());
        }
    }

    @Transactional
    public LoginResponseDTO fazerLoginComGoogle(String accessToken) {
        try {
            // 1. Obter dados do usuário Google
            GooglePeopleApiService.GoogleUserInfo googleUser = googlePeopleApiService.getUserInfo(accessToken);
            String email = googleUser.getEmail();
            String nome = googleUser.getName();
            String googleId = googleUser.getId();

            // 2. Verificar se já existe um SocialLogin com esse provider e providerId
            Optional<SocialLogin> socialLoginExistente = socialLoginRepository.findByProviderAndProviderId("google", googleId);
            if (socialLoginExistente.isPresent()) {
                SocialLogin sl = socialLoginExistente.get();
                // Verificar se está vinculado a um Usuario
                if (sl.getUsuario() != null) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, 
                        "Esta conta Google já está vinculada a uma conta de Usuário.");
                }
                // Se está vinculado a um Admin, continuar o fluxo normal
            }

            // 3. Verificar se já existe AdmRestaurante com esse e-mail
            AdmRestaurante adm = admRepository.findByEmail(email).orElse(null);
            if (adm == null) {
                // 4. Se não existe, criar novo AdmRestaurante e associar SocialLogin
                adm = new AdmRestaurante();
                adm.setNome(nome);
                adm.setEmail(email);
                adm.setSenha(null); // Não tem senha
                adm = admRepository.save(adm);

                // Criar e associar SocialLogin
        SocialLogin socialLogin = SocialLogin.builder()
                        .usuario(null)
                        .adm(adm)
                        .provider("google")
                        .providerId(googleId)
                        .build();
                socialLoginRepository.save(socialLogin);
                adm.getSocialLogins().add(socialLogin);
            } else {
                // 5. Se já existe, garantir que o SocialLogin está associado
                boolean hasGoogle = adm.getSocialLogins().stream()
                        .anyMatch(sl -> "google".equals(sl.getProvider()) && googleId.equals(sl.getProviderId()));
                if (!hasGoogle) {
            SocialLogin socialLogin = SocialLogin.builder()
                .adm(adm)
                .provider("google")
                .providerId(googleId)
                .build();
                    socialLoginRepository.save(socialLogin);
                    adm.getSocialLogins().add(socialLogin);
                }
            }
            admRepository.save(adm);

            // 6. Gerar token JWT e retornar
            String token = jwtUtil.gerarToken(adm.getEmail(), "ADMIN");
            tokenStore.salvarToken(token);
            return LoginMapper.toResponse(adm, token);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao fazer login com Google: " + ex.getMessage());
        }
    }

    @Transactional
    public LoginResponseDTO fazerLogin(LoginRequestDTO dto) {
        try {
            AdmRestaurante adm = admRepository.findByEmail(dto.getEmail())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha incorreta"));

            if (!encoder.matches(dto.getSenha(), adm.getSenha())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha incorreta");
            }

            String token = jwtUtil.gerarToken(adm.getEmail(), "ADMIN");
            tokenStore.salvarToken(token);

            return LoginMapper.toResponse(adm, token);

        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao fazer login: " + ex.getMessage());
        }
    }

    public void logout(String header) {
        try {
            if (header == null || !header.startsWith("Bearer ")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido ou ausente");
            }
            String token = header.substring(7);
            tokenStore.removerToken(token);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao fazer logout: " + ex.getMessage());
        }
    }

    public void logoutGlobal() {
        try {
            String emailLogado = getAdmLogadoEmail();
            tokenStore.removerTodosTokensDaPessoa(emailLogado, "ADMIN");
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao fazer logout global: " + ex.getMessage());
        }
    }

    @Transactional
    public void enviarCodigoRedefinicaoSenha(String email) {
        try {
            AdmRestaurante adm = admRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "E-mail não encontrado."));
            
            if (adm.getSenha() == null || adm.getSenha().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Administrador criado via social login. Não é possível redefinir senha.");
            }
            
            String codigo = verificaEmailService.gerarESalvarCodigo(email);
            
            String subject = "Meal4You - Código de Redefinição de Senha";
            String body = "Olá! \n\nEsse é o seu código para redefinir sua senha: \n\n" + codigo + 
                        "\n\n ATENÇÃO: O CÓDIGO É VÁLIDO SOMENTE POR 5 MINUTOS.";
            emailCodeSenderService.enviarEmail(email, subject, body);
            
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao enviar código de redefinição de senha: " + ex.getMessage());
        }
    }

    @Transactional
    public void redefinirSenha(RedefinirSenhaRequestDTO dto) {
        try {

            if (!verificaEmailService.validarCodigo(dto.getEmail(), dto.getCodigoVerificacao())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Código de verificação inválido ou expirado.");
            }
            
            AdmRestaurante adm = admRepository.findByEmail(dto.getEmail())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Administrador não encontrado."));
            
            adm.setSenha(encoder.encode(dto.getNovaSenha()));
            admRepository.save(adm);
                        
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao redefinir senha: " + ex.getMessage());
        }
    }
}
