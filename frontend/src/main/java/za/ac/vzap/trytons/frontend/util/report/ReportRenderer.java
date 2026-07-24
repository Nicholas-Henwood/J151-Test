package za.ac.vzap.trytons.frontend.util.report;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import za.ac.vzap.trytons.frontend.client.admin.SystemReportResponse;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Turns a {@link SystemReportResponse} into a human-readable report, either as a
 * styled HTML page (for viewing in the browser) or a PDF document (for download).
 * <p>
 * The backend stores each report's payload as a {@code Map<String,Object>} whose
 * entries are either scalar summary values (counts, a season, ...) or lists of
 * uniform row objects (users, fixtures, ...). Both output formats are produced
 * from the same {@link #buildSections} traversal so the view and the download
 * always agree.
 */
public final class ReportRenderer {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Brand palette (also used by the PDF).
    private static final Color BRAND_GREEN = new Color(0x0B, 0x3D, 0x2E);
    private static final Color ROW_ALT = new Color(0xF1, 0xF5, 0xF2);
    private static final Color BORDER = new Color(0xD5, 0xDD, 0xD7);

    private ReportRenderer() {
    }

    /** A summary block or a data table, ready to render in either format. */
    private static final class Section {
        String heading;
        List<String> columns = new ArrayList<>();
        List<List<String>> rows = new ArrayList<>();
    }

    // ------------------------------------------------------------------
    // Shared traversal
    // ------------------------------------------------------------------

    private static List<Section> buildSections(Map<String, Object> result) {
        List<Section> sections = new ArrayList<>();

        Section summary = new Section();
        summary.heading = "Summary";
        summary.columns = List.of("Metric", "Value");

        List<Section> tables = new ArrayList<>();

        if (result != null) {
            for (Map.Entry<String, Object> entry : result.entrySet()) {
                String label = humanize(entry.getKey());
                Object value = entry.getValue();

                if (value instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Map) {
                    tables.add(tableSection(label, list));
                } else if (value instanceof List<?> list) {
                    summary.rows.add(List.of(label, joinScalars(list)));
                } else if (value instanceof Map<?, ?> map) {
                    tables.add(mapSection(label, map));
                } else {
                    summary.rows.add(List.of(label, stringify(value)));
                }
            }
        }

        if (!summary.rows.isEmpty()) {
            sections.add(summary);
        }
        sections.addAll(tables);
        return sections;
    }

    /** Build a table from a list of uniform row maps (columns = union of keys). */
    private static Section tableSection(String heading, List<?> list) {
        Section section = new Section();
        section.heading = heading;

        LinkedHashSet<String> keys = new LinkedHashSet<>();
        for (Object row : list) {
            if (row instanceof Map<?, ?> map) {
                for (Object key : map.keySet()) {
                    keys.add(String.valueOf(key));
                }
            }
        }

        List<String> displayColumns = new ArrayList<>();
        for (String key : keys) {
            displayColumns.add(humanize(key));
        }
        section.columns = displayColumns;

        for (Object row : list) {
            if (!(row instanceof Map<?, ?> map)) {
                continue;
            }
            List<String> cells = new ArrayList<>();
            for (String key : keys) {
                cells.add(stringify(map.get(key)));
            }
            section.rows.add(cells);
        }
        return section;
    }

    /** Render a nested map as a two-column key/value table. */
    private static Section mapSection(String heading, Map<?, ?> map) {
        Section section = new Section();
        section.heading = heading;
        section.columns = List.of("Field", "Value");
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            section.rows.add(List.of(humanize(String.valueOf(entry.getKey())), stringify(entry.getValue())));
        }
        return section;
    }

    // ------------------------------------------------------------------
    // HTML
    // ------------------------------------------------------------------

    public static String toHtml(SystemReportResponse report, String contextPath) {
        List<Section> sections = buildSections(report.getResultJson());

        StringBuilder html = new StringBuilder(4096);
        html.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n")
                .append("<meta charset=\"UTF-8\">\n")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
                .append("<title>").append(esc(report.getReportTitle())).append(" — System Report</title>\n")
                .append("<style>").append(css()).append("</style>\n")
                .append("</head>\n<body>\n<div class=\"report\">\n");

        // Header
        html.append("<header class=\"report-head\">\n")
                .append("<p class=\"eyebrow\">Fantasy TryTons · System Report</p>\n")
                .append("<h1>").append(esc(report.getReportTitle())).append("</h1>\n")
                .append("<div class=\"meta\">")
                .append("<span><strong>Type</strong> ").append(esc(humanize(report.getReportType()))).append("</span>")
                .append("<span><strong>Generated</strong> ")
                .append(report.getGeneratedAt() != null ? esc(report.getGeneratedAt().format(STAMP)) : "—")
                .append("</span>")
                .append("<span><strong>Report ID</strong> ").append(esc(String.valueOf(report.getReportId()))).append("</span>")
                .append("</div>\n");

        Map<String, Object> params = report.getParametersJson();
        if (params != null && !params.isEmpty()) {
            html.append("<div class=\"params\">");
            for (Map.Entry<String, Object> p : params.entrySet()) {
                html.append("<span class=\"chip\">").append(esc(humanize(p.getKey())))
                        .append(": ").append(esc(stringify(p.getValue()))).append("</span>");
            }
            html.append("</div>\n");
        }
        html.append("</header>\n");

        // Toolbar (print / download)
        html.append("<div class=\"toolbar\">")
                .append("<button type=\"button\" onclick=\"window.print()\">Print</button>")
                .append("<a class=\"btn\" href=\"").append(esc(contextPath))
                .append("/admin/reports?download=").append(esc(String.valueOf(report.getReportId())))
                .append("\">Download PDF</a>")
                .append("</div>\n");

        // Body
        if (sections.isEmpty()) {
            html.append("<p class=\"empty\">This report contains no data.</p>\n");
        } else {
            for (Section section : sections) {
                html.append("<section class=\"block\">\n<h2>").append(esc(section.heading)).append("</h2>\n");
                html.append("<div class=\"table-wrap\"><table>\n<thead><tr>");
                for (String col : section.columns) {
                    html.append("<th>").append(esc(col)).append("</th>");
                }
                html.append("</tr></thead>\n<tbody>\n");
                for (List<String> row : section.rows) {
                    html.append("<tr>");
                    for (String cell : row) {
                        html.append("<td>").append(esc(cell)).append("</td>");
                    }
                    html.append("</tr>\n");
                }
                html.append("</tbody>\n</table></div>\n</section>\n");
            }
        }

        html.append("</div>\n</body>\n</html>");
        return html.toString();
    }

    private static String css() {
        return "*{box-sizing:border-box}"
                + "body{margin:0;padding:32px 20px;background:#0b1a13;color:#e9efe9;"
                + "font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;line-height:1.5}"
                + ".report{max-width:960px;margin:0 auto;background:#10241a;border:1px solid #23392c;"
                + "border-radius:14px;padding:34px 34px 40px;box-shadow:0 18px 48px rgba(0,0,0,.35)}"
                + ".eyebrow{margin:0 0 6px;text-transform:uppercase;letter-spacing:.14em;font-size:.72rem;"
                + "font-weight:700;color:#c8a24a}"
                + "h1{margin:0 0 16px;font-size:1.9rem;line-height:1.15;color:#f4efe2}"
                + ".meta{display:flex;flex-wrap:wrap;gap:8px 22px;font-size:.85rem;color:#9db0a2}"
                + ".meta strong{color:#c8d6c9;font-weight:600;margin-right:5px;text-transform:uppercase;"
                + "letter-spacing:.04em;font-size:.72rem}"
                + ".params{margin-top:14px;display:flex;flex-wrap:wrap;gap:8px}"
                + ".chip{background:#1c3527;border:1px solid #2e4a39;border-radius:999px;padding:4px 12px;"
                + "font-size:.78rem;color:#d7e2d8}"
                + ".toolbar{display:flex;gap:10px;margin:24px 0 8px}"
                + ".toolbar button,.toolbar .btn{appearance:none;cursor:pointer;border-radius:8px;padding:9px 16px;"
                + "font-size:.85rem;font-weight:600;text-decoration:none;border:1px solid #2e4a39;"
                + "background:#1c3527;color:#e9efe9}"
                + ".toolbar .btn{background:#c8a24a;border-color:#c8a24a;color:#20170a}"
                + ".block{margin-top:26px}"
                + "h2{margin:0 0 12px;font-size:1.12rem;color:#f4efe2;padding-bottom:8px;border-bottom:1px solid #23392c}"
                + ".table-wrap{overflow-x:auto}"
                + "table{width:100%;border-collapse:collapse;font-size:.86rem}"
                + "th,td{text-align:left;padding:9px 12px;border-bottom:1px solid #23392c;white-space:nowrap}"
                + "thead th{background:#193024;color:#c8d6c9;text-transform:uppercase;letter-spacing:.04em;"
                + "font-size:.72rem;font-weight:700}"
                + "tbody tr:nth-child(even){background:#132719}"
                + "td{color:#dbe5dc}"
                + ".empty{margin-top:24px;color:#9db0a2}"
                + "@media print{body{background:#fff;color:#111;padding:0}"
                + ".report{box-shadow:none;border:none;background:#fff;color:#111}"
                + ".toolbar{display:none}h1,h2,.eyebrow{color:#0b3d2e}"
                + "thead th{background:#0b3d2e;color:#fff}tbody tr:nth-child(even){background:#f1f5f2}"
                + "td,.meta,.chip{color:#222}}";
    }

    // ------------------------------------------------------------------
    // PDF
    // ------------------------------------------------------------------

    public static void writePdf(SystemReportResponse report, OutputStream out) throws IOException {
        List<Section> sections = buildSections(report.getResultJson());
        Document document = new Document(PageSize.A4, 42, 42, 48, 44);
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BRAND_GREEN);
            Font eyebrowFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new Color(0xB0, 0x8A, 0x36));
            Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(0x55, 0x66, 0x5B));

            Paragraph eyebrow = new Paragraph("FANTASY TRYTONS · SYSTEM REPORT", eyebrowFont);
            eyebrow.setSpacingAfter(4);
            document.add(eyebrow);

            Paragraph title = new Paragraph(nz(report.getReportTitle()), titleFont);
            title.setSpacingAfter(6);
            document.add(title);

            StringBuilder meta = new StringBuilder();
            meta.append("Type: ").append(humanize(report.getReportType()));
            if (report.getGeneratedAt() != null) {
                meta.append("      Generated: ").append(report.getGeneratedAt().format(STAMP));
            }
            meta.append("      Report ID: ").append(report.getReportId());
            document.add(new Paragraph(meta.toString(), metaFont));

            Map<String, Object> params = report.getParametersJson();
            if (params != null && !params.isEmpty()) {
                StringBuilder p = new StringBuilder("Parameters: ");
                boolean first = true;
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    if (!first) {
                        p.append("   ");
                    }
                    p.append(humanize(entry.getKey())).append(" ").append(stringify(entry.getValue()));
                    first = false;
                }
                Paragraph paramPara = new Paragraph(p.toString(), metaFont);
                paramPara.setSpacingBefore(2);
                document.add(paramPara);
            }

            if (sections.isEmpty()) {
                Font body = FontFactory.getFont(FontFactory.HELVETICA, 11, new Color(0x44, 0x44, 0x44));
                Paragraph none = new Paragraph("This report contains no data.", body);
                none.setSpacingBefore(20);
                document.add(none);
            } else {
                for (Section section : sections) {
                    addPdfSection(document, section);
                }
            }
        } catch (DocumentException e) {
            throw new IOException("Failed to build PDF report", e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }

    private static void addPdfSection(Document document, Section section) throws DocumentException {
        Font headingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, BRAND_GREEN);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(0x20, 0x20, 0x20));

        Paragraph heading = new Paragraph(section.heading, headingFont);
        heading.setSpacingBefore(18);
        heading.setSpacingAfter(7);
        document.add(heading);

        PdfPTable table = new PdfPTable(Math.max(1, section.columns.size()));
        table.setWidthPercentage(100);
        table.setHeaderRows(1);

        for (String column : section.columns) {
            PdfPCell cell = new PdfPCell(new Phrase(column, headerFont));
            cell.setBackgroundColor(BRAND_GREEN);
            cell.setBorderColor(BORDER);
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(cell);
        }

        int index = 0;
        for (List<String> row : section.rows) {
            Color background = (index++ % 2 == 0) ? Color.WHITE : ROW_ALT;
            for (String value : row) {
                PdfPCell cell = new PdfPCell(new Phrase(value, cellFont));
                cell.setBackgroundColor(background);
                cell.setBorderColor(BORDER);
                cell.setPadding(5);
                table.addCell(cell);
            }
        }
        document.add(table);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /** "activeUserCount" / "ACTIVE_USERS" -> "Active User Count" / "Active Users". */
    static String humanize(String key) {
        if (key == null || key.isBlank()) {
            return "";
        }
        String spaced = key
                .replaceAll("([a-z0-9])([A-Z])", "$1 $2")
                .replace('_', ' ')
                .replace('-', ' ')
                .trim();
        String[] parts = spaced.split("\\s+");
        StringBuilder out = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (out.length() > 0) {
                out.append(' ');
            }
            out.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                out.append(part.substring(1).toLowerCase());
            }
        }
        return out.toString();
    }

    private static String joinScalars(List<?> list) {
        StringBuilder sb = new StringBuilder();
        for (Object item : list) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(stringify(item));
        }
        return sb.length() == 0 ? "—" : sb.toString();
    }

    private static String stringify(Object value) {
        if (value == null) {
            return "—";
        }
        String text = String.valueOf(value);
        return text.isBlank() ? "—" : text;
    }

    private static String nz(String value) {
        return value == null ? "" : value;
    }

    private static String esc(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '&' -> out.append("&amp;");
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                case '"' -> out.append("&quot;");
                case '\'' -> out.append("&#39;");
                default -> out.append(c);
            }
        }
        return out.toString();
    }
}
