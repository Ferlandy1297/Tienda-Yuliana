package com.tienda.service;

import com.tienda.entity.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockAlertService {
    private static final Logger log = LoggerFactory.getLogger(StockAlertService.class);
    private final JavaMailSender mailSender;

    @Value("${app.alerts.admin-email:}")
    private String adminEmail;

    public StockAlertService(@Nullable JavaMailSender mailSender) {
        // JavaMailSender may not be configured; keep reference (may be a no-op proxy)
        this.mailSender = mailSender;
    }

    public void enviarAlertaStockBajo(List<Producto> productos, @Nullable String destinoOverride) {
        try {
            String to = (destinoOverride != null && !destinoOverride.isBlank()) ? destinoOverride : adminEmail;
            if (to == null || to.isBlank()) {
                log.warn("No se envió alerta de stock: admin-email no configurado");
                return;
            }
            if (mailSender == null) {
                log.warn("JavaMailSender no disponible; omitiendo envío de correo");
                return;
            }
            StringBuilder body = new StringBuilder("Productos con stock bajo:\n\n");
            for (Producto p : productos) {
                body.append("- ").append(p.getNombre()).append(" (" + (p.getCodigoBarras()==null?"":p.getCodigoBarras()) + ") ")
                        .append(" stock: ").append(p.getStockActual()).append("\n");
            }
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject("Alerta: Stock Bajo");
            msg.setText(body.toString());
            mailSender.send(msg);
            log.info("Alerta de stock bajo enviada a {} ({} productos)", to, productos.size());
        } catch (Exception e) {
            log.error("Error enviando alerta de stock bajo", e);
        }
    }
}

