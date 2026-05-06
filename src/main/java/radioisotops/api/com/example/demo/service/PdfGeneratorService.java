/*
================================================================================
PROJECT:       [RADIOISOTOPO]
VERSION:       1.0.0
DESCRIPTION:   [Parte de PdfGeneratorService]
AUTHOR:        [Marcos, Wael]
UPDATED:       [06/05/2026]
================================================================================
*/
package radioisotops.api.com.example.demo.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import org.springframework.stereotype.Service;
import radioisotops.api.com.example.demo.model.Patient;
import radioisotops.api.com.example.demo.model.Treatment;

import jakarta.servlet.http.HttpServletResponse;
import java.awt.Color;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGeneratorService {

    private static final Color COLOR_PRIMARY = new Color(0x37517C);   // Azul oscuro
    private static final Color COLOR_ACCENT = new Color(0x4098FF);    // Azul claro
    private static final Color COLOR_LIGHT_GRAY = new Color(0xEBEBEB); // Fondo
    private static final Color COLOR_SUCCESS = new Color(0x42AD4B);   // Verde
    private static final Color COLOR_TEXT_DARK = new Color(0x2C3E50);

    public void exportarInformeAlta(HttpServletResponse response, Patient patient, Treatment treatment, double actividadActual, String estado) throws IOException {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);

        PdfPCell titleCell = new PdfPCell(new Phrase("CERTIFICADO DE ALTA RADIOLÓGICA",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.WHITE)));
        titleCell.setBackgroundColor(COLOR_PRIMARY);
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        titleCell.setPadding(15);
        titleCell.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(titleCell);

        document.add(headerTable);

        Paragraph sub = new Paragraph("Portal de Monitorización Nuclear - radioisotopo.portal",
                FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_ACCENT));
        sub.setAlignment(Paragraph.ALIGN_RIGHT);
        document.add(sub);

        document.add(new Paragraph("Fecha de emisión: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9)));

        document.add(new Paragraph(" ")); // Espaciado

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingBefore(10f);

        addStyledCell(infoTable, "DATOS DEL PACIENTE", COLOR_PRIMARY, true, 2);
        addKeyValueCell(infoTable, "Nombre Completo:", patient.getUser().getNombreCompleto());
        addKeyValueCell(infoTable, "DNI/CIP:", patient.getDni());
        addKeyValueCell(infoTable, "Centro Hospitalario:", patient.getUser().getHospitalRef());

        document.add(infoTable);
        document.add(new Paragraph(" "));

        PdfPTable treatmentTable = new PdfPTable(2);
        treatmentTable.setWidthPercentage(100);

        addStyledCell(treatmentTable, "DETALLES DEL TRATAMIENTO Y ACTIVIDAD", COLOR_PRIMARY, true, 2);
        addKeyValueCell(treatmentTable, "Isótopo Administrado:", treatment.getRadioisotopo());
        addKeyValueCell(treatmentTable, "Dosis Inicial:", String.format("%.2f MBq", treatment.getDosis()));
        addKeyValueCell(treatmentTable, "Actividad Actual Estimada:", String.format("%.2f MBq", actividadActual));

        PdfPCell labelEstado = new PdfPCell(new Phrase("Clasificación de Riesgo:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        labelEstado.setBorder(Rectangle.BOTTOM);
        labelEstado.setBorderColor(COLOR_LIGHT_GRAY);
        labelEstado.setPadding(8);
        treatmentTable.addCell(labelEstado);

        PdfPCell valueEstado = new PdfPCell(new Phrase(estado.toUpperCase(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
        valueEstado.setBackgroundColor(estado.toLowerCase().contains("bajo") ? COLOR_SUCCESS : COLOR_ACCENT);
        valueEstado.setHorizontalAlignment(Element.ALIGN_CENTER);
        valueEstado.setVerticalAlignment(Element.ALIGN_MIDDLE);
        valueEstado.setPadding(8);
        valueEstado.setBorder(Rectangle.NO_BORDER);
        treatmentTable.addCell(valueEstado);

        document.add(treatmentTable);

        document.add(new Paragraph(" "));
        Paragraph graphTitle = new Paragraph("CURVA DE DECAIMIENTO RADIACTIVO (Proyección 400h)",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, COLOR_PRIMARY));
        graphTitle.setSpacingBefore(15);
        document.add(graphTitle);

        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        PdfContentByte cb = writer.getDirectContent();
        dibujarGraficoDecaimientoPro(cb, treatment.getRadioisotopo(), treatment.getDosis());

        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        PdfPTable footerTable = new PdfPTable(1);
        footerTable.setWidthPercentage(40);
        footerTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

        PdfPCell firmaLine = new PdfPCell(new Phrase(" "));
        firmaLine.setBorder(Rectangle.TOP);
        firmaLine.setBorderWidth(1f);
        footerTable.addCell(firmaLine);

        PdfPCell drCell = new PdfPCell(new Phrase("Dr/a. " + treatment.getDoctor().getUser().getNombreCompleto() + "\nEspecialista en Medicina Nuclear",
                FontFactory.getFont(FontFactory.HELVETICA, 10)));
        drCell.setBorder(Rectangle.NO_BORDER);
        drCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        footerTable.addCell(drCell);

        document.add(footerTable);

        document.close();
    }

    private void addStyledCell(PdfPTable table, String text, Color color, boolean isHeader, int colspan) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE)));
        cell.setBackgroundColor(color);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(6);
        cell.setColspan(colspan);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
    }

    private void addKeyValueCell(PdfPTable table, String key, String value) {
        PdfPCell keyCell = new PdfPCell(new Phrase(key, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COLOR_TEXT_DARK)));
        keyCell.setBorder(Rectangle.BOTTOM);
        keyCell.setBorderColor(COLOR_LIGHT_GRAY);
        keyCell.setPadding(8);
        table.addCell(keyCell);

        PdfPCell valCell = new PdfPCell(new Phrase(value, FontFactory.getFont(FontFactory.HELVETICA, 10)));
        valCell.setBorder(Rectangle.BOTTOM);
        valCell.setBorderColor(COLOR_LIGHT_GRAY);
        valCell.setPadding(8);
        table.addCell(valCell);
    }

    private void dibujarGraficoDecaimientoPro(PdfContentByte cb, String isotopo, double dosisInicial) {
        float xBase = 100;
        float yBase = 260;
        float ancho = 380;
        float alto = 120;

        cb.setRGBColorFill(245, 245, 245);
        cb.rectangle(xBase, yBase, ancho, alto);
        cb.fill();

        cb.setLineWidth(1f);
        cb.setRGBColorStroke(150, 150, 150);
        cb.moveTo(xBase, yBase);
        cb.lineTo(xBase + ancho, yBase); // X
        cb.moveTo(xBase, yBase);
        cb.lineTo(xBase, yBase + alto); // Y
        cb.stroke();

        double tMed = (isotopo.contains("131")) ? 192.48 :
                (isotopo.contains("177")) ? 159.36 :
                        (isotopo.contains("60")) ? 46164.0 : 24.0;

        cb.setLineWidth(2.5f);
        cb.setRGBColorStroke(64, 152, 255); // Usamos COLOR_ACCENT (4098FF)

        for (int t = 0; t <= 400; t += 2) {
            double act = dosisInicial * Math.pow(0.5, (double) t / tMed);
            float px = xBase + (float) ((t / 400.0) * ancho);
            float py = yBase + (float) ((act / dosisInicial) * alto);

            if (t == 0) cb.moveTo(px, py);
            else cb.lineTo(px, py);
        }
        cb.stroke();

        cb.beginText();
        try {
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            cb.setFontAndSize(bf, 7);
            cb.showTextAligned(Element.ALIGN_LEFT, "0h", xBase, yBase - 10, 0);
            cb.showTextAligned(Element.ALIGN_RIGHT, "400h (Tiempo)", xBase + ancho, yBase - 10, 0);
            cb.showTextAligned(Element.ALIGN_LEFT, "Actividad (MBq)", xBase - 45, yBase + alto + 5, 0);
        } catch (Exception e) { e.printStackTrace(); }
        cb.endText();
    }
}