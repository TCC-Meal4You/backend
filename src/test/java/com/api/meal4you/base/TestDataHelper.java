package com.api.meal4you.base;

import java.lang.reflect.Field;

/**
 * Classe utilitária para facilitar criação e manipulação de objetos em testes.
 * 
 * Fornece métodos para:
 * - Definir campos privados (via reflection)
 * - Obter valores de campos privados
 * - Facilitar criação de fixtures de teste
 * 
 * Exemplo de uso:
 * <pre>
 * Usuario usuario = new Usuario();
 * TestDataHelper.setFieldValue(usuario, "id", 1L);
 * Object id = TestDataHelper.getFieldValue(usuario, "id");
 * </pre>
 * 
 * @author Meal4You Team
 * @version 1.0.0
 * @since 2026-05-20
 */
public class TestDataHelper {
    
    private TestDataHelper() {
        throw new AssertionError("Não deve ser instantiada");
    }
    
    /**
     * Define um valor em um campo privado via reflection.
     * Útil para testes quando a classe não possui setter público.
     * 
     * @param object o objeto alvo
     * @param fieldName nome do campo
     * @param value valor a ser definido
     * @throws RuntimeException se o campo não existir ou não for acessível
     */
    public static void setFieldValue(Object object, String fieldName, Object value) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(
                String.format("Erro ao definir campo '%s' em %s", fieldName, object.getClass().getName()),
                e
            );
        }
    }
    
    /**
     * Obtém o valor de um campo privado via reflection.
     * 
     * @param object o objeto alvo
     * @param fieldName nome do campo
     * @return o valor do campo
     * @throws RuntimeException se o campo não existir ou não for acessível
     */
    public static Object getFieldValue(Object object, String fieldName) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(
                String.format("Erro ao obter campo '%s' em %s", fieldName, object.getClass().getName()),
                e
            );
        }
    }
}
