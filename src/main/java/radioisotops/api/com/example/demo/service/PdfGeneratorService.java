package radioisotops.api.com.example.demo.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import radioisotops.api.com.example.demo.model.Patient;
import radioisotops.api.com.example.demo.model.Treatment;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGeneratorService {

    public void exportarInformeAlta(HttpServletResponse response, Patient patient, Treatment treatment, double actividadActual, String estado) throws IOException {
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        // 1. Cabecera y Titulo
        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("CERTIFICADO DE ALTA RADIOLOGICA", fontTitle);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(title);

        Font fontSub = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Paragraph sub = new Paragraph("Portal de Monitorizacion Nuclear - radioisotopo.portal", fontSub);
        sub.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(sub);

        document.add(new Paragraph(" "));
        document.add(new Paragraph("----------------------------------------------------------------------------------------------------------------------------------"));
        document.add(new Paragraph(" "));

        // 2. Informacion del Paciente y Tratamiento
        document.add(new Paragraph("FECHA DE EMISION: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        document.add(new Paragraph("PACIENTE: " + patient.getUser().getNombreCompleto()));
        document.add(new Paragraph("DNI/CIP: " + patient.getDni()));
        document.add(new Paragraph("HOSPITAL: " + patient.getUser().getHospitalRef()));
        document.add(new Paragraph(" "));

        document.add(new Paragraph("DETALLES DEL TRATAMIENTO:"));
        document.add(new Paragraph("- Isotopo administrado: " + treatment.getRadioisotopo()));
        document.add(new Paragraph("- Dosis inicial: " + String.format("%.2f", treatment.getDosis()) + " MBq"));
        document.add(new Paragraph("- Actividad estimada actual: " + String.format("%.2f", actividadActual) + " MBq"));
        document.add(new Paragraph("- Clasificacion de riesgo: " + estado));
        document.add(new Paragraph(" "));

        // 3. Dibujo de la Curva de Decaimiento (Grafico)
        document.add(new Paragraph("REPRESENTACION VISUAL DEL DECAIMIENTO FISICO (400h):"));
        document.add(new Paragraph(" "));

        // Dejamos espacio para el grafico (se dibuja en coordenadas absolutas mas abajo)
        for(int i=0; i<10; i++) document.add(new Paragraph(" "));

        // Accedemos al lienzo para dibujar la curva
        PdfContentByte cb = writer.getDirectContent();
        dibujarGraficoDecaimiento(cb, treatment.getRadioisotopo(), treatment.getDosis());

        // 4. Firma y Sello
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Firma del facultativo responsable:"));
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Dr/a. " + treatment.getDoctor().getUser().getNombreCompleto()));
        document.add(new Paragraph("Especialista en Medicina Nuclear"));

        document.close();
    }

    private void dibujarGraficoDecaimiento(PdfContentByte cb, String isotopo, double dosisInicial) {
        float xBase = 100;
        float yBase = 350; // Ajustado para que quepa bien en el centro
        float ancho = 350;
        float alto = 150;

        // Ejes
        cb.setLineWidth(1.2f);
        cb.setRGBColorStroke(50, 50, 50);
        cb.moveTo(xBase, yBase);
        cb.lineTo(xBase + ancho, yBase); // Eje X
        cb.moveTo(xBase, yBase);
        cb.lineTo(xBase, yBase + alto); // Eje Y
        cb.stroke();

        // Vida media basada en el isotopo (sincronizado con PatientController)
        double tMed = (isotopo.contains("I-131") || isotopo.contains("Iodo")) ? 192.48 :
                (isotopo.contains("Lu-177") || isotopo.contains("Lutecio")) ? 159.36 :
                        (isotopo.contains("Co-60") || isotopo.contains("Cobalto")) ? 46164.0 : 1.0;

        cb.setRGBColorStroke(220, 20, 60); // Curva en rojo carmesi
        cb.setLineWidth(2f);

        // Dibujar la curva exponencial A = A0 * e^(-lambda * t)
        for (int t = 0; t <= 400; t += 2) {
            double act = dosisInicial * Math.pow(0.5, (double) t / tMed);
            float px = xBase + (float) ((t / 400.0) * ancho);
            float py = yBase + (float) ((act / dosisInicial) * alto);

            if (t == 0) cb.moveTo(px, py);
            else cb.lineTo(px, py);
        }
        cb.stroke();

        cb.setRGBColorFill(100, 100, 100);
    }
}