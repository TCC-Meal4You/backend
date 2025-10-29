package com.api.meal4you.service;

import java.util.List;

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
import com.api.meal4you.entity.AdmRestaurante;
import com.api.meal4you.entity.Ingrediente;
import com.api.meal4you.entity.Refeicao;
import com.api.meal4you.mapper.AdmRestauranteMapper;
import com.api.meal4you.mapper.LoginMapper;
import com.api.meal4you.repository.AdmRestauranteRepository;
import com.api.meal4you.repository.IngredienteRepository;
import com.api.meal4you.repository.IngredienteRestricaoRepository;
import com.api.meal4you.repository.RefeicaoIngredienteRepository;
import com.api.meal4you.repository.RefeicaoRepository;
import com.api.meal4you.repository.RestauranteRepository;
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
    private final RestauranteRepository restauranteRepository;
    private final IngredienteRepository ingredienteRepository;
    private final IngredienteRestricaoRepository ingredienteRestricaoRepository;
    private final RefeicaoRepository refeicaoRepository;
    private final RefeicaoIngredienteRepository refeicaoIngredienteRepository;

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

    public void enviarCodigoVerificacao(String email) {
        try {
            if (admRepository.findByEmail(email).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado");
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
    public AdmRestauranteResponseDTO atualizarEmail(String novoEmail, String codigoVerificacao) {
        try {
            String emailLogado = getAdmLogadoEmail();
            AdmRestaurante adm = admRepository.findByEmail(emailLogado)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Administrador não autenticado."));

            if (!verificaEmailService.validarCodigo(novoEmail, codigoVerificacao)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código de verificação inválido ou expirado.");
            }

            tokenStore.removerTodosTokensDoUsuario(adm.getEmail());
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

            if (admRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado");
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

            if (dto.getSenha() != null && !dto.getSenha().isBlank()
                    && !encoder.matches(dto.getSenha(), adm.getSenha())) {
                tokenStore.removerTodosTokensDoUsuario(adm.getEmail());
                adm.setSenha(encoder.encode(dto.getSenha()));
                alterado = true;
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
    public void deletarMinhaConta(String senha) {
        try {
            String emailLogado = getAdmLogadoEmail();
            AdmRestaurante adm = admRepository.findByEmail(emailLogado)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Administrador não autenticado."));

            if (!encoder.matches(senha, adm.getSenha())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha incorreta");
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

                restauranteRepository.delete(restaurante);
            });

            tokenStore.removerTodosTokensDoUsuario(adm.getEmail());
            admRepository.delete(adm);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao deletar conta: " + ex.getMessage());
        }
    }

    @Transactional
    public LoginResponseDTO fazerLogin(LoginRequestDTO dto) {
        try {
            AdmRestaurante adm = admRepository.findByEmail(dto.getEmail())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou senha incorreta"));

            if (!encoder.matches(dto.getSenha(), adm.getSenha())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou senha incorreta");
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
            tokenStore.removerTodosTokensDoUsuario(emailLogado);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao fazer logout global: " + ex.getMessage());
        }
    }
}
