package com.ceqb.SistemaVentaDispositivos2025.config;

import org.hashids.Hashids;
import org.springframework.stereotype.Component;

@Component
public class HashidsUtil {
    // Cambia "mi_llave_secreta" por cualquier palabra
    private final Hashids hashids = new Hashids("mi_llave_secreta", 8);

    public String encode(Long id) {
        return hashids.encode(id);
    }

    public Long decode(String hash) {
        long[] ids = hashids.decode(hash);
        return ids.length > 0 ? ids[0] : null;
    }
}
