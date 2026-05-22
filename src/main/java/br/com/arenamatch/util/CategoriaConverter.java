package br.com.arenamatch.util;

import br.com.arenamatch.enums.Categoria;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class CategoriaConverter implements AttributeConverter<Categoria, String> {

    @Override
    public String convertToDatabaseColumn(Categoria categoria) {
        return categoria == null ? null : categoria.name();
    }

    @Override
    public Categoria convertToEntityAttribute(String valor) {
        return Categoria.from(valor);
    }
}
