package com.api.meal4you.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.api.meal4you.dto.*;
import com.api.meal4you.entity.*;
import com.api.meal4you.repository.*;

import com.api.meal4you.mapper.UsuarioAvaliaMapper;
import com.api.meal4you.mapper.LoginMapper;
import com.api.meal4you.mapper.UsuarioMapper;
import com.api.meal4you.security.JwtUtil;
import com.api.meal4you.security.TokenStore;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RestricaoRepository restricaoRepository;
    private final SocialLoginRepository socialLoginRepository;
    private final UsuarioRestricaoRepository usuarioRestricaoRepository;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;
    private final TokenStore tokenStore;
    private final VerificaEmailService verificaEmailService;
    private final EmailCodeSenderService emailCodeSenderService;
    private final GooglePeopleApiService googlePeopleApiService;
    private final RestauranteRepository restauranteRepository;
    private final UsuarioAvaliaRepository usuarioAvaliaRepository;
    private final RestauranteFavoritoRepository restauranteFavoritoRepository;

    public String getUsuarioLogadoEmail() {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getPrincipal() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
            }
            return authentication.getPrincipal().toString();
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao obter usuário logado: " + ex.getMessage());
        }
    }

    @Transactional
    public void enviarCodigoVerificacao(String email) {
        try {
            if (usuarioRepository.findByEmail(email).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
            }
            String codigo = verificaEmailService.gerarESalvarCodigo(email);
            String subject = "Meal4You - Código de Verificação";
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
            if (usuarioRepository.findByEmail(novoEmail).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Não há alteração, o e-mail é o mesmo.");
            }

            String emailLogado = getUsuarioLogadoEmail(); // Pega e-mail do usuário logado
            Usuario usuario = usuarioRepository.findByEmail(emailLogado) // Pega o "objeto" do usuário logado
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado."));

            if (usuario.getSenha() == null || usuario.getSenha().isBlank()) { // <--- Isso serve para bloquear a alteração de e-mail para usuários que logam via social login (ou seja, sem senha cadastrada no banco)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário criado via social login. Não pode alterar e-mail.");
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
    public UsuarioResponseDTO cadastrarUsuario(UsuarioRequestDTO dto) {
        try {
            if (!verificaEmailService.validarCodigo(dto.getEmail(), dto.getCodigoVerificacao())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código de verificação inválido ou expirado.");
            }

            if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
            }
            Usuario usuario = UsuarioMapper.toEntity(dto);
            usuario.setSenha(encoder.encode(usuario.getSenha()));
            usuarioRepository.saveAndFlush(usuario);
            return UsuarioMapper.toResponse(usuario);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao cadastrar usuário: " + ex.getMessage());
        }
    }

    @Transactional
    public UsuarioResponseDTO buscarMeuPerfil() {
        try {
            String emailLogado = getUsuarioLogadoEmail();
            Usuario usuario = usuarioRepository.findByEmail(emailLogado)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado."));
            return UsuarioMapper.toResponse(usuario);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao buscar perfil do usuário: " + ex.getMessage());
        }
    }

    @Transactional
    public void deletarMinhaConta(String email) {
        try {
            String emailLogado = getUsuarioLogadoEmail();
            Usuario usuario = usuarioRepository.findByEmail(emailLogado)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado."));

            if (!email.equals(usuario.getEmail())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail incorreto");
            }

            tokenStore.removerTodosTokensDaPessoa(usuario.getEmail(), "USUARIO");
            socialLoginRepository.deleteByUsuario(usuario);
            usuarioRestricaoRepository.deleteByUsuario(usuario);
            usuarioAvaliaRepository.deleteByUsuario(usuario);
            restauranteFavoritoRepository.deleteByUsuario(usuario);
            usuarioRepository.delete(usuario);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao deletar usuário: " + ex.getMessage());
        }
    }

    @Transactional
    public UsuarioResponseDTO atualizarEmail(String novoEmail, String codigoVerificacao) {
        try {
            String emailLogado = getUsuarioLogadoEmail();
            Usuario usuario = usuarioRepository.findByEmail(emailLogado)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado."));

            if (!verificaEmailService.validarCodigo(novoEmail, codigoVerificacao)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código de verificação inválido ou expirado.");
            }

            tokenStore.removerTodosTokensDaPessoa(usuario.getEmail(), "USUARIO");
            usuario.setEmail(novoEmail);
            usuarioRepository.save(usuario);
            return UsuarioMapper.toResponse(usuario);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao atualizar e-mail: " + ex.getMessage());
        }
    }

    @Transactional
    public UsuarioResponseDTO atualizarMeuPerfil(UsuarioRequestDTO dto) {
        try {
            String emailLogado = getUsuarioLogadoEmail();
            Usuario usuario = usuarioRepository.findByEmail(emailLogado)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado."));

            boolean alterado = false;

            if (dto.getNome() != null && !dto.getNome().isBlank() && !dto.getNome().equals(usuario.getNome())) {
                usuario.setNome(dto.getNome());
                alterado = true;
            }
            if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
                if (usuario.getSenha() == null || usuario.getSenha().isBlank()) { // <--- Isso bloqueia a alteração de senha se o usuário não tem senha cadastrada (criou conta via social login)
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário criado via social login. Não pode definir senha.");
                }
                if (!encoder.matches(dto.getSenha(), usuario.getSenha())) {
                    tokenStore.removerTodosTokensDaPessoa(usuario.getEmail(), "USUARIO");
                    usuario.setSenha(encoder.encode(dto.getSenha()));
                    alterado = true;
                }
            }

            if (!alterado) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nenhuma alteração detectada.");
            }

            usuarioRepository.save(usuario);
            return UsuarioMapper.toResponse(usuario);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao atualizar usuário: " + ex.getMessage());
        }
    }

    @Transactional
    public UsuarioResponseDTO atualizarMinhasRestricoes(UsuarioRestricaoRequestDTO dto) {
        try {
            String emailLogado = getUsuarioLogadoEmail();
            Usuario usuario = usuarioRepository.findByEmail(emailLogado)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado."));

            usuarioRestricaoRepository.deleteByUsuario(usuario);

            usuario.getUsuarioRestricoes().clear();

            if (dto.getRestricaoIds() == null || dto.getRestricaoIds().isEmpty()) {
                return UsuarioMapper.toResponse(usuario);
            }

            List<Restricao> novasRestricoes = restricaoRepository.findAllById(dto.getRestricaoIds());

            if (novasRestricoes.size() != dto.getRestricaoIds().size()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Um ou mais IDs de restrição são inválidos.");
            }

            List<UsuarioRestricao> novasAssociacoes = novasRestricoes.stream()
                    .map(restricao -> UsuarioRestricao.builder()
                            .usuario(usuario)
                            .restricao(restricao)
                            .build())
                    .collect(Collectors.toList());

            usuarioRestricaoRepository.saveAll(novasAssociacoes);

            usuario.setUsuarioRestricoes(novasAssociacoes);
            return UsuarioMapper.toResponse(usuario);

        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao atualizar as restrições do usuário: " + ex.getMessage());
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

            // 2. Verificar se já existe usuário com esse e-mail
            Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
            if (usuario == null) {
                // 3. Se não existe, criar novo usuário e associar SocialLogin
                usuario = new Usuario();
                usuario.setNome(nome);
                usuario.setEmail(email);
                usuario.setSenha(null); // Não tem senha
                usuario = usuarioRepository.save(usuario);

                // Criar e associar SocialLogin
                SocialLogin socialLogin = SocialLogin.builder()
                        .usuario(usuario)
                        .adm(null)
                        .provider("google")
                        .providerId(googleId)
                        .build();
                socialLoginRepository.save(socialLogin);
                usuario.getSocialLogins().add(socialLogin);
            } else {
                // 4. Se já existe, garantir que o SocialLogin está associado
                boolean hasGoogle = usuario.getSocialLogins().stream()
                        .anyMatch(sl -> "google".equals(sl.getProvider()) && googleId.equals(sl.getProviderId()));
                if (!hasGoogle) {
                    SocialLogin socialLogin = SocialLogin.builder()
                            .usuario(usuario)
                            .provider("google")
                            .providerId(googleId)
                            .build();
                    socialLoginRepository.save(socialLogin);
                    usuario.getSocialLogins().add(socialLogin);
                }
            }
            usuarioRepository.save(usuario);

            // 5. Gerar token JWT e retornar
            String token = jwtUtil.gerarToken(usuario.getEmail(), "USUARIO");
            tokenStore.salvarToken(token);
            return LoginMapper.toResponse(usuario, token);
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
            Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                    .orElseThrow(
                            () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou senha incorreta"));

            if (!encoder.matches(dto.getSenha(), usuario.getSenha())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou senha incorreta");
            }

            String token = jwtUtil.gerarToken(usuario.getEmail(), "USUARIO");
            tokenStore.salvarToken(token);

            return LoginMapper.toResponse(usuario, token);
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
            String emailLogado = getUsuarioLogadoEmail();
            tokenStore.removerTodosTokensDaPessoa(emailLogado, "USUARIO");
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao fazer logout global: " + ex.getMessage());
        }
    }

    @Transactional
    public UsuarioAvaliaResponseDTO avaliarRestaurante(UsuarioAvaliaRequestDTO dto) {
        String emailLogado = getUsuarioLogadoEmail();
        Usuario usuario = usuarioRepository.findByEmail(emailLogado)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado."));

        Restaurante restaurante = restauranteRepository.findById(dto.getIdRestaurante())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurante não encontrado."));
                
        if (usuarioAvaliaRepository.existsByUsuarioAndRestaurante(usuario, restaurante)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Você já avaliou este restaurante.");
        }

        if (dto.getNota() < 0 || dto.getNota() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nota inválida. Deve estar entre 0 e 5.");
        }

        UsuarioAvalia avaliacao = UsuarioAvaliaMapper.toEntity(dto, usuario, restaurante);
        avaliacao.setDataAvaliacao(LocalDate.now());
        usuarioAvaliaRepository.save(avaliacao);

        return UsuarioAvaliaMapper.toResponse(avaliacao);
    }

    @Transactional
    public UsuarioAvaliaResponseDTO atualizarAvaliacao(UsuarioAvaliaRequestDTO dto) {
        String emailLogado = getUsuarioLogadoEmail();
        Usuario usuario = usuarioRepository.findByEmail(emailLogado)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado."));

        Restaurante restaurante = restauranteRepository.findById(dto.getIdRestaurante())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurante não encontrado."));

        UsuarioAvalia avaliacao = usuarioAvaliaRepository.findByUsuarioAndRestaurante(usuario, restaurante)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Avaliação não encontrada."));

        if (dto.getNota() < 0 || dto.getNota() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nota inválida. Deve estar entre 0 e 5.");
        }

        avaliacao.setNota(dto.getNota());
        avaliacao.setComentario(dto.getComentario());
        avaliacao.setDataAvaliacao(LocalDate.now());
        usuarioAvaliaRepository.save(avaliacao);

        return UsuarioAvaliaMapper.toResponse(avaliacao);
    }

    @Transactional
    public UsuarioAvaliaResponseDTO deletarAvaliacao(Integer idRestaurante) {
        String emailLogado = getUsuarioLogadoEmail();
        Usuario usuario = usuarioRepository.findByEmail(emailLogado)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado."));

        Restaurante restaurante = restauranteRepository.findById(idRestaurante)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Restaurante não encontrado."));

        UsuarioAvalia avaliacao = usuarioAvaliaRepository.findByUsuarioAndRestaurante(usuario, restaurante)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avaliação não encontrada."));

        usuarioAvaliaRepository.delete(avaliacao);
        return UsuarioAvaliaMapper.toResponse(avaliacao);
    }

    //só ve a propria avaliacao
    @Transactional
    public List<UsuarioAvaliaResponseDTO> verMinhasAvaliacoes() {
        String emailLogado = getUsuarioLogadoEmail();
        Usuario usuario = usuarioRepository.findByEmail(emailLogado)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado."));

        List<UsuarioAvalia> avaliacoes = usuarioAvaliaRepository.findByUsuario(usuario);
        return avaliacoes.stream()
                .map(UsuarioAvaliaMapper::toResponse)
                .collect(Collectors.toList());
    }
}