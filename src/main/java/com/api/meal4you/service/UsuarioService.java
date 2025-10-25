package com.api.meal4you.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.api.meal4you.dto.LoginRequestDTO;
import com.api.meal4you.dto.LoginResponseDTO;
import com.api.meal4you.dto.UsuarioRequestDTO;
import com.api.meal4you.dto.UsuarioResponseDTO;
import com.api.meal4you.dto.UsuarioRestricaoRequestDTO;
import com.api.meal4you.entity.Restricao;
import com.api.meal4you.entity.Usuario;
import com.api.meal4you.entity.UsuarioRestricao;
import com.api.meal4you.mapper.LoginMapper;
import com.api.meal4you.mapper.UsuarioMapper;
import com.api.meal4you.repository.RestricaoRepository;
import com.api.meal4you.repository.UsuarioRepository;
import com.api.meal4you.repository.UsuarioRestricaoRepository;
import com.api.meal4you.security.JwtUtil;
import com.api.meal4you.security.TokenStore;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RestricaoRepository restricaoRepository;
    private final UsuarioRestricaoRepository usuarioRestricaoRepository;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;
    private final TokenStore tokenStore;
    private final VerificaEmailService verificaEmailService;
    private final EmailCodeSenderService emailCodeSenderService;

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
    public UsuarioResponseDTO atualizarEmail(String novoEmail, String codigoVerificacao) {
        try {
            String emailLogado = getUsuarioLogadoEmail();
            Usuario usuario = usuarioRepository.findByEmail(emailLogado)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

            if (!verificaEmailService.validarCodigo(novoEmail, codigoVerificacao)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código de verificação inválido ou expirado.");
            }

            tokenStore.removerTodosTokensDoUsuario(usuario.getEmail());
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
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario não encontrado"));
            return UsuarioMapper.toResponse(usuario);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao buscar perfil do usuário: " + ex.getMessage());
        }
    }

    @Transactional
    public void deletarMinhaConta(String senha) {
        try {
            String emailLogado = getUsuarioLogadoEmail();
            Usuario usuario = usuarioRepository.findByEmail(emailLogado)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

            if (!encoder.matches(senha, usuario.getSenha())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha incorreta");
            }

            tokenStore.removerTodosTokensDoUsuario(usuario.getEmail());
            usuarioRestricaoRepository.deleteByUsuario(usuario);
            usuarioRepository.delete(usuario);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao deletar usuário: " + ex.getMessage());
        }
    }

    @Transactional
    public UsuarioResponseDTO atualizarMeuPerfil(UsuarioRequestDTO dto) {
        try {
            String emailLogado = getUsuarioLogadoEmail();
            Usuario usuario = usuarioRepository.findByEmail(emailLogado)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

            boolean alterado = false;

            if (dto.getNome() != null && !dto.getNome().isBlank() && !dto.getNome().equals(usuario.getNome())) {
                usuario.setNome(dto.getNome());
                alterado = true;
            }
            if (dto.getSenha() != null && !dto.getSenha().isBlank()
                    && !encoder.matches(dto.getSenha(), usuario.getSenha())) {
                tokenStore.removerTodosTokensDoUsuario(usuario.getEmail());
                usuario.setSenha(encoder.encode(dto.getSenha()));
                alterado = true;
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
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

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
            tokenStore.removerTodosTokensDoUsuario(emailLogado);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao fazer logout global: " + ex.getMessage());
        }
    }
}
