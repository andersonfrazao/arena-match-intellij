package br.com.arenamatch.enums;

import java.util.Arrays;

public enum Categoria {
    ESPORTE("Esporte (Livre)"),
    MESCLADO("Mesclado Esp.+Vet.(35+)"),
    VETERANO_35("Veterano (35+)"),
    VETERANO_40("Veterano (40+)"),
    MASTER("Master (50+)"),
    SUB20("Sub-20");

    private final String descricao;

    Categoria(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static Categoria from(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        String normalizado = valor.trim();
        return Arrays.stream(values())
                .filter(categoria -> categoria.name().equalsIgnoreCase(normalizado)
                        || categoria.getDescricao().equalsIgnoreCase(normalizado))
                .findFirst()
                .orElseGet(() -> fromLegacyValue(normalizado));
    }

    private static Categoria fromLegacyValue(String valor) {
        if ("Esporte".equalsIgnoreCase(valor)) {
            return ESPORTE;
        }
        if ("Veterano".equalsIgnoreCase(valor)) {
            return VETERANO_35;
        }
        if ("Master".equalsIgnoreCase(valor)) {
            return MASTER;
        }
        throw new IllegalArgumentException("Categoria invalida: " + valor);
    }
}
