package com.leyou.auth.utils;

import java.io.File;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Created by RookieWangZhiWei on 2019/4/11.
 */
public class RsaUtils {

    public static PublicKey getPublicKey(String filename) throws Exception {
        byte[] bytes = readFile(filename);

        return getPublicKey(bytes);
    }

    public static PrivateKey getPrivateKey(String filename) throws Exception {
        byte[] bytes = readFile(filename);

        return getPrivateKey(bytes);
    }


    public static PublicKey getPublicKey(byte[] bytes) throws Exception {

        X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);

        KeyFactory factory = KeyFactory.getInstance("RSA");

        return factory.generatePublic(spec);
    }

    public static PrivateKey getPrivateKey(byte[] bytes) throws Exception {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePrivate(spec);

    }

    public static void generateKey(String publicKeyFilename, String privateKeyFilename, String secret) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        SecureRandom secureRandom = new SecureRandom(secret.getBytes());
        keyPairGenerator.initialize(1024, secureRandom);
        KeyPair keyPair = keyPairGenerator.genKeyPair();
        // 获取公钥并写出
        byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
        writeFile(publicKeyFilename, publicKeyBytes);
        // 获取私钥并写出
        byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();
        writeFile(privateKeyFilename, privateKeyBytes);
    }


    public static byte[] readFile(String filename) throws Exception {
        return Files.readAllBytes(new File(filename).toPath());
    }

    private static void writeFile(String destPath, byte[] bytes) throws Exception {


        File dest = new File(destPath);

        if (!dest.exists()) {
            dest.createNewFile();
        }

        Files.write(dest.toPath(), bytes);
    }


}
