package com.ceqb.SistemaVentaDispositivos2025.config;

import org.hashids.Hashids;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HashidsUtil {
    private final Hashids hashids;

    public HashidsUtil(
            @Value("${hashids.secret}") String secret,
            @Value("${hashids.min-length}") int minLength) {
        this.hashids = new Hashids(secret, minLength);
    }

    public String encode(Long id) {
        return hashids.encode(id);
    }

    public Long decode(String hash) {
        long[] ids = hashids.decode(hash);
        return ids.length > 0 ? ids[0] : null;
    }
}
