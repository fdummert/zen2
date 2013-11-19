package de.zeos.cometd.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Digester {
    private final MessageDigest messageDigest;

    private final int iterations;

    public Digester(String algorithm, int iterations) {
        try {
            this.messageDigest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No such hashing algorithm", e);
        }

        this.iterations = iterations;
    }

    public String digest(String value) {
        byte[] bytes = value.getBytes();
        synchronized (this.messageDigest) {
            for (int i = 0; i < this.iterations; i++) {
                bytes = this.messageDigest.digest(bytes);
            }
            value = "";
            for (byte b : bytes) {
                value += String.format("%1$02x", b);
            }
            return value;
        }
    }
    
    public Object digestForJS(Object[] args) {
        return digest((String)args[0]);
    }
}
