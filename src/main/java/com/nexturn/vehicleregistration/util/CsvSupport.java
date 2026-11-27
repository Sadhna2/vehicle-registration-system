package com.nexturn.vehicleregistration.util;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Minimal RFC 4180 CSV writing and reading, with protection against spreadsheet formula injection. */
public final class CsvSupport {

  private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

  private CsvSupport() {}

  /** Builds a CSV file (UTF-8 with BOM so Excel shows non-English characters correctly). */
  public static byte[] write(List<String> header, List<List<Object>> rows) {
    StringBuilder csv = new StringBuilder();
    appendRow(csv, new ArrayList<>(header));
    rows.forEach(row -> appendRow(csv, row));
    byte[] body = csv.toString().getBytes(StandardCharsets.UTF_8);
    byte[] file = new byte[UTF8_BOM.length + body.length];
    System.arraycopy(UTF8_BOM, 0, file, 0, UTF8_BOM.length);
    System.arraycopy(body, 0, file, UTF8_BOM.length, body.length);
    return file;
  }

  private static void appendRow(StringBuilder csv, List<?> values) {
    for (int i = 0; i < values.size(); i++) {
      if (i > 0) {
        csv.append(',');
      }
      csv.append(cell(values.get(i)));
    }
    csv.append("\r\n");
  }

  /**
   * Quotes a value when needed. Text that a spreadsheet would run as a formula (starting with =, +,
   * -, @, tab or carriage return) gets a leading apostrophe so it is shown as plain text.
   */
  static String cell(Object value) {
    if (value == null) {
      return "";
    }
    String text = value.toString();
    if (!(value instanceof Number) && !text.isEmpty() && "=+-@\t\r".indexOf(text.charAt(0)) >= 0) {
      text = "'" + text;
    }
    if (text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
      text = "\"" + text.replace("\"", "\"\"") + "\"";
    }
    return text;
  }

  /** Parses CSV text into rows of fields. Handles quoted fields, doubled quotes and line breaks. */
  public static List<List<String>> parse(String text) {
    if (text.startsWith("﻿")) {
      text = text.substring(1);
    }
    List<List<String>> rows = new ArrayList<>();
    List<String> row = new ArrayList<>();
    StringBuilder field = new StringBuilder();
    boolean quoted = false;
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      if (quoted) {
        if (c == '"' && i + 1 < text.length() && text.charAt(i + 1) == '"') {
          field.append('"');
          i++;
        } else if (c == '"') {
          quoted = false;
        } else {
          field.append(c);
        }
      } else if (c == '"' && field.isEmpty()) {
        quoted = true;
      } else if (c == ',') {
        row.add(field.toString());
        field.setLength(0);
      } else if (c == '\n' || c == '\r') {
        if (c == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n') {
          i++;
        }
        row.add(field.toString());
        field.setLength(0);
        rows.add(row);
        row = new ArrayList<>();
      } else {
        field.append(c);
      }
    }
    if (!field.isEmpty() || !row.isEmpty()) {
      row.add(field.toString());
      rows.add(row);
    }
    rows.removeIf(r -> r.stream().allMatch(String::isBlank));
    return rows;
  }
}
