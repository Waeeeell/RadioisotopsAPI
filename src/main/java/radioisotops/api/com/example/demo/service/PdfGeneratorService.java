package radioisotops.api.com.example.demo.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import radioisotops.api.com.example.demo.model.Patient;
import radioisotops.api.com.example.demo.model.Treatment;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Service
public class PdfGeneratorService {

    public void exportarInformeAlta(HttpServletResponse response, Patient patient, Treatment treatment, double actividadActual, String estado) throws IOException {
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        // 1. Cabecera y Título
        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("CERTIFICADO DE ALTA RADIOLÓGICA", fontTitle);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" "));

        // 2. Información del Paciente y Tratamiento
        document.add(new Paragraph("Paciente: " + patient.getUser().getNombreCompleto()));
        document.add(new Paragraph("DNI/CIP: " + patient.getDni()));
        document.add(new Paragraph("Isótopo: " + treatment.getRadioisotopo()));
        document.add(new Paragraph("Dosis inicial: " + treatment.getDosis() + " MBq"));
        document.add(new Paragraph("Estado actual: " + estado + " (" + String.format("%.2f", actividadActual) + " MBq)"));
        document.add(new Paragraph(" "));

        // 3. Dibujo de la Curva de Decaimiento (Gráfico)
        document.add(new Paragraph("Representación visual del decaimiento físico:"));
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" ")); // Espacio para el gráfico
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        // Accedemos al "lienzo" del PDF para dibujar
        PdfContentByte cb = writer.getDirectContent();
        dibujarGraficoDecaimiento(cb, treatment.getRadioisotopo(), treatment.getDosis());

        // 4. Firma y Sello
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Firma del facultativo responsable:"));
        document.add(new Paragraph("Dr/a. " + treatment.getDoctor().getUser().getNombreCompleto()));

        document.close();
    }

    private void dibujarGraficoDecaimiento(PdfContentByte cb, String isotopo, double dosisInicial) {
        float xBase = 100;
        float yBase = 450;
        float ancho = 300;
        float alto = 150;

        cb.setLineWidth(1f);
        cb.setRGBColorStroke(0, 0, 0);
        cb.moveTo(xBase, yBase);
        cb.lineTo(xBase + ancho, yBase);
        cb.moveTo(xBase, yBase);
        cb.lineTo(xBase, yBase + alto);
        cb.stroke();

        double tMed = isotopo.contains("I-131") ? 192.48 : (isotopo.contains("Lu-177") ? 159.36 : 6.01);

        cb.setRGBColorStroke(255, 0, 0); // Curva en rojo
        cb.setLineWidth(2f);

        for (int t = 0; t <= 400; t += 5) { // t en horas
            double act = dosisInicial * Math.pow(0.5, (double) t / tMed);
            float px = xBase + (float) ((t / 400.0) * ancho);
            float py = yBase + (float) ((act / dosisInicial) * alto);

            if (t == 0) cb.moveTo(px, py);
            else cb.lineTo(px, py);
        }
        cb.stroke();
    }
}