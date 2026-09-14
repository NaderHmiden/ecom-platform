package com.platform.ecom.supplier.infrastructure;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Isolates the Commons CSV library behind a simple return type — parsing
 * CSV correctly (quoted commas, embedded newlines, varying line endings)
 * is a solved problem; hand-rolling String.split(",") would silently
 * corrupt data the first time a supplier quotes a comma in a product name.
 */
@Component
public class SupplierCsvParser {

    public List<Map<String, String>> parse(InputStream inputStream) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();

        try (CSVParser parser = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .setIgnoreEmptyLines(true)
                .build()
                .parse(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            for (CSVRecord record : parser) {
                Map<String, String> row = new LinkedHashMap<>();
                record.toMap().forEach((key, value) -> row.put(normalizeHeader(key), value));
                rows.add(row);
            }
        }

        return rows;
    }

    // Supplier files rarely agree on header casing/spacing ("SKU", " Sku ").
    // Normalizing here means SupplierRowValidator checks one canonical key
    // ("sku") without every supplier's formatting quirks leaking into
    // validation logic. NOTE: this does NOT map differently-named columns
    // ("sku_code" -> "sku") — that's a real column-mapping feature for
    // later, deliberately out of scope for proving the pipeline mechanics.
    private String normalizeHeader(String header) {
        return header == null ? "" : header.trim().toLowerCase();
    }
}